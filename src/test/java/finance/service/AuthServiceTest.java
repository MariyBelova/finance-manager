package finance.service;

import finance.storage.FileStorage;
import finance.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private AuthService authService;
    private Path dataDir = Paths.get("data");

    @BeforeEach
    void setUp() throws IOException {
        // Очищаем папку data перед каждым тестом
        if (Files.exists(dataDir)) {
            Files.walk(dataDir)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try { Files.delete(path); } catch (Exception ignored) {}
                    });
            Files.deleteIfExists(dataDir);
        }
        Files.createDirectories(dataDir);

        authService = new AuthService(new FileStorage());
    }

    @AfterEach
    void tearDown() {
        // Можно оставить папку data, чтобы видеть файлы после тестов
    }

    @Test
    void register_and_login() {
        assertTrue(authService.register("alice", "secret"));
        User logged = authService.login("alice", "secret");
        assertNotNull(logged);
        assertEquals("alice", logged.getLogin());
    }

    @Test
    void duplicate_login_rejected() {
        assertTrue(authService.register("bob", "pass"));
        assertFalse(authService.register("bob", "other"));
    }

    @Test
    void invalid_password() {
        authService.register("carol", "1234");
        assertNull(authService.login("carol", "wrong"));
    }

    @Test
    void file_storage_persists_data() {
        authService.register("dave", "pass123");
        // Пересоздаём сервис, чтобы проверить загрузку из файла
        AuthService newAuth = new AuthService(new FileStorage());
        User loaded = newAuth.login("dave", "pass123");
        assertNotNull(loaded);
    }
}

