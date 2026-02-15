package WordleSolver;

import java.io.IOException;
import java.util.*;

/*
PROJECT II: WORDLE AI
NAME: DAVID BIRNEY

Strategy: Strategic ordering + hint-based elimination.

    -> The first part of my strategy for this project was to eliminate any
        words in the "knownWords" list that don't match the information in the hints. The second strategy was to use
        entropy or information theory to rank guesses based on the probability that a guess will reduce number of
        known words the greatest amount.
    -> To implement the hint-based elimination part I used the "tell(Hint h)" method to import the 3 hints and use them
       to scan through the word list and remove any words that didn't match the information in each hint. For instance,
       I used the "correctlyPlaced" hint to check if each word contained the letter in that specific index and if it
       didn't I removed it from the list. I then did this for each hint to trim the word list down to only words that
       could possibly be the secret word after each guess.
    -> To implement the strategic ordering of guesses, I used a method called "getHeuristicGuess()" which basically
       used the frequencies of letters occurring in the words and then computed the "entropy" (computed using helper
       method "updateLetterEntropy()") of each letter, or the likely amount the word list would be trimmed down to by
       guessing a word containing each of those letters. I then I took the sum of all the letter entropies contained in
       each word to assign a score to each word. The method then returned the highest scoring word. This computation
       repeats after each guess since the word list gets smaller after each round and therefore the frequencies also
       change.

****** Unused Strategies & associated methods ********

    -> I tried a couple other strategies to try and improve my program's performance (see trials below), but in the end
       my first strategy turned out to be the best in terms of speed and passing the most tests.
    -> One strategy I tried implementing was just like my letter entropy strategy but, it also incorporated the positions
       of letters as well. To implement this strategy I used a method called "positionalEntropyGuess()", which
       calculated the frequencies of each letter in each of the index positions (0-4 for 5-letter words). It then
       used these numbers to calculate the predicted entropy for each and then summed up these values for each word in
       the list of known words. This method turned out to not do as well as the heuristic guesser in the wordleBot tests.
    -> Finally I tried implementing a method called "maxEntropyGuess" which was kinda like a combination of both
       heuristic and positional entropy strategies. This method basically used every word in the word list as a potential
       guess, and then used every word as the secret word and kept track of all the possible hint patterns from each
       combination (with the help of "getHintPattern(String g, String s)" method). With this information it was able to
       keep track of all the possible hint patterns for each guess and determine the probability of that hint pattern
       occurring and the likely amount of information gained from each hint pattern. The  max entropy for each guess
       was found by summing all the individual entropies from each possible hint pattern for each word and the highest
       one was returned. This method turned out to have the best performance in terms of avg # of guesses and # of games
       won, but it was too slow and failed the time limit (3 seconds) for a couple of the basic wordleBot tests that
       involved multiple games in a row.

Stats obtained for the 3 different strats (Used ("norvig333k.txt, 5, 10000000, 0) for known word list)):
All stats were out of 1000 games.

Max Entropy Avg Wins: 100
Max E Avg # guesses: 3.11
Positional E Avg Wins: 99.57
Pos E Avg Guesses: 3.27
heuristicGuess Avg Wins: 99.80
heuristic Avg Guesses: 3.42

 */

public class DumberBot implements WordlePlayer {

    public List<String> knownWords;

    // Used for computing letter frequencies/entropies.
    public List<String> letters;
    // Holds all letter entropy scores.
    public List<Double> letterEntropies;

    // previously guessed letters. Used for "notInWord" hint.
    public Set<String> prevGuessed;

    public String bestGuess;


    public DumberBot() {
    }


    @Override
    public void beginGame(Wordle game) {
        // Get a copy of the arraylist of known words from WordleGame class, instantiates the various data
        // structures used in the program, and sets the bestWord to an initial guess for the start.

        knownWords = game.getKnownWords();
        System.out.println("Starting word count: " + knownWords.size()); // Testing purposes

        letters = new ArrayList<String>(26);
        for (char ch = 'a'; ch <= 'z'; ch++) {
            String s = String.valueOf(ch);
            letters.add(s);
        }

        // initially setting entropies to zero:
        letterEntropies = new ArrayList<Double>(26);
        for (int i = 0; i < letters.size(); i++) {
            letterEntropies.add(0.0);
        }

        prevGuessed = new HashSet<String>();
        // Just as starting point.
        bestGuess = knownWords.get(0);
    }


    @Override
    public boolean hasNextGuess() {
        return bestGuess != null && !knownWords.isEmpty();
    }


