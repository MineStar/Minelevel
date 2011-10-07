package com.buzzjoe.MineLevel;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.buzzjoe.SimpleDataStorage.SimpleDataStorage;


public class LevelManager {
	
	private static LevelManager instance;
	
	//public SimpleDataStorage lvlDatabase = new SimpleDataStorage("plugins/MineChievements/Data/Level.xml", false);
	public SimpleDataStorage data;
	
	//public SimpleDataStorage config = new SimpleDataStorage("plugins/MineChievements/config.xml", false);
	public SimpleDataStorage config;
	
	public ExperienceManager experienceManager;

	//-------------------------------------------------------------------------
	
	public static LevelManager getInstance() {
		return instance;
	}
	
	//-------------------------------------------------------------------------
    
    /**
     * Constructor
     */
    public LevelManager(SimpleDataStorage data, SimpleDataStorage config) {
    	this.data = data;
    	this.config = config;
    	instance = this;
    }
	
    //-------------------------------------------------------------------------
    
    /**
     * Get the Level of a player.
     * 
     * @param player
     * @return Player's level
     */
    public int getLevel(Player player) {
    	return this.data.getInt(player.getName() + ".level");
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
	
	// ------------------------------------------------------------------------
    
    public boolean checkForLevelUp(Player player) {
    	boolean result = false;
    	int playerExp = ExperienceManager.getInstance().getExp(player);
    	int playerLvl = this.getLevel(player);
    	int nextLvlExp = this.getExpForLevel((int)(playerLvl+1));
    	
    	if (playerExp >= nextLvlExp) {
    		data.set(player.getName() + ".level", (int)(playerLvl+1));
    		result = true;
    		//this.checkForLevelUp(player);
    	}

    	if (result) {
    		this.sendMessage(player, "Level-Up! You made it to level: " + getLevel(player));
    		this.sendMessage(player, "You need a total Experience of " + this.getExpForLevel(playerLvl+2) + " to reach level " + (int)(playerLvl+2));
    	}
    	
    	return result;
    }
    
    // ------------------------------------------------------------------------
    
    /**
     * Calculates needed experience for level
     * 
     * @param level Level number.
     * @return int Level Experience
     */
    public int getExpForLevel(int level) {
    	if (this.useFormula()) {
    		// y = 8x^2 + 3x
    		// y = Exp für neuen Level
    		// x = Exp vom alten Level
    		// http://www.wolframalpha.com/input/?i=plot+8*x^2%2B3x+from+x%3D0+to+30
    	
    		int exp = (int) ((8*(level*level)) + (3*level));
    		return exp;
    	} else {
    		return 0;
    	}  	
    }
    
    // ------------------------------------------------------------------------
    
    /**
     * Returns true if levels should be calculated by a formula
     * 
     * @return boolean
     */
    public boolean useFormula() {
    	boolean result = false;
    	String useFormula = this.config.getString("levelling.useFormula");
    	if (useFormula.equals((String)"true")) result = true;
    	return result;
    }
    
    // ------------------------------------------------------------------------
	
	/**
	 * Will post either a public or private chat message to player.
	 * postPublic = true will post a public message in chat which can be read 
	 * by all players.
	 * If no postPublic is given, the message will be private.
	 * 
	 * @param message String
	 * @param public boolean
	 * @return boolean
	 */	
	
	public void chatMessage(String message, boolean postPublic) {
		// do something
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Will post a private chat message to player.
	 * 
	 * @param message String
	 * @param public boolean
	 * @return boolean
	 */	
	public void chatMessage(String message) {
		chatMessage(message, false);
	}
	
}
