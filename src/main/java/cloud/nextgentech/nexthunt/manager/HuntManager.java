package cloud.nextgentech.nexthunt.manager;

import cloud.nextgentech.nexthunt.Config;
import cloud.nextgentech.nexthunt.util.Color;
import cloud.nextgentech.nexthunt.util.Placeholder;
import cloud.nextgentech.nexthunt.util.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;

import static cloud.nextgentech.nexthunt.util.Time.formatTime;

public class HuntManager implements Listener {
    private final JavaPlugin plugin;
    private final LocationManager locationManager;
    private final Map<UUID, Double> damageMap = new HashMap<>();
    private final Config config;

    private boolean isHuntStarted = false;
    private Player target;
    private int countdown;
    private int maxCountdown;
    private BukkitTask countdownTask;
    private BukkitTask huntTask;
    private BukkitTask broadcastTask;
    private BukkitTask effectTask;
    private BossBar bossBar;

    public HuntManager(JavaPlugin plugin, LocationManager locationManager, Config config) {
        this.plugin = plugin;
        this.locationManager = locationManager;
        this.config = config;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void on(EntityDamageByEntityEvent e) {
        if (target == null) return;

        Entity entity = e.getEntity();

        if (entity != this.target) return;

        if (!(e.getDamager() instanceof Player damager)) return;

        double damage = e.getFinalDamage();

        damageMap.put(damager.getUniqueId(), damageMap.getOrDefault(damager.getUniqueId(), 0.0) + damage);
    }

    @EventHandler
    public void on(PlayerDeathEvent e) {
        final Player player = e.getEntity();

        if (player != target) return;

        final Player killer = e.getEntity().getKiller();

        if (killer == null) return;

        showFinalTop(killer);
    }

    public void startCountdown(Player player) {
        if (isHuntStarted) return;
        if (countdown > 0) return;

        countdown = config.getCountdown();
        maxCountdown = countdown;
        target = player;

        bossBar = Bukkit.createBossBar(
                Placeholder.replacePrefix(config.getBossBarCountDown(), config)
                        .replaceAll("%time%", formatTime(countdown)),
                BarColor.GREEN,
                BarStyle.SOLID
        );

        for (Player p : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(p);
        }

        this.countdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                countdown--;

                updateBossBar();

                if (countdown <= 0) {
                    this.cancel();
                    removeBossBar();
                    startHunt();
                    return;
                }
                if (countdown % 60 == 0) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendMessage(Placeholder.replacePrefix(config.getCountdownMessage(), config)
                                .replaceAll("%time%", (countdown / 60) + " минут"));
                    }
                } else if (countdown <= 5) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendMessage(Placeholder.replacePrefix(config.getCountdownMessage(), config)
                                .replaceAll("%time%", countdown + " секунд"));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void startHunt() {
        isHuntStarted = true;
        countdown = 0;
        damageMap.clear();

        Location loc = locationManager.generateRandomLocation(target, config.getWorld());

        target.teleport(loc);

        bossBar = Bukkit.createBossBar(
                Placeholder.replacePrefix(config.getBossBarHunt(), config)
                        .replaceAll("%name%", target.getName())
                        .replaceAll("%x%", String.format("%.0f", loc.getX()))
                        .replaceAll("%y%", String.format("%.0f", loc.getY()))
                        .replaceAll("%z%", String.format("%.0f", loc.getZ())),
                BarColor.RED,
                BarStyle.SOLID
        );

        for (Player p : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(p);
            p.sendMessage(Placeholder.replacePrefix(config.getStart(), config)
                    .replaceAll("%name%", target.getName()));
        }

        int broadcastPeriod = config.getBroadcastPeriod() * 20;
        this.broadcastTask = new BukkitRunnable() {
            @Override
            public void run() {
                showTopDamage();
            }
        }.runTaskTimer(plugin, broadcastPeriod, broadcastPeriod);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.getOnlinePlayers().forEach(player -> SoundUtil.playSound(config.getStartSound(), player));
        }, 60L);

        long coordinatesPeriod = (long) config.getCoordinatedPeriod() * 20;
        this.huntTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (target == null || !target.isOnline() || target.isDead()) {
                    this.cancel();
                    stop();
                    return;
                }
                updateBossBar();
            }
        }.runTaskTimer(plugin, 0L, coordinatesPeriod);

        this.effectTask = new BukkitRunnable() {
            @Override
            public void run() {
                target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 1, true, false, true));
            }
        }.runTaskTimer(plugin, 0L, 50L);
    }

    public void stop() {
        isHuntStarted = false;

        target = null;
        damageMap.clear();
        removeBossBar();

        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        if (broadcastTask != null) {
            broadcastTask.cancel();
            broadcastTask = null;
        }
        if (huntTask != null) {
            huntTask.cancel();
            huntTask = null;
        }
        if (effectTask != null) {
            effectTask.cancel();
            effectTask = null;
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Placeholder.replacePrefix(config.getEnd(), config));
        }
    }

    private void showTopDamage() {
        if (!isHuntStarted) return;

        List<Map.Entry<UUID, Double>> sorted = damageMap.entrySet().stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
                .limit(config.getTopCount())
                .collect(Collectors.toList());

        for (Player p : Bukkit.getOnlinePlayers()) {
            config.getTop().forEach(line -> {
                if (line.contains("%toplist%")) {
                    sendTopList(p, sorted);
                } else if (line.contains("%damage%")) {
                    double playerDamage = damageMap.getOrDefault(p.getUniqueId(), 0.0);
                    p.sendMessage(Color.format(line)
                            .replaceAll("%damage%", String.format("%.1f", playerDamage)));
                } else {
                    p.sendMessage(Color.format(line));
                }
            });
        }
    }

    private void showFinalTop(Player killer) {
        List<Map.Entry<UUID, Double>> sorted = damageMap.entrySet().stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
                .limit(config.getTopCount())
                .toList();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (killer != null) {
                config.getFinalStrings().forEach(line -> {
                    p.sendMessage(Color.format(line).replaceAll("%killer%", killer.getName()));
                });
            }
            config.getTop().forEach(line -> {
                if (line.contains("%toplist%")) {
                    sendTopList(p, sorted);
                } else if (line.contains("%damage%")) {
                    double playerDamage = damageMap.getOrDefault(p.getUniqueId(), 0.0);
                    p.sendMessage(Color.format(line)
                            .replaceAll("%damage%", String.format("%.1f", playerDamage)));
                } else {
                    p.sendMessage(Color.format(line));
                }
            });
        }
    }

    private void sendTopList(Player p, List<Map.Entry<UUID, Double>> sorted) {
        if (sorted.isEmpty()) {
            for (int i = 0; i < config.getTopCount(); i++) {
                String formatted = Color.format(config.getTopFormatted())
                        .replaceAll("%n%", String.valueOf(i + 1))
                        .replaceAll("%name%", "-")
                        .replaceAll("%d%", "-");
                p.sendMessage(formatted);
            }
            return;
        }

        for (int i = 0; i < sorted.size(); i++) {
            UUID uuid = sorted.get(i).getKey();
            double dmg = sorted.get(i).getValue();
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                String formatted = Color.format(config.getTopFormatted())
                        .replaceAll("%n%", String.valueOf(i + 1))
                        .replaceAll("%name%", player.getName())
                        .replaceAll("%d%", String.format("%.1f", dmg));
                p.sendMessage(formatted);
            }
        }

        if (sorted.size() < config.getTopCount()) {
            for (int i = 0; i < config.getTopCount() - sorted.size(); i++) {
                String formatted = Color.format(config.getTopFormatted())
                        .replaceAll("%n%", String.valueOf(i + 1))
                        .replaceAll("%name%", "-")
                        .replaceAll("%d%", "-");
                p.sendMessage(formatted);
            }
        }
    }

    private void updateBossBar() {
        if (bossBar == null) return;

        if (isHuntStarted) {
            Location loc = target.getLocation();

            bossBar.setTitle(Placeholder.replacePrefix(config.getBossBarHunt(), config)
                    .replaceAll("%name%", target.getName())
                    .replaceAll("%x%", String.format("%.0f", loc.getX()))
                    .replaceAll("%y%", String.format("%.0f", loc.getY()))
                    .replaceAll("%z%", String.format("%.0f", loc.getZ())));
        } else {
            String timeFormat = formatTime(countdown);

            Bukkit.getOnlinePlayers().forEach(player -> {
                SoundUtil.playSound(config.getTimerSound(), player);
            });

            bossBar.setTitle(Placeholder.replacePrefix(config.getBossBarCountDown(), config)
                    .replaceAll("%time%", timeFormat));

            double progress = (double) countdown / (double) maxCountdown;
            bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));

            if (progress <= 0.33) {
                bossBar.setColor(BarColor.RED);
            } else if (progress <= 0.66) {
                bossBar.setColor(BarColor.YELLOW);
            } else {
                bossBar.setColor(BarColor.GREEN);
            }
        }
    }

    private void removeBossBar() {
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
    }

    public void cleanup() {
        if (countdownTask != null) {
            countdownTask.cancel();
        }
        if (huntTask != null) {
            huntTask.cancel();
        }
        if (broadcastTask != null) {
            broadcastTask.cancel();
        }

        removeBossBar();
    }

}