    @Override
    public String nextGuess() {
        // Supplies the Wordle game a guess based on a ranking system using letter frequencies.

        if (knownWords.size() > 3) bestGuess = this.heuristicGuess();
        else bestGuess = knownWords.get(new Random().nextInt(knownWords.size()));

        return bestGuess;
    }


    @Override
    public void tell(Hint h) {
        // Used for the !!hint-based elimination!! based strategy. This method uses the hints obtained from the Hint
        // class to scan the known words list and eliminate any words that do not match the information gained from them.

        String greenHint = h.getCorrectlyPlaced();
        String yellowHint = h.getIncorrectlyPlaced();
        String greyHint = h.getNotInPuzzle();

        // This is just adding characters that are deff in the word to the prevGuessed set so that the greyHint
        // (notInWord hint) will not remove words with these letters.
        for (int i = 0; i < greenHint.length(); i++) {
            if (greenHint.charAt(i) != '-') prevGuessed.add(greenHint.substring(i, i + 1));
            else if (yellowHint.charAt(i) != '-') prevGuessed.add(yellowHint.substring(i, i + 1));
        }

        // keeps track of repeat letters and how many are in the secret word.
        Map<String, Integer> atLeastThisMany = new HashMap<>();
        Map<String, Integer> exactlyThisMany = new HashMap<>();
        // Just used for a final check if the word is not removed by other hints later.
        boolean multiLetterHint = false;

        // When the curGuess has duplicate letters, the "notInWord" hint can give valuable info
        // about how many of those letters are in the word (# repeat letters - # letters in "notInWord" hint).
        // This bit of code determines the number of letters in a word by using repeat letters in the guess.
        for (int i = 0; i < bestGuess.length() - 1; i++) {
            int letterCount = 1;
            for (int j = i + 1; j < bestGuess.length(); j++) {
                if (bestGuess.charAt(j) == bestGuess.charAt(i)) letterCount++;
            }

            if (letterCount > 1) { // If repeat letters in guess:
                // Determines if that letter is in "notInWord" hint. If so, it counts them.
                if (greyHint.contains(bestGuess.substring(i, i + 1))) {
                    for (int j = 0; j < greyHint.length(); j++) {
                        if (greyHint.charAt(j) == bestGuess.charAt(i)) {
                            letterCount--; // Subtracts the ones in grey hint for total in word.
                        }
                    }

                    // Now we know the exact number of this letter.
                    if (letterCount > 0) {
                        multiLetterHint = true;
                        // add the letter and how many there are in this hashMap.
                        exactlyThisMany.put(bestGuess.substring(i, i + 1), letterCount);
                    } // If zero, will be caught later.

                } else { // (wasn't in grey hint) This means there is at least n number of letters, but no less.
                    multiLetterHint = true;
                    atLeastThisMany.put(bestGuess.substring(i, i + 1), letterCount);
                }
            }
        }

        // Main loop for using hints to update word list:
        // Reverse loop to remove while iterating (no errors).
        for (int i = knownWords.size() - 1; i >= 0; i--) {
            String curWord = knownWords.get(i); // Grabs a word.
            // Used to skip to next word after a word is eliminated by a hint.
            boolean wordEliminated = false;

            // Green Hint (correctlyPlaced):
            for (int j = 0; j < greenHint.length(); j++) {
                if (greenHint.charAt(j) != '-') {
                    if (curWord.charAt(j) != greenHint.charAt(j)) {
                        knownWords.remove(curWord);
                        wordEliminated = true;
                        break;
                    }

                    // Yellow hint (incorrectlyPlaced):
                } else if (yellowHint.charAt(j) != '-') {
                    // If the word doesn't contain the letter at all, or it contains it but in the wrong spot (remove).
                    if (!curWord.contains(yellowHint.substring(j, j + 1)) || curWord.charAt(j) == yellowHint.charAt(j)) {
                        knownWords.remove(curWord);
                        wordEliminated = true;
                        break;

                    } else if (multiLetterHint) {
                        // if this letter is in our data structures, checks if correct number in word.
                        if (exactlyThisMany.containsKey(yellowHint.substring(j, j + 1)) ||
                                atLeastThisMany.containsKey(yellowHint.substring(j, j + 1))) {

                            boolean removeIt = false;
                            int actualLetterCount = 0;
                            for (int k = 0; k < curWord.length(); k++) {
                                if (curWord.charAt(k) == yellowHint.charAt(j)) actualLetterCount++;
                            }

                            if (exactlyThisMany.containsKey(yellowHint.substring(j, j + 1))) {
                                int requiredLetterCount = exactlyThisMany.get(yellowHint.substring(j, j + 1));
                                if (requiredLetterCount != actualLetterCount) removeIt = true;

                            } else { // Must be in atLeastThisMany structure!
                                int minLetterCount = atLeastThisMany.get(yellowHint.substring(j, j + 1));
                                if (actualLetterCount < minLetterCount) removeIt = true;
                            }

                            if (removeIt) { // Didn't meet required letter count.
                                knownWords.remove(curWord);
                                wordEliminated = true;
                                break;
                            }
                        }
                    }
                }
            }

            // Grey Hint (notInWord):
            if (wordEliminated) continue; // If word has been eliminated, continue to next word.
            for (int j = 0; j < greyHint.length(); j++) {
                // Prevents removing words prematurely. Ex: word has 1 r but guess contained 2 r's.
                // "r" will be in the yellow and grey hint.
                if (!prevGuessed.contains(greyHint.substring(j, j + 1))) {
                    if (curWord.contains(greyHint.substring(j, j + 1))) {
                        knownWords.remove(curWord);
                        break;
                    }
                }
            }
        }

        // update prevGuessed set after updating wordlist.
        for (int i = 0; i < greyHint.length(); i++) prevGuessed.add(greyHint.substring(i, i + 1));
        System.out.println(" words remaining: " + knownWords.size()); // Used for testing.
    }




