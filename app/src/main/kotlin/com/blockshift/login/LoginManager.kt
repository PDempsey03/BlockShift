package com.blockshift.login

import android.util.Base64
import android.util.Log
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

internal object LoginManager {

    private val userNamesAndPasswords = mutableMapOf<String, String>()
    private val userNamesAndSaltValues = mutableMapOf<String, String>()

    const val MIN_PASSWORD_LENGTH = 8
    const val MIN_USERNAME_LENGTH = 4
    private const val HASH_FUNCTION = "PBKDF2WithHmacSha1"
    private const val HASH_LENGTH = 256
    private const val HASH_ITERATION_COUNT = 2048
    private const val SALT_LENGTH = 16
    private val hashFactory: SecretKeyFactory = SecretKeyFactory.getInstance(HASH_FUNCTION)

    // functionality from https://www.danielhugenroth.com/posts/2021_06_password_hashing_on_android/
    private fun hashPassword(password: String, salt: String): String {
        val spec = PBEKeySpec(password.toCharArray(), salt.toByteArray(), HASH_ITERATION_COUNT, HASH_LENGTH)
        return Base64.encodeToString(hashFactory.generateSecret(spec).encoded, Base64.NO_WRAP)
    }

    fun isValidUsername(username: String): Boolean {
        return username.length >= MIN_USERNAME_LENGTH
                && username.all{it.isLetterOrDigit()}
    }

    fun doesUsernameExist(username: String): Boolean {
        return userNamesAndPasswords.containsKey(username)
    }

    fun addUser(userName: String, password: String) {

        // generate user's unique salt string
        val saltValue = generateSaltString()
        Log.d("Login Manager", "Salt Value Generated $saltValue")

        val hashedPassword = hashPassword(password, saltValue)

        userNamesAndPasswords[userName] = hashedPassword // temporary solution
        userNamesAndSaltValues[userName] = saltValue // temporary solution
    }

    // functionality from https://codersee.com/kotlin-pbkdf2-secure-password-hashing/
    private fun generateSaltString(): String {
        val secureRandom = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        secureRandom.nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
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

    fun tryLogin(username: String, password: String): Boolean{
        if(!doesUsernameExist(username)) return false

        val saltValue = userNamesAndSaltValues.getOrDefault(username, "0")
        val hashedPassword = hashPassword(password, saltValue)
        return userNamesAndPasswords[username] == hashedPassword
    }
}