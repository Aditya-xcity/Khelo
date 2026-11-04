

package com.example.khelo

/**
 * Every screen talks to the app only through this interface, so screens
 * stay decoupled from MainActivity and are easy to test or move into
 * Fragments / Compose later.
 */
interface KheloNav {
    fun goBack()
    fun showHome()
    fun showGames(sportId: String? = null)
    fun showGameDetails(game: Game)
    fun showCreateGame()
    fun showGameCreated(game: Game)
    fun showProfile()
    fun showNotifications()
    fun toggleJoin(game: Game)
    fun confirmJoin(game: Game, onJoined: () -> Unit)
    fun toast(msg: String)
}