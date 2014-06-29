/*      */package com.gabezter4.vitals;

/*      */
/*      */import java.io.BufferedReader;
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
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Chunk;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
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
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;


/*      */public class Vitals extends JavaPlugin
/*      */implements Listener
/*      */{
	/* 92 */final Material[] redstoneArray = { Material.REDSTONE_WIRE,
			Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_ON,
			Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.LEVER,
			Material.STONE_BUTTON, Material.RAILS };
	/* 93 */final Material[] recordsArray = { Material.GOLD_RECORD,
			Material.GREEN_RECORD, Material.RECORD_3, Material.RECORD_4,
			Material.RECORD_5, Material.RECORD_6, Material.RECORD_7,
			Material.RECORD_8, Material.RECORD_9, Material.RECORD_10,
			Material.RECORD_11 };
	/* 94 */final Material[] armorArray = { Material.LEATHER_BOOTS,
			Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET,
			Material.LEATHER_LEGGINGS, Material.IRON_BOOTS,
			Material.IRON_CHESTPLATE, Material.IRON_HELMET,
			Material.IRON_LEGGINGS, Material.GOLD_BOOTS,
			Material.GOLD_CHESTPLATE, Material.GOLD_HELMET,
			Material.GOLD_LEGGINGS, Material.DIAMOND_BOOTS,
			Material.DIAMOND_CHESTPLATE, Material.DIAMOND_HELMET,
			Material.DIAMOND_LEGGINGS };
	/* 95 */final Material[] weaponsArray = { Material.WOOD_SWORD,
			Material.STONE_SWORD, Material.IRON_SWORD, Material.GOLD_SWORD,
			Material.DIAMOND_SWORD, Material.BOW, Material.ARROW,
			Material.WOOD_AXE, Material.STONE_AXE, Material.IRON_AXE,
			Material.GOLD_AXE, Material.DIAMOND_AXE };
	/* 96 */final Material[] toolsArray = { Material.WOOD_AXE,
			Material.WOOD_HOE, Material.WOOD_PICKAXE, Material.WOOD_SPADE,
			Material.STONE_AXE, Material.STONE_HOE, Material.STONE_PICKAXE,
			Material.STONE_SPADE, Material.IRON_AXE, Material.IRON_HOE,
			Material.IRON_PICKAXE, Material.IRON_SPADE, Material.GOLD_AXE,
			Material.GOLD_HOE, Material.GOLD_PICKAXE, Material.GOLD_SPADE,
			Material.DIAMOND_AXE, Material.DIAMOND_HOE,
			Material.DIAMOND_PICKAXE, Material.DIAMOND_SPADE };
	/*      */
	/* 98 */public static Economy econ = null;
	public static Permission perms = null;
	/*      */static Logger log;
	/*      */static Random random;
	/*      */YamlConfiguration local;
	/* 99 */HashMap<String, YamlConfiguration> configs = new HashMap();
	/* 100 */HashMap<String, Integer> tasks = new HashMap();
	HashMap<String, Integer> taskIntervals = new HashMap();
	/* 101 */HashMap<Entity, Integer> unusedCarts = new HashMap();
	/* 102 */HashMap<String, Integer> teleportTasks = new HashMap();
	/* 103 */HashMap<String, Integer> flyingTasks = new HashMap();
	/* 104 */HashMap<String, Long> chainmailNotify = new HashMap();
	/* 105 */HashMap<String, List<String>> customWarps = new HashMap();
	/* 106 */List<String> announcements = new ArrayList();
	/* 107 */HashMap<String, Integer> regionTimers = new HashMap();
	/* 108 */HashMap<String, String> regionlabelPlayers = new HashMap();
	/* 109 */HashMap<String, List<ItemStack>> deathretentionInventory = new HashMap();
	/* 110 */HashMap<String, List<ItemStack>> deathretentionArmor = new HashMap();
	/*      */List<String> regionprotectIgnore;
	/*      */HashMap<String, String[]> regionprotectRegions;
	/* 113 */int countdownTask = -1;
	/* 114 */String eventActive = null;
	Arena arenaActive = null;
	long arenaTime = new Date().getTime();
	/* 115 */long auctionLast = new Date().getTime();
	int auctionBid = 0;
	Player auctionBidder = null;
	Player auctionStarter = null;
	ItemStack auctionItem = null;
	/* 116 */String regionrestoreState = null;
	String regionrestoreName = null;
	String regionrestoreMode = null;
	Player regionrestoreOp = null;
	Location regionrestoreCorner1 = null;
	Location regionrestoreCorner2 = null;
	/* 117 */String setup = null;
	String setupName = null;
	String setupOption = null;
	int setupStep = 1;
	Player setupOp = null;
	List<Block> blockChoices = new ArrayList();
	/*      */Integer plotPrice;
	/*		*/int idx;

	/*      */
	/*      */private boolean isRecord(Material m) {
		/* 120 */for (int i = 0; i < recordsArray.length; i++)
			if (recordsArray[i] == m)
				return true;
		/* 120 */return false;
	}

	/* 121 */private boolean isArmor(Material m) {
		if (isChainmail(m))
			return true;
		for (int i = 0; i < armorArray.length; i++)
			if (armorArray[i] == m)
				return true;
		/* 121 */return false;
	}

	/* 122 */private boolean isWeapon(Material m) {
		for (int i = 0; i < weaponsArray.length; i++)
			if (weaponsArray[i] == m)
				return true;
		/* 122 */return false;
	}

	/* 123 */private boolean isTool(Material m) {
		for (int i = 0; i < toolsArray.length; i++)
			if (toolsArray[i] == m)
				return true;
		/* 123 */return false;
	}

	/* 124 */private boolean isChainmail(Material m) {
		return (m == Material.CHAINMAIL_BOOTS)
				|| (m == Material.CHAINMAIL_CHESTPLATE)
				|| (m == Material.CHAINMAIL_HELMET)
				|| (m == Material.CHAINMAIL_LEGGINGS);
	}

	/* 125 */private String lang(String key) {
		return local.getString(key);
	}

	/*      */private boolean showUsage(CommandSender s, String cmd) {
		/* 127 */s.sendMessage(colorize("&e"
				+ cmd
				+ " - "
				+ lang(new StringBuilder(String.valueOf(cmd)).append("Help")
						.toString())));
		/* 128 */s.sendMessage(colorize("&eUsage: "
				+ lang(
						new StringBuilder(String.valueOf(cmd)).append("Syntax")
								.toString()).replaceAll("<command>", cmd)));
		/* 129 */if ((s.hasPermission(cmd + ".admin"))
				&& (lang(cmd + "AdminSyntax") != null))
			s.sendMessage("Admin Usage: "
					+ lang(
							new StringBuilder(String.valueOf(cmd)).append(
									"AdminSyntax").toString()).replaceAll(
							"<command>", cmd));
		/* 130 */return lang(cmd + "Help") != null;
		/*      */}

	/*      */
	/*      */private void msgNearby(Player p, String msg)
	/*      */{
		/* 133 */Entity en;
		/* 133 */label58: for (Iterator localIterator = p.getNearbyEntities(
				48.0D, 48.0D, 48.0D).iterator(); localIterator.hasNext(); ((Player) en)
				.sendMessage(msg)) {
			en = (Entity) localIterator.next();
			if ((!(en instanceof Player)) || (en == p))
				break label58;
		}
		/*      */}

	/*      */
	/*      */public void onEnable() {
		/* 136 */log = getLogger();
		random = new Random();
		/* 137 */if (!getDataFolder().isDirectory())
			getDataFolder().mkdirs();
		/* 138 */getConfig().options().copyDefaults(true);
		writeConfig();
		/* 139 */String languagefile = getConfig().getString("global_language")
				+ ".yml";
		/* 140 */local = YamlConfiguration.loadConfiguration(new File(
				getDataFolder(), languagefile));
		writeConfig(local, languagefile);
		/* 141 */if (!setupEconomy()) {
			log.severe(toString()
					+ " - Disabled because of missing dependency (Vault)");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		/* 142 */setupPermissions();
		init();
		getServer().getPluginManager().registerEvents(this, this);
		log.info(toString() + " - Enabled");
		/*      */}

	/*      */public void onDisable() {
		/* 145 */getServer().getScheduler().cancelAllTasks();
		/* 146 */writeConfig();
		/*      */String configName;
		/* 146 */for (Iterator localIterator = configs.keySet().iterator(); localIterator
				.hasNext(); saveConfig(configName))
			configName = (String) localIterator.next();
		log.info(toString() + " - Saved and Disabled");
		/*      */}

	/*      */
	/*      */private void init()
	/*      */{
		/* 158 */File announceFile = new File(getDataFolder(),
				"announcements.txt");
		boolean entity = true;
		/* 159 */if (announceFile.exists()) {
			/* 160 */announcements = Arrays.asList(loadText(announceFile)
					.split("\n"));
			/*      */} else {
			/* 162 */String[] def = { "This is the first announcement",
					"This is the second announcement" };
			/* 163 */announcements = Arrays.asList(def);
			saveText(def, announceFile);
			/*      */}
		/* 165 */task(
				"abandonedminecarts",
				Integer.valueOf(enabled("abandonedminecarts") ? abandonedminecartsStart()
						: -1));
		/* 166 */task(
				"announcements",
				Integer.valueOf((enabled("announcements"))
						&& (announcements.size() > 0) ? announceStart() : -1));
		/* 167 */task(
				"antiovercrowding",
				Integer.valueOf(enabled("antiovercrowding") ? antiovercrowdingStart()
						: -1));
		/* 168 */task("arena",
				Integer.valueOf(enabled("arena") ? arenaStart() : -1));
		/* 170 */task("playtime",
				Integer.valueOf(enabled("playtime") ? playtimeStart() : -1));
		/* 171 */task("regionlabels",
				Integer.valueOf(enabled("regionlabels") ? regionlabelsStart()
						: -1));
		/* 172 */task("regionrestore",
				Integer.valueOf(enabled("regionrestore") ? regionrestoreStart()
						: -1));
		/* 173 */task(
				"serverlogarchive",
				Integer.valueOf(enabled("serverlogarchive") ? serverlogarchiveStart()
						: -1));
		/* 174 */task(
				"usefulcompass",
				Integer.valueOf(enabled("gameplay_usefulcompass") ? usefulcompassStart()
						: -1));
		/* 175 */task(
				"worlddate",
				Integer.valueOf((enabled("worlddate"))
						&& (enabled("worlddate_announce")) ? dateStart() : -1));
		/* 176 */customWarps.clear();
		/* 177 */for (String warp : config("customwarps").getKeys(false)) {
			/* 178 */String[] info = config("customwarps").getString(warp)
					.split("_");
			/* 179 */List coords = new ArrayList();
			coords.add(info[0]);
			coords.add(info[1]);
			coords.add(info[2]);
			coords.add(info[3]);
			coords.add(info[4]);
			coords.add(info[5]);
			/* 180 */customWarps.put(warp, coords);
			/*      */}
		/* 182 */regionprotectRegions = new HashMap();
		/* 183 */if (!enabled("regionprotect")) {
			/* 184 */regionprotectIgnore = new ArrayList();
			/*      */} else {
			/* 186 */regionprotectIgnore = Arrays.asList(getConfig().getString(
					"regionprotect_ignoreblockidlist").split(","));
			/* 187 */for (String region : config("regionprotect")
					.getKeys(false)) {
				/* 188 */String[] cuboid = config("regionprotect").getString(
						region).split("_");
				/* 189 */regionprotectRegions.put(region, cuboid);
				/*      */}
			/*      */}
		/*      */}

	/*      */
	/* 194 */static String colorize(String string) {
		return string.replaceAll("(?i)&([a-k0-9])", "§$1");
	}

	/* 195 */static String timestamp(Date d) {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d);
	}

	/* 196 */static String datestamp(Date d) {
		return new SimpleDateFormat("yyyy-MM-dd").format(d);
	}

	/* 197 */void debug(String s) {
		if (enabled("global_debug"))
			log.info("[DEBUG] " + s);
	}

	/* 198 */void broadcastEvent(String s) {
		if (!enabled("global_hideeventsfromconsole"))
			getServer().getConsoleSender().sendMessage(colorize(s));
		for (Player p : getServer().getOnlinePlayers())
			if (p.hasPermission("v.eventbroadcasts"))
				p.sendMessage(colorize(s));
	}

	/* 199 */boolean logEvent(String eventName, String text) {
		return appendText(timestamp(new Date()) + " [" + eventName + "] "
				+ text, new File(getDataFolder(), "events.log"));
	}

	/*      */void cuboidFill(World w, Double minx, Double miny, Double minz,
			Double maxx, Double maxy, Double maxz, int blockID,
			Predicate<Integer> p) {
		/* 201 */for (Double x = minx; x.doubleValue() <= maxx.doubleValue(); x = Double
				.valueOf(x.doubleValue() + 1.0D))
			for (Double y = miny; y.doubleValue() <= maxy.doubleValue(); y = Double
					.valueOf(y.doubleValue() + 1.0D))
				for (Double z = minz; z.doubleValue() <= maxz.doubleValue(); z = Double
						.valueOf(z.doubleValue() + 1.0D)) {
					Location l = new Location(w, x.doubleValue(),
							y.doubleValue(), z.doubleValue());
					if ((p == null)
							|| (p.test(Integer
									.valueOf(l.getBlock().getTypeId()))))
						l.getBlock().setTypeId(blockID);
				}
		/*      */}

	/*      */YamlConfiguration config(String configName) {
		/* 203 */if (!configs.containsKey(configName))
			configs.put(configName, YamlConfiguration
					.loadConfiguration(new File(getDataFolder(), configName
							+ ".yml")));
		return (YamlConfiguration) configs.get(configName);
	}

	/* 204 */private void deleteConfig(String configName) {
		if (configs.containsKey(configName))
			configs.remove(configName);
		File f = new File(getDataFolder(), configName + ".yml");
		if (f.exists())
			f.delete();
	}

	/*      */boolean saveConfig(String configName) {
		/* 206 */if (!configs.containsKey(configName))
			configs.put(configName, YamlConfiguration
					.loadConfiguration(new File(getDataFolder(), configName
							+ ".yml")));
		try {
			/* 207 */debug("saving config file " + configName + ".yml");
			((YamlConfiguration) configs.get(configName)).save(new File(
					getDataFolder(), configName + ".yml"));
			return true;
		} catch (IOException e) {
			/* 208 */log.severe("IO Error while saving file '" + configName
					+ ".yml' to plugin data folder.");
			e.printStackTrace();
		}
		return false;
		/*      */}

	/*      */private boolean setupEconomy() {
		/* 211 */if (getServer().getPluginManager().getPlugin("Vault") == null)
			return false;
		RegisteredServiceProvider rsp = getServer().getServicesManager()
				.getRegistration(Economy.class);
		if (rsp == null)
			return false;
		econ = (Economy) rsp.getProvider();
		return econ != null;
	}

	/* 212 */private boolean setupPermissions() {
		RegisteredServiceProvider rsp = getServer().getServicesManager()
				.getRegistration(Permission.class);
		perms = (Permission) rsp.getProvider();
		return perms != null;
	}

	/*      */private TreeMap<String, Double> mapSort(HashMap<String, Double> map) {
		/* 214 */TreeMap sorted_map = new TreeMap(new ValueComparator(map));
		sorted_map.putAll(map);
		return sorted_map;
		/*      */}

	/* 216 */private void task(String name, Integer id) {
		if (tasks.containsKey(name))
			getServer().getScheduler().cancelTask(
					((Integer) tasks.get(name)).intValue());
		/* 217 */if (id.intValue() == -1)
			tasks.remove(name);
		else
			tasks.put(name, id);
	}

	/*      */private boolean has(String[] args, int index, String s) {
		/* 219 */if (args.length >= index + 1)
			return args[index].equalsIgnoreCase(s);
		return false;
		/*      */}

	/* 221 */private boolean enabled(String module) {
		return (getConfig().getBoolean(module))
				&& ((!module.startsWith("gameplay_")) || (enabled("gameplay")))
				&& ((!module.startsWith("townymods_")) || (enabled("townymods")));
	}

	/*      */
	/*      */private boolean auth(CommandSender s, String permission) {
		/* 224 */if ((!(s instanceof Player))
				|| (perms.playerHas((Player) s, "v." + permission)))
			return true;
		/* 225 */s.sendMessage("You don't have access to do that...");
		debug("player [" + s.getName() + "] denied permission [v." + permission
				+ "]");
		return false;
		/*      */}

	/* 227 */private void setupNew(Player p, String id, String name,
			String option) {
		setup(p, id, name, option, 1, null);
	}

	/*      */private void setup(Player p, String id, String name, String option,
			int step, Block b) {
		/* 229 */setupOp = p;
		setup = id;
		setupName = name;
		setupOption = option;
		setupStep = step;
		/*      */
		/* 231 */if (b != null) {
			blockChoices.add(b);
			setupStep += 1;
		}
		/* 232 */if (setupStep <= 2)
			p.sendMessage(lang("region_select" + setupStep));
		else
			setupComplete();
		/*      */}

	/*      */
	/* 235 */private void setupComplete() {
		if ((setup.equals("regionlabel")) || (setup.equals("regionprotect"))) {
			/* 236 */Block b1 = (Block) blockChoices.get(0);
			Block b2 = (Block) blockChoices.get(1);
			/* 237 */Double x1 = Double.valueOf(b1.getX());
			Double y1 = Double.valueOf(b1.getY());
			Double z1 = Double.valueOf(b1.getZ());
			Double x2 = Double.valueOf(b2.getX());
			Double y2 = Double.valueOf(b2.getY());
			Double z2 = Double.valueOf(b2.getZ());
			/* 238 */Double minx = Double.valueOf(Math.min(x1.doubleValue(),
					x2.doubleValue()));
			Double miny = Double.valueOf(Math.min(y1.doubleValue(),
					y2.doubleValue()));
			Double minz = Double.valueOf(Math.min(z1.doubleValue(),
					z2.doubleValue()));
			Double maxx = Double.valueOf(Math.max(x1.doubleValue(),
					x2.doubleValue()));
			Double maxy = Double.valueOf(Math.max(y1.doubleValue(),
					y2.doubleValue()));
			Double maxz = Double.valueOf(Math.max(z1.doubleValue(),
					z2.doubleValue()));
			/* 239 */if (setupOption.equals("exact"))
				config("regionlabels").set(
						setupName,
						b1.getWorld().getName() + "_" + minx + "_" + miny + "_"
								+ minz + "_" + (maxx.doubleValue() + 1.0D)
								+ "_" + (maxy.doubleValue() + 1.0D) + "_"
								+ (maxz.doubleValue() + 1.0D));
			else
				/* 240 */config(setup)
						.set(setupName,
								b1.getWorld().getName() + "_" + minx + "_" + 0
										+ "_" + minz + "_"
										+ (maxx.doubleValue() + 1.0D) + "_"
										+ 999 + "_"
										+ (maxz.doubleValue() + 1.0D));
			/* 241 */saveConfig(setup);
			setupOp.sendMessage("Region '" + setupName.replaceAll("_", " ")
					+ "' has been " + setup.replaceAll("region", "") + "ed.");
			/* 242 */} else if (setup.equals("regionrestore")) {
			/* 243 */Block b1 = (Block) blockChoices.get(0);
			Block b2 = (Block) blockChoices.get(1);
			/* 244 */if (tasks.containsKey("regionsavebatch"))
				setupOp.sendMessage("Canceled save that was in progress to start new save...");
			/* 245 */task("regionsavebatch", Integer.valueOf(regionsaveBatch(
					setupName, b1, b2, setupOption, setupOp)));
			/*      */} else {
			/* 247 */setupOp
					.sendMessage("Invalid setup type (this should never happen!)");
			/* 248 */}
		setup = null;
		setupName = null;
		setupOption = null;
		setupOp = null;
		setupStep = 1;
		blockChoices = new ArrayList();
	}

	/*      */
	/*      */public boolean onCommand(CommandSender s, Command cmd,
			String commandLabel, String[] args)
	/*      */{
		/* 252 */if ((cmd.getName().equalsIgnoreCase("vhelp"))
				&& (auth(s, "admin"))) {
			/* 253 */vhelp(s, args.length >= 1 ? args[0] : null,
					args.length >= 2 ? args[1] : null);
			return true;
			/*      */}
		/* 255 */if ((cmd.getName().equalsIgnoreCase("vtoggle"))
				&& (auth(s, "admin")) && (args.length == 1)) {
			/* 256 */vtoggle(s, args[0]);
			return true;
			/*      */}
		/* 258 */if ((cmd.getName().equalsIgnoreCase("vsetting"))
				&& (auth(s, "admin")) && (args.length >= 2)) {
			/* 259 */String val = args[1];
			for (int i = 2; i < args.length; i++)
				val = val + " " + args[i];
			vsetting(s, args[0], val);
			return true;
			/*      */}
		/* 261 */if ((cmd.getName().equalsIgnoreCase("vreload"))
				&& (auth(s, "admin"))) {
			/* 262 */reloadConfig();
			init();
			s.sendMessage(toString()
					+ " configuration has been reloaded from the config.yml file.");
			return true;
			/*      */}
		/* 264 */return false;
		/*      */}

	/*      */
	/*      */@EventHandler
	/*      */public void onPlayerJoin(PlayerJoinEvent event) {
		/* 269 */if (enabled("onlineplayersflatfile"))
			onlinePlayers(null);
		/* 270 */if (enabled("betternews"))
			betternews(event.getPlayer(),
					getConfig().getInt("betternews_showonlogin"));
		/*      */}

	/*      */
	/* 274 */@EventHandler
	/*      */public void onPlayerQuit(PlayerQuitEvent event) {
		if (enabled("onlineplayersflatfile"))
			onlinePlayers(event.getPlayer());
		/*      */}

	/*      */
	/*      */@EventHandler
	/*      */public void onPlayerMove(PlayerMoveEvent e)
	/*      */{
		/* 289 */if (e.getFrom().getBlock().equals(e.getTo().getBlock()))
			return;
		/* 290 */Player p = e.getPlayer();
		Material steppingOn = e.getTo().getBlock().getRelative(0, -1, 0)
				.getType();
		/* 291 */if ((steppingOn == Material.SPONGE)
				&& (enabled("gameplay_bouncysponges"))
				&& (p.hasPermission("v.bouncysponges"))) {
			/* 292 */Vector v = p.getVelocity();
			v.setY(2);
			p.setVelocity(v);
			return;
			/* 293 */}
		if ((steppingOn == Material.EMERALD_BLOCK)
				&& (enabled("gameplay_emeraldblockhaste"))
				&& (p.hasPermission("v.emeraldblockhaste"))) {
			/* 294 */p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
					20, 2), true);
			return;
			/*      */}
		/*      */}

	/*      */
	/* 299 */@EventHandler
	/*      */public void onBlockPlace(BlockPlaceEvent e) {
		if ((!regionprotectIgnore.contains(e.getBlock().getTypeId()))
				&& (!perms.playerHas(e.getPlayer(), "v.regionprotect.bypass"))
				&& (regionprotected(e.getBlock().getLocation()))) {
			e.setCancelled(true);
			return;
		}
	}

	/*      */
	/*      */@EventHandler
	/*      */public void onBlockBreak(BlockBreakEvent e) {
		/* 303 */if ((!regionprotectIgnore.contains(e.getBlock().getTypeId()))
				&& (!perms.playerHas(e.getPlayer(), "v.regionprotect.bypass"))
				&& (regionprotected(e.getBlock().getLocation()))) {
			e.setCancelled(true);
			return;
		}
		/*      */}

	/*      */
	/* 307 */@EventHandler
	/*      */public void onCreatureSpawn(CreatureSpawnEvent e) {
		if ((e.getEntityType() == EntityType.VILLAGER)
				&& (getConfig().getString("gameplay_villagerprofessions")
						.length() > 0)) {
			/* 308 */final Villager v = (Villager) e.getEntity();
			/* 309 */getServer().getScheduler().scheduleAsyncDelayedTask(this,
					new Runnable() {
						/*      */public void run() {
							/* 311 */List bannedProfessions = Arrays
									.asList(getConfig()
											.getString(
													"gameplay_villagerprofessions")
											.toUpperCase().split(","));
							/* 312 */Villager.Profession[] prof = Villager.Profession
									.values();
							String pr = v.getProfession().toString();
							/* 313 */debug("Villager " + v.getProfession()
									+ " spawned, banned is "
									+ bannedProfessions);
							/* 314 */if (bannedProfessions.size() == prof.length) {
								debug("[VillagerProfessions] villager spawned, all professions are disabled, removing villager");
								v.remove();
								return;
							}
							/* 315 */while (bannedProfessions.contains(pr)) {
								/* 316 */v.setProfession(prof[Vitals.random
										.nextInt(prof.length)]);
								/*      */}
							/* 318 */debug("[VillagerProfessions] villager spawned as "
									+ pr
									+ ", new profession is "
									+ v.getProfession().toString());
							/*      */}
						/*      */
					}
					/*      */, 1L);
			/*      */}
	}

	/*      */
	/*      */@EventHandler
	/*      */public void onPlayerDeath(PlayerDeathEvent e) {
		/* 325 */Player p = e.getEntity();
		/* 326 */if ((arenaActive != null)
				&& (arenaActive.state.equals("active"))
				&& (arenaActive.playerAlive(p))) {
			arenaActive.playerDisqualify(p, null);
			return;
		}
		/* 327 */if ((enabled("bounties"))
				&& ((arenaActive == null) || (!arenaActive.playerAlive(e
						.getEntity())))
				&& ((e.getEntity().getKiller() instanceof Player))
				&& (e.getEntity().getKiller() != e.getEntity())) {
			/* 328 */String pName = e.getEntity().getName().toLowerCase();
			String kName = e.getEntity().getKiller().getName().toLowerCase();
			bountyDeath(kName, pName);
			/*      */}
		/* 330 */if (enabled("gameplay_deathretention"))
			deathretention(e, p);
		/*      */}

	/*      */
	/* 334 */@EventHandler
	/*      */public void onPlayerRespawn(PlayerRespawnEvent e) {
		Player p = e.getPlayer();
		/* 335 */if (deathretentionInventory.containsKey(p.getName()))
		/*      */{
			/* 336 */ItemStack i;
			/* 336 */for (Iterator localIterator = ((List) deathretentionInventory
					.get(p.getName())).iterator(); localIterator.hasNext(); p
					.getInventory().addItem(new ItemStack[] { i }))
				i = (ItemStack) localIterator.next();
			deathretentionInventory.remove(p.getName());
			/*      */}
		/* 338 */if (deathretentionArmor.containsKey(p.getName())) {
			/* 339 */p.getInventory().setArmorContents(
					(ItemStack[]) ((List) deathretentionArmor.get(p.getName()))
							.toArray(new ItemStack[0]));
			deathretentionArmor.remove(p.getName());
			/*      */}
	}

	/*      */
	/*      */@EventHandler
	/*      */public void onPlayerInteract(PlayerInteractEvent e) {
		/* 344 */final Player p = e.getPlayer();
		Location l = p.getLocation();
		/* 345 */if ((arenaActive != null)
				&& (arenaActive.event.equals("RaceToTheFinish"))
				&& (e.hasItem())
				&& (e.getItem().getType() == Material.ENDER_PEARL)) {
			e.setCancelled(true);
			return;
		}
		/* 346 */if ((enabled("gameplay_blazerodfireball"))
				&& (e.hasItem())
				&& (e.getItem().getType() == Material.BLAZE_ROD)
				&& ((e.getAction() == Action.RIGHT_CLICK_BLOCK) || (e
						.getAction() == Action.RIGHT_CLICK_AIR))
				&& (auth(p, "blazerodfireball"))) {
			/* 347 */removeItem(p, Material.BLAZE_ROD);
			/* 348 */Location loc = p.getEyeLocation().toVector()
					.add(l.getDirection().multiply(2))
					.toLocation(p.getWorld(), l.getYaw(), l.getPitch());
			/* 349 */p.getWorld().spawn(loc, Fireball.class);
			/*      */}
		/* 351 */if ((enabled("gameplay_featherfly"))
				&& (e.hasItem())
				&& (e.getItem().getType() == Material.FEATHER)
				&& ((e.getAction() == Action.RIGHT_CLICK_BLOCK) || (e
						.getAction() == Action.RIGHT_CLICK_AIR))
				&& (auth(p, "featherfly"))) {
			/* 352 */if (p.isFlying()) {
				/* 353 */p.setAllowFlight(false);
				p.sendMessage(lang("featherfly_cancel"));
				msgNearby(p, p.getName() + lang("featherfly_cancelothers"));
				/* 354 */if (flyingTasks.containsKey(p.getName())) {
					/* 355 */getServer().getScheduler()
							.cancelTask(
									((Integer) flyingTasks.get(p.getName()))
											.intValue());
					flyingTasks.remove(p.getName());
					/*      */}
				/* 357 */return;
				/*      */}
			/* 359 */l.add(0.0D, 1.0D, 0.0D);
			p.teleport(l);
			p.setAllowFlight(true);
			p.setFlying(true);
			/* 360 */removeItem(p, Material.FEATHER);
			p.sendMessage(lang("featherfly_effect"));
			msgNearby(p, p.getName() + lang("featherfly_effectothers"));
			/* 361 */flyingTasks.put(p.getName(), Integer.valueOf(getServer()
					.getScheduler().scheduleSyncRepeatingTask(
							this,
							new Runnable() {
								/*      */public void run() {
									/* 363 */debug("[" + p + ", " + p.getName()
											+ ", " + flyingTasks.size() + "]");
									/* 364 */if (!p.isOnline()) {
										/* 365 */getServer()
												.getScheduler()
												.cancelTask(
														((Integer) flyingTasks.get(p
																.getName()))
																.intValue());
										flyingTasks.remove(p.getName());
										/* 366 */} else if ((!p.isFlying())
											|| (!removeItem(p, Material.FEATHER))) {
										/* 367 */p.setAllowFlight(false);
										p.sendMessage(Vitals.this
												.lang("featherfly_cancel"));
										Vitals.this.msgNearby(
												p,
												p.getName()
														+ Vitals.access$0(
																Vitals.this,
																"featherfly_cancelothers"));
										/* 368 */getServer()
												.getScheduler()
												.cancelTask(
														((Integer) flyingTasks.get(p
																.getName()))
																.intValue());
										flyingTasks.remove(p.getName());
										/*      */}
									/*      */}
								/*      */
							}
							/*      */,
							getConfig().getLong("gameplay_featherfly_interval") * 20L,
							getConfig().getLong("gameplay_featherfly_interval") * 20L)));
			/* 372 */return;
			/*      */}
		/* 374 */if (e.isCancelled())
			return;
		/* 375 */if ((arenaActive != null)
				&& (arenaActive.state.equals("setup"))) {
			arenaActive.setup(e);
			return;
		}
		/* 376 */if ((setup != null) && (p == setupOp)
				&& (e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			setup(p, setup, setupName, setupOption, setupStep,
					e.getClickedBlock());
			return;
		}
		/* 377 */if ((enabled("gameplay_safejukebox"))
				&& (p.hasPermission("v.safejukebox"))
				&& (e.getAction() == Action.RIGHT_CLICK_BLOCK)
				&& (e.getClickedBlock().getType() == Material.JUKEBOX)
				&& (e.hasItem()) && (isRecord(e.getItem().getType()))) {
			/* 378 */e.setCancelled(true);
			p.getWorld().playEffect(e.getClickedBlock().getLocation(),
					Effect.RECORD_PLAY, e.getItem().getTypeId());
			return;
			/*      */}
		/*      */}

	/*      */
	/* 382 */public boolean removeItem(Player p, Material m) {
		Inventory inv = p.getInventory();
		/* 383 */for (int i = 0; i < inv.getSize(); i++) {
			/* 384 */ItemStack is = inv.getItem(i);
			/* 385 */if ((is != null) && (is.getType() == m)) {
				/* 386 */if (is.getAmount() > 1)
					is.setAmount(is.getAmount() - 1);
				else
					inv.setItem(i, null);
				return true;
				/*      */}
			/*      */}
		/* 389 */return false;
	}

	/*      */
	/*      */@EventHandler
	/*      */public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
		/* 393 */if ((enabled("gameplay_villagerpermission"))
				&& ((e.getRightClicked() instanceof Villager))
				&& (!e.getPlayer().hasPermission("v.villagerpermission")))
			e.setCancelled(true);
		/*      */}

	/*      */
	/* 397 */@EventHandler
	/*      */public void onPlayerChat(AsyncPlayerChatEvent e) {
		if ((enabled("anticaps"))
				&& (!perms.playerHas(e.getPlayer(), "v.anticaps.bypass"))
				&& (e.getMessage().length() >= 10)) {
			/* 398 */int numCaps = 0;
			for (int i = 0; i < e.getMessage().length(); i++)
				if (Character.isUpperCase(e.getMessage().charAt(i)))
					numCaps++;
			/* 399 */if (1.0D * numCaps / e.getMessage().length() >= getConfig()
					.getDouble("anticaps_cutoffpercent") / 100.0D) {
				/* 400 */e.setCancelled(true);
				e.getPlayer().sendMessage(lang("anticaps"));
				return;
				/*      */}
			/*      */}
		/* 403 */boolean global = false;
		for (String keyword : getConfig()
				.getString("chatworlds_globalkeywords").split(","))
			if (e.getMessage().toLowerCase().contains(keyword.toLowerCase()))
				global = true;
		/* 404 */if ((enabled("chatworlds")) && (!global)
				&& (e.getRecipients().size() > 1))
			for (Player p : (Player[]) e.getRecipients().toArray(new Player[0]))
				/* 405 */if ((p.getWorld() != e.getPlayer().getWorld())
						&& (!p.hasPermission("v.chatworlds.bypass"))
						&& (!perms.playerHas(e.getPlayer(),
								"v.chatworlds.bypass")))
					e.getRecipients().remove(p);
		/*      */Object domainRegex;
		/* 407 */if ((enabled("antiadvertising"))
				&& (!perms.playerHas(e.getPlayer(), "v.antiadvertising.bypass"))) {
			/* 408 */String ipRegex = "(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])";
			domainRegex = "[a-z0-9]+([\\-\\.]{1}[a-z0-9]+)*\\.[a-z]{2,6}";
			/* 409 */String[] whitelist = getConfig().getString(
					"antiadvertising_whitelist").split(",");
			/* 410 */String msg = e.getMessage();
			for (int i = 0; i < whitelist.length; i++)
				msg = msg.replaceAll(whitelist[i], "~~~" + i + "~~~");
			/* 411 */if ((msg.matches(".*" + ipRegex + ".*"))
					|| (msg.matches(".*" + (String) domainRegex + ".*"))) {
				/* 412 */debug("advertising detected, taking action ["
						+ getConfig().getString("antiadvertising_action") + "]");
				/* 413 */if (getConfig().getString("antiadvertising_action")
						.equals("mute")) {
					e.setCancelled(true);
					return;
				}
				/* 414 */if (getConfig().getString("antiadvertising_action")
						.equals("mute")) {
					e.setCancelled(true);
					e.getPlayer().kickPlayer("");
					return;
				}
				/* 415 */if (getConfig().getString("antiadvertising_action")
						.equals("secretmute")) {
					/* 416 */Set r = e.getRecipients();
					r.clear();
					r.add(e.getPlayer());
					/* 417 */for (Player p : getServer().getOnlinePlayers())
						if (p.isOp()) {
							r.add(p);
							p.sendMessage(colorize("&6[AntiAdvertising] Muted message for everyone except sender and ops"));
						}
					/*      */}
				/* 418 */else if (getConfig().getString(
						"antiadvertising_action").equals("replace")) {
					/* 419 */msg = msg.replaceAll(ipRegex, "").replaceAll(
							(String) domainRegex, "");
					/* 420 */for (int i = 0; i < whitelist.length; i++)
						msg = msg.replaceAll("~~~" + i + "~~~", whitelist[i]);
					/* 421 */e.setMessage(msg);
					/*      */}
				/*      */}
			/*      */}
		/* 425 */if (enabled("antistickykeys"))
			e.setMessage(e.getMessage().replaceAll("(.)\\1{4,}", "$1"));
		/* 426 */if (enabled("wordswap"))
		/*      */{
			/* 426 */String word;
			/* 426 */for (domainRegex = config("wordswap").getKeys(false)
					.iterator(); ((Iterator) domainRegex).hasNext(); e
					.setMessage(e.getMessage().replaceAll("(?i)" + word,
							config("wordswap").getString(word))))
				word = (String) ((Iterator) domainRegex).next();
			/*      */}
		/* 427 */if (enabled("helperbot"))
			/* 428 */for (domainRegex = config("helperbot").getKeys(false)
					.iterator(); ((Iterator) domainRegex).hasNext();) {
				String wordpair = (String) ((Iterator) domainRegex).next();
				/* 429 */boolean bot = true;
				String[] botWords = wordpair.split("_");
				String[] chatWords = e.getMessage().split(" ");
				if (!botWords[0].equals("command")) {
					/* 430 */final String response = config("helperbot")
							.getString(wordpair);
					/* 431 */for (String botWord : botWords) {
						/* 432 */boolean wordFound = false;
						/* 433 */for (int i = 0; i < chatWords.length; i++)
							if (chatWords[i].toLowerCase().equals(
									botWord.toLowerCase()))
								wordFound = true;
						/* 434 */if (!wordFound)
							bot = false;
						/*      */}
					/* 436 */if (bot)
						/* 437 */getServer().getScheduler()
								.scheduleAsyncDelayedTask(this, new Runnable() {
									/* 438 */public void run() {
										getServer()
												.broadcastMessage(
														Vitals.colorize(getConfig()
																.getString(
																		"helperbot_chatprefix")
																+ response));
									}
									/*      */
									/*      */
								}
								/*      */, 20L);
					/*      */}
				/*      */}
		/* 442 */if ((enabled("modwarnings"))
				&& (enabled("modwarnings_showlevel"))) {
			/* 443 */int warningLevel = warnLevel(e.getPlayer());
			/* 444 */if (warningLevel > 0) {
				/* 445 */char[] s = new char[warningLevel];
				Arrays.fill(s, '*');
				String stars = new String(s);
				/* 446 */e.setMessage(colorize(getConfig().getString(
						"modwarnings_chatprefix").replaceFirst("\\*", stars))
						+ " " + e.getMessage());
				/*      */}
			/*      */}
	}

	/*      */
	/*      */@EventHandler
	/*      */public void onPlayerTeleport(PlayerTeleportEvent e) {
		/* 452 */if (enabled("regionlabels")) {
			/* 453 */String toRegion = regionGet("regionlabels", e.getTo());
			if (toRegion != null)
				e.getPlayer()
						.sendMessage(
								colorize(lang("regionlabels_enter").replaceAll(
										"\\{region\\}",
										toRegion.replaceAll("_", " "))));
			/*      */}
		/*      */}

	/*      */
	/* 458 */@EventHandler(priority = EventPriority.LOW)
	/*      */public void onEntityDamage(EntityDamageEvent e) {
		if (((e.getEntity() instanceof Villager))
				&& (enabled("unkillablevillagers"))) {
			e.setCancelled(true);
			return;
		}
		/* 459 */if ((e.getEntity() instanceof Player)) {
			/* 460 */Player p = (Player) e.getEntity();
			/* 461 */if ((enabled("gameplay_damagestopsflying"))
					&& (p.isFlying()))
				p.setFlying(false);
			/* 462 */if ((enabled("gameplay_bouncysponges"))
					&& (e.getCause() == EntityDamageEvent.DamageCause.FALL)
					&& (p.getLocation().getBlock().getRelative(0, -1, 0)
							.getType() == Material.SPONGE)) {
				e.setCancelled(true);
				return;
			}
			/* 463 */if ((enabled("gameplay_emeraldblockhaste"))
					&& (e.getCause() == EntityDamageEvent.DamageCause.FALL)
					&& (p.getLocation().getBlock().getRelative(0, -1, 0)
							.getType() == Material.EMERALD_BLOCK)) {
				e.setCancelled(true);
				return;
			}
			/* 464 */if ((arenaActive != null) && (arenaActive.playerAlive(p)))
			/*      */{
				/* 466 */if (arenaActive.event.equals("RaceToTheFinish")) {
					e.setCancelled(true);
					return;
				}
				/* 467 */if (((e instanceof EntityDamageByEntityEvent))
						&& ((((EntityDamageByEntityEvent) e).getDamager() instanceof Player))) {
					/* 468 */if (arenaActive.state.equals("signup")) {
						e.setCancelled(true);
						return;
					}
					/* 469 */EntityDamageByEntityEvent e2 = (EntityDamageByEntityEvent) e;
					Player p2 = (Player) e2.getDamager();
					/*      */
					/* 471 */if ((arenaActive.event.equals("Team PVP"))
							&& (arenaActive.playerAlive(p2))
							&& (arenaActive.sameTeam(p, p2))) {
						e.setCancelled(true);
						return;
					}
					/*      */}
				/*      */}
			/* 474 */if ((getConfig().getDouble("gameplay_superchainmail") != 1.0D)
					&& (p.hasPermission("v.superchainmail"))) {
				/* 475 */Double damageFactor = Double.valueOf(1.0D);
				int numChainmail = 0;
				/* 476 */for (ItemStack armor : p.getInventory()
						.getArmorContents())
					if (isChainmail(armor.getType())) {
						numChainmail++;
						damageFactor = Double.valueOf(damageFactor
								.doubleValue()
								* getConfig().getDouble(
										"gameplay_superchainmail"));
					}
				/* 477 */long lastNotify = chainmailNotify.containsKey(p
						.getName()) ? (new Date().getTime() - ((Long) chainmailNotify
						.get(p.getName())).longValue()) / 1000L : 99999999L;
				/* 478 */if ((numChainmail > 0) && (lastNotify > 10L)) {
					chainmailNotify.put(p.getName(),
							Long.valueOf(new Date().getTime()));
					p.sendMessage(colorize("&7"
							+ lang("superchainmail_effect")
							+ Math.round(100.0D * (1.0D - damageFactor
									.doubleValue())) + "%!"));
				}
				/* 479 */e.setDamage((int) Math.round(damageFactor
						.doubleValue() * e.getDamage()));
				/*      */}
			/*      */}
	}

	/*      */
	/*      */public boolean did(PlayerCommandPreprocessEvent e, CommandSender s,
			String c, String[] args, String mod, String perm, String cmd,
			int min, int max) {
		/* 484 */if (!c.equalsIgnoreCase("/" + cmd))
			return false;
		/* 485 */if (!enabled(mod)) {
			if (s.isOp())
				s.sendMessage("[Vitals module " + mod
						+ " is not enabled - op-only message]");
			return false;
		}
		/* 486 */e.setCancelled(true);
		String cc = c;
		for (int i = 0; i < args.length; i++)
			cc = cc + " " + args[i];
		/* 487 */String msg = "[COMMAND] " + s.getName() + ": " + cc;
		/* 488 */if (!auth(s, perm)) {
			debug(msg + " [access denied]");
			return false;
		}
		/* 489 */if ((args.length >= min) && (args.length <= max)) {
			debug(msg);
			return true;
		}
		/* 490 */debug(msg + " [invalid arguments, showing syntax]");
		/* 491 */showUsage(s, cmd);
		/* 492 */return false;
		/*      */}

	/*      */@EventHandler
	/*      */public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
		/* 496 */final Player p = e.getPlayer();
		String cc = e.getMessage();
		String c = cc.split(" ")[0].toLowerCase();
		/* 497 */String[] a = cc.indexOf(" ") == -1 ? new String[0] : cc
				.substring(cc.indexOf(" ") + 1).split(" ");
		/* 498 */if (did(e, p, c, a, "announcements", "announce", "announce",
				1, 99)) {
			announce(p, a);
			return;
		}
		/* 499 */if (did(e, p, c, a, "arena", "arena", "arena", 0, 99)) {
			arena(p, a);
			return;
		}
		/* 500 */if (did(e, p, c, a, "auctions", "auction", "auction", 0, 1)) {
			auction(p, a);
			return;
		}
		/* 501 */if (did(e, p, c, a, "auctions", "auction.bid", "bid", 1, 1)) {
			auctionbid(p, a[0]);
			return;
		}
		/* 502 */if (did(e, p, c, a, "betterhelp", "help", "help", 0, 2)) {
			betterhelpCmd(p, a);
			return;
		}
		/* 503 */if (did(e, p, c, a, "betternews", "news", "news", 0, 99)) {
			betternewsCmd(p, a);
			return;
		}
		/* 504 */if (did(e, p, c, a, "bounties", "bounty", "bounty", 0, 2)) {
			bountyCmd(p, a);
			return;
		}
		/* 505 */if (did(e, p, c, a, "econpromotions", "buyrank", "buyrank", 0,
				1)) {
			buyrank(p, a.length > 0 ? a[0] : "");
			return;
		}
		/* 506 */if (did(e, p, c, a, "chunkregen", "chunkregen", "chunkregen",
				0, 0)) {
			chunkregen(p);
			return;
		}
		/* 507 */if (did(e, p, c, a, "countdown", "countdown", "countdown", 1,
				99)) {
			countdown(p, a);
			return;
		}
		/* 508 */if (did(e, p, c, a, "customwarps", "customwarp", "customwarp",
				0, 2)) {
			customwarpCmd(p, a.length > 0 ? a[0] : null, has(a, 1, "delete"));
			return;
		}
		/* 509 */if (did(e, p, c, a, "worlddate", "date", "date", 0, 1)) {
			dateCmd(p, a.length > 0 ? a[0] : null);
			return;
		}
		/* 510 */if (did(e, p, c, a, "townymods_findmyplot", "findmyplot",
				"findmyplot", 0, 2)) {
			findmyplotCmd(p, a);
			return;
		}
		/* 511 */if (did(e, p, c, a, "townymods_findplot", "findplot",
				"findplot", 0, 0)) {
			findplot(p);
			return;
		}
		/* 512 */if (did(e, p, c, a, "gamemodeall", "gmall", "gmall", 0, 99)) {
			gmall(p, a.length == 0 ? "" : a[0]);
			return;
		}
		/* 513 */if (did(e, p, c, a, "helperbot", "helperbot.admin",
				"helperbot", 1, 99)) {
			helperbot(p, a);
			return;
		}
		/* 514 */if (did(e, p, c, a, "modvote", "modvote", "modvote", 0, 1)) {
			modvote(p, a.length == 0 ? "" : a[0]);
			return;
		}
		/* 515 */if (did(e, p, c, a, "playerpasswords", "password", "password",
				1, 1)) {
			playerPassword(p, a[0]);
			return;
		}
		/* 516 */if (did(e, p, c, a, "playtime", "playtime", "playtime", 0, 2)) {
			playtimeCmd(p, a);
			return;
		}
		/* 517 */if (did(e, p, c, a, "townymods_plotsalesign", "plotsalesign",
				"plotsalesign", 0, 99)) {
			plotsalesign(p, a);
			return;
		}
		/* 518 */if (did(e, p, c, a, "regionlabels", "regionlabels.admin",
				"regionlabel", 0, 99)) {
			regionlabel(p, a);
			return;
		}
		/* 519 */if (did(e, p, c, a, "regionprotect", "regionprotect",
				"regionprotect", 0, 99)) {
			regionprotect(p, a);
			return;
		}
		/* 520 */if (did(e, p, c, a, "regionrestore", "regionrestore",
				"regionrestore", 1, 1)) {
			regionrestore(p, a[0]);
			return;
		}
		/* 521 */if (did(e, p, c, a, "regionrestore", "regionrestore",
				"regionsave", 0, 99)) {
			regionsave(p, a);
			return;
		}
		/* 522 */if (did(e, p, c, a, "serverlogarchive", "serverlogarchive",
				"serverlogarchive", 0, 0)) {
			serverlogarchive(p);
			return;
		}
		/* 523 */if (did(e, p, c, a, "modwarnings", "warn", "warn", 0, 99)) {
			warnCmd(p, a);
			return;
		}
		/* 524 */if (did(e, p, c, a, "wordswap", "wordswap", "wordswap", 0, 99)) {
			wordswap(p, a);
			return;
		}
		/* 525 */if (did(e, p, c, a, "damageditemsales", "", "smithy", 1, 99)) {
			damagedItemSales(p, a[0]);
			return;
			/*      */}
		/* 527 */if ((enabled("oponlyfromconsole"))
				&& ((c.equals("/op")) || (c.equals("/deop")))) {
			e.setCancelled(true);
			return;
		}
		/* 528 */if ((arenaActive != null) && (arenaActive.playerAlive(p)))
			for (String cmd : getConfig().getString("arena_disabledcommands")
					.split(","))
				/* 529 */if (c.equalsIgnoreCase("/" + cmd)) {
					e.setCancelled(true);
					p.sendMessage(colorize(lang("arena_disabledcommand")));
					return;
					/*      */}
		/* 531 */for (String cmd : getConfig().getString(
				"global_autokickcommands").split(","))
			if ((c.equalsIgnoreCase("/" + cmd)) && (!p.isOp())) {
				e.setCancelled(true);
				p.kickPlayer("");
				return;
			}
		/* 532 */if ((cc.equalsIgnoreCase("/worth"))
				&& (p.hasPermission("essentials.sell"))
				&& (enabled("damageditemsales"))
				&& (damagedItemFullValue(p.getItemInHand()) > 0L)) {
			/* 533 */e.setCancelled(true);
			damagedItemSales(p, "worth");
			return;
			/*      */}
		/* 535 */if ((cc.equalsIgnoreCase("/sell hand"))
				&& (p.hasPermission("essentials.worth"))
				&& (enabled("damageditemsales"))
				&& (damagedItemFullValue(p.getItemInHand()) > 0L)) {
			/* 536 */e.setCancelled(true);
			damagedItemSales(p, "sell");
			return;
			/*      */}
		/* 538 */if (cc.equalsIgnoreCase("/plot claim")) {
			/* 539 */Double restrictedPrice = Double.valueOf(getConfig()
					.getDouble("townymods_restrictedplots"));
			if (restrictedPrice.doubleValue() == -1.0D)
				return;
			/* 540 */plotPrice = (int) findplotprice(p.getLocation());
			if (((double) plotPrice) == 0.0D)
				return;
			/* 541 */boolean permsOverride = p
					.hasPermission("v.restrictedplots.buy");
			boolean buyCancelled = (restrictedPrice.doubleValue() > 0.0D)
					&& (restrictedPrice.equals(plotPrice)) && (!permsOverride);
			/* 542 */debug("[plotclaim] player [" + p.getName()
					+ "] plotPrice [" + plotPrice + "] restrictedPrice ["
					+ restrictedPrice + "] permsOverride [" + permsOverride
					+ "] buyCancelled [" + buyCancelled + "]");
			/* 543 */if (buyCancelled) {
				e.setCancelled(true);
				p.sendMessage(lang("towny_restricted"));
				return;
			}
			/* 544 */if (enabled("townymods_sethomereminder")) {
				/* 545 */getServer().getScheduler().scheduleAsyncDelayedTask(
						this, new Runnable() {
							/*      */public void run() {
								/* 547 */p.sendMessage(Vitals.colorize(Vitals.this
										.lang("towny_sethomereminder1")));
								/* 548 */p.sendMessage(Vitals.colorize(Vitals.this
										.lang("towny_sethomereminder2")));
								/* 549 */p.sendMessage(Vitals.colorize(Vitals.this
										.lang("towny_sethomereminder3")));
								/*      */}
							/*      */
						}
						/*      */, 20L);
				/*      */}
			/* 553 */return;
			/*      */}
		/* 555 */label1968: for (Object plotPrice = customWarps.keySet()
				.iterator(); ((Iterator) plotPrice).hasNext();
		/* 561 */)
		/*      */{
			/* 555 */String warp = (String) ((Iterator) plotPrice).next();
			if ((!cc.equalsIgnoreCase("/" + warp))
					|| ((!p.hasPermission("v.customwarps." + warp)) && (!p
							.hasPermission("v.customwarps.*"))))
				break label1968;
			/* 556 */if ((arenaActive != null) && (arenaActive.playerAlive(p))) {
				p.sendMessage(colorize(lang("arena_disabledcommand")));
				return;
			}
			/* 557 */p.sendMessage(colorize(lang("customwarps_action") + warp
					+ "..."));
			/* 558 */List coords = (List) customWarps.get(warp);
			/* 559 */World w = getServer().getWorld((String) coords.get(0));
			if (w == null)
				return;
			/* 560 */playerTeleport(
					p,
					new Location(w,
							Double.parseDouble((String) coords.get(1)) + 0.5D,
							Double.parseDouble((String) coords.get(2)), Double
									.parseDouble((String) coords.get(3)), Float
									.parseFloat((String) coords.get(4)), Float
									.parseFloat((String) coords.get(5))), null);
			/* 561 */e.setCancelled(true);
			/*      */}
		/* 563 */if (enabled("helperbot"))
			for (plotPrice = config("helperbot").getKeys(false).iterator(); ((Iterator) plotPrice)
					.hasNext();) {
				String wordpair = (String) ((Iterator) plotPrice).next();
				/* 564 */String[] botWords = wordpair.split("_");
				final String response = config("helperbot").getString(wordpair);
				/* 565 */if ((botWords[0].equalsIgnoreCase("command"))
						{
					/* 566 */}}
				/*      */

	/*      */
	/*      */private int abandonedminecartsStart()
	/*      */{
		/* 574 */return getServer().getScheduler().scheduleSyncRepeatingTask(
				this, new Runnable() {
					/*      */public void run() {
						/* 576 */int numMinecarts = 0;
						int numMotionless = 0;
						int numRemoved = 0;
						/*      */Iterator localIterator2;
						/* 577 */for (Iterator localIterator1 = getServer()
								.getWorlds().iterator(); localIterator1
								.hasNext();
						/* 578 */localIterator2.hasNext())
						/*      */{
							/* 577 */World world = (World) localIterator1
									.next();
							/* 578 */localIterator2 = world.getEntities()
									.iterator();
							continue;
						}
						/* 588 */if (numRemoved > 0)
							debug("[abandonedminecarts] removed "
									+ numRemoved
									+ " motionless minecarts.  "
									+ (numMinecarts - numRemoved)
									+ " minecarts remain in existence, "
									+ (numMotionless - numRemoved)
									+ " are motionless and will be removed soon if they stay motionless");
						/*      */}
					/*      */
				}
				/*      */, 1200L, 1200L);
		/*      */}

	/*      */
	/*      */private void announce(Player p, String[] args) {
		/* 594 */if (has(args, 0, "list")) {
			/* 595 */p.sendMessage(colorize("&c[Announcements List]"));
			/* 596 */if (announcements.size() == 0) {
				p.sendMessage("No announcements. To add one, type /announce add <msg>");
				return;
			}
			/* 597 */int i = 1;
			/*      */String msg;
			/* 597 */for (Iterator localIterator = announcements.iterator(); localIterator
					.hasNext(); p.sendMessage(i++ + ": " + msg))
				msg = (String) localIterator.next();
			/*      */}
		/* 598 */else if (has(args, 0, "add")) {
			/* 599 */String msg = "";
			for (int i = 1; i < args.length; i++)
				msg = msg + (msg.length() > 0 ? " " : "") + args[i];
			/* 600 */List newAnnounce = new ArrayList();
			/* 601 */for (int i = 0; i < announcements.size(); i++)
				newAnnounce.add((String) announcements.get(i));
			newAnnounce.add(msg);
			/* 602 */announcements = newAnnounce;
			p.sendMessage("Announcement added: " + msg);
			/* 603 */saveText((String[]) announcements.toArray(new String[0]),
					new File(getDataFolder(), "announcements.txt"));
			init();
			/* 604 */} else if ((has(args, 0, "edit")) && (args.length >= 2)) {
			/*      */try {
				idx = Integer.parseInt(args[1]);
			}
			/*      */catch (Exception e)
			/*      */{
				/*      */int idx;
				/* 606 */p
						.sendMessage("Wrong syntax. Should be /announce edit <id> <msg>");
				/*      */return;
				/*      */}
			/* 607 */idx = 0;
			/* 607 */String msg = "";
			for (int i = 2; i < args.length; i++)
				msg = msg + (msg.length() > 0 ? " " : "") + args[i];
			/* 608 */announcements.set(idx - 1, msg);
			p.sendMessage("Announcement changed: " + msg);
			/* 609 */saveText((String[]) announcements.toArray(new String[0]),
					new File(getDataFolder(), "announcements.txt"));
			init();
			/* 610 */} else if (has(args, 0, "remove")) {
			/*      */try {
				idx = Integer.parseInt(args[1]);
			}
			/*      */catch (Exception e)
			/*      */{
				/*      */int idx;
				/* 612 */p
						.sendMessage("Wrong syntax. Should be /announce remove <id>");
				/*      */return;
				/*      */}
			/* 614 */int idx = 0;
			/* 613 */List newAnnounce = new ArrayList();
			/* 614 */for (int i = 0; i < announcements.size(); i++)
				if (i != idx - 1)
					newAnnounce.add((String) announcements.get(i));
			/* 615 */announcements = newAnnounce;
			p.sendMessage("Announcement " + idx + " removed.");
			/* 616 */saveText((String[]) announcements.toArray(new String[0]),
					new File(getDataFolder(), "announcements.txt"));
			init();
			/*      */}
		/*      */}

	/*      */
	/* 620 */private int announceStart() {
		long announceInterval = getConfig().getLong("announcements_interval");
		/* 621 */if (announceInterval < 10L) {
			log.severe(toString()
					+ " could not enable Announcements: interval must be at least 10 seconds to prevent spamming your server.");
			return -1;
		}
		/* 622 */return getServer().getScheduler().scheduleAsyncRepeatingTask(
				this, new Runnable() {
					/* 623 */private int lastAnnouncement = -1;

					/*      */
					/* 625 */public void run() {
						if (Vitals.this.enabled("announcements.random")) {
							/* 626 */lastAnnouncement = Vitals.random
									.nextInt(announcements.size());
							/*      */}
						/* 628 */else if (++lastAnnouncement > announcements
								.size() - 1)
							lastAnnouncement = 0;
						/*      */
						/* 630 */String announcement = getConfig().getString(
								"announcements_prefix")
								+ (String) announcements.get(lastAnnouncement);
						/* 631 */getServer().broadcastMessage(
								Vitals.colorize(announcement));
						/*      */}
					/*      */
				}
				/*      */, 600L, announceInterval * 20L);
	}

	/*      */
	/*      */private int antiovercrowdingStart()
	/*      */{
		/* 637 */return getServer().getScheduler().scheduleSyncRepeatingTask(
				this, new Runnable() {
					/* 638 */final int threshold = getConfig().getInt(
							"antiovercrowding_maxentities");

					/*      */
					/* 640 */public void run() {
						int numRemoved = 0;
						int max = 0;
						/*      */Iterator localIterator2;
						/* 641 */for (Iterator localIterator1 = getServer()
								.getWorlds().iterator(); localIterator1
								.hasNext();
						/* 642 */localIterator2.hasNext())
						/*      */{
							/* 641 */World w = (World) localIterator1.next();
							/* 642 */localIterator2 = w.getEntities()
									.iterator();
							continue;
							Entity e = (Entity) localIterator2.next();
							/* 643 */int nearby = e.getNearbyEntities(9.0D,
									9.0D, 9.0D).size();
							/* 644 */if ((nearby > threshold)
									&& ((max == 0) || (numRemoved < max))
									&& (!(e instanceof Player))) {
								/* 645 */if (max == 0)
									max = (int) (nearby - threshold * 0.8D);
								numRemoved++;
								e.remove();
								/*      */}
							/*      */}
						/*      */
						/* 649 */if (numRemoved > 0)
							debug("[antiovercrowding] removed " + numRemoved
									+ " entities");
						/*      */}
					/*      */
				}
				/*      */, 20L, 300L);
		/*      */}

	/*      */
	/*      */private void arena(Player p, String[] args) {
		/* 655 */if ((has(args, 0, "list")) && (auth(p, "arena.admin"))) {
			/* 656 */p.sendMessage(colorize("&c[Current Arena Setup]"));
			/* 657 */for (String arena : config("arena")
					.getConfigurationSection("arena").getKeys(false))
				/* 658 */p.sendMessage(arena
						+ " "
						+ config("arena").getConfigurationSection(
								new StringBuilder("arena.").append(arena)
										.toString()).getKeys(false));
			/* 659 */return;
			/*      */}
		/* 661 */if ((has(args, 0, "end")) && (arenaActive != null)
				&& (auth(p, "arena.admin"))) {
			arenaActive.eventEnd(true);
			return;
		}
		/* 662 */if ((has(args, 0, "delete")) && (auth(p, "arena.admin"))) {
			/* 663 */if (eventActive != null) {
				p.sendMessage("Can't delete an arena while an event is running. ("
						+ eventActive + ")");
				return;
			}
			/* 664 */if (!config("arena").getConfigurationSection("arena")
					.getKeys(false).contains(args[1])) {
				p.sendMessage("No arena by that name.");
				return;
			}
			/* 665 */config("arena").set("arena." + args[1], null);
			saveConfig("arena");
			p.sendMessage("Arena '" + args[1] + "' has been deleted.");
			return;
			/*      */}
		/* 667 */if ((has(args, 0, "start")) && (auth(p, "arena.admin"))) {
			/* 668 */if (eventActive != null) {
				p.sendMessage("Can't start an arena event while another event is running. ("
						+ eventActive + ")");
				return;
			}
			/* 669 */if (args.length >= 3)
				arenaBegin(args[1], Integer.parseInt(args[2]));
			else if (args.length == 2)
				arenaBegin(args[1], -1);
			else
				arenaBegin("", -1);
			p.sendMessage("Arena match initiated.");
			/*      */}
		/* 671 */if ((has(args, 0, "setup")) && (auth(p, "arena.admin"))) {
			/* 672 */if (eventActive != null) {
				p.sendMessage("Can't setup an arena while an event is running. ("
						+ eventActive + ")");
				return;
			}
			/* 673 */eventActive = "arenasetup";
			arenaActive = new Arena(this, args[1], p);
			return;
			/*      */}
		/* 675 */if ((args.length == 0) && (arenaActive != null)
				&& (arenaActive.state.equals("signup"))) {
			arenaActive.playerSignup(p);
			return;
		}
		/* 676 */if ((arenaActive != null)
				&& ((args.length == 0) || (has(args, 0, "info")))) {
			arenaActive.info(p);
			return;
		}
		/* 677 */if ((arenaActive == null)
				&& ((args.length == 0) || (has(args, 0, "info")))) {
			p.sendMessage(colorize(lang("arena_signupinfo")
					+ (getConfig().getInt("arena_minutesbetweengames") - (new Date()
							.getTime() - arenaTime) / 60000L) + " minutes"));
			return;
		}
		/*      */}

	/*      */
	/* 680 */private int arenaStart() {
		return getServer().getScheduler().scheduleSyncRepeatingTask(this,
				new Runnable() {
					/*      */public void run() {
						/* 682 */if ((eventActive == null)
								&& (arenaActive == null)
								&& ((new Date().getTime() - arenaTime) / 1000L >= 60 * getConfig()
										.getInt("arena_minutesbetweengames"))) {
							/* 683 */if (getServer().getOnlinePlayers().length >= getConfig()
									.getInt("arena_minimumplayers")) {
								Vitals.this.arenaBegin("", -1);
								return;
							}
							/* 684 */broadcastEvent("&7[Arena] Not enough players online to start an arena match.");
							/* 685 */broadcastEvent("&7The next arena signup period will begin in "
									+ getConfig().getInt(
											"arena_minutesbetweengames")
									+ " minutes.");
							/* 686 */arenaTime = new Date().getTime();
							/*      */}
						/* 688 */if ((arenaActive != null)
								&& (arenaActive.state.equals("end"))) {
							eventActive = null;
							arenaActive = null;
							arenaTime = new Date().getTime();
							/*      */}
						/*      */}
					/*      */
				}
				/*      */, 60L, 60L);
	}

	/*      */
	/*      */private void arenaBegin(String arenaType, int arenaNumber) {
		/* 693 */eventActive = "arena";
		arenaActive = new Arena(this, arenaType, arenaNumber);
		/* 694 */if ((arenaActive.event.equals("HungerGames"))
				&& (enabled("arena_hungergamesregionrestore"))
				&& (new File(getDataFolder(), "regionrestore_hungergames1.yml")
						.exists())) {
			regionrestore(null, "hungergames1");
			debug("[arena] hungergames1 region was restored");
		}
		/*      */}

	/*      */
	/*      */private void auction(final Player p, String[] args) {
		/* 698 */double auctionFee = getConfig().getDouble("auctions_fee");
		/* 699 */long auctionLastDiff = (new Date().getTime() - auctionLast) / 1000L;
		/* 700 */long auctionDelay = getConfig().getLong(
				"auctions_timebetweenauctions");
		/* 701 */if (auctionItem != null) {
			p.sendMessage(colorize(lang("auction_failactive")));
			return;
		}
		/* 702 */if (eventActive != null) {
			p.sendMessage(colorize(lang("auction_failevent") + " ("
					+ eventActive + ")"));
			return;
		}
		/* 703 */if (auctionLastDiff < auctionDelay) {
			p.sendMessage(colorize(lang("auction_faildelay")
					+ (auctionDelay - auctionLastDiff) + " seconds."));
			return;
		}
		/* 704 */if (!econ.has(p.getName(), auctionFee)) {
			p.sendMessage(colorize(lang("auction_failmoney") + auctionFee));
			return;
		}
		/* 705 */if ((auctionStarter == p)
				&& (!p.hasPermission("v.auction.skipqueue"))) {
			p.sendMessage(colorize(lang("auction_failqueue")));
			return;
		}
		/* 706 */ItemStack item = p.getItemInHand();
		/* 707 */if ((item == null) || (item.getType() == Material.AIR)) {
			p.sendMessage(colorize(lang("auction_failitem")));
			return;
		}
		/* 708 */String desc = item.getAmount() + " "
				+ item.getType().toString().toLowerCase().replaceAll("_", " ");
		/* 709 */if ((item.getType().getMaxDurability() > 0)
				&& (item.getDurability() > 0))
			desc = desc
					+ " ("
					+ Math.round(100.0D * item.getDurability()
							/ item.getType().getMaxDurability()) + "% damaged)";
		/* 710 */Map enchants = item.getEnchantments();
		/*      */Enchantment e;
		/* 711 */for (Iterator localIterator = enchants.keySet().iterator(); localIterator
				.hasNext(); desc = desc + " ("
				+ e.getName().toLowerCase().replaceAll("_", " ") + " "
				+ enchants.get(e) + ")")
			e = (Enchantment) localIterator.next();
		/* 712 */if ((args.length == 1)
				&& (p.hasPermission("v.auction.startingbid"))) {
			/*      */try {
				auctionBid = Integer.parseInt(args[0]);
			} catch (Exception event) {
				/* 714 */p.sendMessage(lang("auction_failstartingbid"));
				return;
				/* 715 */}
			if (auctionBid < 0) {
				p.sendMessage(lang("auction_failstartingbid"));
				auctionBid = 0;
				return;
			}
			/*      */}
		/* 717 */eventActive = "auction";
		final String itemDescription = desc;
		auctionItem = item;
		auctionStarter = p;
		p.setItemInHand(null);
		econ.withdrawPlayer(p.getName(), auctionFee);
		/* 718 */p.sendMessage(colorize(lang("auction_success") + " ($"
				+ auctionFee + ")"));
		/* 719 */tasks.put("auction", Integer.valueOf(getServer()
				.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
					/* 720 */int timeLeft = getConfig().getInt(
							"auctions_duration");
					int lastBid = auctionBid;

					/*      */
					/* 722 */public void run() {
						if ((Vitals.this.enabled("auctions_preventsniping"))
								&& (lastBid != auctionBid)) {
							lastBid = auctionBid;
							if (timeLeft < 45)
								timeLeft = 45;
						}
						/*      */
						/* 723 */if ((timeLeft > 0)
								&& (timeLeft
										% getConfig().getInt(
												"auctions_announceinterval") == 0)) {
							/* 724 */broadcastEvent("&b[Auction]&7 "
									+ p.getName() + " is auctioning &b"
									+ itemDescription);
							/* 725 */broadcastEvent("&7High bid: &a$"
									+ auctionBid + " &7Time left: &a"
									+ timeLeft
									+ "s &7To bid type: &a/bid [amount]");
							/*      */}
						/* 727 */if (timeLeft-- <= 0)
							Vitals.this.auctionfinish();
						/*      */}
					/*      */
				}
				/*      */, 20L, 20L)));
		/*      */}

	/*      */
	/*      */private void auctionbid(Player p, String bid) {
		/* 733 */if (auctionItem == null) {
			p.sendMessage(colorize(lang("auctionbid_failactive")));
			return;
		}
		/* 734 */if ((auctionStarter == p) && (!p.isOp())) {
			p.sendMessage(colorize(lang("auctionbid_failstarter")));
			return;
		}
		/* 735 */if (auctionBidder == p) {
			p.sendMessage(colorize(lang("auctionbid_failbidder")));
			return;
		}
		/* 736 */int bidAmount = 0;
		/*      */try {
			bidAmount = Integer.parseInt(bid);
		} catch (Exception e) {
			p.sendMessage(colorize(lang("auctionbid_failnumber")));
			return;
		}
		/* 737 */if (bidAmount <= auctionBid) {
			p.sendMessage(colorize(lang("auctionbid_failtoolow") + auctionBid));
			return;
		}
		/* 738 */if (!econ.has(p.getName(), bidAmount)) {
			p.sendMessage(colorize(lang("auctionbid_failmoney")));
			return;
		}
		/* 739 */if (auctionBidder != null) {
			econ.depositPlayer(auctionBidder.getName(), auctionBid);
			auctionBidder
					.sendMessage(colorize(lang("auctionbid_outbid") + bid));
		}
		/* 740 */econ.withdrawPlayer(p.getName(), bidAmount);
		auctionBid = bidAmount;
		auctionBidder = p;
		p.sendMessage(colorize(lang("auctionbid_success")));
		/*      */}

	/*      */
	/*      */private void auctionfinish() {
		/* 744 */final Player winner = auctionBidder;
		final ItemStack item = auctionItem;
		/* 745 */getServer().getScheduler().cancelTask(
				((Integer) tasks.get("auction")).intValue());
		tasks.remove("auction");
		auctionLast = new Date().getTime();
		/* 746 */if (auctionBidder == null) {
			/* 747 */broadcastEvent(lang("auction_nobids"));
			/* 748 */auctionStarter.getInventory().addItem(
					new ItemStack[] { auctionItem });
			auctionStarter.sendMessage(colorize("&cYou received "
					+ auctionItem.toString()));
			/*      */} else {
			/* 750 */broadcastEvent(lang("auction_sold") + auctionBid + " by "
					+ auctionBidder.getName() + "!");
			/* 751 */econ.depositPlayer(auctionStarter.getName(), auctionBid);
			auctionStarter
					.sendMessage(colorize("&cYou received $" + auctionBid));
			/* 752 */if (auctionBidder.getInventory().firstEmpty() != -1) {
				/* 753 */auctionBidder.getInventory().addItem(
						new ItemStack[] { auctionItem });
				auctionBidder.sendMessage(colorize("&cYou received "
						+ auctionItem.toString()));
				/*      */} else {
				/* 755 */auctionBidder
						.sendMessage(colorize(lang("auction_nospace")));
				/* 756 */getServer().getScheduler().scheduleSyncDelayedTask(
						this, new Runnable() {
							/*      */public void run() {
								/* 758 */if (winner.getInventory().firstEmpty() != -1) {
									winner.getInventory().addItem(
											new ItemStack[] { item });
									winner.sendMessage(Vitals
											.colorize("&cYou received "
													+ item.toString()));
								} else {
									/* 759 */winner.sendMessage(Vitals
											.colorize(Vitals.this
													.lang("auction_itemlost")));
									/*      */}
								/*      */}
							/*      */
						}
						/*      */, 1200L);
				/*      */}
			/*      */}
		/*      */
		/* 765 */String shortDesc = auctionItem.getType().toString()
				.toLowerCase().replaceAll("_", "")
				+ "(" + auctionItem.getAmount() + ")";
		/* 766 */logEvent("Auction", auctionStarter.getName() + " auctioned "
				+ shortDesc + " to "
				+ (auctionBidder == null ? "no one" : auctionBidder.getName())
				+ " for $" + auctionBid);
		/* 767 */auctionItem = null;
		auctionBid = 0;
		auctionBidder = null;
		eventActive = null;
		/*      */}

	/*      */
	/*      */
	/*      */private void betterhelpCmd(Player p, String[] args) {
		/* 779 */List basicCommands = Arrays.asList(getConfig().getString(
				"betterhelp_basiccommands").split(","));
		/* 780 */List advancedCommands = Arrays.asList(getConfig().getString(
				"betterhelp_advancedcommands").split(","));
		/*      */Iterator localIterator1;
		/* 781 */if (args.length == 0) {
			/* 782 */p
					.sendMessage(colorize("&c---- Help: Basic Commands ----"));
			/* 783 */String s = "";
			/*      */String cmd;
			/* 783 */for (localIterator1 = basicCommands.iterator(); localIterator1
					.hasNext(); s = s + (s.length() > 0 ? "&f, " : "") + "&6/"
					+ cmd)
				cmd = (String) localIterator1.next();
			p.sendMessage(colorize(s));
			/* 784 */p
					.sendMessage(colorize("&cTo see how to use a command, type: /help [commandname]"));
			/* 785 */p
					.sendMessage(colorize("&cTo see advanced commands, type: /help advanced"));
			/* 786 */} else if (has(args, 0, "advanced")) {
			/* 787 */p
					.sendMessage(colorize("&c---- Help: Advanced Commands ----"));
			/* 788 */String s = "";
			/*      */String cmd;
			/* 788 */for (localIterator1 = advancedCommands.iterator(); localIterator1
					.hasNext(); s = s + (s.length() > 0 ? "&f, " : "") + "&6/"
					+ cmd)
				cmd = (String) localIterator1.next();
			p.sendMessage(colorize(s));
			/* 789 */} else if (args.length == 1) {
			/* 790 */String cmd = args[0].toLowerCase();
			/* 791 */if ((basicCommands.contains(cmd))
					|| (advancedCommands.contains(cmd)))
				/* 792 */betterhelpInfo(p, cmd);
			/* 793 */else if ((cmd.substring(0, 1).equals("/"))
					&& (cmd.length() > 1)
					&& ((basicCommands.contains(cmd.substring(1))) || (advancedCommands
							.contains(cmd.substring(1)))))
				/* 794 */betterhelpInfo(p, cmd.substring(1));
			/*      */else
				/* 796 */p.sendMessage(lang("betterhelp_unknown"));
			/*      */}
		/* 798 */else if (((has(args, 0, "addbasic"))
				|| (has(args, 0, "addadvanced")) || (has(args, 0, "remove")))
				&& (auth(p, "help.admin"))) {
			/* 799 */String cmd = args[1].toLowerCase();
			/* 800 */String b = "";
			/*      */String c;
			/* 800 */label587: for (Iterator localIterator2 = basicCommands
					.iterator(); localIterator2.hasNext(); b = b
					+ (b.length() > 0 ? "," : "") + c) {
				c = (String) localIterator2.next();
				if ((has(args, 0, "remove")) && (c.equals(cmd)))
					break label587;
			}
			/* 801 */String a = "";
			/*      */String c11;
			/* 801 */label690: for (Iterator localIterator3 = advancedCommands
					.iterator(); localIterator3.hasNext(); a = a
					+ (a.length() > 0 ? "," : "") + c11) {
				c11 = (String) localIterator3.next();
				if ((has(args, 0, "remove")) && (c11.equals(cmd)))
					break label690;
			}
			/* 802 */if (has(args, 0, "addbasic"))
				b = b + (b.length() > 0 ? "," : "") + cmd;
			/* 803 */if (has(args, 0, "addadvanced"))
				a = a + (a.length() > 0 ? "," : "") + cmd;
			/* 804 */getConfig().set("betterhelp_basiccommands", b);
			getConfig().set("betterhelp_advancedcommands", a);
			writeConfig();
			/* 805 */p.sendMessage("Changed saved.");
			/*      */} else {
			/* 807 */showUsage(p, "help");
			/*      */}
		/*      */}

	/*      */
	/* 811 */private void betterhelpInfo(Player p, String cmd) {
		PluginCommand pcmd = getServer().getPluginCommand(cmd);
		/* 812 */if (pcmd == null) {
			/* 813 */if (!showUsage(p, cmd))
				p.sendMessage(lang("betterhelp_unknown"));
			return;
			/*      */}
		/* 815 */p.sendMessage(colorize("&e" + cmd + " - "
				+ pcmd.getDescription()));
		/* 816 */p.sendMessage(colorize("&eUsage: "
				+ pcmd.getUsage().replaceAll("<command>", cmd)));
	}

	/*      */
	/*      */private void betternews(Player p, int numToShow)
	/*      */{
		/* 820 */if (numToShow < 1)
			return;
		p.sendMessage(colorize("&c[LATEST NEWS]"));
		showLatestFileEntries(p, "news.txt", numToShow);
		/*      */}

	/*      */private void betternewsCmd(Player p, String[] args) {
		/* 823 */File newsFile = new File(getDataFolder(), "news.txt");
		/* 824 */String entry = new SimpleDateFormat("MM-dd-yyyy")
				.format(new Date()) + ": ";
		for (int i = 1; i < args.length; i++)
			entry = entry + args[i] + " ";
		/* 825 */if ((args.length == 0) || (!p.hasPermission("v.news.admin"))) {
			/* 826 */betternews(p,
					getConfig().getInt("betternews_showoncommand"));
			/* 827 */} else if (args[0].equalsIgnoreCase("add")) {
			/* 828 */if (newsFile.exists())
				saveText(loadText(newsFile) + entry + "\n", newsFile);
			else
				saveText(entry + "\n", newsFile);
			/* 829 */getServer()
					.broadcastMessage(colorize("&c[NEWS] " + entry));
			p.sendMessage("News entry saved and broadcasted.");
			/* 830 */} else if (args[0].equalsIgnoreCase("edit")) {
			/* 831 */if (!newsFile.exists()) {
				p.sendMessage("There are no news entries.");
				return;
			}
			/* 832 */String newNews = "";
			String[] news = loadText(newsFile).split("\n");
			for (int i = 0; i < news.length - 1; i++)
				newNews = newNews + news[i] + "\n";
			/* 833 */saveText(newNews + entry + "\n", newsFile);
			p.sendMessage("Latest news entry changed.");
			/* 834 */} else if (args[0].equalsIgnoreCase("delete")) {
			/* 835 */if (!newsFile.exists()) {
				p.sendMessage("There are no news entries.");
				return;
			}
			/* 836 */String newNews = "";
			String[] news = loadText(newsFile).split("\n");
			for (int i = 0; i < news.length - 1; i++)
				newNews = newNews + news[i] + "\n";
			/* 837 */saveText(newNews, newsFile);
			p.sendMessage("Latest news entry deleted.");
			/*      */} else {
			/* 839 */showUsage(p, "news");
			/*      */}
		/*      */}

	/*      */
	/*      */private void bountyCmd(Player p, String[] args) {
		/* 844 */if (args.length == 0) {
			/* 845 */p.sendMessage(colorize("&6[Most Wanted]"));
			/* 846 */Set bounties = config("bounties").getKeys(false);
			/* 847 */if (bounties.size() == 0) {
				p.sendMessage(lang("bounty_noneactive"));
				return;
			}
			/* 848 */HashMap map = new HashMap();
			/* 849 */for (String pName : bounties)
				if (config("bounties").getLong(pName + ".amount") > 0L) {
					/* 850 */map.put(
							pName,
							Double.valueOf(config("bounties").getLong(
									pName + ".amount")));
					/*      */}
			/* 852 */TreeMap sorted_map = mapSort(map);
			/* 853 */int numShown = 0;
			int numToShow = 9;
			/* 854 */for (String key : sorted_map.keySet())
				if (numShown++ < numToShow) {
					/* 855 */p.sendMessage("$"
							+ Math.round(((Double) sorted_map.get(key))
									.doubleValue()) + " reward for killing "
							+ key);
					/*      */}
			/* 857 */return;
			/* 858 */}
		if ((args.length == 2) && (auth(p, "bounty.buy"))) {
			/* 859 */int amount = 0;
			/*      */try {
				amount = Integer.parseInt(args[1]);
			} catch (Exception e) {
				/* 861 */p.sendMessage(lang("bounty_failnumber"));
				return;
				/* 862 */}
			if (amount < 0) {
				p.sendMessage(lang("bounty_failnumber"));
				return;
			}
			/* 863 */if ((getConfig().getLong("bounties_minimumnewbounty") > 0L)
					&& (amount < getConfig().getLong(
							"bounties_minimumnewbounty"))) {
				p.sendMessage(lang("bounty_failtoolow")
						+ getConfig().getLong("bounties_minimumnewbounty"));
				return;
			}
			/* 864 */if (amount > econ.getBalance(p.getName())) {
				p.sendMessage("You don't have that much money.");
				return;
			}
			/* 865 */econ.withdrawPlayer(p.getName(), amount);
			/* 866 */config("bounties").set(
					args[0].toLowerCase() + ".amount",
					Long.valueOf(config("bounties").getLong(
							args[0].toLowerCase() + ".amount", 0L)
							+ amount));
			/* 867 */saveConfig("bounties");
			p.sendMessage(lang("bounty_success"));
			/* 868 */broadcastEvent("&5[Bounty]&d " + p.getName()
					+ lang("bounty_announcenew") + amount + "!");
			/* 869 */broadcastEvent("&5[Bounty]&d $"
					+ config("bounties").getLong(
							new StringBuilder(String.valueOf(args[0]
									.toLowerCase())).append(".amount")
									.toString()) + lang("bounty_announcetotal")
					+ args[0].toLowerCase() + "!");
			/* 870 */logEvent("Bounty", p.getName() + " purchased a $" + amount
					+ " bounty on " + args[0].toLowerCase());
			/*      */} else {
			/* 872 */showUsage(p, "bounty");
			/*      */}
		/*      */}

	/*      */
	/* 876 */private void bountyDeath(final String k, final String p) {
		final long bounty = config("bounties").getLong(p + ".amount", 0L);
		/* 877 */if (bounty > 0L) {
			/* 878 */econ.depositPlayer(k, bounty);
			/* 879 */getServer().getScheduler().scheduleAsyncDelayedTask(this,
					new Runnable() {
						/* 880 */public void run() {
							broadcastEvent("&5[Bounty]&d " + k
									+ Vitals.this.lang("bounty_announcekill")
									+ p + "! ($" + bounty + ")");
						}
						/*      */
						/*      */
					}
					/*      */, 20L);
			/* 882 */config("bounties").set(p, null);
			/* 883 */config("bounties").set(p + ".cooldown",
					Long.valueOf(new Date().getTime()));
			/* 884 */saveConfig("bounties");
			/* 885 */logEvent("Bounty", k + " was awarded $" + bounty
					+ " for killing " + p);
			return;
			/*      */}
		/* 887 */List kills = config("bounties").getStringList(k + ".kills");
		/* 888 */long cooldown = config("bounties")
				.getLong(k + ".cooldown", 0L);
		/* 889 */long cooldownRemaining = getConfig().getLong(
				"bounties_cooldownminutes")
				- (new Date().getTime() - cooldown) / 60000L;
		/* 890 */if (cooldownRemaining > 0L) {
			/* 891 */debug("[bounty] " + k + " killed " + p
					+ ": no bounty set for " + k
					+ " because of active cooldown, " + cooldownRemaining
					+ " minutes left");
			/* 892 */} else if ((kills != null) && (kills.contains(p))) {
			/* 893 */debug("[bounty] "
					+ k
					+ " killed "
					+ p
					+ ": no bounty set for "
					+ k
					+ " because the victim was already on the killer's victim list");
			/*      */} else {
			/* 895 */debug("[bounty] " + k + " killed " + p
					+ ": no cooldown or cooldown expired on " + k
					+ " so this is a valid bounty");
			/* 896 */config("bounties").set(
					k + ".amount",
					Long.valueOf(config("bounties").getLong(k + ".amount", 0L)
							+ getConfig().getLong("bounties_amountperkill")));
			/* 897 */if (kills == null)
				kills = new ArrayList();
			/* 898 */kills.add(p);
			config("bounties").set(k + ".kills", kills);
			saveConfig("bounties");
			/* 899 */getServer().getScheduler().scheduleAsyncDelayedTask(this,
					new Runnable() {
						/* 900 */public void run() {
							broadcastEvent("&5[Bounty]&d New bounty! $"
									+ config("bounties")
											.getLong(
													new StringBuilder(String
															.valueOf(k))
															.append(".amount")
															.toString())
									+ Vitals.this.lang("bounty_announcetotal")
									+ k + "!");
						}
						/*      */
						/*      */
					}
					/*      */, 20L);
			/*      */}
		/*      */}

	/*      */
	/*      */private void buyrank(Player p, String rank)
	/*      */{
		/*      */Iterator localIterator;
		/* 906 */if (rank.length() == 0) {
			/* 907 */p.sendMessage(colorize("&c[Available Ranks]"));
			/*      */Double cost;
			/*      */String[] rInfo;
			/* 908 */label168: for (localIterator = Arrays.asList(
					getConfig().getString("econpromotions_ranks").split(";"))
					.iterator(); localIterator.hasNext();
			/* 912 */p.sendMessage(rInfo[1] + " - $" + cost))
			/*      */{
				/* 908 */String eachrank = (String) localIterator.next();
				/*      */try {
					/* 910 */String[] rInfo = eachrank.split(",");
					cost = Double.valueOf(Double.parseDouble(rInfo[2]));
					/*      */}
				/*      */catch (Exception e)
				/*      */{
					/*      */Double cost;
					/* 911 */String err = "Invalid VITALS configuration entry: econpromotions_ranks";
					log.severe(err);
					p.sendMessage(err);
					return;
					/* 912 */}
				if ((rInfo[0].length() != 0)
						&& (!perms.getPrimaryGroup(p)
								.equalsIgnoreCase(rInfo[0])))
					break label168;
				/*      */}
			/*      */} else {
			/* 915 */for (String eachrank : Arrays.asList(getConfig()
					.getString("econpromotions_ranks").split(";"))) {
				/*      */try {
					/* 917 */String[] rInfo = eachrank.split(",");
					Double cost = Double.valueOf(Double.parseDouble(rInfo[2]));
					/*      */}
				/*      */catch (Exception e)
				/*      */{
					/*      */Double cost;
					/* 918 */String err = "Invalid VITALS configuration entry: econpromotions_ranks";
					log.severe(err);
					p.sendMessage(err);
					/*      */return;
					/*      */}
				/*      */Double cost;
				/*      */String[] rInfo;
				/* 919 */if (rank.equalsIgnoreCase(rInfo[1])) {
					/* 920 */if ((rInfo[0].length() > 0)
							&& (!perms.getPrimaryGroup(p).equalsIgnoreCase(
									rInfo[0]))) {
						p.sendMessage(colorize("&fYou have to be rank &a"
								+ rInfo[0] + "&f to buy that promotion."));
						return;
					}
					/* 921 */if (!econ.has(p.getName(), cost.doubleValue())) {
						p.sendMessage("You don't have enough money (" + cost
								+ ") to buy that promotion.");
						return;
					}
					/* 922 */econ.withdrawPlayer(p.getName(),
							cost.doubleValue());
					perms.playerAddGroup(p, rInfo[1]);
					perms.playerRemoveGroup(p, rInfo[0]);
					/* 923 */p
							.sendMessage(colorize("&fCongratulations, you purchased the rank &a"
									+ rInfo[1] + "&f!"));
					/* 924 */logEvent("Buyrank", p.getName()
							+ " bought the rank " + rInfo[1] + " for $" + cost);
					/*      */}
				/*      */}
			/*      */}
		/*      */}

	/*      */
	/*      */private void chunkregen(Player p) {
		/* 931 */Chunk c = p.getLocation().getChunk();
		/* 932 */c.getWorld().regenerateChunk(c.getX(), c.getZ());
		/* 933 */if (getConfig().getBoolean("chunkregen_clearabove")) {
			/* 934 */for (int x = 0; x < 16; x++)
				for (int z = 0; z < 16; z++)
					for (int y = p.getLocation().getBlockY(); y < c.getWorld()
							.getMaxHeight(); y++) {
						/* 935 */c.getBlock(x, y, z).setType(Material.AIR);
						/*      */}
			/*      */}
		/* 938 */c.getWorld().refreshChunk(c.getX(), c.getZ());
		/* 939 */p.sendMessage("Chunk regenerated.");
		/*      */}

	/*      */
	/*      */private void countdown(Player p, String[] args) {
		/* 943 */if (countdownTask != -1) {
			if (has(args, 0, "stop")) {
				countdownCancel();
				getServer()
						.broadcastMessage(colorize("&c[Countdown stopped.]"));
			} else {
				p.sendMessage("There is a countdown already running.");
			}
			return;
		}
		/*      */try {
			/* 945 */seconds = Integer.parseInt(args[0]);
			/*      */}
		/*      */catch (Exception e)
		/*      */{
			/*      */int seconds;
			/* 946 */p
					.sendMessage("You must specific a positive whole number for the countdown (in seconds).");
			/*      */return;
			/*      */}
		/* 947 */int seconds;
		/* 947 */if (seconds < 1) {
			p.sendMessage("You must specific a positive whole number for the countdown (in seconds).");
			return;
		}
		/* 948 */String s = "";
		for (int i = 1; i < args.length; i++)
			s = s + (i > 1 ? " " : "") + args[i];
		/* 949 */int timer = seconds;
		final String msg = s;
		/* 950 */countdownTask = getServer().getScheduler()
				.scheduleSyncRepeatingTask(this, new Runnable() {
					/*      */int clock;

					/*      */
					/* 953 */public void run() {
						if ((clock % 30 == 0)
								|| ((clock < 60) && ((clock % 10 == 0) || (clock < 10)))) {
							/* 954 */String ss = "0" + clock % 60;
							ss = ss.substring(ss.length() - 2, ss.length());
							/* 955 */getServer().broadcastMessage(
									Vitals.colorize("&c[" + clock / 60 + ":"
											+ ss + "] &6" + msg));
							/*      */}
						/* 957 */clock -= 1;
						/* 958 */if (clock < 0)
							Vitals.this.countdownCancel();
						/*      */}
					/*      */
				}
				/*      */, 0L, 20L);
		/*      */}

	/* 962 */private void countdownCancel() {
		getServer().getScheduler().cancelTask(countdownTask);
		countdownTask = -1;
	}

	/*      */
	/*      */private void customwarpCmd(Player p, String warp, boolean delete) {
		/* 965 */if (warp == null) {
			/* 966 */p.sendMessage(colorize("&c[Custom Warps]"));
			/*      */String w;
			/* 966 */for (Iterator localIterator = config("customwarps")
					.getKeys(false).iterator(); localIterator.hasNext(); p
					.sendMessage(w + ": " + config("customwarps").getString(w)))
				w = (String) localIterator.next();
			/*      */}
		/* 967 */else if (delete) {
			/* 968 */if (config("customwarps").get(warp) == null) {
				p.sendMessage("No custom warp by that name.");
				return;
			}
			/* 969 */config("customwarps").set(warp, null);
			saveConfig("customwarps");
			init();
			p.sendMessage("Custom warp deleted.");
			/*      */} else {
			/* 971 */Location l = p.getLocation();
			DecimalFormat df = new DecimalFormat("#.##");
			/* 972 */String x = df.format(l.getX());
			String y = df.format(l.getY());
			String z = df.format(l.getZ());
			String yaw = df.format(l.getYaw());
			String pitch = df.format(l.getPitch());
			/* 973 */config("customwarps").set(
					warp,
					l.getWorld().getName() + "_" + x + "_" + y + "_" + z + "_"
							+ yaw + "_" + pitch);
			/* 974 */saveConfig("customwarps");
			init();
			p.sendMessage("Custom warp '" + warp + "' saved.");
			/*      */}
		/*      */}

	/*      */
	/*      */private long damagedItemFullValue(ItemStack item) {
		/* 979 */int type = item.getTypeId();
		/* 980 */String material = "";
		long fullValue = 0L;
		/* 981 */if ((type == 268) || (type == 269) || (type == 270)
				|| (type == 271) || (type == 290))
			material = "wood";
		/* 982 */if ((type == 272) || (type == 273) || (type == 274)
				|| (type == 275) || (type == 291))
			material = "stone";
		/* 983 */if ((type == 298) || (type == 299) || (type == 300)
				|| (type == 301))
			material = "leather";
		/* 984 */if ((type == 302) || (type == 303) || (type == 304)
				|| (type == 305))
			material = "fire";
		/* 985 */if ((type == 306) || (type == 307) || (type == 308)
				|| (type == 309) || (type == 256) || (type == 257)
				|| (type == 258) || (type == 267) || (type == 292)
				|| (type == 259) || (type == 359))
			material = "ironingot";
		/* 986 */if ((type == 310) || (type == 311) || (type == 312)
				|| (type == 313) || (type == 276) || (type == 277)
				|| (type == 278) || (type == 279) || (type == 293))
			material = "diamond";
		/* 987 */if ((type == 314) || (type == 315) || (type == 316)
				|| (type == 317) || (type == 283) || (type == 284)
				|| (type == 285) || (type == 286) || (type == 294))
			material = "goldingot";
		/* 988 */if ((type == 261) || (type == 346))
			material = "string";
		/* 989 */if ((type == 298) || (type == 302) || (type == 306)
				|| (type == 310) || (type == 314))
			fullValue = 5L * getConfig().getLong(
					"damageditemsales_" + material + "value");
		/* 990 */if ((type == 299) || (type == 303) || (type == 307)
				|| (type == 311) || (type == 315))
			fullValue = 8L * getConfig().getLong(
					"damageditemsales_" + material + "value");
		/* 991 */if ((type == 300) || (type == 304) || (type == 308)
				|| (type == 312) || (type == 316))
			fullValue = 7L * getConfig().getLong(
					"damageditemsales_" + material + "value");
		/* 992 */if ((type == 301) || (type == 305) || (type == 309)
				|| (type == 313) || (type == 317))
			fullValue = 4L * getConfig().getLong(
					"damageditemsales_" + material + "value");
		/* 993 */if ((type == 258) || (type == 271) || (type == 275)
				|| (type == 279) || (type == 286))
			fullValue = 3L * getConfig().getLong(
					"damageditemsales_" + material + "value");
		/* 994 */if ((type == 257) || (type == 270) || (type == 274)
				|| (type == 278) || (type == 285) || (type == 261))
			fullValue = 3L * getConfig().getLong(
					"damageditemsales_" + material + "value");
		/* 995 */if ((type == 256) || (type == 269) || (type == 273)
				|| (type == 277) || (type == 284) || (type == 259))
			fullValue = getConfig().getLong(
					"damageditemsales_" + material + "value");
		/* 996 */if ((type == 290) || (type == 291) || (type == 292)
				|| (type == 293) || (type == 294) || (type == 359)
				|| (type == 346))
			fullValue = 2L * getConfig().getLong(
					"damageditemsales_" + material + "value");
		/* 997 */if ((type == 267) || (type == 268) || (type == 272)
				|| (type == 276) || (type == 283))
			fullValue = 2L * getConfig().getLong(
					"damageditemsales_" + material + "value");
		/* 998 */return fullValue;
		/*      */}

	/*      */private void damagedItemSales(Player p, String action) {
		/* 1001 */ItemStack item = p.getItemInHand();
		/* 1002 */String itemName = item.getType().toString().toLowerCase()
				.replaceAll("_", "");
		/* 1003 */double damagePercent = 1.0D * item.getDurability()
				/ item.getType().getMaxDurability();
		/* 1004 */long value = Math.round(damagedItemFullValue(item)
				* (1.0D - damagePercent));
		/* 1005 */if (value <= 0L) {
			p.sendMessage("Sorry, that item is too badly damaged to sell.");
			return;
		}
		/* 1006 */if (action.equalsIgnoreCase("worth")) {
			/* 1007 */p.sendMessage("That " + itemName + " is "
					+ Math.round(damagePercent * 100.0D)
					+ "% damaged, so it's worth $" + value + ".");
			/* 1008 */} else if (action.equalsIgnoreCase("sell")) {
			/* 1009 */p.setItemInHand(null);
			econ.depositPlayer(p.getName(), value);
			p.sendMessage(colorize("&aSold " + itemName + " for $" + value));
			/* 1010 */log.info(p.getName() + " sold " + itemName + " for $$$"
					+ value + " (((1 item at $" + value + " each)))");
			/*      */}
		/*      */}

	/*      */
	/*      */private void dateCmd(Player p, String d) {
		/* 1015 */if ((d == null) || (!p.hasPermission("v.date.admin"))) {
			/* 1016 */p.sendMessage(colorize("&7The current date/time is "
					+ dateString()));
			/*      */} else {
			/* 1018 */List monthNames = Arrays.asList(getConfig().getString(
					"worlddate_monthnames").split(","));
			/* 1019 */long day = 24000L;
			long serverFullTime = ((World) getServer().getWorlds().get(0))
					.getFullTime();
			long serverDays = (serverFullTime - serverFullTime % day) / day;
			/* 1020 */int mm = 0;
			int dd = 0;
			int yyyy = 0;
			/*      */try {
				mm = Integer.parseInt(d.substring(0, 2));
				dd = Integer.parseInt(d.substring(3, 5));
				yyyy = Integer.parseInt(d.substring(6, 10));
			} catch (Exception e) {
				/* 1022 */p
						.sendMessage("To set the date, you must use the format MM/DD/YYYY");
				return;
				/* 1023 */}
			while (serverDays-- > 0L) {
				/* 1024 */dd--;
				/* 1025 */if (dd < 1) {
					dd = getConfig().getInt("worlddate_dayspermonth");
					mm--;
				}
				/* 1026 */if (mm < 1) {
					mm = monthNames.size();
					yyyy--;
				}
				/*      */}
			/* 1028 */String mmm = "0" + mm;
			String ddd = "0" + dd;
			String serverStartDate = mmm.substring(mmm.length() - 2,
					mmm.length())
					+ "/"
					+ ddd.substring(ddd.length() - 2, ddd.length())
					+ "/"
					+ yyyy;
			/* 1029 */getConfig().set("worlddate_startdate", serverStartDate);
			writeConfig();
			/* 1030 */p
					.sendMessage(colorize("&7The new start date for the server is "
							+ serverStartDate));
			dateCmd(p, null);
			/*      */}
		/*      */}

	/*      */
	/* 1034 */private String dateString() {
		List monthNames = Arrays.asList(getConfig().getString(
				"worlddate_monthnames").split(","));
		/* 1035 */long day = 24000L;
		long serverFullTime = ((World) getServer().getWorlds().get(0))
				.getFullTime();
		/* 1036 */String d = getConfig().getString("worlddate_startdate");
		int mm = 0;
		int dd = 0;
		int yyyy = 0;
		/*      */try {
			mm = Integer.parseInt(d.substring(0, 2));
			dd = Integer.parseInt(d.substring(3, 5));
			yyyy = Integer.parseInt(d.substring(6, 10));
		} catch (Exception e) {
			/* 1038 */log
					.severe("Invalid date format on config setting worlddate.startdate (should be MM/DD/YYYY)");
			return "";
			/* 1039 */}
		long serverDays = (serverFullTime - serverFullTime % day) / day;
		/* 1040 */while (serverDays-- > 0L) {
			/* 1041 */dd++;
			/* 1042 */if (dd > getConfig().getInt("worlddate_dayspermonth")) {
				dd = 1;
				mm++;
			}
			/* 1043 */if (mm > monthNames.size()) {
				mm = 1;
				yyyy++;
			}
			/*      */}
		/* 1045 */long gameTime = serverFullTime % day;
		long hours = gameTime / 1000L + 6L;
		long minutes = gameTime % 1000L * 60L / 1000L;
		String ampm = "AM";
		/* 1046 */if (hours >= 24L)
			dd++;
		if (dd > getConfig().getInt("worlddate_dayspermonth")) {
			dd = 1;
			mm++;
		}
		if (mm > monthNames.size()) {
			mm = 1;
			yyyy++;
		}
		/* 1047 */if (hours >= 12L) {
			hours -= 12L;
			ampm = "PM";
		}
		if (hours >= 12L) {
			hours -= 12L;
			ampm = "AM";
		}
		if (hours == 0L)
			hours = 12L;
		/* 1048 */String min = "0" + minutes;
		min = min.substring(min.length() - 2, min.length());
		/* 1049 */return hours + ":" + min + " " + ampm + " on "
				+ (String) monthNames.get(mm - 1) + " " + dd + ", " + yyyy;
	}

	/*      */
	/*      */private int dateStart() {
		/* 1052 */return getServer().getScheduler().scheduleSyncRepeatingTask(
				this, new Runnable() {
					/*      */public void run() {
						/* 1054 */String dt = Vitals.this.dateString();
						String announce = null;
						/* 1055 */if ((dt.contains("6:00 AM"))
								|| (dt.contains("6:01 AM"))
								|| (dt.contains("6:02 AM")))
							announce = "A new day has arrived!";
						/* 1056 */if ((dt.contains("6:30 PM"))
								|| (dt.contains("6:31 PM"))
								|| (dt.contains("6:32 PM")))
							announce = "Darkness begins to fall...";
						/* 1057 */if ((dt.contains("8:00 PM"))
								|| (dt.contains("8:01 PM"))
								|| (dt.contains("8:02 PM")))
							announce = "Darkness has fallen.";
						/* 1058 */if (announce != null)
							getServer().broadcastMessage(
									Vitals.colorize("&3" + announce
											+ " &7It is now " + dt));
						/*      */}
					/*      */
				}
				/*      */, 20L, 50L);
		/*      */}

	/*      */
	/*      */private void deathretention(PlayerDeathEvent event, final Player p) {
		/* 1064 */List inv = new ArrayList();
		List armor = new ArrayList();
		/* 1065 */int totalitems = 0;
		int totalcost = 0;
		int costperitem = getConfig().getInt(
				"gameplay_deathretention_costperitem");
		if (costperitem < 0)
			costperitem = 0;
		/* 1066 */boolean saveAll = p
				.hasPermission("v.deathretention.allitems");
		boolean saveArmor = p.hasPermission("v.deathretention.armor");
		boolean saveWeapons = p.hasPermission("v.deathretention.weapons");
		boolean saveTools = p.hasPermission("v.deathretention.tools");
		boolean saveExp = p.hasPermission("v.deathretention.experience");
		/* 1067 */String saved = "";
		/* 1068 */if ((arenaActive == null)
				|| (!arenaActive.playerAlive(p))
				|| (!getConfig().getBoolean(
						"gameplay_deathretention_disableduringarenas"))) {
			/* 1069 */for (ItemStack i : p.getInventory().getArmorContents())
				/* 1070 */if ((i != null)
						&&
						/* 1071 */((saveAll) || (saveArmor))
						&& (
						/* 1071 */(costperitem == 0) || (econ.has(p.getName(),
								costperitem)))) {
					/* 1072 */armor.add(i);
					event.getDrops().remove(i);
					/* 1073 */if (i.getType() != Material.AIR)
						totalitems++;
					/* 1074 */if ((i.getType() != Material.AIR)
							&& (costperitem > 0)) {
						econ.withdrawPlayer(p.getName(), costperitem);
						totalcost += costperitem;
						/*      */}
					/*      */}
			/* 1077 */for (ItemStack i : p.getInventory().getContents())
				/* 1078 */if (i != null) {
					/* 1079 */boolean keep = (saveAll)
							|| ((saveArmor) && (isArmor(i.getType())))
							|| ((saveWeapons) && (isWeapon(i.getType())))
							|| ((saveTools) && (isTool(i.getType())));
					/* 1080 */if ((keep)
							&& ((costperitem == 0) || (econ.has(p.getName(),
									costperitem)))) {
						/* 1081 */inv.add(i);
						event.getDrops().remove(i);
						totalitems++;
						if (costperitem > 0) {
							econ.withdrawPlayer(p.getName(), costperitem);
							totalcost += costperitem;
						}
						/*      */}
					/*      */}
			/* 1084 */if (saveAll) {
				saved = "all items";
				/*      */} else {
				/* 1086 */if (saveWeapons)
					saved = saved + (saved.length() > 0 ? "/" : "") + "weapons";
				/* 1087 */if (saveArmor)
					saved = saved + (saved.length() > 0 ? "/" : "") + "armor";
				/* 1088 */if (saveTools)
					saved = saved + (saved.length() > 0 ? "/" : "") + "tools";
				/*      */}
			/*      */}
		/* 1091 */if (saveExp) {
			event.setDroppedExp(0);
			event.setKeepLevel(true);
		}
		/* 1092 */if (saveExp)
			saved = saved + (saved.length() > 0 ? "/" : "") + "experience";
		/* 1093 */if ((saveExp) || (inv.size() > 0) || (armor.size() > 0)) {
			/* 1094 */saved = "Your force of will allowed you to retain "
					+ saved + " even through death";
			/* 1095 */saved = saved
					+ (costperitem > 0 ? ", at a cost of $" + costperitem
							+ " per item." : ".");
			/* 1096 */saved = saved + " You retained " + totalitems + " items";
			/* 1097 */saved = saved
					+ (costperitem > 0 ? " (total cost $" + totalcost + ")."
							: ".");
			/* 1098 */saved = saved
					+ " Any other items have dropped to the ground where you died.";
			/* 1099 */final String msg = saved;
			/* 1100 */getServer().getScheduler().scheduleAsyncDelayedTask(this,
					new Runnable() {
						public void run() {
							p.sendMessage(msg);
						}
						/*      */
						/*      */
					}
					/* 1101 */, 20L);
			/* 1101 */if (inv.size() > 0)
				deathretentionInventory.put(p.getName(), inv);
			/* 1102 */if (armor.size() > 0)
				deathretentionArmor.put(p.getName(), armor);
			/*      */}
		/*      */}

	/*      */
	/*      */private void findmyplotCmd(Player p, String[] args)
	/*      */{
		/* 1107 */int plotNumber = 1;
		String playerName = p.getName();
		/* 1108 */if (args.length >= 1) {
			/* 1109 */if (!auth(p, "findmyplot.others")) {
				showUsage(p, "findmyplot");
				return;
			}
			/* 1110 */playerName = args[0];
			/* 1111 */if (args.length >= 2)
				try {
					/* 1112 */plotNumber = Integer.parseInt(args[1]);
				} catch (Exception e) {
					/* 1113 */p
							.sendMessage("Plot number must be a positive integer.");
					return;
					/*      */}
			/*      */}
		/* 1116 */File residentFile = new File(getServer().getPluginManager()
				.getPlugin("Towny").getDataFolder()
				+ File.separator
				+ "data"
				+ File.separator
				+ "residents"
				+ File.separator + playerName + ".txt");
		/* 1117 */if (!residentFile.exists()) {
			p.sendMessage("Player " + playerName
					+ " is not registered with Towny.");
			return;
		}
		/* 1118 */String[] residentdata = loadText(residentFile).split("\n");
		/* 1119 */for (String line : residentdata)
			/* 1120 */if (line.contains("townBlocks=")) {
				/* 1121 */String[] plotdata = line.split("=")[1].split("\\|")[0]
						.split(":")[1].split(";");
				/* 1122 */if (plotdata.length == 0) {
					/* 1123 */if (playerName != p.getName()) {
						p.sendMessage("That player doesn't own any plots.");
						return;
					}
					/* 1124 */p.sendMessage("You don't own any plots.");
					/* 1125 */if ((enabled("townymods_findplot"))
							&& (p.hasPermission("v.findplot")))
						p.sendMessage(colorize("Find a plot for sale by typing &a/findplot"));
					/*      */}
				/* 1127 */else {
					p.sendMessage("Teleporting you to plot " + plotNumber
							+ " out of " + plotdata.length + " plots owned...");
					/* 1128 */String world = line.split("=")[1].split("\\|")[0]
							.split(":")[0];
					/* 1129 */String[] plotinfo = plotdata[(plotNumber - 1)]
							.split("]")[1].split(",");
					/* 1130 */int x = Integer.parseInt(plotinfo[0]);
					int z = Integer.parseInt(plotinfo[1]);
					/* 1131 */int plotY = getHighestFreeBlockAt(getServer()
							.getWorld(world), x * 16 + 8, z * 16 + 8);
					/* 1132 */Location plotlocation = new Location(getServer()
							.getWorld(world), x * 16 + 8, plotY + 1, z * 16 + 8);
					/* 1133 */playerTeleport(p, plotlocation, null);
				}
				/*      */}
		/*      */}

	/*      */
	/*      */private void findplot(final Player p)
	/*      */{
		/* 1139 */File townsFolder = new File(getServer().getPluginManager()
				.getPlugin("Towny").getDataFolder()
				+ File.separator + "data" + File.separator + "towns");
		/* 1140 */String minworld = "";
		int minx = 0;
		int minz = 0;
		Double minprice = Double.valueOf(1.7976931348623157E+308D);
		/* 1141 */for (File file : townsFolder.listFiles()) {
			/* 1142 */if (file.isFile()) {
				/* 1143 */String[] towndata = loadText(file).split("\n");
				/* 1144 */for (String line : towndata) {
					/* 1145 */if (line.contains("townBlocks=")) {
						/* 1146 */String[] worldplots = line.split("=")[1]
								.split("\\|");
						/* 1147 */for (String worldplot : worldplots) {
							/* 1148 */String world = worldplot.split(":")[0];
							/* 1149 */String[] plotdata = worldplot.split(":")[1]
									.split(";");
							/* 1150 */debug("[findplot] searching "
									+ plotdata.length + " plots in world ["
									+ world + "]...");
							/* 1151 */int fsplots = 0;
							int nfsplots = 0;
							/* 1152 */for (String plot : plotdata) {
								/* 1153 */String[] plotinfo = plot.split("]")[1]
										.split(",");
								/* 1154 */int x = Integer.parseInt(plotinfo[0]);
								int z = Integer.parseInt(plotinfo[1]);
								Double price = Double.valueOf(Double
										.parseDouble(plotinfo[2]));
								/* 1155 */if ((price != null)
										&& (price.doubleValue() != -1.0D))
									fsplots++;
								else
									nfsplots++;
								/* 1156 */if ((price != null)
										&& (price.doubleValue() != -1.0D)
										&& (price.doubleValue() < minprice
												.doubleValue())) {
									minworld = world;
									minx = x;
									minz = z;
									minprice = price;
								}
								/*      */}
							/* 1158 */debug("[findplot] fsplots " + fsplots
									+ " nfsplots " + nfsplots);
							/*      */}
						/*      */}
					/*      */}
				/*      */}
			/*      */}
		/* 1164 */if (minprice.doubleValue() == 1.7976931348623157E+308D) {
			p.sendMessage("No plots for sale could be found.");
			return;
		}
		/* 1165 */p.sendMessage("Plot at [" + minworld + "," + minx + ","
				+ minz + "] is for sale for $" + minprice);
		/* 1166 */if (minworld.length() == 0) {
			p.sendMessage("But the world doesn't seem to exist!");
			return;
		}
		/* 1167 */int plotY = getHighestFreeBlockAt(
				getServer().getWorld(minworld), minx * 16 + 8, minz * 16 + 8);
		/* 1168 */Location plotlocation = new Location(getServer().getWorld(
				minworld), minx * 16 + 8, plotY + 1, minz * 16 + 8);
		/* 1169 */final double plotprice = minprice.doubleValue();
		/* 1170 */playerTeleport(p, plotlocation, new Runnable() {
			/*      */public void run() {
				/* 1172 */if (Vitals.econ.has(p.getName(), plotprice))
					p.sendMessage(Vitals
							.colorize("If you want to buy this plot, type &a/plot claim"));
				else
					/* 1173 */p
							.sendMessage("Unfortunately, you don't have enough money to buy this plot right now.");
				/*      */}
		});
		/*      */}

	/*      */
	/*      */private double findplotprice(Location location) {
		/* 1178 */File townsFolder = new File(getServer().getPluginManager()
				.getPlugin("Towny").getDataFolder()
				+ File.separator + "data" + File.separator + "towns");
		/* 1179 */if (!townsFolder.exists()) {
			debug("[townyprice] someone did /plot claim but towny doesn't appear to be installed, so ignoring it");
			return Double.valueOf(0.0D);
		}
		/* 1180 */debug("[townyprice] someone did /plot claim at location ["
				+ location.getWorld().getName() + "," + location.getX() + ","
				+ location.getZ() + "], so checking the price");
		/* 1181 */for (File file : townsFolder.listFiles()) {
			/* 1182 */if (file.isFile()) {
				/* 1183 */String[] towndata = loadText(file).split("\n");
				/* 1184 */for (String line : towndata)
					/* 1185 */if (line.contains("townBlocks=")) {
						/* 1186 */String[] worldplots = line.split("=")[1]
								.split("\\|");
						/* 1187 */for (String worldplot : worldplots) {
							/* 1188 */String world = worldplot.split(":")[0];
							/* 1189 */String[] plotdata = worldplot.split(":")[1]
									.split(";");
							/* 1190 */debug("[townyprice] searching "
									+ plotdata.length + " plots in world ["
									+ world + "]...");
							/* 1191 */for (String plot : plotdata) {
								/* 1192 */String[] plotinfo = plot.split("]")[1]
										.split(",");
								/* 1193 */int x = Integer.parseInt(plotinfo[0]);
								int z = Integer.parseInt(plotinfo[1]);
								Double price = Double.valueOf(Double
										.parseDouble(plotinfo[2]));
								/* 1194 */Double minx = Double
										.valueOf(x * 16.0D);
								Double maxx = Double.valueOf(x * 16.0D + 16.0D);
								Double minz = Double.valueOf(z * 16.0D);
								Double maxz = Double.valueOf(z * 16.0D + 16.0D);
								/* 1195 */if ((location.getX() >= minx
										.doubleValue())
										&& (location.getX() < maxx
												.doubleValue())
										&& (location.getZ() >= minz
												.doubleValue())
										&& (location.getZ() < maxz
												.doubleValue())) {
									debug("[townyprice] price is " + price);
									return price;
									/*      */}
								/*      */}
							/*      */}
						/*      */}
				/*      */}
			/*      */}
		/* 1202 */debug("[townyprice] price not found");
		return Double.valueOf(-1.0D);
		/*      */}

	/*      */
	/*      */private void gmall(Player p, String param) {
		/* 1206 */List modes = Arrays.asList(new String[] { "survival",
				"creative", "adventure" });
		/* 1207 */if ((param != "") && (!modes.contains(param.toLowerCase()))) {
			/* 1208 */Player player = getServer().getPlayer(param);
			/* 1209 */if (player == null) {
				p.sendMessage("Player '" + param + "' is not online.");
				return;
			}
			/* 1210 */p.sendMessage(param + " is "
					+ (player.isFlying() ? "" : "not ") + "flying. " + param
					+ " is " + (player.isOp() ? "" : "not ") + " op.");
			/* 1211 */if (player.getGameMode().equals(GameMode.CREATIVE))
				p.sendMessage(param + " is in creative mode.");
			/* 1212 */else if (player.getGameMode().equals(GameMode.ADVENTURE))
				p.sendMessage(param + " is in adventure mode.");
			/* 1213 */else if (player.getGameMode().equals(GameMode.SURVIVAL))
				p.sendMessage(param + " is in survival mode.");
			/*      */}
		/* 1214 */else if ((modes.contains(param.toLowerCase()))
				&& (p.hasPermission("v.gmall.admin"))) {
			/* 1215 */GameMode gm = param.equalsIgnoreCase("adventure") ? GameMode.ADVENTURE
					: param.equalsIgnoreCase("creative") ? GameMode.CREATIVE
							: GameMode.SURVIVAL;
			/* 1216 */int numPlayers = 0;
			for (Player player : getServer().getOnlinePlayers()) {
				player.setGameMode(gm);
				numPlayers++;
			}
			/* 1217 */p.sendMessage(numPlayers + " players changed to "
					+ gm.toString() + " mode.");
			/*      */} else {
			/* 1219 */String[] tags = { "in survival mode",
					"in adventure mode", "who are flying", "in creative mode",
					"who are op" };
			/* 1220 */for (int i = 0; i <= 4; i++) {
				/* 1221 */String msg = "";
				int numPlayers = 0;
				/* 1222 */for (Player player : getServer().getOnlinePlayers())
					if (gmallTest(player, i)) {
						numPlayers++;
						msg = msg + " " + player.getName();
					}
				/* 1223 */p.sendMessage("Players " + tags[i] + " ("
						+ numPlayers + "):" + msg);
				/*      */}
			/*      */}
		/*      */}

	/*      */
	/* 1228 */private boolean gmallTest(Player p, int which) {
		switch (which) {
		case 0:
			/* 1229 */return p.getGameMode().equals(GameMode.SURVIVAL);
			/*      */case 1:
			/* 1230 */return p.getGameMode().equals(GameMode.ADVENTURE);
			/*      */case 2:
			/* 1231 */return p.isFlying();
			/*      */case 3:
			/* 1232 */return p.getGameMode().equals(GameMode.CREATIVE);
			/*      */case 4:
			/* 1233 */return p.isOp();
		}
		/* 1234 */return false;
		/*      */}

	/*      */
	/*      */private boolean helperbot(Player p, String[] args)
	/*      */{
		/* 1239 */if (has(args, 0, "list")) {
			/* 1240 */p.sendMessage(colorize("&c[HelperBot Config]"));
			/*      */String wordpair;
			/* 1241 */for (Iterator localIterator = config("helperbot")
					.getKeys(false).iterator(); localIterator.hasNext(); p
					.sendMessage(wordpair.replaceAll("_", ",") + ": "
							+ config("helperbot").get(wordpair)))
				wordpair = (String) localIterator.next();
			return true;
			/*      */}
		/* 1243 */if (args.length < 3)
			return false;
		/* 1244 */String word1 = args[0];
		String word2 = args[1];
		String response = "";
		/* 1245 */for (int i = 2; i < args.length; i++)
			response = response + (response.equals("") ? "" : " ") + args[i];
		/* 1246 */config("helperbot").set(word1 + "_" + word2,
				response.equalsIgnoreCase("delete") ? null : response);
		/* 1247 */saveConfig("helperbot");
		p.sendMessage("HelperBot entry saved.");
		return true;
		/*      */}

	/*      */
	/*      */private void modvote(Player p, String vote)
	/*      */{
		/*      */TreeMap sorted_map;
		/* 1251 */if ((vote.equalsIgnoreCase("top"))
				&& (p.hasPermission("v.modvote.top"))) {
			/* 1252 */HashMap map = new HashMap();
			/* 1253 */for (String key : config("users").getKeys(false)) {
				/* 1254 */if ((config("users").getString(key + ".modvote") != null)
						&& (!config("users").getString(key + ".modvote")
								.equals("none"))) {
					/* 1255 */if (!map.containsKey(config("users").getString(
							key + ".modvote")))
						map.put(config("users").getString(key + ".modvote"),
								Double.valueOf(0.0D));
					/* 1256 */map.put(
							config("users").getString(key + ".modvote"), Double
									.valueOf(((Double) map.get(config("users")
											.getString(key + ".modvote")))
											.doubleValue() + 1.0D));
					/*      */}
				/*      */}
			/* 1259 */sorted_map = mapSort(map);
			/* 1260 */p
					.sendMessage(colorize("&c[Top 9 Players Voted to be Mod]"));
			/* 1261 */int numShown = 0;
			int numToShow = 9;
			/* 1262 */for (String key : sorted_map.keySet())
				if (numShown++ < numToShow)
					/* 1263 */if (((Double) sorted_map.get(key)).equals(Double
							.valueOf(1.0D)))
						p.sendMessage(key + ": 1 vote");
					else
						/* 1264 */p.sendMessage(key
								+ ": "
								+ Math.round(((Double) sorted_map.get(key))
										.doubleValue()) + " votes");
			/*      */}
		/* 1266 */else if ((vote.equalsIgnoreCase("reset"))
				&& (p.hasPermission("v.modvote.reset")))
		/*      */{
			/* 1267 */String key;
			/* 1267 */for (sorted_map = config("users").getKeys(false)
					.iterator(); sorted_map.hasNext(); config("users").set(
					key + ".modvote", null))
				key = (String) sorted_map.next();
			/* 1268 */saveConfig("users");
			p.sendMessage("All votes for moderator have been reset.");
			/* 1269 */} else if (vote == "") {
			/* 1270 */if ((config("users").getString(p.getName() + ".modvote") != null)
					&& (!config("users").getString(p.getName() + ".modvote")
							.equals("none"))) {
				/* 1271 */p
						.sendMessage(colorize("You are currently voting for: &b"
								+ config("users").getString(
										new StringBuilder(String.valueOf(p
												.getName())).append(".modvote")
												.toString())));
				/* 1272 */p
						.sendMessage("Type /modvote none if you wish to cancel your vote.");
				/*      */} else {
				/* 1274 */p
						.sendMessage("You are not currently voting for a mod. Type /modvote [name] to vote for someone!");
				/*      */}
		} else if (perms.playerHas(null, vote, "v.modvote.mod")) {
			/* 1276 */p
					.sendMessage("You can't vote for that person because they are already have moderator or staff permissions.");
			/*      */} else {
			/* 1278 */config("users").set(p.getName() + ".modvote",
					vote.toLowerCase());
			saveConfig("users");
			/* 1279 */p.sendMessage(colorize("&aYou are now voting for: &b"
					+ vote.toLowerCase()));
			/* 1280 */p
					.sendMessage("Make sure to spell their name exactly (capitals don't matter) for your vote to be counted!");
			/* 1281 */p
					.sendMessage("Players have no way of finding out who voted for them, so if they promised you something in return for a vote they are lying!");
			/* 1282 */p
					.sendMessage("Type /modvote none if you wish to cancel your vote.");
			/*      */}
		/*      */}

	/*      */
	/*      */private void onlinePlayers(Player playerToExclude) {
		/* 1287 */File outputFile = new File(getDataFolder(),
				"onlineplayers.txt");
		/* 1288 */String text = "";
		for (Player player : getServer().getOnlinePlayers())
			if (player != playerToExclude)
				text = text + player.getName() + "\n";
		/* 1289 */saveText(text, outputFile);
		/*      */}

	/*      */
	/*      */private void playerPassword(Player p, String password) {
		/* 1293 */if ((password.length() < 5) || (password.length() > 50)) {
			p.sendMessage("Your password must be between 5 and 50 characters in length.");
			return;
		}
		/* 1294 */config("playerpasswords").set(p.getName(), password);
		/* 1295 */if (saveConfig("playerpasswords"))
			p.sendMessage("Your password has been set.");
		else
			/* 1296 */p
					.sendMessage("An error occurred while trying to save your password. Please try again later.");
		/*      */}

	/*      */
	/*      */private int playtimeStart() {
/* 1300 */     return getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() { private Player[] onlinePlayersLast = new Player[0];
/* 1302 */       private long saveInterval = 600L; private long saveIntervalTimer = 0L; private long oneDay = 86400000L;
/*      */       List<Integer> playtimeHistory;
/*      */ 
/* 1305 */       public void run() { List upRanks = Arrays.asList(getConfig().getString("playtime_promotions").split(";"));
/* 1306 */         Player[] onlinePlayers = getServer().getOnlinePlayers();
/* 1307 */         for (Player player : onlinePlayers) {
/* 1308 */           String pName = player.getName().toLowerCase(); boolean addSecond = false; for (Player pp : onlinePlayersLast) if (pp == player) addSecond = true;
/* 1309 */           if (addSecond) {
/* 1310 */             long newPlaytime = config("playtime").getLong(pName + ".playtime", 0L) + 1L;
/* 1311 */             config("playtime").set(pName + ".playtime", Long.valueOf(newPlaytime));
/* 1312 */             if (newPlaytime % 60L == 0L) {
/* 1313 */               for (unknown = upRanks.iterator(); ((Iterator)unknown).hasNext(); ) { String uprank = (String)((Iterator)unknown).next();
/*      */                 try {
/* 1315 */                   String[] rInfo = uprank.split(","); promoteMinutes = Integer.parseInt(rInfo[2]);
/*      */                 }
/*      */                 catch (Exception e)
/*      */                 {
/*      */                   long promoteMinutes;
/* 1316 */                   Vitals.log.severe("Invalid VITALS configuration entry: playtime_promotions");
/*      */                   return;
/*      */                 }
/*      */                 long promoteMinutes;
/*      */                 String[] rInfo;
/* 1317 */                 if ((newPlaytime >= promoteMinutes * 60L) && ((Vitals.perms.getPrimaryGroup(player).equalsIgnoreCase(rInfo[0])) || (rInfo[0] == "any"))) {
/* 1318 */                   Vitals.perms.playerAddGroup(player, rInfo[1]);
/* 1319 */                   Vitals.perms.playerRemoveGroup(player, rInfo[0]);
/*      */                 }
/*      */               }
/*      */             }
/* 1323 */             if (newPlaytime % 3600L == 0L)
/*      */             {
/* 1323 */               String key;
/* 1323 */               for (unknown = config("playtime").getKeys(false).iterator(); ((Iterator)unknown).hasNext(); Vitals.this.playtimeAverageShift(key)) key = (String)((Iterator)unknown).next(); 
/*      */             }
/* 1324 */             Vitals.this.playtimeAverageShift(pName);
/* 1325 */             playtimeHistory = config("playtime").getIntegerList(pName + ".playtimehistory");
/* 1326 */             playtimeHistory.set(playtimeHistory.size() - 1, Integer.valueOf(((Integer)playtimeHistory.get(playtimeHistory.size() - 1)).intValue() + 1));
/* 1327 */             config("playtime").set(pName + ".playtimehistory", playtimeHistory);
/*      */           }
/*      */         }
/* 1330 */         if (++saveIntervalTimer % saveInterval == 0L) {
/* 1331 */           debug("[playtime] saving playtime of " + config("playtime").getKeys(false).size() + " players to disk");
/* 1332 */           if (getConfig().getBoolean("playtime_autocleanup")) {
/* 1333 */             int numPurged = 0;
/*      */             String pName;
/* 1334 */             label808: for (Iterator localIterator = config("playtime").getKeys(false).iterator(); localIterator.hasNext(); 
/* 1339 */               config("playtime").set(pName, null))
/*      */             {
/* 1334 */               pName = (String)localIterator.next();
/* 1335 */               playtimeHistory = config("playtime").getIntegerList(pName + ".playtimehistory");
/* 1336 */               boolean onlineThisWeek = false;
/* 1337 */               for (int d = 1; d <= 7; d++) if (((Integer)playtimeHistory.get(playtimeHistory.size() - d)).intValue() != 0) onlineThisWeek = true;
/* 1338 */               if ((onlineThisWeek) || (config("playtime").getLong(pName + ".playtime") * 1000L >= oneDay)) break label808;
/* 1339 */               numPurged++;
/*      */             }
/*      */ 
/* 1342 */             debug("[playtime] purged " + numPurged + " players that haven't been online for a week and have playtime less than a day");
/*      */           }
/* 1344 */           saveConfig("playtime");
/*      */         }
/* 1346 */         onlinePlayersLast = onlinePlayers;
/*      */       }
/*      */     }
/*      */     , 3L, 20L);
/*      */   }

	/*      */private void playtimeAverageShift(String playername) {
		/* 1351 */long oneDay = 86400000L;
		long thirtyDays = 2592000000L;
		/* 1352 */List playtimeHistory = config("playtime").getIntegerList(
				playername + ".playtimehistory");
		/* 1353 */while (playtimeHistory.size() < 30)
			playtimeHistory.add(0, Integer.valueOf(0));
		/* 1354 */long todayStartedAt = config("playtime").getLong(
				playername + ".playtimehistorymarker");
		/* 1355 */if (new Date().getTime() - todayStartedAt > thirtyDays) {
			/* 1356 */config("playtime").set(
					playername + ".playtimehistorymarker",
					Long.valueOf(new Date().getTime()));
			int i;
			/* 1357 */for (i = 0; i < 30; i++)
				playtimeHistory.set(i, Integer.valueOf(0));
			/* 1358 */config("playtime").set(playername + ".playtimehistory",
					playtimeHistory);
			/* 1359 */return;
			/*      */}
		/* 1361 */while (new Date().getTime() - todayStartedAt > oneDay)
		/*      */{
			/*      */
			/* 1362 */todayStartedAt += oneDay;
			/* 1363 */config("playtime").set(
					playername + ".playtimehistorymarker",
					Long.valueOf(todayStartedAt));
			/* 1364 */playtimeHistory.remove(0);
			/* 1365 */playtimeHistory.add(Integer.valueOf(0));
			/*      */}
		/* 1367 */config("playtime").set(playername + ".playtimehistory",
				playtimeHistory);
		/* 1368 */int sum = 0;
		/*      */Integer entry;
		/* 1368 */for (Iterator localIterator = playtimeHistory.iterator(); localIterator
				.hasNext(); sum += entry.intValue())
			entry = (Integer) localIterator.next();
		/* 1369 */config("playtime").set(playername + ".playtimeaverage",
				Integer.valueOf(sum / playtimeHistory.size()));
		/*      */}

	/*      */private void playtimeCmd(Player p, String[] args) {
/* 1372 */     String playername = args.length > 0 ? args[0] : p.getName();
/* 1373 */     if ((args.length == 2) && (auth(p, "playtime.admin"))) {
/*      */       try {
/* 1375 */         mm = Long.parseLong(args[1]);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*      */         long mm;
/* 1376 */         p.sendMessage("You must specify a positive whole number or zero for the playtime (in minutes).");
/*      */         return;
/*      */       }
/* 1377 */       long mm;
/* 1377 */       if (mm < 0L) { p.sendMessage("You must specify a positive whole number or zero for the playtime (in minutes)."); return; }
/* 1378 */       config("playtime").set(playername.toLowerCase() + ".playtime", Long.valueOf(mm * 60L)); saveConfig("playtime");
/* 1379 */       p.sendMessage(playername.toLowerCase() + "'s total playtime has been set to " + mm + " minutes."); } else {
/* 1380 */       if ((playername.equalsIgnoreCase("top")) && (auth(p, "playtime.top")))
/*      */       {
/* 1382 */         HashMap map = new HashMap();
/*      */         String key;
/* 1383 */         for (e = config("playtime").getKeys(false).iterator(); e.hasNext(); map.put(key, Double.valueOf(config("playtime").getDouble(key + ".playtime")))) key = (String)e.next();
/* 1384 */         TreeMap sorted_map = mapSort(map);
/* 1385 */         p.sendMessage(colorize("&c[Top 9 Playtime]"));
/* 1386 */         int numShown = 0; int numToShow = 9;
/* 1387 */         for (String key : sorted_map.keySet()) if (numShown++ < numToShow) p.sendMessage(key + ": " + Math.round(((Double)sorted_map.get(key)).doubleValue() / 36.0D / 24.0D) / 100.0D + " days");
/* 1388 */         if (p.hasPermission("v.playtime.average")) {
/* 1389 */           map.clear();
/*      */           String pName;
/* 1390 */           for (unknown = config("playtime").getKeys(false).iterator(); unknown.hasNext(); map.put(pName, Double.valueOf(config("playtime").getDouble(pName + ".playtimeaverage")))) { pName = (String)unknown.next(); playtimeAverageShift(pName); }
/* 1391 */           sorted_map = mapSort(map); numShown = 0; numToShow = 9;
/* 1392 */           p.sendMessage(colorize("&c[Top 9 Playtime Daily Average - Last 30 Days]"));
/* 1393 */           for (String pName : sorted_map.keySet()) if (numShown++ < numToShow) p.sendMessage(pName + ": " + Math.round(100.0D * ((Double)sorted_map.get(pName)).doubleValue() / 60.0D) / 100.0D + " minutes");
/*      */         }
/* 1395 */         return;
/* 1396 */       }if ((playername.equalsIgnoreCase(p.getName())) || (auth(p, "playtime.others")))
/*      */       {
/* 1398 */         long playtime = config("playtime").getLong(playername.toLowerCase() + ".playtime");
/* 1399 */         if (playtime == 0L) { p.sendMessage("No player by that name has recorded playtime on this server."); return; }
/* 1400 */         if (playername.equalsIgnoreCase(p.getName())) p.sendMessage("Your total playtime is:"); else
/* 1401 */           p.sendMessage("The total playtime of " + playername.toLowerCase() + " is:");
/* 1402 */         p.sendMessage(TimeUnit.SECONDS.toDays(playtime) + " days, " + TimeUnit.SECONDS.toHours(playtime) % 24L + " hours, " + 
/* 1403 */           TimeUnit.SECONDS.toMinutes(playtime) % 60L + " minutes, " + playtime % 60L + " seconds");
/* 1404 */         if (p.hasPermission("v.playtime.average")) {
/* 1405 */           playtimeAverageShift(playername.toLowerCase());
/* 1406 */           double average = config("playtime").getDouble(playername.toLowerCase() + ".playtimeaverage");
/* 1407 */           p.sendMessage("Average time played per day over the last 30 days:");
/* 1408 */           p.sendMessage(Math.round(100.0D * average / 60.0D) / 100.0D + " minutes");
/*      */         }
/*      */       } else {
/* 1411 */         showUsage(p, "playtime");
/*      */       }
/*      */     }
/*      */   }

	/*      */
	/* 1416 */private void plotsalesign(Player p, String[] args) {
		String[] forsalePixels = { "2,13", "2,12", "2,11", "2,10", "2,9",
				"2,8", "3,13", "3,11", "4,13", "6,13", "6,12", "6,11", "6,10",
				"6,9", "6,8", "7,13", "7,8", "8,13", "8,12", "8,11", "8,10",
				"8,9", "8,8", "10,13", "10,12", "10,11", "10,10", "10,9",
				"10,8", "11,13", "11,11", "12,13", "12,11", "12,10", "13,13",
				"13,12", "13,11", "13,9", "13,8",
				/* 1417 */"1,6", "1,5", "1,4", "1,2", "2,6", "2,4", "2,2",
				"3,6", "3,4", "3,3", "3,2", "5,6", "5,5", "5,4", "5,3", "5,2",
				"6,6", "6,4", "7,6", "7,5", "7,4", "7,3", "7,2", "9,6", "9,5",
				"9,4", "9,3", "9,2", "10,2", "12,6", "12,5", "12,4", "12,3",
				"12,2", "13,6", "13,4", "13,2", "14,6", "14,4", "14,2" };
		/* 1418 */int y = p.getLocation().getBlockY() - 1;
		/* 1419 */for (int x = 0; x < 16; x++)
			for (int z = 0; z < 16; z++) {
				/* 1420 */int type = Material.GRASS.getId();
				/* 1421 */if (args.length >= 1) {
					/* 1422 */Material m = Material.getMaterial(args[0]
							.toUpperCase());
					/* 1423 */if (m == null) {
						p.sendMessage("Invalid type, try sand/grass/etc");
						return;
					}
					/* 1424 */type = m.getId();
					/*      */}
				/* 1426 */byte data = 0;
				/* 1427 */if ((args.length < 2)
						&& ((x == 0) || (x == 15) || (z == 0) || (z == 15))) {
					type = Material.WOOL.getId();
					data = 11;
				}
				/* 1428 */if (args.length < 2)
					for (String pixel : forsalePixels) {
						/* 1429 */String[] pixxel = pixel.split(",");
						/* 1430 */if ((Integer.parseInt(pixxel[0]) == z)
								&& (Integer.parseInt(pixxel[1]) == x)) {
							type = Material.WOOL.getId();
							data = 7;
						}
						/*      */}
				/* 1432 */p.getLocation().getChunk().getBlock(x, y, z)
						.setTypeIdAndData(type, data, true);
				/*      */}
		/*      */}

	/*      */
	/*      */private boolean regionlabel(Player p, String[] args)
	/*      */{
		/* 1437 */if ((has(args, 1, "delete"))
				&& (!config("regionlabels").contains(args[0]))) {
			p.sendMessage("No region exists by that name.");
			return true;
		}
		/* 1438 */if (has(args, 1, "delete")) {
			config("regionlabels").set(args[0], null);
			saveConfig("regionlabels");
			p.sendMessage("Region label deleted.");
			return true;
		}
		/* 1439 */if (has(args, 0, "list")) {
			p.sendMessage(colorize("&c[Labeled Regions]"));
			/*      */String region;
			/* 1439 */for (Iterator localIterator = config("regionlabels")
					.getKeys(false).iterator(); localIterator.hasNext(); p
					.sendMessage(region + ": "
							+ config("regionlabels").get(region)))
				region = (String) localIterator.next();
			return true;
		}
		/* 1440 */if (has(args, 1, "exact")) {
			setupNew(p, "regionlabel", args[0], "exact");
			return true;
		}
		/* 1441 */if (has(args, 1, "normal")) {
			setupNew(p, "regionlabel", args[0], "");
			return true;
		}
		/* 1442 */return false;
		/*      */}

	/*      */private int regionlabelsStart() {
		/* 1445 */return getServer().getScheduler().scheduleAsyncRepeatingTask(
				this, new Runnable() {
					/*      */public void run() {
						/* 1447 */for (Player p : getServer()
								.getOnlinePlayers()) {
							/* 1448 */String lastRegion = (String) regionlabelPlayers
									.get(p.getName());
							String thisRegion = Vitals.this.regionGet(
									"regionlabels", p.getLocation());
							/* 1449 */if ((thisRegion != null)
									&& (!thisRegion.equals(lastRegion))) {
								/* 1450 */p.sendMessage(Vitals
										.colorize(Vitals.this.lang(
												"regionlabels_enter")
												.replaceAll(
														"\\{region\\}",
														thisRegion.replaceAll(
																"_", " "))));
								/* 1451 */regionlabelPlayers.put(p.getName(),
										thisRegion);
								/*      */}
							/*      */}
						/*      */}
					/*      */
				}
				/*      */, 4L, 60L);
		/*      */}

	/*      */private boolean regionprotect(Player p, String[] args) {
		/* 1458 */if ((has(args, 1, "delete"))
				&& (!config("regionprotect").contains(args[0]))) {
			p.sendMessage("No region exists by that name.");
			return true;
		}
		/* 1459 */if (has(args, 1, "delete")) {
			config("regionprotect").set(args[0], null);
			saveConfig("regionprotect");
			p.sendMessage("Region protection removed.");
			return true;
		}
		/* 1460 */if (has(args, 0, "list")) {
			p.sendMessage(colorize("&c[Protected Regions]"));
			/*      */String region;
			/* 1460 */for (Iterator localIterator = config("regionprotect")
					.getKeys(false).iterator(); localIterator.hasNext(); p
					.sendMessage(region + ": "
							+ config("regionprotect").get(region)))
				region = (String) localIterator.next();
			return true;
		}
		/* 1461 */if (has(args, 1, "protect")) {
			setupNew(p, "regionprotect", args[0], "");
			return true;
		}
		/* 1462 */return false;
		/*      */}

	/*      */
	/*      */private String regionGet(String configName, Location loc)
