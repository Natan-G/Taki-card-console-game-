class GameDisplay {
    fun showGameStart(topCard: Card) {
        println("Game starts! First card on table: $topCard")
    }

    fun showPlayerTurn(player: String) {
        println("\n--- ${player}'s Turn ---")
    }

    fun showSkippedTurn(playerName: String) {
        println("$playerName is skipped!")
    }

    fun showGameState(players: List<Player>, topCard: Card) {
        players.forEach { player ->
            print("${player.name}'s cards: ${player.getHandCards().joinToString(", ")} ")
            println(" (total " + player.getHandCards().size + ")")
        }
        println("the top card is $topCard")
    }

    fun showCardOptions(cards: List<Card>) {
        println("\nChoose a card to play (enter number):")
        cards.forEachIndexed { index, card ->
            println("${index + 1}: $card")
        }
    }

    fun showColorChoice(playerName: String) {
        println("\n$playerName, Choose a color:")
        CardColor.entries.filter { it != CardColor.NONE }
            .forEachIndexed { index, color ->
                println("${index + 1}: $color")
            }
    }

    fun showError(message: String) {
        println("Invalid choice. $message")
    }

    fun showDrawCard(playerName: String) {
        println("$playerName has no playable card, drawing one.")
        println("$playerName drew a card and their turn is over.")
    }

    fun showWinner(playerName: String) {
        println("$playerName wins the game!")
    }

    fun showTakiClose(playerName: String) {
        println("$playerName closed the TAKI!")
    }

    fun showNoPlayableCards(playerName: String) {
        println("$playerName has no more playable cards.")
    }

    fun showPlusTwo(playerName: String, drawCount: Int) {
        println("$playerName, you need to draw $drawCount cards or play a PLUS_2.")
    }

    fun showPlusTwoOptions(drawCount: Int) {
        println("You have PLUS_2 cards. Do you want to:")
        println("1: Draw $drawCount cards")
        println("2: Play a PLUS_2 card")
    }

    fun showCardPlayed(playerName: String, card: Card) {
        println("$playerName played $card")
    }

    fun showExtraTurn(playerName: String) {
        println("$playerName gets one extra turn!")
    }

    fun showKingPlayed(playerName: String) {
        println("\n$playerName played KING!")
        println("KING allows playing any card on the next turn, regardless of color or type.")
    }

    fun showBreakPlus3(playerName: String) {
        println("$playerName has BREAK_PLUS_3 card!")
    }

    fun showBreakPlus3Used(playerName: String) {
        println("$playerName used BREAK_PLUS_3 and blocked drawing 3 cards!")
    }

    fun showDrawCards(playerName: String, count: Int) {
        println("$playerName draws $count cards!")
    }

    fun showColorChanged(color: CardColor) {
        println("Color changed to $color")
    }

    fun showTakiTurnStart(playerName: String, color: CardColor) {
        println("\n${playerName}'s TAKI turn (color: $color)")
    }

    fun showTakiOptions(cards: List<Card>) {
        println("Choose a card to play or end TAKI:")
        cards.forEachIndexed { index, card ->
            println("${index + 1}: $card")
        }
        println("${cards.size + 1}: Close TAKI")
    }

    fun showYesNoPrompt(prompt: String) {
        println(prompt)
        println("Please type 'yes' or 'no':")
    }

    fun showInvalidYesNo() {
        println("Invalid input. Please type 'yes' or 'no'.")
    }

    fun showPlus3Draw(playerName: String) {
        println("${playerName} must draw 3 cards!")
    }

    fun showBreakPlus3Draw(playerName: String) {
        println("${playerName} must draw 3 cards due to BREAK_PLUS_3!")
    }

    fun showSuperTakiColor(color: CardColor) {
        println("SUPER_TAKI takes the color: $color")
    }
} 