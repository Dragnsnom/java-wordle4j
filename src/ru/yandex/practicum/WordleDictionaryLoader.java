package ru.yandex.practicum;

import ru.yandex.practicum.Exceptions.DictionaryLoadException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WordleDictionaryLoader {

    private final PrintWriter log;

    public WordleDictionaryLoader(PrintWriter log) {
        this.log = log;
        log.println("WordleDictionaryLoader создан");
    }

    /**
     * Загружает словарь из файла.
     * В случае ошибки ввода-вывода бросает НЕПРОВЕРЯЕМОЕ DictionaryLoadException,
     * так как без словаря игра не может продолжаться.
     */
    public WordleDictionary loadDictionary(String filename) throws DictionaryLoadException {
        log.println("Начало загрузки словаря из файла: " + filename);

        List<String> words = new ArrayList<>();

        try (FileReader fileReader = new FileReader(filename, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(fileReader)) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                String normalizedWord = WordleDictionary.normalizeWord(line);

                if (WordleDictionary.isValidWord(normalizedWord)) {
                    words.add(normalizedWord);
                    log.println("Строка " + lineNumber + ": добавлено слово '" + normalizedWord + "'");
                }
            }

        } catch (FileNotFoundException e) {
            log.println("Критическая ошибка: файл не найден - " + filename);
            throw new DictionaryLoadException("Файл словаря не найден: " + filename, e);
        } catch (IOException e) {
            log.println("Критическая ошибка при чтении файла: " + e.getMessage());
            throw new DictionaryLoadException("Ошибка при чтении файла словаря: " + e.getMessage(), e);
        }

        log.println("Загрузка завершена. Прочитано слов: " + words.size());

        if (words.isEmpty()) {
            throw new DictionaryLoadException("Словарь не содержит слов, подходящих для игры (нужны слова из 5 русских букв)");
        }

        words.sort(String::compareTo);
        return new WordleDictionary(words, log);
    }
}