package com.example.khelo

enum class SkillLevel(val label: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced"),
    ANY("Any Level")
}

enum class GameStatus { OPEN, JOINED, HOSTING, COMPLETED, CANCELLED }

data class Sport(
    val id: String,
    val name: String,
    val emoji: String
)

data class PlayerProfile(
    val id: String,
    val name: String,
    val sportName: String,
    val skill: SkillLevel,
    val distanceKm: Double,
    val gamesPlayed: Int,
    val avatarEmoji: String
)

data class Game(
    val id: String,
    val sportId: String,
    val title: String,
    val hostName: String,
    val dateLabel: String,
    val timeLabel: String,
    val location: String,
    val distanceKm: Double,
    var currentPlayers: Int,
    val maxPlayers: Int,
    val skill: SkillLevel,
    val description: String,
    var status: GameStatus = GameStatus.OPEN,
    var joinedByMe: Boolean = false
) {
    val spotsLeft: Int get() = (maxPlayers - currentPlayers).coerceAtLeast(0)
}

data class AppNotification(
    val id: String,
    val message: String,
    val timeAgo: String
)