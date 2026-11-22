package finance;

import finance.model.*;
import finance.service.*;
import finance.storage.*;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static AuthService authService;
    private static FinanceService financeService;
    private static User currentUser;

    public static void main(String[] args) {
        WalletStorage storage = new FileStorage();
        authService = new AuthService(storage);
        financeService = new FinanceService();

        while (true) {
            if (currentUser == null) {
                showAuthMenu();
            } else {
                showFinanceMenu();
            }
        }
    }

    private static void showAuthMenu() {
        System.out.println("\n=== Авторизация ===");
        System.out.println("1  Регистрация");
        System.out.println("2  Вход");
        System.out.print("Выберите действие: ");
        String choice = sc.nextLine().trim();

        switch (choice) {
            case "1":
                register();
                break;
            case "2":
                login();
                break;
            default:
                System.out.println("Неверная команда.");
        }
    }

    private static void register() {
        System.out.print("Логин: ");
        String login = sc.nextLine().trim();
        System.out.print("Пароль (минимум 4 символа): ");
        String password = sc.nextLine();

        if (authService.register(login, password)) {
            System.out.println("Пользователь зарегистрирован.");
        } else {
            System.out.println("Такой логин уже занят.");
        }
    }

    private static void login() {
        System.out.print("Логин: ");
        String login = sc.nextLine().trim();
        System.out.print("Пароль: ");
        String password = sc.nextLine();

        User user = authService.login(login, password);
        if (user != null) {
            currentUser = user;
            System.out.println("Вы вошли как: " + currentUser.getLogin());
        } else {
            System.out.println("Неверный логин или пароль.");
        }
    }

    private static void showFinanceMenu() {
        System.out.println("\n=== Финансы (" + currentUser.getLogin() + ") ===");
        System.out.println("1  Добавить доход");
        System.out.println("2  Добавить расход");
        System.out.println("3  Установить бюджет по категории");
        System.out.println("4  Отчёт по выбранным категориям (через запятую)");
        System.out.println("5  Полный отчёт (доходы, расходы, бюджеты)");
        System.out.println("6  CSV-экспорт операций");
        System.out.println("h  Справка (примеры команд)");
        System.out.println("0  Выход (с сохранением)");
        System.out.print("Действие: ");

        String choice = sc.nextLine().trim();

        switch (choice) {
            case "1": addIncome(); break;
            case "2": addExpense(); break;
            case "3": setBudget(); break;
            case "4": reportByCategories(); break;
            case "5": fullReport(); break;
            case "6": exportCsv(); break;
            case "h": showHelp(); break;
            case "0":
                System.out.println("Выход. Данные сохранены.");
                System.exit(0);
            default:
                System.out.println("Неизвестная команда. Нажмите h для справки.");
        }
    }

    // --- Реализация команд ---

    private static void addIncome() {
        System.out.print("Категория: ");
        String category = sc.nextLine().trim();
        if (category.isBlank()) {
            System.out.println("Категория не может быть пустой.");
            return;
        }
        System.out.print("Сумма: ");
        String amountStr = sc.nextLine().trim().replace(',', '.');
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: введите корректное число.");
            return;
        }

        boolean ok = financeService.addIncome(currentUser, category, amount);
        System.out.println(ok ? "Доход добавлен." : "Ошибка добавления дохода.");
    }

    private static void addExpense() {
        System.out.print("Категория: ");
        String category = sc.nextLine().trim();
        if (category.isBlank()) {
            System.out.println("Категория не может быть пустой.");
            return;
        }
        System.out.print("Сумма: ");
        String amountStr = sc.nextLine().trim().replace(',', '.');
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: введите корректное число.");
            return;
        }

        String result = financeService.addExpense(currentUser, category, amount);
        System.out.println(result); // Сюда попадают и успех, и предупреждения
    }

    private static void setBudget() {
        System.out.print("Категория: ");
        String category = sc.nextLine().trim();
        if (category.isBlank()) {
            System.out.println("Категория не может быть пустой.");
            return;
        }
        System.out.print("Лимит (число): ");
        String limitStr = sc.nextLine().trim().replace(',', '.');
        double limit;
        try {
            limit = Double.parseDouble(limitStr);
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: введите корректный лимит.");
            return;
        }

        financeService.setBudget(currentUser, category, limit);
        System.out.println("Бюджет установлен: категория \"" + category + "\", лимит " + limit);
    }

    private static void reportByCategories() {
        System.out.print("Категории через запятую (например: Еда,Такси): ");
        String input = sc.nextLine().trim();
        List<String> categories = Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(c -> !c.isBlank())
                .collect(Collectors.toList());

        if (categories.isEmpty()) {
            System.out.println("Нет категорий для отчёта.");
            return;
        }

        Map<String, Double> report = financeService.reportExpenses(currentUser, categories);
        System.out.println("--- Расходы по категориям ---");
        report.forEach((cat, sum) ->
                System.out.printf("%s: %.2f%n", cat, sum));
    }

    private static void fullReport() {
        FinanceService.ReportData data = financeService.getFullReport(currentUser);

        System.out.println("=== Полный отчёт ===");
        System.out.printf("Общий доход: %.2f%n", data.totalIncome);
        System.out.printf("Общие расходы: %.2f%n", data.totalExpenses);
        System.out.println("--- Бюджеты ---");
        for (FinanceService.BudgetStatus bs : data.budgets) {
            System.out.printf("%s | лимит: %.2f | остаток: %.2f%n",
                    bs.category, bs.limit, bs.remaining);
        }
    }

    private static void exportCsv() {
        Path dir = Paths.get("export");
        try {
            Files.createDirectories(dir);
        } catch (IOException ignored) {}

        Path file = dir.resolve(currentUser.getLogin() + "_transactions_" +
                LocalDateTime.now().toString().replaceAll("[:T-]", "_") + ".csv");

        List<Transaction> transactions = currentUser.getWallet().getTransactions();
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write("type;category;amount;date\n");
            for (Transaction t : transactions) {
                String type = (t.getType() == Transaction.Type.INCOME) ? "INCOME" : "EXPENSE";
                writer.write(String.format(Locale.US, "%s;%s;%.2f;%s\n",
                        type, t.getCategory(), t.getAmount(), t.getDate()));
            }
        } catch (IOException e) {
            System.out.println("Ошибка экспорта: " + e.getMessage());
            return;
        }
        System.out.println("CSV-файл создан: " + file.toAbsolutePath());
    }

    private static void showHelp() {
        System.out.println("""
            Примеры команд:
              1  — доход: категория «Продукты», сумма 5000
              2  — расход: категория «Такси», сумма 300
              3  — бюджет: категория «Развлечения», лимит 2000
              4  — отчёт по категориям: введите «Еда,Транспорт»
              5  — полный отчёт
              6  — CSV-экспорт в папку export/
              h  — эта справка
              0  — выход
            """);
    }
}
