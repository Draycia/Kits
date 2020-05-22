package net.draycia.kits.listeners;

import net.draycia.kits.Kit;
import net.draycia.kits.Kits;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private Kits noobaniaKits;

    public PlayerJoinListener(Kits noobaniaKits) {
        this.noobaniaKits = noobaniaKits;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().hasPlayedBefore()) {
            return;
        }

        String kitName = noobaniaKits.getConfig().getString("new-join-kit");

        if (kitName == null || kitName.equalsIgnoreCase("none")) {
            return;
        }

        Kit kit = noobaniaKits.getKit(kitName);

        if (kit == null) {
            noobaniaKits.getLogger().warning("New join kit specified but not found! Config error?");
            return;
        }

        kit.giveToPlayer(event.getPlayer(), false);
    }

}
