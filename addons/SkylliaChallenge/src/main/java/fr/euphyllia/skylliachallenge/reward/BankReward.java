package fr.euphyllia.skylliachallenge.reward;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliabank.SkylliaBank;
import fr.euphyllia.skylliachallenge.SkylliaChallenge;
import fr.euphyllia.skylliachallenge.api.reward.ChallengeReward;
import fr.euphyllia.skylliachallenge.challenge.Challenge;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;

public record BankReward(Number amount) implements ChallengeReward {

    private static final Logger log = LoggerFactory.getLogger(BankReward.class);

    @Override
    public void apply(Player player, Island island, Challenge challenge) {
        Bukkit.getAsyncScheduler().runNow(SkylliaChallenge.getInstance(), task -> {
            boolean result = SkylliaBank.getBankManager().deposit(island.getId(), amount.doubleValue());
            if (result) {
                ConfigLoader.language.sendMessage(player, "addons.challenge.reward.bank.success", Map.of(
                        "%amount%", String.valueOf(amount.doubleValue()),
                        "%challenge_name", challenge.getName()
                ));
                return;
            } else {
                ConfigLoader.language.sendMessage(player, "addons.challenge.reward.bank.failed", Map.of(
                        "%amount%", String.valueOf(amount.doubleValue()),
                        "%challenge_name", challenge.getName()
                ));
                log.error("Failed to deposit money to island bank for challenge reward");
            }
        });
    }

    /**
     * Returns a human-readable description of this reward.
     * <p>
     * Used in GUIs and lores to inform the player about what they will gain.
     * Examples:
     * <ul>
     *     <li>"x3 Diamants"</li>
     *     <li>"5000$ en banque"</li>
     *     <li>"Commande: /fly pendant 1h"</li>
     * </ul>
     * </p>
     *
     * @param locale
     * @return a short displayable string describing this reward
     */
    @Override
    public Component getDisplay(Locale locale) {
        return null;
    }
}
