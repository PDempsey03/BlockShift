package com.blockshift.login

import android.util.Base64
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

internal object LoginManager {

    const val MIN_PASSWORD_LENGTH = 8
    const val MIN_USERNAME_LENGTH = 4
    private const val HASH_FUNCTION = "PBKDF2WithHmacSha1"
    private const val HASH_LENGTH = 256
    private const val HASH_ITERATION_COUNT = 2048
    private const val SALT_LENGTH = 16
    private val hashFactory: SecretKeyFactory = SecretKeyFactory.getInstance(HASH_FUNCTION)
    private lateinit var dataBaseUsers: CollectionReference

    init {
        loadUserDataBase()
    }

    private fun loadUserDataBase(){
        dataBaseUsers = FirebaseFirestore.getInstance().collection("users")
    }

    // functionality from https://www.danielhugenroth.com/posts/2021_06_password_hashing_on_android/
    private fun hashPassword(password: String, salt: String): String {
        val spec = PBEKeySpec(password.toCharArray(), salt.toByteArray(), HASH_ITERATION_COUNT, HASH_LENGTH)
        return Base64.encodeToString(hashFactory.generateSecret(spec).encoded, Base64.NO_WRAP)
    }

    private fun isValidUsername(username: String): Boolean {
        return username.length >= MIN_USERNAME_LENGTH
                && username.all{it.isLetterOrDigit()}
    }

    private fun doesUsernameExist(username: String, onResultCallback: (Boolean) -> Unit, onFailureCallback: (Exception) -> Unit) {
        dataBaseUsers.whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                onResultCallback(!documents.isEmpty)
            }
            .addOnFailureListener { exception ->
                onFailureCallback(exception)
            }
    }

    fun tryAddUser(username: String, password: String, successCallback: (AccountCreationResult) -> Unit, failureCallback: (Exception) -> Unit) {
        val validUsername = isValidUsername(username)
        val validPassword = isValidPassword(password)

        // don't need to check if username exists if either username or password was not even valid
        if(!validUsername) {
            successCallback(AccountCreationResult.INVALID_USERNAME)
            return
        }

        if(!validPassword) {
            successCallback(AccountCreationResult.INVALID_PASSWORD)
            return
        }

        // if valid username and password, proceed to check if username exists
        doesUsernameExist(username, {
            exists ->
                if (exists) {
                    successCallback(AccountCreationResult.USERNAME_TAKEN)
                } else {
                    // get salt value and hashed password to store in database
                    val saltValue = generateSaltString()
                    val hashedPassword = hashPassword(password, saltValue)

                    // instantiate class that firebase can store
                    val userData = UserLoginData(username, hashedPassword, saltValue)

                    // make the call to firestore to attempt to record the data
                    dataBaseUsers.add(userData)
                        .addOnSuccessListener {
                            successCallback(AccountCreationResult.SUCCESS)
                        }
                        .addOnFailureListener { exception ->
                            failureCallback(exception)
                        }
                }
        }, {
            exception -> failureCallback(exception)
        })
    }

    // functionality from https://codersee.com/kotlin-pbkdf2-secure-password-hashing/
    private fun generateSaltString(): String {
        val secureRandom = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        secureRandom.nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    private fun isValidPassword(password: String): Boolean {
        // check length
        if(password.length < MIN_PASSWORD_LENGTH) return false

        var containsDigit = false
        var containsUppercaseLetter = false

        // check contains at least one of each digit and capital letter
        password.forEach {
            if(it.isLetter() && it.isUpperCase()) containsUppercaseLetter = true
            if(it.isDigit()) containsDigit = true
        }

        return containsDigit && containsUppercaseLetter
    }

    fun tryLogin(username: String, password: String, successCallback: (Boolean) -> Unit, failureCallback: (Exception) -> Unit){
        dataBaseUsers.whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // no user found
                    successCallback(false)
                } else {
                    // non-empty, and unique usernames should only return 1 result
                    val userDocument = documents.documents[0]

                    // get the stored hashed password and salt to compare the entered password to
                    val storedHashedPassword = userDocument.getString("password")
                    val storedSalt = userDocument.getString("salt")

                    // null check just in case
                    if(storedSalt == null || storedHashedPassword == null) {
                        successCallback(false)
                        return@addOnSuccessListener
                    }

                    val enteredHashPassword = hashPassword(password, storedSalt)
                    successCallback(enteredHashPassword == storedHashedPassword)
                }
            }
            .addOnFailureListener { exception ->
                failureCallback(exception)
            }
    }
}

data class UserLoginData(val username: String, val password: String, val salt: String)

enum class AccountCreationResult{
    SUCCESS, INVALID_USERNAME, INVALID_PASSWORD, USERNAME_TAKEN
}