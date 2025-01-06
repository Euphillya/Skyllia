package fr.excaliamc.skyllia_value.models;

import java.sql.Timestamp;

public record ItemBrutStock(String material, byte[] brut, int stock, Timestamp timestamp) {
    public ItemBrutStock(String material, byte[] brut, int stock, Timestamp timestamp) {
        this.material = material;
        this.brut = brut;
        this.stock = stock;
        this.timestamp = timestamp;
    }

    public String material() {
        return this.material;
    }

    public byte[] brut() {
        return this.brut;
    }

    public int stock() {
        return this.stock;
    }

    public Timestamp timestamp() {
        return this.timestamp;
    }
}
