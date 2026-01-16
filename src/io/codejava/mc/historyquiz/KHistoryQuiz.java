package io.codejava.mc.historyquiz;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import io.papermc.paper.dialog.Dialog;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;


public class MyPlugin extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {

    @Override
    public void onEnable() {
        // Registering a command
        getComponentLogger().info(net.kyori.adventure.text.Component.text("[KHistoryQuiz] Registering command executor..."));
        getCommand("khistoryquiz").setExecutor(new CommandHandler());
        
        // Registering a listener (Event Handler)
        //getServer().getPluginManager().registerEvents(new MyEventListener(), this);
        
        // Logging to console
        getComponentLogger().info(net.kyori.adventure.text.Component.text("[KHistoryQuiz] v1.0-java enabled."));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("오류: 콘솔에서 또는 플레이어가 아닐 경우 이 명령어를 실행할 수 없습니다.");
            return true;
        }

        player.sendMessage("You ran the command!");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            return List.of("set", "reload", "start", "abort");
        }
        return List.of();
    }
    

    public void showMyDialog(Player player) {
    // Create a simple notice dialog
            Dialog dialog = Dialog.create(builder -> builder
                .base(builder.baseBuilder(Component.text("Important Choice!")))
                .body(List.of(Component.text("Do you want to receive a diamond?")))
                // In 1.21, you can add buttons and actions here
            );

            // Show it to the player
            player.showDialog(dialog);
        }
}