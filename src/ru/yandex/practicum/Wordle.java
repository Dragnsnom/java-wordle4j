package ru.yandex.practicum;

import ru.yandex.practicum.Exceptions.GameAlreadyFinishedException;
import ru.yandex.practicum.Exceptions.InvalidWordLengthException;
import ru.yandex.practicum.Exceptions.WordNotFoundException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Wordle {

    public static void main(String[] args) {
        try (PrintWriter log = createLogFile()) {
            log.println("Программа запущена");

            // 1. Создаем загрузчик словарей
            WordleDictionaryLoader loader = new WordleDictionaryLoader(log);

            // 2. Загружаем словарь
            WordleDictionary dictionary = loader.loadDictionary("words_ru.txt");

            // Проверяем, что словарь не пуст
            if (dictionary == null || dictionary.getSize() == 0) {
                log.println("Ошибка: словарь пуст или не загружен");
                System.err.println("Не удалось загрузить словарь. Игра не может быть начата.");
                return;
            }

            log.println("Словарь загружен. Количество слов: " + dictionary.getSize());

            WordleGame game = new WordleGame(dictionary, log);
            startGameLoop(game, log);
            log.println("Программа завершена");

        } catch (IOException e) {
            System.err.println("Критическая ошибка: не удалось создать лог-файл");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Неожиданная ошибка: " + e.getMessage());
            e.printStackTrace();
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

        log.println("Игровой цикл запущен");
        System.out.println("Добро пожаловать в игру Wordle!");
        System.out.println("Угадайте слово из 5 букв. У вас 6 попыток.");
        System.out.println("Подсказки: + (буква на месте), ^ (буква есть, но не на месте), - (нет такой буквы)");
        System.out.println("Для получения подсказки от компьютера нажмите Enter без ввода слова");
        System.out.println();

        // Игровой цикл продолжается, пока игра не завершена
        while (!game.isGameFinished()) {
            // Выводим количество оставшихся попыток
            System.out.println("\nОсталось попыток: " + game.getRemainingAttempts());
            System.out.print("Введите слово (или нажмите Enter для подсказки): ");

            String userInput = scanner.nextLine().trim().toLowerCase();

            try {
                // Обработка пустого ввода - запрос подсказки
                if (userInput.isEmpty()) {
                    log.println("Пользователь запросил подсказку");
                    String hint = game.getHint();
                    if (hint != null) {
                        System.out.println("Подсказка: " + hint);
                        log.println("Предложена подсказка: " + hint);
                    } else {
                        System.out.println("К сожалению, нет доступных подсказок.");
                        log.println("Нет доступных подсказок");
                    }
                    continue;
                }

                // Делаем ход
                GameResult result = game.makeGuess(userInput);

                // Выводим результат хода
                System.out.println(userInput);
                System.out.println(result.hintLine());

                // Логируем ход
                log.println("Ход: слово='" + userInput + "', результат='" + result.hintLine() + "'");

                // Проверяем, завершена ли игра
                if (result.wordGuessed()) {
                    System.out.println("\nПоздравляем! Вы угадали слово: " + game.getSecretWord());
                    log.println("Игра выиграна! Слово: " + game.getSecretWord());
                } else if (game.isGameFinished()) {
                    System.out.println("\nИгра окончена! Вы не угадали слово.");
                    System.out.println("Загаданное слово было: " + game.getSecretWord());
                    log.println("Игра проиграна. Слово: " + game.getSecretWord());
                }

            } catch (WordNotFoundException e) {
                System.out.println("Ошибка: слово \"" + userInput + "\" не найдено в словаре.");
                System.out.println("Попробуйте другое слово из 5 букв.");
                log.println("Ошибка: слово не найдено в словаре - " + userInput);

            } catch (InvalidWordLengthException e) {
                System.out.println("Ошибка: слово должно состоять из 5 букв.");
                System.out.println("Вы ввели слово длиной " + userInput.length() + " букв.");
                log.println("Ошибка: неверная длина слова - " + userInput);

            } catch (IllegalArgumentException | GameAlreadyFinishedException e) {
                System.out.println("Ошибка: " + e.getMessage());
                log.println("Ошибка валидации: " + e.getMessage());
            }
        }

        scanner.close();
        log.println("Игровой цикл завершен");
    }
}