package com.shun;

public class Order {
    private int id;
    private String instrument;
    private String side;
    private float price;
    private int quantity;

    // Constructor
    public Order() {
    }

    public Order(int id, String instrument, String side, float price, int quantity) {
        this.id = id;
        this.instrument = instrument;
        this.side = side;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "Order { " +
                "id=" + id +
                ", instrument='" + instrument + '\'' +
                ", price=" + price +
                ", side='" + side + '\'' +
                ", quantity=" + quantity +
                " }";
    }
}