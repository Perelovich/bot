package db;

import java.sql.*;

public class UserDAO {
    public static void saveUser(long chatId, String phone) {
        String sql = "INSERT OR IGNORE INTO users(chat_id, phone) VALUES(?,?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, chatId);
            pstmt.setString(2, phone);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean isAdmin(long chatId) {
        String sql = "SELECT 1 FROM admins WHERE chat_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, chatId);
            return pstmt.executeQuery().next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static void addUser(long chatId) {
        String sql = "INSERT OR IGNORE INTO bot_users(chat_id) VALUES(?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, chatId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Считает общее количество уникальных пользователей.
     */
    public static int countUsers() {
        String sql = "SELECT COUNT(*) FROM bot_users";
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
}