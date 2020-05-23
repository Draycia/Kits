package net.draycia.kits;

import co.aikar.commands.PaperCommandManager;
import me.minidigger.minimessage.text.MiniMessageParser;
import net.draycia.kits.commands.KitCommand;
import net.draycia.kits.commands.KitPreviewCommand;
import net.draycia.kits.listeners.PlayerJoinListener;
import net.kyori.text.Component;
import org.black_ixx.bossshop.api.BossShopAPI;
import org.black_ixx.bossshop.api.BossShopAddon;
import org.black_ixx.bossshop.core.BSBuy;
import org.black_ixx.bossshop.core.BSShop;
import org.black_ixx.bossshop.core.prices.BSPriceType;
import org.black_ixx.bossshop.core.rewards.BSRewardType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public final class Kits extends BossShopAddon {

    private File kitFolder = new File(getDataFolder(), "kits");
    private HashMap<String, Kit> kits = new HashMap<>();

    private ArrayList<BSShop> shops = new ArrayList<>();
    private BSShop mainShop = null;

    private YamlConfiguration language;

    public BossShopAPI getBossShopAPI() {
        return getBossShop().getAPI();
    }

    @Override
    public String getAddonName() {
        return "kits";
    }

    @Override
    public String getRequiredBossShopVersion() {
        return "2.0.8";
    }

    @Override
    public void enableAddon() {
        saveDefaultConfig();
        saveResource("language.yml", false);

        language = new YamlConfiguration();

        try {
            language.load(new File(getDataFolder(), "language.yml"));
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        setupListeners();
        setupCommands();
    }

    @Override
    public void bossShopFinishedLoading() {
        loadKits();
    }

    @Override
    public void disableAddon() { }

    @Override
    public void bossShopReloaded(CommandSender commandSender) { }

    private void setupListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
    }

    private void setupCommands() {
        PaperCommandManager manager = new PaperCommandManager(this);

        manager.getCommandContexts().registerContext(Kit.class, c -> getKit(c.popFirstArg()));

        // TODO: make async
        manager.getCommandCompletions().registerCompletion("kits", c -> {
            ArrayList<String> kitNames = new ArrayList<>();

            for (Kit kit : kits.values()) {
                if (c.getSender().hasPermission("kits.use." + kit.getName())) {
                    kitNames.add(kit.getName());
                }
            }

            return kitNames;
        });

        manager.registerCommand(new KitCommand(this));
        manager.registerCommand(new KitPreviewCommand(this));
    }

    public void loadKits() {
        if (mainShop != null) {
            getBossShopAPI().getShopHandler().unloadShop(mainShop);
        }

        mainShop = new BSShop(getBossShopAPI().createNextShopId()) {
            @Override
            public void reloadShop() { }
        };

        mainShop.setShopName("kit-previews");
        mainShop.setDisplayName("Kit Previews");
        mainShop.setSignText("[Kit Previews]");
        mainShop.setNeedPermToCreateSign(true);

        getBossShopAPI().getShopHandler().addShop(mainShop);

        shops.forEach(getBossShopAPI().getShopHandler()::unloadShop);
        shops.clear();

        kits.clear();

        if (!kitFolder.exists()) {
            kitFolder.mkdirs();
            saveResource("kits/example.yml", false);
        }

        int shopIndex = 0;

        for (File kitFile : kitFolder.listFiles()) {
            YamlConfiguration kitConfig = new YamlConfiguration();
            try {
                kitConfig.load(kitFile);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
                continue;
            }

            for (String kitName : kitConfig.getKeys(false)) {
                getLogger().info("Loading kit " + kitName);

                ConfigurationSection kitSection = kitConfig.getConfigurationSection(kitName);
                ArrayList<KitItem> kitItems = new ArrayList<>();

                long cooldown = 0;

                BSShop shop = new BSShop(getBossShopAPI().createNextShopId()) {
                    @Override
                    public void reloadShop() {

                    }
                };

                shop.setShopName(kitName);
                shop.setDisplayName("Kit " + kitName);
                shop.setSignText("[" + kitName + "]");
                shop.setNeedPermToCreateSign(true);

                int itemIndex = 0;

                for (String itemKey : kitSection.getKeys(false)) {
                    if (itemKey.equalsIgnoreCase("cooldown")) {
                        cooldown = kitSection.getLong(itemKey);
                        continue;
                    }

                    if (itemKey.equalsIgnoreCase("shopitem")) {
                        continue;
                    }

                    ConfigurationSection itemSection = kitSection.getConfigurationSection(itemKey);

                    int slotPreference = itemSection.getInt("slot");
                    ItemStack itemStack = itemSection.getItemStack("item");

                    kitItems.add(new KitItem(slotPreference, itemStack));

                    BSBuy bsBuy = getBossShopAPI().createBSBuy(BSRewardType.Nothing, BSPriceType.Nothing, null,
                            null, "", itemIndex++, "");

                    getBossShopAPI().addItemToShop(itemStack, bsBuy, shop);
                }

                getBossShopAPI().getShopHandler().addShop(shop);

                getLogger().info("Loaded " + kitItems.size() + " items for kit " + kitName);

                Kit kit = new Kit(kitItems, kitName, cooldown);

                kit.setShop(shop);

                kits.put(kitName, kit);

                BSBuy shopBuy = getBossShopAPI().createBSBuy(BSRewardType.Shop, BSPriceType.Nothing, shop.getShopName(), null,
                        "", shopIndex++, "kits.use." + kitName);

                ItemStack shopItem = kitSection.getItemStack("shopitem");

                getBossShopAPI().addItemToShop(shopItem, shopBuy, mainShop);

            }
        }

    }

    public Kit getKit(String name) {
        return kits.get(name);
    }

    public Collection<Kit> getKits() {
        return kits.values();
    }

    public BSShop getMainShop() {
        return mainShop;
    }

    public String getLangEntry(String key) {
        return ChatColor.translateAlternateColorCodes('&', language.getString(key, ""));
    }

    public Component getMessage(String key, String... replace) {
        return MiniMessageParser.parseFormat(language.getString(key, ""), replace);
    }
}
