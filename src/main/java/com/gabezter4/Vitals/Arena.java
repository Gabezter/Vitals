package com.gabezter4.Vitals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Arena
  implements Runnable
{
  private static String[] events = { "Spleef", "DoubleSpleef", "Naked", "Ironman", "Archery", "Diamond", "Team", "HungerGames", "RaceToTheFinish" }; private static String[] teamNames = { "Brown", "Gray", "Blue", "Gold" };
  private Vitals vitals;
  private Random random = new Random();
  String event;
  String state;
  Location spectator;
  Block finish;
  private String arena;
  private World world;
  private List<Integer> prizes;
  private int task = -1;
  private int interval;
  private int setupState;
  private int numPlayers = 0; private int teamIterator = 0; private int runCount = 0;
  private long stateTime;
  private boolean decay;
  private Player admin;
  private Player arenaFirst;
  private Player arenaSecond;
  private Player arenaThird;
  private Set<String> players = new HashSet();
  private HashMap<String, String> teams = new HashMap();
  private HashMap<Integer, List<String>> positions = new HashMap();
  private Double x1 = Double.valueOf(0.0D); private Double y1 = Double.valueOf(0.0D); private Double z1 = Double.valueOf(0.0D); private Double x2 = Double.valueOf(0.0D); private Double y2 = Double.valueOf(0.0D); private Double z2 = Double.valueOf(0.0D); private Double minx = Double.valueOf(0.0D); private Double maxx = Double.valueOf(0.0D); private Double miny = Double.valueOf(0.0D); private Double maxy = Double.valueOf(0.0D); private Double minz = Double.valueOf(0.0D); private Double maxz = Double.valueOf(0.0D); List<String> pos1 = null; List<String> pos2 = null;

  public Arena(Vitals plugin, String arenaSetup, Player admin) { this.admin = admin; vitals = plugin; arena = arenaSetup; event = "setup"; state = "setup"; setupState = 0; admin.sendMessage("Arena setup mode activated for arena '" + arena + "'. Right-click the spectator position."); }

  public Arena(Vitals plugin, String eventName, int arenaNumber) {
    vitals = plugin;
    if (!eventName.equals("")) { event = eventName;
    } else {
      int chanceTotal = 0; for (int i = 0; i < events.length; i++) chanceTotal += vitals.getConfig().getInt("arena_chance" + events[i]);
      int rnd = random.nextInt(chanceTotal); int chanceCounter = 0;
      for (int i = 0; i < events.length; i++) { chanceCounter += vitals.getConfig().getInt("arena_chance" + events[i]); if (rnd < chanceCounter) { event = events[i]; break; } }
    }
    if (event.toLowerCase().contains("spleef")) { arena = "spleef";
    } else if (event.toLowerCase().contains("hunger")) { event = "HungerGames"; arena = "hungergames";
    } else if (event.toLowerCase().contains("race")) { event = "RaceToTheFinish"; arena = ("race" + (arenaNumber > 0 ? arenaNumber : random.nextInt(vitals.getConfig().getInt("arena_numracearenas")) + 1)); } else {
      event += " PVP"; arena =  Integer.toString(random.nextInt(vitals.getConfig().getInt("arena_numpvparenas")) + 1);
    }List spec = vitals.config("arena").getStringList("arena." + arena + ".spectator");
    if (spec.size() < 3) { eventEnd(false); vitals.broadcastEvent("&4[" + event + "] &7Could not start match because the arena has not been set up."); return; }
    world = vitals.getServer().getWorld((String)spec.get(0)); spectator = new Location(world, Double.parseDouble((String)spec.get(1)) + 0.5D, Double.parseDouble((String)spec.get(2)) + 1.0D, Double.parseDouble((String)spec.get(3)) + 0.5D);
    if (arena.equals("hungergames")) {
      for (int i = 1; i <= vitals.getConfig().getInt("arena_hungergamesmaxplayers"); i++) positions.put(Integer.valueOf(i), vitals.config("arena").getStringList("arena.hungergames." + i)); 
    }
    else if (arena.contains("race")) {
      pos1 = vitals.config("arena").getStringList("arena." + arena + ".start"); pos2 = vitals.config("arena").getStringList("arena." + arena + ".finish");
      finish = new Location(world, Double.parseDouble((String)pos2.get(1)), Double.parseDouble((String)pos2.get(2)) + 1.0D, Double.parseDouble((String)pos2.get(3))).getBlock();
    } else {
      pos1 = vitals.config("arena").getStringList("arena." + arena + ".corner"); pos2 = vitals.config("arena").getStringList("arena." + arena + ".oppositecorner");
      x1 = Double.valueOf(Double.parseDouble((String)pos1.get(1))); x2 = Double.valueOf(Double.parseDouble((String)pos2.get(1))); y1 = Double.valueOf(Double.parseDouble((String)pos1.get(2))); y2 = Double.valueOf(Double.parseDouble((String)pos2.get(2))); z1 = Double.valueOf(Double.parseDouble((String)pos1.get(3))); z2 = Double.valueOf(Double.parseDouble((String)pos2.get(3)));
      minx = Double.valueOf(Math.min(x1.doubleValue(), x2.doubleValue())); maxx = Double.valueOf(Math.max(x1.doubleValue(), x2.doubleValue())); miny = Double.valueOf(Math.min(y1.doubleValue(), y2.doubleValue())); maxy = Double.valueOf(Math.max(y1.doubleValue(), y2.doubleValue())); minz = Double.valueOf(Math.min(z1.doubleValue(), z2.doubleValue())); maxz = Double.valueOf(Math.max(z1.doubleValue(), z2.doubleValue()));
    }
    interval = vitals.getConfig().getInt("arena_announceintervalseconds");
    decay = ((vitals.getConfig().getBoolean("arena_spleefarenadecay")) && (arena.equals("spleef")));
    state = "signup"; stateTime = new Date().getTime();
    task = vitals.getServer().getScheduler().scheduleSyncRepeatingTask(vitals, this, 1L, 20L);
  }
  private long secondsSince() { return (new Date().getTime() - stateTime) / 1000L; } 
  private String timeElapsed() { String ss = "0" + secondsSince() % 60L; ss = ss.substring(ss.length() - 2, ss.length()); return secondsSince() / 60L + ":" + ss; } 
  private boolean announce() { return (!state.equals("end")) && ((runCount - 1) % interval == 0); } 
  private Set<String> teamsAlive() { Set r = new HashSet(); for (String pName : players) if (!r.contains(teams.get(pName))) r.add((String)teams.get(pName)); 
    return r; } 
  boolean sameTeam(Player p1, Player p2) { return (playerAlive(p1)) && (playerAlive(p2)) && (((String)teams.get(p1.getName())).equals(teams.get(p2.getName()))); } 
  private String raceLeader() {
    if (!arena.contains("race")) return null;
    String leader = null; Double distance = Double.valueOf(1.7976931348623157E+308D);
    for (String pName : players) { Player p = vitals.getServer().getPlayer(pName); Double d = Double.valueOf(p.getLocation().distance(finish.getLocation())); if (d.doubleValue() < distance.doubleValue()) { leader = pName; distance = d; } }
    return leader + " (" + Math.round(distance.doubleValue()) + " meters from finish)";
  }
  void info(Player p) {
    if (p.isOp()) {
      String first = (arenaFirst instanceof Player) ? arenaFirst.getName() : "nobody"; String second = (arenaSecond instanceof Player) ? arenaSecond.getName() : "nobody"; String third = (arenaThird instanceof Player) ? arenaThird.getName() : "nobody";
      p.sendMessage("opinfo: event [" + event + "] arena [" + arena + "] state [" + state + "] playersLeft [" + players.size() + "] secondsElapsed [" + secondsSince() + 
        "] runCount [" + runCount + "] taskID [" + task + "] winners [" + first + "," + second + "," + third + "]");
    }

    if (state.equals("active")) {
      p.sendMessage(Vitals.colorize("&7An arena match is taking place right now in arena '" + arena + "'. Players still alive: &a" + players));
      if (!players.contains(p.getName())) { p.teleport(spectator); p.sendMessage(Vitals.colorize("&7Teleporting you to the spectator area.")); }
    } else if (state.equals("signup")) {
      p.sendMessage(Vitals.colorize("&7Taking signups right now for a match in arena '" + arena + "'. Players signed up so far: &a" + players));
    } else if (state.equals("end")) {
      p.sendMessage(Vitals.colorize("&7An arena match has recently ended."));
    } else {
      p.sendMessage(Vitals.colorize("&7The arena system is under maintenance."));
    }
  }
  private void calcPrizes() { int p1 = vitals.getConfig().getInt("arena_prizefirstplace"); int p2 = vitals.getConfig().getInt("arena_prizesecondplace"); int p3 = vitals.getConfig().getInt("arena_prizethirdplace");
    if (vitals.getConfig().getBoolean("arena_multiplyprizesbynumberofplayers")) { p1 *= numPlayers; p2 *= numPlayers; p3 *= numPlayers; }
    if (event.equalsIgnoreCase("RaceToTheFinish")) { Double x = Double.valueOf(vitals.getConfig().getDouble("arena_raceprizemultiplier")); p1 = (int)(p1 * x.doubleValue()); p2 = (int)(p2 * x.doubleValue()); p3 = (int)(p3 * x.doubleValue()); }
    if (event.equalsIgnoreCase("HungerGames")) { Double x = Double.valueOf(vitals.getConfig().getDouble("arena_hungergamesprizemultiplier")); p1 = (int)(p1 * x.doubleValue()); p2 = (int)(p2 * x.doubleValue()); p3 = (int)(p3 * x.doubleValue()); }
    prizes = new ArrayList(); prizes.add(Integer.valueOf(p1)); prizes.add(Integer.valueOf(p2)); prizes.add(Integer.valueOf(p3)); } 
  boolean playerAlive(Player p) {
    return players.contains(p.getName()); } 
  void playerDisqualify(Player p, String reason) { players.remove(p.getName()); if (players.size() == 2) arenaThird = p; else if (players.size() == 1) arenaSecond = p;  } 
  void playerVictory(Player p) {
    p.sendMessage("YOU MADE IT!"); players.remove(p.getName());
    if (!(arenaFirst instanceof Player)) { arenaFirst = p; vitals.broadcastEvent("&4[" + event + "] &6" + arenaFirst.getName() + " finished in 1st place!! (" + timeElapsed() + ")");
    } else if (!(arenaSecond instanceof Player)) { arenaSecond = p; vitals.broadcastEvent("&4[" + event + "] &6" + arenaSecond.getName() + " finished in 2nd place! (" + timeElapsed() + ")"); } else {
      arenaThird = p; vitals.broadcastEvent("&4[" + event + "] &6" + arenaThird.getName() + " finished in 3rd place! (" + timeElapsed() + ")");
    }
  }
  void playerSignup(Player p) { long secondsToSignup = 60 * vitals.getConfig().getInt("arena_minutestosignup") - secondsSince();
    if (players.contains(p.getName())) { p.sendMessage(Vitals.colorize("&7You are already signed up. The arena match will begin shortly. Players in this match: &a" + players)); return; }
    if (secondsToSignup < 45L) { p.sendMessage(Vitals.colorize("&7The match is about to start, so signups are closed. You'll have to wait for the next round.")); return; }
    p.teleport(spectator);
    if (p.getWorld() != spectator.getWorld()) {
      p.sendMessage("Teleport failed, arena signup cancelled."); return;
    }
    p.sendMessage(Vitals.colorize("&7You have signed up! The arena match will begin shortly. Teleporting you to the spectator area of Arena: " + arena));
    players.add(p.getName()); numPlayers += 1; vitals.debug("[arena] " + p.getName() + " signed up for the arena (" + numPlayers + " players so far)");
    if (event.equals("Team PVP")) {
      teams.put(p.getName(), teamNames[(teamIterator++)]); if (teamIterator >= teamNames.length) teamIterator = 0;
      p.sendMessage("You are on the " + (String)teams.get(p.getName()) + " Team.");
    }
    p.setFoodLevel(20); p.setHealth(p.getMaxHealth());
    if (!vitals.getConfig().getBoolean("arena_noinventorychanges"))
      if (event.contains("Spleef")) { p.getInventory().clear(); p.getInventory().setArmorContents(null);
        p.getInventory().addItem(new ItemStack[] { new ItemStack(Material.STONE_SPADE, 1), new ItemStack(Material.STONE_SPADE, 1), new ItemStack(Material.STONE_SPADE, 1), new ItemStack(Material.STONE_SPADE, 1) });
      } else if ((event.equalsIgnoreCase("Naked PVP")) || (event.equalsIgnoreCase("HungerGames"))) { p.getInventory().clear(); p.getInventory().setArmorContents(null);
      } else if (event.equalsIgnoreCase("Ironman PVP")) { p.getInventory().clear();
        p.getInventory().setArmorContents(new ItemStack[] { new ItemStack(Material.IRON_BOOTS, 1), new ItemStack(Material.IRON_LEGGINGS, 1), new ItemStack(Material.IRON_CHESTPLATE, 1), new ItemStack(Material.IRON_HELMET, 1) });
        p.getInventory().addItem(new ItemStack[] { new ItemStack(Material.IRON_SWORD, 1), new ItemStack(Material.IRON_AXE, 1), new ItemStack(Material.GOLDEN_APPLE, 1) });
      } else if (event.equalsIgnoreCase("Archery PVP")) { p.getInventory().clear();
        p.getInventory().setArmorContents(new ItemStack[] { new ItemStack(Material.LEATHER_BOOTS, 1), new ItemStack(Material.LEATHER_LEGGINGS, 1), new ItemStack(Material.LEATHER_CHESTPLATE, 1), new ItemStack(Material.LEATHER_HELMET, 1) });
        p.getInventory().addItem(new ItemStack[] { new ItemStack(Material.BOW, 1), new ItemStack(Material.ARROW, 64) });
      } else if (event.equalsIgnoreCase("Diamond PVP")) { p.getInventory().clear();
        p.getInventory().setArmorContents(new ItemStack[] { new ItemStack(Material.DIAMOND_BOOTS, 1), new ItemStack(Material.DIAMOND_LEGGINGS, 1), new ItemStack(Material.DIAMOND_CHESTPLATE, 1) });
        p.getInventory().addItem(new ItemStack[] { new ItemStack(Material.DIAMOND_SWORD, 1), new ItemStack(Material.DIAMOND_SWORD, 1), new ItemStack(Material.BOW, 1), new ItemStack(Material.ARROW, 8) });
      } else if (event.equalsIgnoreCase("Team PVP")) { p.getInventory().clear();
        if (((String)teams.get(p.getName())).equals("Brown")) p.getInventory().setArmorContents(new ItemStack[] { new ItemStack(Material.LEATHER_BOOTS, 1), new ItemStack(Material.LEATHER_LEGGINGS, 1), new ItemStack(Material.LEATHER_CHESTPLATE, 1) });
        if (((String)teams.get(p.getName())).equals("Gray")) p.getInventory().setArmorContents(new ItemStack[] { null, null, new ItemStack(Material.IRON_CHESTPLATE, 1) });
        if (((String)teams.get(p.getName())).equals("Blue")) p.getInventory().setArmorContents(new ItemStack[] { new ItemStack(Material.DIAMOND_BOOTS, 1), null, null, new ItemStack(Material.DIAMOND_HELMET, 1) });
        if (((String)teams.get(p.getName())).equals("Gold")) p.getInventory().setArmorContents(new ItemStack[] { new ItemStack(Material.GOLD_BOOTS, 1), new ItemStack(Material.GOLD_LEGGINGS, 1), null, new ItemStack(Material.GOLD_HELMET, 1) });
        p.getInventory().addItem(new ItemStack[] { new ItemStack(Material.IRON_SWORD, 1), new ItemStack(Material.BOW, 1), new ItemStack(Material.ARROW, 8), new ItemStack(Material.GOLDEN_APPLE, 3) }); }
  }

  public void run()
  {
    runCount += 1; if ((state.equals("setup")) || (state.equals("end"))) return;
    boolean victoryCondition = false;
    int maxRunCount = vitals.getConfig().getInt("arena_maxminutesgamecanlast") > 0 ? vitals.getConfig().getInt("arena_maxminutesgamecanlast") * 60 : 3600;

    if (state.equals("active")) {
      if (event.equals("Team PVP")) victoryCondition = teamsAlive().size() == 1;
      else if (event.equals("RaceToTheFinish")) victoryCondition = arenaThird instanceof Player;
      victoryCondition = (victoryCondition) || (players.size() <= 1);
      if (runCount >= maxRunCount) { victoryCondition = true; vitals.broadcastEvent("&4[" + event + "]&7 Game has reached maximum duration and ended."); }
      if (victoryCondition) { gameEnd(); return;
      }
    }
    for (String pName : (String[])players.toArray(new String[0])) {
      Player p = vitals.getServer().getPlayer(pName);
      Block b = (p != null) && (p.isOnline()) ? p.getLocation().getBlock() : null;
      if ((state.equals("signup")) && (arena.contains("race")) && (p != null) && (p.isOnline()) && (
        (p.getWorld() != world) || (p.getLocation().distance(spectator) > 10.0D))) p.teleport(spectator);

      if (state.equals("active")) {
        if ((p == null) || (!p.isOnline())) { players.remove(pName); vitals.broadcastEvent("&4[" + event + "] &7" + pName + " went offline and has been disqualified.");
        } else if ((arena.contains("race")) && (p.isFlying())) { players.remove(pName); vitals.broadcastEvent("&4[" + event + "] &7" + pName + " has been disqualified for flying.");
        } else if ((arena.contains("race")) && (b.getWorld() == world) && (b.getX() == finish.getX()) && (b.getY() == finish.getY()) && (b.getZ() == finish.getZ())) { playerVictory(p);
        } else if ((arena.endsWith("hungergames")) && (players.size() <= 2) && (b.getLocation().distance(spectator) >= 30.0D)) {
          p.sendMessage(Vitals.colorize("&6Fight to the finish! Defeat your last competitor to achieve victory."));
          p.teleport(new Location(world, Double.parseDouble((String)((List)positions.get(Integer.valueOf(1))).get(1)) + 0.5D, Double.parseDouble((String)((List)positions.get(Integer.valueOf(1))).get(2)) + 1.0D, Double.parseDouble((String)((List)positions.get(Integer.valueOf(1))).get(3)) + 0.5D));
        } else if ((!arena.contains("race")) && (!arena.equals("hungergames")) && (vitals.getConfig().getBoolean("arena_preventplayersfromleaving"))) {
          Location pLoc = p.getLocation();
          if ((pLoc.getWorld() != world) || (pLoc.getX() < minx.doubleValue()) || (pLoc.getX() > maxx.doubleValue() + 1.0D) || (pLoc.getZ() < minz.doubleValue()) || (pLoc.getZ() > maxz.doubleValue() + 1.0D)) {
            vitals.debug("[arena] illegal pvp exit while game is in progress by " + pName + ", tp-ing back to arena");
            tp(p); p.sendMessage(Vitals.colorize("&7You are not allowed to leave the arena while you are participating in an arena match."));
          }
        }
      }
    }
    for (Player p : vitals.getServer().getOnlinePlayers()) {
      Location pLoc = p.getLocation();
      if ((state.equals("active")) && 
        (vitals.getConfig().getBoolean("arena_preventnonplayersfromentering")) && (arena != null) && (!arena.contains("race")) && (!arena.equals("hungergames")) && (!players.contains(p.getName())) && (!p.isDead()) && 
        (pLoc.getWorld() == world) && (pLoc.getX() > minx.doubleValue()) && (pLoc.getX() < maxx.doubleValue()) && (pLoc.getZ() > minz.doubleValue()) && (pLoc.getZ() < maxz.doubleValue())) {
        vitals.debug("[arena] illegal pvp entry while game is in progress by " + p.getName() + ", tp-ing to spectator position [" + spectator + "]");
        p.teleport(spectator); p.sendMessage(Vitals.colorize("&7You are not allowed to enter a PVP arena while a match is in progress."));
      }

    }

    if (state.equals("signup")) { gameSignup(); return;
    }
    if ((state.equals("active")) && (announce()))
    {
      String status;
      if ((arena.equals("hungergames")) || (arena.contains("race"))) status = secondsSince() / 60L + " minute" + (secondsSince() / 60L == 1L ? "" : "s") + " elapsed. "; else
        status = secondsSince() + " second" + (secondsSince() == 1L ? "" : "s") + " elapsed. ";
      if (arena.contains("race")) status = status + "In the lead: " + raceLeader();
      else if ((arena.equals("hungergames")) && (players.size() <= 2)) status = status + "Only 2 players left: fight to the finish!"; else
        status = status + players.size() + " players are still alive!";
      vitals.broadcastEvent("&4[" + event + "] &7" + status);
    }
    if ((state.equals("active")) && (decay)) {
      int decayInterval = interval / (event.equalsIgnoreCase("DoubleSpleef") ? 2 : 1);
      Double y = maxy; Double edgeDiff = Double.valueOf(Math.ceil(secondsSince() / decayInterval));
      if (event.equalsIgnoreCase("DoubleSpleef")) if (edgeDiff.doubleValue() < Math.ceil((maxx.doubleValue() - minx.doubleValue()) / 2.0D)) y = Double.valueOf(y.doubleValue() + 5.0D); else edgeDiff = Double.valueOf(edgeDiff.doubleValue() - Math.ceil((maxx.doubleValue() - minx.doubleValue()) / 2.0D));
      Object p = null; Integer typeID = Integer.valueOf(20);
      if ((runCount + 4) % decayInterval == 0) { p = new Predicate() { public boolean test(Integer x) { return x.intValue() != 0; }

	@Override
	public boolean test(Object paramT) {
		// TODO Auto-generated method stub
		return false;
	} } ;
      } else if ((runCount + 1) % decayInterval == 0) { typeID = Integer.valueOf(0); p = new Predicate() { public boolean test(Integer x) { return x.intValue() == 20; }

	@Override
	public boolean test(Object paramT) {
		// TODO Auto-generated method stub
		return false;
	} } ; }
      if (p != null) for (Double x = minx; x.doubleValue() <= maxx.doubleValue(); x = Double.valueOf(x.doubleValue() + 1.0D)) for (Double z = minz; z.doubleValue() <= maxz.doubleValue(); z = Double.valueOf(z.doubleValue() + 1.0D))
            if ((x.doubleValue() <= minx.doubleValue() + edgeDiff.doubleValue()) || (x.doubleValue() >= maxx.doubleValue() - edgeDiff.doubleValue()) || (z.doubleValue() <= minz.doubleValue() + edgeDiff.doubleValue()) || (z.doubleValue() >= maxz.doubleValue() - edgeDiff.doubleValue())) {
              Location l = new Location(world, x.doubleValue(), y.doubleValue(), z.doubleValue()); if (((Predicate)p).test(Integer.valueOf(l.getBlock().getTypeId()))) l.getBlock().setTypeId(typeID.intValue()); 
            }
    }
  }

  private void gameSignup()
  {
    long secondsToSignup = 60 * vitals.getConfig().getInt("arena_minutestosignup") - secondsSince();
    if ((announce()) && (secondsToSignup > 0L) && (secondsToSignup < 45L)) {
      if (players.size() < vitals.getConfig().getInt("arena_minimumplayers")) {
        vitals.broadcastEvent("&4[" + event + "] &6Not enough players signed up (need at least " + vitals.getConfig().getInt("arena_minimumplayers") + ").");
        vitals.broadcastEvent("&6The next arena signup period will begin in " + vitals.getConfig().getInt("arena_minutesbetweengames") + " minutes.");
        vitals.logEvent("arena", numPlayers + " signups, " + vitals.getConfig().getInt("arena_minimumplayers") + " needed, arena cancelled");
        eventEnd(false);
      } else {
        calcPrizes(); vitals.broadcastEvent("&4[" + event + "] &7The match begins in " + secondsToSignup + " seconds! Signups are now closed. Good luck players!!");
        if (event.equals("Team PVP")) vitals.broadcastEvent("&6Prizes:&7 Surviving members of the winning team earn &6$" + prizes.get(1)); else
          vitals.broadcastEvent("&6Prizes:&7 1st place &6$" + prizes.get(0) + "&7, 2nd place &6$" + prizes.get(1) + "&7, 3rd place &6$" + prizes.get(2));
      }
    }
    else if ((announce()) && (secondsToSignup > 0L)) {
      int minutesLeft = (int)Math.round(secondsToSignup / 60.0D); String minutesString = minutesLeft + " minutes";
      vitals.broadcastEvent("&4[" + event + "] &7An arena match is starting in " + minutesString + "! To signup, type &a/arena &7(" + numPlayers + " players so far) - Play and win prizes!");
    }
    else if (secondsToSignup <= 0L) {
      gameStart();
    }
  }

  private void gameStart() { vitals.debug("[arena] match start. event [" + event + "] arena [" + arena + "] #players [" + players.size() + "]");
    calcPrizes(); state = "active"; stateTime = new Date().getTime(); runCount = 0; if (arena.equals("hungergames")) spectator.getWorld().setTime(0L);
    if ((arena.equals("hungergames")) || (arena.contains("race"))) interval *= 2;
    if (arena.equals("spleef")) {
      Double numBlocks = Double.valueOf((1.0D + maxx.doubleValue() - minx.doubleValue()) * (1.0D + maxy.doubleValue() - miny.doubleValue()) * (1.0D + maxz.doubleValue() - minz.doubleValue()));
      if (numBlocks.doubleValue() > 5000.0D) { Vitals.log.warning("[Vitals] skipping fill of spleef arena floor larger than 5000 blocks. if you want autofill then define a smaller area with /arena setup spleef");
      } else {
        vitals.debug("[spleef] filling the arena floor (" + numBlocks + " blocks)");
        vitals.cuboidFill(world, minx, miny, minz, maxx, maxy, maxz, vitals.getConfig().getInt("arena_spleefblockid"), null);
        if (event.equalsIgnoreCase("DoubleSpleef")) vitals.cuboidFill(world, minx, Double.valueOf(miny.doubleValue() + 5.0D), minz, maxx, Double.valueOf(maxy.doubleValue() + 5.0D), maxz, vitals.getConfig().getInt("arena_spleefblockid"), null); else
          vitals.cuboidFill(world, minx, Double.valueOf(miny.doubleValue() + 5.0D), minz, maxx, Double.valueOf(maxy.doubleValue() + 5.0D), maxz, 0, null);
      }
    }
    vitals.debug("[arena] match start. players = " + players);
    int i = 0; for (String pName : (String[])players.toArray(new String[0])) {
      Player p = vitals.getServer().getPlayer(pName); vitals.debug("[arena] match start. checking player #" + ++i + " [" + pName + "]");
      if ((p == null) || (!p.isOnline())) players.remove(pName);
      else if (arena.equals("hungergames")) p.teleport(new Location(world, Double.parseDouble((String)((List)positions.get(Integer.valueOf(i))).get(1)) + 0.5D, Double.parseDouble((String)((List)positions.get(Integer.valueOf(i))).get(2)) + 1.0D, Double.parseDouble((String)((List)positions.get(Integer.valueOf(i))).get(3)) + 0.5D));
      else if (arena.contains("race")) p.teleport(new Location(world, Double.parseDouble((String)pos1.get(1)) + 0.5D, Double.parseDouble((String)pos1.get(2)) + 1.0D, Double.parseDouble((String)pos1.get(3)) + 0.5D)); else {
        tp(p);
      }
    }

    vitals.broadcastEvent("&4[" + event + "] &6The arena match has begun!");
  }

  private void gameEnd()
  {
    Iterator localIterator;
    if (event.equals("Team PVP"))
    {
      String team;
      for (localIterator = teamsAlive().iterator(); localIterator.hasNext(); vitals.broadcastEvent("&4[" + event + "]&6 Game over! Congratulations to: " + team + " Team!!")) team = (String)localIterator.next();
      vitals.broadcastEvent("&7Survivors: &a" + players);
      vitals.broadcastEvent("&6A new arena match will begin in " + vitals.getConfig().getInt("arena_minutesbetweengames") + " minutes.");
      String pName;
      for (localIterator = players.iterator(); localIterator.hasNext(); vitals.getServer().getPlayer(pName).sendMessage(Vitals.colorize("&cYou received $" + prizes.get(1) + " prize money!"))) { pName = (String)localIterator.next(); Vitals.econ.depositPlayer(pName, ((Integer)prizes.get(1)).intValue()); }
      vitals.logEvent(event, numPlayers + " players, " + event + " format, " + secondsSince() + "s elapsed, winners=" + players);
      eventEnd(false); return;
    }
    if (!arena.contains("race"))
    {
      String pName;
      for (localIterator = players.iterator(); localIterator.hasNext(); arenaFirst = vitals.getServer().getPlayer(pName)) pName = (String)localIterator.next();
    }
    else if (!(arenaFirst instanceof Player))
    {
      String pName;
      for (localIterator = players.iterator(); localIterator.hasNext(); arenaFirst = vitals.getServer().getPlayer(pName)) pName = (String)localIterator.next(); 
    }
    else if (!(arenaSecond instanceof Player))
    {
      String pName;
      for (localIterator = players.iterator(); localIterator.hasNext(); arenaSecond = vitals.getServer().getPlayer(pName)) pName = (String)localIterator.next(); 
    }
    else if (!(arenaThird instanceof Player))
    {
      String pName;
      for (localIterator = players.iterator(); localIterator.hasNext(); arenaThird = vitals.getServer().getPlayer(pName)) pName = (String)localIterator.next();
    }
    String first = (arenaFirst instanceof Player) ? arenaFirst.getName() : "nobody"; String second = (arenaSecond instanceof Player) ? arenaSecond.getName() : "nobody"; String third = (arenaThird instanceof Player) ? arenaThird.getName() : "nobody";
    vitals.broadcastEvent("&4[" + event + "]&6 Game over! Congratulations to the winners!!");
    vitals.broadcastEvent("&71st place: &a" + first + "&7 2nd: &a" + second + "&7 3rd: &a" + third);
    vitals.broadcastEvent("&6A new arena match will begin in " + vitals.getConfig().getInt("arena_minutesbetweengames") + " minutes.");
    if ((arenaFirst instanceof Player)) {
      Integer firstPrize = (Integer)prizes.get(0);
      Vitals.econ.depositPlayer(arenaFirst.getName(), firstPrize.intValue());
      arenaFirst.sendMessage(Vitals.colorize("&cYou received $" + firstPrize + " prize money!"));
    }
    if ((arenaSecond instanceof Player)) { Vitals.econ.depositPlayer(arenaSecond.getName(), ((Integer)prizes.get(1)).intValue()); arenaSecond.sendMessage(Vitals.colorize("&cYou received $" + prizes.get(1) + " prize money!")); }
    if ((arenaThird instanceof Player)) { Vitals.econ.depositPlayer(arenaThird.getName(), ((Integer)prizes.get(2)).intValue()); arenaThird.sendMessage(Vitals.colorize("&cYou received $" + prizes.get(2) + " prize money!")); }
    vitals.logEvent(event, numPlayers + " players, " + secondsSince() + "s elapsed, 1st=" + first + ", 2nd=" + second + ", 3rd=" + third);
    eventEnd(false);
  }
  void eventEnd(boolean cancelled) {
    if (cancelled) vitals.broadcastEvent("&4[" + event + "]&6 Match terminated by administrator");
    state = "end"; if (task > -1) { vitals.getServer().getScheduler().cancelTask(task); task = -1; } 
  }

  private void tp(Player p) { Double x = null; Double y = Double.valueOf((y1.doubleValue() + y2.doubleValue()) / 2.0D + 1.0D); Double z = null; if (event.equalsIgnoreCase("DoubleSpleef")) y = Double.valueOf(y.doubleValue() + 5.0D); do {
      x = Double.valueOf(minx.doubleValue() + random.nextInt((int)(maxx.doubleValue() - minx.doubleValue() + 1.0D))); z = Double.valueOf(minz.doubleValue() + random.nextInt((int)(maxz.doubleValue() - minz.doubleValue() + 1.0D)));
    }while ((new Location(world, x.doubleValue(), y.doubleValue(), z.doubleValue()).getBlock().getTypeId() != 0) || (new Location(world, x.doubleValue(), y.doubleValue() + 1.0D, z.doubleValue()).getBlock().getTypeId() != 0) || (new Location(world, x.doubleValue(), y.doubleValue() - 1.0D, z.doubleValue()).getBlock().getType() == Material.LAVA) || (new Location(world, x.doubleValue(), y.doubleValue() - 1.0D, z.doubleValue()).getBlock().getType() == Material.STATIONARY_LAVA));
    vitals.debug("[arena] teleporting " + p.getName() + " to [" + world.getName() + "," + (x.doubleValue() + 0.5D) + "," + y + "," + (z.doubleValue() + 0.5D) + "]");
    p.teleport(new Location(world, x.doubleValue() + 0.5D, y.doubleValue(), z.doubleValue() + 0.5D)); }

  void setup(PlayerInteractEvent event) {
    if ((event.getPlayer() != admin) || (event.getAction() != Action.RIGHT_CLICK_BLOCK)) return;
    if (arena.equals("hungergames")) {
      Location loc = event.getClickedBlock().getLocation(); String[] pos = { loc.getWorld().getName(), Double.toString(loc.getX()), Double.toString(loc.getY()), Double.toString(loc.getZ()) }; List posList = Arrays.asList(pos);
      if (setupState == 0) vitals.config("arena").set("arena." + arena + ".spectator", posList); else
        vitals.config("arena").set("arena." + arena + "." + setupState, posList);
      if (++setupState <= vitals.getConfig().getInt("arena_hungergamesmaxplayers")) { admin.sendMessage("Now right-click starting position " + setupState); } else {
        admin.sendMessage("Setup complete!"); state = "end"; arena = null; setupState = 0; vitals.saveConfig("arena");
      } } else if (arena.contains("race")) {
      String[] arenaPositions = { "spectator", "start", "finish" };
      String[] setMessages = { "Now right-click the start block.", "Now right-click the finish block.", "Arena has been defined! Ending setup mode." };
      Location loc = event.getClickedBlock().getLocation(); String[] pos = { loc.getWorld().getName(), Double.toString(loc.getX()), Double.toString(loc.getY()), Double.toString(loc.getZ()) };
      vitals.config("arena").set("arena." + arena + "." + arenaPositions[setupState], Arrays.asList(pos)); admin.sendMessage(setMessages[setupState]);
      if (++setupState >= arenaPositions.length) { state = "end"; arena = null; setupState = 0; vitals.saveConfig("arena"); }
    } else {
      String[] arenaPositions = { "spectator", "corner", "oppositecorner" };
      String[] setMessages = { "Now right-click a corner of the arena floor.", "Now right-click the opposite corner.", "Arena has been defined! Ending setup mode." };
      Location loc = event.getClickedBlock().getLocation(); String[] pos = { loc.getWorld().getName(), Double.toString(loc.getX()), Double.toString(loc.getY()), Double.toString(loc.getZ()) };
      vitals.config("arena").set("arena." + arena + "." + arenaPositions[setupState], Arrays.asList(pos)); admin.sendMessage(setMessages[setupState]);
      if (++setupState >= arenaPositions.length) { state = "end"; arena = null; setupState = 0; vitals.saveConfig("arena");
      }
    }
  }
}

/* Location:           C:\Users\gabez_000\Downloads\Vitals.jar
 * Qualified Name:     com.pzxc.Vitals.Arena
 * JD-Core Version:    0.6.2
 */