    public String heuristicGuess() {
        // Uses helper method to find letter frequencies (# words containing letter/ total words)
        // and then computes the entropies of each letter (how much word list will be reduced to if secret word
        // contains the letter). Finally, each word is ranked by the sum of all the letter entropies.

        double highestEntropy = -1000;
        String bestWord = null;

        // Helper method -> calculates probability each letter will occur in the secret word.
        // Must update after each guess for remaining words.
        this.updateLetterEntropy();

        for (String curWord : knownWords) {
            double wordEntropy = 0.0;

            for (int j = 0; j < letters.size(); j++) {
                String letter = letters.get(j);
                double letterEntropy = letterEntropies.get(j);

                if (curWord.contains(letter)) {
                    wordEntropy += letterEntropy;
                }
            }

            // Finds the highest scoring word (guess with highest prob of reducing our word list the most).
            if (wordEntropy > highestEntropy) {
                highestEntropy = wordEntropy;
                bestWord = curWord;
            }
        }

        return bestWord;
    }

    public void updateLetterEntropy() {
        // Helper method for heuristicGuess().
        // Updates letter frequencies or the number of times each letter occurs in a word (probability)
        // and then computes the entropy (anticipated information gained) by guessing a word with that letter in it.

        for (String curWord : knownWords) {
            for (int j = 0; j < letters.size(); j++) {

                String letter = letters.get(j);
                double letterFreq = letterEntropies.get(j);
                if (curWord.contains(letter)) {
                    // Increments the count by +1 for each word that contains that letter.
                    letterEntropies.set(j, letterFreq + 1);
                }
            }
        }

        for (int i = 0; i < letterEntropies.size(); i++) {
            double letterFreq = letterEntropies.get(i);
            // probability it's in the word.
            double probInWord = (letterFreq / knownWords.size());
            // Shannon Entropy calculation -> same as (log2(1/P)) * Probability.
            double entropy = (probInWord) * ((Math.log(1 / probInWord)) / (Math.log(2)));

            // replacing letter frequencies with entropy.
            letterEntropies.set(i, entropy);
        }
    }

    // **************************************************************************************************
    // *********** UNUSED METHODS: PREVIOUSLY TRIED STRATEGIES ******************************************
    // **************************************************************************************************

    // *** NOT USED IN PROGRAM ***

    public String maxEntropyGuess2() {
        // Uses maximum entropy calculation to score each word in the word list based on how much "information"
        // that word is likely to give us (how much it is likely to reduce our word list by).
        // This method takes into account not only the letters but also their position and certain combination of letters.

        String bestWord = null;
        double maxEntropy = -1000;

        for (String guess : knownWords) {
            // Holds the hint patterns (list of numbers 0-2) and how often they occur for each guess.
            Map<String, Integer> hintPatterns = new HashMap<>();

            for (String secretWord : knownWords) {
                // produces the possible hint patterns if each word was the answer.
                String possiblePattern = this.getHintPattern2(guess, secretWord);

                // adds to count (+1) if the hint pattern already exists in map.
                if (hintPatterns.containsKey(possiblePattern)) {
                    hintPatterns.computeIfPresent(possiblePattern, ((key, value) -> value + 1));
                    // If pattern is new, it adds it to the map and set value (count) to 1.
                } else hintPatterns.put(possiblePattern, 1);
            }

            Collection<Integer> patternFreqs = hintPatterns.values();
            // Computes the entropy or information gain for current guess.
            double guessEntropy = this.computeMaxEntropy(patternFreqs);
            if (guessEntropy > maxEntropy) {
                maxEntropy = guessEntropy;
                bestWord = guess;
            }
        }
        return bestWord;
    }

