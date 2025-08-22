package db;

import model.Order;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    // Сохранение заказа
    public static void saveOrder(Order order) {
        System.out.println("=== Попытка сохранения ===");
        System.out.println("Данные: " + order); // Проверяем объект

        String sql = "INSERT INTO orders(chat_id, country, brand_model, budget, phone) VALUES(?,?,?,?,?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Логируем параметры
            System.out.println("Параметры SQL: " +
                    order.getChatId() + ", " +
                    order.getCountry() + ", " +
                    order.getBrandModel() + ", " +
                    order.getBudget() + ", " +
                    order.getPhone());

            pstmt.setLong(1, order.getChatId());
            pstmt.setString(2, order.getCountry());
            pstmt.setString(3, order.getBrandModel());
            pstmt.setString(4, order.getBudget());
            pstmt.setString(5, order.getPhone());

            int rows = pstmt.executeUpdate();
            System.out.println("Строк затронуто: " + rows); // Должно быть 1

        } catch (SQLException e) {
            System.err.println("❌ Ошибка SQL:");
            e.printStackTrace();
        }
    }

    // Получение всех заказов
    public static List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY timestamp DESC";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Order order = new Order(
                        rs.getLong("chat_id"),
                        rs.getString("country"),
                        rs.getString("brand_model"),
                        rs.getString("budget"),
                        rs.getString("phone")
                );
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при загрузке заказов:");
            e.printStackTrace();
        }
        return orders;
    }
}