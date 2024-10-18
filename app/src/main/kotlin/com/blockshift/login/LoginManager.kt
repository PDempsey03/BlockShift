package com.blockshift.login

import android.content.Context
import android.util.Base64
import android.util.Log
import com.blockshift.repositories.UserAuthenticationData
import com.blockshift.repositories.UserRepository
import com.blockshift.repositories.UserTableNames
import com.blockshift.settings.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

internal object LoginManager {

    const val MIN_PASSWORD_LENGTH = 8
    const val MAX_PASSWORD_LENGTH = 32
    const val MIN_USERNAME_LENGTH = 4
    const val MAX_USERNAME_LENGTH = 16
    private const val HASH_FUNCTION = "PBKDF2WithHmacSha1"
    private const val HASH_LENGTH = 256 // bits
    private const val HASH_ITERATION_COUNT = 2048
    private const val SALT_LENGTH = 16 // bytes
    private const val AUTH_TOKEN_LENGTH = 32 // bytes
    private const val AUTH_TOKEN_EXPIRATION_DURATION = 100000  // ms TODO: make longer time like days or weeks (100 seconds right now)
    private val hashFactory: SecretKeyFactory = SecretKeyFactory.getInstance(HASH_FUNCTION)
    private val TAG: String = javaClass.simpleName

    fun usernameMeetsLength(username: String): Boolean {
        return username.length in MIN_USERNAME_LENGTH..MAX_USERNAME_LENGTH
    }

    fun usernameMeetsOnlyAlphaNumeric(username: String): Boolean {
        return username.all{it.isLetterOrDigit()}
    }

    fun isValidUsername(username: String): Boolean {
        return usernameMeetsLength(username) && usernameMeetsOnlyAlphaNumeric(username)
    }

    fun passwordMeetsLength(password: String): Boolean {
        return password.length in MIN_PASSWORD_LENGTH .. MAX_PASSWORD_LENGTH
    }

    fun passwordMeetsDigit(password: String): Boolean{
        return password.any { it.isDigit() }
    }

    fun passwordMeetsUppercase(password: String): Boolean {
        return password.any{ it.isUpperCase() }
    }

    fun isValidPassword(password: String): Boolean {
        return passwordMeetsLength(password)
                && passwordMeetsDigit(password)
                && passwordMeetsUppercase(password)
    }

    // functionality from https://www.danielhugenroth.com/posts/2021_06_password_hashing_on_android/
    fun hashPassword(password: String, salt: String): String {
        val spec = PBEKeySpec(password.toCharArray(), salt.toByteArray(), HASH_ITERATION_COUNT, HASH_LENGTH)
        return Base64.encodeToString(hashFactory.generateSecret(spec).encoded, Base64.NO_WRAP)
    }

    // functionality from https://codersee.com/kotlin-pbkdf2-secure-password-hashing/
    fun generateSaltString(): String {
        val secureRandom = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        secureRandom.nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    fun tryLogin(username: String, password: String, successCallback: (Boolean) -> Unit, failureCallback: (Exception) -> Unit) {
        UserRepository.getUserLoginData(username, { userLoginData ->
            // null login data means the user doesn't exist
            if(userLoginData == null) {
                Log.d(TAG, "Failed to login, user doesn't exist")
                successCallback(false)
                return@getUserLoginData
            }

            // compared stored hashed password to the entered hashed password using same salt
            val salt = userLoginData.salt
            val storedHashedPassword = userLoginData.password
            val enteredHashedPassword = hashPassword(password, salt)
            val correctPassword = storedHashedPassword == enteredHashedPassword

            Log.d(TAG, "Login has ${if(correctPassword) "correct" else "incorrect"} password")
            successCallback(correctPassword)
        }, failureCallback)
    }

    fun tryAutoLogin(authUsername: String, authToken: String, successCallback: (Boolean) -> Unit, failureCallback: (Exception) -> Unit) {
        UserRepository.getUserAuthentication(authUsername, { storedAuthData ->
            if(storedAuthData == null) {
                Log.d(TAG, "no stored auth data for $authUsername")
                successCallback(false)
                return@getUserAuthentication
            }

            if(!isAuthDataBeforeExpiration(storedAuthData)){
                Log.d(TAG, "stored auth data is beyond expiration time $authUsername")
                successCallback(false)
                return@getUserAuthentication
            }

            // success is now whether the local auth token matches the firestore token
            successCallback(authToken == storedAuthData.authtoken)

        },  failureCallback)
    }

    fun registerAuthToken(username: String, context: Context) {
        // get data instance of user matching name
        UserRepository.getUserAuthentication(username, { userAuthData ->
            val currentUserAuthData: UserAuthenticationData
            var storeAuthLocally = true
            if(userAuthData == null || !isAuthDataBeforeExpiration(userAuthData)) {
                // create new authentication if old auth was null or is no longer valid
                currentUserAuthData = generateAuthData()
                UserRepository.addUserAuthToken(username, currentUserAuthData, { success ->
                    storeAuthLocally = success
                }, { exception ->
                    Log.e(TAG, "Error connecting to firebase", exception)
                })
            } else {
                currentUserAuthData = userAuthData
            }

            if(storeAuthLocally) {
                updateLocalAuthToken(username, currentUserAuthData.authtoken, context)
            }
        }, { exception ->
            Log.e(TAG, "Error accessing firebase", exception)
        })
    }

    fun unregisterLocalAuthToken(context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            val dataStore = SettingsDataStore.getInstance(context)

            dataStore.removeString(UserTableNames.AUTH_USERNAME)
            dataStore.removeString(UserTableNames.AUTH_TOKEN)
        }
    }

    private fun generateAuthData(): UserAuthenticationData {
        return UserAuthenticationData(generateAuthToken(),
            System.currentTimeMillis() + AUTH_TOKEN_EXPIRATION_DURATION)
    }

    private fun generateAuthToken(): String {
        val secureRandom = SecureRandom()
        val auth = ByteArray(AUTH_TOKEN_LENGTH)
        secureRandom.nextBytes(auth)
        return Base64.encodeToString(auth, Base64.NO_WRAP)
    }

    private fun isAuthDataBeforeExpiration(authData: UserAuthenticationData): Boolean {
        return authData.authtokenexpiration > System.currentTimeMillis()
    }

    private fun updateLocalAuthToken(username: String, authToken: String, context: Context){
        CoroutineScope(Dispatchers.Main).launch {
            val dataStore = SettingsDataStore.getInstance(context)
            dataStore.setString(UserTableNames.AUTH_USERNAME, username)
            dataStore.setString(UserTableNames.AUTH_TOKEN, authToken)
        }
    }
}