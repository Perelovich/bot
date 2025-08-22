package model;

public enum UserState {
    START,                  // Начальное состояние
    CHOOSE_COUNTRY,         // Выбор страны
    ENTER_BRAND_MODEL,      // Ввод марки и модели
    ENTER_BUDGET,           // Ввод бюджета
    ENTER_PHONE,            // Ввод телефона
    ORDER_CONFIRMED,        // Заказ подтвержден
    CONTACT_ADMIN           // Связь с администратором
}