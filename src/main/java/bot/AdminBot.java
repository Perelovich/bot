package bot;

import db.AdminDAO;
import db.Database;
import db.OrderDAO;
import model.Order;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import util.BotConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminBot extends TelegramLongPollingBot {
    @Override
    public String getBotUsername() {
        return "admnistrtivecarorderbot";
    }

    @Override
    public String getBotToken() {
        return BotConfig.getAdminBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (text.equals("/orders")) {
                handleListOrders(chatId);
            }
        }
    }

    private void handleListOrders(long chatId) {
        System.out.println("Запрос заказов из БД...");
        List<Order> orders = OrderDAO.getAllOrders();
        System.out.println("Найдено заказов: " + orders.size());

        if (orders.isEmpty()) {
            sendMessage(chatId, "ℹ️ Список заказов пуст");
            return;
        }
        if (!AdminDAO.isAdmin(chatId)) {
            sendMessage(chatId, "❌ Доступ запрещен");
            return;
        }
        StringBuilder response = new StringBuilder("📋 Последние заказы:\n\n");
        for (Order order : orders) {
            response.append(String.format(
                    "🌍 Страна: %s\n🚗 Авто: %s\n💰 Бюджет: %s\n📞 Телефон: %s\n\n",
                    order.getCountry(), order.getBrandModel(),
                    order.getBudget(), order.getPhone()
            ));
        }

        sendMessage(chatId, response.toString());
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    public static List<Order> getAllOrders() {
        System.out.println("=== ЗАПРОС ВСЕХ ЗАКАЗОВ ==="); // Логирование
        List<Order> orders = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM orders")) {

            while (rs.next()) {
                Order order = new Order(
                        rs.getLong("chat_id"),
                        rs.getString("country"),
                        rs.getString("brand_model"),
                        rs.getString("budget"),
                        rs.getString("phone")
                );
                System.out.println("Найден заказ: " + order); // Логирование
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при запросе заказов:");
            e.printStackTrace();
        }
        return orders;
    }
    private void handleAddAdmin(long chatId, String command) {
        if (!AdminDAO.isAdmin(chatId)) {
            sendMessage(chatId, "❌ У вас нет прав администратора");
            return;
        }

        String[] parts = command.split(" ");
        if (parts.length == 2) {
            String username = parts[1].replace("@", "");
            AdminDAO.addAdmin(0, username); // chat_id=0 для username-админов
            sendMessage(chatId, "✅ Администратор @" + username + " добавлен");
        } else {
            sendMessage(chatId, "Используйте: /add_admin username");
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
}