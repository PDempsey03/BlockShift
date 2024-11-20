import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.blockshift.R
import com.blockshift.model.repositories.UserTableNames
import com.blockshift.ui.settings.SettingsActivity
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HighScoreTabTest {
    val activityRule = ActivityTestRule(SettingsActivity::class.java)


    @Before
    fun setUp() {
        Intents.init()
        //IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun tearDown() {
        Intents.release()
        //IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun testHighScoresDisplayed() {
        val intent = Intent(ApplicationProvider.getApplicationContext(),SettingsActivity::class.java).apply {
            putExtra(UserTableNames.USERNAME,"lcl")
            putExtra(UserTableNames.DISPLAY_NAME,"Guest")
        }

        //Launch caught intent to check correct display
        val scenario = ActivityScenario.launch<SettingsActivity>(intent)

        scenario.use {
            onView(withText("High Scores")).perform(click())

            onView(withId(R.id.high_scores_recycler_view))
                .check(matches(hasMinimumChildCount(1)))
        }
    }

    private fun waitForLoading(id: Int, timeout:Long = 500000L) {
        val startTime = System.currentTimeMillis()
        while(System.currentTimeMillis() - startTime < timeout) {
            try {
                onView(withId(id)).check(matches(hasMinimumChildCount(1)))
            } catch (e:Exception) {
                Thread.sleep(100)
            }
        }
    }
}
