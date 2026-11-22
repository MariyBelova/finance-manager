package finance.storage;

import finance.model.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.time.LocalDateTime;

public class FileStorage implements WalletStorage {

    private static final Path DATA_DIR = Paths.get("data");

    public FileStorage() {
        // Создаём папку data, если её нет
        try {
            Files.createDirectories(DATA_DIR);
        } catch (IOException e) {
            System.out.println("Не удалось создать папку data: " + e.getMessage());
        }
    }

    @Override
    public void save(User user) {
        Path filePath = DATA_DIR.resolve(user.getLogin() + ".txt");
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write("USER;" + user.getLogin() + ";" + user.getPassword());
            writer.newLine();
            // Сохраняем бюджеты
            for (Map.Entry<String, Double> entry : user.getWallet().getBudgets().entrySet()) {
                writer.write("BUDGET;" + entry.getKey() + ";" + String.format(Locale.US, "%.2f", entry.getValue()));
                writer.newLine();
            }
            // Сохраняем транзакции
            for (Transaction t : user.getWallet().getTransactions()) {
                String type = (t.getType() == Transaction.Type.INCOME) ? "INCOME" : "EXPENSE";
                writer.write(type + ";" + t.getCategory() + ";" +
                        String.format(Locale.US, "%.2f", t.getAmount()) + ";" + t.getDate());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Ошибка сохранения файла: " + e.getMessage());
        }
    }

    @Override
    public User load(String login) {
        Path filePath = DATA_DIR.resolve(login + ".txt");
        if (!Files.exists(filePath)) {
            return null;
        }

        String password = null;
        List<String> lines;

        try {
            lines = Files.readAllLines(filePath);
        } catch (IOException e) {
            System.out.println("Ошибка загрузки файла: " + e.getMessage());
            return null;
        }

        // Сначала находим строку с паролем
        for (String line : lines) {
            if (line.startsWith("USER;")) {
                String[] parts = line.split(";", -1);
                if (parts.length >= 3) {
                    password = parts[2];
                }
                break;
            }
        }
        if (password == null) {
            return null;
        }

        User user = new User(login, password);
        Wallet wallet = user.getWallet();
                for (String line : lines) {
            if (line.isBlank() || line.startsWith("USER;")) continue;
            String[] parts = line.split(";", -1);
            if (parts.length < 3) continue;

            String command = parts[0];
            if ("BUDGET".equals(command) && parts.length >= 3) {
                wallet.setBudget(parts[1], Double.parseDouble(parts[2]));
            } else if ("INCOME".equals(command) || "EXPENSE".equals(command)) {
                Transaction.Type type = "INCOME".equals(command)
                        ? Transaction.Type.INCOME : Transaction.Type.EXPENSE;
                try {
                    LocalDateTime date = LocalDateTime.parse(parts[3]);
                    wallet.addTransaction(new Transaction(type, parts[1],
                            Double.parseDouble(parts[2]), date));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return user;
    }

}
