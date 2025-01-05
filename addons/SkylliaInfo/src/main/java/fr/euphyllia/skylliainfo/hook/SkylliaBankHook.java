package fr.euphyllia.skylliainfo.hook;

import fr.euphyllia.skylliabank.SkylliaBank;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SkylliaBankHook {

    public static void sendMessage(MiniMessage miniMessage, Player player, UUID islandId) {
        SkylliaBank.getBankManager().getBankAccount(islandId).thenAcceptAsync(bankAccount -> {
            player.sendMessage(miniMessage.deserialize(
                    "<yellow>Bank: </yellow><white>" + bankAccount.balance() + "</white>"));
        });
    }
}
