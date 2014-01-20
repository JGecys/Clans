package com.gmail.jgecys;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scoreboard.Score;

public class EventListener extends Main implements Listener {
	
	@EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerKill(PlayerDeathEvent e) {
		Player killed = e.getEntity();
        Player killer = killed.getKiller();
        Score scorekiller = Main.board.getObjective("kills").getScore(killer);
        scorekiller.setScore(scorekiller.getScore() + Main.pointsForKill);
        
        Score scorekilled = Main.board.getObjective("kills").getScore(killed);
        scorekilled.setScore(scorekilled.getScore() + Main.pointsForDeath);
        
        if(Main.inTeam(killer) == true){
	        e.setDeathMessage(ChatColor.GREEN + killed.getName() +
	        			ChatColor.WHITE + " has been killed by " +
	        			ChatColor.RED + killer.getName() +
	        			ChatColor.WHITE + " from " +
	        			ChatColor.YELLOW + board.getPlayerTeam(killer).getName());
        }
    }
	
}
