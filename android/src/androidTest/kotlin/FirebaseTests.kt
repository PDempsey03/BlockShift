import androidx.test.platform.app.InstrumentationRegistry
import com.blockshift.model.repositories.AccountCreationResult
import com.blockshift.model.repositories.UserRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.BeforeClass
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class FirebaseTests {

    @Test
    fun addAndCheckUserExistence() {
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

        @JvmStatic
        @BeforeClass
        fun setUp() {

            println("Going for global setup")
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
                    // ff it fails, then assume that there is no firebase emulator connected
                    connected = false
                    latch.countDown()
                }

            latch.await(FIRESTORE_TIMEOUT_LENGTH, TimeUnit.SECONDS)
            if(!connected) {
                fail("Firebase emulator must be running to run firebase tests")
            }
        }
    }
}
