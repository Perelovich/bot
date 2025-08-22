package util;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class KeyboardFactory {

    // Основная клавиатура с выбором страны
    public static ReplyKeyboardMarkup createCountryKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);
        keyboardMarkup.setSelective(true);

        List<KeyboardRow> keyboard = new ArrayList<>();

        // Первый ряд кнопок
        KeyboardRow row1 = new KeyboardRow();
        row1.add("🇩🇪 Германия");  // Флаг Германии
        row1.add("🇰🇷 Корея");    // Флаг Южной Кореи
        keyboard.add(row1);

        // Второй ряд кнопок
        KeyboardRow row2 = new KeyboardRow();
        row2.add("🇨🇳 Китай");     // Флаг Китая
        row2.add("🇯🇵 Япония");    // Флаг Японии
        keyboard.add(row2);

        // Третий ряд кнопок
        KeyboardRow row3 = new KeyboardRow();
        row3.add("📞 Связаться с администратором");
        keyboard.add(row3);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    // Клавиатура без Японии (для предупреждения о правом руле)
    public static ReplyKeyboardMarkup createCountryKeyboardWithoutJapan() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);
        keyboardMarkup.setSelective(true);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("🇩🇪 Германия");
        row1.add("🇰🇷 Корея");
        keyboard.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add("🇨🇳 Китай");  // Альтернативный вариант флага Китая
        keyboard.add(row2);

        KeyboardRow row3 = new KeyboardRow();
        row3.add("📞 Связаться с администратором");
        keyboard.add(row3);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    // Остальные методы остаются без изменений
    public static ReplyKeyboardMarkup createContactKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        KeyboardRow row = new KeyboardRow();
        row.add("📱 Отправить мой контакт");
        row.add("✏️ Ввести вручную");

        keyboardMarkup.setKeyboard(List.of(row));
        return keyboardMarkup;
    }

    public static ReplyKeyboardMarkup createMainMenuKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("🚗 Оформить заказ");
        keyboard.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add("📞 Связаться с администратором");
        keyboard.add(row2);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    public static ReplyKeyboardMarkup createConfirmationKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        KeyboardRow row = new KeyboardRow();
        row.add("✅ Подтвердить");
        row.add("❌ Отменить");

        keyboardMarkup.setKeyboard(List.of(row));
        return keyboardMarkup;
    }
}