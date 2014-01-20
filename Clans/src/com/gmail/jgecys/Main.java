package com.gmail.jgecys;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class Main extends JavaPlugin{
	
	public static ScoreboardManager MANAGER = null;
	public static Scoreboard board = null;
	
	public Plugin plugin = this;
	
    private FileConfiguration customConfig = null;
    private File customConfigFile = null;
	
	public static Logger log = Logger.getLogger("Minecraft");
	
	public boolean FriendlyFire = false;
	public static int pointsForKill = 2;
	public static int pointsForDeath = -1;
	
	public static Set<Owner> Owners;
	
	@Override
    public void onEnable(){
	    customConfigFile = new File(getDataFolder(), "config.yml");
	    customConfig = this.getCustomConfig();
		if(Main.MANAGER == null){
			Main.MANAGER = Bukkit.getScoreboardManager();
		}
		if(Main.board == null){
			Main.board = Main.MANAGER.getMainScoreboard();
		}
		
		Main.Owners = new HashSet<Owner>();
		
	    //this.reloadConfig();
	    
		
		this.getServer().getPluginManager().registerEvents(new EventListener(), this);
		
		FriendlyFire = customConfig.getBoolean("friendlyFire");
		Main.pointsForKill = customConfig.getInt("pointsForKill");
		Main.pointsForDeath = customConfig.getInt("pointsForDeath");
		
		Owner temp = new Owner();
		String name;
		String path;
		try{
			for(Team team : board.getTeams())
			{
				path = "Owners." + team.getName();
				name = customConfig.getString(path);
				temp.team = team;
				temp.setOwner((Player) this.getServer().getOfflinePlayer(name));
				Main.Owners.add(temp);
			}
		}catch(Exception e){
			saveCustomConfig();
			for(Team team : board.getTeams())
			{
				path = "Owners." + team.getName();
				name = customConfig.getString(path);
				temp.team = team;
				temp.setOwner(this.getServer().getOfflinePlayer(name));
				Main.Owners.add(temp);
			}
		}
		
		ClansEnable();
		
		log.info("["+this.getName()+"] has been Enabled!");
    }
	
	public void Scoreboard(CommandSender sender)
	{
		Set<OfflinePlayer> PlayerList = new HashSet<OfflinePlayer>();
		OfflinePlayer[] Players = null;
		
		for(OfflinePlayer off : this.getServer().getOfflinePlayers()){
			PlayerList.add(off);
		}
		Players = PlayerList.toArray(new OfflinePlayer[0]);
		
		
		OfflinePlayer temp;
		int l = Players.length;
		int score1, score2;
		for(int j = 0; j < l; j++){
			for(int i = 0; i < l - 1; i++){
				score1 = board.getObjective("kills").getScore(Players[i]).getScore();
				score2 = board.getObjective("kills").getScore(Players[i+1]).getScore();
				if(score2 > score1){
					temp = Players[i];
					Players[i] = Players[i+1];
					Players[i+1] = temp;
				}
			}
		}
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "--------------------Scoreboard--------------------");
		for(int i = 1; i <= 5 && i <= l; i++){
			if(i == 1){
				sender.sendMessage(ChatColor.RED + "" + i +". "+ Players[i-1].getName() + " : " + board.getObjective("kills").getScore(Players[i-1]).getScore());
			}
			else if(i == 2){
				sender.sendMessage(ChatColor.GOLD + "" + i +". "+ Players[i-1].getName() + " : " + board.getObjective("kills").getScore(Players[i-1]).getScore());
			}
			else if(i ==3){
				sender.sendMessage(ChatColor.GREEN + "" + i +". "+ Players[i-1].getName() + " : " + board.getObjective("kills").getScore(Players[i-1]).getScore());
			}
			else{
				sender.sendMessage(i+". "+ Players[i-1].getName() + " : " + board.getObjective("kills").getScore(Players[i-1]).getScore());
			}
		}
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "--------------------------------------------------");
	}
	
	public void giveOwner(CommandSender sender, String target, String Clan)
	{
		if(sender instanceof Player){
			Player senderPlayer = (Player) sender;
			if(isOwner(senderPlayer)){
				try {
					OfflinePlayer targetPlayer = this.getServer().getOfflinePlayer(target);
					Owner own = getOwnerFromTeam(board.getPlayerTeam(senderPlayer));
					own.setOwner(targetPlayer);
					sender.sendMessage("New Owner set!");
				} catch (Exception e) {
					sender.sendMessage("Player not found");
				}
			}
			else sender.sendMessage("You cant give what you dont have!");
		}
		else if(!(sender instanceof Player)){
			try{
				OfflinePlayer targetPlayer = this.getServer().getOfflinePlayer(target);
				Owner own = getOwnerFromTeam(board.getPlayerTeam(targetPlayer));
				own.setOwner(targetPlayer);
			}catch(Exception e){
				sender.sendMessage("Player not found");
			}
			
		}
		
	}
	
	
    public void reloadCustomConfig() {
    	log.info("["+this.getName()+"] Reading configuration file...");
        if (this.customConfigFile == null) {
        	this.customConfigFile = new File(getDataFolder(), "config.yml");
        }
        this.customConfig = YamlConfiguration.loadConfiguration(this.customConfigFile);
        
        // Look for defaults in the jar
        InputStream defConfigStream = this.getResource("/Clans/config.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            this.customConfig.setDefaults(defConfig);
        }
    }
    
    public FileConfiguration getCustomConfig() {
        if (customConfig == null) {
            reloadCustomConfig();
        }
        return customConfig;
    }
    
    public void saveCustomConfig() {
        if (customConfig == null || customConfigFile == null) {
            return;
        }
        try {
            customConfig.save(customConfigFile);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
        }
    }

    
	public void ClansEnable(){
		
		for(Team clan : board.getTeams()){
			clan.setAllowFriendlyFire(false);
		}
		
		if (board.getObjective("showhealth") == null){
			Objective objective = board.registerNewObjective("showhealth", "health");
			objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
			objective.setDisplayName("/ 20");
		}
		
		if (board.getObjective("kills") == null){
			Objective kills = board.registerNewObjective("kills", "TOTAL_KILLS");
			kills.setDisplaySlot(DisplaySlot.PLAYER_LIST);
		}
		
		for(Player online : Bukkit.getOnlinePlayers()){
		online.setScoreboard(board);
		online.setHealth(online.getHealth()); //Update their health
		}
	}
	
	@Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(sender.hasPermission("clans.default")){
	    	
			if (sender instanceof Player) {
				Player senderPlayer = (Player) sender;
				if(cmd.getName().equalsIgnoreCase("invite") && sender.hasPermission("clans.invite")){ 
		    		if(args.length == 1 && inTeam(senderPlayer)){
		    			Team team = board.getPlayerTeam(senderPlayer);
		    			inviteToTeam(args[0], team, sender);
		    			return true;
		    		}
		    		else sender.sendMessage(ChatColor.LIGHT_PURPLE + "You're not in a Clan");
		    	}
		    	if(cmd.getName().equalsIgnoreCase("join") && sender.hasPermission("clans.join")){ 
		    		if(args.length == 1){
		    			joinTeam(args[0], senderPlayer);
		    			return true;
		    		}
		    	}
		    	if(cmd.getName().equalsIgnoreCase("setowner") && sender.hasPermission("clans.setowner")){ 
		    		if(args.length == 1 && sender instanceof Player){
		    			giveOwner(sender, args[0], " ");
		    			return true;
		    		}
		    		else if(args.length == 2 && !(sender instanceof Player)){
		    			giveOwner(sender, args[0], args[1]);
		    		}
		    	}
		    	if(cmd.getName().equalsIgnoreCase("leave") && sender.hasPermission("clans.leave")){
		    		this.leaveTeam(senderPlayer);
		    		return true;
		    	}
		    	if(cmd.getName().equalsIgnoreCase("clanlist") && sender.hasPermission("clans.clanlist")){ 
		    		listClans(senderPlayer);
					return true;
		    	}
			}
	    	if(cmd.getName().equalsIgnoreCase("create") && sender.hasPermission("clans.create")){
	    		if(args.length == 2){
	    			newTeam(sender, args[0], args[1]);
	    			return true;
	    		}
	    	}
	    	
	    	if(cmd.getName().equalsIgnoreCase("claninfo") && sender.hasPermission("clans.claninfo")){
	    		if(args.length == 1){
					if(board.getTeam(args[0]) != null){
						Team targetTeam = board.getTeam(args[0]);
	    				clanInfo(sender, targetTeam);
					}
					else sender.sendMessage(ChatColor.LIGHT_PURPLE + "Cant find such Clan");
					return true;
	    		}
	    		else if (sender instanceof Player) {
	    			if (args.length == 0){
	    				Player senderPlayer = (Player) sender;
		    			if(inTeam(senderPlayer)){
		    				Team senderTeam = board.getPlayerTeam(senderPlayer);
		    				clanInfo(senderPlayer, senderTeam);
		    			}
	    			else sender.sendMessage(ChatColor.LIGHT_PURPLE + "Youre not in a Clan");
	    			return true;
	    			}
	    		}
	    		
	    	}
	    	if(cmd.getName().equalsIgnoreCase("scoreboard") && sender.hasPermission("clans.scoreboard")){ // If the player typed /basic then do the following...
	    		Scoreboard(sender);
	    		return true;
	    	}
	    	if(cmd.getName().equalsIgnoreCase("kick") && sender.hasPermission("clans.kick")){
	    		if(args.length == 1){
	    			kickPlayer(sender, args[0]);
	    			return true;
	    		}
	    	}
	    	if(cmd.getName().equalsIgnoreCase("settag") && sender.hasPermission("clans.settag")){
	    		if(args.length == 1){
	    			setTag(sender, args[0]);
	    			return true;
	    		}
	    	}
	    	
		}
    	return false; 
    }
    
    private void kickPlayer(CommandSender sender, String string) {
    	if(sender instanceof Player){
			Player senderPlayer = (Player) sender;
			OfflinePlayer targetPlayer = this.getServer().getOfflinePlayer(string);
			
			if(board.getPlayerTeam(senderPlayer) == board.getPlayerTeam(targetPlayer)){
				Owner own = getOwnerFromTeam(board.getPlayerTeam(senderPlayer));
				if(own.getOwner() == senderPlayer){
					if(senderPlayer == targetPlayer){
						sender.sendMessage("You cant Kick yourself out of Clan. (Use /leave)");
						return;
					}
					else{
						own.team.removePlayer(targetPlayer);
						sender.sendMessage("Kicked.");
					}
				}
				else sender.sendMessage("You cant kick others!");
			}
			else sender.sendMessage("Player is not in your Clan!");
    	}
    	else{
    		OfflinePlayer targetPlayer = this.getServer().getOfflinePlayer(string);
    		Team team = board.getPlayerTeam(targetPlayer);
    		Owner own = getOwnerFromTeam(team);
    		if(own.getOwner() != targetPlayer){
    			team.removePlayer(targetPlayer);
    			log.info("Player " + targetPlayer.getName() +  " Kicked from " + team.getName());
    		}
    		else if(team.getSize() == 1){
    			Main.Owners.remove(own);
    			customConfig.set("Owners."+team.getName(), null);
    			log.info("Unregistering Clan " + team.getName());
    			team.unregister();
    		}
    		else{
    			team.removePlayer(targetPlayer);
    			for(OfflinePlayer pl : team.getPlayers()){
    				if(pl != targetPlayer){
    					own.setOwner(pl);
    					log.info(targetPlayer.getName() + " has been Kicked from " + team.getName() + ". New Owner: " + pl.getName());
    					break;
    				}
    			}
    		}
    	}
		
		
	}

	private void clanInfo(CommandSender sender, Team team) {
    	try{
    		int score = 0;
    		Owner own = this.getOwnerFromTeam(team);
    		sender.sendMessage(ChatColor.LIGHT_PURPLE + "----------------------------------------");
    		sender.sendMessage(ChatColor.GREEN + "Clan Name:");
    		sender.sendMessage("   "+ team.getName());
    		sender.sendMessage(ChatColor.GREEN + "Clan Tag:");
    		sender.sendMessage("   "+ team.getPrefix());
    		sender.sendMessage(ChatColor.GREEN + "Clan Owner:");
    		sender.sendMessage("   "+ own.getOwner().getName());
    		sender.sendMessage(ChatColor.GREEN + "Populiarity:");
    		sender.sendMessage("   "+ team.getSize());
    		for(OfflinePlayer mate : team.getPlayers()){
    			score += board.getObjective("kills").getScore(mate).getScore();
    		}
    		sender.sendMessage(ChatColor.GREEN + "Total Score:");
    		sender.sendMessage("   "+ score);
    		sender.sendMessage(ChatColor.GREEN + "Member List:  ");
    		for(OfflinePlayer mate : team.getPlayers()){
    			sender.sendMessage("   "+ mate.getName());
    		}
    		sender.sendMessage(ChatColor.LIGHT_PURPLE + "----------------------------------------");
    	
    	}catch(Exception e){
    		sender.sendMessage("Could not find given Clan");
    	}
	}

	public void listClans(Player player){
		int pos = 1;
    	if(board.getTeams().isEmpty() == false){
    		player.sendMessage(ChatColor.GREEN + "Clan List:");
        	for(Team team : board.getTeams()){
        		player.sendMessage("    " + pos + ". " + team.getName());
        		pos++;
        	}
    	}
    	else{
    		player.sendMessage(ChatColor.LIGHT_PURPLE + "Currently, theres no Clans created");
    	}
    	
    }
	
	public void setTag(CommandSender sender, String tag)
	{
		if(sender instanceof Player){
			Player senderPlayer = (Player) sender;
			if(inTeam(senderPlayer)){
				if(isOwner(senderPlayer)){
					if(tag.length() <= 6){
						board.getPlayerTeam(senderPlayer).setPrefix(tag);
						sender.sendMessage("Tag Set.");
					}
					else sender.sendMessage("Tag too long");
					
				}
				else sender.sendMessage("You must own Clan.");
			}
			else sender.sendMessage("Youre not in a Clan");
		}
		else sender.sendMessage("You cant use this from console");
	}
    
    public static boolean inTeam(Player player){
    	Team team = board.getPlayerTeam(player);
    	if(team != null) return true;
    	else return false;
    }
    
    public void joinTeam(String name, Player player){
    	try{
    		if(inTeam(player) && board.getTeam(name) == board.getPlayerTeam(player)){
    			player.sendMessage("You're already in this team");
    			return;
    		}
	    	if(inTeam(player)){
	    		this.leaveTeam(player);
	    		Team team = (Team) board.getTeam(name);
	        	team.addPlayer(player);
	        	player.sendMessage(ChatColor.GREEN + "Joined " + team.getName());
	    	}
	    	else{
		    	Team team = (Team) board.getTeam(name);
		    	team.addPlayer(player);
		    	player.sendMessage(ChatColor.GREEN + "Joined " + team.getName());
	    	}
    	}catch(Exception e){
    		player.sendMessage(ChatColor.LIGHT_PURPLE + "No such Clan");
    	}
    }
    
    public boolean leaveTeam(Player player){
    	if(inTeam(player) == true){
    		Team team = getPlayersTeam(player);
    		team.removePlayer(player);
    		player.sendMessage("You left your Clan");
    		Owner own = getOwnerFromTeam(team);
    		if(team.getSize() == 0){
    			Main.Owners.remove(own);
    			customConfig.set("Owners."+team.getName(), null);
    			team.unregister();
    		}
    		else{
    			boolean wasOwner = false;
    			if(player == own.getOwner()){
    				wasOwner = true;
    				for (OfflinePlayer mate : team.getPlayers()){
    	    			if (mate != own.getOwner()){
    	    				own.setOwner(mate);
    	    				player.sendMessage("New owner: "+ mate.getName());
    	    				customConfig.set("Owners."+team.getName(), mate.getName());
    	    				break;
    	    			}
    	    		}
    			}
	    		for (OfflinePlayer mate : team.getPlayers()){
	    			
	    			if (mate.isOnline()){
	    				((Player) mate).sendMessage(player.getName() + " left your Clan");
	    				if(wasOwner){
	    					((Player) mate).sendMessage("New Owner: " + own.getOwner().getName());
	    				}
	    			}
	    		}
    		}
    		return true;
    	}
    	else{
    		player.sendMessage("You dont have a Clan");
    		return false;
    	}
    	
    }

	public Team getPlayersTeam(Player player){
    	Team last = null;
    	for(Team team : board.getTeams()){
    		for(OfflinePlayer inteam : team.getPlayers()){
    			if(inteam.getName() == player.getName()){
    				last = team;
    			}
    		}
    	}
    	return last;
    	
    }
	

	public void newTeam(CommandSender sender, String name, String prefix){
		try{
			if(prefix.length() > 6){
				sender.sendMessage("Tag too long.");
				return;
			}
			if(name.length() > 16){
				sender.sendMessage("Name too long.");
				return;
			}
			Player player = (Player) sender;
			if(inTeam(player)){
				leaveTeam(player);
			}
			Team team = board.registerNewTeam(name);
			
			team.setPrefix(prefix);
			team.addPlayer(player);
			player.sendMessage(ChatColor.GREEN + "Created Clan named " + team.getName());
			team.setAllowFriendlyFire(false);
			
			Owner owntest = new Owner();
			owntest.setOwner(player);
			owntest.team = team;
			Main.Owners.add(owntest);
			customConfig.set("Owners."+ team.getName(), player.getName());
			
		}catch(Exception e){
			sender.sendMessage(ChatColor.LIGHT_PURPLE + "Clan already exists");
			log.info(""+e.toString());
		}
		
	}
	
	public void inviteToTeam(String target, Team team, CommandSender sender){
		try{
			Player player = (Player) Bukkit.getPlayer(target);
			if(inTeam(player) == false){
				player.sendMessage(ChatColor.YELLOW + sender.getName() + ChatColor.GREEN + " invited you to join " + ChatColor.YELLOW + team.getName());
				player.sendMessage(ChatColor.GREEN + "Join using command /join <clan name>");
				sender.sendMessage(ChatColor.GREEN + "Invite sent");
			}
			else sender.sendMessage(ChatColor.LIGHT_PURPLE + "Player is in another Clan");
		}catch(Exception e){
			sender.sendMessage(ChatColor.LIGHT_PURPLE + "Player not Found");
		}
	}
	
	public Set<Owner> getOwners()
	{
		return Main.Owners;
	}
	
	public Owner getOwnerFromTeam(Team team)
	{
		Owner owner;
		for(Owner own : Main.Owners){
			if(own.team == team){
				owner = own;
				return owner;
			}
		}
		return null;
	}
	
	public boolean isOwner(Player player)
	{
		for(Owner owner : Main.Owners){
			if(owner.getOwner() == player){
				return true;
			}
		}
		return false;
	}
	
	public Team getOwnersTeam(Player owner)
	{
		Team team;
		if(isOwner(owner)){
			team = Main.board.getPlayerTeam(owner);
			return team;
		}
		else return null;
	}
	
	@Override
	public void onDisable(){
		saveCustomConfig();
    }

	
}
