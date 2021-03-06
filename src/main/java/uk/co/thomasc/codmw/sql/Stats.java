package uk.co.thomasc.codmw.sql;

import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TimerTask;

import org.bukkit.entity.Player;
import org.getspout.spout.player.SpoutCraftPlayer;

import uk.co.thomasc.codmw.Main;
import uk.co.thomasc.codmw.Team;
import uk.co.thomasc.codmw.objects.CPlayer;

public class Stats {
	
	private CPlayer p;
	private HashMap<Stat, Integer> stats = new HashMap<Stat, Integer>();
	public List<Achievement> achs = new ArrayList<Achievement>();
	private List<Achievement> toach = new java.util.ArrayList<Achievement>(Arrays.asList(Achievement.values()));
	private List<Stat> updated = new ArrayList<Stat>();
	private List<Stat> newv = new ArrayList<Stat>();
	private List<Achievement> newa = new ArrayList<Achievement>();
	private Main plugin;
	private List<Achievement> hidden = Arrays.asList(Achievement.TEAMNOSWITCH, Achievement.ONTHEGROUND, Achievement.KONAMI, Achievement.READINGABOOK, Achievement.FIREARMS);
	private int tid;
	
	public Stats(Main instance, CPlayer _p) {
		plugin = instance;
		p = _p;
		ResultSet r = plugin.sql.query("SELECT type, count FROM cod_stats WHERE PID = '" + p.dbid + "'");
		ResultSet r2 = plugin.sql.query("SELECT aid FROM cod_achievement WHERE PID = '" + p.dbid + "'");
		try {
			while (r.next()) {
				Stat s = Stat.valueOf(r.getInt("type"));
				if (s != null) {
					stats.put(s, r.getInt("count"));
				}
			}
			while (r2.next()) {
				Achievement a = Achievement.valueOf(r2.getInt("aid"));
				if (a != null) {
					achs.add(a);
				}
			}
			toach.removeAll(achs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		tid = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new updatestats(), 600L, 600L);
	}
	
	public void incStat(Stat s) {
		incStat(s, 1);
	}
	
	public void incStat(Stat s, int c) {
		if (stats.containsKey(s) && !updated.contains(s)) {
			updated.add(s);
		} else if (!stats.containsKey(s) && !newv.contains(s)) {
			newv.add(s);
		}
		int out = getStat(s) + c;
		if (out < 0) { out = 0; }
		ArrayList<Achievement> tmp = new ArrayList<Achievement>();
		for (Achievement a : toach) {
			if (a.getStat() == s && a.getCount() <= out) {
				tmp.add(a);
			}
		}
		for (Achievement a : tmp) {
			awardAchievement(a);
		}
		stats.put(s, out);
	}
	
	public void maxStat(Stat s, int c) {
		boolean r = false;
		if (stats.containsKey(s) && stats.get(s) < c) {
			updated.add(s);
		} else if (!stats.containsKey(s)) {
			newv.add(s);
		} else {
			r = true;
		}
		ArrayList<Achievement> tmp = new ArrayList<Achievement>();
		for (Achievement a : toach) {
			if (a.getStat() == s && a.getCount() <= c) {
				tmp.add(a);
			}
		}
		for (Achievement a : tmp) {
			awardAchievement(a);
		}
		if (r) { return; }
		stats.put(s, c);
	}
	
	public int getStat(Stat s) {
		if (stats.containsKey(s)) {
			return stats.get(s);
		}
		return 0;
	}
	
	public void awardAchievement(Achievement a) {
		if (!achs.contains(a)) {
			Player ex = p.p;
			SpoutCraftPlayer cp = (SpoutCraftPlayer) p.p;
			if (cp.isSpoutCraftEnabled()) {
				if (a.getHidden()) {
					ex.sendMessage(p.getTeam().getColour() + p.nick + " earned achievement: '" + a.getName() + "' (" + a.getDesc() + ")");
				} else {
					ex = null;
				}
				cp.sendNotification("Achievement Get!", a.getName(), a.getMat());
			} else {
				p.p.sendMessage(p.getTeam().getColour() + "You earned achievement: " + a.getText());
			}
			plugin.game.sendMessage(Team.BOTH, p.getTeam().getColour() + p.nick + " earned achievement: " + a.getText(), ex);
			incStat(Stat.POINTS, a.getPoints());
			newa.add(a);
			achs.add(a);
			toach.remove(a);
		}
		if (!achs.contains(Achievement.GIGLOCK_HOLMES) && achs.containsAll(hidden)) {
			awardAchievement(Achievement.GIGLOCK_HOLMES);
		}
	}
	
	public void destroy() {
		plugin.getServer().getScheduler().cancelTask(tid);
		update();
	}
	
	public class updatestats extends TimerTask {
		@Override
		public void run() {
			update();
		}
	}
	
	public void update() {
		List<Stat> r = new ArrayList<Stat>();
		for (Stat i : newv) {
			plugin.sql.update("INSERT INTO cod_stats VALUES('" + p.dbid + "', '" + i.getId() + "', '" + stats.get(i) + "')");
			r.add(i);
		}
		newv.removeAll(r);
		updated.removeAll(r);
		r.clear();
		for (Stat i : updated) {
			plugin.sql.update("UPDATE cod_stats SET count = '" + stats.get(i) + "' WHERE PID = '" + p.dbid + "' and type = '" + i.getId() + "'");
			r.add(i);
		}
		updated.removeAll(r);
		r.clear();
		for (Achievement a : newa) {
			plugin.sql.update("INSERT INTO cod_achievement VALUES(NULL, '" + p.dbid + "', '" + a.getId() + "')");
		}
		newa.clear();
	}
}