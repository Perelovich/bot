package db;

import model.Order;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    // 1. Сохранение заказа (этот метод у вас был правильный)
    public static void saveOrder(Order order) {
        String sql = "INSERT INTO orders(chat_id, country, brand_model, budget, phone, user_name, order_time) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, order.getChatId());
            pstmt.setString(2, order.getCountry());
            pstmt.setString(3, order.getBrandModel());
            pstmt.setString(4, order.getBudget());
            pstmt.setString(5, order.getPhone());
            pstmt.setString(6, order.getUserName());
            pstmt.setTimestamp(7, order.getOrderTime());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка при сохранении заказа:");
            e.printStackTrace();
        }
    }

    // 2. Подсчет всех заказов для пагинации
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

    // 3. Получение одной страницы заказов для пагинации
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
    
    // Мы пока не реализовывали фильтрацию, поэтому методы findOrders и countFoundOrders
    // можно временно удалить, чтобы они не вызывали ошибок. 
    // Мы вернемся к ним, когда будем делать поиск.
}
