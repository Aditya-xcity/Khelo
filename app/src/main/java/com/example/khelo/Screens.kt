package com.example.khelo

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView

object Screens {

    private fun EditText.bindDraft(state: Bundle, key: String) = apply {
        setText(state.getString(key).orEmpty())
        doAfterTextChanged { state.putString(key, it.toString()) }
    }


    fun home(ctx: Context, nav: KheloNav, state: Bundle): View {
        val scroll = NestedScrollView(ctx).apply { isFillViewport = true }
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(ctx.dp(20), ctx.dp(24), ctx.dp(20), ctx.dp(20))
        }

        val header = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        header.addView(TextView(ctx).apply {
            text = "Khelo"; textSize = 26f; setTypeface(null, Typeface.BOLD); setTextColor(Palette.textPrimary)
        }, LinearLayout.LayoutParams(0, WRAP, 1f))

        val bell = TextView(ctx).apply {
            text = "🔔"; textSize = 18f; gravity = Gravity.CENTER
            contentDescription = "Notifications"
            setPadding(ctx.dp(10), ctx.dp(10), ctx.dp(10), ctx.dp(10))
            background = Drawables.circle(Palette.bg)
            isClickable = true; isFocusable = true
            setOnClickListener { nav.showNotifications() }
        }
        header.addView(bell, LinearLayout.LayoutParams(ctx.dp(48), ctx.dp(48)).apply { marginEnd = ctx.dp(8) })

        val profileIcon = TextView(ctx).apply {
            text = MockRepo.myProfile.avatarEmoji; textSize = 18f; gravity = Gravity.CENTER
            contentDescription = "Profile"
            background = Drawables.circle(Palette.primaryLight)
            isClickable = true; isFocusable = true
            setOnClickListener { nav.showProfile() }
        }
        header.addView(profileIcon, LinearLayout.LayoutParams(ctx.dp(48), ctx.dp(48)))
        root.addView(header, ctx.marginTop(0))

