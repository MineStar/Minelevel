package com.buzzjoe.MineLevel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.buzzjoe.SimpleDataStorage.SimpleDataStorage;

import com.bukkit.gemo.utils.UtilPermissions;

/**
 * 
 * Changelog:
 * 0.6
 * *moved /slap to AdminStuff
 * 
 * 0.5 /Souli
 * *Implemented additional command for /stats:
 * *<player name> returns stats for the specified player
 * Implemented useless stuff /slap <player name>
 * 
 * 0.4
 * * Implemented new command /stats for Statistics
 * * Implemented Statistics Manager
 * 
 * 0.3
 * * New user command structure
 * * combined different data and config files to data.xml and config.xml
 * * ability to set autosave-time via config
 * 
 * 0.2:
 * * integrated LevelManager
 * 
 * 0.1.1:
 * * Updated SimpleDataStorage to v. 0.3.2
 * 
 * 0.1:
 * initial
 */
public class MineLevelCore extends JavaPlugin implements Runnable 
{

	public ExperienceManager experienceManager;
	public LevelManager levelManager;
	public StatisticsManager statisticsManager;
	
	public SimpleDataStorage data = new SimpleDataStorage("plugins/MineLevel/data.xml", false);
	public SimpleDataStorage config = new SimpleDataStorage("plugins/MineLevel/config.xml", false);
	public SimpleDataStorage statistics = new SimpleDataStorage("plugins/MineLevel/statistics.xml", false);
	
	public enum Commands
	{
	    help,
	    info,
	    NOVALUE;

	    public static Commands toCommand(String str)
	    {
	        try {
	            return valueOf(str);
	        } 
	        catch (Exception ex) {
	            return NOVALUE;
	        }
	    }   
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void onDisable() {
		this.run();
	    // read PluginDescriptionFile
	    PluginDescriptionFile pdfFile = this.getDescription();
		System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " disabled" );
	}

	//-------------------------------------------------------------------------
	
	public MineLevelCore() {
		this.experienceManager 	= new ExperienceManager(this.data, this.config);
		this.levelManager 		= new LevelManager(this.data, this.config);
		this.statisticsManager	= new StatisticsManager(this.statistics, this.config);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void onEnable() {
		
		/*
		this.experienceManager = new ExperienceManager(this.data, this.config);
		this.levelManager = new LevelManager(this.data, this.config);
		*/
		// call this.run() every x minutes
		int autosave = this.config.getInt("system.autosaveEveryMinutes");
		getServer().getScheduler().scheduleSyncRepeatingTask(this, this, 0, autosave*60*20);
				
	    // read PluginDescriptionFile
	    PluginDescriptionFile pdfFile = this.getDescription();
		System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " enabled" );
	}
	
