package ru.yandex.practicum.Exceptions;

/**
 * Исключение, возникающее когда слово не найдено в словаре
 */
public class WordNotFoundException extends Exception {

    public WordNotFoundException(String message) {
        super(message);
    }
}