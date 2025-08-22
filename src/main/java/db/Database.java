package db;

import java.sql.*;
import java.io.File;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:bot.db";

    static {
        System.out.println("Путь к базе данных: " + new File("bot.db").getAbsolutePath());
    }

    // Подключение к БД
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        conn.setAutoCommit(true); // Важно для сохранения данных!
        return conn;
    }

    // Создание таблиц
    public static void init() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            // Создаем таблицу администраторов (если её нет)
            stmt.execute("CREATE TABLE IF NOT EXISTS admins (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "chat_id INTEGER UNIQUE, " +
                    "username TEXT)");
            // Таблица заказов
            stmt.execute("CREATE TABLE IF NOT EXISTS orders (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "chat_id INTEGER, " +
                    "country TEXT, " +
                    "brand_model TEXT, " +
                    "budget TEXT, " +
                    "phone TEXT, " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");

            System.out.println("Таблица orders создана/проверена");
        } catch (SQLException e) {
            System.err.println("Ошибка при создании таблиц:");
            e.printStackTrace();
        }
    }
}