package ru.yandex.practicum;

import ru.yandex.practicum.Exceptions.DictionaryLoadException;
import ru.yandex.practicum.Exceptions.GameAlreadyFinishedException;
import ru.yandex.practicum.Exceptions.InvalidWordLengthException;
import ru.yandex.practicum.Exceptions.WordNotFoundException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Wordle {

    public static void main(String[] args) {
        PrintWriter log = null;

        try {
            log = createLogFile();
            log.println("Программа запущена");

            WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
            WordleDictionary dictionary = loader.loadDictionary("words_ru.txt");

            log.println("Словарь загружен. Количество слов: " + dictionary.getSize());

            WordleGame game = new WordleGame(dictionary, log);

            // Запускаем игровой цикл
            startGameLoop(game, log);

            log.println("Программа завершена");

        } catch (DictionaryLoadException e) {
            System.err.println("КРИТИЧЕСКАЯ ОШИБКА: " + e.getMessage());
            log.println("КРИТИЧЕСКАЯ ОШИБКА: " + e.getMessage());
            e.printStackTrace(log);
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Непредвиденная ошибка: " + e.getMessage());
            if (log != null) {
                log.println("Непредвиденная ошибка: " + e.getMessage());
                e.printStackTrace(log);
            }
            System.exit(1);
        } finally {
            if (log != null) {
                log.close();
            }
        }
    }

    private static PrintWriter createLogFile() throws IOException {
        File logFile = new File("game.log");
        FileOutputStream fos = new FileOutputStream(logFile);
        OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
        return new PrintWriter(osw);
    }

    private static void startGameLoop(WordleGame game, PrintWriter log) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Добро пожаловать в игру Wordle!");
        System.out.println("Угадайте слово из 5 букв. У вас 6 попыток.");
        System.out.println("Подсказки: + (буква на месте), ^ (буква есть, но не на месте), - (нет такой буквы)");
        System.out.println("Для получения подсказки от компьютера нажмите Enter без ввода слова");

        while (!game.isGameFinished()) {
            System.out.println("\nОсталось попыток: " + game.getRemainingAttempts());
            System.out.print("Введите слово (или нажмите Enter для подсказки): ");

            String userInput = scanner.nextLine().trim().toLowerCase();

            if (userInput.isEmpty()) {
                String hint = game.getHint();
                if (hint != null) {
                    System.out.println("Подсказка: " + hint);
                } else {
                    System.out.println("К сожалению, нет доступных подсказок.");
                }
                continue;
            }

            try {
                GameResult result = game.makeGuess(userInput);
                System.out.println(userInput);
                System.out.println(result.hintLine());

                if (result.wordGuessed()) {
                    System.out.println("\nПоздравляем! Вы угадали слово: " + game.getSecretWord());
                } else if (game.isGameFinished()) {
                    System.out.println("\nИгра окончена! Вы не угадали слово.");
                    System.out.println("Загаданное слово было: " + game.getSecretWord());
                }

            } catch (WordNotFoundException e) {
                System.out.println("Ошибка: слово \"" + userInput + "\" не найдено в словаре.");
                System.out.println("Попробуйте другое слово из 5 букв.");

            } catch (InvalidWordLengthException e) {
                System.out.println("Ошибка: " + e.getMessage());

            } catch (GameAlreadyFinishedException e) {
                System.out.println("Ошибка: " + e.getMessage());
                break;
            }
        }

        scanner.close();
    }
}