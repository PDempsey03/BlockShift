package com.blockshift.model.repositories

import android.util.Log
import com.blockshift.ui.login.LoginManager
import com.blockshift.model.login.UserViewModel
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

object UserRepository {
    private lateinit var dataBaseUsers: CollectionReference

    private val TAG: String = javaClass.simpleName
    private var listeningUsername: String? = null
    private var listenerRegistration: ListenerRegistration? = null

    init{
        loadUserDataBase()
    }

    private fun loadUserDataBase(){
        dataBaseUsers = FirebaseFirestore.getInstance().collection(UserTableNames.USERS)
    }

    fun startListeningForUser(viewModel: UserViewModel) {
        // remove any previous potential user listeners
        listenerRegistration?.remove()
        listenerRegistration = null

        val userData = viewModel.currentUser.value ?: return

        val username = userData.username
        Log.d(TAG, "Registering to user $username")
        listenerRegistration = dataBaseUsers
            .whereEqualTo(UserTableNames.USERNAME, username)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                Log.d(TAG, "UPDATE TO USER $username")
                if (error != null) {
                    Log.e(TAG, "Error in listening to user changes", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    // Process the user data here
                    val newUserData = snapshot.documents[0].toObject(CompleteUserData::class.java)?.toUserData()
                    if(newUserData != null) viewModel.currentUser.value = newUserData
                }
            }
    }

    fun createUser(username: String, password: String, onSuccessCallback: (AccountCreationResult) -> Unit, onFailureCallback: (Exception) -> Unit) {
        // perform last checks to ensure valid credentials
        val validUsername = LoginManager.isValidUsername(username)
        val validPassword = LoginManager.isValidPassword(password)

        if(!validUsername) {
            onSuccessCallback(AccountCreationResult.INVALID_USERNAME)
        }

        if(!validPassword) {
            onSuccessCallback(AccountCreationResult.INVALID_PASSWORD)
            return
        }

        doesUserExist(username, { exists ->
            if(exists) {
                onSuccessCallback(AccountCreationResult.USERNAME_TAKEN)
                return@doesUserExist
            }

            // get salt value and hashed password to store in database
            val saltValue = LoginManager.generateSaltString()
            val hashedPassword = LoginManager.hashPassword(password, saltValue)

            // generate login data with the default display name being the username
            val userLoginData = CompleteUserData(username, username, hashedPassword, saltValue)

            dataBaseUsers
                .add(userLoginData)
                .addOnSuccessListener {
                    onSuccessCallback(AccountCreationResult.SUCCESS)
                }
                .addOnFailureListener(onFailureCallback)

        }, onFailureCallback)
    }

