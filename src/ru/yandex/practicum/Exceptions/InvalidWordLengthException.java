package ru.yandex.practicum.Exceptions;

/**
 * Исключение, возникающее при неверной длине слова
 */
public class InvalidWordLengthException extends Exception {

    public InvalidWordLengthException(String message) {
        super(message);
    }
}
