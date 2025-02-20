class Player(val name: String) {
    private val handCards: MutableList<Card> = mutableListOf() // The player's hand of cards
    var isTurnSkipped: Boolean = false
    var canPlayAgain: Boolean = false


    // Getter for handCards to allow read-only access from outside
    fun getHandCards(): List<Card> = handCards.toList()


    fun drawCard(deck: Deck) {
        val drawnCard = deck.drawCard()
        if (drawnCard != null) {
            handCards.add(drawnCard)
        } else {
            println("$name tried to draw a card, but the deck is empty.")
        }
    }

    fun playCard(card: Card): Boolean {
        if (handCards.contains(card)) {
            handCards.remove(card)
            println("$name played a card: $card")
            return true
        }
        return false
    }

    // Check if the player can play a card (based on the current game state)
    fun canPlayCard(topCard: Card): Boolean {
        return handCards.any { card ->
            when {
                // After KING, any card can be played
                topCard.type == CardType.KING -> true
                
                // BREAK_PLUS_3 is only valid if responding to a PLUS_3
                card.type == CardType.BREAK_PLUS_3 -> 
                    topCard.type == CardType.PLUS_3

                // For number cards: Must match color OR value
                card.type == CardType.NUMBER && 
                    (card.color == topCard.color || card.value == topCard.value) -> true

                // For special cards: Match either color or type
                card.type != CardType.NUMBER && 
                    (card.color == topCard.color || card.type == topCard.type) -> true

                // Wild cards can always be played
                card.color == CardColor.NONE && card.type != CardType.BREAK_PLUS_3 -> true

                else -> false
            }
        }
    }

    // Check if the player has won
    fun hasWon(): Boolean {
        return handCards.isEmpty()
    }

    fun hasCard(cardType: CardType): Boolean {
        return getHandCards().any { it.type == cardType }
    }


}
