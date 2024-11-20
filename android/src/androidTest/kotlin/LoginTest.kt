import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.blockshift.ui.login.LoginActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.blockshift.R
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.blockshift.model.repositories.UserData
import com.blockshift.model.repositories.UserTableNames
import com.blockshift.ui.settings.SettingsActivity
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.hamcrest.Matchers.allOf

@RunWith(AndroidJUnit4::class)
class LoginTest {
    @get:Rule
    val activityRule = ActivityTestRule(LoginActivity::class.java)


    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testLoginLaunchesSettingsActivity() {
        //Test that playing as Guest launches the main page
        onView(withId(R.id.guestButton)).perform(click())
        intended(allOf(
            hasComponent(SettingsActivity::class.java.name),
            hasExtra(UserTableNames.USERNAME,"lcl"),
            hasExtra(UserTableNames.DISPLAY_NAME,"Guest")
        ))
    }
}
