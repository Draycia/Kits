package net.draycia.kits;

import co.aikar.commands.PaperCommandManager;
import net.draycia.kits.commands.KitCommand;
import net.draycia.kits.commands.KitPreviewCommand;
import net.draycia.kits.listeners.PlayerJoinListener;
import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.api.BossShopAPI;
import org.black_ixx.bossshop.core.BSBuy;
import org.black_ixx.bossshop.core.BSShop;
import org.black_ixx.bossshop.core.prices.BSPriceType;
import org.black_ixx.bossshop.core.rewards.BSRewardType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public final class Kits extends JavaPlugin {

    private File kitFolder = new File(getDataFolder(), "kits");
    private HashMap<String, Kit> kits = new HashMap<>();

    private ArrayList<BSShop> shops = new ArrayList<>();
    private BossShopAPI bossShopAPI;
    private BSShop mainShop;

    @Override
    public void onEnable() {
        bossShopAPI = ((BossShop) Bukkit.getPluginManager().getPlugin("BossShopPro")).getAPI();

        mainShop = new ShopGUI(bossShopAPI.createNextShopId());
        mainShop.setShopName("kit-previews");
        mainShop.setDisplayName("Kit Previews");
        mainShop.setSignText("[Kit Previews]");
        mainShop.setNeedPermToCreateSign(true);

        saveDefaultConfig();

        loadKits();

        setupListeners();
        setupCommands();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

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
        shops.forEach(bossShopAPI.getShopHandler()::unloadShop);
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

                int id = bossShopAPI.createNextShopId();

                BSShop shop = new ShopGUI(id);
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

                    BSBuy bsBuy = bossShopAPI.createBSBuy(BSRewardType.Nothing, BSPriceType.Nothing, null,
                            null, "", itemIndex++, "");

                    bossShopAPI.addItemToShop(itemStack, bsBuy, shop);
                    bossShopAPI.getShopHandler().addShop(shop);
                }

                getLogger().info("Loaded " + kitItems.size() + " items for kit " + kitName);

                Kit kit = new Kit(kitItems, kitName, cooldown);

                kit.setShop(shop);

                kits.put(kitName, kit);

                BSBuy shopBuy = bossShopAPI.createBSBuy(BSRewardType.Shop, BSPriceType.Nothing, shop.getShopName(), null,
                        "", shopIndex++, "kits.use." + kitName);

                ItemStack shopItem = kitSection.getItemStack("shopitem");

                bossShopAPI.addItemToShop(shopItem, shopBuy, mainShop);
                bossShopAPI.getShopHandler().addShop(mainShop);

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

    public BossShopAPI getBossShopAPI() {
        return bossShopAPI;
    }
}
