package com.Top_Cat.CODMW.listeners;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import net.minecraft.server.EntityItem;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.Top_Cat.CODMW.main;
import com.Top_Cat.CODMW.team;
import com.Top_Cat.CODMW.objects.chopper;
import com.Top_Cat.CODMW.objects.claymore;
import com.Top_Cat.CODMW.objects.player;
import com.Top_Cat.CODMW.objects.sentry;
import com.Top_Cat.CODMW.objects.streaks;

public class CODPlayerListener extends PlayerListener {
	
	main plugin;
	Timer t = new Timer();
	ArrayList<Material> allowed_pickup = new ArrayList<Material>();
	
	public CODPlayerListener(main instance) {
        plugin = instance;
        allowed_pickup.add(Material.FEATHER);
        allowed_pickup.add(Material.WALL_SIGN);
        allowed_pickup.add(Material.APPLE);
        allowed_pickup.add(Material.BONE);
        allowed_pickup.add(Material.DISPENSER);
        allowed_pickup.add(Material.DIAMOND);
    }
	
	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		plugin.setDoors();
		plugin.totele.add(event.getPlayer());
		t.schedule(new tele(plugin), 200);
		plugin.clearinv(event.getPlayer());
		event.setJoinMessage(plugin.d + "9" + event.getPlayer().getDisplayName() + " has joined the fray");
		event.getPlayer().sendMessage(plugin.d + "9Welcome to The Gigcast's MineCod Server!");
		event.getPlayer().sendMessage(plugin.d + "9Please choose your team!");
		event.getPlayer().setHealth(20);
	}
	
	public class tele extends TimerTask {

		main plugin;
		
		public tele(main instance) {
			plugin = instance;
		}
		
		@Override
		public void run() {
			for (Player i : plugin.totele) {
				i.teleport(plugin.teamselect);
			}
			plugin.totele.clear();
		}
		
	}
	
	@Override
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		String com = event.getMessage();
        String coms[] = com.split(" ");
        
		if (coms[0].equalsIgnoreCase("/example")) {
            
        }
	}
	
	@Override
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (plugin.players.containsKey(event.getPlayer())) {
			plugin.players.get(event.getPlayer()).destroy();
			plugin.players.remove(event.getPlayer());
		}
	}

	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
		Location t = event.getTo();
		if (!plugin.players.containsKey(event.getPlayer())) {
			if (t.getX() > -10 && t.getX() < -8 && t.getZ() > 14 && t.getZ() < 16 && t.getBlockY() == 64) {
				new player(plugin, event.getPlayer(), team.GOLD);
			} else if (t.getX() > -10 && t.getX() < -8 && t.getZ() > 10 && t.getZ() < 12 && t.getBlockY() == 64) {
				new player(plugin, event.getPlayer(), team.DIAMOND);
			} else if (t.getX() > -8 && t.getX() < -6 && t.getZ() > 12 && t.getZ() < 14 && t.getBlockY() == 64) {
				//Random team
				if (plugin.gold > plugin.diam) {
					new player(plugin, event.getPlayer(), team.DIAMOND);
				} else {
					new player(plugin, event.getPlayer(), team.GOLD);
				}
			} else {
				return;
			}
			plugin.setDoors();
			event.setTo(plugin.prespawn);
		}
		for (claymore i : plugin.clays) {
			if (i.exploded == false) {
				i.detect(event.getPlayer());
			}
		}
		if (event.getTo().getBlock().getRelative(0, -1, 0).getType() == Material.DISPENSER) {
			for (sentry i : plugin.sentries) {
				if (i.b == event.getTo().getBlock().getRelative(0, -2, 0)) {
					event.setTo(plugin.game.spawntele(plugin.players.get(event.getPlayer()), event.getPlayer(), false));
					event.getPlayer().sendMessage(plugin.d + "bOnly Gigs stand on dispensers, you have been respawned!");
				}
			}
		}
	}
	
	@Override
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		CraftEntity item = (CraftEntity)event.getItem();
		int itemId = ((EntityItem)item.getHandle()).itemStack.id;
		int amm = ((EntityItem)item.getHandle()).itemStack.count;
		if (!allowed_pickup.contains(Material.getMaterial(itemId))) {
			event.setCancelled(true);
		} else if (Material.getMaterial(itemId) == Material.FEATHER) {
			int ammo = 0;
			for (ItemStack i : event.getPlayer().getInventory().getContents()) {
				if (i != null) {
					if (i.getType() == Material.FEATHER) {
						ammo += i.getAmount();
						event.getPlayer().getInventory().remove(i);
					}
				}
			}
			if (ammo < 99) {
				event.getItem().remove();
				amm += ammo;
			} else {
				amm = ammo;
			}
			if (amm > 99) { amm = 99; }
			PlayerInventory i = event.getPlayer().getInventory();
			if (i.getItem(7) != null && i.getItem(7).getType() != Material.FEATHER && i.getItem(7).getType() != Material.AIR) {
				i.addItem(new ItemStack(Material.FEATHER, amm));
			} else {
				i.setItem(7, new ItemStack(Material.FEATHER, amm));
			}
			event.setCancelled(true);
		}
	}
	
	@Override
	public void onPlayerChat(PlayerChatEvent event) {
		if (plugin.players.containsKey(event.getPlayer())) {
			String p = "b";
			if (plugin.players.get(event.getPlayer()).getTeam() == team.GOLD) {
				p = "6";
			}
			event.setMessage(plugin.d + p + event.getMessage());
		}
	}
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		event.setUseInteractedBlock(Result.DENY);
		event.setUseItemInHand(Result.ALLOW);
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Material um = event.getPlayer().getItemInHand().getType();
			if (um == Material.BOW) {
				plugin.players.get(event.getPlayer()).stime = new Date().getTime() + 5000;
			} else if (um == Material.APPLE) {
				plugin.players.get(event.getPlayer()).vtime = new Date().getTime() + 10000;
				Player i = event.getPlayer();
				if (plugin.players.get(event.getPlayer()).getTeam() == team.GOLD) {
					i.getInventory().setChestplate(new ItemStack(Material.GOLD_CHESTPLATE, 1));
					i.getInventory().setLeggings(new ItemStack(Material.GOLD_LEGGINGS, 1));
					i.getInventory().setBoots(new ItemStack(Material.GOLD_BOOTS, 1));
				} else {
					i.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE, 1));
					i.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS, 1));
					i.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS, 1));
				}
				i.updateInventory();
				plugin.players.get(event.getPlayer()).inv = true;
			} else if (um == Material.BONE) {
				event.getPlayer().getInventory().removeItem(new ItemStack(Material.BONE, 1));
				System.out.println("BONE");
				team t = team.DIAMOND;
				if (plugin.players.get(event.getPlayer()).getTeam() == team.DIAMOND) {
					t = team.GOLD;
				}
				for (Player i : plugin.players.keySet()) {
					player _p = plugin.players.get(i);
					if (_p.getTeam() == t) {
						System.out.println(i.getDisplayName());
						Wolf w = (Wolf) plugin.currentWorld.spawnCreature(plugin.game.spawns3.get(plugin.game.generator.nextInt(plugin.game.spawns3.size())), CreatureType.WOLF);
						w.setTarget(i);
						w.setOwner(event.getPlayer());
						plugin.wolves.put(w, new Date().getTime() + 30000);
					}
				}
			} else if (um == Material.DIAMOND) {
				event.getPlayer().getInventory().removeItem(new ItemStack(Material.DIAMOND, 1));
				new chopper(plugin, event.getPlayer());
			}
		}
	}
	
	@Override
	public void onPlayerBedEnter(PlayerBedEnterEvent event) {
		event.setCancelled(true);
	}
}