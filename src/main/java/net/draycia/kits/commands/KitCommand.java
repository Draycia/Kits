package net.draycia.kits.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.draycia.cooldowns.CooldownManager;
import net.draycia.kits.Kit;
import net.draycia.kits.Kits;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import net.kyori.text.adapter.bukkit.TextAdapter;
import net.kyori.text.event.ClickEvent;
import net.kyori.text.event.HoverEvent;
import net.kyori.text.format.TextColor;
import org.bukkit.Bukkit;
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
                TextAdapter.sendComponent(player, noobaniaKits.getMessage("cooldown", "kitname", kit.getName())); // TODO: say how long it's on cd for
                return;
            }

            kit.giveToPlayer(player, false);

            if (kit.getCooldown() > 0) {
                cooldownManager.setUserCooldown(player.getUniqueId(), "kit-" + kit.getName(),
                        TimeUnit.SECONDS, kit.getCooldown(), (uuid, id) -> {
                            TextAdapter.sendComponent(player, noobaniaKits.getMessage("cooldown-end", "kitname", kit.getName()));
                 });
            }

            TextAdapter.sendComponent(player, noobaniaKits.getMessage("used-kit", "kitname", kit.getName()));

            return;
        }

        TextComponent.Builder builder = TextComponent.builder()
                .append(noobaniaKits.getMessage("kits"));

        Iterator<Kit> kitIterator = noobaniaKits.getKits().iterator();
        Kit entry;

        while (kitIterator.hasNext()) {
            entry = kitIterator.next();

            if (!player.hasPermission("kits.use." + entry.getName())) {
                continue;
            }

            builder = builder.append(noobaniaKits.getMessage("click-to-use", "kitname", entry.getName()));

            if (kitIterator.hasNext()) {
                builder = builder.append(noobaniaKits.getMessage("separator"));
            }
        }

        TextAdapter.sendComponent(player, builder.build());
    }

    @CommandPermission("kits.reload")
    @Subcommand("reload")
    public void reload(CommandSender sender) {
        noobaniaKits.reloadConfig();
        noobaniaKits.reloadLanguage();
        noobaniaKits.loadKits();
        TextAdapter.sendComponent(sender, noobaniaKits.getMessage("reloaded"));
    }

}
