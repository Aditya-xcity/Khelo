package com.example.khelo

import androidx.appcompat.app.AppCompatActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ImageView
import android.content.res.Configuration
import android.content.res.ColorStateList
import android.widget.Toast

class MainActivity : AppCompatActivity(), KheloNav {

    private lateinit var contentContainer: FrameLayout
    private lateinit var bottomNav: LinearLayout
    private lateinit var navItemViews: List<LinearLayout>
    private val routes = arrayListOf("home")
    private var screenState = Bundle()
    private var imeVisible = false
    private var activeTab = 0
    private val backCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() = goBack()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.getStringArrayList("routes")?.let {
            routes.clear()
            routes.addAll(it.filter { route ->
                !route.contains(':') || MockRepo.games.any { game -> game.id == route.substringAfter(':') }
            })
            if (routes.isEmpty()) routes.add("home")
        }
        screenState = savedInstanceState?.getBundle("screens") ?: Bundle()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = android.os.Build.VERSION.SDK_INT >= 26
        }
        if (android.os.Build.VERSION.SDK_INT < 26) window.navigationBarColor = Palette.charcoal

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Palette.bg)
        }
        contentContainer = object : FrameLayout(this) {
            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                getChildAt(0)?.layoutParams?.width =
                    minOf(View.MeasureSpec.getSize(widthMeasureSpec), dp(640))
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }
        root.addView(contentContainer, LinearLayout.LayoutParams(MATCH, 0, 1f))
        bottomNav = buildBottomNav()
        root.addView(bottomNav, LinearLayout.LayoutParams(MATCH, WRAP))
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val safe = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or
                    WindowInsetsCompat.Type.ime()
            )
            view.setPadding(safe.left, safe.top, safe.right, safe.bottom)
            imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            updateNav()
            WindowInsetsCompat.CONSUMED
        }
        onBackPressedDispatcher.addCallback(this, backCallback)
        setContentView(root)
        render()
    }

    private fun state(route: String = routes.last()): Bundle =
        screenState.getBundle(route) ?: Bundle().also { screenState.putBundle(route, it) }

    private fun saveScroll() {
        findScroll()?.let { state().putInt("scroll", it.scrollY) }
    }

    private fun findScroll(): NestedScrollView? {
        val screen = contentContainer.getChildAt(0)
        return screen as? NestedScrollView ?: (screen as? LinearLayout)?.getChildAt(0) as? NestedScrollView
    }

    override fun onSaveInstanceState(outState: Bundle) {
        saveScroll()
        outState.putStringArrayList("routes", ArrayList(routes))
        outState.putBundle("screens", screenState)
        super.onSaveInstanceState(outState)
    }

    private fun navigate(route: String, tab: Boolean = false) {
        if (route == routes.last()) return
        saveScroll()
        WindowCompat.getInsetsController(window, window.decorView).hide(WindowInsetsCompat.Type.ime())
        if (tab) {
            routes.clear()
            routes.add("home")
        }
        if (routes.last() != route) routes.add(route)
        render()
    }

    override fun goBack() {
        if (routes.size == 1) {
            finish()
            return
        }
        saveScroll()
        routes.removeAt(routes.lastIndex)
        render()
    }

    private fun render() {
        val route = routes.last()
        val state = state()
        val game = MockRepo.games.firstOrNull { it.id == route.substringAfter(':') }
        val view = when {
            route == "games" -> Screens.games(this, this, state)
            route == "create" -> Screens.createGame(this, this, state)
            route == "profile" -> Screens.profile(this, this)
            route == "notifications" -> Screens.notifications(this, this)
            route.startsWith("details:") && game != null -> Screens.gameDetails(this, this, game)
            route.startsWith("created:") && game != null -> Screens.gameCreated(this, this, game)
            else -> Screens.home(this, this, state)
        }
        backCallback.isEnabled = routes.size > 1
        activeTab = listOf("home", "games", "create", "profile").indexOf(route)
        contentContainer.removeAllViews()
        contentContainer.addView(view, FrameLayout.LayoutParams(MATCH, MATCH, Gravity.CENTER_HORIZONTAL))
        findScroll()?.let { scroll ->
            scroll.descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
            scroll.isFocusableInTouchMode = true
            scroll.requestFocus()
            scroll.post { scroll.scrollTo(0, state.getInt("scroll")) }
        }
        refreshNavHighlight()
        updateNav()
    }

    private fun updateNav() {
        bottomNav.visibility = if (activeTab >= 0 && !imeVisible) View.VISIBLE else View.GONE
    }

    override fun showHome() {
        if (routes.last() == "home") {
            saveScroll()
            render()
        } else navigate("home", tab = true)
    }

    override fun showGames(sportId: String?) {
        if (sportId != null) {
            screenState.putBundle("games", Bundle().apply { putString("sport", sportId) })
        }
        navigate("games", tab = true)
    }

    override fun showGameDetails(game: Game) {
        val route = "details:${game.id}"
        if (routes.last() == route) {
            saveScroll()
            render()
        } else navigate(route)
    }

    override fun showCreateGame() = navigate("create")
    override fun showGameCreated(game: Game) {
        routes.removeAt(routes.lastIndex)
        screenState.remove("create")
        routes.add("created:${game.id}")
        render()
    }
    override fun showProfile() = navigate("profile", tab = true)
    override fun showNotifications() = navigate("notifications")

    override fun toggleJoin(game: Game) {
        if (game.joinedByMe) {
            game.joinedByMe = false
            game.currentPlayers = (game.currentPlayers - 1).coerceAtLeast(0)
            toast("You left ${game.title}")
        } else {
            if (game.spotsLeft <= 0) {
                toast("This game is full")
                return
            }
            game.joinedByMe = true
            game.currentPlayers += 1
            toast("You joined ${game.title}!")
        }
    }

    override fun confirmJoin(game: Game, onJoined: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Join ${game.title}?")
            .setMessage("${game.dateLabel} • ${game.timeLabel}\n${game.location}\n${game.spotsLeft} spots available")
            .setPositiveButton("JOIN GAME") { dialog, _ ->
                toggleJoin(game)
                onJoined()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    // ---------- Bottom navigation ----------

    private fun buildBottomNav(): LinearLayout {
        val nav = LinearLayout(this)
        nav.orientation = LinearLayout.HORIZONTAL
        nav.gravity = Gravity.CENTER
        nav.setPadding(0, dp(4), 0, dp(4))
        nav.background = Drawables.topRounded(Palette.card, dpf(20))
        nav.elevation = dpf(2)

        val items = listOf(
            Triple(R.drawable.ic_tab_home, "Home", 0),
            Triple(R.drawable.ic_tab_games, "Games", 1),
            Triple(R.drawable.ic_tab_create, "Create", 2),
            Triple(R.drawable.ic_tab_profile, "Profile", 3)
        )

        val landscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        navItemViews = items.map { (icon, label, index) ->
            val wrap = LinearLayout(this)
            wrap.orientation = if (landscape) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
            wrap.gravity = Gravity.CENTER
            wrap.minimumHeight = dp(if (landscape) 48 else 64)
            wrap.contentDescription = label
            wrap.background = Drawables.ripple(Palette.card, dpf(16))
            wrap.isClickable = true
            wrap.isFocusable = true

            val iconView = ImageView(this).apply {
                setImageResource(icon)
                scaleType = ImageView.ScaleType.CENTER
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }
            val labelView = TextView(this).apply {
                text = label
                textSize = 12f
                gravity = Gravity.CENTER
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }
            wrap.tag = Pair(iconView, labelView)
            wrap.addView(iconView, LinearLayout.LayoutParams(dp(if (landscape) 40 else 56), dp(32)))
            wrap.addView(labelView, LinearLayout.LayoutParams(WRAP, WRAP).apply { if (landscape) marginStart = dp(4) else topMargin = dp(2) })

            wrap.setOnClickListener {
                when (index) {
                    0 -> navigate("home", tab = true)
                    1 -> showGames(null)
                    2 -> navigate("create", tab = true)
                    3 -> showProfile()
                }
            }
            nav.addView(wrap, LinearLayout.LayoutParams(0, WRAP, 1f))
            wrap
        }

        refreshNavHighlightInternal()
        return nav
    }

    private fun refreshNavHighlight() {
        refreshNavHighlightInternal()
    }

    private fun refreshNavHighlightInternal() {
        navItemViews.forEachIndexed { index, wrap ->
            val tagPair = wrap.tag as Pair<*, *>
            val iconView = tagPair.first as ImageView
            val labelView = tagPair.second as TextView
            val selected = index == activeTab
            wrap.isSelected = selected
            iconView.background = if (selected) Drawables.rounded(Palette.primaryLight, dpf(24)) else null
            iconView.imageTintList = ColorStateList.valueOf(if (selected) Palette.primary else Palette.textSecondary)
            labelView.setTextColor(if (selected) Palette.primary else Palette.textSecondary)
            labelView.setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
        }
    }
}