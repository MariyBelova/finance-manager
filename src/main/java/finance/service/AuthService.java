package finance.service;

import finance.model.User;
import finance.storage.WalletStorage;

import java.util.HashMap;
import java.util.Map;

public class AuthService {

    private final Map<String, User> users = new HashMap<>();
    private final WalletStorage storage;

    public AuthService(WalletStorage storage) {
        this.storage = storage;
    }

    public boolean register(String login, String password) {
        if (users.containsKey(login)) {
            return false;
        }
        User user = new User(login, password);
        users.put(login, user);
        storage.save(user);
        return true;
    }

    public User login(String login, String password) {
        User storedUser = storage.load(login);
        if (storedUser == null) {
            return null;
        }
        return storedUser.checkPassword(password) ? storedUser : null;
    }

    public User getUser(String login) {
        return users.get(login);
    }
}
