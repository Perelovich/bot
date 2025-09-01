package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AdminDAO {

    public static void addAdmin(long chatId, String username) {
        String sql = "INSERT OR IGNORE INTO admins(chat_id, username) VALUES(?,?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, chatId);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Проверяет права и автоматически "активирует" админа, добавленного по нику.
     * @param chatId Уникальный ID чата пользователя.
     * @param username Ник пользователя в Telegram.
     * @return true, если пользователь является администратором.
     */
    public static boolean isAdmin(long chatId, String username) {
        // Сначала проверяем по chat_id. Если нашли - это точно админ.
        String sqlCheckById = "SELECT 1 FROM admins WHERE chat_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlCheckById)) {
            pstmt.setLong(1, chatId);
            if (pstmt.executeQuery().next()) {
                return true; // Нашли по ID, доступ разрешен
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // Если по chat_id не нашли, ищем по нику, но только те записи, что ждут активации (chat_id = 0)
        if (username != null && !username.isEmpty()) {
            String sqlCheckByUsername = "SELECT 1 FROM admins WHERE username = ? AND chat_id = 0";
            try (Connection conn = Database.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sqlCheckByUsername)) {
                pstmt.setString(1, username);
                if (pstmt.executeQuery().next()) {
                    // Нашли "неактивированного" админа. Активируем его!
                    updateAdminChatId(username, chatId);
                    return true; // Разрешаем доступ, так как он теперь активирован
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Если ничего не нашли, значит, это не админ
        return false;
    }

    /**
     * Обновляет chat_id для администратора, добавленного по username.
     */
    private static void updateAdminChatId(String username, long chatId) {
        String sql = "UPDATE admins SET chat_id = ? WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, chatId);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
            System.out.println("Активирован администратор " + username + " с chat_id " + chatId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeAdmin(String username) {
        String sql = "DELETE FROM admins WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username.replace("@", ""));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getAdminList() {
        List<String> admins = new ArrayList<>();
        String sql = "SELECT username FROM admins WHERE username IS NOT NULL";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                admins.add("@" + rs.getString("username"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return admins;
    }

    public static List<Long> getAdminChatIds() {
        List<Long> adminIds = new ArrayList<>();
        String sql = "SELECT chat_id FROM admins WHERE chat_id != 0";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                adminIds.add(rs.getLong("chat_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return adminIds;
    }
}