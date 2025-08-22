package bot;

import model.UserState;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import util.BotConfig;
import util.BotConstants;
import util.KeyboardFactory;
import model.Order;
import model.UserState;
import db.OrderDAO;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class UserBot extends TelegramLongPollingBot {
    private final ConcurrentHashMap<Long, UserState> userStates = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Long> lastActivity = new ConcurrentHashMap<>();
 private final Map<Long, Order> tempOrders = new ConcurrentHashMap<>();

    @Override
    public String getBotUsername() {
        return "Lesha2417teBot";
    }

    @Override
    public String getBotToken() {
        return BotConfig.getUserBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            // Переменная называется 'text', а не 'messageText'
            String text = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            System.out.println("\n=== Новое сообщение ===");
            System.out.println("ChatID: " + chatId);
            System.out.println("Текст: " + text);
            System.out.println("Текущее состояние: " + userStates.get(chatId));

            lastActivity.put(chatId, System.currentTimeMillis());

            // Используем переменную 'text' вместо 'messageText'
            if (text.equals("/start")) {
                sendMessage(chatId, BotConstants.WELCOME_MESSAGE, KeyboardFactory.createCountryKeyboard());
                userStates.put(chatId, UserState.CHOOSE_COUNTRY);
                tempOrders.put(chatId, new Order(chatId)); // Не забываем создать заказ
            } else if (text.equals("📞 Связаться с администратором")) {
                sendMessage(chatId, BotConstants.CONTACT_ADMIN_MESSAGE, null);
            } else {
                handleUserState(chatId, text); // Передаем 'text' а не 'messageText'
            }
        }
    }

    private void handleUserState(long chatId, String text) {
        UserState state = userStates.getOrDefault(chatId, UserState.START);
        switch (state) {
            case CHOOSE_COUNTRY:
                if (text.equals("🇯🇵 Япония")) {
                    sendMessage(chatId, BotConstants.JAPAN_WARNING, KeyboardFactory.createCountryKeyboardWithoutJapan());
                } else {
                    // Сохраняем страну в заказ
                    Order order = tempOrders.get(chatId);
                    if (order != null) {
                        order.setCountry(text);
                    }
                    sendMessage(chatId, BotConstants.BRAND_MODEL_QUESTION, new ReplyKeyboardRemove(true));
                    userStates.put(chatId, UserState.ENTER_BRAND_MODEL);
                }
                break;

            case ENTER_BRAND_MODEL:
                // Сохраняем модель в заказ
                Order order = tempOrders.get(chatId);
                if (order != null) {
                    order.setBrandModel(text);
                }
                sendMessage(chatId, BotConstants.BUDGET_QUESTION, null);
                userStates.put(chatId, UserState.ENTER_BUDGET);
                break;

            case ENTER_BUDGET:
                // Сохраняем бюджет в заказ
                order = tempOrders.get(chatId);
                if (order != null) {
                    order.setBudget(text);
                }
                sendMessage(chatId, BotConstants.PHONE_QUESTION, null);
                userStates.put(chatId, UserState.ENTER_PHONE);
                break;

            case ENTER_PHONE:
                if (text.matches("7\\d{10}")) {
                    // Получаем и сохраняем заказ
                    order = tempOrders.get(chatId);
                    if (order != null) {
                        order.setPhone(text);
                        OrderDAO.saveOrder(order); // Вот это было пропущено!
                        tempOrders.remove(chatId);
                    }
                    sendMessage(chatId, BotConstants.ORDER_COMPLETE_MESSAGE, null);
                    userStates.put(chatId, UserState.ORDER_CONFIRMED);
                } else {
                    sendMessage(chatId, "Неверный формат номера. Пример: 79123456789", null);
                }
                break;
        }
    }
    // Новый метод для простых сообщений
    public void sendMessage(long chatId, String text) {
        sendMessage(chatId, text, null);
    }
    public void sendMessage(long chatId, String text, ReplyKeyboard replyMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        if (replyMarkup != null) {
            message.setReplyMarkup(replyMarkup);
        }
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public ConcurrentHashMap<Long, Long> getLastActivityMap() {
        return lastActivity;
    }

    public ConcurrentHashMap<Long, UserState> getUserStates() {
        return userStates;
    }
    private void handlePhone(long chatId, String phone) {
        Order order = tempOrders.get(chatId);
        if (order == null) {
            System.err.println("❌ Заказ не найден для chatId: " + chatId);
            return;
        }

        System.out.println("Перед сохранением: " + order); // Проверяем объект
        order.setPhone(phone);
        OrderDAO.saveOrder(order);
        tempOrders.remove(chatId);
    }
    private void handleCountrySelection(long chatId, String country) {
        System.out.println("=== Выбор страны ===");
        System.out.println("Текущее состояние: " + userStates.get(chatId));

        Order order = tempOrders.get(chatId);
        if (order == null) {
            System.err.println("❌ tempOrders пуст для chatId: " + chatId);
            return;
        }
        order.setCountry(country);
        System.out.println("Страна сохранена: " + country);
    }
    private void debugTempOrders() {
        System.out.println("=== Содержимое tempOrders ===");
        tempOrders.forEach((chatId, order) -> {
            System.out.println("ChatID: " + chatId + " -> " + order);
        });
    }
}