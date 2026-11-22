package finance.model;

public class User {
    private final String login;
    private final String password;
    private final Wallet wallet = new Wallet();

    public User(String login, String password) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Логин не может быть пустым");
        }
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Пароль должен быть не короче 4 символов");
        }
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }
    public String getPassword() {
        return password;
    }
    public boolean checkPassword(String pwd) {
        return password.equals(pwd);
    }

    public Wallet getWallet() {
        return wallet;
    }
}

