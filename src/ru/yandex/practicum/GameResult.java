package ru.yandex.practicum;

public record GameResult(String hintLine, boolean wordGuessed, int remainingAttempts) {}