        val hero = ctx.baseCard(24, 20).apply {
            background = Drawables.rounded(Palette.primaryDark, ctx.dpf(24))
        }
        hero.addView(TextView(ctx).apply {
            text = "PLAY LOCAL  •  ${MockRepo.myLocation.uppercase()}"
            textSize = 12f
            letterSpacing = 0.08f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Palette.primaryLight)
        })
        hero.addView(TextView(ctx).apply {
            text = "Find your game."
            textSize = 30f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
        }, ctx.marginTop(8))
        hero.addView(TextView(ctx).apply {
            text = "Join nearby games or create your own."
            textSize = 15f
            setTextColor(Palette.primaryLight)
        }, ctx.marginTop(4))
        root.addView(hero, ctx.marginTop(16))
        val search = ctx.searchBarView("Search nearby games")
        val query = (search.getChildAt(1) as EditText).bindDraft(state, "query")
        root.addView(search, ctx.marginTop(16))

        root.addView(ctx.sectionTitle("Choose your sport"), ctx.marginTop(26))

        val sportScroll = HorizontalScrollView(ctx).apply { isHorizontalScrollBarEnabled = false }
        val sportRow = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL }
        MockRepo.sports.forEachIndexed { i, sport ->
            val tile = ctx.sportTile(sport, false) { nav.showGames(sport.id) }
            sportRow.addView(tile, LinearLayout.LayoutParams(WRAP, WRAP).apply {
                if (i != 0) marginStart = ctx.dp(10)
            })
        }
        sportScroll.addView(sportRow)
        root.addView(sportScroll, ctx.marginTop(14))

        val nearbyHeader = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        nearbyHeader.addView(ctx.sectionTitle("Games near you"), LinearLayout.LayoutParams(0, WRAP, 1f))
        nearbyHeader.addView(TextView(ctx).apply {
            minimumHeight = ctx.dp(48); gravity = Gravity.CENTER_VERTICAL
            text = "See all"; textSize = 13f; setTypeface(null, Typeface.BOLD); setTextColor(Palette.primary)
            isClickable = true; isFocusable = true
            setOnClickListener { nav.showGames(null) }
        })
        root.addView(nearbyHeader, ctx.marginTop(28))

        val nearby = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
        root.addView(nearby, ctx.marginTop(4))
        fun refreshNearby() {
            nearby.removeAllViews()
            val term = query.text.toString().trim()
            val games = MockRepo.games.filter { game ->
                listOf(game.title, game.location, game.hostName, MockRepo.sportById(game.sportId).name)
                    .any { it.contains(term, ignoreCase = true) }
            }.sortedByDescending { it.spotsLeft > 0 }.let { if (term.isEmpty()) it.take(4) else it }
            if (games.isEmpty()) {
                nearby.addView(ctx.baseCard().apply {
                    addView(ctx.sectionTitle("No matching games"))
                    addView(TextView(ctx).apply {
                        text = "Try a different title, sport, or venue."
                        setTextColor(Palette.textSecondary)
                    }, ctx.marginTop(4))
                }, ctx.marginTop(12))
            }
            games.forEach { game ->
                nearby.addView(
                    ctx.gameCard(game, onOpen = { nav.showGameDetails(game) },
                        onJoinToggle = { nav.toggleJoin(game); refreshNearby() }),
                    ctx.marginTop(12)
                )
            }
        }
        query.doAfterTextChanged { refreshNearby() }
        refreshNearby()

        val createPrompt = ctx.baseCard(20, 16).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            background = Drawables.rounded(Palette.charcoal, ctx.dpf(18))
        }
        val promptText = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
        promptText.addView(TextView(ctx).apply {
            text = "Can't find a game?"; textSize = 15f; setTypeface(null, Typeface.BOLD); setTextColor(Color.WHITE)
        })
        promptText.addView(TextView(ctx).apply {
            text = "Create one and find players in minutes."; textSize = 12.5f; setTextColor(Color.parseColor("#C7CBD1"))
        }, ctx.marginTop(2))
        createPrompt.addView(promptText, ctx.marginTop(0))
        createPrompt.addView(TextView(ctx).apply {
            minimumHeight = ctx.dp(48); gravity = Gravity.CENTER
            text = "+ Create"; textSize = 13f; setTypeface(null, Typeface.BOLD); setTextColor(Palette.charcoal)
            setPadding(ctx.dp(14), ctx.dp(10), ctx.dp(14), ctx.dp(10))
            background = Drawables.ripple(Color.WHITE, ctx.dpf(18))
            isClickable = true; isFocusable = true
            setOnClickListener { nav.showCreateGame() }
        }, ctx.marginTop(12))
        root.addView(createPrompt, ctx.marginTop(24))

        scroll.addView(root)
        return scroll
    }

    fun games(ctx: Context, nav: KheloNav, state: Bundle): View {
        val scroll = NestedScrollView(ctx).apply { isFillViewport = true }
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(ctx.dp(20), ctx.dp(24), ctx.dp(20), ctx.dp(20))
        }

        root.addView(TextView(ctx).apply {
            text = "Find a Game"; textSize = 24f; setTypeface(null, Typeface.BOLD); setTextColor(Palette.textPrimary)
        })
        root.addView(TextView(ctx).apply {
            text = "📍 Near ${MockRepo.myLocation}"; textSize = 13f; setTextColor(Palette.textSecondary)
        }, ctx.marginTop(4))

        val search = ctx.searchBarView("Search games")
        val query = (search.getChildAt(1) as EditText).bindDraft(state, "query")
        root.addView(search, ctx.marginTop(16))

        var selectedDate = state.getString("date") ?: "Any date"
        var updateResults: () -> Unit = {}
        val dateScroll = HorizontalScrollView(ctx).apply { isHorizontalScrollBarEnabled = false }
        val dateRow = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL }
        val dates = listOf("Any date") + MockRepo.games.map { it.dateLabel }.distinct()
        val dateChipViews = mutableListOf<TextView>()
        lateinit var refreshDateChips: () -> Unit
        dates.forEachIndexed { i, d ->
            val chip = ctx.filterChip(d, d == selectedDate) {
                selectedDate = d
                state.putString("date", d)
                refreshDateChips()
                updateResults()
            }
            dateChipViews.add(chip)
            dateRow.addView(chip, LinearLayout.LayoutParams(WRAP, WRAP).apply { if (i != 0) marginStart = ctx.dp(8) })
        }
        refreshDateChips = {
            dateChipViews.forEachIndexed { i, chip ->
                val sel = dates[i] == selectedDate
                chip.background = Drawables.ripple(if (sel) Palette.primary else Palette.card, ctx.dpf(20), Palette.border, ctx.dp(1))
                chip.setTextColor(if (sel) Color.WHITE else Palette.textPrimary)
                chip.setTypeface(null, if (sel) Typeface.BOLD else Typeface.NORMAL)
            }
        }
        dateScroll.addView(dateRow)
        root.addView(dateScroll, ctx.marginTop(18))

        var selectedSport: String? = state.getString("sport")
        val sportScroll = HorizontalScrollView(ctx).apply { isHorizontalScrollBarEnabled = false }
        val sportChipRow = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL }
        val filterOptions = listOf<Pair<String?, String>>(null to "All") + MockRepo.sports.map { it.id to "${it.emoji} ${it.name}" }

        val gamesListContainer = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }

        fun refreshGamesList() {
            gamesListContainer.removeAllViews()
            val term = query.text.toString().trim()
            val filtered = MockRepo.games.filter { game ->
                (selectedSport == null || game.sportId == selectedSport) &&
                    (selectedDate == "Any date" || game.dateLabel == selectedDate) &&
                    listOf(game.title, game.location, game.hostName, MockRepo.sportById(game.sportId).name)
                        .any { it.contains(term, ignoreCase = true) }
            }
            if (filtered.isEmpty()) {
                gamesListContainer.addView(TextView(ctx).apply {
                    text = "No games found for this filter yet.\nWhy not create one?"
                    textSize = 14f; setTextColor(Palette.textSecondary); gravity = Gravity.CENTER
                }, ctx.marginTop(30))
            } else {
                filtered.forEachIndexed { i, game ->
                    gamesListContainer.addView(
                        ctx.gameCard(game, onOpen = { nav.showGameDetails(game) }, onJoinToggle = { nav.toggleJoin(game); refreshGamesList() }),
                        ctx.marginTop(if (i == 0) 0 else 14)
                    )
                }
            }
        }

        fun refreshSportChips() {
            sportChipRow.removeAllViews()
            filterOptions.forEachIndexed { i, pair ->
                val (id, label) = pair
                val chip = ctx.filterChip(label, id == selectedSport) {
                    selectedSport = id
                    state.putString("sport", id)
                    refreshSportChips()
                    refreshGamesList()
                }
                sportChipRow.addView(chip, LinearLayout.LayoutParams(WRAP, WRAP).apply { if (i != 0) marginStart = ctx.dp(8) })
            }
        }
        refreshSportChips()
        sportScroll.addView(sportChipRow)
        root.addView(sportScroll, ctx.marginTop(12))

        root.addView(gamesListContainer, ctx.marginTop(20))
        updateResults = ::refreshGamesList
        query.doAfterTextChanged { refreshGamesList() }
        refreshGamesList()

        root.addView(ctx.primaryButton("+ CREATE GAME") { nav.showCreateGame() }, ctx.marginTop(24))

        scroll.addView(root)
        return scroll
    }

    fun gameDetails(ctx: Context, nav: KheloNav, game: Game): View {
        val outer = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Palette.bg) }
        val scroll = NestedScrollView(ctx).apply { isFillViewport = true }
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(ctx.dp(20), ctx.dp(24), ctx.dp(20), ctx.dp(20))
        }

        val sport = MockRepo.sportById(game.sportId)

        val topRow = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        topRow.addView(ctx.backButton(nav::goBack))
        topRow.addView(TextView(ctx).apply {
            text = "${sport.emoji} ${sport.name.uppercase()}"; textSize = 13f
            setTypeface(null, Typeface.BOLD); setTextColor(Palette.textSecondary)
        }, ctx.marginStart(6))
        root.addView(topRow)

        root.addView(TextView(ctx).apply {
            text = game.title; textSize = 26f; setTypeface(null, Typeface.BOLD); setTextColor(Palette.textPrimary)
        }, ctx.marginTop(10))

        root.addView(TextView(ctx).apply {
            text = "Hosted by ${game.hostName}"; textSize = 14f; setTextColor(Palette.textSecondary)
        }, ctx.marginTop(4))

        val infoCard = ctx.baseCard(18, 18)
        infoCard.addView(infoRow(ctx, "🗓", "${game.dateLabel}  •  ${game.timeLabel}"))
        infoCard.addView(infoRow(ctx, "📍", "${game.location}  •  ${game.distanceKm} km away"), ctx.marginTop(12))
        infoCard.addView(infoRow(ctx, "🎯", game.skill.label), ctx.marginTop(12))
        root.addView(infoCard, ctx.marginTop(20))

        val playersCard = ctx.baseCard(18, 18)
        val progRow = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        progRow.addView(TextView(ctx).apply {
            text = "${game.currentPlayers} / ${game.maxPlayers} Players"; textSize = 16f
            setTypeface(null, Typeface.BOLD); setTextColor(Palette.textPrimary)
        }, LinearLayout.LayoutParams(0, WRAP, 1f))
        progRow.addView(
            ctx.statusBadge(
                if (game.spotsLeft > 0) "${game.spotsLeft} spots available" else "Full",
                if (game.spotsLeft > 0) Palette.urgentLight else Palette.border,
                if (game.spotsLeft > 0) Palette.urgent else Palette.textSecondary
            )
        )
        playersCard.addView(progRow, ctx.marginTop(0))

        playersCard.addView(com.google.android.material.progressindicator.LinearProgressIndicator(ctx).apply {
            max = game.maxPlayers
            progress = game.currentPlayers
            trackThickness = ctx.dp(8)
            trackCornerRadius = ctx.dp(4)
            setIndicatorColor(Palette.primary)
            trackColor = Palette.primaryLight
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }, ctx.marginTop(12))
        root.addView(playersCard, ctx.marginTop(16))

        root.addView(ctx.sectionTitle("Description"), ctx.marginTop(24))
        root.addView(TextView(ctx).apply {
            text = game.description; textSize = 14f; setTextColor(Palette.textSecondary)
            setLineSpacing(ctx.dpf(3), 1f)
        }, ctx.marginTop(8))

        scroll.addView(root)
        outer.addView(scroll, LinearLayout.LayoutParams(MATCH, 0, 1f))

        val ctaBar = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(ctx.dp(16), ctx.dp(8), ctx.dp(16), ctx.dp(8))
            background = Drawables.topRounded(Palette.card, ctx.dpf(20))
            elevation = ctx.dpf(10)
        }
        val ctaBtn = ctx.primaryButton(if (game.joinedByMe) "JOINED ✓" else "JOIN GAME", filled = !game.joinedByMe) {
            if (!game.joinedByMe) {
                nav.confirmJoin(game) {
                    nav.showGameDetails(game)
                }
            } else {
                nav.toggleJoin(game)
                nav.showGameDetails(game)
            }
        }
        ctaBar.addView(ctaBtn)
        outer.addView(ctaBar)

        return outer
    }

    fun createGame(ctx: Context, nav: KheloNav, state: Bundle): View {
        val scroll = NestedScrollView(ctx).apply { isFillViewport = true }
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(ctx.dp(20), ctx.dp(24), ctx.dp(20), ctx.dp(20))
        }

        val topRow = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        topRow.addView(ctx.backButton(nav::goBack))
        topRow.addView(TextView(ctx).apply {
            text = "Create Game"; textSize = 20f; setTypeface(null, Typeface.BOLD); setTextColor(Palette.textPrimary)
        }, ctx.marginStart(4))
        root.addView(topRow)

        root.addView(ctx.sectionTitle("Choose Sport"), ctx.marginTop(20))
        var selectedSportId = state.getString("sport") ?: MockRepo.sports.first().id
        val sportScroll = HorizontalScrollView(ctx).apply { isHorizontalScrollBarEnabled = false }
        val sportRow = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL }
        val sportTiles = mutableListOf<Pair<String, LinearLayout>>()
        lateinit var refreshSportSel: () -> Unit
        MockRepo.sports.forEachIndexed { i, sport ->
            val tile = ctx.sportTile(sport, sport.id == selectedSportId) {
                selectedSportId = sport.id
                state.putString("sport", sport.id)
                refreshSportSel()
            }
            sportTiles.add(sport.id to tile)
            sportRow.addView(tile, LinearLayout.LayoutParams(WRAP, WRAP).apply { if (i != 0) marginStart = ctx.dp(10) })
        }
        refreshSportSel = {
            sportTiles.forEach { (id, tileView) ->
                val sel = id == selectedSportId
                tileView.background = Drawables.ripple(
                    if (sel) Palette.primary else Palette.card, ctx.dpf(18),
                    if (sel) Palette.primary else Palette.border, ctx.dp(1)
                )
                val nameTv = tileView.getChildAt(1) as TextView
                nameTv.setTextColor(if (sel) Color.WHITE else Palette.textPrimary)
            }
        }
        sportScroll.addView(sportRow)
        root.addView(sportScroll, ctx.marginTop(12))

        root.addView(ctx.sectionTitle("Game Title"), ctx.marginTop(24))
        val titleInput = ctx.formInput("e.g. Sunday Cricket").bindDraft(state, "title")
        root.addView(titleInput, ctx.marginTop(8))

        root.addView(ctx.sectionTitle("Date & Time"), ctx.marginTop(24))
        val dateInput = ctx.formInput("Date (e.g. Sunday)").bindDraft(state, "date")
        val timeInput = ctx.formInput("Time (e.g. 6:00 PM)").bindDraft(state, "time")
        root.addView(dateInput, ctx.marginTop(8))
        root.addView(timeInput, ctx.marginTop(8))

        root.addView(ctx.sectionTitle("Location"), ctx.marginTop(24))
        val locationInput = ctx.formInput("Search location or use current").bindDraft(state, "location")
        root.addView(locationInput, ctx.marginTop(8))
        root.addView(TextView(ctx).apply {
            text = "📍 Use current location"; textSize = 13f; setTypeface(null, Typeface.BOLD); setTextColor(Palette.primary)
            isClickable = true; isFocusable = true
            setPadding(0, ctx.dp(8), 0, 0)
            setOnClickListener { locationInput.setText(MockRepo.myLocation); nav.toast("Using current location") }
        })

        root.addView(ctx.sectionTitle("Players Required"), ctx.marginTop(24))
        var maxPlayers = state.getInt("players", 11)
        val playersRow = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val playersValue = TextView(ctx).apply {
            text = maxPlayers.toString(); textSize = 18f; setTypeface(null, Typeface.BOLD)
            setTextColor(Palette.textPrimary); gravity = Gravity.CENTER
        }
        fun stepperBtn(label: String, onClick: () -> Unit) = TextView(ctx).apply {
            text = label; textSize = 18f; setTypeface(null, Typeface.BOLD); gravity = Gravity.CENTER
            setTextColor(Palette.primary)
            background = Drawables.circle(Palette.primaryLight)
            isClickable = true; isFocusable = true
            setOnClickListener { onClick() }
        }
        val minusBtn = stepperBtn("−") { if (maxPlayers > 2) { maxPlayers--; state.putInt("players", maxPlayers); playersValue.text = maxPlayers.toString() } }
        val plusBtn = stepperBtn("+") { if (maxPlayers < 30) { maxPlayers++; state.putInt("players", maxPlayers); playersValue.text = maxPlayers.toString() } }
        playersRow.addView(minusBtn, LinearLayout.LayoutParams(ctx.dp(48), ctx.dp(48)))
        playersRow.addView(playersValue, LinearLayout.LayoutParams(0, WRAP, 1f))
        playersRow.addView(plusBtn, LinearLayout.LayoutParams(ctx.dp(48), ctx.dp(48)))
        root.addView(playersRow, ctx.marginTop(10))

        root.addView(ctx.sectionTitle("Skill Level"), ctx.marginTop(24))
        var selectedSkill = SkillLevel.valueOf(state.getString("skill") ?: SkillLevel.ANY.name)
        val skillRow = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL }
        val skillChips = mutableListOf<Pair<SkillLevel, TextView>>()
        lateinit var refreshSkillChips: () -> Unit
        SkillLevel.values().forEachIndexed { i, level ->
            val chip = ctx.filterChip(level.label, level == selectedSkill) {
                selectedSkill = level
                state.putString("skill", level.name)
                refreshSkillChips()
            }
            skillChips.add(level to chip)
            skillRow.addView(chip, LinearLayout.LayoutParams(WRAP, WRAP).apply { if (i != 0) marginStart = ctx.dp(8) })
        }
        refreshSkillChips = {
            skillChips.forEach { (level, chip) ->
                val sel = level == selectedSkill
                chip.background = Drawables.ripple(if (sel) Palette.primary else Palette.card, ctx.dpf(20), Palette.border, ctx.dp(1))
                chip.setTextColor(if (sel) Color.WHITE else Palette.textPrimary)
            }
        }
        val skillScroll = HorizontalScrollView(ctx).apply { isHorizontalScrollBarEnabled = false }
        skillScroll.addView(skillRow)
        root.addView(skillScroll, ctx.marginTop(10))

        root.addView(ctx.sectionTitle("Description (optional)"), ctx.marginTop(24))
        val descInput = ctx.formInput("Add any notes for players...", multiline = true).bindDraft(state, "desc")
        root.addView(descInput, ctx.marginTop(8))

        val createBtn = ctx.primaryButton("CREATE GAME") {
            val title = titleInput.text.toString().ifBlank { "${MockRepo.sportById(selectedSportId).name} Game" }
            val date = dateInput.text.toString().ifBlank { "Today" }
            val time = timeInput.text.toString().ifBlank { "6:00 PM" }
            val location = locationInput.text.toString().ifBlank { MockRepo.myLocation }
            val newGame = Game(
                id = "g${MockRepo.games.size + 1}", sportId = selectedSportId, title = title,
                hostName = MockRepo.myProfile.name, dateLabel = date, timeLabel = time, location = location,
                distanceKm = 0.0, currentPlayers = 1, maxPlayers = maxPlayers, skill = selectedSkill,
                description = descInput.text.toString().ifBlank { "No description provided." },
                status = GameStatus.HOSTING, joinedByMe = true
            )
            MockRepo.games.add(0, newGame)
            MockRepo.myGamesHosted++
            nav.showGameCreated(newGame)
        }
        root.addView(createBtn, ctx.marginTop(28))

        scroll.addView(root)
        return scroll
    }

    fun gameCreated(ctx: Context, nav: KheloNav, game: Game): View {
        val scroll = NestedScrollView(ctx).apply { isFillViewport = true }
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(ctx.dp(24), ctx.dp(24), ctx.dp(24), ctx.dp(24))
        }

        root.addView(ctx.backButton(nav::goBack), LinearLayout.LayoutParams(WRAP, WRAP))
        root.addView(TextView(ctx).apply { text = "🎉"; textSize = 48f; gravity = Gravity.CENTER }, ctx.marginTop(0))
        root.addView(TextView(ctx).apply {
            text = "Game created!"; textSize = 24f; setTypeface(null, Typeface.BOLD)
            setTextColor(Palette.textPrimary); gravity = Gravity.CENTER
        }, ctx.marginTop(12))
        root.addView(TextView(ctx).apply {
            text = "Your game is live. Invite players to fill the remaining spots."
            textSize = 13.5f; setTextColor(Palette.textSecondary); gravity = Gravity.CENTER
        }, ctx.marginTop(6))

        val summary = ctx.baseCard(18, 18)
        summary.addView(TextView(ctx).apply {
            text = game.title; textSize = 18f; setTypeface(null, Typeface.BOLD); setTextColor(Palette.textPrimary)
        })
        summary.addView(TextView(ctx).apply {
            text = "${game.dateLabel} • ${game.timeLabel}"; textSize = 13f; setTextColor(Palette.textSecondary)
        }, ctx.marginTop(6))
        summary.addView(TextView(ctx).apply {
            text = "📍 ${game.location}"; textSize = 13f; setTextColor(Palette.textSecondary)
        }, ctx.marginTop(2))
        summary.addView(TextView(ctx).apply {
            text = "${game.currentPlayers} / ${game.maxPlayers} Players  •  ${game.spotsLeft} spots left"
            textSize = 13.5f; setTypeface(null, Typeface.BOLD); setTextColor(Palette.primaryDark)
        }, ctx.marginTop(10))
        root.addView(summary, ctx.marginTop(24))

        root.addView(ctx.primaryButton("Invite Players") { nav.toast("Invite link copied!") }, ctx.marginTop(20))
        root.addView(ctx.primaryButton("Share Game", filled = false) { nav.toast("Share sheet opened") }, ctx.marginTop(10))
        root.addView(ctx.primaryButton("View Game", filled = false) { nav.showGameDetails(game) }, ctx.marginTop(10))

        scroll.addView(root)
        return scroll
    }

    fun profile(ctx: Context, nav: KheloNav): View {
        val scroll = NestedScrollView(ctx).apply { isFillViewport = true }
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(ctx.dp(20), ctx.dp(28), ctx.dp(20), ctx.dp(20))
        }
        val me = MockRepo.myProfile

        root.addView(TextView(ctx).apply {
            text = me.avatarEmoji; textSize = 40f; gravity = Gravity.CENTER
            background = Drawables.circle(Palette.primaryLight)
        }, LinearLayout.LayoutParams(ctx.dp(84), ctx.dp(84)).apply { gravity = Gravity.CENTER_HORIZONTAL })

        root.addView(TextView(ctx).apply {
            text = me.name; textSize = 22f; setTypeface(null, Typeface.BOLD)
            setTextColor(Palette.textPrimary); gravity = Gravity.CENTER
        }, ctx.marginTop(14))

        root.addView(TextView(ctx).apply {
            text = "📍 ${MockRepo.myLocation}"; textSize = 13f; setTextColor(Palette.textSecondary); gravity = Gravity.CENTER
        }, ctx.marginTop(4))

        val statsCard = ctx.baseCard(18, 18)
        statsCard.orientation = LinearLayout.HORIZONTAL
        statsCard.addView(statItem(ctx, me.gamesPlayed.toString(), "Games Played"), LinearLayout.LayoutParams(0, WRAP, 1f))
        statsCard.addView(statItem(ctx, MockRepo.myGamesHosted.toString(), "Games Hosted"), LinearLayout.LayoutParams(0, WRAP, 1f))
        statsCard.addView(statItem(ctx, MockRepo.games.count { it.joinedByMe }.toString(), "Upcoming"), LinearLayout.LayoutParams(0, WRAP, 1f))
        root.addView(statsCard, ctx.marginTop(24))

        root.addView(ctx.sectionTitle("My Sports"), ctx.marginTop(24))
        val sportsCard = ctx.baseCard(16, 16)
        sportsCard.addView(infoRow(ctx, "🏏", "Cricket — Intermediate"))
        sportsCard.addView(infoRow(ctx, "⚽", "Football — Beginner"), ctx.marginTop(10))
        root.addView(sportsCard, ctx.marginTop(10))

        root.addView(ctx.primaryButton("Edit Profile", filled = false) { nav.toast("Edit profile coming soon") }, ctx.marginTop(24))
        root.addView(ctx.primaryButton("My Games") { nav.toast("Showing your games") }, ctx.marginTop(10))
        root.addView(ctx.primaryButton("My Hosted Games", filled = false) { nav.toast("Showing hosted games") }, ctx.marginTop(10))

        root.addView(ctx.sectionTitle("Players near you"), ctx.marginTop(28))
        MockRepo.players.forEach { p ->
            root.addView(ctx.playerCard(p) { nav.toast("Invite sent to ${p.name}") }, ctx.marginTop(10))
        }

        scroll.addView(root)
        return scroll
    }

    fun notifications(ctx: Context, nav: KheloNav): View {
        val scroll = NestedScrollView(ctx).apply { isFillViewport = true }
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(ctx.dp(20), ctx.dp(24), ctx.dp(20), ctx.dp(20))
        }

        val topRow = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        topRow.addView(ctx.backButton(nav::goBack))
        topRow.addView(TextView(ctx).apply {
            text = "Notifications"; textSize = 20f; setTypeface(null, Typeface.BOLD); setTextColor(Palette.textPrimary)
        }, ctx.marginStart(4))
        root.addView(topRow)

        MockRepo.notifications.forEachIndexed { i, n ->
            val card = ctx.baseCard(14, 14)
            card.orientation = LinearLayout.HORIZONTAL
            card.gravity = Gravity.CENTER_VERTICAL
            card.addView(TextView(ctx).apply { text = "🔔"; textSize = 16f }, ctx.marginEnd(12))
            val col = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
            col.addView(TextView(ctx).apply { text = n.message; textSize = 13.5f; setTextColor(Palette.textPrimary) })
            col.addView(TextView(ctx).apply { text = n.timeAgo; textSize = 11.5f; setTextColor(Palette.textSecondary) },
                LinearLayout.LayoutParams(WRAP, WRAP).apply { topMargin = ctx.dp(2) })
            card.addView(col, LinearLayout.LayoutParams(0, WRAP, 1f))
            root.addView(card, LinearLayout.LayoutParams(MATCH, WRAP).apply { topMargin = if (i == 0) ctx.dp(20) else ctx.dp(10) })
        }

        scroll.addView(root)
        return scroll
    }
}