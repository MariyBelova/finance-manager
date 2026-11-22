package finance.service;

import finance.model.*;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.Locale;
public class FinanceService {


    public boolean addIncome(User user, String category, double amount) {
        try {
            user.getWallet().addTransaction(new Transaction(Transaction.Type.INCOME, category, amount));
            return true;
        } catch (IllegalArgumentException e) {
            // Валидация суммы внутри Transaction
            return false;
        }
    }

    public String addExpense(User user, String category, double amount) {
        try {
            Wallet w = user.getWallet();
            double remaining = w.getRemainingBudget(category);
            double spent = w.getCategoryTotal(Transaction.Type.EXPENSE, category);

            // Сначала добавляем — чтобы считать корректно
            w.addTransaction(new Transaction(Transaction.Type.EXPENSE, category, amount));

            double newSpent = spent + amount;
            double budget = w.getBudget(category) != null ? w.getBudget(category) : 0;

            if (budget > 0 && newSpent > budget) {
                return "Превышение бюджета по категории \"" + category + "\"! Потрачено: " + newSpent + ", лимит: " + budget;
            }
            if (budget > 0 && (newSpent >= budget * 0.8)) {
                // Оповещение при 80%
                return "Внимание: израсходовано 80% и более бюджета по категории \"" + category + "\". Текущее: " + newSpent;
            }
            return "Расход успешно добавлен.";
        } catch (IllegalArgumentException e) {
            return "Ошибка: сумма должна быть положительной.";
        }
    }

    public void setBudget(User user, String category, double limit) {
        user.getWallet().setBudget(category, limit);
    }


    public Map<String, Double> reportExpenses(User user, List<String> categories) {
        return user.getWallet().categoryExpenses(categories);
    }

    public ReportData getFullReport(User user) {
        Wallet w = user.getWallet();
        double totalIncome = w.getTotalIncome();
        double totalExpenses = w.getTotalExpenses();
        Map<String, Double> budgets = w.getBudgets();

        List<BudgetStatus> budgetStatuses = budgets.entrySet().stream()
                .map(e -> new BudgetStatus(
                        e.getKey(),
                        e.getValue(),
                        w.getRemainingBudget(e.getKey())
                ))
                .collect(Collectors.toList());

        return new ReportData(totalIncome, totalExpenses, budgetStatuses);
    }


    public static class BudgetStatus {
        public final String category;
        public final double limit;
        public final double remaining;

        public BudgetStatus(String category, double limit, double remaining) {
            this.category = category;
            this.limit = limit;
            this.remaining = remaining;
        }
    }

    public static class ReportData {
        public final double totalIncome;
        public final double totalExpenses;
        public final List<BudgetStatus> budgets;

        public ReportData(double totalIncome, double totalExpenses, List<BudgetStatus> budgets) {
            this.totalIncome = totalIncome;
            this.totalExpenses = totalExpenses;
            this.budgets = budgets;
        }
    }
}

