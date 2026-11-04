package com.example.khelo

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
const val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT

object Palette {
    val primary = Color.parseColor("#08743D")
    val primaryDark = Color.parseColor("#085831")
    val primaryLight = Color.parseColor("#E6F7EC")
    val urgent = Color.parseColor("#9C390D")
    val urgentLight = Color.parseColor("#FFF1EA")
    val bg = Color.parseColor("#F3F7F3")
    val card = Color.WHITE
    val textPrimary = Color.parseColor("#1A1A1A")
    val textSecondary = Color.parseColor("#6B7280")
    val border = Color.parseColor("#E5E7EB")
    val charcoal = Color.parseColor("#2B2F33")
}

fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
fun Context.dpf(value: Int): Float = dp(value).toFloat()

object Drawables {
    fun rounded(fill: Int, radiusDp: Float, strokeColor: Int = 0, strokeWidthPx: Int = 0): GradientDrawable {
        val d = GradientDrawable()
        d.shape = GradientDrawable.RECTANGLE
        d.cornerRadius = radiusDp
        d.setColor(fill)
        if (strokeWidthPx > 0) d.setStroke(strokeWidthPx, strokeColor)
        return d
    }

    fun topRounded(fill: Int, radiusDp: Float): GradientDrawable {
        val d = GradientDrawable()
        d.shape = GradientDrawable.RECTANGLE
        d.cornerRadii = floatArrayOf(radiusDp, radiusDp, radiusDp, radiusDp, 0f, 0f, 0f, 0f)
        d.setColor(fill)
        return d
    }

    fun circle(fill: Int): GradientDrawable {
        val d = GradientDrawable()
        d.shape = GradientDrawable.OVAL
        d.setColor(fill)
        return d
    }

    fun ripple(fill: Int, radiusDp: Float, strokeColor: Int = 0, strokeWidthPx: Int = 0): RippleDrawable {
        val base = rounded(fill, radiusDp, strokeColor, strokeWidthPx)
        val mask = rounded(Color.WHITE, radiusDp)
        return RippleDrawable(ColorStateList.valueOf(Color.parseColor("#33000000")), base, mask)
    }
}

// ---------- Layout helpers ----------

fun Context.marginTop(v: Int = 0): LinearLayout.LayoutParams =
    LinearLayout.LayoutParams(MATCH, WRAP).apply { topMargin = dp(v) }

fun Context.marginStart(v: Int = 0): LinearLayout.LayoutParams =
    LinearLayout.LayoutParams(WRAP, WRAP).apply { leftMargin = dp(v) }

fun Context.marginEnd(v: Int = 0): LinearLayout.LayoutParams =
    LinearLayout.LayoutParams(WRAP, WRAP).apply { rightMargin = dp(v) }

// ---------- Reusable components ----------

fun Context.baseCard(radiusDp: Int = 18, paddingDp: Int = 16): LinearLayout = LinearLayout(this).apply {
    orientation = LinearLayout.VERTICAL
    setPadding(dp(paddingDp), dp(paddingDp), dp(paddingDp), dp(paddingDp))
    background = Drawables.rounded(Palette.card, dpf(radiusDp), Palette.border, dp(1))
    elevation = dpf(1)
}

fun Context.sectionTitle(text: String): TextView = TextView(this).apply {
    this.text = text
    textSize = 19f
    setTypeface(null, Typeface.BOLD)
    setTextColor(Palette.textPrimary)
}

fun Context.primaryButton(text: String, filled: Boolean = true, onClick: () -> Unit): TextView {
    val tv = TextView(this)
    tv.text = text
    tv.textSize = 15f
    tv.setTypeface(null, Typeface.BOLD)
    tv.gravity = Gravity.CENTER
    tv.setPadding(dp(20), dp(14), dp(20), dp(14))
    tv.minimumHeight = dp(48)
    tv.isClickable = true
    tv.isFocusable = true
    if (filled) {
        tv.background = Drawables.ripple(Palette.primary, dpf(24))
        tv.setTextColor(Color.WHITE)
    } else {
        tv.background = Drawables.ripple(Color.WHITE, dpf(24), Palette.primary, dp(2))
        tv.setTextColor(Palette.primary)
    }
    tv.setOnClickListener { onClick() }
    return tv
}

