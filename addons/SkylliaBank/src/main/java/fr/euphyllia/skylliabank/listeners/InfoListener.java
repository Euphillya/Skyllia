package fr.euphyllia.skylliabank.listeners;

import fr.euphyllia.skyllia.api.event.IslandInfoEvent;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliabank.SkylliaBank;
import fr.euphyllia.skylliabank.api.BankAccount;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class InfoListener implements Listener {

    @EventHandler
    public void onIslandInfoEvent(final IslandInfoEvent event) {
        CompletableFuture<BankAccount> future = SkylliaBank.getBankManager().getOrLoadBankAccount(event.getIsland().getId());
        if (future == null) {
            return;
        }
        BankAccount account = future.getNow(new BankAccount(event.getIsland().getId(), -1.0));

        if (account.balance() < 0) {
            return;
        }

        Component component = Component.text("")
                .append(ConfigLoader.language.translate(event.getViewer(), "addons.bank.display.title"))
                .append(Component.newline())
                .append(ConfigLoader.language.translate(event.getViewer(), "addons.bank.display.balance", Map.of("%amount%", String.valueOf(account.balance()))));

        event.addLine(component);
    }
}
