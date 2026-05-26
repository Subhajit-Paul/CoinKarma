package com.coinkarma.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.hasText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.coinkarma.app.data.profile.UserProfile
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val app = context.applicationContext as CoinKarmaApp
        runBlocking {
            // Clear database and insert a fresh profile with onboardingCompleted = false
            app.db.clearAllTables()
            app.db.profile().upsert(UserProfile(displayName = "Arjun", onboardingCompleted = false))
        }
    }

    @Test
    fun testOnboardingProfileNameValidation() {
        // Wait for splash screen animations (~2 seconds) to finish and onboarding Welcome step to display
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText("Continue")).fetchSemanticsNodes().isNotEmpty()
        }

        // We are on page 0 (WelcomeStep). Let's click Continue to go to page 1.
        composeTestRule.onNodeWithText("Continue").performClick()

        // Page 1 (AboutStep). Let's click Continue to go to page 2.
        composeTestRule.onNodeWithText("Continue").performClick()

        // Page 2 (PermissionsStep). Let's click Continue to go to page 3.
        composeTestRule.onNodeWithText("Continue").performClick()

        // Page 3 (SignInStep) - Skip to page 4
        composeTestRule.onNodeWithText("I'll do this later").performClick()

        // Page 4 (ProfileStep). The display name defaults to "Arjun". The continue button should be enabled.
        composeTestRule.onNodeWithText("Continue").assertIsEnabled()

        // Clear the name
        composeTestRule.onNodeWithText("Arjun").performTextClearance()

        // The name is empty now. Let's assert that the validation warning is displayed
        composeTestRule.onNodeWithText("Display name cannot be empty").assertIsDisplayed()

        // And the "Continue" button should be disabled
        composeTestRule.onNodeWithText("Continue").assertIsNotEnabled()

        // Type a valid name back in
        composeTestRule.onNodeWithText("").performTextInput("Test User")

        // The error should disappear and the continue button should be enabled
        composeTestRule.onNodeWithText("Continue").assertIsEnabled()
    }
}
