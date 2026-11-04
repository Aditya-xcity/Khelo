package com.example.khelo

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {
    private fun View.tree(): Sequence<View> = sequence {
        yield(this@tree)
        if (this@tree is ViewGroup) for (i in 0 until childCount) yieldAll(getChildAt(i).tree())
    }

    private fun MainActivity.label(text: String): View =
        window.decorView.tree().first { it.isShown && it is TextView && it.text.toString() == text }

    private fun MainActivity.control(description: String): View =
        window.decorView.tree().last { it.isShown && it.contentDescription == description }

    @Test
    fun tabsStayInsideInsetsAndDraftSurvivesRecreation() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity {
                val decor = it.window.decorView
                val insets = checkNotNull(ViewCompat.getRootWindowInsets(decor))
                    .getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
                for (tab in listOf("Home", "Games", "Create", "Profile")) {
                    val view = it.control(tab)
                    val rect = Rect(0, 0, view.width, view.height)
                    (decor as ViewGroup).offsetDescendantRectToMyCoords(view, rect)
                    assertTrue("Unsafe tab bounds for $tab: $rect", rect.width() > 0 &&
                        rect.left >= insets.left && rect.right <= decor.width - insets.right &&
                        rect.top >= insets.top && rect.bottom <= decor.height - insets.bottom)
                }
                it.control("Create").performClick()
                val title = it.window.decorView.tree().filterIsInstance<EditText>().first()
                title.setText("Draft match")
                it.control("Profile").performClick()
                it.label(MockRepo.myProfile.name)
                it.control("Create").performClick()
                it.label("Draft match")
            }
            scenario.recreate()
            scenario.onActivity {
                it.label("Create Game")
                it.label("Draft match")
                it.onBackPressedDispatcher.onBackPressed()
                it.label("Find your game.")
            }
        }
    }

    @Test
    fun gameDetailsBackRestoresDateFilterAndNotificationsBackReturnsHome() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity {
                it.control("Notifications").performClick()
                it.label("Notifications")
                it.control("Back").performClick()
                it.label("Find your game.")
                it.control("Games").performClick()
                it.label("Tomorrow").performClick()
                val card = it.label("Badminton Doubles").parent as View
                card.performClick()
                it.label("Description")
                assertFalse(it.window.decorView.tree().any { view ->
                    view.isShown && view.contentDescription == "Games"
                })
                it.onBackPressedDispatcher.onBackPressed()
                it.label("Badminton Doubles")
                assertFalse(it.window.decorView.tree().any { view ->
                    view.isShown && view is TextView && view.text.toString() == "Sunday Cricket"
                })
            }
        }
    }
}
