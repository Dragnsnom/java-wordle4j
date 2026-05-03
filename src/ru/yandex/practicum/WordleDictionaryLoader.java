package ru.yandex.practicum;

import ru.yandex.practicum.Exceptions.DictionaryLoadException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WordleDictionaryLoader {

    private final PrintWriter log;

    public WordleDictionaryLoader(PrintWriter log) {
        this.log = log;
        log.println("WordleDictionaryLoader создан");
    }

    public WordleDictionary loadDictionary(String filename) throws IOException, DictionaryLoadException {
        log.println("Начало загрузки словаря из файла: " + filename);

        List<String> words = new ArrayList<>();

        try (FileReader fileReader = new FileReader(filename, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(fileReader)) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                String normalizedWord = WordleDictionary.normalizeWord(line);

                if (WordleDictionary.isValidWord(normalizedWord)) {
                    words.add(normalizedWord);
                    log.println("Строка " + lineNumber + ": добавлено слово '" + normalizedWord + "'");
                } else {
                    log.println("Строка " + lineNumber + ": слово '" + normalizedWord + "' не подходит (длина "
                            + normalizedWord.length() + "), пропущено");
                }
            }

        } catch (FileNotFoundException e) {
            log.println("Ошибка: файл не найден - " + filename);
            throw new FileNotFoundException("Файл словаря не найден: " + filename);
        } catch (IOException e) {
            log.println("Ошибка при чтении файла: " + e.getMessage());
            throw new IOException("Ошибка при чтении файла словаря: " + e.getMessage(), e);
        }

        log.println("Загрузка завершена. Прочитано слов: " + words.size());

        if (words.isEmpty()) {
            log.println("Ошибка: словарь пуст или не содержит подходящих слов");
            throw new DictionaryLoadException("Словарь не содержит слов, подходящих для игры (нужны слова из 5 русских букв)");
        }

        words.sort(String::compareTo);
        log.println("Словарь отсортирован по алфавиту");

        return new WordleDictionary(words, log);
    }
}