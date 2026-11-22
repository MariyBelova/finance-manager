package finance.model;

import java.util.*;
import java.util.stream.Collectors;

    public class Wallet {
        private final List<Transaction> transactions = new ArrayList<>();
        private final Map<String, Double> budgets = new LinkedHashMap<>();

        public void addTransaction(Transaction t) {
            transactions.add(t);
        }

        public List<Transaction> getTransactions() {
            return Collections.unmodifiableList(transactions);
        }

        public Map<String, Double> getBudgets() {
            return Collections.unmodifiableMap(budgets);
        }

        // Бюджет: устанавливаем лимит по категории
        public void setBudget(String category, double limit) {
            if (limit < 0) {
                throw new IllegalArgumentException("Лимит бюджета не может быть отрицательным");
            }
            budgets.put(category, limit);
        }

        public Double getBudget(String category) {
            return budgets.get(category);
        }

        // Остаток бюджета: лимит минус потраченное
        public double getRemainingBudget(String category) {
            Double budget = budgets.get(category);
            if (budget == null) return 0;
            double spent = getCategoryTotal(Transaction.Type.EXPENSE, category);
            return budget - spent;
        }

        // Сумма по типу и категории
        public double getCategoryTotal(Transaction.Type type, String category) {
            return transactions.stream()
                    .filter(t -> t.getType() == type && t.getCategory().equalsIgnoreCase(category))
                    .mapToDouble(Transaction::getAmount)
                    .sum();
        }

        // Итоговые суммы
        public double getTotalIncome() {
            return getTypeTotal(Transaction.Type.INCOME);
        }

        public double getTotalExpenses() {
            return getTypeTotal(Transaction.Type.EXPENSE);
        }

        private double getTypeTotal(Transaction.Type type) {
            return transactions.stream()
                    .filter(t -> t.getType() == type)
                    .mapToDouble(Transaction::getAmount)
                    .sum();
        }

        // Статистика по нескольким категориям (для отчётов)
        public Map<String, Double> categoryExpenses(List<String> categories) {
            return categories.stream()
                    .collect(Collectors.toMap(
                            c -> c,
                            c -> getCategoryTotal(Transaction.Type.EXPENSE, c)
                    ));
        }
    }