fun Context.filterChip(text: String, selected: Boolean, onClick: () -> Unit): TextView {
    val tv = TextView(this)
    tv.text = text
    tv.textSize = 13f
    tv.gravity = Gravity.CENTER
    tv.setPadding(dp(16), dp(9), dp(16), dp(9))
    tv.minimumHeight = dp(48)
    tv.isClickable = true
    tv.isFocusable = true
    tv.setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
    if (selected) {
        tv.background = Drawables.ripple(Palette.primary, dpf(20))
        tv.setTextColor(Color.WHITE)
    } else {
        tv.background = Drawables.ripple(Palette.card, dpf(20), Palette.border, dp(1))
        tv.setTextColor(Palette.textPrimary)
    }
    tv.setOnClickListener { onClick() }
    return tv
}

fun Context.statusBadge(text: String, bg: Int, fg: Int): TextView = TextView(this).apply {
    this.text = text
    textSize = 11.5f
    setTypeface(null, Typeface.BOLD)
    setTextColor(fg)
    setPadding(dp(10), dp(5), dp(10), dp(5))
    background = Drawables.rounded(bg, dpf(20))
}

fun Context.formInput(hint: String, multiline: Boolean = false): EditText = EditText(this).apply {
    this.hint = hint
    minimumHeight = dp(48)
    textSize = 14f
    setHintTextColor(Palette.textSecondary)
    setTextColor(Palette.textPrimary)
    setPadding(dp(16), dp(14), dp(16), dp(14))
    background = Drawables.rounded(Palette.card, dpf(14), Palette.border, dp(1))
    if (multiline) {
        minLines = 3
        gravity = Gravity.TOP or Gravity.START
        inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
    } else {
        setSingleLine(true)
    }
}

fun Context.searchBarView(hint: String): LinearLayout {
    val wrap = LinearLayout(this)
    wrap.orientation = LinearLayout.HORIZONTAL
    wrap.gravity = Gravity.CENTER_VERTICAL
    wrap.setPadding(dp(16), dp(10), dp(16), dp(10))
    wrap.background = Drawables.rounded(Palette.card, dpf(14), Palette.border, dp(1))
    wrap.elevation = dpf(1)

    wrap.addView(TextView(this).apply { text = "🔍"; textSize = 16f }, marginEnd(10))
    val edit = EditText(this).apply {
        this.hint = hint
        textSize = 15f
        setSingleLine(true)
        setBackgroundColor(Color.TRANSPARENT)
        setHintTextColor(Palette.textSecondary)
        setTextColor(Palette.textPrimary)
    }
    wrap.addView(edit, LinearLayout.LayoutParams(0, WRAP, 1f))
    return wrap
}

fun Context.sportTile(sport: Sport, selected: Boolean, onClick: () -> Unit): LinearLayout {
    val tile = LinearLayout(this)
    tile.orientation = LinearLayout.VERTICAL
    tile.gravity = Gravity.CENTER
    tile.isClickable = true
    tile.isFocusable = true
    tile.minimumWidth = dp(96)
    tile.setPadding(dp(12), dp(12), dp(12), dp(12))
    tile.background = Drawables.ripple(
        if (selected) Palette.primary else Palette.card,
        dpf(18),
        if (selected) Palette.primary else Palette.border,
        dp(1)
    )
    tile.elevation = dpf(if (selected) 3 else 1)

    val emoji = TextView(this).apply { text = sport.emoji; textSize = 26f; gravity = Gravity.CENTER }
    val name = TextView(this).apply {
        text = sport.name
        textSize = 12.5f
        gravity = Gravity.CENTER
        setTypeface(null, Typeface.BOLD)
        setTextColor(if (selected) Color.WHITE else Palette.textPrimary)
        setPadding(0, dp(6), 0, 0)
    }
    tile.addView(emoji)
    tile.addView(name)
    tile.setOnClickListener { onClick() }
    return tile
}

