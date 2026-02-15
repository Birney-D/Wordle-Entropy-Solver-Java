package WordleSolver;

public interface Hint {

    /**
     * Returns a String whose length is the same length as the puzzle's solution.
     * If no characters in a given guess are correctly placed, this String
     * will contain only '-' characters.  For each character in the guess
     * that is in the correct location (same location as in the solution),
     * this String will contain that character.
     *
     * So, for a secret word of 'hello' and a guess of 'melts',
     * getCorrectlyPlaced() would return "-el--"
     *
     * So, for a secret word of 'helps' and a guess of 'hello',
     * getCorrectlyPlaced() would return "hel--" since the
     * first three characters are correctly placed.
     *
     * @return a String indicating correctly placed characters
     */
    public String getCorrectlyPlaced();

    /**
     * Returns a String whose length is the same length as the puzzle's solution.
     * Indicates which characters in the guess are in the puzzle's solution
     * but not at the same location as in the guess.
     *
     * So, for a secret word of 'chase' and a guess of 'hello',
     * getIncorrectlyPlaced() would return "-h--e"
     *
     * Note that for a secret word of 'helps' and a guess of 'hello',
     * getIncorrectlyPlaced() would return "----". The
     * second 'l' is not incorrectly placed; rather, it is
     * not needed to complete the solution given that the first
     * three letters are 'hel'.
     *
     * @return a String indicating correctly placed characters
     */
    public String getIncorrectlyPlaced();

    /**
     * Returns a String whose length is between 0 and the length of the solution.
     * Indicates characters in the guess that are not anywhere in the solution.
     *
     * So, for a secret word of 'helps' and a guess of 'hello',
     * getNotInPuzzle() would return "lo". Note, that it *does not* simply return "o".
     *
     * @return a String indicating correctly placed characters
     */
    public String getNotInPuzzle();

    /**
     *
     * @return true if the guess solved the puzzle.
     */
    public boolean isWin();
}
