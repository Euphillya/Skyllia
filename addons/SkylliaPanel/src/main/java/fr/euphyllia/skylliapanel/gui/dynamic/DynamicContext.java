package fr.euphyllia.skylliapanel.gui.dynamic;

import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import org.jetbrains.annotations.Nullable;

public record DynamicContext(
        @Nullable Players players,
        @Nullable WarpIsland warp
) {

    public String resolve(String input) {
        if (input == null || input.isBlank()) return input;
        String out = input;

        if (players != null) {
            out = out.replace("%dynamic_player_name%", players.getLastKnowName())
                    .replace("%dynamic_player_role%", players.getRoleType().name());
        }

        if (warp != null) {
            out = out.replace("%dynamic_warp_name%", warp.warpName());
        }

        return out;
    }

}
