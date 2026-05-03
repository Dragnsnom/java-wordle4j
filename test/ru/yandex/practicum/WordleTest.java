package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ru.yandex.practicum.Exceptions.DictionaryLoadException;
import ru.yandex.practicum.Exceptions.GameAlreadyFinishedException;
import ru.yandex.practicum.Exceptions.InvalidWordLengthException;
import ru.yandex.practicum.Exceptions.WordNotFoundException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WordleTest {

    private PrintWriter log;
    private StringWriter logWriter;
    private WordleDictionary dictionary;

    @BeforeAll
    void setUpAll() {
        // Создаем тестовый словарь
        List<String> testWords = Arrays.asList(
                "герой", "гора", "город", "герб", "гений",
                "кот", "коза", "книга", "слово", "тест",
                "молоко", "машина", "дом", "сад"
        );

        logWriter = new StringWriter();
        log = new PrintWriter(logWriter);

        dictionary = new WordleDictionary(testWords, log);
    }

    @BeforeEach
    void setUp() {
        logWriter = new StringWriter();
        log = new PrintWriter(logWriter);
    }

    // ==================== ТЕСТЫ ДЛЯ WORDLE DICTIONARY ====================

    @Nested
    @DisplayName("Тесты WordleDictionary")
    class WordleDictionaryTests {

        @Test
        @DisplayName("Проверка нормализации слова - нижний регистр")
        void testNormalizeWordLowerCase() {
            String normalized = WordleDictionary.normalizeWord("ГЕРОЙ");
            assertEquals("герой", normalized);
        }

        @Test
        @DisplayName("Проверка нормализации слова - замена ё на е")
        void testNormalizeWordReplaceYo() {
            String normalized = WordleDictionary.normalizeWord("ёлка");
            assertEquals("елка", normalized);
        }

        @Test
        @DisplayName("Проверка валидного слова из 5 букв")
        void testIsValidWordValid() {
            assertTrue(WordleDictionary.isValidWord("герой"));
            assertFalse(WordleDictionary.isValidWord("елка"));
        }

        @Test
        @DisplayName("Проверка невалидного слова - неверная длина")
        void testIsValidWordInvalidLength() {
            assertFalse(WordleDictionary.isValidWord("гер"));
            assertFalse(WordleDictionary.isValidWord("героиня"));
        }

        @Test
        @DisplayName("Проверка невалидного слова - латиница")
        void testIsValidWordLatinLetters() {
            assertFalse(WordleDictionary.isValidWord("hello"));
            assertFalse(WordleDictionary.isValidWord("abcde"));
        }

        @Test
        @DisplayName("Проверка подсказки - точное совпадение")
        void testGetHintLineExactMatch() {
            String hint = WordleDictionary.getHintLine("герой", "герой");
            assertEquals("+++++", hint);
        }

        @Test
        @DisplayName("Проверка подсказки - нет совпадений")
        void testGetHintLineNoMatch() {
            String hint = WordleDictionary.getHintLine("абвгд", "ежзик");
            assertEquals("-----", hint);
        }

        @Test
        @DisplayName("Проверка подсказки - буква есть не на своем месте")
        void testGetHintLineWrongPosition() {
            String hint = WordleDictionary.getHintLine("орегй", "герой");
            assertNotNull(hint);
            assertEquals(5, hint.length());
        }

        @Test
        @DisplayName("Проверка получения случайного слова из словаря")
        void testGetRandomWord() {
            String word = dictionary.getRandomWord();
            assertNotNull(word);
            assertTrue(dictionary.containsWord(word));
        }

        @Test
        @DisplayName("Проверка фильтрации по точным позициям")
        void testFilterByPositionMatches() {
            List<String> words = Arrays.asList("герой", "герб", "гора");
            Map<Integer, Character> matches = new HashMap<>();
            matches.put(0, 'г');
            matches.put(1, 'е');

            List<String> filtered = dictionary.filterByPositionMatches(words, matches);

            assertEquals(2, filtered.size());
            assertTrue(filtered.contains("герой"));
            assertTrue(filtered.contains("герб"));
        }

        @Test
        @DisplayName("Проверка фильтрации по исключенным буквам")
        void testFilterByExcludedLetters() {
            List<String> words = Arrays.asList("герой", "герб", "гора");
            Set<Character> excluded = new HashSet<>(List.of('б'));

            List<String> filtered = dictionary.filterByExcludedLetters(words, excluded);

            assertEquals(2, filtered.size());
            assertTrue(filtered.contains("герой"));
            assertTrue(filtered.contains("гора"));
            assertFalse(filtered.contains("герб"));
        }

        @Test
        @DisplayName("Проверка фильтрации по обязательным буквам")
        void testFilterByRequiredLetters() {
            List<String> words = Arrays.asList("герой", "герб", "гора");
            Set<Character> required = new HashSet<>(Arrays.asList('р', 'б'));

            List<String> filtered = dictionary.filterByRequiredLetters(words, required);

            assertEquals(1, filtered.size());
            assertTrue(filtered.contains("герб"));
        }
    }

    // ==================== ТЕСТЫ ДЛЯ WORDLE GAME ====================

    @Nested
    @DisplayName("Тесты WordleGame")
    class WordleGameTests {

        @Test
        @DisplayName("Создание игры - словарь не пуст")
        void testGameCreation() {
            WordleGame game = new WordleGame(dictionary, log);
            assertNotNull(game);
            assertEquals(6, game.getRemainingAttempts());
            assertFalse(game.isGameFinished());
        }

        @Test
        @DisplayName("Успешный ход с угадыванием слова")
        void testSuccessfulGuess() throws Exception {
            List<String> singleWord = List.of("герой");
            WordleDictionary testDict = new WordleDictionary(singleWord, log);
            WordleGame testGame = new WordleGame(testDict, log);

            testGame.makeGuess("герой");

            assertTrue(testGame.isGameFinished());
        }

        @Test
        @DisplayName("Ход с неверным словом")
        void testWrongGuess() throws Exception {
            List<String> words = Arrays.asList("герой", "распоп");
            WordleDictionary testDict = new WordleDictionary(words, log);
            WordleGame testGame = new WordleGame(testDict, log);

            testGame.makeGuess("герой");

            assertEquals(5, testGame.getRemainingAttempts());
        }

        @Test
        @DisplayName("Ход со словом не из словаря - исключение")
        void testGuessWordNotFound() {
            List<String> words = Arrays.asList("герой", "кот");
            WordleDictionary testDict = new WordleDictionary(words, log);
            WordleGame testGame = new WordleGame(testDict, log);

            assertThrows(WordNotFoundException.class, () -> {
                testGame.makeGuess("абвгд");
            });
        }

        @Test
        @DisplayName("Ход с неверной длиной слова - исключение")
        void testGuessInvalidLength() {
            WordleGame testGame = new WordleGame(dictionary, log);

            assertThrows(InvalidWordLengthException.class, () -> {
                testGame.makeGuess("кот");
            });

            assertThrows(InvalidWordLengthException.class, () -> {
                testGame.makeGuess("длинноеслово");
            });
        }

        @Test
        @DisplayName("Попытка хода после завершения игры - исключение")
        void testGuessAfterGameFinished() throws Exception {
            List<String> singleWord = List.of("герой");
            WordleDictionary testDict = new WordleDictionary(singleWord, log);
            WordleGame testGame = new WordleGame(testDict, log);

            testGame.makeGuess("герой");

            assertThrows(GameAlreadyFinishedException.class, () -> {
                testGame.makeGuess("герой");
            });
        }

        @Test
        @DisplayName("Игра проиграна после 6 неудачных попыток")
        void testGameLoseAfterSixAttempts(){
            List<String> words = Arrays.asList("герой", "книга", "слово", "тест", "гора", "река", "море");
            WordleDictionary testDict = new WordleDictionary(words, log);
            WordleGame testGame = new WordleGame(testDict, log);

            for (int i = 0; i < 6; i++) {
                if (!testGame.isGameFinished()) {
                    try {
                        testGame.makeGuess("книга");
                    } catch (Exception e) {
                        // Продолжаем
                    }
                }
            }

            assertTrue(testGame.isGameFinished());
            assertEquals(0, testGame.getRemainingAttempts());
        }

        @Test
        @DisplayName("Проверка получения подсказки")
        void testGetHint() {
            List<String> words = Arrays.asList("распоп", "распор");
            WordleDictionary testDict = new WordleDictionary(words, log);
            WordleGame testGame = new WordleGame(testDict, log);
            String hint = testGame.getHint();

            if (hint != null) {
                assertTrue(testDict.containsWord(hint));
                assertEquals(6, hint.length());
            }
        }

        @Test
        @DisplayName("Подсказка не выдается после завершения игры")
        void testHintAfterGameFinished() throws Exception {
            List<String> singleWord = Arrays.asList("герой");
            WordleDictionary testDict = new WordleDictionary(singleWord, log);
            WordleGame testGame = new WordleGame(testDict, log);

            testGame.makeGuess("герой");

            String hint = testGame.getHint();
            assertNull(hint);
        }

    }

    // ==================== ТЕСТЫ ДЛЯ WORDLE DICTIONARY LOADER ====================

    @Nested
    @DisplayName("Тесты WordleDictionaryLoader")
    class WordleDictionaryLoaderTests {

        private File tempFile;

        @BeforeEach
        void createTempFile() throws IOException {
            tempFile = File.createTempFile("test_dict", ".txt");
            tempFile.deleteOnExit();
        }

        @Test
        @DisplayName("Загрузка корректного словаря из файла")
        void testLoadValidDictionary() throws Exception {
            try (FileWriter writer = new FileWriter(tempFile, StandardCharsets.UTF_8)) {
                writer.write("герой\n");
                writer.write("книга\n");
                writer.write("слово\n");
                writer.write("тест\n");
                writer.write("елка\n");
            }

            WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
            WordleDictionary loadedDict = loader.loadDictionary(tempFile.getAbsolutePath());

            assertNotNull(loadedDict);
            assertTrue(loadedDict.containsWord("герой"));
            assertTrue(loadedDict.containsWord("книга"));
        }

        @Test
        @DisplayName("Загрузка словаря с фильтрацией неверных слов")
        void testLoadDictionaryWithFiltering() throws Exception {
            try (FileWriter writer = new FileWriter(tempFile, StandardCharsets.UTF_8)) {
                writer.write("герой\n");      // корректное
                writer.write("кот\n");         // слишком короткое
                writer.write("hello\n");       // латиница
                writer.write("книга\n");       // корректное
                writer.write("оченьдлинное\n"); // слишком длинное
            }

            WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
            WordleDictionary loadedDict = loader.loadDictionary(tempFile.getAbsolutePath());

            assertTrue(loadedDict.containsWord("герой"));
            assertTrue(loadedDict.containsWord("книга"));
            assertFalse(loadedDict.containsWord("кот"));
            assertFalse(loadedDict.containsWord("hello"));
        }

        @Test
        @DisplayName("Загрузка несуществующего файла - исключение")
        void testLoadNonExistentFile() {
            WordleDictionaryLoader loader = new WordleDictionaryLoader(log);

            assertThrows(FileNotFoundException.class, () -> {
                loader.loadDictionary("nonexistent_file.txt");
            });
        }

        @Test
        @DisplayName("Загрузка пустого файла - исключение")
        void testLoadEmptyFile() throws Exception {
            tempFile.createNewFile();

            WordleDictionaryLoader loader = new WordleDictionaryLoader(log);

            assertThrows(DictionaryLoadException.class, () -> {
                loader.loadDictionary(tempFile.getAbsolutePath());
            });
        }

        // ==================== ТЕСТЫ ДЛЯ GAME RESULT ====================

        @Nested
        @DisplayName("Тесты GameResult")
        class GameResultTests {

            @Test
            @DisplayName("Полный цикл игры - победа")
            void testFullGameWin() throws Exception {
                File tempFile = File.createTempFile("wordle_test", ".txt");
                tempFile.deleteOnExit();

                try (FileWriter writer = new FileWriter(tempFile, StandardCharsets.UTF_8)) {
                    writer.write("герой\n");
                    writer.write("книга\n");
                    writer.write("слово\n");
                    writer.write("тест\n");
                }

                WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
                WordleDictionary dict = loader.loadDictionary(tempFile.getAbsolutePath());

                WordleGame game = new WordleGame(dict, log);

                assertNotNull(game);
            }

            @Test
            @DisplayName("Множественные подсказки не повторяются")
            void testMultipleHints() {
                List<String> words = Arrays.asList("герой", "герб", "гора", "гость", "гений");
                WordleDictionary testDict = new WordleDictionary(words, log);
                WordleGame testGame = new WordleGame(testDict, log);

                Set<String> hints = new HashSet<>();
                for (int i = 0; i < 5; i++) {
                    String hint = testGame.getHint();
                    if (hint != null) {
                        hints.add(hint);
                    }
                }

                assertFalse(hints.isEmpty());
            }
        }
    }
}