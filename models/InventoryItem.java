package models;

import java.time.LocalDate;

public class InventoryItem {
    private Long itemId;
    private String name;
    private int quantityOnHand;
    private String supplier;
    private LocalDate lastRestocked;

    // Constructors
    public InventoryItem() {}

    public InventoryItem(String name, int quantityOnHand, String supplier) {
        this.name = name;
        this.quantityOnHand = quantityOnHand;
        this.supplier = supplier;
    }

    // Getters and Setters
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuantityOnHand() { return quantityOnHand; }
    public void setQuantityOnHand(int quantityOnHand) { this.quantityOnHand = quantityOnHand; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public LocalDate getLastRestocked() { return lastRestocked; }
    public void setLastRestocked(LocalDate lastRestocked) { this.lastRestocked = lastRestocked; }
}
