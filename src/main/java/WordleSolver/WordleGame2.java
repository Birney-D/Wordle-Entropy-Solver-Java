package WordleSolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class WordleGame2 {

    private ArrayList<String> knownWords;
    private ArrayList<String> answerList; // some day we will do this.

    public WordleGame2(String file1, String file2) throws IOException {
        // Used for wordle (NYTimes version)
        loadWords(file1, file2);
    }

    public WordleGame2(String file, int length, long minFreq, long maxFreq) throws IOException {
        // Used for absurdle!!
        loadWords(file, length, minFreq, maxFreq);
    }

    public int numberOfKnownWords() { return knownWords.size(); } // For testing.

    public void loadWords(String accepted, String answers) throws IOException {
        // Scans each line of file and adds the word to known words list if correct length and within range specified.
        knownWords = new ArrayList<>();
        answerList = new ArrayList<>();
        Scanner acceptedScanner = new Scanner(new File(accepted));
        Scanner answerScanner = new Scanner(new File(answers));

        while (acceptedScanner.hasNext()) {
            String acceptedWord = acceptedScanner.next();
            knownWords.add(acceptedWord.toLowerCase());
        }
        acceptedScanner.close();

        while (answerScanner.hasNext()) {
            String answerWord = answerScanner.next();
            answerList.add(answerWord);
        }
        answerScanner.close();
    }

    public void loadWords(String file, int length, long minFreq, long maxFreq) throws IOException {
        knownWords = new ArrayList<>();
        Scanner s = new Scanner(new File(file));

        while (s.hasNext()) {
            String word = s.next();
            long freq = s.nextLong();

            if (word.length() == length && freq >= minFreq) {
                if (maxFreq != 0) {
                    if (freq <= maxFreq) knownWords.add(word.toLowerCase());
                } else knownWords.add(word.toLowerCase()); // If maxFreq is 0, no upper limit.
            }
        }
        s.close();
    }

    public void printNum(String fname, int freq, int len) {
        // Used for testing. Prints all words from file that match parameters.
        try {
            Scanner scanner = new Scanner(new File(fname));
            while (scanner.hasNext()) {
                String token = scanner.next();
                System.out.println(token);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not Found!!!");
            e.printStackTrace();
        }
    }

    public ArrayList<String> getKnownWords() { return new ArrayList<>(knownWords); } // For testing.
    public ArrayList<String> getAnswerList() { return new ArrayList<>(answerList); } // Gives a copy of the list.

}