    // *** NOT USED IN PROGRAM ***
    public String getHintPattern2(String guess, String secretWord) {
        // Helper method for maxEntropyGuess(). Very similar to the Hint constructor except it returns numbers.
        // Creates the hint pattern for each guess/secret word combo in form of Integer[] for easy comparison.

        // Pattern of numbers for hints: 2 for correctly placed, 1 for incorrectly placed, 0 for notInWord.
        // Integer[] to work well with ArrayList.
        Hint h = new HintGenerator(guess, secretWord);

        String greenHint = h.getCorrectlyPlaced();
        String yellowHint = h.getIncorrectlyPlaced();

        StringBuilder hintPattern = new StringBuilder();

        for (int i = 0; i < greenHint.length(); i++) {
            if (greenHint.charAt(i) != '-') hintPattern.append('2');
            else if (yellowHint.charAt(i) != '-') hintPattern.append('1');
            else hintPattern.append('0');
        }

        return hintPattern.toString();
    }



    public String maxEntropyGuess() {
        // Uses maximum entropy calculation to score each word in the word list based on how much "information"
        // that word is likely to give us (how much it is likely to reduce our word list by).
        // This method takes into account not only the letters but also their position and certain combination of letters.

        String bestWord = null;
        double maxEntropy = -1000;

        for (String guess : knownWords) {
            // Holds the hint patterns (list of numbers 0-2) and how often they occur for each guess.
            Map<ArrayList<Integer>, Integer> hintPatterns = new HashMap<>();

            for (String secretWord : knownWords) {
                // produces the possible hint patterns if each word was the answer.
                ArrayList<Integer> possiblePattern = this.getHintPattern(guess, secretWord);

                // adds to count (+1) if the hint pattern already exists in map.
                if (hintPatterns.containsKey(possiblePattern)) {
                    hintPatterns.computeIfPresent(possiblePattern, ((key, value) -> value + 1));
                    // If pattern is new, it adds it to the map and set value (count) to 1.
                } else hintPatterns.put(possiblePattern, 1);
            }

            Collection<Integer> patternFreqs = hintPatterns.values();
            // Computes the entropy or information gain for current guess.
            double guessEntropy = this.computeMaxEntropy(patternFreqs);
            if (guessEntropy > maxEntropy) {
                maxEntropy = guessEntropy;
                bestWord = guess;
            }
        }
        return bestWord;
    }

    // *** NOT USED IN PROGRAM ***
    public ArrayList<Integer> getHintPattern(String guess, String secretWord) {
        // Helper method for maxEntropyGuess(). Very similar to the Hint constructor except it returns numbers.
        // Creates the hint pattern for each guess/secret word combo in form of Integer[] for easy comparison.

        // Pattern of numbers for hints: 2 for correctly placed, 1 for incorrectly placed, 0 for notInWord.
        // Integer[] to work well with ArrayList.
        Integer[] hintPattern = new Integer[guess.length()];
        Arrays.fill(hintPattern, 0);

        // Check if perfect match:
        if (guess.equals(secretWord)) {
            Arrays.fill(hintPattern, 2);
        } else {
            // Used to keep track of used indexes so not to count them twice (incorrectlyPlaced hint).
            ArrayList<Integer> prevUsedIndexes = new ArrayList<>();

            // Check for correctly placed characters.
            for (int i = 0; i < guess.length(); i++) {
                if (guess.charAt(i) == secretWord.charAt(i)) {
                    hintPattern[i] = 2;
                    prevUsedIndexes.add(i);
                }
            }

            // Second step: check for letters in word but wrong location.
            for (int i = 0; i < guess.length(); i++) {
                if (hintPattern[i] == 2 || !secretWord.contains(guess.substring(i, i + 1))) continue;

                for (int j = 0; j < secretWord.length(); j++) {
                    // Prevents counting prev matches.
                    if (guess.charAt(i) == secretWord.charAt(j) && !prevUsedIndexes.contains(j)) {
                        hintPattern[i] = 1;
                        prevUsedIndexes.add(j);
                        break;
                    }
                }
            } // All the rest are not in word (zeros).
        }
        return new ArrayList<Integer>(Arrays.asList(hintPattern));
    }

