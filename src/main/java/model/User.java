package model;

public class User {
    private long chatId;
    private String phone;

    public User(long chatId, String phone) {
        this.chatId = chatId;
        this.phone = phone;
    }

    // Геттеры
    public long getChatId() { return chatId; }
    public String getPhone() { return phone; }
}