package com.gabezter4.Vitals;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
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
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class VitalListener extends Vitals implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (enabled("onlineplayersflatfile"))
			onlinePlayers(null);
		if (enabled("betternews"))
			betternews(event.getPlayer(),
					getConfig().getInt("betternews_showonlogin"));
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (enabled("onlineplayersflatfile"))
			onlinePlayers(event.getPlayer());
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.getFrom().getBlock().equals(e.getTo().getBlock()))
			return;
		Player p = e.getPlayer();
		Material steppingOn = e.getTo().getBlock().getRelative(0, -1, 0)
				.getType();
		if ((steppingOn == Material.SPONGE)
				&& (enabled("gameplay_bouncysponges"))
				&& (p.hasPermission("v.bouncysponges"))) {
			Vector v = p.getVelocity();
			v.setY(2);
			p.setVelocity(v);
			return;
		}
		if ((steppingOn == Material.EMERALD_BLOCK)
				&& (enabled("gameplay_emeraldblockhaste"))
				&& (p.hasPermission("v.emeraldblockhaste"))) {
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 2),
					true);
			return;
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		if ((!regionprotectIgnore.contains(e.getBlock().getTypeId()))
				&& (!perms.playerHas(e.getPlayer(), "v.regionprotect.bypass"))
				&& (regionprotected(e.getBlock().getLocation()))) {
			e.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		if ((!regionprotectIgnore.contains(e.getBlock().getTypeId()))
				&& (!perms.playerHas(e.getPlayer(), "v.regionprotect.bypass"))
				&& (regionprotected(e.getBlock().getLocation()))) {
			e.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent e) {
		if ((e.getEntityType() == EntityType.VILLAGER)
				&& (getConfig().getString("gameplay_villagerprofessions")
						.length() > 0)) {
			final Villager v = (Villager) e.getEntity();
			getServer().getScheduler().scheduleAsyncDelayedTask(this,
					new Runnable() {
						public void run() {
							List bannedProfessions = Arrays.asList(getConfig()
									.getString("gameplay_villagerprofessions")
									.toUpperCase().split(","));
							Villager.Profession[] prof = Villager.Profession
									.values();
							String pr = v.getProfession().toString();
							debug("Villager " + v.getProfession()
									+ " spawned, banned is "
									+ bannedProfessions);
							if (bannedProfessions.size() == prof.length) {
								debug("[VillagerProfessions] villager spawned, all professions are disabled, removing villager");
								v.remove();
								return;
							}
							while (bannedProfessions.contains(pr)) {
								v.setProfession(prof[Vitals.random
										.nextInt(prof.length)]);
							}
							debug("[VillagerProfessions] villager spawned as "
									+ pr + ", new profession is "
									+ v.getProfession().toString());
						}
					}, 1L);
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		if ((arenaActive != null) && (arenaActive.state.equals("active"))
				&& (arenaActive.playerAlive(p))) {
			arenaActive.playerDisqualify(p, null);
			return;
		}
		if ((enabled("bounties"))
				&& ((arenaActive == null) || (!arenaActive.playerAlive(e
						.getEntity())))
				&& ((e.getEntity().getKiller() instanceof Player))
				&& (e.getEntity().getKiller() != e.getEntity())) {
			String pName = e.getEntity().getName().toLowerCase();
			String kName = e.getEntity().getKiller().getName().toLowerCase();
			bountyDeath(kName, pName);
		}
		if (enabled("gameplay_deathretention"))
			deathretention(e, p);
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		Player p = e.getPlayer();
		if (deathretentionInventory.containsKey(p.getName())) {
			ItemStack i;
			for (Iterator localIterator = ((List) deathretentionInventory.get(p
					.getName())).iterator(); localIterator.hasNext(); p
					.getInventory().addItem(new ItemStack[] { i }))
				i = (ItemStack) localIterator.next();
			deathretentionInventory.remove(p.getName());
		}
		if (deathretentionArmor.containsKey(p.getName())) {
			p.getInventory().setArmorContents(
					(ItemStack[]) ((List) deathretentionArmor.get(p.getName()))
							.toArray(new ItemStack[0]));
			deathretentionArmor.remove(p.getName());
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		final Player p = e.getPlayer();
		Location l = p.getLocation();
		if ((arenaActive != null)
				&& (arenaActive.event.equals("RaceToTheFinish"))
				&& (e.hasItem())
				&& (e.getItem().getType() == Material.ENDER_PEARL)) {
			e.setCancelled(true);
			return;
		}
		if ((enabled("gameplay_blazerodfireball"))
				&& (e.hasItem())
				&& (e.getItem().getType() == Material.BLAZE_ROD)
				&& ((e.getAction() == Action.RIGHT_CLICK_BLOCK) || (e
						.getAction() == Action.RIGHT_CLICK_AIR))
				&& (auth(p, "blazerodfireball"))) {
			removeItem(p, Material.BLAZE_ROD);
			Location loc = p.getEyeLocation().toVector()
					.add(l.getDirection().multiply(2))
					.toLocation(p.getWorld(), l.getYaw(), l.getPitch());
			p.getWorld().spawn(loc, Fireball.class);
		}
		if ((enabled("gameplay_featherfly"))
				&& (e.hasItem())
				&& (e.getItem().getType() == Material.FEATHER)
				&& ((e.getAction() == Action.RIGHT_CLICK_BLOCK) || (e
						.getAction() == Action.RIGHT_CLICK_AIR))
				&& (auth(p, "featherfly"))) {
			if (p.isFlying()) {
				p.setAllowFlight(false);
				p.sendMessage(lang("featherfly_cancel"));
				msgNearby(p, p.getName() + lang("featherfly_cancelothers"));
				if (flyingTasks.containsKey(p.getName())) {
					getServer().getScheduler()
							.cancelTask(
									((Integer) flyingTasks.get(p.getName()))
											.intValue());
					flyingTasks.remove(p.getName());
				}
				return;
			}
			l.add(0.0D, 1.0D, 0.0D);
			p.teleport(l);
			p.setAllowFlight(true);
			p.setFlying(true);
			removeItem(p, Material.FEATHER);
			p.sendMessage(lang("featherfly_effect"));
			msgNearby(p, p.getName() + lang("featherfly_effectothers"));
			flyingTasks.put(p.getName(), Integer.valueOf(getServer()
					.getScheduler().scheduleSyncRepeatingTask(
							this,
							new Runnable() {
								public void run() {
									debug("[" + p + ", " + p.getName() + ", "
											+ flyingTasks.size() + "]");
									if (!p.isOnline()) {
										getServer()
												.getScheduler()
												.cancelTask(
														((Integer) flyingTasks.get(p
																.getName()))
																.intValue());
										flyingTasks.remove(p.getName());
									} else if ((!p.isFlying())
											|| (!removeItem(p, Material.FEATHER))) {
										p.setAllowFlight(false);
										p.sendMessage(lang("featherfly_cancel"));
										Vitals.msgNearby(
												p,
												p.getName()
														+ Vitals.access$0(this,
																"featherfly_cancelothers"));
										getServer()
												.getScheduler()
												.cancelTask(
														((Integer) flyingTasks.get(p
																.getName()))
																.intValue());
										flyingTasks.remove(p.getName());
									}
								}
							},
							getConfig().getLong("gameplay_featherfly_interval") * 20L,
							getConfig().getLong("gameplay_featherfly_interval") * 20L)));
			return;
		}
		if (e.isCancelled())
			return;
		if ((arenaActive != null) && (arenaActive.state.equals("setup"))) {
			arenaActive.setup(e);
			return;
		}
		if ((setup != null) && (p == setupOp)
				&& (e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			setup(p, setup, setupName, setupOption, setupStep,
					e.getClickedBlock());
			return;
		}
		if ((enabled("gameplay_safejukebox"))
				&& (p.hasPermission("v.safejukebox"))
				&& (e.getAction() == Action.RIGHT_CLICK_BLOCK)
				&& (e.getClickedBlock().getType() == Material.JUKEBOX)
				&& (e.hasItem()) && (isRecord(e.getItem().getType()))) {
			e.setCancelled(true);
			p.getWorld().playEffect(e.getClickedBlock().getLocation(),
					Effect.RECORD_PLAY, e.getItem().getTypeId());
			return;
		}
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
		if ((enabled("gameplay_villagerpermission"))
				&& ((e.getRightClicked() instanceof Villager))
				&& (!e.getPlayer().hasPermission("v.villagerpermission")))
			e.setCancelled(true);
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent e) {
		if ((enabled("anticaps"))
				&& (!perms.playerHas(e.getPlayer(), "v.anticaps.bypass"))
				&& (e.getMessage().length() >= 10)) {
			int numCaps = 0;
			for (int i = 0; i < e.getMessage().length(); i++)
				if (Character.isUpperCase(e.getMessage().charAt(i)))
					numCaps++;
			if (1.0D * numCaps / e.getMessage().length() >= getConfig()
					.getDouble("anticaps_cutoffpercent") / 100.0D) {
				e.setCancelled(true);
				e.getPlayer().sendMessage(lang("anticaps"));
				return;
			}
		}
		boolean global = false;
		for (String keyword : getConfig()
				.getString("chatworlds_globalkeywords").split(","))
			if (e.getMessage().toLowerCase().contains(keyword.toLowerCase()))
				global = true;
		if ((enabled("chatworlds")) && (!global)
				&& (e.getRecipients().size() > 1))
			for (Player p : (Player[]) e.getRecipients().toArray(new Player[0]))
				if ((p.getWorld() != e.getPlayer().getWorld())
						&& (!p.hasPermission("v.chatworlds.bypass"))
						&& (!perms.playerHas(e.getPlayer(),
								"v.chatworlds.bypass")))
					e.getRecipients().remove(p);
		Object domainRegex;
		if ((enabled("antiadvertising"))
				&& (!perms.playerHas(e.getPlayer(), "v.antiadvertising.bypass"))) {
			String ipRegex = "(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])";
			domainRegex = "[a-z0-9]+([\\-\\.]{1}[a-z0-9]+)*\\.[a-z]{2,6}";
			String[] whitelist = getConfig().getString(
					"antiadvertising_whitelist").split(",");
			String msg = e.getMessage();
			for (int i = 0; i < whitelist.length; i++)
				msg = msg.replaceAll(whitelist[i], "~~~" + i + "~~~");
			if ((msg.matches(".*" + ipRegex + ".*"))
					|| (msg.matches(".*" + (String) domainRegex + ".*"))) {
				debug("advertising detected, taking action ["
						+ getConfig().getString("antiadvertising_action") + "]");
				if (getConfig().getString("antiadvertising_action").equals(
						"mute")) {
					e.setCancelled(true);
					return;
				}
				if (getConfig().getString("antiadvertising_action").equals(
						"mute")) {
					e.setCancelled(true);
					e.getPlayer().kickPlayer("");
					return;
				}
				if (getConfig().getString("antiadvertising_action").equals(
						"secretmute")) {
					Set r = e.getRecipients();
					r.clear();
					r.add(e.getPlayer());
					for (Player p : getServer().getOnlinePlayers())
						if (p.isOp()) {
							r.add(p);
							p.sendMessage(colorize("&6[AntiAdvertising] Muted message for everyone except sender and ops"));
						}
				} else if (getConfig().getString("antiadvertising_action")
						.equals("replace")) {
					msg = msg.replaceAll(ipRegex, "").replaceAll(
							(String) domainRegex, "");
					for (int i = 0; i < whitelist.length; i++)
						msg = msg.replaceAll("~~~" + i + "~~~", whitelist[i]);
					e.setMessage(msg);
				}
			}
		}
		if (enabled("antistickykeys"))
			e.setMessage(e.getMessage().replaceAll("(.)\\1{4,}", "$1"));
		if (enabled("wordswap")) {
			String word;
			for (domainRegex = config("wordswap").getKeys(false).iterator(); ((Iterator) domainRegex)
					.hasNext(); e.setMessage(e.getMessage().replaceAll(
					"(?i)" + word, config("wordswap").getString(word))))
				word = (String) ((Iterator) domainRegex).next();
		}
		if (enabled("helperbot"))
			for (domainRegex = config("helperbot").getKeys(false).iterator(); ((Iterator) domainRegex)
					.hasNext();) {
				String wordpair = (String) ((Iterator) domainRegex).next();
				boolean bot = true;
				String[] botWords = wordpair.split("_");
				String[] chatWords = e.getMessage().split(" ");
				if (!botWords[0].equals("command")) {
					final String response = config("helperbot").getString(
							wordpair);
					for (String botWord : botWords) {
						boolean wordFound = false;
						for (int i = 0; i < chatWords.length; i++)
							if (chatWords[i].toLowerCase().equals(
									botWord.toLowerCase()))
								wordFound = true;
						if (!wordFound)
							bot = false;
					}
					if (bot)
						getServer().getScheduler().scheduleAsyncDelayedTask(
								this, new Runnable() {
									public void run() {
										getServer()
												.broadcastMessage(
														Vitals.colorize(getConfig()
																.getString(
																		"helperbot_chatprefix")
																+ response));
									}

								}, 20L);
				}
			}
		if ((enabled("modwarnings")) && (enabled("modwarnings_showlevel"))) {
			int warningLevel = warnLevel(e.getPlayer());
			if (warningLevel > 0) {
				char[] s = new char[warningLevel];
				Arrays.fill(s, '*');
				String stars = new String(s);
				e.setMessage(colorize(getConfig().getString(
						"modwarnings_chatprefix").replaceFirst("\\*", stars))
						+ " " + e.getMessage());
			}
		}
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		if (enabled("regionlabels")) {
			String toRegion = regionGet("regionlabels", e.getTo());
			if (toRegion != null)
				e.getPlayer()
						.sendMessage(
								colorize(lang("regionlabels_enter").replaceAll(
										"\\{region\\}",
										toRegion.replaceAll("_", " "))));
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onEntityDamage(EntityDamageEvent e) {
		if (((e.getEntity() instanceof Villager))
				&& (enabled("unkillablevillagers"))) {
			e.setCancelled(true);
			return;
		}
		if ((e.getEntity() instanceof Player)) {
			Player p = (Player) e.getEntity();
			if ((enabled("gameplay_damagestopsflying")) && (p.isFlying()))
				p.setFlying(false);
			if ((enabled("gameplay_bouncysponges"))
					&& (e.getCause() == EntityDamageEvent.DamageCause.FALL)
					&& (p.getLocation().getBlock().getRelative(0, -1, 0)
							.getType() == Material.SPONGE)) {
				e.setCancelled(true);
				return;
			}
			if ((enabled("gameplay_emeraldblockhaste"))
					&& (e.getCause() == EntityDamageEvent.DamageCause.FALL)
					&& (p.getLocation().getBlock().getRelative(0, -1, 0)
							.getType() == Material.EMERALD_BLOCK)) {
				e.setCancelled(true);
				return;
			}
			if ((arenaActive != null) && (arenaActive.playerAlive(p))) {
				if (arenaActive.event.equals("RaceToTheFinish")) {
					e.setCancelled(true);
					return;
				}
				if (((e instanceof EntityDamageByEntityEvent))
						&& ((((EntityDamageByEntityEvent) e).getDamager() instanceof Player))) {
					if (arenaActive.state.equals("signup")) {
						e.setCancelled(true);
						return;
					}
					EntityDamageByEntityEvent e2 = (EntityDamageByEntityEvent) e;
					Player p2 = (Player) e2.getDamager();

					if ((arenaActive.event.equals("Team PVP"))
							&& (arenaActive.playerAlive(p2))
							&& (arenaActive.sameTeam(p, p2))) {
						e.setCancelled(true);
						return;
					}
				}
			}
			if ((getConfig().getDouble("gameplay_superchainmail") != 1.0D)
					&& (p.hasPermission("v.superchainmail"))) {
				Double damageFactor = Double.valueOf(1.0D);
				int numChainmail = 0;
				for (ItemStack armor : p.getInventory().getArmorContents())
					if (isChainmail(armor.getType())) {
						numChainmail++;
						damageFactor = Double.valueOf(damageFactor
								.doubleValue()
								* getConfig().getDouble(
										"gameplay_superchainmail"));
					}
				long lastNotify = chainmailNotify.containsKey(p.getName()) ? (new Date()
						.getTime() - ((Long) chainmailNotify.get(p.getName()))
						.longValue()) / 1000L : 99999999L;
				if ((numChainmail > 0) && (lastNotify > 10L)) {
					chainmailNotify.put(p.getName(),
							Long.valueOf(new Date().getTime()));
					p.sendMessage(colorize("&7"
							+ lang("superchainmail_effect")
							+ Math.round(100.0D * (1.0D - damageFactor
									.doubleValue())) + "%!"));
				}
				e.setDamage((int) Math.round(damageFactor.doubleValue()
						* e.getDamage()));
			}
		}
	}

}
