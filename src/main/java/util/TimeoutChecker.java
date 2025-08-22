package util;

import bot.UserBot;
import java.util.concurrent.*;

public class TimeoutChecker {
    private static final long TIMEOUT_MINUTES = 3000;

    public static void start(UserBot userBot) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            userBot.getLastActivityMap().forEach((chatId, lastActiveTime) -> {
                if ((currentTime - lastActiveTime) > TimeUnit.MINUTES.toMillis(TIMEOUT_MINUTES)) {
                    // Теперь можем вызывать sendMessage, так как он public
                    userBot.sendMessage(chatId,
                            "❌ Вы неактивны. Продолжить? /start",
                            null);
                }
            });
        }, 1, 1, TimeUnit.MINUTES);
    }
}