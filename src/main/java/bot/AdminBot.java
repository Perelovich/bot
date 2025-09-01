package bot;

import db.AdminDAO;
import db.OrderDAO;
import model.Order;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import util.BotConfig;

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

    private static final int PAGE_SIZE = 5;

    @Override
    public void onUpdateReceived(Update update) {
        // 1. Обработка нажатий на инлайн-кнопки
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();

            if (callbackData.startsWith("page_")) {
                int page = Integer.parseInt(callbackData.split("_")[1]);
                updateOrdersPage(chatId, messageId, page);
            }
            return;
        }

        // 2. Обработка текстовых команд
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String username = update.getMessage().getFrom().getUserName();

            // Проверяем права администратора (и активируем при необходимости)
            if (!AdminDAO.isAdmin(chatId, username)) {
                sendMessage(chatId, "❌ Доступ запрещен.");
                return;
            }

            // Обработка команд
            if (text.equals("/orders")) {
                showOrdersPage(chatId, 1);
            } else if (text.startsWith("/add_admin")) {
                handleAddAdmin(chatId, text);
            } else if (text.startsWith("/remove_admin")) {
                handleRemoveAdmin(chatId, text);
            } else if (text.equals("/admin_list")) {
                handleAdminList(chatId);
            }
        }
    }

    private void showOrdersPage(long chatId, int page) {
        int totalOrders = OrderDAO.countOrders();
        if (totalOrders == 0) {
            sendMessage(chatId, "ℹ️ Список заказов пуст.");
            return;
        }

        int totalPages = (int) Math.ceil((double) totalOrders / PAGE_SIZE);
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int offset = (page - 1) * PAGE_SIZE;
        List<Order> orders = OrderDAO.getOrders(PAGE_SIZE, offset);

        StringBuilder response = new StringBuilder(String.format("📋 Заказы (Страница %d из %d):\n\n", page, totalPages));
        for (Order order : orders) {
            response.append(String.format(
                    "👤 Клиент: %s (ID: %d)\n" +
                            "🌍 Страна: %s\n" +
                            "🚗 Авто: %s\n" +
                            "💰 Бюджет: %s\n" +
                            "📞 Телефон: %s\n" +
                            "⏰ Время: %s\n\n",
                    order.getUserName(), order.getChatId(), order.getCountry(), order.getBrandModel(),
                    order.getBudget(), order.getPhone(), order.getOrderTime()
            ));
        }

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(response.toString());
        message.setReplyMarkup(createPaginationKeyboard(page, totalPages));

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void updateOrdersPage(long chatId, int messageId, int page) {
        int totalOrders = OrderDAO.countOrders();
        int totalPages = (int) Math.ceil((double) totalOrders / PAGE_SIZE);

        if (page < 1) page = 1;
        if (page > totalPages && totalPages > 0) page = totalPages;

        int offset = (page - 1) * PAGE_SIZE;
        List<Order> orders = OrderDAO.getOrders(PAGE_SIZE, offset);

        StringBuilder response = new StringBuilder(String.format("📋 Заказы (Страница %d из %d):\n\n", page, totalPages));
        if (!orders.isEmpty()){
            for (Order order : orders) {
                response.append(String.format(
                        "👤 Клиент: %s (ID: %d)\n" +
                                "🌍 Страна: %s\n" +
                                "🚗 Авто: %s\n" +
                                "💰 Бюджет: %s\n" +
                                "📞 Телефон: %s\n" +
                                "⏰ Время: %s\n\n",
                        order.getUserName(), order.getChatId(), order.getCountry(), order.getBrandModel(),
                        order.getBudget(), order.getPhone(), order.getOrderTime()
                ));
            }
        } else {
            response.append("На этой странице нет заказов.");
        }

        EditMessageText editedMessage = new EditMessageText();
        editedMessage.setChatId(String.valueOf(chatId));
        editedMessage.setMessageId(messageId);
        editedMessage.setText(response.toString());
        editedMessage.setReplyMarkup(createPaginationKeyboard(page, totalPages));

        try {
            execute(editedMessage);
        } catch (TelegramApiException e) {
            if (!e.getMessage().contains("message is not modified")) {
                e.printStackTrace();
            }
        }
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

    private void handleAddAdmin(long chatId, String command) {
        String[] parts = command.split(" ");
        if (parts.length == 2) {
            String username = parts[1].replace("@", "");
            AdminDAO.addAdmin(0, username);
            sendMessage(chatId, "✅ Администратор @" + username + " успешно добавлен. Ему нужно написать боту, чтобы активировать доступ.");
        } else {
            sendMessage(chatId, "⚠️ Неверный формат. Используйте: /add_admin @username");
        }
    }

    private void handleRemoveAdmin(long chatId, String command) {
        String[] parts = command.split(" ");
        if (parts.length == 2) {
            String username = parts[1].replace("@", "");
            AdminDAO.removeAdmin(username);
            sendMessage(chatId, "✅ Администратор @" + username + " удален.");
        } else {
            sendMessage(chatId, "⚠️ Неверный формат. Используйте: /remove_admin @username");
        }
    }

    private void handleAdminList(long chatId) {
        List<String> admins = AdminDAO.getAdminList();
        if (admins.isEmpty()) {
            sendMessage(chatId, "ℹ️ Список администраторов пуст.");
            return;
        }
        sendMessage(chatId, "👑 Список администраторов:\n" + String.join("\n", admins));
    }

    private InlineKeyboardMarkup createPaginationKeyboard(int currentPage, int totalPages) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        if (currentPage > 1) {
            InlineKeyboardButton backButton = new InlineKeyboardButton();
            backButton.setText("⬅️ Назад");
            backButton.setCallbackData("page_" + (currentPage - 1));
            row.add(backButton);
        }

        if (totalPages > 0) {
            InlineKeyboardButton pageButton = new InlineKeyboardButton();
            pageButton.setText(currentPage + " / " + totalPages);
            pageButton.setCallbackData("noop");
            row.add(pageButton);
        }

        if (currentPage < totalPages) {
            InlineKeyboardButton nextButton = new InlineKeyboardButton();
            nextButton.setText("Вперед ➡️");
            nextButton.setCallbackData("page_" + (currentPage + 1));
            row.add(nextButton);
        }

        if (!row.isEmpty()) {
            rows.add(row);
        }
        markup.setKeyboard(rows);
        return markup;
    }
}