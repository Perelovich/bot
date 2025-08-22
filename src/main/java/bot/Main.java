package bot;

import db.AdminDAO;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import util.TimeoutChecker;
import model.Order;
import db.OrderDAO;
import java.util.TimeZone;  // Добавьте этот импорт

public class Main {
    public static void main(String[] args) {
        try {
            // Устанавливаем время по Москве
            System.setProperty("user.timezone", "Europe/Moscow");
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));

            // Инициализация базы данных
            db.Database.init();
            AdminDAO.addAdmin(5208772935L, "Aleksei_tg0");
// После Database.init()
            //                Order testOrder = new Order(12345, "Тест", "Тест", "100000", "79990001122");
            //        OrderDAO.saveOrder(testOrder);
            //     System.out.println("Тестовый заказ сохранён");
            // Запуск ботов
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            UserBot userBot = new UserBot();
            botsApi.registerBot(userBot);
            botsApi.registerBot(new AdminBot());

            // Запуск проверки таймаутов
            TimeoutChecker.start(userBot);

            System.out.println("Боты успешно запущены!");
        } catch (Exception e) {
            System.err.println("Ошибка при запуске ботов:");
            e.printStackTrace();
        }
    }
}