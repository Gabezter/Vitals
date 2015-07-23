package com.gabezter4.Vitals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Chunk;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

public class Vitals extends JavaPlugin
  implements Listener
{
  final Material[] redstoneArray = { Material.REDSTONE_WIRE, Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_ON, Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.LEVER, Material.STONE_BUTTON, Material.RAILS };
  final Material[] recordsArray = { Material.GOLD_RECORD, Material.GREEN_RECORD, Material.RECORD_3, Material.RECORD_4, Material.RECORD_5, Material.RECORD_6, Material.RECORD_7, Material.RECORD_8, Material.RECORD_9, Material.RECORD_10, Material.RECORD_11 };
  final Material[] armorArray = { Material.LEATHER_BOOTS, Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET, Material.LEATHER_LEGGINGS, Material.IRON_BOOTS, Material.IRON_CHESTPLATE, Material.IRON_HELMET, Material.IRON_LEGGINGS, Material.GOLD_BOOTS, Material.GOLD_CHESTPLATE, Material.GOLD_HELMET, Material.GOLD_LEGGINGS, Material.DIAMOND_BOOTS, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_HELMET, Material.DIAMOND_LEGGINGS };
  final Material[] weaponsArray = { Material.WOOD_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.GOLD_SWORD, Material.DIAMOND_SWORD, Material.BOW, Material.ARROW, Material.WOOD_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLD_AXE, Material.DIAMOND_AXE };
  final Material[] toolsArray = { Material.WOOD_AXE, Material.WOOD_HOE, Material.WOOD_PICKAXE, Material.WOOD_SPADE, Material.STONE_AXE, Material.STONE_HOE, Material.STONE_PICKAXE, Material.STONE_SPADE, Material.IRON_AXE, Material.IRON_HOE, Material.IRON_PICKAXE, Material.IRON_SPADE, Material.GOLD_AXE, Material.GOLD_HOE, Material.GOLD_PICKAXE, Material.GOLD_SPADE, Material.DIAMOND_AXE, Material.DIAMOND_HOE, Material.DIAMOND_PICKAXE, Material.DIAMOND_SPADE };

  public static Economy econ = null; public static Permission perms = null;
  static Logger log;
  static Random random;
  YamlConfiguration local;
  HashMap<String, YamlConfiguration> configs = new HashMap();
  HashMap<String, Integer> tasks = new HashMap(); HashMap<String, Integer> taskIntervals = new HashMap();
  HashMap<Entity, Integer> unusedCarts = new HashMap();
  HashMap<String, Integer> teleportTasks = new HashMap();
  HashMap<String, Integer> flyingTasks = new HashMap();
  HashMap<String, Long> chainmailNotify = new HashMap();
  HashMap<String, List<String>> customWarps = new HashMap();
  List<String> announcements = new ArrayList();
  HashMap<String, Integer> regionTimers = new HashMap();
  HashMap<String, String> regionlabelPlayers = new HashMap();
  HashMap<String, List<ItemStack>> deathretentionInventory = new HashMap();
  HashMap<String, List<ItemStack>> deathretentionArmor = new HashMap();
  List<String> regionprotectIgnore;
  HashMap<String, String[]> regionprotectRegions;
  int countdownTask = -1;
  String eventActive = null; Arena arenaActive = null; long arenaTime = new Date().getTime();
  long auctionLast = new Date().getTime(); int auctionBid = 0; Player auctionBidder = null; Player auctionStarter = null; ItemStack auctionItem = null;
  String regionrestoreState = null; String regionrestoreName = null; String regionrestoreMode = null; Player regionrestoreOp = null; Location regionrestoreCorner1 = null; Location regionrestoreCorner2 = null;
  String setup = null; String setupName = null; String setupOption = null; int setupStep = 1; Player setupOp = null; List<Block> blockChoices = new ArrayList();

  private boolean isRecord(Material m) {
    for (int i = 0; i < recordsArray.length; i++) if (recordsArray[i] == m) return true; 
    return false; } 
  private boolean isArmor(Material m) { if (isChainmail(m)) return true; for (int i = 0; i < armorArray.length; i++) if (armorArray[i] == m) return true; 
    return false; } 
  private boolean isWeapon(Material m) { for (int i = 0; i < weaponsArray.length; i++) if (weaponsArray[i] == m) return true; 
    return false; } 
  private boolean isTool(Material m) { for (int i = 0; i < toolsArray.length; i++) if (toolsArray[i] == m) return true; 
    return false; } 
  private boolean isChainmail(Material m) { return (m == Material.CHAINMAIL_BOOTS) || (m == Material.CHAINMAIL_CHESTPLATE) || (m == Material.CHAINMAIL_HELMET) || (m == Material.CHAINMAIL_LEGGINGS); } 
  private String lang(String key) { return local.getString(key); } 
  private boolean showUsage(CommandSender s, String cmd) {
    s.sendMessage(colorize("&e" + cmd + " - " + lang(new StringBuilder(String.valueOf(cmd)).append("Help").toString())));
    s.sendMessage(colorize("&eUsage: " + lang(new StringBuilder(String.valueOf(cmd)).append("Syntax").toString()).replaceAll("<command>", cmd)));
    if ((s.hasPermission(cmd + ".admin")) && (lang(cmd + "AdminSyntax") != null)) s.sendMessage("Admin Usage: " + lang(new StringBuilder(String.valueOf(cmd)).append("AdminSyntax").toString()).replaceAll("<command>", cmd));
    return lang(cmd + "Help") != null;
  }

  private void msgNearby(Player p, String msg)
  {
    Entity en;
    label58: for (Iterator localIterator = p.getNearbyEntities(48.0D, 48.0D, 48.0D).iterator(); localIterator.hasNext(); ((Player)en).sendMessage(msg)) { en = (Entity)localIterator.next(); if ((!(en instanceof Player)) || (en == p)) break label58;  } 
  }

  public void onEnable() {
    log = getLogger(); random = new Random();
    if (!getDataFolder().isDirectory()) getDataFolder().mkdirs();
    getConfig().options().copyDefaults(true); writeConfig();
    String languagefile = getConfig().getString("global_language") + ".yml";
    local = YamlConfiguration.loadConfiguration(new File(getDataFolder(), languagefile)); writeConfig(local, languagefile);
    if (!setupEconomy()) { log.severe(toString() + " - Disabled because of missing dependency (Vault)"); getServer().getPluginManager().disablePlugin(this); return; }
    setupPermissions(); init(); getServer().getPluginManager().registerEvents(this, this); log.info(toString() + " - Enabled");
  }
  public void onDisable() {
    getServer().getScheduler().cancelAllTasks();
    writeConfig();
    String configName;
    for (Iterator localIterator = configs.keySet().iterator(); localIterator.hasNext(); saveConfig(configName)) configName = (String)localIterator.next(); log.info(toString() + " - Saved and Disabled");
  }

  private void init()
  {
    File announceFile = new File(getDataFolder(), "announcements.txt"); boolean entity = true;
    if (announceFile.exists()) {
      announcements = Arrays.asList(loadText(announceFile).split("\n"));
    } else {
      String[] def = { "This is the first announcement", "This is the second announcement" };
      announcements = Arrays.asList(def); saveText(def, announceFile);
    }
    task("abandonedminecarts", Integer.valueOf(enabled("abandonedminecarts") ? abandonedminecartsStart() : -1));
    task("announcements", Integer.valueOf((enabled("announcements")) && (announcements.size() > 0) ? announceStart() : -1));
    task("antiovercrowding", Integer.valueOf(enabled("antiovercrowding") ? antiovercrowdingStart() : -1));
    task("arena", Integer.valueOf(enabled("arena") ? arenaStart() : -1));
    task("author", Integer.valueOf(entity ? authorStart() : -1));
    task("playtime", Integer.valueOf(enabled("playtime") ? playtimeStart() : -1));
    task("regionlabels", Integer.valueOf(enabled("regionlabels") ? regionlabelsStart() : -1));
    task("regionrestore", Integer.valueOf(enabled("regionrestore") ? regionrestoreStart() : -1));
    task("serverlogarchive", Integer.valueOf(enabled("serverlogarchive") ? serverlogarchiveStart() : -1));
    task("usefulcompass", Integer.valueOf(enabled("gameplay_usefulcompass") ? usefulcompassStart() : -1));
    task("worlddate", Integer.valueOf((enabled("worlddate")) && (enabled("worlddate_announce")) ? dateStart() : -1));
    customWarps.clear();
    for (String warp : config("customwarps").getKeys(false)) {
      String[] info = config("customwarps").getString(warp).split("_");
      List coords = new ArrayList(); coords.add(info[0]); coords.add(info[1]); coords.add(info[2]); coords.add(info[3]); coords.add(info[4]); coords.add(info[5]);
      customWarps.put(warp, coords);
    }
    regionprotectRegions = new HashMap();
    if (!enabled("regionprotect")) {
      regionprotectIgnore = new ArrayList();
    } else {
      regionprotectIgnore = Arrays.asList(getConfig().getString("regionprotect_ignoreblockidlist").split(","));
      for (String region : config("regionprotect").getKeys(false)) {
        String[] cuboid = config("regionprotect").getString(region).split("_");
        regionprotectRegions.put(region, cuboid);
      }
    }
  }

  static String colorize(String string) { return string.replaceAll("(?i)&([a-k0-9])", "\u00A7$1"); } 
  static String timestamp(Date d) { return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d); } 
  static String datestamp(Date d) { return new SimpleDateFormat("yyyy-MM-dd").format(d); } 
  void debug(String s) { if (enabled("global_debug")) log.info("[DEBUG] " + s);  } 
  void broadcastEvent(String s) { if (!enabled("global_hideeventsfromconsole")) getServer().getConsoleSender().sendMessage(colorize(s)); for (Player p : getServer().getOnlinePlayers()) if (p.hasPermission("v.eventbroadcasts")) p.sendMessage(colorize(s));   } 
  boolean logEvent(String eventName, String text) { return appendText(timestamp(new Date()) + " [" + eventName + "] " + text, new File(getDataFolder(), "events.log")); } 
  void cuboidFill(World w, Double minx, Double miny, Double minz, Double maxx, Double maxy, Double maxz, int blockID, Predicate<Integer> p) {
    for (Double x = minx; x.doubleValue() <= maxx.doubleValue(); x = Double.valueOf(x.doubleValue() + 1.0D)) for (Double y = miny; y.doubleValue() <= maxy.doubleValue(); y = Double.valueOf(y.doubleValue() + 1.0D)) for (Double z = minz; z.doubleValue() <= maxz.doubleValue(); z = Double.valueOf(z.doubleValue() + 1.0D)) { Location l = new Location(w, x.doubleValue(), y.doubleValue(), z.doubleValue()); if ((p == null) || (p.test(Integer.valueOf(l.getBlock().getTypeId())))) l.getBlock().setTypeId(blockID);  }   
  }
  YamlConfiguration config(String configName) {
    if (!configs.containsKey(configName)) configs.put(configName, YamlConfiguration.loadConfiguration(new File(getDataFolder(), configName + ".yml"))); return (YamlConfiguration)configs.get(configName); } 
  private void deleteConfig(String configName) { if (configs.containsKey(configName)) configs.remove(configName); File f = new File(getDataFolder(), configName + ".yml"); if (f.exists()) f.delete();  } 
  boolean saveConfig(String configName) {
    if (!configs.containsKey(configName)) configs.put(configName, YamlConfiguration.loadConfiguration(new File(getDataFolder(), configName + ".yml"))); try {
      debug("saving config file " + configName + ".yml"); ((YamlConfiguration)configs.get(configName)).save(new File(getDataFolder(), configName + ".yml")); return true; } catch (IOException e) {
      log.severe("IO Error while saving file '" + configName + ".yml' to plugin data folder."); e.printStackTrace(); } return false;
  }
  private boolean setupEconomy() {
    if (getServer().getPluginManager().getPlugin("Vault") == null) return false; RegisteredServiceProvider rsp = getServer().getServicesManager().getRegistration(Economy.class); if (rsp == null) return false; econ = (Economy)rsp.getProvider(); return econ != null; } 
  private boolean setupPermissions() { RegisteredServiceProvider rsp = getServer().getServicesManager().getRegistration(Permission.class); perms = (Permission)rsp.getProvider(); return perms != null; } 
  private TreeMap<String, Double> mapSort(HashMap<String, Double> map) {
    TreeMap sorted_map = new TreeMap(new ValueComparator(map)); sorted_map.putAll(map); return sorted_map;
  }
  private void task(String name, Integer id) { if (tasks.containsKey(name)) getServer().getScheduler().cancelTask(((Integer)tasks.get(name)).intValue());
    if (id.intValue() == -1) tasks.remove(name); else tasks.put(name, id);  } 
  private boolean has(String[] args, int index, String s) {
    if (args.length >= index + 1) return args[index].equalsIgnoreCase(s); return false;
  }
  private boolean enabled(String module) { return (getConfig().getBoolean(module)) && ((!module.startsWith("gameplay_")) || (enabled("gameplay"))) && ((!module.startsWith("townymods_")) || (enabled("townymods"))); }

  private boolean auth(CommandSender s, String permission) {
    if ((!(s instanceof Player)) || (perms.playerHas((Player)s, "v." + permission))) return true;
    s.sendMessage("You don't have access to do that..."); debug("player [" + s.getName() + "] denied permission [v." + permission + "]"); return false;
  }
  private void setupNew(Player p, String id, String name, String option) { setup(p, id, name, option, 1, null); } 
  private void setup(Player p, String id, String name, String option, int step, Block b) {
    setupOp = p; setup = id; setupName = name; setupOption = option; setupStep = step;

    if (b != null) { blockChoices.add(b); setupStep += 1; }
    if (setupStep <= 2) p.sendMessage(lang("region_select" + setupStep)); else setupComplete(); 
  }

  private void setupComplete() { if ((setup.equals("regionlabel")) || (setup.equals("regionprotect"))) {
      Block b1 = (Block)blockChoices.get(0); Block b2 = (Block)blockChoices.get(1);
      Double x1 = Double.valueOf(b1.getX()); Double y1 = Double.valueOf(b1.getY()); Double z1 = Double.valueOf(b1.getZ()); Double x2 = Double.valueOf(b2.getX()); Double y2 = Double.valueOf(b2.getY()); Double z2 = Double.valueOf(b2.getZ());
      Double minx = Double.valueOf(Math.min(x1.doubleValue(), x2.doubleValue())); Double miny = Double.valueOf(Math.min(y1.doubleValue(), y2.doubleValue())); Double minz = Double.valueOf(Math.min(z1.doubleValue(), z2.doubleValue())); Double maxx = Double.valueOf(Math.max(x1.doubleValue(), x2.doubleValue())); Double maxy = Double.valueOf(Math.max(y1.doubleValue(), y2.doubleValue())); Double maxz = Double.valueOf(Math.max(z1.doubleValue(), z2.doubleValue()));
      if (setupOption.equals("exact")) config("regionlabels").set(setupName, b1.getWorld().getName() + "_" + minx + "_" + miny + "_" + minz + "_" + (maxx.doubleValue() + 1.0D) + "_" + (maxy.doubleValue() + 1.0D) + "_" + (maxz.doubleValue() + 1.0D)); else
        config(setup).set(setupName, b1.getWorld().getName() + "_" + minx + "_" + 0 + "_" + minz + "_" + (maxx.doubleValue() + 1.0D) + "_" + 999 + "_" + (maxz.doubleValue() + 1.0D));
      saveConfig(setup); setupOp.sendMessage("Region '" + setupName.replaceAll("_", " ") + "' has been " + setup.replaceAll("region", "") + "ed.");
    } else if (setup.equals("regionrestore")) {
      Block b1 = (Block)blockChoices.get(0); Block b2 = (Block)blockChoices.get(1);
      if (tasks.containsKey("regionsavebatch")) setupOp.sendMessage("Canceled save that was in progress to start new save...");
      task("regionsavebatch", Integer.valueOf(regionsaveBatch(setupName, b1, b2, setupOption, setupOp)));
    } else {
      setupOp.sendMessage("Invalid setup type (this should never happen!)");
    }setup = null; setupName = null; setupOption = null; setupOp = null; setupStep = 1; blockChoices = new ArrayList(); }

  public boolean onCommand(CommandSender s, Command cmd, String commandLabel, String[] args)
  {
    if ((cmd.getName().equalsIgnoreCase("vhelp")) && (auth(s, "admin"))) {
      vhelp(s, args.length >= 1 ? args[0] : null, args.length >= 2 ? args[1] : null); return true;
    }
    if ((cmd.getName().equalsIgnoreCase("vtoggle")) && (auth(s, "admin")) && (args.length == 1)) {
      vtoggle(s, args[0]); return true;
    }
    if ((cmd.getName().equalsIgnoreCase("vsetting")) && (auth(s, "admin")) && (args.length >= 2)) {
      String val = args[1]; for (int i = 2; i < args.length; i++) val = val + " " + args[i]; vsetting(s, args[0], val); return true;
    }
    if ((cmd.getName().equalsIgnoreCase("vreload")) && (auth(s, "admin"))) {
      reloadConfig(); init(); s.sendMessage(toString() + " configuration has been reloaded from the config.yml file."); return true;
    }
    return false;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    if (enabled("onlineplayersflatfile")) onlinePlayers(null);
    if (enabled("betternews")) betternews(event.getPlayer(), getConfig().getInt("betternews_showonlogin")); 
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) { if (enabled("onlineplayersflatfile")) onlinePlayers(event.getPlayer());
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent e)
  {
    if (e.getFrom().getBlock().equals(e.getTo().getBlock())) return;
    Player p = e.getPlayer(); Material steppingOn = e.getTo().getBlock().getRelative(0, -1, 0).getType();
    if ((steppingOn == Material.SPONGE) && (enabled("gameplay_bouncysponges")) && (p.hasPermission("v.bouncysponges"))) {
      Vector v = p.getVelocity(); v.setY(2); p.setVelocity(v); return;
    }if ((steppingOn == Material.EMERALD_BLOCK) && (enabled("gameplay_emeraldblockhaste")) && (p.hasPermission("v.emeraldblockhaste"))) {
      p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 2), true); return;
    }
  }

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent e) { if ((!regionprotectIgnore.contains(e.getBlock().getTypeId())) && (!perms.playerHas(e.getPlayer(), "v.regionprotect.bypass")) && (regionprotected(e.getBlock().getLocation()))) { e.setCancelled(true); return; } }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent e) {
    if ((!regionprotectIgnore.contains(e.getBlock().getTypeId())) && (!perms.playerHas(e.getPlayer(), "v.regionprotect.bypass")) && (regionprotected(e.getBlock().getLocation()))) { e.setCancelled(true); return; } 
  }

  @EventHandler
  public void onCreatureSpawn(CreatureSpawnEvent e) { if ((e.getEntityType() == EntityType.VILLAGER) && (getConfig().getString("gameplay_villagerprofessions").length() > 0)) {
      final Villager v = (Villager)e.getEntity();
      getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
        public void run() {
          List bannedProfessions = Arrays.asList(getConfig().getString("gameplay_villagerprofessions").toUpperCase().split(","));
          Villager.Profession[] prof = Villager.Profession.values(); String pr = v.getProfession().toString();
          debug("Villager " + v.getProfession() + " spawned, banned is " + bannedProfessions);
          if (bannedProfessions.size() == prof.length) { debug("[VillagerProfessions] villager spawned, all professions are disabled, removing villager"); v.remove(); return; }
          while (bannedProfessions.contains(pr)) {
            v.setProfession(prof[Vitals.random.nextInt(prof.length)]);
          }
          debug("[VillagerProfessions] villager spawned as " + pr + ", new profession is " + v.getProfession().toString());
        }
      }
      , 1L);
    } }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent e) {
    Player p = e.getEntity();
    if ((arenaActive != null) && (arenaActive.state.equals("active")) && (arenaActive.playerAlive(p))) { arenaActive.playerDisqualify(p, null); return; }
    if ((enabled("bounties")) && ((arenaActive == null) || (!arenaActive.playerAlive(e.getEntity()))) && ((e.getEntity().getKiller() instanceof Player)) && (e.getEntity().getKiller() != e.getEntity())) {
      String pName = e.getEntity().getName().toLowerCase(); String kName = e.getEntity().getKiller().getName().toLowerCase(); bountyDeath(kName, pName);
    }
    if (enabled("gameplay_deathretention")) deathretention(e, p); 
  }

  @EventHandler
  public void onPlayerRespawn(PlayerRespawnEvent e) { Player p = e.getPlayer();
    if (deathretentionInventory.containsKey(p.getName()))
    {
      ItemStack i;
      for (Iterator localIterator = ((List)deathretentionInventory.get(p.getName())).iterator(); localIterator.hasNext(); p.getInventory().addItem(new ItemStack[] { i })) i = (ItemStack)localIterator.next(); deathretentionInventory.remove(p.getName());
    }
    if (deathretentionArmor.containsKey(p.getName())) {
      p.getInventory().setArmorContents((ItemStack[])((List)deathretentionArmor.get(p.getName())).toArray(new ItemStack[0])); deathretentionArmor.remove(p.getName());
    } }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent e) {
    final Player p = e.getPlayer(); Location l = p.getLocation();
    if ((arenaActive != null) && (arenaActive.event.equals("RaceToTheFinish")) && (e.hasItem()) && (e.getItem().getType() == Material.ENDER_PEARL)) { e.setCancelled(true); return; }
    if ((enabled("gameplay_blazerodfireball")) && (e.hasItem()) && (e.getItem().getType() == Material.BLAZE_ROD) && ((e.getAction() == Action.RIGHT_CLICK_BLOCK) || (e.getAction() == Action.RIGHT_CLICK_AIR)) && (auth(p, "blazerodfireball"))) {
      removeItem(p, Material.BLAZE_ROD);
      Location loc = p.getEyeLocation().toVector().add(l.getDirection().multiply(2)).toLocation(p.getWorld(), l.getYaw(), l.getPitch());
      p.getWorld().spawn(loc, Fireball.class);
    }
    if ((enabled("gameplay_featherfly")) && (e.hasItem()) && (e.getItem().getType() == Material.FEATHER) && ((e.getAction() == Action.RIGHT_CLICK_BLOCK) || (e.getAction() == Action.RIGHT_CLICK_AIR)) && (auth(p, "featherfly"))) {
      if (p.isFlying()) {
        p.setAllowFlight(false); p.sendMessage(lang("featherfly_cancel")); msgNearby(p, p.getName() + lang("featherfly_cancelothers"));
        if (flyingTasks.containsKey(p.getName())) {
          getServer().getScheduler().cancelTask(((Integer)flyingTasks.get(p.getName())).intValue()); flyingTasks.remove(p.getName());
        }
        return;
      }
      l.add(0.0D, 1.0D, 0.0D); p.teleport(l); p.setAllowFlight(true); p.setFlying(true);
      removeItem(p, Material.FEATHER); p.sendMessage(lang("featherfly_effect")); msgNearby(p, p.getName() + lang("featherfly_effectothers"));
      flyingTasks.put(p.getName(), Integer.valueOf(getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
        public void run() {
          debug("[" + p + ", " + p.getName() + ", " + flyingTasks.size() + "]");
          if (!p.isOnline()) {
            getServer().getScheduler().cancelTask(((Integer)flyingTasks.get(p.getName())).intValue()); flyingTasks.remove(p.getName());
          } else if ((!p.isFlying()) || (!removeItem(p, Material.FEATHER))) {
            p.setAllowFlight(false); p.sendMessage(Vitals.this.lang("featherfly_cancel")); Vitals.this.msgNearby(p, p.getName() + Vitals.access$0(Vitals.this, "featherfly_cancelothers"));
            getServer().getScheduler().cancelTask(((Integer)flyingTasks.get(p.getName())).intValue()); flyingTasks.remove(p.getName());
          }
        }
      }
      , getConfig().getLong("gameplay_featherfly_interval") * 20L, getConfig().getLong("gameplay_featherfly_interval") * 20L)));
      return;
    }
    if (e.isCancelled()) return;
    if ((arenaActive != null) && (arenaActive.state.equals("setup"))) { arenaActive.setup(e); return; }
    if ((setup != null) && (p == setupOp) && (e.getAction() == Action.RIGHT_CLICK_BLOCK)) { setup(p, setup, setupName, setupOption, setupStep, e.getClickedBlock()); return; }
    if ((enabled("gameplay_safejukebox")) && (p.hasPermission("v.safejukebox")) && (e.getAction() == Action.RIGHT_CLICK_BLOCK) && (e.getClickedBlock().getType() == Material.JUKEBOX) && (e.hasItem()) && (isRecord(e.getItem().getType()))) {
      e.setCancelled(true); p.getWorld().playEffect(e.getClickedBlock().getLocation(), Effect.RECORD_PLAY, e.getItem().getTypeId()); return;
    }
  }

  public boolean removeItem(Player p, Material m) { Inventory inv = p.getInventory();
    for (int i = 0; i < inv.getSize(); i++) {
      ItemStack is = inv.getItem(i);
      if ((is != null) && (is.getType() == m)) {
        if (is.getAmount() > 1) is.setAmount(is.getAmount() - 1); else inv.setItem(i, null); return true;
      }
    }
    return false; }

  @EventHandler
  public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
    if ((enabled("gameplay_villagerpermission")) && ((e.getRightClicked() instanceof Villager)) && (!e.getPlayer().hasPermission("v.villagerpermission"))) e.setCancelled(true); 
  }

  @EventHandler
  public void onPlayerChat(AsyncPlayerChatEvent e) { if ((enabled("anticaps")) && (!perms.playerHas(e.getPlayer(), "v.anticaps.bypass")) && (e.getMessage().length() >= 10)) {
      int numCaps = 0; for (int i = 0; i < e.getMessage().length(); i++) if (Character.isUpperCase(e.getMessage().charAt(i))) numCaps++;
      if (1.0D * numCaps / e.getMessage().length() >= getConfig().getDouble("anticaps_cutoffpercent") / 100.0D) {
        e.setCancelled(true); e.getPlayer().sendMessage(lang("anticaps")); return;
      }
    }
    boolean global = false; for (String keyword : getConfig().getString("chatworlds_globalkeywords").split(",")) if (e.getMessage().toLowerCase().contains(keyword.toLowerCase())) global = true;
    if ((enabled("chatworlds")) && (!global) && (e.getRecipients().size() > 1)) for (Player p : (Player[])e.getRecipients().toArray(new Player[0]))
        if ((p.getWorld() != e.getPlayer().getWorld()) && (!p.hasPermission("v.chatworlds.bypass")) && (!perms.playerHas(e.getPlayer(), "v.chatworlds.bypass"))) e.getRecipients().remove(p);
    Object domainRegex;
    if ((enabled("antiadvertising")) && (!perms.playerHas(e.getPlayer(), "v.antiadvertising.bypass"))) {
      String ipRegex = "(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])"; domainRegex = "[a-z0-9]+([\\-\\.]{1}[a-z0-9]+)*\\.[a-z]{2,6}";
      String[] whitelist = getConfig().getString("antiadvertising_whitelist").split(",");
      String msg = e.getMessage(); for (int i = 0; i < whitelist.length; i++) msg = msg.replaceAll(whitelist[i], "~~~" + i + "~~~");
      if ((msg.matches(".*" + ipRegex + ".*")) || (msg.matches(".*" + (String)domainRegex + ".*"))) {
        debug("advertising detected, taking action [" + getConfig().getString("antiadvertising_action") + "]");
        if (getConfig().getString("antiadvertising_action").equals("mute")) { e.setCancelled(true); return; }
        if (getConfig().getString("antiadvertising_action").equals("mute")) { e.setCancelled(true); e.getPlayer().kickPlayer(""); return; }
        if (getConfig().getString("antiadvertising_action").equals("secretmute")) {
          Set r = e.getRecipients(); r.clear(); r.add(e.getPlayer());
          for (Player p : getServer().getOnlinePlayers()) if (p.isOp()) { r.add(p); p.sendMessage(colorize("&6[AntiAdvertising] Muted message for everyone except sender and ops")); }  
        }
        else if (getConfig().getString("antiadvertising_action").equals("replace")) {
          msg = msg.replaceAll(ipRegex, "").replaceAll((String)domainRegex, "");
          for (int i = 0; i < whitelist.length; i++) msg = msg.replaceAll("~~~" + i + "~~~", whitelist[i]);
          e.setMessage(msg);
        }
      }
    }
    if (enabled("antistickykeys")) e.setMessage(e.getMessage().replaceAll("(.)\\1{4,}", "$1"));
    if (enabled("wordswap"))
    {
      String word;
      for (domainRegex = config("wordswap").getKeys(false).iterator(); ((Iterator)domainRegex).hasNext(); e.setMessage(e.getMessage().replaceAll("(?i)" + word, config("wordswap").getString(word)))) word = (String)((Iterator)domainRegex).next(); 
    }
    if (enabled("helperbot"))
      for (domainRegex = config("helperbot").getKeys(false).iterator(); ((Iterator)domainRegex).hasNext(); ) { String wordpair = (String)((Iterator)domainRegex).next();
        boolean bot = true; String[] botWords = wordpair.split("_"); String[] chatWords = e.getMessage().split(" "); if (!botWords[0].equals("command")) {
          final String response = config("helperbot").getString(wordpair);
          for (String botWord : botWords) {
            boolean wordFound = false;
            for (int i = 0; i < chatWords.length; i++) if (chatWords[i].toLowerCase().equals(botWord.toLowerCase())) wordFound = true;
            if (!wordFound) bot = false;
          }
          if (bot)
            getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
              public void run() { getServer().broadcastMessage(Vitals.colorize(getConfig().getString("helperbot_chatprefix") + response)); }

            }
            , 20L);
        }
      }
    if ((enabled("modwarnings")) && (enabled("modwarnings_showlevel"))) {
      int warningLevel = warnLevel(e.getPlayer());
      if (warningLevel > 0) {
        char[] s = new char[warningLevel]; Arrays.fill(s, '*'); String stars = new String(s);
        e.setMessage(colorize(getConfig().getString("modwarnings_chatprefix").replaceFirst("\\*", stars)) + " " + e.getMessage());
      }
    } }

  @EventHandler
  public void onPlayerTeleport(PlayerTeleportEvent e) {
    if (enabled("regionlabels")) {
      String toRegion = regionGet("regionlabels", e.getTo()); if (toRegion != null) e.getPlayer().sendMessage(colorize(lang("regionlabels_enter").replaceAll("\\{region\\}", toRegion.replaceAll("_", " ")))); 
    }
  }

  @EventHandler(priority=EventPriority.LOW)
  public void onEntityDamage(EntityDamageEvent e) { if (((e.getEntity() instanceof Villager)) && (enabled("unkillablevillagers"))) { e.setCancelled(true); return; }
    if ((e.getEntity() instanceof Player)) {
      Player p = (Player)e.getEntity();
      if ((enabled("gameplay_damagestopsflying")) && (p.isFlying())) p.setFlying(false);
      if ((enabled("gameplay_bouncysponges")) && (e.getCause() == EntityDamageEvent.DamageCause.FALL) && (p.getLocation().getBlock().getRelative(0, -1, 0).getType() == Material.SPONGE)) { e.setCancelled(true); return; }
      if ((enabled("gameplay_emeraldblockhaste")) && (e.getCause() == EntityDamageEvent.DamageCause.FALL) && (p.getLocation().getBlock().getRelative(0, -1, 0).getType() == Material.EMERALD_BLOCK)) { e.setCancelled(true); return; }
      if ((arenaActive != null) && (arenaActive.playerAlive(p)))
      {
        if (arenaActive.event.equals("RaceToTheFinish")) { e.setCancelled(true); return; }
        if (((e instanceof EntityDamageByEntityEvent)) && ((((EntityDamageByEntityEvent)e).getDamager() instanceof Player))) {
          if (arenaActive.state.equals("signup")) { e.setCancelled(true); return; }
          EntityDamageByEntityEvent e2 = (EntityDamageByEntityEvent)e; Player p2 = (Player)e2.getDamager();

          if ((arenaActive.event.equals("Team PVP")) && (arenaActive.playerAlive(p2)) && (arenaActive.sameTeam(p, p2))) { e.setCancelled(true); return; }
        }
      }
      if ((getConfig().getDouble("gameplay_superchainmail") != 1.0D) && (p.hasPermission("v.superchainmail"))) {
        Double damageFactor = Double.valueOf(1.0D); int numChainmail = 0;
        for (ItemStack armor : p.getInventory().getArmorContents()) if (isChainmail(armor.getType())) { numChainmail++; damageFactor = Double.valueOf(damageFactor.doubleValue() * getConfig().getDouble("gameplay_superchainmail")); }
        long lastNotify = chainmailNotify.containsKey(p.getName()) ? (new Date().getTime() - ((Long)chainmailNotify.get(p.getName())).longValue()) / 1000L : 99999999L;
        if ((numChainmail > 0) && (lastNotify > 10L)) { chainmailNotify.put(p.getName(), Long.valueOf(new Date().getTime())); p.sendMessage(colorize("&7" + lang("superchainmail_effect") + Math.round(100.0D * (1.0D - damageFactor.doubleValue())) + "%!")); }
        e.setDamage((int)Math.round(damageFactor.doubleValue() * e.getDamage()));
      }
    } }

  public boolean did(PlayerCommandPreprocessEvent e, CommandSender s, String c, String[] args, String mod, String perm, String cmd, int min, int max) {
    if (!c.equalsIgnoreCase("/" + cmd)) return false;
    if (!enabled(mod)) { if (s.isOp()) s.sendMessage("[Vitals module " + mod + " is not enabled - op-only message]"); return false; }
    e.setCancelled(true); String cc = c; for (int i = 0; i < args.length; i++) cc = cc + " " + args[i];
    String msg = "[COMMAND] " + s.getName() + ": " + cc;
    if (!auth(s, perm)) { debug(msg + " [access denied]"); return false; }
    if ((args.length >= min) && (args.length <= max)) { debug(msg); return true; }
    debug(msg + " [invalid arguments, showing syntax]");
    showUsage(s, cmd);
    return false;
  }
  @EventHandler
  public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
    final Player p = e.getPlayer(); String cc = e.getMessage(); String c = cc.split(" ")[0].toLowerCase();
    String[] a = cc.indexOf(" ") == -1 ? new String[0] : cc.substring(cc.indexOf(" ") + 1).split(" ");
    if (did(e, p, c, a, "announcements", "announce", "announce", 1, 99)) { announce(p, a); return; }
    if (did(e, p, c, a, "arena", "arena", "arena", 0, 99)) { arena(p, a); return; }
    if (did(e, p, c, a, "auctions", "auction", "auction", 0, 1)) { auction(p, a); return; }
    if (did(e, p, c, a, "auctions", "auction.bid", "bid", 1, 1)) { auctionbid(p, a[0]); return; }
    if (did(e, p, c, a, "betterhelp", "help", "help", 0, 2)) { betterhelpCmd(p, a); return; }
    if (did(e, p, c, a, "betternews", "news", "news", 0, 99)) { betternewsCmd(p, a); return; }
    if (did(e, p, c, a, "bounties", "bounty", "bounty", 0, 2)) { bountyCmd(p, a); return; }
    if (did(e, p, c, a, "econpromotions", "buyrank", "buyrank", 0, 1)) { buyrank(p, a.length > 0 ? a[0] : ""); return; }
    if (did(e, p, c, a, "chunkregen", "chunkregen", "chunkregen", 0, 0)) { chunkregen(p); return; }
    if (did(e, p, c, a, "countdown", "countdown", "countdown", 1, 99)) { countdown(p, a); return; }
    if (did(e, p, c, a, "customwarps", "customwarp", "customwarp", 0, 2)) { customwarpCmd(p, a.length > 0 ? a[0] : null, has(a, 1, "delete")); return; }
    if (did(e, p, c, a, "worlddate", "date", "date", 0, 1)) { dateCmd(p, a.length > 0 ? a[0] : null); return; }
    if (did(e, p, c, a, "townymods_findmyplot", "findmyplot", "findmyplot", 0, 2)) { findmyplotCmd(p, a); return; }
    if (did(e, p, c, a, "townymods_findplot", "findplot", "findplot", 0, 0)) { findplot(p); return; }
    if (did(e, p, c, a, "gamemodeall", "gmall", "gmall", 0, 99)) { gmall(p, a.length == 0 ? "" : a[0]); return; }
    if (did(e, p, c, a, "helperbot", "helperbot.admin", "helperbot", 1, 99)) { helperbot(p, a); return; }
    if (did(e, p, c, a, "modvote", "modvote", "modvote", 0, 1)) { modvote(p, a.length == 0 ? "" : a[0]); return; }
    if (did(e, p, c, a, "playerpasswords", "password", "password", 1, 1)) { playerPassword(p, a[0]); return; }
    if (did(e, p, c, a, "playtime", "playtime", "playtime", 0, 2)) { playtimeCmd(p, a); return; }
    if (did(e, p, c, a, "townymods_plotsalesign", "plotsalesign", "plotsalesign", 0, 99)) { plotsalesign(p, a); return; }
    if (did(e, p, c, a, "regionlabels", "regionlabels.admin", "regionlabel", 0, 99)) { regionlabel(p, a); return; }
    if (did(e, p, c, a, "regionprotect", "regionprotect", "regionprotect", 0, 99)) { regionprotect(p, a); return; }
    if (did(e, p, c, a, "regionrestore", "regionrestore", "regionrestore", 1, 1)) { regionrestore(p, a[0]); return; }
    if (did(e, p, c, a, "regionrestore", "regionrestore", "regionsave", 0, 99)) { regionsave(p, a); return; }
    if (did(e, p, c, a, "serverlogarchive", "serverlogarchive", "serverlogarchive", 0, 0)) { serverlogarchive(p); return; }
    if (did(e, p, c, a, "modwarnings", "warn", "warn", 0, 99)) { warnCmd(p, a); return; }
    if (did(e, p, c, a, "wordswap", "wordswap", "wordswap", 0, 99)) { wordswap(p, a); return; }
    if (did(e, p, c, a, "damageditemsales", "", "smithy", 1, 99)) { damagedItemSales(p, a[0]); return;
    }
    if ((enabled("oponlyfromconsole")) && ((c.equals("/op")) || (c.equals("/deop")))) { e.setCancelled(true); return; }
    if ((arenaActive != null) && (arenaActive.playerAlive(p))) for (String cmd : getConfig().getString("arena_disabledcommands").split(","))
        if (c.equalsIgnoreCase("/" + cmd)) { e.setCancelled(true); p.sendMessage(colorize(lang("arena_disabledcommand"))); return;
        }
    for (String cmd : getConfig().getString("global_autokickcommands").split(",")) if ((c.equalsIgnoreCase("/" + cmd)) && (!p.isOp())) { e.setCancelled(true); p.kickPlayer(""); return; }
    if ((cc.equalsIgnoreCase("/worth")) && (p.hasPermission("essentials.sell")) && (enabled("damageditemsales")) && (damagedItemFullValue(p.getItemInHand()) > 0L)) {
      e.setCancelled(true); damagedItemSales(p, "worth"); return;
    }
    if ((cc.equalsIgnoreCase("/sell hand")) && (p.hasPermission("essentials.worth")) && (enabled("damageditemsales")) && (damagedItemFullValue(p.getItemInHand()) > 0L)) {
      e.setCancelled(true); damagedItemSales(p, "sell"); return;
    }
    if (cc.equalsIgnoreCase("/plot claim")) {
      Double restrictedPrice = Double.valueOf(getConfig().getDouble("townymods_restrictedplots")); if (restrictedPrice.doubleValue() == -1.0D) return;
      plotPrice = findplotprice(p.getLocation()); if (((Double)plotPrice).doubleValue() == 0.0D) return;
      boolean permsOverride = p.hasPermission("v.restrictedplots.buy"); boolean buyCancelled = (restrictedPrice.doubleValue() > 0.0D) && (restrictedPrice.equals(plotPrice)) && (!permsOverride);
      debug("[plotclaim] player [" + p.getName() + "] plotPrice [" + plotPrice + "] restrictedPrice [" + restrictedPrice + "] permsOverride [" + permsOverride + "] buyCancelled [" + buyCancelled + "]");
      if (buyCancelled) { e.setCancelled(true); p.sendMessage(lang("towny_restricted")); return; }
      if (enabled("townymods_sethomereminder")) {
        getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
          public void run() {
            p.sendMessage(Vitals.colorize(Vitals.this.lang("towny_sethomereminder1")));
            p.sendMessage(Vitals.colorize(Vitals.this.lang("towny_sethomereminder2")));
            p.sendMessage(Vitals.colorize(Vitals.this.lang("towny_sethomereminder3")));
          }
        }
        , 20L);
      }
      return;
    }
    label1968: for (Object plotPrice = customWarps.keySet().iterator(); ((Iterator)plotPrice).hasNext(); 
      return)
    {
      String warp = (String)((Iterator)plotPrice).next(); if ((!cc.equalsIgnoreCase("/" + warp)) || ((!p.hasPermission("v.customwarps." + warp)) && (!p.hasPermission("v.customwarps.*")))) break label1968;
      if ((arenaActive != null) && (arenaActive.playerAlive(p))) { p.sendMessage(colorize(lang("arena_disabledcommand"))); return; }
      p.sendMessage(colorize(lang("customwarps_action") + warp + "..."));
      List coords = (List)customWarps.get(warp);
      World w = getServer().getWorld((String)coords.get(0)); if (w == null) return;
      playerTeleport(p, new Location(w, Double.parseDouble((String)coords.get(1)) + 0.5D, Double.parseDouble((String)coords.get(2)), Double.parseDouble((String)coords.get(3)), Float.parseFloat((String)coords.get(4)), Float.parseFloat((String)coords.get(5))), null);
      e.setCancelled(true);
    }
    if (enabled("helperbot")) for (plotPrice = config("helperbot").getKeys(false).iterator(); ((Iterator)plotPrice).hasNext(); ) { String wordpair = (String)((Iterator)plotPrice).next();
        String[] botWords = wordpair.split("_"); final String response = config("helperbot").getString(wordpair);
        if ((botWords[0].equalsIgnoreCase("command")) && (c.equalsIgnoreCase("/" + botWords[1])))
          getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
            public void run() { p.sendMessage(Vitals.colorize(getConfig().getString("helperbot_chatprefix") + response)); }

          }
          , 20L);
      }
  }

  private int abandonedminecartsStart()
  {
    return getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      public void run() {
        int numMinecarts = 0; int numMotionless = 0; int numRemoved = 0;
        Iterator localIterator2;
        for (Iterator localIterator1 = getServer().getWorlds().iterator(); localIterator1.hasNext(); 
          localIterator2.hasNext())
        {
          World world = (World)localIterator1.next();
          localIterator2 = world.getEntities().iterator(); continue; Entity entity = (Entity)localIterator2.next();
          if (((entity instanceof Minecart)) && (!(entity instanceof StorageMinecart)) && (!(entity instanceof PoweredMinecart))) {
            numMinecarts++; if (entity.getVelocity().length() < 0.001D) numMotionless++;
            if ((entity.getPassenger() == null) && (entity.getVelocity().length() < 0.001D)) {
              if (unusedCarts.containsKey(entity)) unusedCarts.put(entity, Integer.valueOf(((Integer)unusedCarts.get(entity)).intValue() + 1)); else unusedCarts.put(entity, Integer.valueOf(1));
              if (((Integer)unusedCarts.get(entity)).intValue() >= 3) { numRemoved++; unusedCarts.remove(entity); entity.remove(); }
            } else if (unusedCarts.containsKey(entity)) { unusedCarts.remove(entity); }

          }
        }
        if (numRemoved > 0) debug("[abandonedminecarts] removed " + numRemoved + " motionless minecarts.  " + (numMinecarts - numRemoved) + " minecarts remain in existence, " + (numMotionless - numRemoved) + " are motionless and will be removed soon if they stay motionless");
      }
    }
    , 1200L, 1200L);
  }

  private void announce(Player p, String[] args) {
    if (has(args, 0, "list")) {
      p.sendMessage(colorize("&c[Announcements List]"));
      if (announcements.size() == 0) { p.sendMessage("No announcements. To add one, type /announce add <msg>"); return; }
      int i = 1;
      String msg;
      for (Iterator localIterator = announcements.iterator(); localIterator.hasNext(); p.sendMessage(i++ + ": " + msg)) msg = (String)localIterator.next(); 
    }
    else if (has(args, 0, "add")) {
      String msg = ""; for (int i = 1; i < args.length; i++) msg = msg + (msg.length() > 0 ? " " : "") + args[i];
      List newAnnounce = new ArrayList();
      for (int i = 0; i < announcements.size(); i++) newAnnounce.add((String)announcements.get(i)); newAnnounce.add(msg);
      announcements = newAnnounce; p.sendMessage("Announcement added: " + msg);
      saveText((String[])announcements.toArray(new String[0]), new File(getDataFolder(), "announcements.txt")); init();
    } else if ((has(args, 0, "edit")) && (args.length >= 2)) {
      try { idx = Integer.parseInt(args[1]); }
      catch (Exception e)
      {
        int idx;
        p.sendMessage("Wrong syntax. Should be /announce edit <id> <msg>");
        return;
      }
      int idx;
      String msg = ""; for (int i = 2; i < args.length; i++) msg = msg + (msg.length() > 0 ? " " : "") + args[i];
      announcements.set(idx - 1, msg); p.sendMessage("Announcement changed: " + msg);
      saveText((String[])announcements.toArray(new String[0]), new File(getDataFolder(), "announcements.txt")); init();
    } else if (has(args, 0, "remove")) {
      try { idx = Integer.parseInt(args[1]); }
      catch (Exception e)
      {
        int idx;
        p.sendMessage("Wrong syntax. Should be /announce remove <id>");
        return;
      }
      int idx;
      List newAnnounce = new ArrayList();
      for (int i = 0; i < announcements.size(); i++) if (i != idx - 1) newAnnounce.add((String)announcements.get(i));
      announcements = newAnnounce; p.sendMessage("Announcement " + idx + " removed.");
      saveText((String[])announcements.toArray(new String[0]), new File(getDataFolder(), "announcements.txt")); init();
    }
  }

  private int announceStart() { long announceInterval = getConfig().getLong("announcements_interval");
    if (announceInterval < 10L) { log.severe(toString() + " could not enable Announcements: interval must be at least 10 seconds to prevent spamming your server."); return -1; }
    return getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
      private int lastAnnouncement = -1;

      public void run() { if (Vitals.this.enabled("announcements.random")) {
          lastAnnouncement = Vitals.random.nextInt(announcements.size());
        }
        else if (++lastAnnouncement > announcements.size() - 1) lastAnnouncement = 0;

        String announcement = getConfig().getString("announcements_prefix") + (String)announcements.get(lastAnnouncement);
        getServer().broadcastMessage(Vitals.colorize(announcement));
      }
    }
    , 600L, announceInterval * 20L); }

  private int antiovercrowdingStart()
  {
    return getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      final int threshold = getConfig().getInt("antiovercrowding_maxentities");

      public void run() { int numRemoved = 0; int max = 0;
        Iterator localIterator2;
        for (Iterator localIterator1 = getServer().getWorlds().iterator(); localIterator1.hasNext(); 
          localIterator2.hasNext())
        {
          World w = (World)localIterator1.next();
          localIterator2 = w.getEntities().iterator(); continue; Entity e = (Entity)localIterator2.next();
          int nearby = e.getNearbyEntities(9.0D, 9.0D, 9.0D).size();
          if ((nearby > threshold) && ((max == 0) || (numRemoved < max)) && (!(e instanceof Player))) {
            if (max == 0) max = (int)(nearby - threshold * 0.8D); numRemoved++; e.remove();
          }
        }

        if (numRemoved > 0) debug("[antiovercrowding] removed " + numRemoved + " entities");
      }
    }
    , 20L, 300L);
  }

  private void arena(Player p, String[] args) {
    if ((has(args, 0, "list")) && (auth(p, "arena.admin"))) {
      p.sendMessage(colorize("&c[Current Arena Setup]"));
      for (String arena : config("arena").getConfigurationSection("arena").getKeys(false))
        p.sendMessage(arena + " " + config("arena").getConfigurationSection(new StringBuilder("arena.").append(arena).toString()).getKeys(false));
      return;
    }
    if ((has(args, 0, "end")) && (arenaActive != null) && (auth(p, "arena.admin"))) { arenaActive.eventEnd(true); return; }
    if ((has(args, 0, "delete")) && (auth(p, "arena.admin"))) {
      if (eventActive != null) { p.sendMessage("Can't delete an arena while an event is running. (" + eventActive + ")"); return; }
      if (!config("arena").getConfigurationSection("arena").getKeys(false).contains(args[1])) { p.sendMessage("No arena by that name."); return; }
      config("arena").set("arena." + args[1], null); saveConfig("arena"); p.sendMessage("Arena '" + args[1] + "' has been deleted."); return;
    }
    if ((has(args, 0, "start")) && (auth(p, "arena.admin"))) {
      if (eventActive != null) { p.sendMessage("Can't start an arena event while another event is running. (" + eventActive + ")"); return; }
      if (args.length >= 3) arenaBegin(args[1], Integer.parseInt(args[2])); else if (args.length == 2) arenaBegin(args[1], -1); else arenaBegin("", -1); p.sendMessage("Arena match initiated.");
    }
    if ((has(args, 0, "setup")) && (auth(p, "arena.admin"))) {
      if (eventActive != null) { p.sendMessage("Can't setup an arena while an event is running. (" + eventActive + ")"); return; }
      eventActive = "arenasetup"; arenaActive = new Arena(this, args[1], p); return;
    }
    if ((args.length == 0) && (arenaActive != null) && (arenaActive.state.equals("signup"))) { arenaActive.playerSignup(p); return; }
    if ((arenaActive != null) && ((args.length == 0) || (has(args, 0, "info")))) { arenaActive.info(p); return; }
    if ((arenaActive == null) && ((args.length == 0) || (has(args, 0, "info")))) { p.sendMessage(colorize(lang("arena_signupinfo") + (getConfig().getInt("arena_minutesbetweengames") - (new Date().getTime() - arenaTime) / 60000L) + " minutes")); return; } 
  }

  private int arenaStart() { return getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      public void run() {
        if ((eventActive == null) && (arenaActive == null) && ((new Date().getTime() - arenaTime) / 1000L >= 60 * getConfig().getInt("arena_minutesbetweengames"))) {
          if (getServer().getOnlinePlayers().length >= getConfig().getInt("arena_minimumplayers")) { Vitals.this.arenaBegin("", -1); return; }
          broadcastEvent("&7[Arena] Not enough players online to start an arena match.");
          broadcastEvent("&7The next arena signup period will begin in " + getConfig().getInt("arena_minutesbetweengames") + " minutes.");
          arenaTime = new Date().getTime();
        }
        if ((arenaActive != null) && (arenaActive.state.equals("end"))) { eventActive = null; arenaActive = null; arenaTime = new Date().getTime();
        }
      }
    }
    , 60L, 60L); }

  private void arenaBegin(String arenaType, int arenaNumber) {
    eventActive = "arena"; arenaActive = new Arena(this, arenaType, arenaNumber);
    if ((arenaActive.event.equals("HungerGames")) && (enabled("arena_hungergamesregionrestore")) && (new File(getDataFolder(), "regionrestore_hungergames1.yml").exists())) { regionrestore(null, "hungergames1"); debug("[arena] hungergames1 region was restored"); }
  }

  private void auction(final Player p, String[] args) {
    double auctionFee = getConfig().getDouble("auctions_fee");
    long auctionLastDiff = (new Date().getTime() - auctionLast) / 1000L;
    long auctionDelay = getConfig().getLong("auctions_timebetweenauctions");
    if (auctionItem != null) { p.sendMessage(colorize(lang("auction_failactive"))); return; }
    if (eventActive != null) { p.sendMessage(colorize(lang("auction_failevent") + " (" + eventActive + ")")); return; }
    if (auctionLastDiff < auctionDelay) { p.sendMessage(colorize(lang("auction_faildelay") + (auctionDelay - auctionLastDiff) + " seconds.")); return; }
    if (!econ.has(p.getName(), auctionFee)) { p.sendMessage(colorize(lang("auction_failmoney") + auctionFee)); return; }
    if ((auctionStarter == p) && (!p.hasPermission("v.auction.skipqueue"))) { p.sendMessage(colorize(lang("auction_failqueue"))); return; }
    ItemStack item = p.getItemInHand();
    if ((item == null) || (item.getType() == Material.AIR)) { p.sendMessage(colorize(lang("auction_failitem"))); return; }
    String desc = item.getAmount() + " " + item.getType().toString().toLowerCase().replaceAll("_", " ");
    if ((item.getType().getMaxDurability() > 0) && (item.getDurability() > 0)) desc = desc + " (" + Math.round(100.0D * item.getDurability() / item.getType().getMaxDurability()) + "% damaged)";
    Map enchants = item.getEnchantments();
    Enchantment e;
    for (Iterator localIterator = enchants.keySet().iterator(); localIterator.hasNext(); desc = desc + " (" + e.getName().toLowerCase().replaceAll("_", " ") + " " + enchants.get(e) + ")") e = (Enchantment)localIterator.next();
    if ((args.length == 1) && (p.hasPermission("v.auction.startingbid"))) {
      try { auctionBid = Integer.parseInt(args[0]); } catch (Exception e) {
        p.sendMessage(lang("auction_failstartingbid")); return;
      }if (auctionBid < 0) { p.sendMessage(lang("auction_failstartingbid")); auctionBid = 0; return; }
    }
    eventActive = "auction"; final String itemDescription = desc; auctionItem = item; auctionStarter = p; p.setItemInHand(null); econ.withdrawPlayer(p.getName(), auctionFee);
    p.sendMessage(colorize(lang("auction_success") + " ($" + auctionFee + ")"));
    tasks.put("auction", Integer.valueOf(getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      int timeLeft = getConfig().getInt("auctions_duration"); int lastBid = auctionBid;

      public void run() { if ((Vitals.this.enabled("auctions_preventsniping")) && (lastBid != auctionBid)) { lastBid = auctionBid; if (timeLeft < 45) timeLeft = 45;  }

        if ((timeLeft > 0) && (timeLeft % getConfig().getInt("auctions_announceinterval") == 0)) {
          broadcastEvent("&b[Auction]&7 " + p.getName() + " is auctioning &b" + itemDescription);
          broadcastEvent("&7High bid: &a$" + auctionBid + " &7Time left: &a" + timeLeft + "s &7To bid type: &a/bid [amount]");
        }
        if (timeLeft-- <= 0) Vitals.this.auctionfinish();
      }
    }
    , 20L, 20L)));
  }

  private void auctionbid(Player p, String bid) {
    if (auctionItem == null) { p.sendMessage(colorize(lang("auctionbid_failactive"))); return; }
    if ((auctionStarter == p) && (!p.isOp())) { p.sendMessage(colorize(lang("auctionbid_failstarter"))); return; }
    if (auctionBidder == p) { p.sendMessage(colorize(lang("auctionbid_failbidder"))); return; }
    int bidAmount = 0;
    try { bidAmount = Integer.parseInt(bid); } catch (Exception e) { p.sendMessage(colorize(lang("auctionbid_failnumber"))); return; }
    if (bidAmount <= auctionBid) { p.sendMessage(colorize(lang("auctionbid_failtoolow") + auctionBid)); return; }
    if (!econ.has(p.getName(), bidAmount)) { p.sendMessage(colorize(lang("auctionbid_failmoney"))); return; }
    if (auctionBidder != null) { econ.depositPlayer(auctionBidder.getName(), auctionBid); auctionBidder.sendMessage(colorize(lang("auctionbid_outbid") + bid)); }
    econ.withdrawPlayer(p.getName(), bidAmount); auctionBid = bidAmount; auctionBidder = p; p.sendMessage(colorize(lang("auctionbid_success")));
  }

  private void auctionfinish() {
    final Player winner = auctionBidder; final ItemStack item = auctionItem;
    getServer().getScheduler().cancelTask(((Integer)tasks.get("auction")).intValue()); tasks.remove("auction"); auctionLast = new Date().getTime();
    if (auctionBidder == null) {
      broadcastEvent(lang("auction_nobids"));
      auctionStarter.getInventory().addItem(new ItemStack[] { auctionItem }); auctionStarter.sendMessage(colorize("&cYou received " + auctionItem.toString()));
    } else {
      broadcastEvent(lang("auction_sold") + auctionBid + " by " + auctionBidder.getName() + "!");
      econ.depositPlayer(auctionStarter.getName(), auctionBid); auctionStarter.sendMessage(colorize("&cYou received $" + auctionBid));
      if (auctionBidder.getInventory().firstEmpty() != -1) {
        auctionBidder.getInventory().addItem(new ItemStack[] { auctionItem }); auctionBidder.sendMessage(colorize("&cYou received " + auctionItem.toString()));
      } else {
        auctionBidder.sendMessage(colorize(lang("auction_nospace")));
        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
          public void run() {
            if (winner.getInventory().firstEmpty() != -1) { winner.getInventory().addItem(new ItemStack[] { item }); winner.sendMessage(Vitals.colorize("&cYou received " + item.toString())); } else {
              winner.sendMessage(Vitals.colorize(Vitals.this.lang("auction_itemlost")));
            }
          }
        }
        , 1200L);
      }
    }

    String shortDesc = auctionItem.getType().toString().toLowerCase().replaceAll("_", "") + "(" + auctionItem.getAmount() + ")";
    logEvent("Auction", auctionStarter.getName() + " auctioned " + shortDesc + " to " + (auctionBidder == null ? "no one" : auctionBidder.getName()) + " for $" + auctionBid);
    auctionItem = null; auctionBid = 0; auctionBidder = null; eventActive = null;
  }

  private int authorStart() {
    return getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      public void run() {
        for (Player p : getServer().getOnlinePlayers()) if ((!p.hasPermission("v.admin")) && (!p.isOp())) p.sendMessage("This server uses the Vitals plugin, made by the minecraft server 6d7.com");
      }
    }
    , 144000L, 144000L);
  }

  private void betterhelpCmd(Player p, String[] args) {
    List basicCommands = Arrays.asList(getConfig().getString("betterhelp_basiccommands").split(","));
    List advancedCommands = Arrays.asList(getConfig().getString("betterhelp_advancedcommands").split(","));
    Iterator localIterator1;
    if (args.length == 0) {
      p.sendMessage(colorize("&c---- Help: Basic Commands ----"));
      String s = "";
      String cmd;
      for (localIterator1 = basicCommands.iterator(); localIterator1.hasNext(); s = s + (s.length() > 0 ? "&f, " : "") + "&6/" + cmd) cmd = (String)localIterator1.next(); p.sendMessage(colorize(s));
      p.sendMessage(colorize("&cTo see how to use a command, type: /help [commandname]"));
      p.sendMessage(colorize("&cTo see advanced commands, type: /help advanced"));
    } else if (has(args, 0, "advanced")) {
      p.sendMessage(colorize("&c---- Help: Advanced Commands ----"));
      String s = "";
      String cmd;
      for (localIterator1 = advancedCommands.iterator(); localIterator1.hasNext(); s = s + (s.length() > 0 ? "&f, " : "") + "&6/" + cmd) cmd = (String)localIterator1.next(); p.sendMessage(colorize(s));
    } else if (args.length == 1) {
      String cmd = args[0].toLowerCase();
      if ((basicCommands.contains(cmd)) || (advancedCommands.contains(cmd)))
        betterhelpInfo(p, cmd);
      else if ((cmd.substring(0, 1).equals("/")) && (cmd.length() > 1) && ((basicCommands.contains(cmd.substring(1))) || (advancedCommands.contains(cmd.substring(1)))))
        betterhelpInfo(p, cmd.substring(1));
      else
        p.sendMessage(lang("betterhelp_unknown"));
    }
    else if (((has(args, 0, "addbasic")) || (has(args, 0, "addadvanced")) || (has(args, 0, "remove"))) && (auth(p, "help.admin"))) {
      String cmd = args[1].toLowerCase();
      String b = "";
      String c;
      label587: for (Iterator localIterator2 = basicCommands.iterator(); localIterator2.hasNext(); b = b + (b.length() > 0 ? "," : "") + c) { c = (String)localIterator2.next(); if ((has(args, 0, "remove")) && (c.equals(cmd))) break label587; }
      String a = "";
      String c;
      label690: for (Iterator localIterator3 = advancedCommands.iterator(); localIterator3.hasNext(); a = a + (a.length() > 0 ? "," : "") + c) { c = (String)localIterator3.next(); if ((has(args, 0, "remove")) && (c.equals(cmd))) break label690; }
      if (has(args, 0, "addbasic")) b = b + (b.length() > 0 ? "," : "") + cmd;
      if (has(args, 0, "addadvanced")) a = a + (a.length() > 0 ? "," : "") + cmd;
      getConfig().set("betterhelp_basiccommands", b); getConfig().set("betterhelp_advancedcommands", a); writeConfig();
      p.sendMessage("Changed saved.");
    } else {
      showUsage(p, "help");
    }
  }

  private void betterhelpInfo(Player p, String cmd) { PluginCommand pcmd = getServer().getPluginCommand(cmd);
    if (pcmd == null) {
      if (!showUsage(p, cmd)) p.sendMessage(lang("betterhelp_unknown")); return;
    }
    p.sendMessage(colorize("&e" + cmd + " - " + pcmd.getDescription()));
    p.sendMessage(colorize("&eUsage: " + pcmd.getUsage().replaceAll("<command>", cmd))); }

  private void betternews(Player p, int numToShow)
  {
    if (numToShow < 1) return; p.sendMessage(colorize("&c[LATEST NEWS]")); showLatestFileEntries(p, "news.txt", numToShow);
  }
  private void betternewsCmd(Player p, String[] args) {
    File newsFile = new File(getDataFolder(), "news.txt");
    String entry = new SimpleDateFormat("MM-dd-yyyy").format(new Date()) + ": "; for (int i = 1; i < args.length; i++) entry = entry + args[i] + " ";
    if ((args.length == 0) || (!p.hasPermission("v.news.admin"))) {
      betternews(p, getConfig().getInt("betternews_showoncommand"));
    } else if (args[0].equalsIgnoreCase("add")) {
      if (newsFile.exists()) saveText(loadText(newsFile) + entry + "\n", newsFile); else saveText(entry + "\n", newsFile);
      getServer().broadcastMessage(colorize("&c[NEWS] " + entry)); p.sendMessage("News entry saved and broadcasted.");
    } else if (args[0].equalsIgnoreCase("edit")) {
      if (!newsFile.exists()) { p.sendMessage("There are no news entries."); return; }
      String newNews = ""; String[] news = loadText(newsFile).split("\n"); for (int i = 0; i < news.length - 1; i++) newNews = newNews + news[i] + "\n";
      saveText(newNews + entry + "\n", newsFile); p.sendMessage("Latest news entry changed.");
    } else if (args[0].equalsIgnoreCase("delete")) {
      if (!newsFile.exists()) { p.sendMessage("There are no news entries."); return; }
      String newNews = ""; String[] news = loadText(newsFile).split("\n"); for (int i = 0; i < news.length - 1; i++) newNews = newNews + news[i] + "\n";
      saveText(newNews, newsFile); p.sendMessage("Latest news entry deleted.");
    } else {
      showUsage(p, "news");
    }
  }

  private void bountyCmd(Player p, String[] args) {
    if (args.length == 0) {
      p.sendMessage(colorize("&6[Most Wanted]"));
      Set bounties = config("bounties").getKeys(false);
      if (bounties.size() == 0) { p.sendMessage(lang("bounty_noneactive")); return; }
      HashMap map = new HashMap();
      for (String pName : bounties) if (config("bounties").getLong(pName + ".amount") > 0L) {
          map.put(pName, Double.valueOf(config("bounties").getLong(pName + ".amount")));
        }
      TreeMap sorted_map = mapSort(map);
      int numShown = 0; int numToShow = 9;
      for (String key : sorted_map.keySet()) if (numShown++ < numToShow) {
          p.sendMessage("$" + Math.round(((Double)sorted_map.get(key)).doubleValue()) + " reward for killing " + key);
        }
      return;
    }if ((args.length == 2) && (auth(p, "bounty.buy"))) {
      int amount = 0;
      try { amount = Integer.parseInt(args[1]); } catch (Exception e) {
        p.sendMessage(lang("bounty_failnumber")); return;
      }if (amount < 0) { p.sendMessage(lang("bounty_failnumber")); return; }
      if ((getConfig().getLong("bounties_minimumnewbounty") > 0L) && (amount < getConfig().getLong("bounties_minimumnewbounty"))) { p.sendMessage(lang("bounty_failtoolow") + getConfig().getLong("bounties_minimumnewbounty")); return; }
      if (amount > econ.getBalance(p.getName())) { p.sendMessage("You don't have that much money."); return; }
      econ.withdrawPlayer(p.getName(), amount);
      config("bounties").set(args[0].toLowerCase() + ".amount", Long.valueOf(config("bounties").getLong(args[0].toLowerCase() + ".amount", 0L) + amount));
      saveConfig("bounties"); p.sendMessage(lang("bounty_success"));
      broadcastEvent("&5[Bounty]&d " + p.getName() + lang("bounty_announcenew") + amount + "!");
      broadcastEvent("&5[Bounty]&d $" + config("bounties").getLong(new StringBuilder(String.valueOf(args[0].toLowerCase())).append(".amount").toString()) + lang("bounty_announcetotal") + args[0].toLowerCase() + "!");
      logEvent("Bounty", p.getName() + " purchased a $" + amount + " bounty on " + args[0].toLowerCase());
    } else {
      showUsage(p, "bounty");
    }
  }

  private void bountyDeath(final String k, final String p) { final long bounty = config("bounties").getLong(p + ".amount", 0L);
    if (bounty > 0L) {
      econ.depositPlayer(k, bounty);
      getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
        public void run() { broadcastEvent("&5[Bounty]&d " + k + Vitals.this.lang("bounty_announcekill") + p + "! ($" + bounty + ")"); }

      }
      , 20L);
      config("bounties").set(p, null);
      config("bounties").set(p + ".cooldown", Long.valueOf(new Date().getTime()));
      saveConfig("bounties");
      logEvent("Bounty", k + " was awarded $" + bounty + " for killing " + p); return;
    }
    List kills = config("bounties").getStringList(k + ".kills");
    long cooldown = config("bounties").getLong(k + ".cooldown", 0L);
    long cooldownRemaining = getConfig().getLong("bounties_cooldownminutes") - (new Date().getTime() - cooldown) / 60000L;
    if (cooldownRemaining > 0L) {
      debug("[bounty] " + k + " killed " + p + ": no bounty set for " + k + " because of active cooldown, " + cooldownRemaining + " minutes left");
    } else if ((kills != null) && (kills.contains(p))) {
      debug("[bounty] " + k + " killed " + p + ": no bounty set for " + k + " because the victim was already on the killer's victim list");
    } else {
      debug("[bounty] " + k + " killed " + p + ": no cooldown or cooldown expired on " + k + " so this is a valid bounty");
      config("bounties").set(k + ".amount", Long.valueOf(config("bounties").getLong(k + ".amount", 0L) + getConfig().getLong("bounties_amountperkill")));
      if (kills == null) kills = new ArrayList();
      kills.add(p); config("bounties").set(k + ".kills", kills); saveConfig("bounties");
      getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
        public void run() { broadcastEvent("&5[Bounty]&d New bounty! $" + config("bounties").getLong(new StringBuilder(String.valueOf(k)).append(".amount").toString()) + Vitals.this.lang("bounty_announcetotal") + k + "!"); }

      }
      , 20L);
    }
  }

  private void buyrank(Player p, String rank)
  {
    Iterator localIterator;
    if (rank.length() == 0) {
      p.sendMessage(colorize("&c[Available Ranks]"));
      Double cost;
      String[] rInfo;
      label168: for (localIterator = Arrays.asList(getConfig().getString("econpromotions_ranks").split(";")).iterator(); localIterator.hasNext(); 
        p.sendMessage(rInfo[1] + " - $" + cost))
      {
        String eachrank = (String)localIterator.next();
        try {
          String[] rInfo = eachrank.split(","); cost = Double.valueOf(Double.parseDouble(rInfo[2]));
        }
        catch (Exception e)
        {
          Double cost;
          String err = "Invalid VITALS configuration entry: econpromotions_ranks"; log.severe(err); p.sendMessage(err); return;
        }if ((rInfo[0].length() != 0) && (!perms.getPrimaryGroup(p).equalsIgnoreCase(rInfo[0]))) break label168;
      }
    } else {
      for (String eachrank : Arrays.asList(getConfig().getString("econpromotions_ranks").split(";"))) {
        try {
          String[] rInfo = eachrank.split(","); cost = Double.valueOf(Double.parseDouble(rInfo[2]));
        }
        catch (Exception e)
        {
          Double cost;
          String err = "Invalid VITALS configuration entry: econpromotions_ranks"; log.severe(err); p.sendMessage(err);
          return;
        }
        Double cost;
        String[] rInfo;
        if (rank.equalsIgnoreCase(rInfo[1])) {
          if ((rInfo[0].length() > 0) && (!perms.getPrimaryGroup(p).equalsIgnoreCase(rInfo[0]))) { p.sendMessage(colorize("&fYou have to be rank &a" + rInfo[0] + "&f to buy that promotion.")); return; }
          if (!econ.has(p.getName(), cost.doubleValue())) { p.sendMessage("You don't have enough money (" + cost + ") to buy that promotion."); return; }
          econ.withdrawPlayer(p.getName(), cost.doubleValue()); perms.playerAddGroup(p, rInfo[1]); perms.playerRemoveGroup(p, rInfo[0]);
          p.sendMessage(colorize("&fCongratulations, you purchased the rank &a" + rInfo[1] + "&f!"));
          logEvent("Buyrank", p.getName() + " bought the rank " + rInfo[1] + " for $" + cost);
        }
      }
    }
  }

  private void chunkregen(Player p) {
    Chunk c = p.getLocation().getChunk();
    c.getWorld().regenerateChunk(c.getX(), c.getZ());
    if (getConfig().getBoolean("chunkregen_clearabove")) {
      for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) for (int y = p.getLocation().getBlockY(); y < c.getWorld().getMaxHeight(); y++) {
            c.getBlock(x, y, z).setType(Material.AIR);
          }
    }
    c.getWorld().refreshChunk(c.getX(), c.getZ());
    p.sendMessage("Chunk regenerated.");
  }

  private void countdown(Player p, String[] args) {
    if (countdownTask != -1) { if (has(args, 0, "stop")) { countdownCancel(); getServer().broadcastMessage(colorize("&c[Countdown stopped.]")); } else { p.sendMessage("There is a countdown already running."); } return; }
    try {
      seconds = Integer.parseInt(args[0]);
    }
    catch (Exception e)
    {
      int seconds;
      p.sendMessage("You must specific a positive whole number for the countdown (in seconds).");
      return;
    }
    int seconds;
    if (seconds < 1) { p.sendMessage("You must specific a positive whole number for the countdown (in seconds)."); return; }
    String s = ""; for (int i = 1; i < args.length; i++) s = s + (i > 1 ? " " : "") + args[i];
    int timer = seconds; final String msg = s;
    countdownTask = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      int clock;

      public void run() { if ((clock % 30 == 0) || ((clock < 60) && ((clock % 10 == 0) || (clock < 10)))) {
          String ss = "0" + clock % 60; ss = ss.substring(ss.length() - 2, ss.length());
          getServer().broadcastMessage(Vitals.colorize("&c[" + clock / 60 + ":" + ss + "] &6" + msg));
        }
        clock -= 1;
        if (clock < 0) Vitals.this.countdownCancel();
      }
    }
    , 0L, 20L);
  }
  private void countdownCancel() { getServer().getScheduler().cancelTask(countdownTask); countdownTask = -1; }

  private void customwarpCmd(Player p, String warp, boolean delete) {
    if (warp == null) {
      p.sendMessage(colorize("&c[Custom Warps]"));
      String w;
      for (Iterator localIterator = config("customwarps").getKeys(false).iterator(); localIterator.hasNext(); p.sendMessage(w + ": " + config("customwarps").getString(w))) w = (String)localIterator.next(); 
    }
    else if (delete) {
      if (config("customwarps").get(warp) == null) { p.sendMessage("No custom warp by that name."); return; }
      config("customwarps").set(warp, null); saveConfig("customwarps"); init(); p.sendMessage("Custom warp deleted.");
    } else {
      Location l = p.getLocation(); DecimalFormat df = new DecimalFormat("#.##");
      String x = df.format(l.getX()); String y = df.format(l.getY()); String z = df.format(l.getZ()); String yaw = df.format(l.getYaw()); String pitch = df.format(l.getPitch());
      config("customwarps").set(warp, l.getWorld().getName() + "_" + x + "_" + y + "_" + z + "_" + yaw + "_" + pitch);
      saveConfig("customwarps"); init(); p.sendMessage("Custom warp '" + warp + "' saved.");
    }
  }

  private long damagedItemFullValue(ItemStack item) {
    int type = item.getTypeId();
    String material = ""; long fullValue = 0L;
    if ((type == 268) || (type == 269) || (type == 270) || (type == 271) || (type == 290)) material = "wood";
    if ((type == 272) || (type == 273) || (type == 274) || (type == 275) || (type == 291)) material = "stone";
    if ((type == 298) || (type == 299) || (type == 300) || (type == 301)) material = "leather";
    if ((type == 302) || (type == 303) || (type == 304) || (type == 305)) material = "fire";
    if ((type == 306) || (type == 307) || (type == 308) || (type == 309) || (type == 256) || (type == 257) || (type == 258) || (type == 267) || (type == 292) || (type == 259) || (type == 359)) material = "ironingot";
    if ((type == 310) || (type == 311) || (type == 312) || (type == 313) || (type == 276) || (type == 277) || (type == 278) || (type == 279) || (type == 293)) material = "diamond";
    if ((type == 314) || (type == 315) || (type == 316) || (type == 317) || (type == 283) || (type == 284) || (type == 285) || (type == 286) || (type == 294)) material = "goldingot";
    if ((type == 261) || (type == 346)) material = "string";
    if ((type == 298) || (type == 302) || (type == 306) || (type == 310) || (type == 314)) fullValue = 5L * getConfig().getLong("damageditemsales_" + material + "value");
    if ((type == 299) || (type == 303) || (type == 307) || (type == 311) || (type == 315)) fullValue = 8L * getConfig().getLong("damageditemsales_" + material + "value");
    if ((type == 300) || (type == 304) || (type == 308) || (type == 312) || (type == 316)) fullValue = 7L * getConfig().getLong("damageditemsales_" + material + "value");
    if ((type == 301) || (type == 305) || (type == 309) || (type == 313) || (type == 317)) fullValue = 4L * getConfig().getLong("damageditemsales_" + material + "value");
    if ((type == 258) || (type == 271) || (type == 275) || (type == 279) || (type == 286)) fullValue = 3L * getConfig().getLong("damageditemsales_" + material + "value");
    if ((type == 257) || (type == 270) || (type == 274) || (type == 278) || (type == 285) || (type == 261)) fullValue = 3L * getConfig().getLong("damageditemsales_" + material + "value");
    if ((type == 256) || (type == 269) || (type == 273) || (type == 277) || (type == 284) || (type == 259)) fullValue = getConfig().getLong("damageditemsales_" + material + "value");
    if ((type == 290) || (type == 291) || (type == 292) || (type == 293) || (type == 294) || (type == 359) || (type == 346)) fullValue = 2L * getConfig().getLong("damageditemsales_" + material + "value");
    if ((type == 267) || (type == 268) || (type == 272) || (type == 276) || (type == 283)) fullValue = 2L * getConfig().getLong("damageditemsales_" + material + "value");
    return fullValue;
  }
  private void damagedItemSales(Player p, String action) {
    ItemStack item = p.getItemInHand();
    String itemName = item.getType().toString().toLowerCase().replaceAll("_", "");
    double damagePercent = 1.0D * item.getDurability() / item.getType().getMaxDurability();
    long value = Math.round(damagedItemFullValue(item) * (1.0D - damagePercent));
    if (value <= 0L) { p.sendMessage("Sorry, that item is too badly damaged to sell."); return; }
    if (action.equalsIgnoreCase("worth")) {
      p.sendMessage("That " + itemName + " is " + Math.round(damagePercent * 100.0D) + "% damaged, so it's worth $" + value + ".");
    } else if (action.equalsIgnoreCase("sell")) {
      p.setItemInHand(null); econ.depositPlayer(p.getName(), value); p.sendMessage(colorize("&aSold " + itemName + " for $" + value));
      log.info(p.getName() + " sold " + itemName + " for $$$" + value + " (((1 item at $" + value + " each)))");
    }
  }

  private void dateCmd(Player p, String d) {
    if ((d == null) || (!p.hasPermission("v.date.admin"))) {
      p.sendMessage(colorize("&7The current date/time is " + dateString()));
    } else {
      List monthNames = Arrays.asList(getConfig().getString("worlddate_monthnames").split(","));
      long day = 24000L; long serverFullTime = ((World)getServer().getWorlds().get(0)).getFullTime(); long serverDays = (serverFullTime - serverFullTime % day) / day;
      int mm = 0; int dd = 0; int yyyy = 0;
      try { mm = Integer.parseInt(d.substring(0, 2)); dd = Integer.parseInt(d.substring(3, 5)); yyyy = Integer.parseInt(d.substring(6, 10)); } catch (Exception e) {
        p.sendMessage("To set the date, you must use the format MM/DD/YYYY"); return;
      }while (serverDays-- > 0L) {
        dd--;
        if (dd < 1) { dd = getConfig().getInt("worlddate_dayspermonth"); mm--; }
        if (mm < 1) { mm = monthNames.size(); yyyy--; }
      }
      String mmm = "0" + mm; String ddd = "0" + dd; String serverStartDate = mmm.substring(mmm.length() - 2, mmm.length()) + "/" + ddd.substring(ddd.length() - 2, ddd.length()) + "/" + yyyy;
      getConfig().set("worlddate_startdate", serverStartDate); writeConfig();
      p.sendMessage(colorize("&7The new start date for the server is " + serverStartDate)); dateCmd(p, null);
    }
  }

  private String dateString() { List monthNames = Arrays.asList(getConfig().getString("worlddate_monthnames").split(","));
    long day = 24000L; long serverFullTime = ((World)getServer().getWorlds().get(0)).getFullTime();
    String d = getConfig().getString("worlddate_startdate"); int mm = 0; int dd = 0; int yyyy = 0;
    try { mm = Integer.parseInt(d.substring(0, 2)); dd = Integer.parseInt(d.substring(3, 5)); yyyy = Integer.parseInt(d.substring(6, 10)); } catch (Exception e) {
      log.severe("Invalid date format on config setting worlddate.startdate (should be MM/DD/YYYY)"); return "";
    }long serverDays = (serverFullTime - serverFullTime % day) / day;
    while (serverDays-- > 0L) {
      dd++;
      if (dd > getConfig().getInt("worlddate_dayspermonth")) { dd = 1; mm++; }
      if (mm > monthNames.size()) { mm = 1; yyyy++; }
    }
    long gameTime = serverFullTime % day; long hours = gameTime / 1000L + 6L; long minutes = gameTime % 1000L * 60L / 1000L; String ampm = "AM";
    if (hours >= 24L) dd++; if (dd > getConfig().getInt("worlddate_dayspermonth")) { dd = 1; mm++; } if (mm > monthNames.size()) { mm = 1; yyyy++; }
    if (hours >= 12L) { hours -= 12L; ampm = "PM"; } if (hours >= 12L) { hours -= 12L; ampm = "AM"; } if (hours == 0L) hours = 12L;
    String min = "0" + minutes; min = min.substring(min.length() - 2, min.length());
    return hours + ":" + min + " " + ampm + " on " + (String)monthNames.get(mm - 1) + " " + dd + ", " + yyyy; }

  private int dateStart() {
    return getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      public void run() {
        String dt = Vitals.this.dateString(); String announce = null;
        if ((dt.contains("6:00 AM")) || (dt.contains("6:01 AM")) || (dt.contains("6:02 AM"))) announce = "A new day has arrived!";
        if ((dt.contains("6:30 PM")) || (dt.contains("6:31 PM")) || (dt.contains("6:32 PM"))) announce = "Darkness begins to fall...";
        if ((dt.contains("8:00 PM")) || (dt.contains("8:01 PM")) || (dt.contains("8:02 PM"))) announce = "Darkness has fallen.";
        if (announce != null) getServer().broadcastMessage(Vitals.colorize("&3" + announce + " &7It is now " + dt));
      }
    }
    , 20L, 50L);
  }

  private void deathretention(PlayerDeathEvent event, final Player p) {
    List inv = new ArrayList(); List armor = new ArrayList();
    int totalitems = 0; int totalcost = 0; int costperitem = getConfig().getInt("gameplay_deathretention_costperitem"); if (costperitem < 0) costperitem = 0;
    boolean saveAll = p.hasPermission("v.deathretention.allitems"); boolean saveArmor = p.hasPermission("v.deathretention.armor"); boolean saveWeapons = p.hasPermission("v.deathretention.weapons"); boolean saveTools = p.hasPermission("v.deathretention.tools"); boolean saveExp = p.hasPermission("v.deathretention.experience");
    String saved = "";
    if ((arenaActive == null) || (!arenaActive.playerAlive(p)) || (!getConfig().getBoolean("gameplay_deathretention_disableduringarenas"))) {
      for (ItemStack i : p.getInventory().getArmorContents())
        if ((i != null) && 
          ((saveAll) || (saveArmor)) && (
          (costperitem == 0) || (econ.has(p.getName(), costperitem)))) {
          armor.add(i); event.getDrops().remove(i);
          if (i.getType() != Material.AIR) totalitems++;
          if ((i.getType() != Material.AIR) && (costperitem > 0)) { econ.withdrawPlayer(p.getName(), costperitem); totalcost += costperitem;
          }
        }
      for (ItemStack i : p.getInventory().getContents())
        if (i != null) {
          boolean keep = (saveAll) || ((saveArmor) && (isArmor(i.getType()))) || ((saveWeapons) && (isWeapon(i.getType()))) || ((saveTools) && (isTool(i.getType())));
          if ((keep) && ((costperitem == 0) || (econ.has(p.getName(), costperitem)))) {
            inv.add(i); event.getDrops().remove(i); totalitems++; if (costperitem > 0) { econ.withdrawPlayer(p.getName(), costperitem); totalcost += costperitem; }
          }
        }
      if (saveAll) { saved = "all items";
      } else {
        if (saveWeapons) saved = saved + (saved.length() > 0 ? "/" : "") + "weapons";
        if (saveArmor) saved = saved + (saved.length() > 0 ? "/" : "") + "armor";
        if (saveTools) saved = saved + (saved.length() > 0 ? "/" : "") + "tools";
      }
    }
    if (saveExp) { event.setDroppedExp(0); event.setKeepLevel(true); }
    if (saveExp) saved = saved + (saved.length() > 0 ? "/" : "") + "experience";
    if ((saveExp) || (inv.size() > 0) || (armor.size() > 0)) {
      saved = "Your force of will allowed you to retain " + saved + " even through death";
      saved = saved + (costperitem > 0 ? ", at a cost of $" + costperitem + " per item." : ".");
      saved = saved + " You retained " + totalitems + " items";
      saved = saved + (costperitem > 0 ? " (total cost $" + totalcost + ")." : ".");
      saved = saved + " Any other items have dropped to the ground where you died.";
      final String msg = saved;
      getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() { public void run() { p.sendMessage(msg); }

      }
      , 20L);
      if (inv.size() > 0) deathretentionInventory.put(p.getName(), inv);
      if (armor.size() > 0) deathretentionArmor.put(p.getName(), armor); 
    }
  }

  private void findmyplotCmd(Player p, String[] args)
  {
    int plotNumber = 1; String playerName = p.getName();
    if (args.length >= 1) {
      if (!auth(p, "findmyplot.others")) { showUsage(p, "findmyplot"); return; }
      playerName = args[0];
      if (args.length >= 2) try {
          plotNumber = Integer.parseInt(args[1]); } catch (Exception e) {
          p.sendMessage("Plot number must be a positive integer."); return;
        }
    }
    File residentFile = new File(getServer().getPluginManager().getPlugin("Towny").getDataFolder() + File.separator + "data" + File.separator + "residents" + File.separator + playerName + ".txt");
    if (!residentFile.exists()) { p.sendMessage("Player " + playerName + " is not registered with Towny."); return; }
    String[] residentdata = loadText(residentFile).split("\n");
    for (String line : residentdata)
      if (line.contains("townBlocks=")) {
        String[] plotdata = line.split("=")[1].split("\\|")[0].split(":")[1].split(";");
        if (plotdata.length == 0) {
          if (playerName != p.getName()) { p.sendMessage("That player doesn't own any plots."); return; }
          p.sendMessage("You don't own any plots.");
          if ((enabled("townymods_findplot")) && (p.hasPermission("v.findplot"))) p.sendMessage(colorize("Find a plot for sale by typing &a/findplot")); 
        }
        else { p.sendMessage("Teleporting you to plot " + plotNumber + " out of " + plotdata.length + " plots owned...");
          String world = line.split("=")[1].split("\\|")[0].split(":")[0];
          String[] plotinfo = plotdata[(plotNumber - 1)].split("]")[1].split(",");
          int x = Integer.parseInt(plotinfo[0]); int z = Integer.parseInt(plotinfo[1]);
          int plotY = getHighestFreeBlockAt(getServer().getWorld(world), x * 16 + 8, z * 16 + 8);
          Location plotlocation = new Location(getServer().getWorld(world), x * 16 + 8, plotY + 1, z * 16 + 8);
          playerTeleport(p, plotlocation, null); }
      }
  }

  private void findplot(final Player p)
  {
    File townsFolder = new File(getServer().getPluginManager().getPlugin("Towny").getDataFolder() + File.separator + "data" + File.separator + "towns");
    String minworld = ""; int minx = 0; int minz = 0; Double minprice = Double.valueOf(1.7976931348623157E+308D);
    for (File file : townsFolder.listFiles()) {
      if (file.isFile()) {
        String[] towndata = loadText(file).split("\n");
        for (String line : towndata) {
          if (line.contains("townBlocks=")) {
            String[] worldplots = line.split("=")[1].split("\\|");
            for (String worldplot : worldplots) {
              String world = worldplot.split(":")[0];
              String[] plotdata = worldplot.split(":")[1].split(";");
              debug("[findplot] searching " + plotdata.length + " plots in world [" + world + "]...");
              int fsplots = 0; int nfsplots = 0;
              for (String plot : plotdata) {
                String[] plotinfo = plot.split("]")[1].split(",");
                int x = Integer.parseInt(plotinfo[0]); int z = Integer.parseInt(plotinfo[1]); Double price = Double.valueOf(Double.parseDouble(plotinfo[2]));
                if ((price != null) && (price.doubleValue() != -1.0D)) fsplots++; else nfsplots++;
                if ((price != null) && (price.doubleValue() != -1.0D) && (price.doubleValue() < minprice.doubleValue())) { minworld = world; minx = x; minz = z; minprice = price; }
              }
              debug("[findplot] fsplots " + fsplots + " nfsplots " + nfsplots);
            }
          }
        }
      }
    }
    if (minprice.doubleValue() == 1.7976931348623157E+308D) { p.sendMessage("No plots for sale could be found."); return; }
    p.sendMessage("Plot at [" + minworld + "," + minx + "," + minz + "] is for sale for $" + minprice);
    if (minworld.length() == 0) { p.sendMessage("But the world doesn't seem to exist!"); return; }
    int plotY = getHighestFreeBlockAt(getServer().getWorld(minworld), minx * 16 + 8, minz * 16 + 8);
    Location plotlocation = new Location(getServer().getWorld(minworld), minx * 16 + 8, plotY + 1, minz * 16 + 8);
    final double plotprice = minprice.doubleValue();
    playerTeleport(p, plotlocation, new Runnable() {
      public void run() {
        if (Vitals.econ.has(p.getName(), plotprice)) p.sendMessage(Vitals.colorize("If you want to buy this plot, type &a/plot claim")); else
          p.sendMessage("Unfortunately, you don't have enough money to buy this plot right now.");
      } } );
  }

  private Double findplotprice(Location location) {
    File townsFolder = new File(getServer().getPluginManager().getPlugin("Towny").getDataFolder() + File.separator + "data" + File.separator + "towns");
    if (!townsFolder.exists()) { debug("[townyprice] someone did /plot claim but towny doesn't appear to be installed, so ignoring it"); return Double.valueOf(0.0D); }
    debug("[townyprice] someone did /plot claim at location [" + location.getWorld().getName() + "," + location.getX() + "," + location.getZ() + "], so checking the price");
    for (File file : townsFolder.listFiles()) {
      if (file.isFile()) {
        String[] towndata = loadText(file).split("\n");
        for (String line : towndata)
          if (line.contains("townBlocks=")) {
            String[] worldplots = line.split("=")[1].split("\\|");
            for (String worldplot : worldplots) {
              String world = worldplot.split(":")[0];
              String[] plotdata = worldplot.split(":")[1].split(";");
              debug("[townyprice] searching " + plotdata.length + " plots in world [" + world + "]...");
              for (String plot : plotdata) {
                String[] plotinfo = plot.split("]")[1].split(",");
                int x = Integer.parseInt(plotinfo[0]); int z = Integer.parseInt(plotinfo[1]); Double price = Double.valueOf(Double.parseDouble(plotinfo[2]));
                Double minx = Double.valueOf(x * 16.0D); Double maxx = Double.valueOf(x * 16.0D + 16.0D); Double minz = Double.valueOf(z * 16.0D); Double maxz = Double.valueOf(z * 16.0D + 16.0D);
                if ((location.getX() >= minx.doubleValue()) && (location.getX() < maxx.doubleValue()) && (location.getZ() >= minz.doubleValue()) && (location.getZ() < maxz.doubleValue())) { debug("[townyprice] price is " + price); return price;
                }
              }
            }
          }
      }
    }
    debug("[townyprice] price not found"); return Double.valueOf(-1.0D);
  }

  private void gmall(Player p, String param) {
    List modes = Arrays.asList(new String[] { "survival", "creative", "adventure" });
    if ((param != "") && (!modes.contains(param.toLowerCase()))) {
      Player player = getServer().getPlayer(param);
      if (player == null) { p.sendMessage("Player '" + param + "' is not online."); return; }
      p.sendMessage(param + " is " + (player.isFlying() ? "" : "not ") + "flying. " + param + " is " + (player.isOp() ? "" : "not ") + " op.");
      if (player.getGameMode().equals(GameMode.CREATIVE)) p.sendMessage(param + " is in creative mode.");
      else if (player.getGameMode().equals(GameMode.ADVENTURE)) p.sendMessage(param + " is in adventure mode.");
      else if (player.getGameMode().equals(GameMode.SURVIVAL)) p.sendMessage(param + " is in survival mode."); 
    }
    else if ((modes.contains(param.toLowerCase())) && (p.hasPermission("v.gmall.admin"))) {
      GameMode gm = param.equalsIgnoreCase("adventure") ? GameMode.ADVENTURE : param.equalsIgnoreCase("creative") ? GameMode.CREATIVE : GameMode.SURVIVAL;
      int numPlayers = 0; for (Player player : getServer().getOnlinePlayers()) { player.setGameMode(gm); numPlayers++; }
      p.sendMessage(numPlayers + " players changed to " + gm.toString() + " mode.");
    } else {
      String[] tags = { "in survival mode", "in adventure mode", "who are flying", "in creative mode", "who are op" };
      for (int i = 0; i <= 4; i++) {
        String msg = ""; int numPlayers = 0;
        for (Player player : getServer().getOnlinePlayers()) if (gmallTest(player, i)) { numPlayers++; msg = msg + " " + player.getName(); }
        p.sendMessage("Players " + tags[i] + " (" + numPlayers + "):" + msg);
      }
    }
  }

  private boolean gmallTest(Player p, int which) { switch (which) { case 0:
      return p.getGameMode().equals(GameMode.SURVIVAL);
    case 1:
      return p.getGameMode().equals(GameMode.ADVENTURE);
    case 2:
      return p.isFlying();
    case 3:
      return p.getGameMode().equals(GameMode.CREATIVE);
    case 4:
      return p.isOp(); }
    return false;
  }

  private boolean helperbot(Player p, String[] args)
  {
    if (has(args, 0, "list")) {
      p.sendMessage(colorize("&c[HelperBot Config]"));
      String wordpair;
      for (Iterator localIterator = config("helperbot").getKeys(false).iterator(); localIterator.hasNext(); p.sendMessage(wordpair.replaceAll("_", ",") + ": " + config("helperbot").get(wordpair))) wordpair = (String)localIterator.next(); return true;
    }
    if (args.length < 3) return false;
    String word1 = args[0]; String word2 = args[1]; String response = "";
    for (int i = 2; i < args.length; i++) response = response + (response.equals("") ? "" : " ") + args[i];
    config("helperbot").set(word1 + "_" + word2, response.equalsIgnoreCase("delete") ? null : response);
    saveConfig("helperbot"); p.sendMessage("HelperBot entry saved."); return true;
  }

  private void modvote(Player p, String vote)
  {
    TreeMap sorted_map;
    if ((vote.equalsIgnoreCase("top")) && (p.hasPermission("v.modvote.top"))) {
      HashMap map = new HashMap();
      for (String key : config("users").getKeys(false)) {
        if ((config("users").getString(key + ".modvote") != null) && (!config("users").getString(key + ".modvote").equals("none"))) {
          if (!map.containsKey(config("users").getString(key + ".modvote"))) map.put(config("users").getString(key + ".modvote"), Double.valueOf(0.0D));
          map.put(config("users").getString(key + ".modvote"), Double.valueOf(((Double)map.get(config("users").getString(key + ".modvote"))).doubleValue() + 1.0D));
        }
      }
      sorted_map = mapSort(map);
      p.sendMessage(colorize("&c[Top 9 Players Voted to be Mod]"));
      int numShown = 0; int numToShow = 9;
      for (String key : sorted_map.keySet()) if (numShown++ < numToShow)
          if (((Double)sorted_map.get(key)).equals(Double.valueOf(1.0D))) p.sendMessage(key + ": 1 vote"); else
            p.sendMessage(key + ": " + Math.round(((Double)sorted_map.get(key)).doubleValue()) + " votes");
    }
    else if ((vote.equalsIgnoreCase("reset")) && (p.hasPermission("v.modvote.reset")))
    {
      String key;
      for (sorted_map = config("users").getKeys(false).iterator(); sorted_map.hasNext(); config("users").set(key + ".modvote", null)) key = (String)sorted_map.next();
      saveConfig("users"); p.sendMessage("All votes for moderator have been reset.");
    } else if (vote == "") {
      if ((config("users").getString(p.getName() + ".modvote") != null) && (!config("users").getString(p.getName() + ".modvote").equals("none"))) {
        p.sendMessage(colorize("You are currently voting for: &b" + config("users").getString(new StringBuilder(String.valueOf(p.getName())).append(".modvote").toString())));
        p.sendMessage("Type /modvote none if you wish to cancel your vote.");
      } else {
        p.sendMessage("You are not currently voting for a mod. Type /modvote [name] to vote for someone!");
      } } else if (perms.playerHas(null, vote, "v.modvote.mod")) {
      p.sendMessage("You can't vote for that person because they are already have moderator or staff permissions.");
    } else {
      config("users").set(p.getName() + ".modvote", vote.toLowerCase()); saveConfig("users");
      p.sendMessage(colorize("&aYou are now voting for: &b" + vote.toLowerCase()));
      p.sendMessage("Make sure to spell their name exactly (capitals don't matter) for your vote to be counted!");
      p.sendMessage("Players have no way of finding out who voted for them, so if they promised you something in return for a vote they are lying!");
      p.sendMessage("Type /modvote none if you wish to cancel your vote.");
    }
  }

  private void onlinePlayers(Player playerToExclude) {
    File outputFile = new File(getDataFolder(), "onlineplayers.txt");
    String text = ""; for (Player player : getServer().getOnlinePlayers()) if (player != playerToExclude) text = text + player.getName() + "\n";
    saveText(text, outputFile);
  }

  private void playerPassword(Player p, String password) {
    if ((password.length() < 5) || (password.length() > 50)) { p.sendMessage("Your password must be between 5 and 50 characters in length."); return; }
    config("playerpasswords").set(p.getName(), password);
    if (saveConfig("playerpasswords")) p.sendMessage("Your password has been set."); else
      p.sendMessage("An error occurred while trying to save your password. Please try again later.");
  }

  private int playtimeStart() {
    return getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() { private Player[] onlinePlayersLast = new Player[0];
      private long saveInterval = 600L; private long saveIntervalTimer = 0L; private long oneDay = 86400000L;
      List<Integer> playtimeHistory;

      public void run() { List upRanks = Arrays.asList(getConfig().getString("playtime_promotions").split(";"));
        Player[] onlinePlayers = getServer().getOnlinePlayers();
        for (Player player : onlinePlayers) {
          String pName = player.getName().toLowerCase(); boolean addSecond = false; for (Player pp : onlinePlayersLast) if (pp == player) addSecond = true;
          if (addSecond) {
            long newPlaytime = config("playtime").getLong(pName + ".playtime", 0L) + 1L;
            config("playtime").set(pName + ".playtime", Long.valueOf(newPlaytime));
            if (newPlaytime % 60L == 0L) {
              for (??? = upRanks.iterator(); ((Iterator)???).hasNext(); ) { String uprank = (String)((Iterator)???).next();
                try {
                  String[] rInfo = uprank.split(","); promoteMinutes = Integer.parseInt(rInfo[2]);
                }
                catch (Exception e)
                {
                  long promoteMinutes;
                  Vitals.log.severe("Invalid VITALS configuration entry: playtime_promotions");
                  return;
                }
                long promoteMinutes;
                String[] rInfo;
                if ((newPlaytime >= promoteMinutes * 60L) && ((Vitals.perms.getPrimaryGroup(player).equalsIgnoreCase(rInfo[0])) || (rInfo[0] == "any"))) {
                  Vitals.perms.playerAddGroup(player, rInfo[1]);
                  Vitals.perms.playerRemoveGroup(player, rInfo[0]);
                }
              }
            }
            if (newPlaytime % 3600L == 0L)
            {
              String key;
              for (??? = config("playtime").getKeys(false).iterator(); ((Iterator)???).hasNext(); Vitals.this.playtimeAverageShift(key)) key = (String)((Iterator)???).next(); 
            }
            Vitals.this.playtimeAverageShift(pName);
            playtimeHistory = config("playtime").getIntegerList(pName + ".playtimehistory");
            playtimeHistory.set(playtimeHistory.size() - 1, Integer.valueOf(((Integer)playtimeHistory.get(playtimeHistory.size() - 1)).intValue() + 1));
            config("playtime").set(pName + ".playtimehistory", playtimeHistory);
          }
        }
        if (++saveIntervalTimer % saveInterval == 0L) {
          debug("[playtime] saving playtime of " + config("playtime").getKeys(false).size() + " players to disk");
          if (getConfig().getBoolean("playtime_autocleanup")) {
            int numPurged = 0;
            String pName;
            label808: for (Iterator localIterator = config("playtime").getKeys(false).iterator(); localIterator.hasNext(); 
              config("playtime").set(pName, null))
            {
              pName = (String)localIterator.next();
              playtimeHistory = config("playtime").getIntegerList(pName + ".playtimehistory");
              boolean onlineThisWeek = false;
              for (int d = 1; d <= 7; d++) if (((Integer)playtimeHistory.get(playtimeHistory.size() - d)).intValue() != 0) onlineThisWeek = true;
              if ((onlineThisWeek) || (config("playtime").getLong(pName + ".playtime") * 1000L >= oneDay)) break label808;
              numPurged++;
            }

            debug("[playtime] purged " + numPurged + " players that haven't been online for a week and have playtime less than a day");
          }
          saveConfig("playtime");
        }
        onlinePlayersLast = onlinePlayers;
      }
    }
    , 3L, 20L);
  }
  private void playtimeAverageShift(String playername) {
    long oneDay = 86400000L; long thirtyDays = 2592000000L;
    List playtimeHistory = config("playtime").getIntegerList(playername + ".playtimehistory");
    while (playtimeHistory.size() < 30) playtimeHistory.add(0, Integer.valueOf(0));
    long todayStartedAt = config("playtime").getLong(playername + ".playtimehistorymarker");
    if (new Date().getTime() - todayStartedAt > thirtyDays) {
      config("playtime").set(playername + ".playtimehistorymarker", Long.valueOf(new Date().getTime()));
      for (i = 0; i < 30; i++) playtimeHistory.set(i, Integer.valueOf(0));
      config("playtime").set(playername + ".playtimehistory", playtimeHistory);
      return;
    }
    while (new Date().getTime() - todayStartedAt > oneDay)
    {
      int i;
      todayStartedAt += oneDay;
      config("playtime").set(playername + ".playtimehistorymarker", Long.valueOf(todayStartedAt));
      playtimeHistory.remove(0);
      playtimeHistory.add(Integer.valueOf(0));
    }
    config("playtime").set(playername + ".playtimehistory", playtimeHistory);
    int sum = 0;
    Integer entry;
    for (Iterator localIterator = playtimeHistory.iterator(); localIterator.hasNext(); sum += entry.intValue()) entry = (Integer)localIterator.next();
    config("playtime").set(playername + ".playtimeaverage", Integer.valueOf(sum / playtimeHistory.size()));
  }
  private void playtimeCmd(Player p, String[] args) {
    String playername = args.length > 0 ? args[0] : p.getName();
    if ((args.length == 2) && (auth(p, "playtime.admin"))) {
      try {
        mm = Long.parseLong(args[1]);
      }
      catch (Exception e)
      {
        long mm;
        p.sendMessage("You must specify a positive whole number or zero for the playtime (in minutes).");
        return;
      }
      long mm;
      if (mm < 0L) { p.sendMessage("You must specify a positive whole number or zero for the playtime (in minutes)."); return; }
      config("playtime").set(playername.toLowerCase() + ".playtime", Long.valueOf(mm * 60L)); saveConfig("playtime");
      p.sendMessage(playername.toLowerCase() + "'s total playtime has been set to " + mm + " minutes."); } else {
      if ((playername.equalsIgnoreCase("top")) && (auth(p, "playtime.top")))
      {
        HashMap map = new HashMap();
        String key;
        for (e = config("playtime").getKeys(false).iterator(); e.hasNext(); map.put(key, Double.valueOf(config("playtime").getDouble(key + ".playtime")))) key = (String)e.next();
        TreeMap sorted_map = mapSort(map);
        p.sendMessage(colorize("&c[Top 9 Playtime]"));
        int numShown = 0; int numToShow = 9;
        for (String key : sorted_map.keySet()) if (numShown++ < numToShow) p.sendMessage(key + ": " + Math.round(((Double)sorted_map.get(key)).doubleValue() / 36.0D / 24.0D) / 100.0D + " days");
        if (p.hasPermission("v.playtime.average")) {
          map.clear();
          String pName;
          for (??? = config("playtime").getKeys(false).iterator(); ???.hasNext(); map.put(pName, Double.valueOf(config("playtime").getDouble(pName + ".playtimeaverage")))) { pName = (String)???.next(); playtimeAverageShift(pName); }
          sorted_map = mapSort(map); numShown = 0; numToShow = 9;
          p.sendMessage(colorize("&c[Top 9 Playtime Daily Average - Last 30 Days]"));
          for (String pName : sorted_map.keySet()) if (numShown++ < numToShow) p.sendMessage(pName + ": " + Math.round(100.0D * ((Double)sorted_map.get(pName)).doubleValue() / 60.0D) / 100.0D + " minutes");
        }
        return;
      }if ((playername.equalsIgnoreCase(p.getName())) || (auth(p, "playtime.others")))
      {
        long playtime = config("playtime").getLong(playername.toLowerCase() + ".playtime");
        if (playtime == 0L) { p.sendMessage("No player by that name has recorded playtime on this server."); return; }
        if (playername.equalsIgnoreCase(p.getName())) p.sendMessage("Your total playtime is:"); else
          p.sendMessage("The total playtime of " + playername.toLowerCase() + " is:");
        p.sendMessage(TimeUnit.SECONDS.toDays(playtime) + " days, " + TimeUnit.SECONDS.toHours(playtime) % 24L + " hours, " + 
          TimeUnit.SECONDS.toMinutes(playtime) % 60L + " minutes, " + playtime % 60L + " seconds");
        if (p.hasPermission("v.playtime.average")) {
          playtimeAverageShift(playername.toLowerCase());
          double average = config("playtime").getDouble(playername.toLowerCase() + ".playtimeaverage");
          p.sendMessage("Average time played per day over the last 30 days:");
          p.sendMessage(Math.round(100.0D * average / 60.0D) / 100.0D + " minutes");
        }
      } else {
        showUsage(p, "playtime");
      }
    }
  }

  private void plotsalesign(Player p, String[] args) { String[] forsalePixels = { "2,13", "2,12", "2,11", "2,10", "2,9", "2,8", "3,13", "3,11", "4,13", "6,13", "6,12", "6,11", "6,10", "6,9", "6,8", "7,13", "7,8", "8,13", "8,12", "8,11", "8,10", "8,9", "8,8", "10,13", "10,12", "10,11", "10,10", "10,9", "10,8", "11,13", "11,11", "12,13", "12,11", "12,10", "13,13", "13,12", "13,11", "13,9", "13,8", 
      "1,6", "1,5", "1,4", "1,2", "2,6", "2,4", "2,2", "3,6", "3,4", "3,3", "3,2", "5,6", "5,5", "5,4", "5,3", "5,2", "6,6", "6,4", "7,6", "7,5", "7,4", "7,3", "7,2", "9,6", "9,5", "9,4", "9,3", "9,2", "10,2", "12,6", "12,5", "12,4", "12,3", "12,2", "13,6", "13,4", "13,2", "14,6", "14,4", "14,2" };
    int y = p.getLocation().getBlockY() - 1;
    for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
        int type = Material.GRASS.getId();
        if (args.length >= 1) {
          Material m = Material.getMaterial(args[0].toUpperCase());
          if (m == null) { p.sendMessage("Invalid type, try sand/grass/etc"); return; }
          type = m.getId();
        }
        byte data = 0;
        if ((args.length < 2) && ((x == 0) || (x == 15) || (z == 0) || (z == 15))) { type = Material.WOOL.getId(); data = 11; }
        if (args.length < 2) for (String pixel : forsalePixels) {
            String[] pixxel = pixel.split(",");
            if ((Integer.parseInt(pixxel[0]) == z) && (Integer.parseInt(pixxel[1]) == x)) { type = Material.WOOL.getId(); data = 7; }
          }
        p.getLocation().getChunk().getBlock(x, y, z).setTypeIdAndData(type, data, true);
      } 
  }

  private boolean regionlabel(Player p, String[] args)
  {
    if ((has(args, 1, "delete")) && (!config("regionlabels").contains(args[0]))) { p.sendMessage("No region exists by that name."); return true; }
    if (has(args, 1, "delete")) { config("regionlabels").set(args[0], null); saveConfig("regionlabels"); p.sendMessage("Region label deleted."); return true; }
    if (has(args, 0, "list")) { p.sendMessage(colorize("&c[Labeled Regions]"));
      String region;
      for (Iterator localIterator = config("regionlabels").getKeys(false).iterator(); localIterator.hasNext(); p.sendMessage(region + ": " + config("regionlabels").get(region))) region = (String)localIterator.next(); return true; }
    if (has(args, 1, "exact")) { setupNew(p, "regionlabel", args[0], "exact"); return true; }
    if (has(args, 1, "normal")) { setupNew(p, "regionlabel", args[0], ""); return true; }
    return false;
  }
  private int regionlabelsStart() {
    return getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
      public void run() {
        for (Player p : getServer().getOnlinePlayers()) {
          String lastRegion = (String)regionlabelPlayers.get(p.getName()); String thisRegion = Vitals.this.regionGet("regionlabels", p.getLocation());
          if ((thisRegion != null) && (!thisRegion.equals(lastRegion))) {
            p.sendMessage(Vitals.colorize(Vitals.this.lang("regionlabels_enter").replaceAll("\\{region\\}", thisRegion.replaceAll("_", " "))));
            regionlabelPlayers.put(p.getName(), thisRegion);
          }
        }
      }
    }
    , 4L, 60L);
  }
  private boolean regionprotect(Player p, String[] args) {
    if ((has(args, 1, "delete")) && (!config("regionprotect").contains(args[0]))) { p.sendMessage("No region exists by that name."); return true; }
    if (has(args, 1, "delete")) { config("regionprotect").set(args[0], null); saveConfig("regionprotect"); p.sendMessage("Region protection removed."); return true; }
    if (has(args, 0, "list")) { p.sendMessage(colorize("&c[Protected Regions]"));
      String region;
      for (Iterator localIterator = config("regionprotect").getKeys(false).iterator(); localIterator.hasNext(); p.sendMessage(region + ": " + config("regionprotect").get(region))) region = (String)localIterator.next(); return true; }
    if (has(args, 1, "protect")) { setupNew(p, "regionprotect", args[0], ""); return true; }
    return false;
  }

  private String regionGet(String configName, Location loc)
  {
    String region;
    label223: for (Iterator localIterator = config(configName).getKeys(false).iterator(); localIterator.hasNext(); 
      return region)
    {
      region = (String)localIterator.next();
      String[] cuboid = config(configName).getString(region).split("_");
      String world = cuboid[0]; Double minx = Double.valueOf(Double.parseDouble(cuboid[1])); Double miny = Double.valueOf(Double.parseDouble(cuboid[2])); Double minz = Double.valueOf(Double.parseDouble(cuboid[3])); Double maxx = Double.valueOf(Double.parseDouble(cuboid[4])); Double maxy = Double.valueOf(Double.parseDouble(cuboid[5])); Double maxz = Double.valueOf(Double.parseDouble(cuboid[6]));
      if ((!loc.getWorld().getName().equals(world)) || (loc.getX() < minx.doubleValue()) || (loc.getX() >= maxx.doubleValue()) || (loc.getY() < miny.doubleValue()) || (loc.getY() >= maxy.doubleValue()) || (loc.getZ() < minz.doubleValue()) || (loc.getZ() >= maxz.doubleValue())) break label223;
    }
    return null;
  }
  private boolean regionprotected(Location loc) {
    label211: for (Iterator localIterator = regionprotectRegions.keySet().iterator(); localIterator.hasNext(); 
      return true)
    {
      String region = (String)localIterator.next();
      String[] cuboid = (String[])regionprotectRegions.get(region);
      Double minx = Double.valueOf(Double.parseDouble(cuboid[1])); Double miny = Double.valueOf(Double.parseDouble(cuboid[2])); Double minz = Double.valueOf(Double.parseDouble(cuboid[3])); Double maxx = Double.valueOf(Double.parseDouble(cuboid[4])); Double maxy = Double.valueOf(Double.parseDouble(cuboid[5])); Double maxz = Double.valueOf(Double.parseDouble(cuboid[6]));
      if ((!loc.getWorld().getName().equals(cuboid[0])) || (loc.getX() < minx.doubleValue()) || (loc.getX() >= maxx.doubleValue()) || (loc.getY() < miny.doubleValue()) || (loc.getY() >= maxy.doubleValue()) || (loc.getZ() < minz.doubleValue()) || (loc.getZ() >= maxz.doubleValue())) break label211;
    }
    return false;
  }
  private List<String> regionrestoreFiles() {
    List regions = new ArrayList();
    for (File f : getDataFolder().listFiles()) {
      if ((f.getName().length() >= 14) && (f.getName().substring(0, 14).equals("regionrestore_")) && (f.getName().substring(f.getName().length() - 4, f.getName().length()).equals(".yml"))) {
        regions.add(f.getName().replaceFirst("regionrestore_", "").replaceAll(".yml", ""));
      }
    }
    return regions;
  }
  private void regionrestoreList(Player p) {
    p.sendMessage(colorize("&c[Saved Regions]"));
    for (String region : regionrestoreFiles()) {
      int blockCount = config("regionrestore_" + region).contains(region + ".data") ? config("regionrestore_" + region).getConfigurationSection(region + ".data").getKeys(false).size() : 0;
      p.sendMessage(region + " (" + blockCount + " blocks) (autorestore every " + config(new StringBuilder("regionrestore_").append(region).toString()).getInt(new StringBuilder(String.valueOf(region)).append(".interval").toString()) + " minutes)");
    }
  }

  private boolean regionsave(Player p, String[] args) { if ((has(args, 1, "delete")) || (has(args, 1, "interval"))) {
      File f = new File(getDataFolder(), "regionrestore_" + args[0] + ".yml");
      if (!f.exists()) { p.sendMessage("No region exists by that name."); return true; }
    }
    if (has(args, 1, "delete")) { deleteConfig("regionrestore_" + args[0]); p.sendMessage("Region data deleted."); init(); return true; }
    if ((has(args, 1, "interval")) && (args.length == 3)) { config("regionrestore_" + args[0]).set(args[0] + ".interval", Integer.valueOf(Integer.parseInt(args[2]))); saveConfig("regionrestore_" + args[0]); init(); p.sendMessage("Region '" + args[0] + "' will now automatically restore every " + args[2] + " minutes."); return true; }
    if (has(args, 0, "list")) { regionrestoreList(p); return true; }
    if (has(args, 1, "inventory")) { setupNew(p, "regionrestore", args[0], "inventory"); return true; }
    if (has(args, 1, "all")) { setupNew(p, "regionrestore", args[0], ""); return true; }
    return false; }

  private void regionrestore(Player p, String region) {
    if (region.equalsIgnoreCase("list")) { regionrestoreList(p); return; }
    File f = new File(getDataFolder(), "regionrestore_" + region + ".yml");
    if (!f.exists()) { if (p != null) p.sendMessage("No region exists by that name"); return; }
    if (tasks.containsKey("regionrestorebatch")) p.sendMessage("Canceled restore operation in progress to start new restore...");
    task("regionrestorebatch", Integer.valueOf(regionrestoreBatch(region, p)));
  }
  private int regionsaveBatch(final String region, final Block b1, Block b2, final String option, final Player notify) {
    return getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() { YamlConfiguration cfg;
      Double x1;
      Double y1;
      Double z1;
      Double x2;
      Double y2;
      Double z2;
      Double minx;
      Double miny;
      Double minz;
      Double maxx;
      Double maxy;
      Double maxz;
      World w;
      int blockNum;
      int blocksPer;
      StringBuilder air;
      StringBuilder stone;
      StringBuilder dirt;
      StringBuilder water;
      StringBuilder grass;
      StringBuilder sand;
      StringBuilder gravel;
      StringBuilder bedrock;
      StringBuilder lava;

      public void run() { int i = 0;
        if (blockNum == 0) {
          config("regionrestore_" + region).set(region, null);
          config("regionrestore_" + region).set(region + ".interval", Integer.valueOf(-1));
          config("regionrestore_" + region).set(region + ".world", b1.getWorld().getName());
          config("regionrestore_" + region).set(region + ".minx", minx);
          config("regionrestore_" + region).set(region + ".miny", miny);
          config("regionrestore_" + region).set(region + ".minz", minz);
        }
        for (long x = 0L; x <= maxx.doubleValue() - minx.doubleValue(); x += 1L) for (long y = 0L; y <= maxy.doubleValue() - miny.doubleValue(); y += 1L) for (long z = 0L; z <= maxz.doubleValue() - minz.doubleValue(); z += 1L) {
              Block b = new Location(w, minx.doubleValue() + x, miny.doubleValue() + y, minz.doubleValue() + z).getBlock();
              if ((!option.equals("inventory")) || ((b.getState() instanceof InventoryHolder))) {
                i++; if (i >= blockNum) {
                  if (i >= blockNum + blocksPer) { blockNum = i; if (i % (blocksPer * 5) == 0) notify.sendMessage("saved " + i / 1000 + "k blocks so far..."); return; }
                  if (b.getType() == Material.AIR) { if (air.length() > 0) air.append(","); air.append(x); air.append(","); air.append(y); air.append(","); air.append(z);
                  } else if (b.getType() == Material.STONE) { if (stone.length() > 0) stone.append(","); stone.append(x); stone.append(","); stone.append(y); stone.append(","); stone.append(z);
                  } else if (b.getType() == Material.DIRT) { if (dirt.length() > 0) dirt.append(","); dirt.append(x); dirt.append(","); dirt.append(y); dirt.append(","); dirt.append(z);
                  } else if (b.getType() == Material.STATIONARY_WATER) { if (water.length() > 0) water.append(","); water.append(x); water.append(","); water.append(y); water.append(","); water.append(z);
                  } else if (b.getType() == Material.GRASS) { if (grass.length() > 0) grass.append(","); grass.append(x); grass.append(","); grass.append(y); grass.append(","); grass.append(z);
                  } else if (b.getType() == Material.SAND) { if (sand.length() > 0) sand.append(","); sand.append(x); sand.append(","); sand.append(y); sand.append(","); sand.append(z);
                  } else if (b.getType() == Material.GRAVEL) { if (gravel.length() > 0) gravel.append(","); gravel.append(x); gravel.append(","); gravel.append(y); gravel.append(","); gravel.append(z);
                  } else if (b.getType() == Material.BEDROCK) { if (bedrock.length() > 0) bedrock.append(","); bedrock.append(x); bedrock.append(","); bedrock.append(y); bedrock.append(","); bedrock.append(z);
                  } else if (b.getType() == Material.STATIONARY_LAVA) { if (lava.length() > 0) lava.append(","); lava.append(x); lava.append(","); lava.append(y); lava.append(","); lava.append(z); } else {
                    StringBuilder key = new StringBuilder(); StringBuilder val = new StringBuilder();
                    key.append(region); key.append(".data."); key.append(x); key.append("_"); key.append(y); key.append("_"); key.append(z);
                    val.append(b.getTypeId()); val.append("_"); val.append(b.getData());
                    cfg.set(key.toString(), val.toString());
                    if ((b.getState() instanceof InventoryHolder)) {
                      Inventory inv = ((InventoryHolder)b.getState()).getInventory(); List contents = new ArrayList();
                      for (ItemStack is : inv.getContents()) if (is != null) contents.add(new CardboardBox(is).toString());
                      cfg.set(key + "_contents", contents); notify.sendMessage("saved " + b.getType() + " inventory (" + contents.size() + " itemstacks)");
                    }
                  }
                }
              }
            }  cfg.set(region + ".air", air.toString()); cfg.set(region + ".stone", stone.toString()); cfg.set(region + ".dirt", dirt.toString());
        cfg.set(region + ".water", water.toString()); cfg.set(region + ".grass", grass.toString()); cfg.set(region + ".sand", sand.toString());
        cfg.set(region + ".gravel", gravel.toString()); cfg.set(region + ".bedrock", bedrock.toString()); cfg.set(region + ".lava", lava.toString());
        saveConfig("regionrestore_" + region);
        notify.sendMessage("FINISHED! The region '" + region + "' has been saved. (total " + i + " blocks)"); Vitals.this.task("regionsavebatch", Integer.valueOf(-1));
      }
    }
    , 0L, 4L);
  }
  private int regionrestoreBatch(final String region, final Player notify) {
    return getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() { YamlConfiguration cfg;
      World w;
      int blockNum;
      int n;
      int blocksPer;
      Double minx;
      Double miny;
      Double minz;
      boolean didAir;
      boolean didStone;
      boolean didDirt;
      boolean didWater;
      boolean didGrass;
      boolean didSand;
      boolean didGravel;
      boolean didBedrock;
      boolean didLava;
      String[] air;
      String[] stone;
      String[] dirt;
      String[] water;
      String[] grass;
      String[] sand;
      String[] gravel;
      String[] bedrock;
      String[] lava;

      public void run() { int i = 0; List contents = new ArrayList();
        if ((!didAir) && (air.length > 1)) { didAir = true;
          int j;
          n += (j = Vitals.this.regionrestoreArray(w, air, Material.AIR, minx, miny, minz, notify)); if ((n > blocksPer) && (j > 0)) return; 
        }
        if ((!didStone) && (stone.length > 1)) { didStone = true;
          int j;
          n += (j = Vitals.this.regionrestoreArray(w, stone, Material.STONE, minx, miny, minz, notify)); if ((n > blocksPer) && (j > 0)) return; 
        }
        if ((!didDirt) && (dirt.length > 1)) { didDirt = true;
          int j;
          n += (j = Vitals.this.regionrestoreArray(w, dirt, Material.DIRT, minx, miny, minz, notify)); if ((n > blocksPer) && (j > 0)) return; 
        }
        if ((!didWater) && (water.length > 1)) { didWater = true;
          int j;
          n += (j = Vitals.this.regionrestoreArray(w, water, Material.STATIONARY_WATER, minx, miny, minz, notify)); if ((n > blocksPer) && (j > 0)) return; 
        }
        if ((!didGrass) && (grass.length > 1)) { didGrass = true;
          int j;
          n += (j = Vitals.this.regionrestoreArray(w, grass, Material.GRASS, minx, miny, minz, notify)); if ((n > blocksPer) && (j > 0)) return; 
        }
        if ((!didSand) && (sand.length > 1)) { didSand = true;
          int j;
          n += (j = Vitals.this.regionrestoreArray(w, sand, Material.SAND, minx, miny, minz, notify)); if ((n > blocksPer) && (j > 0)) return; 
        }
        if ((!didGravel) && (gravel.length > 1)) { didGravel = true;
          int j;
          n += (j = Vitals.this.regionrestoreArray(w, gravel, Material.GRAVEL, minx, miny, minz, notify)); if ((n > blocksPer) && (j > 0)) return; 
        }
        if ((!didBedrock) && (bedrock.length > 1)) { didBedrock = true;
          int j;
          n += (j = Vitals.this.regionrestoreArray(w, bedrock, Material.BEDROCK, minx, miny, minz, notify)); if ((n > blocksPer) && (j > 0)) return; 
        }
        if ((!didLava) && (lava.length > 1)) { didLava = true;
          int j;
          n += (j = Vitals.this.regionrestoreArray(w, lava, Material.STATIONARY_LAVA, minx, miny, minz, notify)); if ((n > blocksPer) && (j > 0)) return; 
        }
        for (String pos : cfg.getConfigurationSection(region + ".data").getKeys(false)) if (!pos.contains("contents")) {
            String[] data = cfg.getString(region + ".data." + pos).split("_");
            String[] coords = pos.split("_");
            Integer blockType = Integer.valueOf(Integer.parseInt(data[0])); byte blockData = (byte)Integer.parseInt(data[1]);
            Block b = new Location(w, minx.doubleValue() + Double.parseDouble(coords[0]), miny.doubleValue() + Double.parseDouble(coords[1]), minz.doubleValue() + Double.parseDouble(coords[2])).getBlock();

            if ((b.getTypeId() != blockType.intValue()) || (b.getData() != blockData) || ((b.getState() instanceof InventoryHolder))) {
              i++; if (i >= blockNum) {
                if (i >= blockNum + blocksPer) { blockNum = i; if (notify != null) notify.sendMessage("restored " + i + " miscellaneous blocks so far..."); return; }
                b.setTypeIdAndData(blockType.intValue(), blockData, false);
                if ((b.getState() instanceof InventoryHolder)) {
                  contents.clear();
                  String item;
                  for (Iterator localIterator2 = cfg.getStringList(region + ".data." + pos + "_contents").iterator(); localIterator2.hasNext(); contents.add(new CardboardBox(item).unbox())) item = (String)localIterator2.next();
                  if (notify != null) notify.sendMessage("restoring " + b.getType() + " contents - " + contents.size() + " itemstacks");
                  Inventory inv = ((InventoryHolder)b.getState()).getInventory(); inv.clear(); inv.addItem((ItemStack[])contents.toArray(new ItemStack[0]));
                  List c = Arrays.asList(inv.getContents()); Collections.shuffle(c); inv.setContents((ItemStack[])c.toArray(new ItemStack[0]));
                }
              }
            }
          } if (notify != null) notify.sendMessage("FINISHED! The region '" + region + "' has been restored (" + (n + i) + " blocks changed)");
        Vitals.this.task("regionrestorebatch", Integer.valueOf(-1));
      }
    }
    , 0L, 100L);
  }
  private int regionrestoreArray(World w, String[] array, Material m, Double minx, Double miny, Double minz, Player notify) {
    int blockNum = 0;
    for (int i = 0; i < array.length; i += 3) {
      Block b = new Location(w, minx.doubleValue() + Double.parseDouble(array[i]), miny.doubleValue() + Double.parseDouble(array[(i + 1)]), minz.doubleValue() + Double.parseDouble(array[(i + 2)])).getBlock();
      if (b.getType() != m) { blockNum++; b.setTypeIdAndData(m.getId(), (byte)0, false); }
    }
    if ((notify != null) && (blockNum > 0)) notify.sendMessage("restored " + blockNum + " " + m.toString().toLowerCase() + " blocks...");
    return blockNum;
  }
  private int regionrestoreStart() {
    final HashMap regions = new HashMap();
    for (String region : regionrestoreFiles()) if (config("regionrestore_" + region).getInt(region + ".interval") > 0) {
        regions.put(region, Integer.valueOf(config("regionrestore_" + region).getInt(region + ".interval"))); regionTimers.put(region, Integer.valueOf(0));
      }
    return getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      public void run() {
        for (String region : regionTimers.keySet()) {
          regionTimers.put(region, Integer.valueOf(((Integer)regionTimers.get(region)).intValue() + 1));
          if (((Integer)regionTimers.get(region)).intValue() >= ((Integer)regions.get(region)).intValue()) { regionTimers.put(region, Integer.valueOf(0)); Vitals.this.regionrestore(null, region); debug("[regionrestore] autorestored '" + region + "' based on interval"); return;
          }
        }
      }
    }
    , 1200L, 1200L);
  }

  private void serverlogarchive(Player p) {
    String dataPath = getDataFolder().getAbsolutePath();
    String logPath = dataPath.substring(0, dataPath.lastIndexOf("plugins")) + "server.log";
    File serverLog = new File(logPath); debug("[serverlogarchive] log exists? " + serverLog.exists());
    File archiveFolder = new File(getDataFolder(), "serverlogarchive");
    if (!archiveFolder.exists()) { debug("[serverlogarchive] creating archive folder"); archiveFolder.mkdir(); }
    String archiveLogName = new SimpleDateFormat("yyyyMMdd.HHmmss").format(new Date()) + ".log";
    File archiveLog = new File(archiveFolder, archiveLogName);
    FileChannel source = null; FileChannel destination = null;
    try {
      if (!archiveLog.exists()) { debug("[serverlogarchive] creating archive file " + archiveLogName); archiveLog.createNewFile(); }
      source = new FileInputStream(serverLog).getChannel();
      destination = new FileOutputStream(archiveLog).getChannel();
      destination.transferFrom(source, 0L, source.size());
      if (p != null) p.sendMessage("Successfully copied the server log to: plugins\\Vitals\\serverlogarchive\\" + archiveLogName);
      FileWriter outFile = new FileWriter(serverLog);
      PrintWriter out = new PrintWriter(outFile);
      out.println("");
      if (p != null) p.sendMessage("Successfully cleared the server log.");
      source.close(); destination.close(); debug("[serverlogarchive] archive complete.");
    }
    catch (IOException e) {
      if (p != null) p.sendMessage("Severe: Could not archive the server log due to an IO Exception.");
      log.severe("Could not archive the server log due to an IO Exception.");
      e.printStackTrace();
    }
  }

  private int serverlogarchiveStart() {
    return getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
      public void run() {
        long lastArchive = 0L; long archiveInterval = getConfig().getLong("serverlogarchive_intervalhours") * 60L;
        long keepTime = getConfig().getLong("serverlogarchive_daystokeep") * 86400000L;
        File archiveFolder = new File(getDataFolder(), "serverlogarchive");
        if (archiveFolder.exists()) for (File f : archiveFolder.listFiles()) {
            if ((keepTime > 0L) && (new Date().getTime() - f.lastModified() > keepTime)) f.delete();
            else if (f.lastModified() > lastArchive) lastArchive = f.lastModified();
          }
        long lastArchiveDiff = (new Date().getTime() - lastArchive) / 1000L / 60L;
        debug("[serverlogarchive] checking archives... last archive " + lastArchiveDiff + " minutes ago. interval = " + archiveInterval + " minutes");
        if (lastArchiveDiff > archiveInterval) { debug("[serverlogarchive] archiving..."); Vitals.this.serverlogarchive(null);
        }
      }
    }
    , 600L, 12000L);
  }

  private int usefulcompassStart() {
    return getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      public void run() {
        for (Player p : getServer().getOnlinePlayers()) if (p.getInventory().contains(Material.COMPASS))
            if (p.hasPermission("v.usefulcompass")) {
              World w = p.getWorld(); Location l = p.getLocation(); Location nearest = null;
              for (Player pp : getServer().getOnlinePlayers()) if ((pp != p) && (pp.getWorld() == w) && ((nearest == null) || (l.distanceSquared(pp.getLocation()) < l.distanceSquared(nearest)))) nearest = pp.getLocation();
              if (nearest != null) p.setCompassTarget(nearest); 
            }
            else { p.setCompassTarget(new Location(p.getWorld(), 0.0D, 0.0D, 0.0D)); }

      }
    }
    , 90L, 100L);
  }

  private void warnCmd(Player p, String[] args) {
    if (args.length == 0) {
      List recentwarnings = config("modwarnings").getStringList("recentwarnings");
      p.sendMessage(colorize("&c[Mod Warnings]"));
      if (recentwarnings.size() == 0) { p.sendMessage("There haven't been any warnings issued.");
        return;
      }
      String warning;
      for (Iterator localIterator = recentwarnings.iterator(); localIterator.hasNext(); p.sendMessage(warning)) warning = (String)localIterator.next(); 
    }
    else if (args.length == 2) {
      if (getServer().getPlayer(args[0]) == null) { p.sendMessage("There is no player online by that name."); return; }
      if (perms.playerHas(getServer().getPlayer(args[0]), "v.warn.exempt")) { p.sendMessage("You can't warn that player."); return; }
      String user = getServer().getPlayer(args[0]).getName().toLowerCase();
      String reason = ""; for (int i = 1; i < args.length; i++) reason = reason + (i > 1 ? " " : "") + args[i];
      int decrease = (reason.equalsIgnoreCase("decrease")) && (p.hasPermission("v.warn.admin")) ? 1 : 0;
      List warnings = config("modwarnings").getLongList(user);
      for (Long warning : (Long[])warnings.toArray(new Long[0]))
        if ((new Date().getTime() - warning.longValue()) / 60000L > getConfig().getLong("modwarnings_decayminutes")) { warnings.remove(warning);
        } else if (decrease > 0) { decrease--; warnings.remove(warning);
        }
      if (reason.equalsIgnoreCase("decrease")) {
        if (!auth(p, "warn.admin")) { showUsage(p, "warn"); return; }
        p.sendMessage(args[0] + "'s warning level has been decreased to " + warnings.size());
        config("modwarnings").set(user, warnings);
      } else if (reason.equalsIgnoreCase("reset")) {
        if (!auth(p, "warn.admin")) { showUsage(p, "warn"); return; }
        p.sendMessage(args[0] + "'s warning level has been reset.");
        warnings.clear();
        config("modwarnings").set(user, warnings);
      } else {
        warnings.add(Long.valueOf(new Date().getTime()));
        config("modwarnings").set(user, warnings);
        List recentwarnings = config("modwarnings").getStringList("recentwarnings");
        if (recentwarnings.size() > 10) recentwarnings.remove(0); recentwarnings.add(timestamp(new Date()) + ": " + user + " warned by " + p.getName() + " for: " + reason);
        config("modwarnings").set("recentwarnings", recentwarnings);
        String action = getConfig().getString("modwarnings_level" + warnings.size()); String cmd = "";
        if ((action != null) && (action.length() > 1)) {
          cmd = action.substring(1).replaceAll("\\{player\\}", user);
          getServer().dispatchCommand(getServer().getConsoleSender(), cmd);
        }
        debug("[modwarnings] executing command [Warning Level " + warnings.size() + ":" + cmd + "]");
        getServer().broadcastMessage(colorize("&c-------------------------"));
        getServer().broadcastMessage(colorize("&c" + user + ", you have been warned by " + p.getName() + " for: " + reason));
        getServer().broadcastMessage(colorize("&cWarning level: " + warnings.size() + (cmd == null ? "" : new StringBuilder(". Action taken: ").append(cmd).toString())));
        getServer().broadcastMessage(colorize("&c-------------------------"));
      }
      saveConfig("modwarnings");
    } else {
      showUsage(p, "warn");
    }
  }

  private int warnLevel(Player p) { List warnings = config("modwarnings").getLongList(p.getName().toLowerCase()); int level = warnings.size();
    for (Long warning : (Long[])warnings.toArray(new Long[0])) {
      if ((new Date().getTime() - warning.longValue()) / 60000L > getConfig().getLong("modwarnings_decayminutes")) warnings.remove(warning);
    }
    if (warnings.size() != level) { config("modwarnings").set(p.getName().toLowerCase(), warnings); saveConfig("modwarnings"); }
    return warnings.size(); }

  private void wordswap(Player p, String[] args)
  {
    if (args.length == 0) { p.sendMessage(colorize("&c[Wordswap List]"));
      String word;
      for (Iterator localIterator = config("wordswap").getKeys(false).iterator(); localIterator.hasNext(); p.sendMessage(word + ": " + config("wordswap").getString(word))) word = (String)localIterator.next(); return; }
    if ((args.length == 1) && (config("wordswap").contains(args[0]))) { config("wordswap").set(args[0], null); saveConfig("wordswap"); p.sendMessage("Word swap deleted."); return; }
    if (args.length == 2) { config("wordswap").set(args[0], args[1]); saveConfig("wordswap"); p.sendMessage("Word swap set."); return; }
  }

  private void vtoggle(CommandSender sender, String module) {
    if ((module.equalsIgnoreCase("allon")) || (module.equalsIgnoreCase("alloff"))) {
      for (String key : getConfig().getKeys(false)) if (key.indexOf("_") == -1) getConfig().set(key, Boolean.valueOf(module.equalsIgnoreCase("allon")));
      writeConfig(); init(); sender.sendMessage(toString() + " - All modules have been " + (module.equalsIgnoreCase("allon") ? "enabled" : "disabled")); return;
    }
    if (getConfig().getString(module) == null) { sender.sendMessage("That is not a valid Vitals module. Type /vhelp"); return; }
    getConfig().set(module, Boolean.valueOf(!getConfig().getBoolean(module)));
    writeConfig(); init(); sender.sendMessage(toString() + " module [" + module + "] has been " + (getConfig().getBoolean(module) ? "enabled" : "disabled"));
  }
  private void vsetting(CommandSender sender, String setting, String value) {
    if (value.equals("true")) getConfig().set(setting, Boolean.valueOf(true));
    else if (value.equals("false")) getConfig().set(setting, Boolean.valueOf(false));
    else if (value.equals("null")) getConfig().set(setting, null); else
      try {
        getConfig().set(setting, Double.valueOf(Double.parseDouble(value))); } catch (Exception e) {
        getConfig().set(setting, value);
      }
    writeConfig(); init(); sender.sendMessage(toString() + " setting has been changed.");
  }
  private void vhelp(CommandSender s, String option, String param) {
    if ((option == null) || (option.equalsIgnoreCase("settings"))) {
      String info = ""; String invalid = ""; s.sendMessage(colorize("&c[" + toString() + "]"));
      for (String key : getConfig().getKeys(false)) {
        if (key.indexOf("_") == -1) {
          info = info + (info.length() > 0 ? " " : "") + (getConfig().getBoolean(key) ? "&a" : "&7") + key;
        }
      }
      s.sendMessage(colorize(info));
      if (invalid.length() > 0) s.sendMessage(colorize("&cSETTINGS NOT IN THE RIGHT SECTION: " + invalid));
    }
    else
    {
      int numMotionless;
      World world;
      if (option.equalsIgnoreCase("minecarts")) {
        int numMinecarts = 0; numMotionless = 0;
        Iterator localIterator2;
        for (??? = getServer().getWorlds().iterator(); ???.hasNext(); 
          localIterator2.hasNext())
        {
          world = (World)???.next();
          localIterator2 = world.getEntities().iterator(); continue; Entity entity = (Entity)localIterator2.next();
          if (((entity instanceof Minecart)) && (!(entity instanceof StorageMinecart)) && (!(entity instanceof PoweredMinecart))) {
            numMinecarts++; if (entity.getVelocity().length() == 0.0D) numMotionless++;
            if ((param != null) && (numMinecarts == Integer.parseInt(param))) {
              s.sendMessage("Info on minecart #" + numMinecarts + ": velocity=" + entity.getVelocity().length() + " passenger=" + entity.getPassenger());
              if ((s instanceof Player)) playerTeleport((Player)s, entity.getLocation(), null);
            }
          }
        }

        s.sendMessage(numMinecarts + " minecarts exist, " + numMotionless + " are motionless");
      } else if (option.equalsIgnoreCase("memory")) {
        s.sendMessage(colorize("&c[" + toString() + " Memory Usage]"));
        s.sendMessage(configs.size() + " configs: " + configs.keySet());
        s.sendMessage(tasks.size() + " tasks: " + tasks.keySet());
        s.sendMessage(unusedCarts.size() + " unusedCarts: " + unusedCarts.keySet());
        s.sendMessage(teleportTasks.size() + " teleportTasks: " + teleportTasks.keySet());
        s.sendMessage(flyingTasks.size() + " flyingTasks: " + flyingTasks.keySet());
        s.sendMessage(chainmailNotify.size() + " chainmailNotify: " + chainmailNotify.keySet());
        s.sendMessage(customWarps.size() + " customWarps: " + customWarps.keySet());
        s.sendMessage(regionTimers.size() + " regionTimers: " + regionTimers.keySet());
        s.sendMessage(regionlabelPlayers.size() + " regionlabelPlayers: " + regionlabelPlayers.keySet());
        s.sendMessage(deathretentionInventory.size() + " deathretentionInventory: " + deathretentionInventory.keySet());
        s.sendMessage(deathretentionArmor.size() + " deathretentionArmor: " + deathretentionArmor.keySet());
        s.sendMessage(announcements.size() + " announcements");
        s.sendMessage(regionprotectIgnore.size() + " regionprotectIgnore: " + regionprotectIgnore);
        s.sendMessage(regionprotectRegions.size() + " regionprotectRegions: " + regionprotectRegions.keySet());
      } else if (option.equalsIgnoreCase("tasks")) {
        s.sendMessage(colorize("&c[" + toString() + " Running Tasks]"));
        String task;
        for (numMotionless = tasks.keySet().iterator(); numMotionless.hasNext(); s.sendMessage(task + " [ID " + tasks.get(task) + "]")) task = (String)numMotionless.next(); 
      }
      else if (option.equalsIgnoreCase("events")) {
        s.sendMessage(colorize("&c[" + toString() + " Event Log]"));
        showLatestFileEntries(s, "events.log", 9);
      } else {
        List msg = new ArrayList();
        for (String key : getConfig().getKeys(false)) {
          if (key.startsWith(option + "_")) msg.add(key + ": " + getConfig().getString(key));
        }
        if (msg.size() == 0) { s.sendMessage("No settings available for a module by that name."); return; }
        s.sendMessage(colorize("&c[Vitals module " + option.toUpperCase() + " is currently " + (enabled(option) ? "ENABLED" : "DISABLED") + "]"));
        String line;
        for (world = msg.iterator(); world.hasNext(); s.sendMessage(line)) line = (String)world.next(); 
      }
    }
  }

  private void playerTeleport(final Player p, final Location location, final Runnable callback) { final int delay = getConfig().getInt("global_teleportdelayseconds"); final Location pLoc = p.getLocation();
    if (delay < 0) { log.severe(toString() + ": Invalid configuration, global_teleportdelayseconds should be 0 or higher"); return; }
    if (delay > 0) p.sendMessage(colorize("&cTeleport in progress, don't move for " + delay + " seconds..."));
    teleportTasks.put(p.getName(), Integer.valueOf(getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      int timer = 0;

      public void run() { boolean cancelTask = false;
        if ((timer++ < delay) && (p.isOnline())) {
          if (p.getLocation().distanceSquared(pLoc) > 0.01D) { cancelTask = true; p.sendMessage(Vitals.colorize("&cYou moved! Teleport cancelled.")); }
        } else {
          cancelTask = true; if (p.isOnline()) { p.teleport(location); if (callback != null) callback.run();  }

        }
        if ((cancelTask) && (teleportTasks.containsKey(p.getName()))) { getServer().getScheduler().cancelTask(((Integer)teleportTasks.get(p.getName())).intValue()); teleportTasks.remove(p.getName());
        }
      }
    }
    , 0L, 20L))); }

  public static int getHighestFreeBlockAt(World world, int posX, int posZ) {
    int maxHeight = world.getMaxHeight(); int searchedHeight = maxHeight - 1; Block lastBlock = null;
    for (; searchedHeight > 0; 
      searchedHeight--)
    {
      Block block = world.getBlockAt(posX, searchedHeight, posZ);
      if ((lastBlock != null) && (lastBlock.getType() == Material.AIR) && (block.getType() != Material.AIR)) break;
      lastBlock = block;
    }
    searchedHeight++; return searchedHeight;
  }
  private String loadText(File textFile) {
    StringBuilder contents = new StringBuilder();
    try {
      BufferedReader input = new BufferedReader(new FileReader(textFile));
      try { for (String line = null; (line = input.readLine()) != null; contents.append(line + "\n")); } finally {
        input.close();
      }
    } catch (IOException e) { log.severe("Could not load file '" + textFile.getName() + "' from plugin data folder."); e.printStackTrace(); }
    return contents.toString();
  }
  private boolean saveText(String text, File textFile) {
    try { PrintWriter out = new PrintWriter(new FileWriter(textFile)); out.print(text); out.flush(); out.close(); return true; } catch (IOException e) {
      log.severe("Could not save file '" + textFile.getName() + "' to plugin data folder."); e.printStackTrace(); } return false;
  }
  private boolean saveText(String[] lines, File textFile) {
    try { PrintWriter out = new PrintWriter(new FileWriter(textFile)); for (String line : lines) out.println(line); out.flush(); out.close(); return true; } catch (IOException e) {
      log.severe("Could not save file '" + textFile.getName() + "' to plugin data folder."); e.printStackTrace(); } return false;
  }
  private boolean appendText(String text, File textFile) {
    try { PrintWriter out = new PrintWriter(new FileWriter(textFile, true)); out.println(text); out.flush(); out.close(); return true; } catch (IOException e) {
      log.severe("Could not save file '" + textFile.getName() + "' to plugin data folder."); e.printStackTrace(); } return false;
  }
  private void writeConfig() { writeConfig(getConfig(), "config.yml"); } 
  private void writeConfig(FileConfiguration cfg, String file) {
    try {
      PrintWriter out = new PrintWriter(new FileWriter(new File(getDataFolder(), file)));
      BufferedReader in = new BufferedReader(new InputStreamReader(getResource(file)));
      YamlConfiguration def = new YamlConfiguration();
      String line = in.readLine(); int lineCount = 0;
      for (; line != null; 
        line = in.readLine())
      {
        if ((line.indexOf(":") == -1) || (line.substring(0, 1).equals("#"))) {
          out.println(line);
        } else {
          String[] splitColon = line.split(":"); String[] splitPound = line.split("#");
          Object value = cfg.get(splitColon[0]);
          if (value == null) try {
              def.loadFromString(line); value = def.get(splitColon[0]); cfg.set(splitColon[0], value);
            } catch (Exception localException) {
            } if ((value instanceof String)) value = "'" + value + "'";
          String output = splitColon[0] + ": " + value;
          if (splitPound.length > 1) {
            while (output.length() < 46) output = output + " ";
            output = output + " #" + splitPound[1];
          }
          out.println(output);
        }
        lineCount++;
      }
      in.close(); out.flush(); out.close(); debug(lineCount + " lines written to " + file);
    } catch (IOException e) {
      log.severe("Could not save configuration file '" + file + "'."); e.printStackTrace();
    }
  }
  private void showLatestFileEntries(CommandSender sender, String filename, int numToShow) { File f = new File(getDataFolder(), filename);
    if (!f.exists()) { sender.sendMessage(colorize("&7No entries.")); return; }
    String[] data = loadText(f).split("\n");
    for (int i = numToShow; i >= 1; i--) if ((data.length - i >= 0) && (data[(data.length - i)].length() > 0)) {
        String s = data[(data.length - i)];
        if (filename.contains(".log")) s = s.replaceAll(datestamp(new Date()), "");
        sender.sendMessage(colorize("&7" + s));
      }
  }
}

/* Location:           C:\Users\gabez_000\Downloads\Vitals.jar
 * Qualified Name:     com.pzxc.Vitals.Vitals
 * JD-Core Version:    0.6.2
 */