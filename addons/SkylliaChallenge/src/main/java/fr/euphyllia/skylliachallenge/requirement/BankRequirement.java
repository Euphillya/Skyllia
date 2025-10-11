package fr.euphyllia.skylliachallenge.requirement;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliabank.SkylliaBank;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicBoolean;

public record BankRequirement(double amount) implements ChallengeRequirement {

    @Override
    public boolean isMet(Player player, Island island) {
        if (SkylliaBank.getInstance() == null) return false;
        AtomicBoolean ok = new AtomicBoolean(false);
        SkylliaBank.getBankManager().getBankAccount(island.getId()).thenAccept(acc -> {
            if (acc != null && acc.balance() >= amount) ok.set(true);
        }).join();
        return ok.get();
    }

    @Override
    public String getDisplay() {
        return "Avoir " + amount + " dans la banque d’île";
    }
}