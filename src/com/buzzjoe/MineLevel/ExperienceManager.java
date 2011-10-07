package com.buzzjoe.MineLevel;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.buzzjoe.SimpleDataStorage.SimpleDataStorage;

public class ExperienceManager {

	private static ExperienceManager instance;
	
	//public SimpleDataStorage expDatabase = new SimpleDataStorage("plugins/MineChievements/Data/Experience.xml", false);
	public SimpleDataStorage data;
	
	//public SimpleDataStorage config = new SimpleDataStorage("plugins/MineChievements/config.xml", false);
	public SimpleDataStorage config;
	
	
	public static ExperienceManager getInstance() {
		return instance;
	}
	
    //-------------------------------------------------------------------------
    
    public ExperienceManager(SimpleDataStorage data, SimpleDataStorage config) {
    	this.data = data;
    	this.config = config;
    	instance = this;
    }
    
	//-------------------------------------------------------------------------
	
    public int getExp(Player player) {
		return this.data.getInt(player.getName() + ".xp.total");
	}
	
	//-------------------------------------------------------------------------
	
	public void sendMessage(Player player, String message, ChatColor color) {
		player.sendMessage(ChatColor.AQUA +"[MineLevel] " + color + message);
	}
	
	//-------------------------------------------------------------------------
	
	public void sendMessage(Player player, String message) {
		ChatColor color = ChatColor.WHITE;
		player.sendMessage(ChatColor.AQUA +"[MineLevel] " + color + message);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Increase the Experience of player about amount.<br>
	 * Optional message will be shown to Player if he has enabled to see every
	 * increase of Experience.
	 * 
	 * @param player Player object
	 * @param amount Amount to increase player's experience
	 * @param message Optional Message. See method description for details
	 * @return New amount of player's experience
	 */
    public int addExp(Player player, int amount, String message) {
    	
    	int exp = 0;
    	    	
    	if (amount!=0) {
	    	// Add Exp and get the new amount
			exp = this.data.increment(player.getName() + ".xp.total", amount);
	
			// Get after how much new exp the player will receive a chat info
			//int msgAfter = this.config.getInt("messaging.experience.player.InfoAfterExpEarned");
			int msgAfter = getExpNotifyAmount(player);
			
			// Mabye we need to send a message. Let's take a look.
			if (this.expNotifyEnabled(player)) {
				if ((msgAfter == 1)) {
					
					//This Message goes out when User wants to be informed more detailed
					
					if (amount >= 1) {
						this.sendMessage(player, "Info: XP + " + amount + " (total: " + exp + ") " + message);
					} else {
						this.sendMessage(player, "Info: XP " + amount + " (total: " + exp + ") " + message);
					}
					
				} else if ((exp % msgAfter == 0) & (exp != 0)) {
					
					//This Message will be sent automatically when Player made x new Points.
					this.sendMessage(player, "Info: Your current Experience: " + exp);
					
				}
			}
			
			LevelManager.getInstance().checkForLevelUp(player);
			
    	} else {
    		exp = this.data.getInt(player.getName() + ".xp.total");
    	}
    	
		return exp;
	}
	
	//-------------------------------------------------------------------------
	
	public int addExp(Player player, int amount) {
		return this.addExp(player, amount, "");
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return True if player wants to see every Exp-Up
	 */
	public boolean expNotifyEnabled(Player player) {
		// pre-set expSeeEnabled
		// We want to store this Setting in a "human readable" way. So we set it:
		String expNoticeEnabled = "false";
		
		// Build the key for SimpleDataStorage
		String key = player.getName() + ".xp.Notify";
		
		// Check if key exists
		if (this.data.keyExists(key)) {
			// Key exists. Get it's value.
			expNoticeEnabled = this.data.getString(key);
		} else {
			// Key does not exist. Make it and set it to false.
			this.data.set(key, "false");
		}
		
		// pre-set the result
		boolean result = false;
		
		// if requested String returned "true", set result to true
		if (expNoticeEnabled.equals((String)"true")) result = true;		
		
		return result;
	}
	
	//-------------------------------------------------------------------------
	
	public int getExpNotifyAmount(Player player) {
		int result = 10;
		String key = player.getName() + ".xp.NotifyAmount";
		if (this.data.keyExists(key)) {
			result = this.data.getInt(key);
		} else {
			result = this.config.getInt("messaging.experience.player.NotifyAmountDefault");
			this.data.set(key, result);
		}
		return result;
	}

	//-------------------------------------------------------------------------
	
	public void setExpNotifyAmount(Player player, int amount) {
		this.data.set(player.getName() + ".xp.NotifyAmount", amount);
	}
	
	//-------------------------------------------------------------------------
}
