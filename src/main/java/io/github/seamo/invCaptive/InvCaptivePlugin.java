package io.github.seamo.invCaptive;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class InvCaptivePlugin implements Listener {
    private final JavaPlugin plugin;

    private final File inventoryFile;

    private List<Material> randomBlocks;

    public InvCaptivePlugin(JavaPlugin plugin) {
        this.plugin = plugin;
        this.inventoryFile = new File(plugin.getDataFolder(), "inventory.yml");
        loadRandomBlocks();
    }

    private void loadRandomBlocks() {
        if (this.inventoryFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(this.inventoryFile);
            this.randomBlocks = new ArrayList<>();
            for (String blockName : config.getStringList("randomBlocks")) {
                Material material = Material.getMaterial(blockName);
                if (material != null && material.isBlock())
                    this.randomBlocks.add(material);
            }
        } else {
            this.randomBlocks = getRandomBlocks(39);
            saveRandomBlocks();
        }
    }

    private void saveRandomBlocks() {
        YamlConfiguration config = new YamlConfiguration();
        List<String> blockNames = new ArrayList<>();
        for (Material material : this.randomBlocks)
            blockNames.add(material.name());
        config.set("randomBlocks", blockNames);
        try {
            config.save(this.inventoryFile);
        } catch (IOException e) {
            this.plugin.getLogger().severe("Could not save inventory.yml: " + e.getMessage());
        }
    }

    private List<Material> getRandomBlocks(int count) {
        List<Material> allBlocks = new ArrayList<>();
        for (Material material : Material.values()) {
            if (material.isBlock())
                allBlocks.add(material);
        }
        Collections.shuffle(allBlocks);
        return allBlocks.subList(0, Math.min(count, allBlocks.size()));
    }

    @EventHandler
    public void onPlayerBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (this.randomBlocks.contains(block.getType())) {
            player.sendMessage(ChatColor.GREEN + player.getName() +
                    ChatColor.GOLD + "이(가) " +
                    ChatColor.RED +
                    block.getType().name().toLowerCase() +
                    ChatColor.GOLD + "을(를) 부셨습니다!");

            for (int i = 0; i < player.getInventory().getSize(); i++) {
                if (player.getInventory().getItem(i) != null &&
                        player.getInventory().getItem(i).getType() == Material.BARRIER) {
                    // Collect all slots containing barriers
                    List<Integer> barrierSlots = new ArrayList<>();
                    for (int j = 0; j < player.getInventory().getSize(); j++) {
                        if (player.getInventory().getItem(j) != null &&
                                player.getInventory().getItem(j).getType() == Material.BARRIER) {
                            barrierSlots.add(j);
                        }
                    }

                    // Randomly select one slot
                    if (!barrierSlots.isEmpty()) {
                        int randomIndex = new Random().nextInt(barrierSlots.size());
                        int selectedSlot = barrierSlots.get(randomIndex);

                        // Replace the barrier in the selected slot
                        ItemStack goldenApple = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);
                        ItemMeta meta = goldenApple.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName(ChatColor.GOLD + "해제된 인벤토리"); // Set the name to "ㅇ"
                            goldenApple.setItemMeta(meta);
                        }
                        player.getInventory().setItem(selectedSlot, goldenApple);
                    }
                    break; // Stop after replacing one barrier
                }
            }

            this.randomBlocks.remove(block.getType());
            saveRandomBlocks();
            player.getWorld().spawn(player.getLocation(), Firework.class);
        }
    }
}