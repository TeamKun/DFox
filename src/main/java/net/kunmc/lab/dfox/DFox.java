package net.kunmc.lab.dfox;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Fox;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DFox extends JavaPlugin implements Listener , CommandExecutor, TabCompleter {
    public static boolean GAME = false;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(this, this);

        new DFoxTask(this).runTaskTimer(this, 2, 10);
    }

    @Override
    public void onDisable() {
        DFoxTask.disable();
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (cmd.getName().equals("df")) {
            if(args.length == 1){
                if(args[0].equals("on")){
                    if(GAME){
                        sender.sendMessage(ChatColor.RED + "プラグインはすでにonです");
                    }else{
                        GAME = true;
                        sender.sendMessage(ChatColor.GREEN + "プラグインをonにしました");
                    }
                }else if(args[0].equals("off")){
                    if(GAME){
                        GAME = false;
                        sender.sendMessage(ChatColor.GREEN + "プラグインをoffにしました");
                    }else{
                        sender.sendMessage(ChatColor.RED + "プラグインはすでにoffです");
                    }
                }else if(args[0].equals("help")){
                    sender.sendMessage(ChatColor.GOLD + "-------------コマンド一覧-------------");
                    sender.sendMessage("/df on   : プラグインのon");
                    sender.sendMessage("/df off  : プラグインのoff");
                    sender.sendMessage("/df help : プラグインのコマンド一覧");
                    sender.sendMessage(ChatColor.GOLD + "-------------コマンド一覧-------------");
                }else{
                    sender.sendMessage(ChatColor.RED + "引数が異なります./df help");
                }
            }else{
                sender.sendMessage(ChatColor.RED + "引数が異なります./df help");
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equals("df")) {
            if (args.length == 1) {
                return (sender.hasPermission("df")
                        ? Stream.of("on","off")
                        : Stream.of("on","off","help"))
                        .filter(e -> e.startsWith(args[0])).collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e){
        if(e.getEntity() instanceof Fox){
            LivingEntity fox = e.getEntity();
            if(fox.getKiller() == null) return;
            if(this.getConfig().getStringList("users").contains(fox.getKiller().getName())){
                e.getDrops().clear();
                e.setDroppedExp(0);
            }
        }
    }
}