    fun updateUserPassword(username: String, oldPassword: String, newPassword: String, onSuccessCallback: (Boolean) -> Unit, onFailureCallback: (Exception) -> Unit) {
        dataBaseUsers
            .whereEqualTo(UserTableNames.USERNAME, username)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if(querySnapshot.isEmpty) {
                    onSuccessCallback(false)
                    Log.d(TAG, "Could not update user password, username doesn't exist")
                    return@addOnSuccessListener
                }

                val userDoc = querySnapshot.documents[0]
                val userDocRef = userDoc.reference

                // make sure the login data is not null
                val userLoginData = userDoc.toObject(CompleteUserData::class.java)
                if(userLoginData == null) {
                    onSuccessCallback(false)
                    return@addOnSuccessListener
                }

                // make sure the old password matches
                val oldSalt = userLoginData.salt
                val oldHashedPassword = LoginManager.hashPassword(oldPassword, oldSalt)
                if(oldHashedPassword != userLoginData.password) {
                    Log.d(TAG, "Old password did not match when updating password")
                    onSuccessCallback(false)
                    return@addOnSuccessListener
                }

                // get new password information
                val newSalt = LoginManager.generateSaltString()
                val newHashedPassword = LoginManager.hashPassword(newPassword, newSalt)

                // run a transaction as we don't want a case of just one value being stored but the other not
                FirebaseFirestore.getInstance().runTransaction { transaction ->
                    // Update the password and salt fields
                    transaction.update(userDocRef, UserTableNames.PASSWORD, newHashedPassword)
                    transaction.update(userDocRef, UserTableNames.SALT, newSalt)
                }
                    .addOnSuccessListener { onSuccessCallback(true) }
                    .addOnFailureListener(onFailureCallback)
            }
            .addOnFailureListener(onFailureCallback)
    }

    fun updateUserDisplayName(username: String, newDisplayName: String, onSuccessCallback: (Boolean) -> Unit, onFailureCallback: (Exception) -> Unit) {
        dataBaseUsers
            .whereEqualTo(UserTableNames.USERNAME, username)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if(querySnapshot.isEmpty) {
                    onSuccessCallback(false)
                    Log.d(TAG, "Could not update display name, username doesn't exist")
                    return@addOnSuccessListener
                }

                val userDoc = querySnapshot.documents[0].reference
                userDoc.update(UserTableNames.DISPLAY_NAME, newDisplayName)
                    .addOnSuccessListener { onSuccessCallback(true) }
                    .addOnFailureListener(onFailureCallback)

            }.addOnFailureListener(onFailureCallback)
    }

    fun deleteUser(userData: UserData, onSuccessCallback: (Boolean) -> Unit, onFailureCallback: (Exception) -> Unit) {
        dataBaseUsers.whereEqualTo(UserTableNames.USERNAME, userData.username)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if(querySnapshot.isEmpty) {
                    onSuccessCallback(false)
                    Log.d(TAG, "Could not delete user, username doesn't exist")
                    return@addOnSuccessListener
                }

                // get the unique reference to the user's doc
                val userDoc = querySnapshot.documents[0].reference

                userDoc
                    .delete()
                    .addOnSuccessListener{
                        Log.d(TAG, "Successfully deleted user")
                        onSuccessCallback(true)
                    }
                    .addOnFailureListener(onFailureCallback)
            }
            .addOnFailureListener(onFailureCallback)
    }

    fun doesUserExist(username: String, onSuccessCallback: (Boolean) -> Unit, onFailureCallback: (Exception) -> Unit) {
        dataBaseUsers
            .whereEqualTo(UserTableNames.USERNAME, username)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val exists = !querySnapshot.isEmpty
                Log.d(TAG, "User $username does${if (exists) "" else "n't"} exist")
                onSuccessCallback(exists)
            }
            .addOnFailureListener(onFailureCallback)
    }

    fun getCompleteUserData(username: String, onSuccessCallback: (CompleteUserData?) -> Unit, onFailureCallback: (Exception) -> Unit) {
        dataBaseUsers
            .whereEqualTo(UserTableNames.USERNAME, username)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]

                    val userLoginData = document.toObject(CompleteUserData::class.java)

                    onSuccessCallback(userLoginData)
                } else {
                    onSuccessCallback(null)
                }
            }
            .addOnFailureListener(onFailureCallback)
    }

    fun getUserAuthentication(username: String, onSuccessCallback: (UserAuthenticationData?) -> Unit, onFailureCallback: (Exception) -> Unit) {
        dataBaseUsers
            .whereEqualTo(UserTableNames.USERNAME, username)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if(querySnapshot.isEmpty) {
                    onSuccessCallback(null)
                    return@addOnSuccessListener
                }

                val userDoc = querySnapshot.documents[0]
                val authData = userDoc.get(UserTableNames.AUTHENTICATION)?.let {
                    userDoc.toObject(UserAuthenticationData::class.java)
                }

                onSuccessCallback(authData)
            }
            .addOnFailureListener(onFailureCallback)
    }

    fun addUserAuthToken(username: String, authData: UserAuthenticationData, onSuccessCallback: (Boolean) -> Unit, onFailureCallback: (Exception) -> Unit) {
        dataBaseUsers
            .whereEqualTo(UserTableNames.USERNAME, username)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if(querySnapshot.isEmpty) {
                    Log.d(TAG, "Could not add authentication, user not found")
                    onSuccessCallback(false)
                    return@addOnSuccessListener
                }

                // update the user's auth token
                val userDoc = querySnapshot.documents[0].reference
                userDoc.update(UserTableNames.AUTHENTICATION, authData)
                onSuccessCallback(true)
            }.addOnFailureListener(onFailureCallback)
    }
}

/*
 * Complete user data stores all potential information about a user
 */
data class CompleteUserData(
    val username: String = "",
    val displayname: String = "",
    val password: String = "",
    val salt: String = "",
    val authentication: UserAuthenticationData? = null
) {
    fun toUserData() : UserData {
        return UserData(username, displayname)
    }
}

/*
 * User data stores data used in the running of the app
 * while the user is logged in
 */
data class UserData(
    val username: String = "",
    val displayname: String = "",
)

/*
 * User authentication stores data needed for auto logging a user in on app open
 */
data class UserAuthenticationData(
    val authtoken: String = "",
    val authtokenexpiration: Long = 0
)

enum class AccountCreationResult{
    SUCCESS, INVALID_USERNAME, USERNAME_TAKEN, INVALID_PASSWORD
}

internal object UserTableNames{
    const val USERS = "users"
    const val USERNAME = "username"
    const val DISPLAY_NAME = "displayname"
    const val PASSWORD = "password"
    const val SALT = "salt"
    const val AUTHENTICATION = "authentication"
    const val AUTH_USERNAME = "authusername"
    const val AUTH_TOKEN = "authtoken"
    const val AUTH_TOKEN_EXPIRATION = "authtokenexpiration"
}