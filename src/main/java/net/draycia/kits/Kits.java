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
    private BossShopAPI bossShopAPI = null;

    @Override
    public void onEnable() {
        bossShopAPI = ((BossShop) Bukkit.getPluginManager().getPlugin("BossShopPro")).getAPI();

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
        manager.registerCommand(new KitPreviewCommand(bossShopAPI));
    }

    public void loadKits() {
        kits.clear();

        if (!kitFolder.exists()) {
            kitFolder.mkdirs();
            saveResource("kits/example.yml", false);
        }

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

                int index = 0;

                for (String itemKey : kitSection.getKeys(false)) {
                    if (itemKey.equalsIgnoreCase("cooldown")) {
                        cooldown = kitSection.getLong(itemKey);
                        continue;
                    }

                    ConfigurationSection itemSection = kitSection.getConfigurationSection(itemKey);

                    int slotPreference = itemSection.getInt("slot");
                    ItemStack itemStack = itemSection.getItemStack("item");

                    kitItems.add(new KitItem(slotPreference, itemStack));

                    BSBuy bsBuy = bossShopAPI.createBSBuy(BSRewardType.Nothing, BSPriceType.Nothing, null,
                            null, "", index++, "");

                    bossShopAPI.addItemToShop(itemStack, bsBuy, shop);
                }

                getLogger().info("Loaded " + kitItems.size() + " items for kit " + kitName);

                Kit kit = new Kit(kitItems, kitName, cooldown);

                kit.setShop(shop);

                kits.put(kitName, kit);
            }
        }

    }

    public Kit getKit(String name) {
        return kits.get(name);
    }

    public Collection<Kit> getKits() {
        return kits.values();
    }
}