fun Context.gameCard(game: Game, onOpen: () -> Unit, onJoinToggle: () -> Unit): LinearLayout {
    val sport = MockRepo.sportById(game.sportId)
    val card = baseCard(radiusDp = 18, paddingDp = 16)
    card.isClickable = true
    card.isFocusable = true
    card.background = Drawables.ripple(Palette.card, dpf(18), Palette.border, dp(1))
    card.setOnClickListener { onOpen() }

    val topRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
    topRow.addView(TextView(this).apply {
        text = "${sport.emoji}  ${sport.name.uppercase()}"
        textSize = 12f
        setTypeface(null, Typeface.BOLD)
        setTextColor(Palette.textSecondary)
    }, LinearLayout.LayoutParams(0, WRAP, 1f))
    topRow.addView(
        statusBadge(
            if (game.spotsLeft > 0) "🟠 ${game.spotsLeft} SPOTS LEFT" else "FULL",
            if (game.spotsLeft > 0) Palette.urgentLight else Palette.border,
            if (game.spotsLeft > 0) Palette.urgent else Palette.textSecondary
        )
    )
    card.addView(topRow, marginTop(0))

    card.addView(TextView(this).apply {
        text = game.title
        textSize = 18f
        setTypeface(null, Typeface.BOLD)
        setTextColor(Palette.textPrimary)
    }, marginTop(6))

    card.addView(TextView(this).apply {
        text = "Hosted by ${game.hostName}  •  ${game.dateLabel} • ${game.timeLabel}"
        textSize = 12.5f
        setTextColor(Palette.textSecondary)
    }, marginTop(4))

    card.addView(TextView(this).apply {
        text = "📍 ${game.location}   •   ${game.distanceKm} km away"
        textSize = 12.5f
        setTextColor(Palette.textSecondary)
    }, marginTop(2))

    val row2 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
    row2.addView(TextView(this).apply {
        text = "${game.currentPlayers} / ${game.maxPlayers} Players"
        textSize = 13f
        setTypeface(null, Typeface.BOLD)
        setTextColor(Palette.textPrimary)
    }, LinearLayout.LayoutParams(0, WRAP, 1f))
    row2.addView(statusBadge(game.skill.label, Palette.primaryLight, Palette.primaryDark))
    card.addView(row2, marginTop(10))

    val joinBtn = primaryButton(if (game.joinedByMe) "JOINED ✓" else "JOIN GAME", filled = !game.joinedByMe) {
        onJoinToggle()
    }
    card.addView(joinBtn, marginTop(12))

    return card
}

fun Context.playerCard(player: PlayerProfile, onInvite: () -> Unit): LinearLayout {
    val card = baseCard(16, 14)
    card.orientation = LinearLayout.HORIZONTAL
    card.gravity = Gravity.CENTER_VERTICAL

    val avatar = TextView(this).apply {
        text = player.avatarEmoji
        textSize = 20f
        gravity = Gravity.CENTER
        background = Drawables.circle(Palette.primaryLight)
    }
    card.addView(avatar, LinearLayout.LayoutParams(dp(48), dp(48)).apply { rightMargin = dp(12) })

    val info = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    info.addView(TextView(this).apply {
        text = player.name
        textSize = 15f
        setTypeface(null, Typeface.BOLD)
        setTextColor(Palette.textPrimary)
    })
    info.addView(TextView(this).apply {
        text = "${player.sportName} • ${player.skill.label}"
        textSize = 12.5f
        setTextColor(Palette.textSecondary)
    }, marginTop(2))
    info.addView(TextView(this).apply {
        text = "${player.distanceKm} km away  •  ${player.gamesPlayed} games played"
        textSize = 11.5f
        setTextColor(Palette.textSecondary)
    }, marginTop(2))
    card.addView(info, LinearLayout.LayoutParams(0, WRAP, 1f))

    val inviteBtn = TextView(this).apply {
        text = "Invite"
        textSize = 13f
        setTypeface(null, Typeface.BOLD)
        setTextColor(Palette.primary)
        setPadding(dp(14), dp(8), dp(14), dp(8))
        background = Drawables.ripple(Color.WHITE, dpf(16), Palette.primary, dp(2))
        isClickable = true
        isFocusable = true
        setOnClickListener { onInvite() }
    }
    card.addView(inviteBtn)

    return card
}

fun infoRow(ctx: Context, icon: String, text: String): LinearLayout {
    val row = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
    row.addView(TextView(ctx).apply { this.text = icon; textSize = 16f }, ctx.marginEnd(10))
    row.addView(TextView(ctx).apply { this.text = text; textSize = 14f; setTextColor(Palette.textPrimary) })
    return row
}

fun statItem(ctx: Context, value: String, label: String): LinearLayout {
    val col = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER }
    col.addView(TextView(ctx).apply {
        text = value; textSize = 18f; setTypeface(null, Typeface.BOLD)
        setTextColor(Palette.primaryDark); gravity = Gravity.CENTER
    })
    col.addView(TextView(ctx).apply {
        text = label; textSize = 11f; setTextColor(Palette.textSecondary); gravity = Gravity.CENTER
    }, LinearLayout.LayoutParams(WRAP, WRAP).apply { topMargin = ctx.dp(2) })
    return col
}
fun Context.backButton(onClick: () -> Unit): TextView = TextView(this).apply {
    text = "←"
    textSize = 22f
    gravity = Gravity.CENTER
    minimumWidth = dp(48)
    minimumHeight = dp(48)
    contentDescription = "Back"
    setTextColor(Palette.primary)
    background = Drawables.ripple(Palette.primaryLight, dpf(24))
    isFocusable = true
    setOnClickListener { onClick() }
}
