package ru.yandex.practicum;

import ru.yandex.practicum.Exceptions.GameAlreadyFinishedException;
import ru.yandex.practicum.Exceptions.InvalidWordLengthException;
import ru.yandex.practicum.Exceptions.WordNotFoundException;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class WordleGame {

    private final String answer;
    private int remainingAttempts;
    private final WordleDictionary dictionary;
    private final PrintWriter log;
    private boolean gameFinished;
    private boolean wordGuessed;

    private final List<String> guessHistory;
    private final List<String> hintHistory;

    private final Set<Character> requiredLetters;
    private final Set<Character> excludedLetters;
    private final Map<Integer, Character> exactMatches;
    private final Map<Integer, Character> wrongPositions;

    private static final int MAX_ATTEMPTS = 6;
    private static final int WORD_LENGTH = 5;

    public WordleGame(WordleDictionary dictionary, PrintWriter log) {
        this.dictionary = dictionary;
        this.log = log;
        this.answer = dictionary.getRandomWord();
        this.remainingAttempts = MAX_ATTEMPTS;
        this.gameFinished = false;
        this.wordGuessed = false;
        this.guessHistory = new ArrayList<>();
        this.hintHistory = new ArrayList<>();
        this.requiredLetters = new HashSet<>();
        this.excludedLetters = new HashSet<>();
        this.exactMatches = new HashMap<>();
        this.wrongPositions = new HashMap<>();

        log.println("WordleGame создан. Загаданное слово: " + answer);
        log.println("Начальное состояние: попыток осталось " + remainingAttempts);
    }

    public GameResult makeGuess(String guess) throws WordNotFoundException,
            InvalidWordLengthException,
            GameAlreadyFinishedException {
        log.println("makeGuess: попытка " + (MAX_ATTEMPTS - remainingAttempts + 1)
                + ", слово: " + guess);

        // Проверяем, не завершена ли игра
        if (gameFinished) {
            log.println("Ошибка: игра уже завершена");
            throw new GameAlreadyFinishedException("Игра уже завершена. Начните новую игру.");
        }

        String normalizedGuess = WordleDictionary.normalizeWord(guess);
        log.println("Нормализованное слово: " + normalizedGuess);

        if (normalizedGuess.length() != WORD_LENGTH) {
            log.println("Ошибка: неверная длина слова - " + normalizedGuess.length());
            throw new InvalidWordLengthException("Слово должно состоять из " + WORD_LENGTH + " букв. " +
                    "Вы ввели " + normalizedGuess.length() + " букв.");
        }

        if (!dictionary.containsWord(normalizedGuess)) {
            log.println("Ошибка: слово не найдено в словаре - " + normalizedGuess);
            throw new WordNotFoundException("Слово \"" + normalizedGuess + "\" не найдено в словаре.");
        }

        String hintLine = WordleDictionary.getHintLine(normalizedGuess, answer);
        log.println("Подсказка: " + hintLine);

        guessHistory.add(normalizedGuess);
        hintHistory.add(hintLine);

        updateLetterInfo(normalizedGuess, hintLine);

        boolean isGuessed = normalizedGuess.equals(answer);

        remainingAttempts--;

        // Создаем результат
        GameResult result = new GameResult(hintLine, isGuessed, remainingAttempts);

        // Обновляем состояние игры
        if (isGuessed) {
            gameFinished = true;
            wordGuessed = true;
            log.println("Игра выиграна! Слово: " + answer);
        } else if (remainingAttempts == 0) {
            gameFinished = true;
            wordGuessed = false;
            log.println("Игра проиграна. Загаданное слово: " + answer);
        }

        log.println("Ход завершен. Осталось попыток: " + remainingAttempts
                + ", игра завершена: " + gameFinished);

        return result;
    }

    private void updateLetterInfo(String guess, String hintLine) {
        char[] guessChars = guess.toCharArray();
        char[] hintChars = hintLine.toCharArray();

        for (int i = 0; i < hintChars.length; i++) {
            char letter = guessChars[i];
            char hint = hintChars[i];

            if (hint == '+') {
                // Буква на правильном месте
                exactMatches.put(i, letter);
                requiredLetters.add(letter);
                log.println("Точное совпадение: позиция " + i + ", буква '" + letter + "'");
            } else if (hint == '^') {
                // Буква есть, но не на этом месте
                wrongPositions.put(i, letter);
                requiredLetters.add(letter);
                log.println("Буква есть: '" + letter + "' не на позиции " + i);
            } else if (hint == '-') {
                // Проверяем, не встречалась ли эта буква в других местах как + или ^
                boolean isRequired = requiredLetters.contains(letter);
                boolean isInExact = exactMatches.containsValue(letter);
                boolean isInWrong = wrongPositions.containsValue(letter);

                if (!isRequired && !isInExact && !isInWrong) {
                    excludedLetters.add(letter);
                    log.println("Исключенная буква: '" + letter + "'");
                }
            }
        }

        log.println("Текущее состояние: required=" + requiredLetters
                + ", excluded=" + excludedLetters
                + ", exact=" + exactMatches
                + ", wrong=" + wrongPositions);
    }

    public String getHint() {
        log.println("Запрос подсказки");

        // Если игра уже завершена, подсказки не даем
        if (gameFinished) {
            log.println("Игра завершена, подсказка недоступна");
            return null;
        }

        // Получаем список возможных слов с учетом всех ограничений
        List<String> possibleWords = dictionary.getPossibleWords(
                requiredLetters, excludedLetters, exactMatches, wrongPositions
        );

        log.println("Найдено возможных слов: " + possibleWords.size());

        if (possibleWords.isEmpty()) {
            log.println("Нет доступных подсказок");
            return null;
        }

        // Исключаем уже использованные слова
        List<String> unusedWords = new ArrayList<>(possibleWords);
        unusedWords.removeAll(guessHistory);

        log.println("Неиспользованных слов: " + unusedWords.size());

        if (unusedWords.isEmpty()) {
            // Если все возможные слова уже использованы, возвращаем любое из возможных
            String hint = possibleWords.get(new Random().nextInt(possibleWords.size()));
            log.println("Подсказка (все слова использованы): " + hint);
            return hint;
        }

        // Выбираем случайное слово из неиспользованных
        String hint = unusedWords.get(new Random().nextInt(unusedWords.size()));
        log.println("Подсказка: " + hint);

        return hint;
    }

    public boolean isGameFinished() {
        return gameFinished;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public String getSecretWord() {
        return answer;
    }
}