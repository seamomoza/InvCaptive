package io.github.seamo.invCaptive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class InvCaptive extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new BarrierKeep(), this);
        getCommand("captive").setExecutor(new CaptiveCommandExecutor());
        Bukkit.getPluginManager().registerEvents(new InvCaptivePlugin(this), this); // 'this' 전달
    }


    public class CaptiveCommandExecutor implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                // 첫 번째 칸을 제외한 인벤토리 슬롯에 베리어 아이템 배치
                for (int i = 1; i < player.getInventory().getSize(); i++) { // 첫 번째 칸 제외
                    ItemStack barrier = new ItemStack(Material.BARRIER);
                    ItemMeta meta = barrier.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName("§c막힌 인벤토리");
                        barrier.setItemMeta(meta);
                    }
                    player.getInventory().setItem(i, barrier);
                }

                return true;
            }
            return false;
        }
    }
}