	public ExperienceManager getExperienceManager() {
		return this.experienceManager;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Things that need to run scheduled
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		/*
		this.experienceManager.data.writeToFile();
		this.levelManager.data.writeToFile();
		*/
		this.data.writeToFile();
		this.statistics.writeToFile();
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		if(sender instanceof Player)
		{			
			Player player = (Player)sender;
			
			if(commandLabel.equalsIgnoreCase("lvl") | commandLabel.equalsIgnoreCase("level"))
			{	
				
				if (args.length == 0) {
					
					int lvl = levelManager.getLevel(player);
					int exp = experienceManager.getExp(player);
					int expForNextLvl = levelManager.getExpForLevel((int)(lvl+1));
					this.sendMessage(player, "Your current Level: " + lvl);
					this.sendMessage(player, "Your current Experience: " + exp);
					this.sendMessage(player, "Total Experience needed for level " + (lvl+1) + ": " + expForNextLvl);
					this.sendMessage(player, "See /" + commandLabel + " help for more commands.");
				
				} else {
					
					switch (Commands.toCommand(args[0].toLowerCase())) {
						
					case help:
						this.sendMessage(player, "'/" + commandLabel + " info' to turn on or off XP notices");
						this.sendMessage(player, "'/" + commandLabel + " info <number>' to see XP message after x new XP.");
						this.sendMessage(player, "'/" + commandLabel + " info 1' to see every XP you make plus more details.");
						break;
					
					//---------------------------------------------------------
						
					case info:
						if (args.length == 1) {
							if (this.changePlayerExpNotify(player)) {
								this.sendMessage(player, "You will be notified every " + this.experienceManager.getExpNotifyAmount(player) + " XP about your XP from now on.");
							} else {
								this.sendMessage(player, "You won't be notified about your XP from now on.");
							}							
						} else if(args.length == 2)  {
							if (this.isNumeric(args[1])) {
								try {
									int amount = Integer.parseInt(args[1]);
									this.experienceManager.setExpNotifyAmount(player, amount);
									this.experienceManager.data.set(player.getName() + ".xp.Notify", "true");
									this.sendMessage(player, "You will be notified every " + args[1] + " XP from now on.");
								} catch (Exception e) {
									this.sendMessage(player, "Incorrect notify amount. Please give me a number starting at 1 or higher.", ChatColor.RED);
								}				
							} else {
								this.sendMessage(player, "Incorrect notify amount. Please give me a number starting at 1 or higher.", ChatColor.RED);
							}
						}
						break;

					//---------------------------------------------------------
						
					default:
						this.sendMessage(player, "Don't know parameter " + args[0] + ". Type /" + commandLabel + " help for details.", ChatColor.RED);
						break;
					}
				}	
				
			} else if(commandLabel.equalsIgnoreCase("stats") | commandLabel.equalsIgnoreCase("statistics")) {
				
				if (args.length == 0) {
					// No args --> print out players own statistics
					try {
						int totalPlace = this.statisticsManager.getIntPlayerValue("block.total.placeCount", player);
						int totalBreak = this.statisticsManager.getIntPlayerValue("block.total.breakCount", player);
						String firstActivity = this.statisticsManager.getStringPlayerValue("firstActivity", player);
						this.sendMessage(player, "Your Statistics since " + firstActivity);
						this.sendMessage(player, "Blocks broken: " + totalBreak);
						this.sendMessage(player, "Blocks placed: " + totalPlace);
					} catch (Exception e) {
						//this.sendMessage(player, "Incorrect notify amount. Please give me a number starting at 1 or higher.", ChatColor.RED);
					}	
				} else {
					switch (Commands.toCommand(args[0].toLowerCase())) {
						case help:
							this.sendMessage(player, "'/" + commandLabel + " info' to turn on or off XP notices");
							break;
						
						//---------------------------------------------------------
							
						case info:
							break;
							
						default: //stats for specified user
							if(!UtilPermissions.playerCanUseCommand(player,"minelevel.stats.detail")){
								break;
							}
							
							Player f_player=getPlayer(args[0]);
							if(f_player==null){
								this.sendMessageStats(player,"Player '"+args[0]+"' isn't online or doesn't exist.");
								break;
							}
							if(args.length==1){
								try {
									int totalPlace = this.statisticsManager.getIntPlayerValue("block.total.placeCount",f_player);
									int totalBreak = this.statisticsManager.getIntPlayerValue("block.total.breakCount",f_player);
									String firstActivity = this.statisticsManager.getStringPlayerValue("firstActivity",f_player);
									this.sendMessageStats(player, "Statistics of '"+ChatColor.DARK_RED+f_player.getDisplayName()+"'");
									this.sendMessageStats(player, "Active since "+firstActivity);
									this.sendMessageStats(player, "Blocks broken: " + totalBreak);
									this.sendMessageStats(player, "Blocks placed: " + totalPlace);
								}
								catch (Exception e) {
								
								}
							}
							else if(args.length==2&&args[1].equals("detail")){
								try {
									int totalPlace = this.statisticsManager.getIntPlayerValue("block.total.placeCount",f_player);
									int totalBreak = this.statisticsManager.getIntPlayerValue("block.total.breakCount",f_player);
									String firstActivity = this.statisticsManager.getStringPlayerValue("firstActivity",f_player);
									int placedPerDay=0,brokenPerDay=0;
									
									try {
										SimpleDateFormat date = new SimpleDateFormat("MM.dd.yyyy HH:mm:ss");
										Date date1 = date.parse(firstActivity);
										
										Calendar cal_1 = new GregorianCalendar();
										Calendar cal_2 = new GregorianCalendar();
										cal_1.setTime(date1);
										
										Date firstDate = date.parse("01.04.2011 00:00:00");
										
										if(cal_2.getTime().getTime()<cal_1.getTime().getTime()||date1.before(firstDate)){
											date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
											date1 = date.parse(firstActivity);
											cal_1.setTime(date1);
										}
										
										long time = cal_2.getTime().getTime() - cal_1.getTime().getTime();
										int days = Math.round((float)(time/(24*60*60*1000))==0?1:Math.round(time/(24*60*60*1000)));
										
										System.out.println("days online: "+days);
										
										
										
										placedPerDay=Math.round((float)totalPlace/days);
										brokenPerDay=Math.round((float)totalBreak/days);
									}
									catch (ParseException e) {
										e.printStackTrace();
									}
									
									this.sendMessageStats(player,ChatColor.YELLOW+"---Start---------------------------------");
									this.sendMessageStats(player, "Statistics of '"+ChatColor.DARK_RED+f_player.getDisplayName()+"'");
									this.sendMessageStats(player, "Active since "+firstActivity);
									this.sendMessageStats(player, "Blocks broken: " + totalBreak);
									this.sendMessageStats(player, "Blocks placed: " + totalPlace);
									this.sendMessageStats(player,"Blocks placed per day: "+placedPerDay);
									this.sendMessageStats(player,"Blocks broken per day: "+brokenPerDay);
									this.sendMessageStats(player,ChatColor.YELLOW+"---End-----------------------------------");								
								}
								catch (Exception e) {
								
								}
							}
							break;
					}
				}
				
			}
			
		}
		return true;
	}
	
	
	/*public Player getPlayer(String name)
    {
        Player[] pList = getServer().getOnlinePlayers();
        for(Player player : pList)
        {
            if(player.getName().equalsIgnoreCase(name))
                return player;
        }
        
        return null;
    }*/
	
