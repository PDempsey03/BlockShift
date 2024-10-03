package com.blockshift.login

import android.content.Context
import android.util.Base64
import com.blockshift.settings.SettingsDataStore
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

internal object LoginManager {

    const val MIN_PASSWORD_LENGTH = 8
    const val MIN_USERNAME_LENGTH = 4
    private const val HASH_FUNCTION = "PBKDF2WithHmacSha1"
    private const val HASH_LENGTH = 256 // bits
    private const val HASH_ITERATION_COUNT = 2048
    private const val SALT_LENGTH = 16 // bytes
    private const val AUTH_TOKEN_LENGTH = 32 // bytes
    private const val AUTH_TOKEN_EXPIRATION_DURATION = 100000  // ms TODO: make longer time like days or weeks (100 seconds right now)
    private val hashFactory: SecretKeyFactory = SecretKeyFactory.getInstance(HASH_FUNCTION)
    private lateinit var dataBaseUsers: CollectionReference

    init {
        loadUserDataBase()
    }

    private fun loadUserDataBase(){
        dataBaseUsers = FirebaseFirestore.getInstance().collection(UserTableNames.USERS)
    }

    // functionality from https://www.danielhugenroth.com/posts/2021_06_password_hashing_on_android/
    private fun hashPassword(password: String, salt: String): String {
        val spec = PBEKeySpec(password.toCharArray(), salt.toByteArray(), HASH_ITERATION_COUNT, HASH_LENGTH)
        return Base64.encodeToString(hashFactory.generateSecret(spec).encoded, Base64.NO_WRAP)
    }

    fun isValidUsername(username: String): Boolean {
        return username.length >= MIN_USERNAME_LENGTH
                && username.all{it.isLetterOrDigit()}
    }

    private fun doesUsernameExist(username: String, onResultCallback: (Boolean) -> Unit, onFailureCallback: (Exception) -> Unit) {
        val userDoc = dataBaseUsers.document(username)

        userDoc.get()
            .addOnSuccessListener { document ->
                onResultCallback(document.exists())
            }
            .addOnFailureListener(onFailureCallback)
    }

    fun tryAddUser(username: String, password: String, confirmPassword: String,
                   successCallback: (AccountCreationResult) -> Unit, failureCallback: (Exception) -> Unit) {
        val validUsername = isValidUsername(username)
        val validPassword = isValidPassword(password)
        val passwordMatch = password == confirmPassword

        // exit if the basic criteria were not met
        if(!validUsername) {
            successCallback(AccountCreationResult.INVALID_USERNAME)
            return
        }
        else if(!validPassword) {
            successCallback(AccountCreationResult.INVALID_PASSWORD)
            return
        }
        else if(!passwordMatch) {
            successCallback(AccountCreationResult.PASSWORD_MISMATCH)
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
                    dataBaseUsers.document(username).set(userData)
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

    private fun generateAuthToken(): String {
        val secureRandom = SecureRandom()
        val auth = ByteArray(AUTH_TOKEN_LENGTH)
        secureRandom.nextBytes(auth)
        return Base64.encodeToString(auth, Base64.NO_WRAP)
    }

    fun registerAuthToken(username: String, context: Context) {
        // get data instance of user matching name
        val userDoc = dataBaseUsers.document(username)
        userDoc.get()
            .addOnSuccessListener {
                document ->
                if (document.exists()) {
                    var authToken = document.getString(UserTableNames.AUTH_TOKEN)
                    val authTokenExpirationTime = document.getLong(UserTableNames.AUTH_TOKEN_EXPIRATION)
                    val currentTime = System.currentTimeMillis()

                    if(authToken == null || authTokenExpirationTime == null || currentTime - authTokenExpirationTime > 0) {
                        // create auth token
                        authToken = generateAuthToken()

                        // first try to add time stamp for expiration
                        userDoc
                            .update(UserTableNames.AUTH_TOKEN_EXPIRATION, System.currentTimeMillis() + AUTH_TOKEN_EXPIRATION_DURATION)
                            .addOnSuccessListener {

                                // if successful in adding timestamp, then try to add auth token
                                userDoc
                                    .update(UserTableNames.AUTH_TOKEN, authToken)
                                    .addOnSuccessListener {
                                        // if successfully added to database, then store the auth token locally
                                        updateLocalAuthToken(username, authToken, context)
                                    }
                            }
                    } else {
                        // store auth token locally
                        updateLocalAuthToken(username, authToken, context)
                    }
                }
            }
    }

    suspend fun unregisterAuthToken(context: Context) {
        val settingsDataStore = SettingsDataStore.getInstance(context)

        settingsDataStore.removeString(UserTableNames.AUTH_USERNAME)
        settingsDataStore.removeString(UserTableNames.AUTH_TOKEN)
    }

    private fun updateLocalAuthToken(username: String, authToken: String, context: Context){
        CoroutineScope(Dispatchers.Main).launch {
            val dataStore = SettingsDataStore.getInstance(context)
            dataStore.setString(UserTableNames.AUTH_USERNAME, username)
            dataStore.setString(UserTableNames.AUTH_TOKEN, authToken)
        }
    }

    fun isValidPassword(password: String): Boolean {
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

    fun tryLogin(username: String, password: String, successCallback: (Boolean) -> Unit, failureCallback: (Exception) -> Unit) {
        val userDoc = dataBaseUsers.document(username)
        userDoc.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // get the stored hashed password and salt to compare the entered password to
                    val storedHashedPassword = document.getString(UserTableNames.PASSWORD)
                    val storedSalt = document.getString(UserTableNames.SALT)

                    // null check just in case
                    if(storedSalt == null || storedHashedPassword == null) {
                        successCallback(false)
                        return@addOnSuccessListener
                    }

                    val enteredHashPassword = hashPassword(password, storedSalt)
                    successCallback(enteredHashPassword == storedHashedPassword)
                } else {
                    // no user found
                    successCallback(false)
                }
            }
            .addOnFailureListener { exception ->
                failureCallback(exception)
            }
    }

    fun tryAutoLogin(authUsername: String, authToken: String, successCallback: (Boolean) -> Unit, failureCallback: (Exception) -> Unit) {
        val userDoc = dataBaseUsers.document(authUsername)
        userDoc.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val actualAuthToken = document.getString(UserTableNames.AUTH_TOKEN)
                    val authTokenExpirationTime = document.getLong(UserTableNames.AUTH_TOKEN_EXPIRATION)
                    val currentTime = System.currentTimeMillis()

                    if(actualAuthToken != null && authTokenExpirationTime != null
                        && authTokenExpirationTime - currentTime > 0 && authToken == actualAuthToken)  {
                        successCallback(true)
                    } else {
                        successCallback(false)
                    }
                } else {
                    // no user found
                    successCallback(false)
                }
            }
            .addOnFailureListener { exception ->
                failureCallback(exception)
            }
    }
}

data class UserLoginData(val displayname: String, val password: String, val salt: String)

enum class AccountCreationResult{
    SUCCESS, INVALID_USERNAME, USERNAME_TAKEN, INVALID_PASSWORD, PASSWORD_MISMATCH
}

internal object UserTableNames{
    const val USERS = "users"
    const val DISPLAY_NAME = "displayname"
    const val PASSWORD = "password"
    const val SALT = "salt"
    const val AUTH_USERNAME = "authusername"
    const val AUTH_TOKEN = "authtoken"
    const val AUTH_TOKEN_EXPIRATION = "authtokenexpiration"
}