/*      */   {
/*      */     String region;
/* 1465 */     label223: for (Iterator localIterator = config(configName).getKeys(false).iterator(); localIterator.hasNext(); 
/* 1468 */       return region)
/*      */     {
/* 1465 */       region = (String)localIterator.next();
/* 1466 */       String[] cuboid = config(configName).getString(region).split("_");
/* 1467 */       String world = cuboid[0]; Double minx = Double.valueOf(Double.parseDouble(cuboid[1])); Double miny = Double.valueOf(Double.parseDouble(cuboid[2])); Double minz = Double.valueOf(Double.parseDouble(cuboid[3])); Double maxx = Double.valueOf(Double.parseDouble(cuboid[4])); Double maxy = Double.valueOf(Double.parseDouble(cuboid[5])); Double maxz = Double.valueOf(Double.parseDouble(cuboid[6]));
/* 1468 */       if ((!loc.getWorld().getName().equals(world)) || (loc.getX() < minx.doubleValue()) || (loc.getX() >= maxx.doubleValue()) || (loc.getY() < miny.doubleValue()) || (loc.getY() >= maxy.doubleValue()) || (loc.getZ() < minz.doubleValue()) || (loc.getZ() >= maxz.doubleValue())) break label223;
/*      */     }
/* 1470 */     return null;
/*      */   }

	/*      */private boolean regionprotected(Location loc) {
/* 1473 */     label211: for (Iterator localIterator = regionprotectRegions.keySet().iterator(); localIterator.hasNext(); 
/* 1476 */       return true)
/*      */     {
/* 1473 */       String region = (String)localIterator.next();
/* 1474 */       String[] cuboid = (String[])regionprotectRegions.get(region);
/* 1475 */       Double minx = Double.valueOf(Double.parseDouble(cuboid[1])); Double miny = Double.valueOf(Double.parseDouble(cuboid[2])); Double minz = Double.valueOf(Double.parseDouble(cuboid[3])); Double maxx = Double.valueOf(Double.parseDouble(cuboid[4])); Double maxy = Double.valueOf(Double.parseDouble(cuboid[5])); Double maxz = Double.valueOf(Double.parseDouble(cuboid[6]));
/* 1476 */       if ((!loc.getWorld().getName().equals(cuboid[0])) || (loc.getX() < minx.doubleValue()) || (loc.getX() >= maxx.doubleValue()) || (loc.getY() < miny.doubleValue()) || (loc.getY() >= maxy.doubleValue()) || (loc.getZ() < minz.doubleValue()) || (loc.getZ() >= maxz.doubleValue())) break label211;
/*      */     }
/* 1478 */     return false;
/*      */   }

	/*      */private List<String> regionrestoreFiles() {
		/* 1481 */List regions = new ArrayList();
		/* 1482 */for (File f : getDataFolder().listFiles()) {
			/* 1483 */if ((f.getName().length() >= 14)
					&& (f.getName().substring(0, 14).equals("regionrestore_"))
					&& (f.getName().substring(f.getName().length() - 4,
							f.getName().length()).equals(".yml"))) {
				/* 1484 */regions.add(f.getName()
						.replaceFirst("regionrestore_", "")
						.replaceAll(".yml", ""));
				/*      */}
			/*      */}
		/* 1487 */return regions;
		/*      */}

	/*      */private void regionrestoreList(Player p) {
		/* 1490 */p.sendMessage(colorize("&c[Saved Regions]"));
		/* 1491 */for (String region : regionrestoreFiles()) {
			/* 1492 */int blockCount = config("regionrestore_" + region)
					.contains(region + ".data") ? config(
					"regionrestore_" + region)
					.getConfigurationSection(region + ".data").getKeys(false)
					.size() : 0;
			/* 1493 */p.sendMessage(region
					+ " ("
					+ blockCount
					+ " blocks) (autorestore every "
					+ config(
							new StringBuilder("regionrestore_").append(region)
									.toString()).getInt(
							new StringBuilder(String.valueOf(region)).append(
									".interval").toString()) + " minutes)");
			/*      */}
		/*      */}

	/*      */
	/* 1497 */private boolean regionsave(Player p, String[] args) {
		if ((has(args, 1, "delete")) || (has(args, 1, "interval"))) {
			/* 1498 */File f = new File(getDataFolder(), "regionrestore_"
					+ args[0] + ".yml");
			/* 1499 */if (!f.exists()) {
				p.sendMessage("No region exists by that name.");
				return true;
			}
			/*      */}
		/* 1501 */if (has(args, 1, "delete")) {
			deleteConfig("regionrestore_" + args[0]);
			p.sendMessage("Region data deleted.");
			init();
			return true;
		}
		/* 1502 */if ((has(args, 1, "interval")) && (args.length == 3)) {
			config("regionrestore_" + args[0]).set(args[0] + ".interval",
					Integer.valueOf(Integer.parseInt(args[2])));
			saveConfig("regionrestore_" + args[0]);
			init();
			p.sendMessage("Region '" + args[0]
					+ "' will now automatically restore every " + args[2]
					+ " minutes.");
			return true;
		}
		/* 1503 */if (has(args, 0, "list")) {
			regionrestoreList(p);
			return true;
		}
		/* 1504 */if (has(args, 1, "inventory")) {
			setupNew(p, "regionrestore", args[0], "inventory");
			return true;
		}
		/* 1505 */if (has(args, 1, "all")) {
			setupNew(p, "regionrestore", args[0], "");
			return true;
		}
		/* 1506 */return false;
	}

	/*      */
	/*      */private void regionrestore(Player p, String region) {
		/* 1509 */if (region.equalsIgnoreCase("list")) {
			regionrestoreList(p);
			return;
		}
		/* 1510 */File f = new File(getDataFolder(), "regionrestore_" + region
				+ ".yml");
		/* 1511 */if (!f.exists()) {
			if (p != null)
				p.sendMessage("No region exists by that name");
			return;
		}
		/* 1512 */if (tasks.containsKey("regionrestorebatch"))
			p.sendMessage("Canceled restore operation in progress to start new restore...");
		/* 1513 */task("regionrestorebatch",
				Integer.valueOf(regionrestoreBatch(region, p)));
		/*      */}

	/*      */private int regionsaveBatch(final String region, final Block b1,
			Block b2, final String option, final Player notify) {
		/* 1516 */return getServer().getScheduler().scheduleSyncRepeatingTask(
				this, new Runnable() {
					YamlConfiguration cfg;
					/*      */Double x1;
					/*      */Double y1;
					/*      */Double z1;
					/*      */Double x2;
					/*      */Double y2;
					/*      */Double z2;
					/*      */Double minx;
					/*      */Double miny;
					/*      */Double minz;
					/*      */Double maxx;
					/*      */Double maxy;
					/*      */Double maxz;
					/*      */World w;
					/*      */int blockNum;
					/*      */int blocksPer;
					/*      */StringBuilder air;
					/*      */StringBuilder stone;
					/*      */StringBuilder dirt;
					/*      */StringBuilder water;
					/*      */StringBuilder grass;
					/*      */StringBuilder sand;
					/*      */StringBuilder gravel;
					/*      */StringBuilder bedrock;
					/*      */StringBuilder lava;

					/*      */
					/* 1523 */public void run() {
						int i = 0;
						/* 1524 */if (blockNum == 0) {
							/* 1525 */config("regionrestore_" + region).set(
									region, null);
							/* 1526 */config("regionrestore_" + region).set(
									region + ".interval", Integer.valueOf(-1));
							/* 1527 */config("regionrestore_" + region).set(
									region + ".world", b1.getWorld().getName());
							/* 1528 */config("regionrestore_" + region).set(
									region + ".minx", minx);
							/* 1529 */config("regionrestore_" + region).set(
									region + ".miny", miny);
							/* 1530 */config("regionrestore_" + region).set(
									region + ".minz", minz);
							/*      */}
						/* 1532 */for (long x = 0L; x <= maxx.doubleValue()
								- minx.doubleValue(); x += 1L)
							for (long y = 0L; y <= maxy.doubleValue()
									- miny.doubleValue(); y += 1L)
								for (long z = 0L; z <= maxz.doubleValue()
										- minz.doubleValue(); z += 1L) {
									/* 1533 */Block b = new Location(w, minx
											.doubleValue() + x, miny
											.doubleValue() + y, minz
											.doubleValue() + z).getBlock();
									/* 1534 */if ((!option.equals("inventory"))
											|| ((b.getState() instanceof InventoryHolder))) {
										/* 1535 */i++;
										if (i >= blockNum) {
											/* 1536 */if (i >= blockNum
													+ blocksPer) {
												blockNum = i;
												if (i % (blocksPer * 5) == 0)
													notify.sendMessage("saved "
															+ i
															/ 1000
															+ "k blocks so far...");
												return;
											}
											/* 1537 */if (b.getType() == Material.AIR) {
												if (air.length() > 0)
													air.append(",");
												air.append(x);
												air.append(",");
												air.append(y);
												air.append(",");
												air.append(z);
												/* 1538 */} else if (b
													.getType() == Material.STONE) {
												if (stone.length() > 0)
													stone.append(",");
												stone.append(x);
												stone.append(",");
												stone.append(y);
												stone.append(",");
												stone.append(z);
												/* 1539 */} else if (b
													.getType() == Material.DIRT) {
												if (dirt.length() > 0)
													dirt.append(",");
												dirt.append(x);
												dirt.append(",");
												dirt.append(y);
												dirt.append(",");
												dirt.append(z);
												/* 1540 */} else if (b
													.getType() == Material.STATIONARY_WATER) {
												if (water.length() > 0)
													water.append(",");
												water.append(x);
												water.append(",");
												water.append(y);
												water.append(",");
												water.append(z);
												/* 1541 */} else if (b
													.getType() == Material.GRASS) {
												if (grass.length() > 0)
													grass.append(",");
												grass.append(x);
												grass.append(",");
												grass.append(y);
												grass.append(",");
												grass.append(z);
												/* 1542 */} else if (b
													.getType() == Material.SAND) {
												if (sand.length() > 0)
													sand.append(",");
												sand.append(x);
												sand.append(",");
												sand.append(y);
												sand.append(",");
												sand.append(z);
												/* 1543 */} else if (b
													.getType() == Material.GRAVEL) {
												if (gravel.length() > 0)
													gravel.append(",");
												gravel.append(x);
												gravel.append(",");
												gravel.append(y);
												gravel.append(",");
												gravel.append(z);
												/* 1544 */} else if (b
													.getType() == Material.BEDROCK) {
												if (bedrock.length() > 0)
													bedrock.append(",");
												bedrock.append(x);
												bedrock.append(",");
												bedrock.append(y);
												bedrock.append(",");
												bedrock.append(z);
												/* 1545 */} else if (b
													.getType() == Material.STATIONARY_LAVA) {
												if (lava.length() > 0)
													lava.append(",");
												lava.append(x);
												lava.append(",");
												lava.append(y);
												lava.append(",");
												lava.append(z);
											} else {
												/* 1546 */StringBuilder key = new StringBuilder();
												StringBuilder val = new StringBuilder();
												/* 1547 */key.append(region);
												key.append(".data.");
												key.append(x);
												key.append("_");
												key.append(y);
												key.append("_");
												key.append(z);
												/* 1548 */val.append(b
														.getTypeId());
												val.append("_");
												val.append(b.getData());
												/* 1549 */cfg.set(
														key.toString(),
														val.toString());
												/* 1550 */if ((b.getState() instanceof InventoryHolder)) {
													/* 1551 */Inventory inv = ((InventoryHolder) b
															.getState())
															.getInventory();
													List contents = new ArrayList();
													/* 1552 */for (ItemStack is : inv
															.getContents())
														if (is != null)
															contents.add(new CardboardBox(
																	is)
																	.toString());
													/* 1553 */cfg.set(key
															+ "_contents",
															contents);
													notify.sendMessage("saved "
															+ b.getType()
															+ " inventory ("
															+ contents.size()
															+ " itemstacks)");
													/*      */}
												/*      */}
											/*      */}
										/*      */}
									/*      */}
						cfg.set(region + ".air", air.toString());
						cfg.set(region + ".stone", stone.toString());
						cfg.set(region + ".dirt", dirt.toString());
						/* 1558 */cfg.set(region + ".water", water.toString());
						cfg.set(region + ".grass", grass.toString());
						cfg.set(region + ".sand", sand.toString());
						/* 1559 */cfg.set(region + ".gravel", gravel.toString());
						cfg.set(region + ".bedrock", bedrock.toString());
						cfg.set(region + ".lava", lava.toString());
						/* 1560 */saveConfig("regionrestore_" + region);
						/* 1561 */notify.sendMessage("FINISHED! The region '"
								+ region + "' has been saved. (total " + i
								+ " blocks)");
						Vitals.this.task("regionsavebatch", Integer.valueOf(-1));
						/*      */}
					/*      */
				}
				/*      */, 0L, 4L);
		/*      */}

	/*      */private int regionrestoreBatch(final String region, final Player notify) {
		/* 1566 */return getServer().getScheduler().scheduleSyncRepeatingTask(
				this, new Runnable() {
					YamlConfiguration cfg;
					/*      */World w;
					/*      */int blockNum;
					/*      */int n;
					/*      */int blocksPer;
					/*      */Double minx;
					/*      */Double miny;
					/*      */Double minz;
					/*      */boolean didAir;
					/*      */boolean didStone;
					/*      */boolean didDirt;
					/*      */boolean didWater;
					/*      */boolean didGrass;
					/*      */boolean didSand;
					/*      */boolean didGravel;
					/*      */boolean didBedrock;
					/*      */boolean didLava;
					/*      */String[] air;
					/*      */String[] stone;
					/*      */String[] dirt;
					/*      */String[] water;
					/*      */String[] grass;
					/*      */String[] sand;
					/*      */String[] gravel;
					/*      */String[] bedrock;
					/*      */String[] lava;

					/*      */
					/* 1576 */public void run() {
						int i = 0;
						List contents = new ArrayList();
						/* 1577 */if ((!didAir) && (air.length > 1)) {
							didAir = true;
							/*      */int j;
							/* 1577 */n += (j = Vitals.this.regionrestoreArray(
									w, air, Material.AIR, minx, miny, minz,
									notify));
							if ((n > blocksPer) && (j > 0))
								return;
							/*      */}
						/* 1578 */if ((!didStone) && (stone.length > 1)) {
							didStone = true;
							/*      */int j;
							/* 1578 */n += (j = Vitals.this.regionrestoreArray(
									w, stone, Material.STONE, minx, miny, minz,
									notify));
							if ((n > blocksPer) && (j > 0))
								return;
							/*      */}
						/* 1579 */if ((!didDirt) && (dirt.length > 1)) {
							didDirt = true;
							/*      */int j;
							/* 1579 */n += (j = Vitals.this.regionrestoreArray(
									w, dirt, Material.DIRT, minx, miny, minz,
									notify));
							if ((n > blocksPer) && (j > 0))
								return;
							/*      */}
						/* 1580 */if ((!didWater) && (water.length > 1)) {
							didWater = true;
							/*      */int j;
							/* 1580 */n += (j = Vitals.this.regionrestoreArray(
									w, water, Material.STATIONARY_WATER, minx,
									miny, minz, notify));
							if ((n > blocksPer) && (j > 0))
								return;
							/*      */}
						/* 1581 */if ((!didGrass) && (grass.length > 1)) {
							didGrass = true;
							/*      */int j;
							/* 1581 */n += (j = Vitals.this.regionrestoreArray(
									w, grass, Material.GRASS, minx, miny, minz,
									notify));
							if ((n > blocksPer) && (j > 0))
								return;
							/*      */}
						/* 1582 */if ((!didSand) && (sand.length > 1)) {
							didSand = true;
							/*      */int j;
							/* 1582 */n += (j = Vitals.this.regionrestoreArray(
									w, sand, Material.SAND, minx, miny, minz,
									notify));
							if ((n > blocksPer) && (j > 0))
								return;
							/*      */}
						/* 1583 */if ((!didGravel) && (gravel.length > 1)) {
							didGravel = true;
							/*      */int j;
							/* 1583 */n += (j = Vitals.this.regionrestoreArray(
									w, gravel, Material.GRAVEL, minx, miny,
									minz, notify));
							if ((n > blocksPer) && (j > 0))
								return;
							/*      */}
						/* 1584 */if ((!didBedrock) && (bedrock.length > 1)) {
							didBedrock = true;
							/*      */int j;
							/* 1584 */n += (j = Vitals.this.regionrestoreArray(
									w, bedrock, Material.BEDROCK, minx, miny,
									minz, notify));
							if ((n > blocksPer) && (j > 0))
								return;
							/*      */}
						/* 1585 */if ((!didLava) && (lava.length > 1)) {
							didLava = true;
							/*      */int j;
							/* 1585 */n += (j = Vitals.this.regionrestoreArray(
									w, lava, Material.STATIONARY_LAVA, minx,
									miny, minz, notify));
							if ((n > blocksPer) && (j > 0))
								return;
							/*      */}
						/* 1586 */for (String pos : cfg
								.getConfigurationSection(region + ".data")
								.getKeys(false))
							if (!pos.contains("contents")) {
								/* 1587 */String[] data = cfg.getString(
										region + ".data." + pos).split("_");
								/* 1588 */String[] coords = pos.split("_");
								/* 1589 */Integer blockType = Integer
										.valueOf(Integer.parseInt(data[0]));
								byte blockData = (byte) Integer
										.parseInt(data[1]);
								/* 1590 */Block b = new Location(w, minx
										.doubleValue()
										+ Double.parseDouble(coords[0]), miny
										.doubleValue()
										+ Double.parseDouble(coords[1]), minz
										.doubleValue()
										+ Double.parseDouble(coords[2]))
										.getBlock();
								/*      */
								/* 1592 */if ((b.getTypeId() != blockType
										.intValue())
										|| (b.getData() != blockData)
										|| ((b.getState() instanceof InventoryHolder))) {
									/* 1593 */i++;
									if (i >= blockNum) {
										/* 1594 */if (i >= blockNum + blocksPer) {
											blockNum = i;
											if (notify != null)
												notify.sendMessage("restored "
														+ i
														+ " miscellaneous blocks so far...");
											return;
										}
										/* 1595 */b.setTypeIdAndData(
												blockType.intValue(),
												blockData, false);
										/* 1596 */if ((b.getState() instanceof InventoryHolder)) {
											/* 1597 */contents.clear();
											/*      */String item;
											/* 1597 */for (Iterator localIterator2 = cfg
													.getStringList(
															region
																	+ ".data."
																	+ pos
																	+ "_contents")
													.iterator(); localIterator2
													.hasNext(); contents
													.add(new CardboardBox(item)
															.unbox()))
												item = (String) localIterator2
														.next();
											/* 1598 */if (notify != null)
												notify.sendMessage("restoring "
														+ b.getType()
														+ " contents - "
														+ contents.size()
														+ " itemstacks");
											/* 1599 */Inventory inv = ((InventoryHolder) b
													.getState()).getInventory();
											inv.clear();
											inv.addItem((ItemStack[]) contents
													.toArray(new ItemStack[0]));
											/* 1600 */List c = Arrays
													.asList(inv.getContents());
											Collections.shuffle(c);
											inv.setContents((ItemStack[]) c
													.toArray(new ItemStack[0]));
											/*      */}
										/*      */}
									/*      */}
								/*      */}
						if (notify != null)
							notify.sendMessage("FINISHED! The region '"
									+ region + "' has been restored ("
									+ (n + i) + " blocks changed)");
						/* 1605 */Vitals.this.task("regionrestorebatch",
								Integer.valueOf(-1));
						/*      */}
					/*      */
				}
				/*      */, 0L, 100L);
		/*      */}

	/*      */private int regionrestoreArray(World w, String[] array, Material m,
			Double minx, Double miny, Double minz, Player notify) {
		/* 1610 */int blockNum = 0;
		/* 1611 */for (int i = 0; i < array.length; i += 3) {
			/* 1612 */Block b = new Location(w, minx.doubleValue()
					+ Double.parseDouble(array[i]), miny.doubleValue()
					+ Double.parseDouble(array[(i + 1)]), minz.doubleValue()
					+ Double.parseDouble(array[(i + 2)])).getBlock();
			/* 1613 */if (b.getType() != m) {
				blockNum++;
				b.setTypeIdAndData(m.getId(), (byte) 0, false);
			}
			/*      */}
		/* 1615 */if ((notify != null) && (blockNum > 0))
			notify.sendMessage("restored " + blockNum + " "
					+ m.toString().toLowerCase() + " blocks...");
		/* 1616 */return blockNum;
		/*      */}

	/*      */private int regionrestoreStart() {
		/* 1619 */final HashMap regions = new HashMap();
		/* 1620 */for (String region : regionrestoreFiles())
			if (config("regionrestore_" + region).getInt(region + ".interval") > 0) {
				/* 1621 */regions.put(region, Integer
						.valueOf(config("regionrestore_" + region).getInt(
								region + ".interval")));
				regionTimers.put(region, Integer.valueOf(0));
				/*      */}
		/* 1623 */return getServer().getScheduler().scheduleSyncRepeatingTask(
				this, new Runnable() {
					/*      */public void run() {
						/* 1625 */for (String region : regionTimers.keySet()) {
							/* 1626 */regionTimers.put(region, Integer
									.valueOf(((Integer) regionTimers
											.get(region)).intValue() + 1));
							/* 1627 */if (((Integer) regionTimers.get(region))
									.intValue() >= ((Integer) regions
									.get(region)).intValue()) {
								regionTimers.put(region, Integer.valueOf(0));
								Vitals.this.regionrestore(null, region);
								debug("[regionrestore] autorestored '" + region
										+ "' based on interval");
								return;
								/*      */}
							/*      */}
						/*      */}
					/*      */
				}
				/*      */, 1200L, 1200L);
		/*      */}

	/*      */
	/*      */private void serverlogarchive(Player p) {
		/* 1634 */String dataPath = getDataFolder().getAbsolutePath();
		/* 1635 */String logPath = dataPath.substring(0,
				dataPath.lastIndexOf("plugins"))
				+ "server.log";
		/* 1636 */File serverLog = new File(logPath);
		debug("[serverlogarchive] log exists? " + serverLog.exists());
		/* 1637 */File archiveFolder = new File(getDataFolder(),
				"serverlogarchive");
		/* 1638 */if (!archiveFolder.exists()) {
			debug("[serverlogarchive] creating archive folder");
			archiveFolder.mkdir();
		}
		/* 1639 */String archiveLogName = new SimpleDateFormat(
				"yyyyMMdd.HHmmss").format(new Date()) + ".log";
		/* 1640 */File archiveLog = new File(archiveFolder, archiveLogName);
		/* 1641 */FileChannel source = null;
		FileChannel destination = null;
		/*      */try {
			/* 1643 */if (!archiveLog.exists()) {
				debug("[serverlogarchive] creating archive file "
						+ archiveLogName);
				archiveLog.createNewFile();
			}
			/* 1644 */source = new FileInputStream(serverLog).getChannel();
			/* 1645 */destination = new FileOutputStream(archiveLog)
					.getChannel();
			/* 1646 */destination.transferFrom(source, 0L, source.size());
			/* 1647 */if (p != null)
				p.sendMessage("Successfully copied the server log to: plugins\\Vitals\\serverlogarchive\\"
						+ archiveLogName);
			/* 1648 */FileWriter outFile = new FileWriter(serverLog);
			/* 1649 */PrintWriter out = new PrintWriter(outFile);
			/* 1650 */out.println("");
			/* 1651 */if (p != null)
				p.sendMessage("Successfully cleared the server log.");
			/* 1652 */source.close();
			destination.close();
			debug("[serverlogarchive] archive complete.");
			/*      */}
		/*      */catch (IOException e) {
			/* 1655 */if (p != null)
				p.sendMessage("Severe: Could not archive the server log due to an IO Exception.");
			/* 1656 */log
					.severe("Could not archive the server log due to an IO Exception.");
			/* 1657 */e.printStackTrace();
			/*      */}
		/*      */}

	/*      */
	/*      */private int serverlogarchiveStart() {
		/* 1662 */return getServer().getScheduler().scheduleAsyncRepeatingTask(
				this, new Runnable() {
					/*      */public void run() {
						/* 1664 */long lastArchive = 0L;
						long archiveInterval = getConfig().getLong(
								"serverlogarchive_intervalhours") * 60L;
						/* 1665 */long keepTime = getConfig().getLong(
								"serverlogarchive_daystokeep") * 86400000L;
						/* 1666 */File archiveFolder = new File(
								getDataFolder(), "serverlogarchive");
						/* 1667 */if (archiveFolder.exists())
							for (File f : archiveFolder.listFiles()) {
								/* 1668 */if ((keepTime > 0L)
										&& (new Date().getTime()
												- f.lastModified() > keepTime))
									f.delete();
								/* 1669 */else if (f.lastModified() > lastArchive)
									lastArchive = f.lastModified();
								/*      */}
						/* 1671 */long lastArchiveDiff = (new Date().getTime() - lastArchive) / 1000L / 60L;
						/* 1672 */debug("[serverlogarchive] checking archives... last archive "
								+ lastArchiveDiff
								+ " minutes ago. interval = "
								+ archiveInterval + " minutes");
						/* 1673 */if (lastArchiveDiff > archiveInterval) {
							debug("[serverlogarchive] archiving...");
							Vitals.this.serverlogarchive(null);
							/*      */}
						/*      */}
					/*      */
				}
				/*      */, 600L, 12000L);
		/*      */}

	/*      */
	/*      */private int usefulcompassStart() {
		/* 1679 */return getServer().getScheduler().scheduleSyncRepeatingTask(
				this, new Runnable() {
					/*      */public void run() {
						/* 1681 */for (Player p : getServer()
								.getOnlinePlayers())
							if (p.getInventory().contains(Material.COMPASS))
								/* 1682 */if (p
										.hasPermission("v.usefulcompass")) {
									/* 1683 */World w = p.getWorld();
									Location l = p.getLocation();
									Location nearest = null;
									/* 1684 */for (Player pp : getServer()
											.getOnlinePlayers())
										if ((pp != p)
												&& (pp.getWorld() == w)
												&& ((nearest == null) || (l
														.distanceSquared(pp
																.getLocation()) < l
														.distanceSquared(nearest))))
											nearest = pp.getLocation();
									/* 1685 */if (nearest != null)
										p.setCompassTarget(nearest);
									/*      */}
								/* 1687 */else {
									p.setCompassTarget(new Location(p
											.getWorld(), 0.0D, 0.0D, 0.0D));
								}
						/*      */
						/*      */}
					/*      */
				}
				/*      */, 90L, 100L);
		/*      */}

	/*      */
	/*      */private void warnCmd(Player p, String[] args) {
		/* 1695 */if (args.length == 0) {
			/* 1696 */List recentwarnings = config("modwarnings")
					.getStringList("recentwarnings");
			/* 1697 */p.sendMessage(colorize("&c[Mod Warnings]"));
			/* 1698 */if (recentwarnings.size() == 0) {
				p.sendMessage("There haven't been any warnings issued.");
				/*      */return;
				/*      */}
			/* 1699 */String warning;
			/* 1699 */for (Iterator localIterator = recentwarnings.iterator(); localIterator
					.hasNext(); p.sendMessage(warning))
				warning = (String) localIterator.next();
			/*      */}
		/* 1700 */else if (args.length == 2) {
			/* 1701 */if (getServer().getPlayer(args[0]) == null) {
				p.sendMessage("There is no player online by that name.");
				return;
			}
			/* 1702 */if (perms.playerHas(getServer().getPlayer(args[0]),
					"v.warn.exempt")) {
				p.sendMessage("You can't warn that player.");
				return;
			}
			/* 1703 */String user = getServer().getPlayer(args[0]).getName()
					.toLowerCase();
			/* 1704 */String reason = "";
			for (int i = 1; i < args.length; i++)
				reason = reason + (i > 1 ? " " : "") + args[i];
			/* 1705 */int decrease = (reason.equalsIgnoreCase("decrease"))
					&& (p.hasPermission("v.warn.admin")) ? 1 : 0;
			/* 1706 */List warnings = config("modwarnings").getLongList(user);
			/* 1707 */for (Long warning : (Long[]) warnings
					.toArray(new Long[0]))
				/* 1708 */if ((new Date().getTime() - warning.longValue()) / 60000L > getConfig()
						.getLong("modwarnings_decayminutes")) {
					warnings.remove(warning);
					/* 1709 */} else if (decrease > 0) {
					decrease--;
					warnings.remove(warning);
					/*      */}
			/* 1711 */if (reason.equalsIgnoreCase("decrease")) {
				/* 1712 */if (!auth(p, "warn.admin")) {
					showUsage(p, "warn");
					return;
				}
				/* 1713 */p.sendMessage(args[0]
						+ "'s warning level has been decreased to "
						+ warnings.size());
				/* 1714 */config("modwarnings").set(user, warnings);
				/* 1715 */} else if (reason.equalsIgnoreCase("reset")) {
				/* 1716 */if (!auth(p, "warn.admin")) {
					showUsage(p, "warn");
					return;
				}
				/* 1717 */p.sendMessage(args[0]
						+ "'s warning level has been reset.");
				/* 1718 */warnings.clear();
				/* 1719 */config("modwarnings").set(user, warnings);
				/*      */} else {
				/* 1721 */warnings.add(Long.valueOf(new Date().getTime()));
				/* 1722 */config("modwarnings").set(user, warnings);
				/* 1723 */List recentwarnings = config("modwarnings")
						.getStringList("recentwarnings");
				/* 1724 */if (recentwarnings.size() > 10)
					recentwarnings.remove(0);
				recentwarnings.add(timestamp(new Date()) + ": " + user
						+ " warned by " + p.getName() + " for: " + reason);
				/* 1725 */config("modwarnings").set("recentwarnings",
						recentwarnings);
				/* 1726 */String action = getConfig().getString(
						"modwarnings_level" + warnings.size());
				String cmd = "";
				/* 1727 */if ((action != null) && (action.length() > 1)) {
					/* 1728 */cmd = action.substring(1).replaceAll(
							"\\{player\\}", user);
					/* 1729 */getServer().dispatchCommand(
							getServer().getConsoleSender(), cmd);
					/*      */}
				/* 1731 */debug("[modwarnings] executing command [Warning Level "
						+ warnings.size() + ":" + cmd + "]");
				/* 1732 */getServer().broadcastMessage(
						colorize("&c-------------------------"));
				/* 1733 */getServer().broadcastMessage(
						colorize("&c" + user + ", you have been warned by "
								+ p.getName() + " for: " + reason));
				/* 1734 */getServer().broadcastMessage(
						colorize("&cWarning level: "
								+ warnings.size()
								+ (cmd == null ? "" : new StringBuilder(
										". Action taken: ").append(cmd)
										.toString())));
				/* 1735 */getServer().broadcastMessage(
						colorize("&c-------------------------"));
				/*      */}
			/* 1737 */saveConfig("modwarnings");
			/*      */} else {
			/* 1739 */showUsage(p, "warn");
			/*      */}
		/*      */}

	/*      */
	/* 1743 */private int warnLevel(Player p) {
		List warnings = config("modwarnings").getLongList(
				p.getName().toLowerCase());
		int level = warnings.size();
		/* 1744 */for (Long warning : (Long[]) warnings.toArray(new Long[0])) {
			/* 1745 */if ((new Date().getTime() - warning.longValue()) / 60000L > getConfig()
					.getLong("modwarnings_decayminutes"))
				warnings.remove(warning);
			/*      */}
		/* 1747 */if (warnings.size() != level) {
			config("modwarnings").set(p.getName().toLowerCase(), warnings);
			saveConfig("modwarnings");
		}
		/* 1748 */return warnings.size();
	}

	/*      */
	/*      */private void wordswap(Player p, String[] args)
	/*      */{
		/* 1752 */if (args.length == 0) {
			p.sendMessage(colorize("&c[Wordswap List]"));
			/*      */String word;
			/* 1752 */for (Iterator localIterator = config("wordswap").getKeys(
					false).iterator(); localIterator.hasNext(); p
					.sendMessage(word + ": "
							+ config("wordswap").getString(word)))
				word = (String) localIterator.next();
			return;
		}
		/* 1753 */if ((args.length == 1)
				&& (config("wordswap").contains(args[0]))) {
			config("wordswap").set(args[0], null);
			saveConfig("wordswap");
			p.sendMessage("Word swap deleted.");
			return;
		}
		/* 1754 */if (args.length == 2) {
			config("wordswap").set(args[0], args[1]);
			saveConfig("wordswap");
			p.sendMessage("Word swap set.");
			return;
		}
		/*      */}

	/*      */
	/*      */private void vtoggle(CommandSender sender, String module) {
		/* 1758 */if ((module.equalsIgnoreCase("allon"))
				|| (module.equalsIgnoreCase("alloff"))) {
			/* 1759 */for (String key : getConfig().getKeys(false))
				if (key.indexOf("_") == -1)
					getConfig().set(key,
							Boolean.valueOf(module.equalsIgnoreCase("allon")));
			/* 1760 */writeConfig();
			init();
			sender.sendMessage(toString()
					+ " - All modules have been "
					+ (module.equalsIgnoreCase("allon") ? "enabled"
							: "disabled"));
			return;
			/*      */}
		/* 1762 */if (getConfig().getString(module) == null) {
			sender.sendMessage("That is not a valid Vitals module. Type /vhelp");
			return;
		}
		/* 1763 */getConfig().set(module,
				Boolean.valueOf(!getConfig().getBoolean(module)));
		/* 1764 */writeConfig();
		init();
		sender.sendMessage(toString() + " module [" + module + "] has been "
				+ (getConfig().getBoolean(module) ? "enabled" : "disabled"));
		/*      */}

	/*      */private void vsetting(CommandSender sender, String setting,
			String value) {
		/* 1767 */if (value.equals("true"))
			getConfig().set(setting, Boolean.valueOf(true));
		/* 1768 */else if (value.equals("false"))
			getConfig().set(setting, Boolean.valueOf(false));
		/* 1769 */else if (value.equals("null"))
			getConfig().set(setting, null);
		else
			/*      */try {
				/* 1771 */getConfig().set(setting,
						Double.valueOf(Double.parseDouble(value)));
			} catch (Exception e) {
				/* 1772 */getConfig().set(setting, value);
				/*      */}
		/* 1774 */writeConfig();
		init();
		sender.sendMessage(toString() + " setting has been changed.");
		/*      */}

	/*      */private void vhelp(CommandSender s, String option, String param) {
/* 1777 */     if ((option == null) || (option.equalsIgnoreCase("settings"))) {
/* 1778 */       String info = ""; String invalid = ""; s.sendMessage(colorize("&c[" + toString() + "]"));
/* 1779 */       for (String key : getConfig().getKeys(false)) {
/* 1780 */         if (key.indexOf("_") == -1) {
/* 1781 */           info = info + (info.length() > 0 ? " " : "") + (getConfig().getBoolean(key) ? "&a" : "&7") + key;
/*      */         }
/*      */       }
/* 1784 */       s.sendMessage(colorize(info));
/* 1785 */       if (invalid.length() > 0) s.sendMessage(colorize("&cSETTINGS NOT IN THE RIGHT SECTION: " + invalid));
/*      */     }
/*      */     else
/*      */     {
/*      */       int numMotionless;
/*      */       World world;
/* 1786 */       if (option.equalsIgnoreCase("minecarts")) {
/* 1787 */         int numMinecarts = 0; numMotionless = 0;
/*      */         Iterator localIterator2;
/* 1788 */         for (Iterator<World> unknown = getServer().getWorlds().iterator(); unknown.hasNext(); 
/* 1789 */           localIterator2.hasNext())
/*      */         {
/* 1788 */           world = (World)unknown.next();
/* 1789 */           localIterator2 = world.getEntities().iterator(); continue; Entity entity = (Entity)localIterator2.next();
/* 1790 */           if (((entity instanceof Minecart)) && (!(entity instanceof StorageMinecart)) && (!(entity instanceof PoweredMinecart))) {
/* 1791 */             numMinecarts++; if (entity.getVelocity().length() == 0.0D) numMotionless++;
/* 1792 */             if ((param != null) && (numMinecarts == Integer.parseInt(param))) {
/* 1793 */               s.sendMessage("Info on minecart #" + numMinecarts + ": velocity=" + entity.getVelocity().length() + " passenger=" + entity.getPassenger());
/* 1794 */               if ((s instanceof Player)) playerTeleport((Player)s, entity.getLocation(), null);
/*      */             }
/*      */           }
/*      */         }
/*      */ 
/* 1799 */         s.sendMessage(numMinecarts + " minecarts exist, " + numMotionless + " are motionless");
/* 1800 */       } else if (option.equalsIgnoreCase("memory")) {
/* 1801 */         s.sendMessage(colorize("&c[" + toString() + " Memory Usage]"));
/* 1802 */         s.sendMessage(configs.size() + " configs: " + configs.keySet());
/* 1803 */         s.sendMessage(tasks.size() + " tasks: " + tasks.keySet());
/* 1804 */         s.sendMessage(unusedCarts.size() + " unusedCarts: " + unusedCarts.keySet());
/* 1805 */         s.sendMessage(teleportTasks.size() + " teleportTasks: " + teleportTasks.keySet());
/* 1806 */         s.sendMessage(flyingTasks.size() + " flyingTasks: " + flyingTasks.keySet());
/* 1807 */         s.sendMessage(chainmailNotify.size() + " chainmailNotify: " + chainmailNotify.keySet());
/* 1808 */         s.sendMessage(customWarps.size() + " customWarps: " + customWarps.keySet());
/* 1809 */         s.sendMessage(regionTimers.size() + " regionTimers: " + regionTimers.keySet());
/* 1810 */         s.sendMessage(regionlabelPlayers.size() + " regionlabelPlayers: " + regionlabelPlayers.keySet());
/* 1811 */         s.sendMessage(deathretentionInventory.size() + " deathretentionInventory: " + deathretentionInventory.keySet());
/* 1812 */         s.sendMessage(deathretentionArmor.size() + " deathretentionArmor: " + deathretentionArmor.keySet());
/* 1813 */         s.sendMessage(announcements.size() + " announcements");
/* 1814 */         s.sendMessage(regionprotectIgnore.size() + " regionprotectIgnore: " + regionprotectIgnore);
/* 1815 */         s.sendMessage(regionprotectRegions.size() + " regionprotectRegions: " + regionprotectRegions.keySet());
/* 1816 */       } else if (option.equalsIgnoreCase("tasks")) {
/* 1817 */         s.sendMessage(colorize("&c[" + toString() + " Running Tasks]"));
/*      */         String task;
/* 1818 */         for (numMotionless = tasks.keySet().iterator(); numMotionless.hasNext(); s.sendMessage(task + " [ID " + tasks.get(task) + "]")) task = (String)numMotionless.next(); 
/*      */       }
/* 1819 */       else if (option.equalsIgnoreCase("events")) {
/* 1820 */         s.sendMessage(colorize("&c[" + toString() + " Event Log]"));
/* 1821 */         showLatestFileEntries(s, "events.log", 9);
/*      */       } else {
/* 1823 */         List msg = new ArrayList();
/* 1824 */         for (String key : getConfig().getKeys(false)) {
/* 1825 */           if (key.startsWith(option + "_")) msg.add(key + ": " + getConfig().getString(key));
/*      */         }
/* 1827 */         if (msg.size() == 0) { s.sendMessage("No settings available for a module by that name."); return; }
/* 1828 */         s.sendMessage(colorize("&c[Vitals module " + option.toUpperCase() + " is currently " + (enabled(option) ? "ENABLED" : "DISABLED") + "]"));
/*      */         String line;
/* 1829 */         for (world = (World) msg.iterator(); ((Iterator) world).hasNext(); s.sendMessage(line)) line = (String)((Iterator) world).next(); 
/*      */       }
/*      */     }
/*      */   }

	/*      */
	/* 1834 */private void playerTeleport(final Player p,
			final Location location, final Runnable callback) {
		final int delay = getConfig().getInt("global_teleportdelayseconds");
		final Location pLoc = p.getLocation();
		/* 1835 */if (delay < 0) {
			log.severe(toString()
					+ ": Invalid configuration, global_teleportdelayseconds should be 0 or higher");
			return;
		}
		/* 1836 */if (delay > 0)
			p.sendMessage(colorize("&cTeleport in progress, don't move for "
					+ delay + " seconds..."));
		/* 1837 */teleportTasks.put(p.getName(), Integer.valueOf(getServer()
				.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
					/* 1838 */int timer = 0;

					/*      */
					/* 1840 */public void run() {
						boolean cancelTask = false;
						/* 1841 */if ((timer++ < delay) && (p.isOnline())) {
							/* 1842 */if (p.getLocation().distanceSquared(pLoc) > 0.01D) {
								cancelTask = true;
								p.sendMessage(Vitals
										.colorize("&cYou moved! Teleport cancelled."));
							}
							/*      */} else {
							/* 1844 */cancelTask = true;
							if (p.isOnline()) {
								p.teleport(location);
								if (callback != null)
									callback.run();
							}
							/*      */
							/*      */}
						/* 1846 */if ((cancelTask)
								&& (teleportTasks.containsKey(p.getName()))) {
							getServer().getScheduler().cancelTask(
									((Integer) teleportTasks.get(p.getName()))
											.intValue());
							teleportTasks.remove(p.getName());
							/*      */}
						/*      */}
					/*      */
				}
				/*      */, 0L, 20L)));
	}

	/*      */
	/*      */public static int getHighestFreeBlockAt(World world, int posX, int posZ) {
		/* 1851 */int maxHeight = world.getMaxHeight();
		int searchedHeight = maxHeight - 1;
		Block lastBlock = null;
		/* 1852 */for (; searchedHeight > 0;
		/* 1855 */searchedHeight--)
		/*      */{
			/* 1853 */Block block = world
					.getBlockAt(posX, searchedHeight, posZ);
			/* 1854 */if ((lastBlock != null)
					&& (lastBlock.getType() == Material.AIR)
					&& (block.getType() != Material.AIR))
				break;
			/* 1855 */lastBlock = block;
			/*      */}
		/* 1857 */searchedHeight++;
		return searchedHeight;
		/*      */}

	/*      */private String loadText(File textFile) {
		/* 1860 */StringBuilder contents = new StringBuilder();
		/*      */try {
			/* 1862 */BufferedReader input = new BufferedReader(new FileReader(
					textFile));
			/*      */try {
				for (String line = null; (line = input.readLine()) != null; contents
						.append(line + "\n"))
					;
			} finally {
				/* 1864 */input.close();
				/*      */}
			/*      */} catch (IOException e) {
			log.severe("Could not load file '" + textFile.getName()
					+ "' from plugin data folder.");
			e.printStackTrace();
		}
		/* 1867 */return contents.toString();
		/*      */}

	/*      */private boolean saveText(String text, File textFile) {
		/*      */try {
			PrintWriter out = new PrintWriter(new FileWriter(textFile));
			out.print(text);
			out.flush();
			out.close();
			return true;
		} catch (IOException e) {
			/* 1871 */log.severe("Could not save file '" + textFile.getName()
					+ "' to plugin data folder.");
			e.printStackTrace();
		}
		return false;
		/*      */}

	/*      */private boolean saveText(String[] lines, File textFile) {
		/*      */try {
			PrintWriter out = new PrintWriter(new FileWriter(textFile));
			for (String line : lines)
				out.println(line);
			out.flush();
			out.close();
			return true;
		} catch (IOException e) {
			/* 1875 */log.severe("Could not save file '" + textFile.getName()
					+ "' to plugin data folder.");
			e.printStackTrace();
		}
		return false;
		/*      */}

	/*      */private boolean appendText(String text, File textFile) {
		/*      */try {
			PrintWriter out = new PrintWriter(new FileWriter(textFile, true));
			out.println(text);
			out.flush();
			out.close();
			return true;
		} catch (IOException e) {
			/* 1879 */log.severe("Could not save file '" + textFile.getName()
					+ "' to plugin data folder.");
			e.printStackTrace();
		}
		return false;
		/*      */}

	/* 1881 */private void writeConfig() {
		writeConfig(getConfig(), "config.yml");
	}

	/*      */private void writeConfig(FileConfiguration cfg, String file) {
		/*      */try {
			/* 1884 */PrintWriter out = new PrintWriter(new FileWriter(
					new File(getDataFolder(), file)));
			/* 1885 */BufferedReader in = new BufferedReader(
					new InputStreamReader(getResource(file)));
			/* 1886 */YamlConfiguration def = new YamlConfiguration();
			/* 1887 */String line = in.readLine();
			int lineCount = 0;
			/* 1888 */for (; line != null;
			/* 1905 */line = in.readLine())
			/*      */{
				/* 1889 */if ((line.indexOf(":") == -1)
						|| (line.substring(0, 1).equals("#"))) {
					/* 1890 */out.println(line);
					/*      */} else {
					/* 1892 */String[] splitColon = line.split(":");
					String[] splitPound = line.split("#");
					/* 1893 */Object value = cfg.get(splitColon[0]);
					/* 1894 */if (value == null)
						try {
							/* 1895 */def.loadFromString(line);
							value = def.get(splitColon[0]);
							cfg.set(splitColon[0], value);
							/*      */} catch (Exception localException) {
							/*      */}
					if ((value instanceof String))
						value = "'" + value + "'";
					/* 1898 */String output = splitColon[0] + ": " + value;
					/* 1899 */if (splitPound.length > 1) {
						/* 1900 */while (output.length() < 46)
							output = output + " ";
						/* 1901 */output = output + " #" + splitPound[1];
						/*      */}
					/* 1903 */out.println(output);
					/*      */}
				/* 1905 */lineCount++;
				/*      */}
			/* 1907 */in.close();
			out.flush();
			out.close();
			debug(lineCount + " lines written to " + file);
			/*      */} catch (IOException e) {
			/* 1909 */log.severe("Could not save configuration file '" + file
					+ "'.");
			e.printStackTrace();
			/*      */}
		/*      */}

	/* 1912 */private void showLatestFileEntries(CommandSender sender,
			String filename, int numToShow) {
		File f = new File(getDataFolder(), filename);
		/* 1913 */if (!f.exists()) {
			sender.sendMessage(colorize("&7No entries."));
			return;
		}
		/* 1914 */String[] data = loadText(f).split("\n");
		/* 1915 */for (int i = numToShow; i >= 1; i--)
			if ((data.length - i >= 0)
					&& (data[(data.length - i)].length() > 0)) {
				/* 1916 */String s = data[(data.length - i)];
				/* 1917 */if (filename.contains(".log"))
					s = s.replaceAll(datestamp(new Date()), "");
				/* 1918 */sender.sendMessage(colorize("&7" + s));
				/*      */}
		/*      */}
	/*      */
}