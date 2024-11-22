import android.content.Context
import android.icu.util.TimeUnit
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.blockshift.model.db.AppDatabase
import com.blockshift.model.db.HighScore
import com.blockshift.model.db.HighScoreDao
import com.blockshift.model.db.User
import com.blockshift.model.db.UserDao
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeoutException
import com.blockshift.ui.mainpage.HomePageFragment
import com.blockshift.ui.mainpage.getUserLevelProgress

@RunWith(AndroidJUnit4::class)
class RoomDBTest {

    private lateinit var userDao: UserDao
    private lateinit var highScoreDao: HighScoreDao
    private lateinit var db: AppDatabase

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        userDao = db.userDao()
        highScoreDao = db.highScoreDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testWriteAndUpdateUser() = runTest {
        val testUser = User("testUser","testUser")
        userDao.insert(testUser)

        val retrieved = userDao.findByID(testUser.username)
        assert(retrieved == testUser)

        val updateUser = User("testUser","newDisplayName")
        userDao.update(updateUser)

        val retrievedUpdated = userDao.findByID(testUser.username)
        assert(retrievedUpdated == updateUser)
    }

    @Test
    fun testDeleteUser() = runTest {
        val testUser = User("testUser","testUser")
        userDao.insert(testUser)

        userDao.delete(testUser)

        val retrieved = userDao.findByID(testUser.username)
        assertNull(retrieved)
    }

    @Test
    fun testWriteAndUpdateHighScore() = runBlocking {
        val testUser = User("testUser","testUser")
        userDao.insert(testUser)

        val testHS = HighScore(testUser.username,1,5,2,3)
        highScoreDao.insert(testHS)

        val retrieved = highScoreDao.getHighScoreByLevel(testHS.username,1).getOrAwaitValue()
        assert(retrieved == testHS)

        val newTestHS = HighScore(testUser.username,1,7,6,5)
        highScoreDao.update(newTestHS)

        val retrievedUpdate = highScoreDao.getHighScoreByLevel(testHS.username,1).getOrAwaitValue()
        assert(retrievedUpdate == newTestHS)
    }

    @Test
    fun testDeleteHighScore() = runBlocking {
        val testUser = User("testUser","testUser")
        userDao.insert(testUser)

        val testHS = HighScore(testUser.username,1,5,2,3)
        highScoreDao.insert(testHS)

        highScoreDao.delete(testHS)

        val retrieved = highScoreDao.getHighScoreByLevel(testUser.username,1).getOrAwaitValue()
        assertNull(retrieved)
    }

    @Test
    fun testGetUserLevelProgress() = runTest {
        val testUser = User("testUser","testUser")
        userDao.insert(testUser)

        val testHS = HighScore(testUser.username,1,5,2,3)
        highScoreDao.insert(testHS)

        highScoreDao.insert(HighScore(testUser.username,2, Int.MAX_VALUE,Long.MAX_VALUE,Int.MAX_VALUE))

        val scores = highScoreDao.getHighScores(testUser.username).getOrAwaitValue()

        val nextLevel = getUserLevelProgress(scores)

        assert(nextLevel == 2)

    }
}

fun <T> LiveData<T>.getOrAwaitValue(
    time: Long = 2,
    timeUnit: java.util.concurrent.TimeUnit = java.util.concurrent.TimeUnit.SECONDS
): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(value: T) {
            data = value
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }

    this.observeForever(observer)

    // Don't wait indefinitely if the LiveData is not set.
    if (!latch.await(time, timeUnit)) {
        throw TimeoutException("LiveData value was never set.")
    }

    @Suppress("UNCHECKED_CAST")
    return data as T
}
