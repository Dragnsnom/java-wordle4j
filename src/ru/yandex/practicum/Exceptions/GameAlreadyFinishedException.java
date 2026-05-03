package ru.yandex.practicum.Exceptions;

/**
 * Исключение, возникающее при попытке сделать ход в уже завершенной игре
 */
public class GameAlreadyFinishedException extends Exception {

    public GameAlreadyFinishedException(String message) {
        super(message);
    }
}