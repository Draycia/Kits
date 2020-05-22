package net.draycia.kits.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.draycia.cooldowns.CooldownManager;
import net.draycia.kits.Kit;
import net.draycia.kits.Kits;
import net.kyori.text.TextComponent;
import net.kyori.text.adapter.bukkit.TextAdapter;
import net.kyori.text.event.ClickEvent;
import net.kyori.text.event.HoverEvent;
import net.kyori.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

@CommandAlias("kit")
public class KitCommand extends BaseCommand {

    private Kits noobaniaKits;
    private CooldownManager cooldownManager;

    public KitCommand(Kits noobaniaKits) {
        this.noobaniaKits = noobaniaKits;
        this.cooldownManager = Bukkit.getServicesManager().getRegistration(CooldownManager.class).getProvider();
    }

    @Default
    @CommandCompletion("@kits")
    @Syntax("[kit name]")
    @Description("Lists available kits")
    public void baseCommand(Player player, @Optional Kit kit) {
        if (kit != null) {
            if (cooldownManager.isOnCooldown(player.getUniqueId(), "kit-" + kit.getName())) {
                player.sendMessage(ChatColor.RED + "That kit is on cooldown!"); // TODO: say how long it's on cd for
                return;
            }

            kit.giveToPlayer(player, false);

            if (kit.getCooldown() > 0) {
                cooldownManager.setUserCooldown(player.getUniqueId(), "kit-" + kit.getName(),
                        TimeUnit.SECONDS, kit.getCooldown(), (uuid, id) -> {
                            player.sendMessage(ChatColor.GREEN + "Kit " + kit.getName() + " is no longer on cooldown!");
                        });
            }

            player.sendMessage(ChatColor.GREEN + "Used kit " + kit.getName() + ".");

            return;
        }

        TextComponent.Builder builder = TextComponent.builder()
                .append(TextComponent.of("Kits: ").color(TextColor.GREEN));

        Iterator<Kit> kitIterator = noobaniaKits.getKits().iterator();
        Kit entry;

        while (kitIterator.hasNext()) {
            entry = kitIterator.next();

            if (!player.hasPermission("kits.use." + entry.getName())) {
                continue;
            }

            builder = builder.append(TextComponent.of(entry.getName()).color(TextColor.GOLD)
                    .clickEvent(ClickEvent.runCommand("/kits:kit " + entry.getName()))
                    .hoverEvent(HoverEvent.showText(TextComponent.of("Click to use kit " + entry.getName()))));

            if (kitIterator.hasNext()) {
                builder = builder.append(TextComponent.of(", ").color(TextColor.GRAY));
            }
        }

        TextAdapter.sendComponent(player, builder.build());
    }

    @CommandPermission("kits.reload")
    @Subcommand("reload")
    public void reload(CommandSender sender) {
        noobaniaKits.reloadConfig();
        noobaniaKits.loadKits();
        sender.sendMessage(ChatColor.GREEN + "Reloaded config and kits!");
    }

}
