package db;

import model.Order;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    // Сохранение заказа
    public static void saveOrder(Order order) {
        // ❗️ 1. Добавляем 'order_time' в SQL-запрос
        String sql = "INSERT INTO orders(chat_id, country, brand_model, budget, phone, user_name, order_time) VALUES(?,?,?,?,?,?,?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, order.getChatId());
            pstmt.setString(2, order.getCountry());
            pstmt.setString(3, order.getBrandModel());
            pstmt.setString(4, order.getBudget());
            pstmt.setString(5, order.getPhone());
            pstmt.setString(6, order.getUserName());
            pstmt.setTimestamp(7, order.getOrderTime()); // ❗️ 2. Добавляем седьмой параметр - время

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Ошибка при сохранении заказа:");
            e.printStackTrace();
        }
    }

    // Получение всех заказов
  /**
     * Retrieves the most recent N orders from the database.
     * @param limit The maximum number of orders to retrieve.
     * @return A list of the latest orders.
     */
    public static List<Order> getLatestOrders(int limit) {
        List<Order> orders = new ArrayList<>();
        // The SQL query is correct, it sorts by time descending and limits the result
        String sql = "SELECT * FROM orders ORDER BY order_time DESC LIMIT ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the limit parameter in the SQL query
            pstmt.setInt(1, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // For each row in the result, create a new Order object
                    Order order = new Order(
                            rs.getLong("chat_id"),
                            rs.getString("country"),
                            rs.getString("brand_model"),
                            rs.getString("budget"),
                            rs.getString("phone"),
                            rs.getString("user_name"),
                            rs.getTimestamp("order_time")
                    );
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching latest orders:");
            e.printStackTrace();
        }

        return orders;
    }
    // ✅ Вставьте этот код в класс db/OrderDAO.java

    public static List<Order> getAllOrders() {
        System.out.println("=== ЗАПРОС ВСЕХ ЗАКАЗОВ ===");
        List<Order> orders = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM orders")) {

            while (rs.next()) {
                // ❗️ ВОТ ИСПРАВЛЕНИЕ:
                // Теперь мы передаем все 7 параметров в новый конструктор,
                // который мы создали ранее в классе Order.
                Order order = new Order(
                        rs.getLong("chat_id"),
                        rs.getString("country"),
                        rs.getString("brand_model"),
                        rs.getString("budget"),
                        rs.getString("phone"),
                        rs.getString("user_name"),    // <-- Добавлено
                        rs.getTimestamp("order_time") // <-- Добавлено
                );
                System.out.println("Найден заказ: " + order);
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при запросе заказов:");
            e.printStackTrace();
        }
        return orders;
    }
    // В файле db/OrderDAO.java

    /**
     * Считает общее количество заказов в базе данных.
     * @return Общее количество заказов.
     */
    public static int countOrders() {
        String sql = "SELECT COUNT(*) FROM orders";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Получает порцию (страницу) заказов из базы данных.
     * @param limit  Количество заказов на странице.
     * @param offset Смещение (сколько заказов пропустить с начала).
     * @return Список заказов для конкретной страницы.
     */
    public static List<Order> getOrders(int limit, int offset) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_time DESC LIMIT ? OFFSET ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            pstmt.setInt(2, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(new Order(
                            rs.getLong("chat_id"),
                            rs.getString("country"),
                            rs.getString("brand_model"),
                            rs.getString("budget"),
                            rs.getString("phone"),
                            rs.getString("user_name"),
                            rs.getTimestamp("order_time")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }
    // В файле db/OrderDAO.java

    private static final List<String> ALLOWED_COLUMNS = List.of("country", "phone", "user_name");

    /**
     * Ищет заказы по заданному критерию.
     * @param column По какой колонке искать (country, phone, user_name).
     * @param query  Что искать (значение).
     * @return Список найденных заказов.
     */
    public static List<Order> findOrders(String column, String query, int limit, int offset) {
        if (!ALLOWED_COLUMNS.contains(column)) {
            return new ArrayList<>(); // Защита от SQL-инъекций
        }
        List<Order> orders = new ArrayList<>();
        // Используем 'LIKE' для частичного совпадения
        String sql = "SELECT * FROM orders WHERE " + column + " LIKE ? ORDER BY order_time DESC LIMIT ? OFFSET ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + query + "%");
            pstmt.setInt(2, limit);
            pstmt.setInt(3, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                // ... (здесь такой же код, как в getOrders, который создает new Order(...) и добавляет в список)
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    /**
     * Считает количество заказов, подходящих под критерий поиска.
     */
    public static int countFoundOrders(String column, String query) {
        if (!ALLOWED_COLUMNS.contains(column)) {
            return 0; // Защита
        }
        String sql = "SELECT COUNT(*) FROM orders WHERE " + column + " LIKE ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + query + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}