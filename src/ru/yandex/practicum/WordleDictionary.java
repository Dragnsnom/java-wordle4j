package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class WordleDictionary {

    private final List<String> words;
    private final PrintWriter log;

    public WordleDictionary(List<String> words, PrintWriter log) {
        this.words = new ArrayList<>(words);
        this.log = log;
        log.println("WordleDictionary создан. Загружено слов: " + this.words.size());
    }

    public int getSize() {
        return words.size();
    }

    public boolean containsWord(String word) {
        word = normalizeWord(word);
        return words.contains(word);
    }

    public String getRandomWord() {
        if (words.isEmpty()) {
            return null;
        }
        Random random = new Random();
        return words.get(random.nextInt(words.size()));
    }

    public static String normalizeWord(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        String normalized = word.toLowerCase();
        return normalized.replace('ё', 'е');
    }

    public static boolean isValidWord(String word) {
        if (word == null || word.length() != 5) {
            return false;
        }
        // Проверка, что все символы - русские буквы
        for (char c : word.toCharArray()) {
            if (!isRussianLetter(c)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isRussianLetter(char c) {
        c = Character.toLowerCase(c);
        return (c >= 'а' && c <= 'я') || c == 'ё';
    }

    public static String getHintLine(String guess, String secret) {
        if (guess == null || secret == null || guess.length() != secret.length()) {
            return "-----";
        }

        char[] guessChars = guess.toCharArray();
        char[] secretChars = secret.toCharArray();
        char[] hint = new char[guess.length()];

        // Массив для отметки использованных букв в секретном слове
        boolean[] used = new boolean[secret.length()];

        // Первый проход: ищем точные совпадения (+)
        for (int i = 0; i < guessChars.length; i++) {
            if (guessChars[i] == secretChars[i]) {
                hint[i] = '+';
                used[i] = true;
            }
        }

        // Второй проход: ищем буквы, которые есть, но не на своих местах (^)
        for (int i = 0; i < guessChars.length; i++) {
            if (hint[i] == '+') {
                continue; // Пропускаем уже отмеченные точные совпадения
            }

            boolean found = false;
            for (int j = 0; j < secretChars.length; j++) {
                if (!used[j] && guessChars[i] == secretChars[j]) {
                    found = true;
                    used[j] = true;
                    break;
                }
            }

            if (found) {
                hint[i] = '^';
            } else {
                hint[i] = '-';
            }
        }

        return new String(hint);
    }

    public List<String> filterByPositionMatches(List<String> words,
                                                Map<Integer, Character> positionMatches) {
        if (positionMatches.isEmpty()) {
            return words;
        }

        return words.stream()
                .filter(word -> {
                    for (Map.Entry<Integer, Character> entry : positionMatches.entrySet()) {
                        int position = entry.getKey();
                        char expectedChar = entry.getValue();
                        if (word.charAt(position) != expectedChar) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    public List<String> filterByExcludedLetters(List<String> words,
                                                Set<Character> excludedLetters) {
        if (excludedLetters.isEmpty()) {
            return words;
        }

        return words.stream()
                .filter(word -> {
                    for (char excluded : excludedLetters) {
                        if (word.indexOf(excluded) != -1) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    public List<String> filterByRequiredLetters(List<String> words,
                                                Set<Character> requiredLetters) {
        if (requiredLetters.isEmpty()) {
            return words;
        }

        return words.stream()
                .filter(word -> {
                    for (char required : requiredLetters) {
                        if (word.indexOf(required) == -1) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    public List<String> filterByWrongPositions(List<String> words,
                                               Map<Integer, Character> wrongPositions) {
        if (wrongPositions.isEmpty()) {
            return words;
        }

        return words.stream()
                .filter(word -> {
                    for (Map.Entry<Integer, Character> entry : wrongPositions.entrySet()) {
                        int position = entry.getKey();
                        char wrongChar = entry.getValue();
                        if (word.charAt(position) == wrongChar) {
                            return false;
                        }
                        if (word.indexOf(wrongChar) == -1) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    public List<String> getPossibleWords(Set<Character> requiredLetters,
                                         Set<Character> excludedLetters,
                                         Map<Integer, Character> exactMatches,
                                         Map<Integer, Character> wrongPositions) {
        List<String> possible = new ArrayList<>(words);

        log.println("Фильтрация слов. Начальное количество: " + possible.size());

        // Применяем фильтры по порядку
        possible = filterByPositionMatches(possible, exactMatches);
        log.println("После фильтрации по точным позициям: " + possible.size());

        possible = filterByExcludedLetters(possible, excludedLetters);
        log.println("После исключения букв: " + possible.size());

        possible = filterByRequiredLetters(possible, requiredLetters);
        log.println("После обязательных букв: " + possible.size());

        possible = filterByWrongPositions(possible, wrongPositions);
        log.println("После фильтрации по неправильным позициям: " + possible.size());

        return possible;
    }

}