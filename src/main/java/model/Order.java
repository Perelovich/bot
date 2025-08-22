package model;

public class Order {
    private long chatId;
    private String country;
    private String brandModel;
    private String budget;
    private String phone;

    // Два конструктора:
    public Order(long chatId) {
        this.chatId = chatId;
    }

    public Order(long chatId, String country, String brandModel, String budget, String phone) {
        this.chatId = chatId;
        this.country = country;
        this.brandModel = brandModel;
        this.budget = budget;
        this.phone = phone;
    }

    // Сеттеры
    public void setPhone(String phone) { this.phone = phone; }
    public void setCountry(String country) { this.country = country; }
    public void setBrandModel(String brandModel) { this.brandModel = brandModel; }
    public void setBudget(String budget) { this.budget = budget; }

    // Геттеры
    public long getChatId() { return chatId; }
    public String getCountry() { return country; }
    public String getBrandModel() { return brandModel; }
    public String getBudget() { return budget; }
    public String getPhone() { return phone; }

    @Override
    public String toString() {
        return String.format(
                "Order[chatId=%d, country=%s, model=%s, budget=%s, phone=%s]",
                chatId, country, brandModel, budget, phone
        );
    }
}