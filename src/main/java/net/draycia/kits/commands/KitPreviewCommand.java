package net.draycia.kits.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.draycia.kits.Kit;
import net.draycia.kits.Kits;
import org.black_ixx.bossshop.api.BossShopAPI;
import org.bukkit.entity.Player;

@CommandAlias("kitp|kitpreview")
public class KitPreviewCommand extends BaseCommand {

    private Kits kits;

    public KitPreviewCommand(Kits kits) {
        this.kits = kits;
    }

    @Default
    @CommandCompletion("@kits")
    @Syntax("[kit name]")
    @Description("Shows kit contents")
    public void baseCommand(Player player, @Optional Kit kit) {
        if (kit != null) {
            kits.getBossShopAPI().openShop(player, kit.getShop());
            return;
        }

        kits.getBossShopAPI().openShop(player, kits.getMainShop());
    }

}
