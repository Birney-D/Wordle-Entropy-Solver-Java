package WordleSolver;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class BasicWordleBotTests {
    static String theBotName = null;

    @Test
    public void canCreateBot() {
        createBot();
    }

    public static WordlePlayer createBot() {
        WordlePlayer player = null;

        try {
            if (theBotName == null) {
                // bot discovery...
                File currentDirectory = new File("./");
                theBotName = "DumberBot";
                int botCount = 0;
                for(String s : currentDirectory.list()) {
                    if (s.endsWith("Bot.java")) {
                        theBotName = s.substring(0,s.length()-5);
                        botCount++;
                    }
                }
                if (botCount > 1) throw new IllegalStateException("Multiple Bots? This shouldn't happen in production...");
//                if (botCount == 0) throw new IllegalStateException("Submitted java file must end with 'Bot.java', like DumbBot.java");
            }

            player = (WordlePlayer) Class.forName(theBotName).getConstructor().newInstance();

        } catch(ClassNotFoundException cnf) {
          throw new NullPointerException("Cannot instantiate your bot. Do you have a constructor with no arguments?");
        } catch (IllegalAccessException e) {
            throw new NullPointerException("Cannot instantiate your bot. Do you have a constructor with no arguments?");
        } catch (InstantiationException e) {
            throw new NullPointerException("Cannot instantiate your bot. Do you have a constructor with no arguments?");
        } catch (NoSuchMethodException e) {
            throw new NullPointerException("Cannot instantiate your bot. Do you have a constructor with no arguments?");
        } catch (InvocationTargetException e) {
            throw new NullPointerException("Cannot instantiate your bot. Do you have a constructor with no arguments?");
        }
        return player;
    }

    public static int runGame(Wordle game, WordlePlayer bot, int maxguesses, boolean verbose) {
        int guesses = 0;
        Hint h = null;

        while(bot.hasNextGuess() && guesses < maxguesses) {
            guesses++;
            String guess = bot.nextGuess();
            h = game.guess(guess);
            if (verbose) {
                System.out.println("Guessing: " + guess);
                System.out.println("  Hint is: " + h.toString());
                if (h.isWin()) System.out.println("  ** Game won in " + guesses);
            }
            bot.tell(h);
            if (h.isWin()) {
                return guesses;
            }
        }
        return -1;
    }
    
    @Test
    public void canBotGuessFromThreeWordListIn5Tries() throws IOException {
        String[] s = {"ham", "pan", "can"};

        int reps = 20;

        while (reps-- > 0) {
            Wordle w = new WordleGame(s);
            w.initGame();
            WordlePlayer bot = BasicWordleBotTests.createBot();
            bot.beginGame(w);
            int result = runGame(w, bot, 5, false);
            Assert.assertTrue(result != -1);
        }
    }



    @Test(timeout=100)
    public void betterThanDumbBot1() throws IOException {
        String[] s = {"ham", "pan", "can"};

        int reps = 100;
        long guesses = 0;

        while (reps-- > 0) {
            Wordle w = new WordleGame(s);
            w.initGame();
            WordlePlayer bot = BasicWordleBotTests.createBot();
            bot.beginGame(w);
            int result = runGame(w, bot, 3, false);
            Assert.assertTrue("There are only 3 words, but your bot couldn't solve the puzzle in with 3 guesses.", result != -1);
            guesses += result;
        }
        guesses /= reps;
        Assert.assertTrue("There are only 3 words, but you took more than 2 guesses...", guesses < 2);
    }


    @Test(timeout=3000)
    public void somewhatSmart1() throws IOException {
        // all of these are reachable with max=8 steps with 3000 monte-carlo games
        // when run with a minfreq dictionary of 10000000
        String[] s = {"other", "their", "there", "price", "email", "books", "order"};

        int allowedTries = 8;
        long guesses;
        Wordle w = new WordleGame("norvig333k.txt", 5, 10000000, 0);
        int nSolved = 0;
        for(String secret : s) {
            guesses = 0;
            w.initGame(secret);
            WordlePlayer bot = BasicWordleBotTests.createBot();
            bot.beginGame(w);
            int result = runGame(w, bot, allowedTries, false);
            if (result != -1) {
                nSolved += 1;
            }
        }
        Assert.assertTrue("Expected to get most of these to be 'somewhat smart'. Are you using the clues?", nSolved >= 4);
    }

    @Test(timeout=3000)
    public void somewhatSmart2() throws IOException {
        // all of these are reachable with max=7 steps with 3000 monte-carlo games
        // when run with a minfreq dictionary of 10000000
        String[] s = {"about", "first", "would", "click", "world", "music", "video"};

        int allowedTries = 7;
        long guesses;
        Wordle w = new WordleGame("norvig333k.txt", 5, 10000000, 0);
        int nSolved = 0;
        for(String secret : s) {
            guesses = 0;
            w.initGame(secret);
            WordlePlayer bot = BasicWordleBotTests.createBot();
            bot.beginGame(w);
            int result = runGame(w, bot, allowedTries, false);
            if (result != -1) {
                nSolved += 1;
            }
        }
        Assert.assertTrue("Expected to get most of these to be 'somewhat smart'. Are you using the clues?", nSolved >=4);
    }

    @Test(timeout=3000)
    public void somewhatSmart3() throws IOException {
        // all of these are reachable with max=6 steps with 3000 monte-carlo games
        // when run with a minfreq dictionary of 10000000
        String[] s = {"could", "would", "forum"};

        int allowedTries = 6;
        long guesses;
        Wordle w = new WordleGame("norvig333k.txt", 5, 10000000, 0);
        int nSolved = 0;
        for(String secret : s) {
            guesses = 0;
            w.initGame(secret);
            WordlePlayer bot = BasicWordleBotTests.createBot();
            bot.beginGame(w);
            int result = runGame(w, bot, allowedTries, false);
            if (result != -1) {
                nSolved += 1;
            }
        }
        Assert.assertTrue("Expected to get most of these to be 'somewhat smart'. Are you using the clues?", nSolved >= 2);
    }

}
