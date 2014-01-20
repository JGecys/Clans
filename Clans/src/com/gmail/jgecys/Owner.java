package com.gmail.jgecys;

import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.Team;

public class Owner {
	
	private OfflinePlayer owner;
	public Team team;
	
	public Owner() {
        owner = null;
        team = null;
        
    }
	
	public OfflinePlayer getOwner()
	{
		return this.owner;
	}
	
	public Team getTeam()
	{
		return this.team;
	}
	
	public void setOwner(OfflinePlayer offlinePlayer)
	{
		this.owner = offlinePlayer;
	}
	
	
	
}
