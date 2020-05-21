package net.draycia.kits.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.draycia.kits.Kit;
import org.black_ixx.bossshop.api.BossShopAPI;
import org.bukkit.entity.Player;

@CommandAlias("kitp|kitpreview")
public class KitPreviewCommand extends BaseCommand {

    private BossShopAPI bossShopAPI;

    public KitPreviewCommand(BossShopAPI bossShopAPI) {
        this.bossShopAPI = bossShopAPI;
    }

    @Default
    @CommandCompletion("@kits")
    @Syntax("[kit name]")
    @Description("Shows kit contents")
    public void baseCommand(Player player, @Optional Kit kit) {
        if (kit != null) {
            bossShopAPI.openShop(player, kit.getShop());
        }
    }

}