    // *** NOT USED IN PROGRAM ***
    public double computeMaxEntropy(Collection<Integer> freqs) {
        // Helper method to maxEntropyGuess() method. Uses frequencies obtained from the getHintPatter() method
        // to calculate the entropy of each guess.

        double maxEntropy = 0.0;
        double numberOfPatterns = knownWords.size();

        for (Integer f : freqs) {
            // Probability of pattern occurring.
            double patternProb = (f / numberOfPatterns);
            // How much that pattern would trim down our word list. same as log2(1/P)
            double bitsInformation = (Math.log(1 / patternProb)) / (Math.log(2));
            double patternEntropy = (patternProb * bitsInformation);
            // sum of all the possible pattern entropies.
            maxEntropy += patternEntropy;
        }

        return maxEntropy;
    }

    // *** NOT USED IN PROGRAM ***
    public String positionalEntropyGuess() {
        // Computes the entropies of letters based on the probability they occur in a specific index position.

        double maxPositionEntropy = -1000;
        String bestWord = null;

        // keeps track of indices where each letter is found most often.
        List<Double[]> positionalEntropies = new ArrayList<>(26);
        int wordLength = knownWords.get(0).length();

        for (int i = 0; i < 26; i++) {
            Double[] letterIndexes = new Double[wordLength];
            Arrays.fill(letterIndexes, 0.0);
            positionalEntropies.add(letterIndexes);
        }

        // Uses this helper method as well to get update letter freqs/entropies.
        this.updateLetterEntropy();

        for (String curWord : knownWords) {
            for (int j = 0; j < letters.size(); j++) {
                String letter = letters.get(j);
                Double[] indexes = positionalEntropies.get(j);

                // documents where each letter occurs in all words.
                if (curWord.contains(letter)) {
                    for (int k = 0; k < curWord.length(); k++) {
                        if (curWord.substring(k, k + 1).equals(letter)) {
                            indexes[k] += 1;
                        }
                    }
                }
            }
        }

        // computes probability of each letter in certain indices and the associated entropy.
        for (Double[] indexFreqs : positionalEntropies) {
            for (int i = 0; i < indexFreqs.length; i++) {
                double freq = indexFreqs[i];
                double prob = freq / (knownWords.size() * wordLength);
                double indexEntropy = (Math.log(1 / prob) / Math.log(2)) * prob;
                indexFreqs[i] = indexEntropy;
            }
        }

        for (String word : knownWords) {
            double wordEntropy = 0.0;

            // Sums up all the entropies of each letter in each word.
            for (int i = 0; i < word.length(); i++) {
                String letter = word.substring(i, i + 1);
                int letterIndex = letters.indexOf(letter);
                Double[] indexEntropies = positionalEntropies.get(letterIndex);
                wordEntropy += indexEntropies[i];
            }

            // Combining letter entropy without position.
            for (int i = 0; i < letters.size(); i++) {
                String letter = letters.get(i);
                if (word.contains(letter)) wordEntropy += letterEntropies.get(i);
            }

            if (wordEntropy > maxPositionEntropy) {
                maxPositionEntropy = wordEntropy;
                bestWord = word;
            }
        }

        return bestWord;
    }

    // *** MAIN METHOD FOR MANUAL TESTING *** NOT USED IN PROGRAM ****
    public static void main(String[] args) throws IOException {

        // Used for running many games back-to-back to gain stats for best possible strategy/methods.

        long startTime = System.nanoTime();

        String[] s = {"other", "their", "there", "price", "email", "books", "order",
                "about", "first", "would", "click", "world", "music", "video"};

        double totalGuesses = 0;
        int wins = 0;
        int gamesPlayed = 100;

        Wordle w = new WordleGame("norvig333k.txt", 5, 100000, 0);
        for (int i = 0; i < gamesPlayed; i++) {
            w.initGame(); // Random word
            DumberBot dum = new DumberBot();
            dum.beginGame(w);
            Hint h = null;

            int guesses = 0;
            int maxGuesses = 6;

            while (dum.hasNextGuess() && guesses < maxGuesses) {
                guesses++;
                h = w.guess(dum.nextGuess()); // get a hint from the bots guess.
                System.out.println("guess: " + dum.bestGuess);
                dum.tell(h); // tell the bot the hint.

                if (h.isWin()) break;
            }
            if (h.isWin()) wins += 1;
            totalGuesses += guesses;
        }
        double totalTime = System.nanoTime() - startTime;
        double avgTime = totalTime / gamesPlayed;
        double avgGuesses = totalGuesses / gamesPlayed;
        System.out.println("Average Time: " + avgTime / 1000000 + "ms" + "\n"
                + "Average Guesses: " + avgGuesses + "\n"
                + "Number of Wins out of " + gamesPlayed + " games: " + wins);
    }
}
