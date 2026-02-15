package WordleSolver;

import java.util.ArrayList;

/**
 * An interface to describe the basic actions needed by an implementation of the Wordle game.
 * Note that in the prior project, we had not yet discussed interfaces. Thus your prior code
 * does not 'implement' this interface, although it could easily be made to do so.  If you'd like
 * to do that, you'll need something for the getRules() method (your code won't have one). Simply
 * have this method return the value 0; that will get you going...
 *
 *
 */
public interface Wordle {

    static int RULES_ALLOW_ONLY_WORD_GUESSES = 0;

    /**
     * A Wordle Puzzle is created by selecting a secret string, which the player tries to guess.
     *
     * This method returns a list of the possible secret strings.
     * Note that all Strings in this list will have the same length.
     *
     * @return a copy of the list of possible solutions to the puzzle.
     */
    public ArrayList<String> getKnownWords();

    /**
     * Returns an integer:
     *
     * Wordle.RULES_ALLOW_ONLY_WORD_GUESSES - indicates that a word submitted to guess() that is
     *   not from the list of KnownWords will throw an IllegalArgumentException.
     *
     * No other rules are defined at this point in time.
     *
     * @return an int from the set above.
     */
    public int getRules();

    /**
     *
     * @param word
     * @return
     */
    public Hint guess(String word);

    public void initGame();

    public void initGame(String secret); // had to add this for tests.
}
