package db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:bot.db";

    static {
        System.out.println("Путь к базе данных: " + new File("bot.db").getAbsolutePath());
    }

    // Подключение к БД
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // Создание и инициализация таблиц
    public static void init() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Таблица администраторов
            stmt.execute("CREATE TABLE IF NOT EXISTS admins (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "chat_id BIGINT UNIQUE, " + // <-- (Рекомендация) Заменено на BIGINT
                    "username TEXT)");

            // Таблица заказов
            stmt.execute("CREATE TABLE IF NOT EXISTS orders (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "chat_id BIGINT, " +          // <-- (Рекомендация) Заменено на BIGINT
                    "country TEXT, " +
                    "brand_model TEXT, " +
                    "budget TEXT, " +
                    "phone TEXT, " +
                    "user_name TEXT, " +          // <-- 1. ДОБАВЛЕНА КОЛОНКА
                    "order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"); // <-- 2. ПЕРЕИМЕНОВАНА КОЛОНКА
            //подсчет пользователей
            stmt.execute("CREATE TABLE IF NOT EXISTS bot_users (" +
                    "chat_id BIGINT PRIMARY KEY)");
            System.out.println("Таблицы созданы/проверены");

        } catch (SQLException e) {
            System.err.println("Ошибка при создании таблиц:");
            e.printStackTrace();
        }
    }
}