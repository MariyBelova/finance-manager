package finance.service;

import finance.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FinanceServiceTest {

    private FinanceService service;
    private User user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        service = new FinanceService();
        user = new User("testuser", "password");
        wallet = user.getWallet();
    }

    @Test
    void addIncome_success() {
        assertTrue(service.addIncome(user, "Зарплата", 10000));
        assertEquals(10000, wallet.getTotalIncome(), 0.01);
    }

    @Test
    void addExpense_success_and_warning() {
        // Сначала добавим доход, чтобы были деньги
        service.addIncome(user, "Зарплата", 5000);

        String result = service.addExpense(user, "Такси", 4500);
        assertTrue(result.contains("успешно"));
        assertEquals(4500, wallet.getTotalExpenses(), 0.01);

        // Теперь добавим ещё, чтобы превысить бюджет
        service.setBudget(user, "Такси", 5000); // лимит 5000
        String warning = service.addExpense(user, "Такси", 1000); // потратим ещё 1000 → итого 5500
        assertTrue(warning.contains("Превышение бюджета"));
    }

    @Test
    void setBudget_and_remaining() {
        service.setBudget(user, "Еда", 3000);
        assertEquals(3000, user.getWallet().getBudget("Еда"), 0.01);
        service.addExpense(user, "Еда", 1200);
        assertEquals(1800, user.getWallet().getRemainingBudget("Еда"), 0.01);
    }

    @Test
    void negativeAmount_rejected() {
        assertFalse(service.addIncome(user, "Ошибка", -100));
        String res = service.addExpense(user, "Ошибка", -50);
        assertTrue(res.contains("Ошибка"));
    }

    @Test
    void fullReport_data_integrity() {
        service.addIncome(user, "Стипендия", 2000);
        service.addExpense(user, "Книги", 800);
        service.setBudget(user, "Книги", 1000);

        var report = service.getFullReport(user);
        assertEquals(2000, report.totalIncome, 0.01);
        assertEquals(800, report.totalExpenses, 0.01);
        assertEquals(1, report.budgets.size());
    }
}
