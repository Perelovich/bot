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
}