package com.example.khelo

object MockRepo {

    val sports = listOf(
        Sport("cricket", "Cricket", "🏏"),
        Sport("football", "Football", "⚽"),
        Sport("badminton", "Badminton", "🏸"),
        Sport("tennis", "Tennis", "🎾"),
        Sport("basketball", "Basketball", "🏀"),
        Sport("volleyball", "Volleyball", "🏐")
    )

    fun sportById(id: String): Sport = sports.first { it.id == id }

    val games = mutableListOf(
        Game(
            id = "g1", sportId = "cricket", title = "Sunday Cricket", hostName = "Rahul",
            dateLabel = "Today", timeLabel = "6:00 PM", location = "Nehru Stadium",
            distanceKm = 2.4, currentPlayers = 7, maxPlayers = 11, skill = SkillLevel.INTERMEDIATE,
            description = "Looking for 4 more players for a friendly cricket game. Bring your own kit if possible."
        ),
        Game(
            id = "g2", sportId = "football", title = "Evening Football", hostName = "Aditya",
            dateLabel = "Today", timeLabel = "7:30 PM", location = "Green Park Ground",
            distanceKm = 3.1, currentPlayers = 8, maxPlayers = 10, skill = SkillLevel.ANY,
            description = "Casual 5-a-side football, all skill levels welcome."
        ),
        Game(
            id = "g3", sportId = "badminton", title = "Badminton Doubles", hostName = "Sneha",
            dateLabel = "Tomorrow", timeLabel = "5:00 PM", location = "Khelo Sports Complex",
            distanceKm = 1.1, currentPlayers = 3, maxPlayers = 4, skill = SkillLevel.BEGINNER,
            description = "Need 1 more player for doubles, beginner-friendly."
        ),
        Game(
            id = "g4", sportId = "basketball", title = "Saturday Basketball", hostName = "Karan",
            dateLabel = "Sat 12", timeLabel = "4:00 PM", location = "City Basketball Court",
            distanceKm = 4.5, currentPlayers = 6, maxPlayers = 8, skill = SkillLevel.ADVANCED,
            description = "Competitive half-court game, looking for 2 more solid players."
        ),
        Game(
            id = "g5", sportId = "tennis", title = "Friendly Tennis Doubles", hostName = "Priya",
            dateLabel = "Fri 11", timeLabel = "6:30 AM", location = "Riverside Tennis Club",
            distanceKm = 5.2, currentPlayers = 2, maxPlayers = 4, skill = SkillLevel.INTERMEDIATE,
            description = "Early morning doubles, need 2 more players."
        )
    )

    val players = listOf(
        PlayerProfile("p1", "Rahul", "Cricket", SkillLevel.INTERMEDIATE, 2.1, 18, "🧑"),
        PlayerProfile("p2", "Sneha", "Badminton", SkillLevel.BEGINNER, 1.4, 9, "👩"),
        PlayerProfile("p3", "Karan", "Basketball", SkillLevel.ADVANCED, 3.8, 32, "🧑‍🦱"),
        PlayerProfile("p4", "Priya", "Tennis", SkillLevel.INTERMEDIATE, 4.9, 15, "👩‍🦰")
    )

    val notifications = listOf(
        AppNotification("n1", "Rahul joined your cricket game.", "5m ago"),
        AppNotification("n2", "Your game starts in 2 hours.", "1h ago"),
        AppNotification("n3", "You joined Sunday Cricket.", "3h ago"),
        AppNotification("n4", "Only 1 spot remains in your game.", "1d ago"),
        AppNotification("n5", "Someone invited you to join a football game.", "2d ago")
    )

    val myProfile = PlayerProfile("me", "Aditya", "Cricket", SkillLevel.INTERMEDIATE, 0.0, 24, "🙂")
    const val myLocation = "Haridwar"
    var myGamesHosted = 8
}