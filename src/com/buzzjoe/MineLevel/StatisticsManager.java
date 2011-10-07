package com.buzzjoe.MineLevel;

import org.buzzjoe.SimpleDataStorage.SimpleDataStorage;
import org.bukkit.entity.Player;
import java.util.Calendar;
import java.text.SimpleDateFormat;

/**
 * Changelog:
 * 
 * 0.1:
 * initial
 */
public class StatisticsManager {

	private static StatisticsManager instance;
	public SimpleDataStorage data;
	public SimpleDataStorage config;
	
	//-------------------------------------------------------------------------
	
	public static StatisticsManager getInstance() {
		return instance;
	}
	
    //-------------------------------------------------------------------------
    
    public StatisticsManager(SimpleDataStorage data, SimpleDataStorage config) {
    	this.data = data;
    	this.config = config;
    	instance = this;
    }
    
    //-------------------------------------------------------------------------
    // Global Statistics ------------------------------------------------------
    
    public void setGlobalValue(String counterName, Object value) {
    	this.data.set(counterName, value);
    }
    
    //-------------------------------------------------------------------------
    
    public int incGlobalValue(String counterName, int amount) {
    	return this.data.increment(counterName);
    }
    
    //-------------------------------------------------------------------------
    
    public int decGlobalValue(String counterName, int amount) {
    	return this.data.decrement(counterName);
    }
    
    //-------------------------------------------------------------------------
    
    public int getIntPlayerValue(String counterName, Player player) throws Exception {
    	if (this.data.keyExists(player.getName() + "." + counterName)) {
    		return this.data.getInt(player.getName() + "." + counterName);
    	} else {
    		throw new Exception("Player Value " + player.getName() + "." + counterName + " is not an Integer.");
    	}
    }

    //-------------------------------------------------------------------------
    
    public String getStringPlayerValue(String counterName, Player player) throws Exception {
    	if (this.data.keyExists(player.getName() + "." + counterName)) {
    		return this.data.getString(player.getName() + "." + counterName);
    	} else {
    		throw new Exception("Player Value " + player.getName() + "." + counterName + " is not a String.");
    	}
    }
    
    //-------------------------------------------------------------------------	
    // Player Statistics ------------------------------------------------------
    
    public void setPlayerValue(String counterName, Object value, Player player) {
    	this.data.set(player.getName() + "." + counterName, value);
    	this.setPlayerLastActivity(player);
    }
    
    //-------------------------------------------------------------------------
    
    public int incPlayerValue(String counterName, int amount, Player player) {
    	this.setPlayerLastActivity(player);
    	return this.data.increment(player.getName() + "." + counterName, amount);
    }
    
    //-------------------------------------------------------------------------
    
    public int decPlayerValue(String counterName, int amount, Player player) {
    	this.setPlayerLastActivity(player);
    	return this.data.decrement(player.getName() + "." + counterName, amount);
    }
    
    //-------------------------------------------------------------------------
    
    private void setPlayerLastActivity(Player player) {
    	Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String time = sdf.format(cal.getTime());
    	if (!this.data.keyExists(player.getName() + ".firstActivity")) {
    		this.data.set(player.getName() + ".firstActivity", time);
    	}
    	this.data.set(player.getName() + ".lastActivity", time);
    }
    
    //-------------------------------------------------------------------------	
}
