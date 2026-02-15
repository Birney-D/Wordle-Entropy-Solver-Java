package WordleSolver;

import java.io.IOException;
import java.util.*;

public class SmartBot {

    public List<String> knownWords;
    public List<String> answerWords;

    // Used for computing letter frequencies/entropies.
    public List<String> letters;
    // Holds all letter entropy scores.
    public List<Double> letterEntropies;

    // previously guessed letters. Used for "notInWord" hint.
    public Set<String> prevGuessed;

    public String bestGuess;
    public String userGuess;


    public SmartBot() { }


    public void beginGame(WordleGame2 game) {
        // Get a copy of the arraylist of known words from WordleGame class, instantiates the various data
        // structures used in the program, and sets the bestWord to an initial guess for the start.

        knownWords = game.getKnownWords();
        answerWords = game.getAnswerList();
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
        userGuess = "";
    }

    public boolean hasNextGuess() {
        return bestGuess != null && !knownWords.isEmpty();
    }

    public String nextGuess() {
        // Supplies the Wordle game a guess based on a ranking system using letter frequencies.

        bestGuess = this.maxEntropyGuess();
//        else bestGuess = knownWords.get(new Random().nextInt(knownWords.size()));

        return bestGuess;
    }

    public void tell(String hint) {
       // bestGuess: "salet" hint: "02010"

        for (int i = 0; i < hint.length(); i++) {
            if (hint.charAt(i) == '2') prevGuessed.add(userGuess.substring(i, i + 1));
            else if (hint.charAt(i) == '1') prevGuessed.add(userGuess.substring(i, i + 1));
        }

        // keeps track of repeat letters and how many are in the secret word.
        Map<String, Integer> atLeastThisMany = new HashMap<>();
        Map<String, Integer> exactlyThisMany = new HashMap<>();
        // Just used for a final check if the word is not removed by other hints later.
        boolean multiLetterHint = false;

        for (int i = 0; i < userGuess.length() - 1; i++) {
            int letterCount = 1;
            for (int j = i + 1; j < userGuess.length(); j++) {
                if (userGuess.charAt(j) == userGuess.charAt(i)) letterCount++;
            }

            if (letterCount > 1) { // If repeat letters in guess:
                // Determines if that letter is in "notInWord" hint. If so, it counts them.
                boolean greyLetter = false;
                for (int j = 0; j < hint.length(); j++) {
                    if (hint.charAt(j) == '0' && userGuess.charAt(j) == userGuess.charAt(i)) {
                        letterCount--;
                        greyLetter = true;
                    }
                }

                if (greyLetter) {

                    // Now we know the exact number of this letter.
                    if (letterCount > 0) {
                        multiLetterHint = true;
                        // add the letter and how many there are in this hashMap.
                        exactlyThisMany.put(userGuess.substring(i, i + 1), letterCount);
                    }

                } else { // (wasn't in grey hint) This means there is at least n number of letters, but no less.
                    multiLetterHint = true;
                    atLeastThisMany.put(userGuess.substring(i, i + 1), letterCount);
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
            for (int j = 0; j < hint.length(); j++) {
                if (hint.charAt(j) == '2') {
                    if (curWord.charAt(j) != userGuess.charAt(j)) {
                        knownWords.remove(curWord);
                        wordEliminated = true;
                        break;
                    }

                    // Yellow hint (incorrectlyPlaced):
                } else if (hint.charAt(j) == '1') {
                    // If the word doesn't contain the letter at all, or it contains it but in the wrong spot (remove).
                    if (!curWord.contains(userGuess.substring(j, j + 1)) || curWord.charAt(j) == userGuess.charAt(j)) {
                        knownWords.remove(curWord);
                        wordEliminated = true;
                        break;

                    } else if (multiLetterHint) {
                        // if this letter is in our data structures, checks if correct number in word.
                        if (exactlyThisMany.containsKey(userGuess.substring(j, j + 1)) ||
                            atLeastThisMany.containsKey(userGuess.substring(j, j + 1))) {

                            boolean removeIt = false;
                            int actualLetterCount = 0;
                            for (int k = 0; k < curWord.length(); k++) {
                                if (curWord.charAt(k) == userGuess.charAt(j)) actualLetterCount++;
                            }

                            if (exactlyThisMany.containsKey(userGuess.substring(j, j + 1))) {
                                int requiredLetterCount = exactlyThisMany.get(userGuess.substring(j, j + 1));
                                if (requiredLetterCount != actualLetterCount) removeIt = true;

                            } else { // Must be in atLeastThisMany structure!
                                int minLetterCount = atLeastThisMany.get(userGuess.substring(j, j + 1));
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
            for (int j = 0; j < hint.length(); j++) {
                // Prevents removing words prematurely. Ex: word has 1 r but guess contained 2 r's.
                // "r" will be in the yellow and grey hint.
                if (hint.charAt(j) == '0' && !prevGuessed.contains(userGuess.substring(j, j + 1))) {
                    if (curWord.contains(userGuess.substring(j, j + 1))) {
                        knownWords.remove(curWord);
                        break;
                    }
                }
            }
        }

        // update prevGuessed set after updating wordlist.
        for (int i = 0; i < hint.length(); i++) prevGuessed.add(userGuess.substring(i, i + 1));
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

    public String maxEntropyGuess() {
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
                String possiblePattern = this.getHintPattern(guess, secretWord);

                // adds to count (+1) if the hint pattern already exists in map.
                if (hintPatterns.containsKey(possiblePattern)) {
                    hintPatterns.computeIfPresent(possiblePattern, ((key, value) -> value + 1));
                    // If pattern is new, it adds it to the map and set value (count) to 1.
                } else hintPatterns.put(possiblePattern, 1);
            }

            Collection<Integer> patternFreqs = hintPatterns.values();
            // Computes the entropy or information gain for current guess.
            double guessEntropy = this.computeMaxEntropy(patternFreqs);
//            double commonWordScore = 1.0 / knownWords.size();
//            if (answerWords.contains(guess)) guessEntropy += commonWordScore;
            if (guessEntropy > maxEntropy) {
                maxEntropy = guessEntropy;
                bestWord = guess;
            }
        }
        return bestWord;
    }

    public String getHintPattern(String guess, String secretWord) {
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

    public static void main(String[] args) throws IOException {
        WordleGame2 game2 = new WordleGame2("answerWords.txt", "answerWords.txt");
        SmartBot smartyPants = new SmartBot();
        smartyPants.beginGame(game2);
        Scanner s = new Scanner(System.in);

        while (smartyPants.hasNextGuess()) {

            String botGuess = smartyPants.nextGuess();
            // best guess based on words remaining
            System.out.println("Suggested Guess: " + botGuess);
            smartyPants.userGuess = botGuess; // default guess

            while (true) {
                System.out.print("What word would you like to guess?: ");
                String userGuess = s.nextLine();

                // check user input for validity
                if (userGuess.length() != 5 || !userGuess.matches("[a-zA-Z]+")) {
                    System.err.println("Invalid guess! Guess must be 5 letters and contain only characters a-z");
                    continue;
                }

                // set user guess
                smartyPants.userGuess = userGuess.toLowerCase();
                break;
            }

            // 0 = not in word; 1 = present but in wrong spot; 2 = correct
            while (true) {
                System.out.print("What is the hint pattern?: ");
                String hint = s.nextLine();  
                if (hint.length() != 5 || !hint.matches("[012]+")) {
                    System.err.println("Hint pattern not correct! Hint pattern only accepts 5 numbers 0 - 2");
                    continue;     
                }
                
                if (hint.equals("22222")) {
                    System.out.println("Congratulations! You Win!");
                    s.close();
                    return;
                }

                smartyPants.tell(hint);
                break;
            }
        }

        s.close();
    }
}
