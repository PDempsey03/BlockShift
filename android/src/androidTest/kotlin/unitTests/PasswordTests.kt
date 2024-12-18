package unitTests

import com.blockshift.ui.login.LoginManager
import org.junit.Test
import org.junit.Assert.*

class PasswordTests {

    @Test
    fun validPasswords() {
        val standardPassword = "wino325Aonr4n13rf"
        val minLengthPassword = "P1aaaaaa"
        val maxLengthPassword = "A2345t6781234567d3456t781234567A"
        val startLowercaseLetter = "a12eBoqu234"
        val startUppercaseLetter = "Z12eeoqu234A"
        val endUppercaseLetter = "a12eeoqu234A"
        val containsUppercaseLetter = "euh1Hveeq222"
        val startSymbol = "@12eeoqu234A"
        val endSymbol = "12eeoqu234A@"
        val containsSymbols = "12!eeo@qu%234A"
        val startDigit = "1eeoqFWuaAA"
        val endDigit = "WHeoevnb9s"
        val containsDigit = "wfqA4veeAF"

        val passwords = listOf(
            standardPassword,
            minLengthPassword,
            maxLengthPassword,
            startLowercaseLetter,
            startUppercaseLetter,
            endUppercaseLetter,
            containsUppercaseLetter,
            startSymbol,
            endSymbol,
            containsSymbols,
            startDigit,
            endDigit,
            containsDigit)

        // ensure all valid passwords are returning as valid
        for(password in passwords) {
            assert(LoginManager.isValidPassword(password)) {
                "password $password was found to be invalid"
            }
        }
    }

    @Test
    fun invalidPasswords() {
        val justShortLength = "Dw35gfU"
        val veryShortLength = "F1r"
        val justLongLength = "A234567812ik567812345678rt3456781"
        val veryLongLength = "123456781234567DJalde6781234567812345678123456781234567812345678123456781234567812345678"
        val noCapitalLetter = "cere36t3evgerg5ge"
        val noDigit = "dwDSewvnenAJJD"

        val passwords = listOf(
            justShortLength,
            veryShortLength,
            justLongLength,
            veryLongLength,
            noCapitalLetter,
            noDigit)

        // ensure all invalid passwords are returning as invalid
        for(password in passwords) {
            assert(!LoginManager.isValidPassword(password)) {
                "password $password was found to be valid"
            }
        }
    }

    @Test
    fun validPasswordHashingSameSalt() {
        val testPassword = "TestPassword123"
        val sameSalt = LoginManager.generateSaltString()

        val hashedPasswordOne = LoginManager.hashPassword(testPassword, sameSalt)
        val hashedPasswordTwo = LoginManager.hashPassword(testPassword, sameSalt)

        // ensure same password and salt generate same hash
        assertEquals("Password hashing on same password and salt didn't yield the same hash",
            hashedPasswordOne, hashedPasswordTwo)
    }

    @Test
    fun validPasswordHashingDifferentSalt() {
        val testPassword = "TestPassword123"
        val saltOne = LoginManager.generateSaltString()
        val saltTwo = LoginManager.generateSaltString()

        // ensure the salt values aren't the same
        assertNotEquals(saltOne, saltTwo)

        val hashedPasswordOne = LoginManager.hashPassword(testPassword, saltOne)
        val hashedPasswordTwo = LoginManager.hashPassword(testPassword, saltTwo)

        // ensure no 2 different salt values will generate the same hash
        assertNotEquals("Same password on different salts yielded the same hash", hashedPasswordOne, hashedPasswordTwo)
    }
}
