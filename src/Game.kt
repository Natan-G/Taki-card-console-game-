class Game {
    private val deck = Deck()
    private val players = mutableListOf<Player>()
    private val display = GameDisplay()

    private var currentPlayerIndex = 0
    private var isDirectionReversed = false
    private var currentTopCard: Card
    private var lastColoredCard: Card? = null
    private var lastValidColor: CardColor = CardColor.NONE

    init {
        initializePlayers()
        dealInitialCards()
        currentTopCard = drawStartingCard() // Ensures it's a number card
    }

    private fun initializePlayers() {
        for (i in 1..4) {
            players.add(Player("Player $i"))
        }
    }

    private fun dealInitialCards() {
        repeat(8) {
            for (player in players) {
                player.drawCard(deck)
            }
        }
    }

    private fun drawStartingCard(): Card {
        var card: Card?
        do {
            card = deck.drawCard()
            if (card != null && card.type != CardType.NUMBER) {
                deck.returnCard(card)  // Return non-number card back to deck
            }
        } while (card == null || card.type != CardType.NUMBER) // Ensure it's a regular number card
        return card
    }

    private data class TakiState(
        var currentPlayer: Player,
        var color: CardColor,
        var isActive: Boolean = true,
        var lastPlayedNumber: Int? = null,
        var lastPlayedCard: Card? = null
    )

    private data class PlusTwoState(
        var currentPlayer: Player,
        var drawCount: Int
    )

    fun start() {
        display.showGameStart(currentTopCard)
        while (true) {  // Main game loop
            val currentPlayer = players[currentPlayerIndex]
            display.showPlayerTurn(currentPlayer.name)
            display.showGameState(players, currentTopCard)

            if (currentPlayer.isTurnSkipped) {
                display.showSkippedTurn(currentPlayer.name)
                currentPlayer.isTurnSkipped = false
                nextTurn()
                continue
            }

            // Check if player has any valid cards to play
            if (currentPlayer.canPlayCard(currentTopCard)) {
                val cardToPlay = chooseCardToPlay(currentPlayer)
                playCard(currentPlayer, cardToPlay)
            } else { // Player take card from deck
                display.showDrawCard(currentPlayer.name)
                currentPlayer.drawCard(deck)
                currentPlayer.canPlayAgain = false
                nextTurn()
                continue
            }

            // Check if the current player won the game
            if (currentPlayer.hasWon()) {
                display.showWinner(currentPlayer.name)
                break
            }

            // Move to next player if current player doesn't have an extra turn
            if (!currentPlayer.canPlayAgain) {
                nextTurn()
            }
        }
    }

    /**
     * Moves the game to the next player's turn.
     * Considers:
     * - Direction of play (normal/reversed)
     * - Extra turns (PLUS, KING)
     * - Skipped turns (STOP)
     */
    private fun nextTurn() {
        val currentPlayer = players[currentPlayerIndex]

        if (currentPlayer.canPlayAgain) {
            currentPlayer.canPlayAgain = false
            return // Don't move to the next player yet
        }

        // Move to next player
        currentPlayerIndex = if (isDirectionReversed) {
            (currentPlayerIndex - 1 + players.size) % players.size
        } else {
            (currentPlayerIndex + 1) % players.size
        }
    }

    /**
     * Allows player to choose a card to play from their valid options.
     * Considers:
     * - Card matching rules (color/number/type)
     * - KING effect (any card allowed)
     * - Drawing when no valid plays
     */
    private fun chooseCardToPlay(player: Player): Card {
        while (true) {
            val validCards = getValidCardsForPlay(player)
            if (validCards.isEmpty()) {
                return handleNoValidCards(player)
            }
            
            showCardOptions(validCards)
            val chosenCard = getCardChoice(validCards)
            if (chosenCard != null) {
                return chosenCard
            }
        }
    }

    private fun getValidCardsForPlay(player: Player): List<Card> {
        return if (currentTopCard.type == CardType.KING) {
            player.getHandCards()
        } else {
            player.getHandCards().filter { card ->
                isValidPlay(card, currentTopCard)
            }
        }
    }

    private fun isValidPlay(card: Card, topCard: Card): Boolean {
        return when {
            card.type == CardType.BREAK_PLUS_3 -> 
                topCard.type == CardType.PLUS_3
            
            card.type == CardType.NUMBER && 
                (card.color == topCard.color || card.value == topCard.value) -> true
            
            card.type != CardType.NUMBER && 
                (card.color == topCard.color || card.type == topCard.type) -> true
            
            card.color == CardColor.NONE && card.type != CardType.BREAK_PLUS_3 -> true
            
            else -> false
        }
    }

    /**
     * Handles playing a card and its effects.
     * @param player The player who played the card
     * @param card The card being played
     */
    private fun playCard(player: Player, card: Card) {
        handleCardPlacement(player, card)
        handleCardType(player, card)
    }

    /**
     * Handles the physical placement of a card and color tracking
     */
    private fun handleCardPlacement(player: Player, card: Card) {
        player.playCard(card)
        updateCardColors(card)
        currentTopCard = card
        player.canPlayAgain = false
    }

    /**
     * Updates color tracking when a new card is played
     */
    private fun updateCardColors(card: Card) {
        if (card.color != CardColor.NONE) {
            lastColoredCard = card
            lastValidColor = card.color
        }
    }

    /**
     * Handles the effect of different card types
     */
    private fun handleCardType(player: Player, card: Card) {
        when (card.type) {
            CardType.STOP -> skipNextPlayer()
            CardType.PLUS_2 -> handlePlusTwoEffect(player)
            CardType.SWITCH_DIRECTION -> isDirectionReversed = !isDirectionReversed
            CardType.SWITCH_COLOR -> chooseColor()
            CardType.TAKI -> playTaki(player, card.color)
            CardType.SUPER_TAKI -> {
                val superTakiColor = if (currentTopCard.color != CardColor.NONE) {
                    currentTopCard.color
                } else {
                    lastValidColor
                }
                display.showSuperTakiColor(superTakiColor)
                playTaki(player, superTakiColor)
            }
            CardType.PLUS -> handlePlusCard(player)
            CardType.PLUS_3 -> handlePlusThreeCard(player)
            CardType.BREAK_PLUS_3 -> cancelPlusThree()
            CardType.KING -> handleKingCard(player)
            else -> player.canPlayAgain = false
        }
    }

    private fun skipNextPlayer() {
        getNextPlayer().isTurnSkipped = true
    }

    /**
     * Handles PLUS_2 card chain effect.
     * Each affected player can either:
     * - Play another PLUS_2 (increasing draw count)
     * - Draw the accumulated cards and get their turn
     * @param startingPlayer The player who played the initial PLUS_2
     */
    private fun handlePlusTwoEffect(startingPlayer: Player) {
        val plusTwoState = PlusTwoState(getNextPlayer(), 2)
        handlePlusTwoChain(plusTwoState)
    }

    private fun handlePlusTwoChain(state: PlusTwoState) {
        while (true) {
            display.showPlusTwo(state.currentPlayer.name, state.drawCount)

            val plusTwoCards = getPlusTwoCards(state.currentPlayer)
            if (plusTwoCards.isEmpty()) {
                drawCardsAndEndChain(state)
                return
            }

            // Only show options if player has PLUS_2 cards
            if (!shouldPlayPlusTwo(state.drawCount)) {
                drawCardsAndEndChain(state)
                return
            }

            val chosenCard = choosePlusTwoCard(plusTwoCards) ?: run {
                drawCardsAndEndChain(state)
                return
            }

            playPlusTwoCard(state, chosenCard)
        }
    }

    private fun getPlusTwoCards(player: Player): List<Card> {
        return player.getHandCards().filter { it.type == CardType.PLUS_2 }
    }

    private fun drawCardsAndEndChain(state: PlusTwoState) {
        display.showDrawCards(state.currentPlayer.name, state.drawCount)
        repeat(state.drawCount) {
            state.currentPlayer.drawCard(deck)
        }
        currentPlayerIndex = players.indexOf(state.currentPlayer)
    }

    private fun shouldPlayPlusTwo(drawCount: Int): Boolean {
        display.showPlusTwoOptions(drawCount)
        return getValidNumericInput(1, 2) == 2
    }

    private fun choosePlusTwoCard(plusTwoCards: List<Card>): Card? {
        println("Choose which PLUS_2 card to play:")
        plusTwoCards.forEachIndexed { index, card ->
            println("${index + 1}: $card")
        }

        val choice = getValidNumericInput(1, plusTwoCards.size)
        return plusTwoCards[choice - 1]
    }

    private fun playPlusTwoCard(state: PlusTwoState, card: Card) {
        state.currentPlayer.playCard(card)
        currentTopCard = card
        state.drawCount += 2
        currentPlayerIndex = players.indexOf(state.currentPlayer)
        state.currentPlayer = getNextPlayer()
    }


    private fun handlePlusCard(player: Player) {
        player.canPlayAgain = true
        display.showExtraTurn(player.name)
    }

    private fun handleKingCard(player: Player) {
        player.canPlayAgain = true
        display.showKingPlayed(player.name)
    }

    private fun handlePlusThreeCard(player: Player) {
        allOtherPlayersDraw(3)
        display.showGameState(players, currentTopCard)
        chooseColor()
    }

    /**
     * Handles color selection for color-changing cards.
     * Used by:
     * - SWITCH_COLOR
     * - PLUS_3
     */
    private fun chooseColor() {
        val nextPlayer = getNextPlayer()
        display.showColorChoice(nextPlayer.name)

        val validColors = CardColor.entries.filter { it != CardColor.NONE }
        val choice = getValidNumericInput(1, validColors.size)
        val chosenColor = validColors[choice - 1]

        display.showColorChanged(chosenColor)
        currentTopCard = Card(chosenColor, CardType.SWITCH_COLOR)
    }

    /**
     * Handles a TAKI card sequence.
     * Players can play multiple cards of:
     * - The same color as the TAKI
     * - The same number as their last played number card
     * The sequence ends when:
     * - A player closes the TAKI
     * - A player has no more playable cards
     * - Next player has no matching cards
     * @param player The player who started the TAKI
     * @param color The active color for the TAKI sequence
     */
    private fun playTaki(player: Player, color: CardColor) {
        val takiState = TakiState(player, color)
        
        while (takiState.isActive) {
            handleTakiTurn(takiState)
            if (!takiState.isActive) break
        }
        
        processTakiEndEffects(takiState)
    }



    private fun handleTakiTurn(state: TakiState) {
        display.showTakiTurnStart(state.currentPlayer.name, state.color)
        
        val playableCards = getPlayableCardsForTaki(state)
        if (playableCards.isEmpty()) {
            display.showNoPlayableCards(state.currentPlayer.name)
            state.isActive = false
            return
        }

        val chosenCard = getTakiCardChoice(state.currentPlayer, playableCards)
        if (chosenCard == null) {
            display.showTakiClose(state.currentPlayer.name)
            state.isActive = false
            return
        }

        playTakiCard(state, chosenCard)
        
        // Don't move to next player unless TAKI is closed or no more cards
        if (state.isActive && hasMorePlayableCards(state)) {
            handleTakiTurn(state)  // Continue with same player
        }
    }

    private fun getPlayableCardsForTaki(state: TakiState): List<Card> {
        return state.currentPlayer.getHandCards().filter { card ->
            card.color == state.color || 
            (state.lastPlayedNumber != null && 
             card.type == CardType.NUMBER && 
             card.value == state.lastPlayedNumber)
        }
    }

    private fun getTakiCardChoice(player: Player, playableCards: List<Card>): Card? {
        display.showTakiOptions(playableCards)
        val choice = getValidNumericInput(1, playableCards.size + 1)
        return if (choice == playableCards.size + 1) null else playableCards[choice - 1]
    }

    private fun playTakiCard(state: TakiState, card: Card) {
        state.currentPlayer.playCard(card)
        currentTopCard = card
        state.lastPlayedCard = card
        display.showCardPlayed(state.currentPlayer.name, card)

        // Update TAKI color if card has a different color
        if (card.color != CardColor.NONE) {
            state.color = card.color  // Update the TAKI state color
        }

        state.lastPlayedNumber = if (card.type == CardType.NUMBER) {
            card.value
        } else {
            null
        }

        if (!hasMorePlayableCards(state)) {
            display.showNoPlayableCards(state.currentPlayer.name)
            state.isActive = false
        }
    }

    private fun hasMorePlayableCards(state: TakiState): Boolean {
        return state.currentPlayer.getHandCards().any { card -> 
            card.color == state.color || 
            (state.lastPlayedNumber != null && 
             card.type == CardType.NUMBER && 
             card.value == state.lastPlayedNumber)
        }
    }

    private fun processTakiEndEffects(state: TakiState) {
        state.lastPlayedCard?.let { card ->
            processCardEffects(card, state.currentPlayer)
        }
    }

    // helper function to process card effects
    private fun processCardEffects(card: Card, player: Player) {
        when (card.type) {
            CardType.STOP -> skipNextPlayer()
            CardType.PLUS_2 -> handlePlusTwoEffect(player)
            CardType.SWITCH_DIRECTION -> isDirectionReversed = !isDirectionReversed
            CardType.SWITCH_COLOR -> chooseColor()
            CardType.PLUS -> {
                player.canPlayAgain = true
                display.showExtraTurn(player.name)
            }
            CardType.PLUS_3 -> {
                allOtherPlayersDraw(3)
                println("\nAfter PLUS_3, next player chooses the new color.")
                chooseColor()
            }
            else -> {} // No special effect for other cards
        }
    }

    /**
     * Handles PLUS_3 effect on all other players.
     * - Each player can use BREAK_PLUS_3 to block
     * - Only the first BREAK_PLUS_3 takes effect
     * - If blocked, the PLUS_3 player draws instead
     * @param count The number of cards to draw (always 3)
     */
    private fun allOtherPlayersDraw(count: Int) {
        var wasBreakUsed = false
        
        for (player in players) {
            if (player == players[currentPlayerIndex]) continue
            
            if (shouldHandleBreakPlus3(player, count, wasBreakUsed)) {
                wasBreakUsed = handleBreakPlus3(player)
                if (wasBreakUsed) return
            }
            
            if (!wasBreakUsed) {
                drawCardsForPlayer(player, count)
            }
        }
    }

    private fun shouldHandleBreakPlus3(player: Player, count: Int, wasBreakUsed: Boolean): Boolean {
        return count == 3 && 
               player.hasCard(CardType.BREAK_PLUS_3) && 
               !wasBreakUsed
    }

    private fun handleBreakPlus3(player: Player): Boolean {
        display.showBreakPlus3(player.name)
        if (getYesNoChoice("Do you want to use BREAK_PLUS_3 to block drawing 3 cards?")) {
            val breakCard = player.getHandCards().first { it.type == CardType.BREAK_PLUS_3 }
            player.playCard(breakCard)
            display.showBreakPlus3Used(player.name)
            handlePlus3Reversal()
            return true
        }
        return false
    }

    private fun drawCardsForPlayer(player: Player, count: Int) {
        display.showDrawCards(player.name, count)
        repeat(count) { player.drawCard(deck) }
    }

    private fun handlePlus3Reversal() {
        val plus3Player = players[currentPlayerIndex]
        display.showPlus3Draw(plus3Player.name)
        repeat(3) { plus3Player.drawCard(deck) }
    }

    private fun cancelPlusThree() {
        val lastPlayer = getPreviousPlayer()
        display.showBreakPlus3Draw(lastPlayer.name)
        repeat(3) { lastPlayer.drawCard(deck) }
    }

    /**
     * Gets the next player in turn order.
     * Considers direction (normal/reversed).
     */
    private fun getNextPlayer(): Player {
        val nextIndex = if (isDirectionReversed) {
            (currentPlayerIndex - 1 + players.size) % players.size
        } else {
            (currentPlayerIndex + 1) % players.size
        }
        return players[nextIndex]
    }

    private fun getPreviousPlayer(): Player {
        val prevIndex = if (isDirectionReversed) {
            (currentPlayerIndex + 1) % players.size
        } else {
            (currentPlayerIndex - 1 + players.size) % players.size
        }
        return players[prevIndex]
    }

    private fun getYesNoChoice(prompt: String): Boolean {
        while (true) {
            display.showYesNoPrompt(prompt)
            when (readlnOrNull()?.trim()?.lowercase()) {
                "yes" -> return true
                "no" -> return false
                else -> display.showInvalidYesNo()
            }
        }
    }

    private fun showCardOptions(validCards: List<Card>) {
        display.showCardOptions(validCards)
    }

    private fun handleNoValidCards(player: Player): Card {
        display.showError("You have no playable cards. You must draw a card.")
        player.drawCard(deck)
        return player.getHandCards().last() // Return the drawn card
    }

    private fun getCardChoice(validCards: List<Card>): Card? {
        val input = readlnOrNull()?.toIntOrNull()
        
        if (input == null || input !in 1..validCards.size) {
            display.showError("Please enter a number between 1 and ${validCards.size}.")
            return null
        }
        return validCards[input - 1]
    }

    private fun getValidNumericInput(min: Int, max: Int): Int {
        while (true) {
            val input = readlnOrNull()?.trim()?.toIntOrNull()
            if (input != null && input in min..max) {
                return input
            }
            display.showError("Please enter a number between $min and $max.")
        }
    }
}
