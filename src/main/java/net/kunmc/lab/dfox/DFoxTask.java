package net.kunmc.lab.dfox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import org.bukkit.entity.Fox;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class DFoxTask extends BukkitRunnable{
    DFox plugin;
    boolean cnf = false;
    Map<String, Integer> killed;
    public static Map<String, BossBar> bar;
    Map<String, Integer> stats;


    public DFoxTask(DFox dFox) {
        this.plugin = dFox;
        killed = new HashMap<>();
        bar = new HashMap<>();
        stats = new HashMap<>();
    }

    static void disable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            for (Map.Entry<String, BossBar> entry : bar.entrySet()) {
                entry.getValue().removePlayer(p);
            }
        }
    }

    @Override
    public void run() {
        if(DFox.GAME) {
            for (String s : plugin.getConfig().getStringList("users")) {
                if (Bukkit.getPlayer(s) != null) {
                    if (Bukkit.getPlayer(s).isOnline()) {
                        double range = plugin.getConfig().getDouble("range");
                        for (LivingEntity le : Bukkit.getPlayer(s).getLocation().getNearbyLivingEntities(range)) {
                            if (le instanceof Fox) {
                                if (!le.isDead()) {
                                    Player p = Bukkit.getPlayer(s);
                                    final Location loc = le.getLocation();
                                    List<Location> locations = calc(loc, p.getEyeLocation().clone().add(0, -1, 0), 0.2);
                                    new BukkitRunnable() {
                                        Location loc2 = loc.clone();

                                        @Override
                                        public void run() {
                                            List<Location> locations = calc(loc2, p.getEyeLocation().clone().add(0, -1, 0), 0.2);
                                            Location l = locations.get(0);
                                            l.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, l, 1);
                                            if (locations.size() >= 2) {
                                                l = locations.get(1);
                                                l.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, l, 1);
                                            }
                                            loc2 = l;
                                            if (locations.size() < 1) {
                                                this.cancel();
                                            }
                                            if (l.getBlockX() == p.getLocation().getBlockX() && l.getBlockZ() == p.getLocation().getBlockZ() && l.getBlockY() == p.getLocation().getBlockY()) {
                                                this.cancel();
                                            }
                                        }
                                    }.runTaskTimer(this.plugin, 1, 1);

                                    int kill = 0;
                                    if (killed.containsKey(p.getName())) {
                                        kill = killed.get(p.getName());
                                    }
                                    int i = (int) Math.floor(kill / 100);
                                    p.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 20 * 300, i, true));
                                    p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 300, i, true));
                                    if(!cnf){
                                        plugin.saveDefaultConfig();
                                        killed.put(s, plugin.getConfig().getInt("killed"));
                                        cnf = true;
                                    }
                                    if (killed.containsKey(s)) {
                                        killed.replace(s, killed.get(s) + 1);
                                    } else {
                                        killed.put(s, 1);
                                    }

                                    plugin.getConfig().set("killed",killed.get(s));
                                    plugin.saveConfig();

                                    le.damage(1000, p);
                                    le.setKiller(p);
                                }
                            }
                        }
                    }
                }
            }

            for (Map.Entry<String, Integer> entry : killed.entrySet()) {
                String name = entry.getKey();
                int kill = entry.getValue();

                BossBar boss;
                int i = (int) Math.floor(kill / 3) + 1;
                if (bar.containsKey(name)) {
                    boss = bar.get(name);
                } else {
                    boss = Bukkit.createBossBar(name + "がキツネを吸収した数: " + kill + " Lv: " + i, BarColor.WHITE, BarStyle.SEGMENTED_10);
                    bar.put(name, boss);
                }

                BarColor color = BarColor.WHITE;
                if (kill <= 3) {
                    color = BarColor.WHITE;
                } else if (kill <= 6) {
                    color = BarColor.PURPLE;
                } else if (kill <= 9) {
                    color = BarColor.BLUE;
                } else if (kill <= 12) {
                    color = BarColor.GREEN;
                } else if (kill <= 15) {
                    color = BarColor.YELLOW;
                } else if (kill <= 18) {
                    color = BarColor.PINK;
                } else if (kill <= 21) {
                    color = BarColor.RED;
                } else {
                    color = BarColor.RED;
                }

                boss.setTitle(name + "がキツネを吸収した数: " + kill + " Lv: " + i);
                boss.setColor(color);

                double count100 = (double) Math.floor(kill / 3);
                boss.setProgress((kill - (3 * count100)) / 3);

                for (Player p : Bukkit.getOnlinePlayers()) {
                    boss.addPlayer(p);
                }
            }
        }
    }

    public static List<Location> calc(Location loc1, Location loc2, double distanceBetween) throws IllegalArgumentException {
        if (loc1.getWorld() != loc2.getWorld() || distanceBetween <= 0) {
            throw new IllegalArgumentException();
        }

        List<Location> locations = new ArrayList<>();
        loc1 = loc1.clone();
        loc2 = loc2.clone();

        Vector v = adjustExactDistance(loc2.clone().subtract(loc1.clone()).toVector().normalize(), distanceBetween);
        Location firstLocation1 = loc1.clone();
        while (firstLocation1.distance(loc2) >= firstLocation1.distance(loc1)) {
            locations.add(loc1.clone());
            loc1.add(v);
        }

        return locations;
    }

    private static Vector adjustExactDistance(Vector v, double length) {
        return v.multiply(length / v.length());
    }

    public ArrayList<Location> getCircle(Location center, double radius, int amount) {
        World world = center.getWorld();
        double increment = (2 * Math.PI) / amount;
        ArrayList<Location> locations = new ArrayList<Location>();
        for (int i = 0; i < amount; i++) {
            double angle = i * increment;
            double x = center.getX() + (radius * Math.cos(angle));
            double z = center.getZ() + (radius * Math.sin(angle));
            locations.add(new Location(world, x, center.getY(), z));
        }
        return locations;
    }
}
