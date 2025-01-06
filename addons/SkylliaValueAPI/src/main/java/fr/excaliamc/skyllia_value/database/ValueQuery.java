package fr.excaliamc.skyllia_value.database;

import fr.excaliamc.skyllia_value.models.ItemBrutStock;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ValueQuery {

    public ValueQuery() {
    }

    public CompletableFuture<Boolean> hasIncrement(UUID islandId, String material, byte[] itemStackBytes, int incrementValue) {
        throw new RuntimeException();
    }

    public CompletableFuture<Boolean> hasDecrement(UUID islandId, String material, byte[] itemStackBytes, int decrementValue, Timestamp timestamp) {
        throw new RuntimeException();
    }

    public CompletableFuture<List<ItemBrutStock>> allMaterialOnIsland(UUID islandId) {
        throw new RuntimeException();
    }

    public CompletableFuture<Boolean> updateBlockValuePlot(String material, double value) {
        throw new RuntimeException();
    }

    public CompletableFuture<Map<String, Double>> getValueMaterial() {
        throw new RuntimeException();
    }

    public CompletableFuture<Double> getValueIsland(UUID islandId) {
        throw new RuntimeException();
    }
}
