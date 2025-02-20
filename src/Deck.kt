class Deck {
    private val cards: MutableList<Card> = mutableListOf()
    private val discardPile = mutableListOf<Card>()

    init {
        initializeDeck()
        shuffle()
    }

    private fun initializeDeck() {
        val colors = listOf(CardColor.RED, CardColor.BLUE, CardColor.GREEN, CardColor.YELLOW)

        // add cards 1-9
        for (color in colors) {
            for (i in 1..9) {
                if (i == 2) {
                    cards.add(Card(color, CardType.PLUS_2)) // Replace 2 with PLUS_2
                } else {
                    cards.add(Card(color, CardType.NUMBER, i))
                }
            }
        }

        // Add special action cards - one per color
        for (color in colors) {
            cards.add(Card(color, CardType.STOP))
            cards.add(Card(color, CardType.SWITCH_DIRECTION))
            cards.add(Card(color, CardType.PLUS))
            cards.add(Card(color, CardType.TAKI))
        }

        // Add wild  cards
        repeat(2) { cards.add(Card(CardColor.NONE, CardType.SWITCH_COLOR)) }

        cards.add(Card(CardColor.NONE, CardType.SUPER_TAKI))
        cards.add(Card(CardColor.NONE, CardType.KING))
        cards.add(Card(CardColor.NONE, CardType.PLUS_3))
        cards.add(Card(CardColor.NONE, CardType.BREAK_PLUS_3))

        // Duplicate the deck to get a full set of 116 cards
        cards.addAll(cards.toList())
    }

    private fun shuffle() {
        cards.shuffle()
    }

    fun drawCard(): Card? {
        if (cards.isEmpty() && discardPile.isNotEmpty()) {
            println("Reshuffling discard pile into deck...")
            cards.addAll(discardPile)
            discardPile.clear()
            cards.shuffle()
        }
        return if (cards.isNotEmpty()) cards.removeLast() else null
    }

    fun returnCard(card: Card) {
        cards.add(card)
        cards.shuffle()  // Shuffle after returning 
    }
}
