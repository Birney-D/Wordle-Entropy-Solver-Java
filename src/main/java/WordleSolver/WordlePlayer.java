package WordleSolver;

/**
 * A WordlePlayer interacts with a game of Wordle.
 *
 * Somewhere, (e.g., in my test harness) code will:
 *  1. create a Wordle Game
 *  2. create a Wordle Player (e.g., your Bot, or one of mine)
 *  3. call the WorldePlayer's beginGame() method to allow the Bot to 'get ready'
 *  4. loop and:
 *    4a. see if the WordlePlayer has a guess (using hasNextGuess())
 *    4b. ask for a guess from the WordlePlayer (using nextGuess())
 *    4c. give the guess to the Game, and retrieve a hint
 *    4d. pass the hint back to the WordlePlayer (using tell())
 *
 *  Code implementing this 'game loop' is illustrated in the Readme; you can implement similar
 *  code yourself for testing.
 */
public interface WordlePlayer {

    /**
     * Called prior to starting a game, to allow the player to fetch legal words
     * from the game instance.
     *
     * @param game
     */
    public void beginGame(Wordle game);

    /**
     *
     * @return true if a player can make a guess.
     */
    public boolean hasNextGuess();

    /**
     *
     * @return the player's next guess.
     */
    public String nextGuess();

    /**
     * Called by the 'game loop' to communicate a hint from the game instance
     * to the player.
     *
     * @param h
     */
    public void tell(Hint h);

}