	public Player getPlayer(String name)
    {
		if(name==null)
			return null;
		
        List<Player> pList;
       /* for(Player player : pList)
        {
            if(player.getName().equalsIgnoreCase(name))
                return player;
        }*/
		pList=getServer().matchPlayer(name);
		if(pList.size()>1||pList.size()<1)
			return null;
		else return pList.get(0);
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
	
	//-------------------------------------------------------------------------
	
	public void sendMessageStats(Player player, String message) {
		ChatColor color = ChatColor.WHITE;
		player.sendMessage(ChatColor.DARK_RED +"[Stats] " + color + message);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Dirty hack to check if String is an integer
	 * @param aStringValue
	 */
	private boolean isNumeric(String aStringValue) {
		Pattern pattern = Pattern.compile( "\\d+" );

		Matcher matcher = pattern.matcher(aStringValue);
		return matcher.matches();
	} 
	
	//-------------------------------------------------------------------------
	
	private void setPlayerExpNotify(Player player, boolean doNotify) {
		if (doNotify)
			this.data.set(player.getName() + ".xp.Notify", "true");
		else
			this.data.set(player.getName() + ".xp.Notify", "false");
	}
	
	//-------------------------------------------------------------------------
	
	private boolean changePlayerExpNotify(Player player) {
		
		if (this.experienceManager.expNotifyEnabled(player)) {
			this.setPlayerExpNotify(player, false);
			return false;
		} else {
			this.setPlayerExpNotify(player, true);
			return true;
		}
		
	}

}
