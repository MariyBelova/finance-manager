package finance.model;

import java.time.LocalDateTime;
import java.util.Locale;

public class Transaction {
    public enum Type { INCOME, EXPENSE }

    private final Type type;
    private final String category;
    private final double amount;
    private final LocalDateTime date;

    public Transaction(Type type, String category, double amount) {
        this(type, category, amount, LocalDateTime.now());
    }

    public Transaction(Type type, String category, double amount, LocalDateTime date) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной");
        }
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.date = date;
    }

    public Type getType() { return type; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public LocalDateTime getDate() { return date; }

    @Override
    public String toString() {
        String label = (type == Type.INCOME) ? "доход" : "расход";
        return String.format(Locale.US, "%s [%s] %.2f (%s)",
                label, category, amount, date.toLocalDate());
    }
}

