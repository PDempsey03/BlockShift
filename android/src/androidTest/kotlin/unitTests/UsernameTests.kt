package unitTests

import com.blockshift.ui.login.LoginManager
import org.junit.Test

class UsernameTests {
    @Test
    fun validUsernames() {
        val standardUsername = "username"
        val startUppercase = "Username"
        val endUppercase = "usernamE"
        val containsUppercase = "useRname"
        val startsDigit = "1username"
        val endsDigit = "username9"
        val containsDigit = "user5name"

        val usernames = listOf(
            standardUsername,
            startUppercase,
            endUppercase,
            containsUppercase,
            startsDigit,
            endsDigit,
            containsDigit
        )

        // ensure all valid usernames are returning as valid
        for(username in usernames) {
            assert(LoginManager.isValidUsername(username)) {
                "username $username was found to be invalid"
            }
        }
    }

    @Test
    fun invalidUsernames() {
        val justShortLength = "pat"
        val veryShortLength = "p"
        val justLongLength = "thisIsJustTooLong"
        val veryLongLength = "ThisIsWayToLongToBeAUsernameButWhyNotTry"
        val containsNonAlphanumericSpace = "This hasASpace"
        val containsNonAlphanumericSymbol = "ThisIsAn@"

        val usernames = listOf(
            justShortLength,
            veryShortLength,
            justLongLength,
            veryLongLength,
            containsNonAlphanumericSpace,
            containsNonAlphanumericSymbol
        )

        // ensure all invalid usernames are returning as invalid
        for(username in usernames) {
            assert(!LoginManager.isValidUsername(username)){
                "username $username was found to be valid"
            }
        }
    }
}
