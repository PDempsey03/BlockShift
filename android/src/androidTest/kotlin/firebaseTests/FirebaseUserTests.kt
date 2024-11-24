package firebaseTests

import androidx.test.platform.app.InstrumentationRegistry
import com.blockshift.model.repositories.AccountCreationResult
import com.blockshift.model.repositories.UserAuthenticationData
import com.blockshift.model.repositories.UserData
import com.blockshift.model.repositories.UserRepository
import com.blockshift.model.repositories.UserTableNames
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.BeforeClass
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class FirebaseUserTests {

    @Test
    fun userExistence() {
        val username = "TestUsername"
        val password = "ExamplePassword1"

        val latch = CountDownLatch(1)
        var testResult: AccountCreationResult? = null

        // Add a user
        UserRepository.createUser(
            username,
            password,
            { accountCreationResult ->
                testResult = accountCreationResult
                latch.countDown()
            },
            { exception ->
                fail("User creation failed: ${exception.message}")
                latch.countDown()
            }
        )

        // wait for callback to be made
        latch.await(FIRESTORE_TIMEOUT_LENGTH, TimeUnit.SECONDS)
        assertEquals("Account Creation Result was $testResult", AccountCreationResult.SUCCESS, testResult)

        // check if user exists
        val existsLatch = CountDownLatch(1)
        var doesExist = false

        UserRepository.doesUserExist(
            username,
            { exists ->
                doesExist = exists
                existsLatch.countDown()
            },
            { exception ->
                fail("Check for user existence failed: ${exception.message}")
                existsLatch.countDown()
            }
        )

        // wait for existence callback
        existsLatch.await(FIRESTORE_TIMEOUT_LENGTH, TimeUnit.SECONDS)
        assertTrue(doesExist)
    }

    @Test
    fun usernameTaken() {
        val username = "TestUsername2"
        val password = "ExamplePassword2"
        val password2 = "ExamplePassword2Diff"

        val latch = CountDownLatch(1)
        var testResult: AccountCreationResult? = null

        // add user with given username
        UserRepository.createUser(
            username,
            password,
            { accountCreationResult ->
                testResult = accountCreationResult
                latch.countDown()
            },
            { exception ->
                fail("User creation failed: ${exception.message}")
                latch.countDown()
            }
        )

        // wait for callback to be made
        latch.await(FIRESTORE_TIMEOUT_LENGTH, TimeUnit.SECONDS)
        assertEquals("Account Creation Result was $testResult", AccountCreationResult.SUCCESS, testResult)

        val secondLatch = CountDownLatch(1)
        var secondTestResult: AccountCreationResult? = null

        // add another user with the same username
        UserRepository.createUser(
            username,
            password2,
            { accountCreationResult ->
                secondTestResult = accountCreationResult
                secondLatch.countDown()
            }, { exception ->
                fail("User creation threw exception on second user: ${exception.message}")
                secondLatch.countDown()
            }
        )

        // wait for callback to be made
        secondLatch.await(FIRESTORE_TIMEOUT_LENGTH, TimeUnit.SECONDS)
        assertEquals("Account creation result was $secondTestResult", AccountCreationResult.USERNAME_TAKEN, secondTestResult)
    }

    @Test
    fun updatePassword() {
        val username = "TestUsername3"
        val password = "ExamplePassword3"
        val newPassword = "NewExamplePassword3"

        val latch = CountDownLatch(1)
        var testResult: AccountCreationResult? = null

        UserRepository.createUser(
            username,
            password,
            { accountCreationResult ->
                testResult = accountCreationResult
                latch.countDown()
            },
            { exception ->
                fail("User creation failed: ${exception.message}")
                latch.countDown()
            }
        )

        latch.await(FIRESTORE_TIMEOUT_LENGTH, TimeUnit.SECONDS)
        assertEquals("User account creation was not successful", AccountCreationResult.SUCCESS, testResult)

        val passwordLatch = CountDownLatch(1)
        var passwordChangeSuccess = false
        UserRepository.updateUserPassword(
            username,
            password,
            newPassword,
            { success ->
                passwordChangeSuccess = success
                passwordLatch.countDown()
            },
            { exception ->
                passwordLatch.countDown()
                fail("Exception thrown in password change ${exception.message}")
            }
        )

        passwordLatch.await(FIRESTORE_TIMEOUT_LENGTH, TimeUnit.SECONDS)
        assertTrue("Password change was not successful", passwordChangeSuccess)
    }

    @Test
    fun updateDisplayName() {
        val username = "TestUsername4"
        val password = "ExamplePassword4"
        val newDisplayName = "TestDisplayName4"

        val latch = CountDownLatch(1)
        var testResult: AccountCreationResult? = null

        UserRepository.createUser(
            username,
            password,
            { accountCreationResult ->
                testResult = accountCreationResult
                latch.countDown()
            },
            { exception ->
                fail("User creation failed: ${exception.message}")
                latch.countDown()
            }
        )

        latch.await(FIRESTORE_TIMEOUT_LENGTH, TimeUnit.SECONDS)
        assertEquals("User account creation was not successful", AccountCreationResult.SUCCESS, testResult)

        val displayNameLatch = CountDownLatch(1)
        var displayNameChangeSuccess = false
        UserRepository.updateUserDisplayName(
            username,
            newDisplayName,
            { success ->
                displayNameChangeSuccess = success
                displayNameLatch.countDown()
            },
            { exception ->
                displayNameLatch.countDown()
                fail("Exception thrown in password change ${exception.message}")
            }
        )

        displayNameLatch.await(FIRESTORE_TIMEOUT_LENGTH, TimeUnit.SECONDS)
        assertTrue("Password change was not successful", displayNameChangeSuccess)
    }

    @Test
    fun deleteUser() {
        val username = "TestUsername5"
        val password = "ExamplePassword5"

        val latch = CountDownLatch(1)
        var testResult: AccountCreationResult? = null

        UserRepository.createUser(
            username,
            password,
            { accountCreationResult ->
                testResult = accountCreationResult
                latch.countDown()
            },
            { exception ->
                fail("User creation failed: ${exception.message}")
                latch.countDown()
            }
        )

        latch.await(FIRESTORE_TIMEOUT_LENGTH, TimeUnit.SECONDS)
        assertEquals("User account creation was not successful", AccountCreationResult.SUCCESS, testResult)

        val deleteLatch = CountDownLatch(1)
        var deletedAccount = false
        val userData = UserData(username, username)

        UserRepository.deleteUser(
            userData,
            { success ->
                deletedAccount = success
                deleteLatch.countDown()
            },
            { exception ->
                deleteLatch.countDown()
                fail("Exception thrown when attempting to delete user ${exception.message}")
            }
        )

        deleteLatch.await(FIRESTORE_TIMEOUT_LENGTH, TimeUnit.SECONDS)
        assertTrue("Account was not successfully deleted", deletedAccount)
    }

    @Test
    fun userAuthData() {
        val username = "TestUsername6"
        val password = "ExamplePassword6"

        val latch = CountDownLatch(1)
        var testResult: AccountCreationResult? = null

        UserRepository.createUser(
            username,
            password,
            { accountCreationResult ->
                testResult = accountCreationResult
                latch.countDown()
            },
            { exception ->
                fail("User creation failed: ${exception.message}")
                latch.countDown()
            }
        )

        latch.await(FIRESTORE_TIMEOUT_LENGTH, TimeUnit.SECONDS)
        assertEquals("User account creation was not successful", AccountCreationResult.SUCCESS, testResult)

        val addAuthLatch = CountDownLatch(1)
        val userAuthData = UserAuthenticationData("4hfeuiwhu4b", 3294823582311L)
        var addedAuthData = false

        UserRepository.addUserAuthToken(
            username,
            userAuthData,
            { success ->
                addedAuthData = success
                addAuthLatch.countDown()
            },
            { exception ->
                fail("Exception thrown when adding auth data ${exception.message}")
                addAuthLatch.countDown()
            })

        addAuthLatch.await(FIRESTORE_TIMEOUT_LENGTH, TimeUnit.SECONDS)
        assertTrue("Auth data was not added", addedAuthData)
    }

    companion object {

    /*
     * NOTE ON EMULATOR USE:
     * requires node.js and firebase-tools to be installed
     * node.js can be installed from internet and firebase-tools can be installed
     * from npm via 'npm install -g firebase-tools'
     *
     * run below command to allow for ui screen viewable at 'http://127.0.0.1:4000/firestore/default/data'
     * './android/src/androidTest/FirebaseEmulator/RunFirebaseEmulatorWindows.bat true'
     *
     * otherwise just run the below command for minimum testing support
     * './android/src/androidTest/FirebaseEmulator/RunFirebaseEmulatorWindows.bat'
     *
     * if not on windows, it is recommended to make a bash script or appropriate os script file
     * or you can run this: 'firebase emulators:start --only firestore'
     * which may create debug files in the base project directory
     *
     * You could just open the FirebaseEmulator directory and call either
     * 'firebase emulators:start' for full website access at the above link
     * or just 'firebase emulators:start --only firestore' for minimum testing support
     */

        private lateinit var firestore: FirebaseFirestore
        private const val FIRESTORE_HOST = "10.0.2.2"
        private const val FIRESTORE_PORT = 8080
        const val FIRESTORE_TIMEOUT_LENGTH = 10L
        var usingEmulator = false

        @JvmStatic
        @BeforeClass
        fun setUp() {

            firestore = FirebaseFirestore.getInstance()
            firestore.useEmulator(
                FIRESTORE_HOST,
                FIRESTORE_PORT
            )
            FirebaseApp.initializeApp(InstrumentationRegistry.getInstrumentation().targetContext)

            val latch = CountDownLatch(1)
            var connected = false

            // ensure that there is a connection to the emulator
            firestore.collection("dummyCollection")
                .document("dummy")
                .get()
                .addOnSuccessListener {
                    // ff this succeeds, then assume the firebase emulator is connected
                    connected = true
                    latch.countDown()
                }
                .addOnFailureListener {
                    // if it fails, then assume that there is no firebase emulator connected
                    connected = false
                    latch.countDown()
                }

            latch.await(FIRESTORE_TIMEOUT_LENGTH, TimeUnit.SECONDS)
            if(!connected) {
                fail("Firebase emulator must be running to run firebase tests")
            }
            usingEmulator = true
        }

        @JvmStatic
        @AfterClass
        fun cleanup() {

            // don't accidentally delete our whole users collection (again)
            if(!usingEmulator) return

            val latch = CountDownLatch(1)
            lateinit var documentsLatch: CountDownLatch

            firestore
                .collection(UserTableNames.USERS)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    documentsLatch = CountDownLatch(querySnapshot.size())
                    latch.countDown()
                    querySnapshot.documents.forEach {
                        it.reference.delete()
                            .addOnSuccessListener {
                                documentsLatch.countDown()
                            }
                            .addOnFailureListener {
                                documentsLatch.countDown()
                            }
                    }

                }
                .addOnFailureListener {
                    latch.countDown()
                }

            latch.await(FIRESTORE_TIMEOUT_LENGTH, TimeUnit.SECONDS)
            documentsLatch.await(3 * FIRESTORE_TIMEOUT_LENGTH, TimeUnit.SECONDS)
            assertTrue("Could not access User Collection", latch.count == 0L)
            assertTrue("Not all documents were deleted", documentsLatch.count == 0L)
        }
    }
}
