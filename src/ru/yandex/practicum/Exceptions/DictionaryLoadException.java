package ru.yandex.practicum.Exceptions;

/**
 * Исключение, возникающее при проблемах с загрузкой или состоянием словаря
 */
public class DictionaryLoadException extends Exception {

    public DictionaryLoadException(String message) {
        super(message);
    }

    public DictionaryLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
