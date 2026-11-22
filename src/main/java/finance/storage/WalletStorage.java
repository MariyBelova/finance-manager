package finance.storage;

import finance.model.User;

public interface WalletStorage {
    void save(User user);
    User load(String login);
}
