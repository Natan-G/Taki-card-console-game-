enum class CardColor {RED, BLUE, GREEN, YELLOW, NONE}//NONE FOR special cards
enum class CardType {
    NUMBER,                // 1-9
    STOP,                  // Next player loses their turn
    PLUS_2,                // Forces the next player to draw 2 cards (stackable)
    SWITCH_DIRECTION,      // Reverses the direction of play
    SWITCH_COLOR,          // Wild: Allows the player to choose the next color
    TAKI,                  // Allows the player to drop multiple cards of the same color
    SUPER_TAKI,            // Wild TAKI that matches the leading color
    PLUS,                  // Gives the player another turn
    PLUS_3,                // Forces all other players to draw 3 cards
    BREAK_PLUS_3,          // Cancels a PLUS_3 and player that choose PLUS_3 draws 3 cards
    KING                   // Grants an extra turn with no restrictions (resets play)
}
data class Card(val color: CardColor, val type: CardType, val value: Int? = null) {
    override fun toString(): String {
        return if (color == CardColor.NONE) {
            "$type $color"
        } else if (type == CardType.NUMBER) {
            "$value $color"
        } else {
            "$type $color"
        }
    }
}



