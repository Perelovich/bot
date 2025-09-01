package model;

import java.sql.Timestamp;

public class Order {
    private long chatId;
    private String country;
    private String brandModel;
    private String budget;
    private String phone;
    private String userName;
    private Timestamp orderTime;

    // Конструктор для создания временного заказа в боте
    public Order(long chatId) {
        this.chatId = chatId;
    }

    // Полный конструктор для создания объекта из данных БД
    public Order(long chatId, String country, String brandModel, String budget, String phone, String userName, Timestamp orderTime) {
        this.chatId = chatId;
        this.country = country;
        this.brandModel = brandModel;
        this.budget = budget;
        this.phone = phone;
        this.userName = userName;
        this.orderTime = orderTime;
    }

    // Getters
    public long getChatId() { return chatId; }
    public String getCountry() { return country; }
    public String getBrandModel() { return brandModel; }
    public String getBudget() { return budget; }
    public String getPhone() { return phone; }
    public String getUserName() { return userName; }
    public Timestamp getOrderTime() { return orderTime; }

    // Setters
    public void setCountry(String country) { this.country = country; }
    public void setBrandModel(String brandModel) { this.brandModel = brandModel; }
    public void setBudget(String budget) { this.budget = budget; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setOrderTime(Timestamp orderTime) { this.orderTime = orderTime; }

    @Override
    public String toString() {
        return String.format(
                "Order[chatId=%d, user=%s, country=%s, model=%s, budget=%s, phone=%s, time=%s]",
                chatId, userName, country, brandModel, budget, phone, orderTime
        );
    }
}