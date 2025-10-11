package fr.euphyllia.skylliachallenge.requirement;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliabank.BankManager;
import fr.euphyllia.skylliabank.SkylliaBank;
import fr.euphyllia.skylliachallenge.api.requirement.ChallengeRequirement;
import fr.euphyllia.skylliachallenge.storage.ProgressStoragePartial;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public record BankRequirement(int requirementId, NamespacedKey challengeKey,
                              double amount) implements ChallengeRequirement {

    @Override
    public boolean isMet(Player player, Island island) {
        if (SkylliaBank.getInstance() == null) return false;
        long target = (long) amount;
        long partial = ProgressStoragePartial.getPartial(island.getId(), challengeKey, requirementId);
        return partial >= target;
    }

    @Override
    public boolean consume(Player player, Island island) {
        if (SkylliaBank.getInstance() == null) return false;
        long target = (long) amount;
        long already = ProgressStoragePartial.getPartial(island.getId(), challengeKey, requirementId);
        if (already >= target) return true;

        long needed = target - already;

        BankManager bank = SkylliaBank.getBankManager();
        double current = bank.getBankAccount(island.getId()).join().balance();
        long available = (long) Math.floor(current);

        long toMove = Math.min(needed, available);
        if (toMove <= 0) return false;

        boolean ok = bank.withdraw(island.getId(), toMove).join();
        if (!ok) return false;

        ProgressStoragePartial.addPartial(island.getId(), challengeKey, requirementId, toMove);
        return true;

    }

    @Override
    public String getDisplay() {
        return "Avoir " + amount + " dans la banque d’île";
    }
}