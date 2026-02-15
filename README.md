# Wordle Solver – Entropy-Based Search (Java)

A Java implementation of an intelligent Wordle-solving algorithm that selects guesses using Shannon entropy and information gain.

The solver models all possible feedback patterns for each candidate guess and chooses the word that maximizes expected information gain, significantly reducing the solution space after each round.

## Algorithm Overview

For each candidate guess:

1. Simulate all possible hint patterns against remaining words.
2. Compute frequency of each feedback pattern.
3. Calculate Shannon entropy:
   H = Σ p(pattern) * log₂(1 / p(pattern))
4. Select the guess with the highest expected information gain.

This approach minimizes the expected remaining search space after each guess.

## How to Run Program

1. Compile
```bash
mvn clean package
```
2. Run
```bash
java -jar target/to/<your-jar-name>.jar
```
3. Follow the prompt:
* It will give you a suggested "best word" guess
* It will ask you which word you decided to choose
* It will ask for the "Hint Pattern" you received from Wordle
	- 0: letter not in word (gray tile)
	- 1: letter in word but not in correct position (yellow tile)
	- 2: letter in word and in correct position (green tile)
	* Example of hint patter for [gray, yellow, green, gray, yello] ~ 01201
* It will then give you the next recommended word based on the hint
* This continues until you guess correctly or you run out of turns

