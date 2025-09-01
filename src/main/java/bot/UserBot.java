package bot;

import db.AdminDAO;
import db.UserDAO;
import model.Order;
import model.UserState;
import db.OrderDAO;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import util.BotConfig;
import util.BotConstants;
import util.KeyboardFactory;
import java.sql.Timestamp;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.awt.SystemColor.text;

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
        if (!update.hasMessage()) {
            return;
        }


        long chatId = update.getMessage().getChatId();
        UserState state = userStates.getOrDefault(chatId, UserState.NONE);
        lastActivity.put(chatId, System.currentTimeMillis());

        if (update.getMessage().hasContact() && state == UserState.AWAITING_PHONE) {
            String phone = update.getMessage().getContact().getPhoneNumber();
            finalizeOrder(chatId, phone);
        } else if (update.getMessage().hasText()) {
            String text = update.getMessage().getText();

            if (text.equals("/start")) {
                UserDAO.addUser(chatId);//добавка для счетчика
                sendMessage(chatId, BotConstants.WELCOME_MESSAGE, KeyboardFactory.createCountryKeyboard());
                userStates.put(chatId, UserState.AWAITING_COUNTRY);

                // Создаем новый заказ и СРАЗУ сохраняем имя пользователя
                Order newOrder = new Order(chatId);
                String userName = update.getMessage().getFrom().getFirstName(); // Получаем имя
                newOrder.setUserName(userName); // Устанавливаем его
                tempOrders.put(chatId, newOrder);
            } else if (text.equals("📞 Связаться с администратором")) {
                sendMessage(chatId, BotConstants.CONTACT_ADMIN_MESSAGE, null);
            } else {
                handleUserState(chatId, text);
            }
        }
    }

    private void handleUserState(long chatId, String text) {
        UserState state = userStates.getOrDefault(chatId, UserState.NONE);
        Order order;

        switch (state) {
            case AWAITING_COUNTRY:
                order = tempOrders.get(chatId);

                if (text.equals("🇯🇵 Япония")) {
                    sendMessage(chatId, BotConstants.JAPAN_WARNING, KeyboardFactory.createJapanConfirmationKeyboard());
                } else if (text.equals("✅ Продолжить с Японией")) {
                    if (order != null) {
                        order.setCountry("🇯🇵 Япония");
                    }
                    sendMessage(chatId, BotConstants.BRAND_MODEL_QUESTION, new ReplyKeyboardRemove(true));
                    userStates.put(chatId, UserState.AWAITING_BRAND_MODEL);
                } else if (text.equals("🇰🇷 Корея") || text.equals("🇨🇳 Китай") || text.equals("🇩🇪 Германия")) {
                    if (order != null) {
                        order.setCountry(text);
                    }
                    sendMessage(chatId, BotConstants.BRAND_MODEL_QUESTION, new ReplyKeyboardRemove(true));
                    userStates.put(chatId, UserState.AWAITING_BRAND_MODEL);
                } else {
                    sendMessage(chatId, "Пожалуйста, используйте кнопки для выбора.");
                }
                break;

            case AWAITING_BRAND_MODEL:
                order = tempOrders.get(chatId);
                if (order != null) {
                    order.setBrandModel(text);
                }
                sendMessage(chatId, BotConstants.BUDGET_QUESTION, null);
                userStates.put(chatId, UserState.AWAITING_BUDGET);
                break;

            case AWAITING_BUDGET:
                order = tempOrders.get(chatId);
                if (order != null) {
                    order.setBudget(text);
                }
                requestPhoneNumber(chatId);
                userStates.put(chatId, UserState.AWAITING_PHONE);
                break;

            case AWAITING_PHONE:
                if (text.matches("^(\\+)?(\\d{10,15})$")) {
                    finalizeOrder(chatId, text);
                } else {
                    sendMessage(chatId, "Неверный формат номера. Пожалуйста, используйте кнопку или введите номер в формате +79123456789");
                }
                break;
        }
    }

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

    private void requestPhoneNumber(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("📞 Отлично! Теперь, пожалуйста, поделитесь вашим номером телефона, нажав на кнопку ниже. Либо, укажите Ваш номер телефона в формате 7ХХХХХХХХХ\n" +
                "\n" +
                "⚠\uFE0FНомер нужен для авторизации, проверка защиты от ботов, благодарим за понимание.");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        KeyboardButton contactButton = new KeyboardButton("📱 Поделиться контактом");
        contactButton.setRequestContact(true);
        KeyboardRow row = new KeyboardRow();
        row.add(contactButton);
        keyboardMarkup.setKeyboard(List.of(row));
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void finalizeOrder(long chatId, String phone) {
        Order order = tempOrders.get(chatId);
        if (order != null) {
            order.setPhone(phone);
            order.setOrderTime(new Timestamp(System.currentTimeMillis()));
            OrderDAO.saveOrder(order);
            tempOrders.remove(chatId);

            sendMessage(chatId, BotConstants.ORDER_COMPLETE_MESSAGE, new ReplyKeyboardRemove(true));
            userStates.put(chatId, UserState.NONE);
            // --- НОВАЯ ЛОГИКА: УВЕДОМЛЕНИЕ АДМИНОВ ---
            notifyAdmins(order);
        } else {
            sendMessage(chatId, "Произошла ошибка, попробуйте начать заново: /start");
        }
    }

    public ConcurrentHashMap<Long, Long> getLastActivityMap() {
        return lastActivity;
    }

    public ConcurrentHashMap<Long, UserState> getUserStates() {
        return userStates;
    }
    private void notifyAdmins(Order order) {
        // Предполагается, что у вас в AdminDAO есть метод, возвращающий ID админов
        List<Long> adminIds = AdminDAO.getAdminChatIds();

        String messageText = String.format(
                "🔔 Новый заказ!\n\n" +
                        "👤 Клиент: %s\n" +
                        "🌍 Страна: %s\n" +
                        "🚗 Авто: %s\n" +
                        "💰 Бюджет: %s\n" +
                        "📞 Телефон: %s",
                order.getUserName(), order.getCountry(),
                order.getBrandModel(), order.getBudget(), order.getPhone()
        );

        for (Long adminId : adminIds) {
            // Тут нужно использовать метод sendMessage из вашего AdminBot'a
            // или создать экземпляр AdminBot для отправки.
            // Простой вариант - временно создать SendMessage прямо здесь.
            SendMessage message = new SendMessage(String.valueOf(adminId), messageText);
            try {
                execute(message); // Отправляем от имени UserBot
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}