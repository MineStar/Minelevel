package org.buzzjoe.SimpleDataStorage;

import java.io.*;
import org.apache.commons.codec.binary.Base64;

import java.util.ArrayList;
import java.util.Properties;


/**
 * A class for handling a simple way to store data on the local file system.
 * Data will be stored as XML and can be given as String or Integer.<br>
 * <br>
 * There's no need to make the file. SDS will do that for you when it 
 * needs to write to it the first time.<br>
 * <br>
 * Usage:<br>
 * <br>
 * (1) <br>
 * Make an Instance of SDS by giving it a file including it's path:<br>
 * <br>
 * SimpleDataStorage sds = new SimpleDataStorage('path/to/file.xml');<br>
 * <br>
 * (2.1)<br>
 * Get data:<br>
 * <br>
 * Object someData = sds.get('keyName');<br>
 * <br>
 * You will get a result of type Object. So you need to cast it to Integer,
 * String or whatever yourself.<br>
 * Keys can be tiled into sections when using a dot (buzzjoe.home).<br>
 * SDS will eat every SERIALIZEABLE data type you give it.
 * <br>
 * (2.2)<br>
 * Set data:<br>
 * <br>
 * String someData = 'Germany';<br>
 * sds.set('buzzjoe.home', someData)<br>
 * <br>
 * This Software uses the Apache Commons Codec library under Apache License 2.0<br>
 * Homepage: http://commons.apache.org/codec/<br>
 * License: http://www.apache.org/licenses/LICENSE-2.0.html<br>
 * <br>
 * License: free for all time<br>
 * <br>
 * Version: 0.3.3<br>
 * <br>
 * Just save data in destructor when autosave is enabled.<br>
 * <br>
 * Version: 0.3.2<br>
 * <br>
 * Changelog:<br>
 * <br>
 * 0.3.2:<br>
 * Fixed some issues when requesting a key that does not exist.<br>
 * <br>
 * 0.3.1:<br>
 * Bugfix for wrong decrement() when called without amount.<br>
 * <br>
 * 0.3:<br>
 * Improved performance. SDS now only writes out to a file if changes were 
 * made. So there are no writing-actions when just getting some information 
 * but not setting anyting.<br>
 * Even writeToFile() will do nothing if there wasn't any change.<br>
 * <br>
 * 0.2:<br>
 * Added increment(), decrement(), keyExists() and a switch for turning off 
 * autosave. When turned off writeToFile() must be called manually to store 
 * Data in an XML file.<br>
 * <br>
 * 0.1:<br>
 * Initial release<br>
 * 
 * @author BuzzJoe - michael@xenotek.de
 */
public class SimpleDataStorage {
	
	private String version = "0.3.2";
	
	/**
	 * @var file The file's name which content is availabe in class' instance
	 */
	private String file;
	
	/**
	 * @var data Propreties container
	 */
	private Properties data = new Properties();
	
	/**
	 * @var Enables autosave when setting or deleting a key
	 */
	private boolean doAutosave;
	
	/**
	 * @var Flag that is set to true when any data has been changed
	 */
	private boolean dataChanged = false;
	
	//-------------------------------------------------------------------------
	
	public String version() {
		return this.version;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Stores Data out of this.file in local properties container this.data
	 * 
	 * @param file (String) Filename (XML, with path) to be read into local "data" Container
	 */
	public SimpleDataStorage(String file, boolean autosave) {
		this.file = file;		
		this.doAutosave = autosave;		
		FileInputStream fis;
		try {
			fis = new FileInputStream(this.file);
			this.data.loadFromXML(fis);
	        fis.close();
		} catch (Exception e) {
			// well. Seems like there was no file - That's no fault.
			// Anyway, that can be ignored. File will be made when we will
			// set some data the first time.
			// so: nothing to handle here
		}

 	}
	
	//-------------------------------------------------------------------------

	/**
	 * Constructor without autosave parameter. Autosave will be turned ON when
	 * calling this.
	 * @param file (String) Filename (XML, with path) to be read into local "data" Container
	 */
	public SimpleDataStorage(String file) {
		this(file, true);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Write this.data into XML file before gc-ing.
	 */
	public void finalize() {
		if (this.dataChanged & this.doAutosave) this.writeToFile();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns all available properties in this.data
	 * 
	 * @return All stored properties. Returns Instance of java.util.Properties
	 */
	public Properties getAll() {
		return this.data;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns properties value out of this.data container.
	 * 
	 * @param propertyName Property to get  
	 * @return a string containing the value of requestet proprety 
	 */
	public Object get(String propertyName) {
		
		String data = this.data.getProperty(propertyName);
		
		try {
		    int integer = Integer.parseInt(data);
		    // Looks like we've got an Integer value. Return it.
		    return integer;
		} catch(NumberFormatException nFE) {
			// whooops. This wasn't integer. Let's try if we can deserialize...
			try {

				byte[] decoded = Base64.decodeBase64(data);
				
				ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(decoded));
				Object oData = in.readObject();
				in.close();
				
				return oData;
				
			} catch (Exception e) {
				// Nope. Not even serialized stuff. So it must be a string
				return data;
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Does the same like this.get() but will return an int Object.<br>
	 * Returns 0 when key does not exist.<br>
	 * ATTENTION! This method is NOT suitable for proofing if a special key 
	 * exists!<br>
	 * Use this.keyExists(key); for that!
	 * 
	 * @param keyName
	 * @return int Object
	 */
	public int getInt(String keyName) {
		
		int result = 0;	
		
		// see if requested key exists
		if (this.keyExists(keyName)) {
			Object data = this.get(keyName);
				
			try {
				result = (Integer)data;
			} catch (Exception e){
				System.out.println("SimpleDataStorage - Error: Can't cast to int. Requested key " + keyName + " is not an Integer type.");
			}
		}
		
		return result;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Does the same like this.get() but will return an ArrayList Object.
	 * Returns empty ArrayList Object when key does not exist.<br>
	 * ATTENTION! This method is NOT suitable for proofing if a special key 
	 * exists!<br>
	 * Use this.keyExists(key); for that!
	 * 
	 * @param keyName
	 * @return ArrayList Object
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<Object> getArrayList(String keyName) {		
		ArrayList<Object> result = new ArrayList<Object>();
		
		if (this.keyExists(keyName)) {
			Object data = this.get(keyName);
			try {
				result = (ArrayList<Object>)data;
			} catch (Exception e){
				System.out.println("SimpleDataStorage - Error: Can't cast to ArrayList. Requested key " + keyName + " is not an ArrayList type.");
			}
		}	
		return result;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Does the same like this.get() but will return an String Object.
	 * Returns an empty String when key does not exist.<br>
	 * ATTENTION! This method is NOT suitable for proofing if a special key 
	 * exists!<br>
	 * Use this.keyExists(key); for that!
	 * 
	 * @param keyName
	 * @return String Object
	 */
	public String getString(String keyName) {
		String result = new String();
		
		if (this.keyExists(keyName)) {
			Object data = this.get(keyName);
			try {
				result = (String)data;
			} catch (Exception e){
				System.out.println("SimpleDataStorage - Error: Can't cast to int. Requested key " + keyName + " is not an String type.");
			}
		}
		return result;
	}
	
	//-------------------------------------------------------------------------
	/**
	 * Stores some Data in this.data container.<br>
	 * 
	 * @param keyName f.e. 'myKey' or 'buzzjoe.home'
	 * @param data data to be stored. Accepts String and Integer by default. Other datatypes will be serialized.
	 * @param serializeData set to true if you want to serialize in every case
	 * 
	 */
	public void set(String keyName, Object data, boolean serializeData) {
		
		if ((data instanceof Integer || data instanceof String) && !serializeData) {
			this.data.setProperty(keyName, data.toString());
			this.dataChanged = true;
		} else {
			
			// okay. Seems like we got something to serialize
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buf = null;
			try {				
				ObjectOutput out = new ObjectOutputStream(bos);
				out.writeObject(data);
				out.close();
				buf = bos.toByteArray();
				
			} catch (Exception e) {
				System.out.println("SimpleDataStorage - Error: Given datatype for key " + keyName + " is not serializeable. Can't store it.");
				System.out.println(e.toString());
			}
			
			byte[] encoded = Base64.encodeBase64(buf);
			String outString = "";
			try {
				outString = new String(encoded, "ASCII");
			} catch (Exception e) {
				
			}
			this.data.setProperty(keyName, outString);
			this.dataChanged = true;
		}
		
		// this is a very bad idea. But it is the only secure way to store
		// the data to the file system. 
		// Needs some polish because method is called after every changing action
		if (this.doAutosave) this.writeToFile();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Second method for handling different parameter.<br>
	 * See other this.set() method for more details.
	 * 
	 * @param keyName f.e. 'myProperty' or 'buzzjoe.home'
	 * @param data data to be stored. Accepts String and Integer by default. Other datatypes will be serialized.
	 */
	public void set(String keyName, Object data) {
		this.set(keyName, data, false);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Delete a property out of the this.data storage
	 * 
	 * @param keyName Property to delete
	 */
	public void delete(String keyName) {
		this.data.remove(keyName);	
		this.dataChanged = true;
		
		// this is a very bad idea. But it is the only secure way to store
		// the data to the file system. 
		// Needs some polish because method is called after every changing action
		if (this.doAutosave) this.writeToFile();
	}	
	
	//-------------------------------------------------------------------------
	
	/**
	 * Write this.data to this.file as XML<br>
	 * Will do nothing when not data has been changed before.
	 */
	public void writeToFile() {
		
		// data changed? If not then do nothing. That's good for performance.
		if (this.dataChanged) {
			
			String filePath;
			
			// check if path to file was given and extract it
			try {
				filePath = this.file.substring(0, this.file.lastIndexOf("/"));
			} catch (StringIndexOutOfBoundsException e) {
				filePath = "";
			}
			
			// if there was a file path given, then make folders if needed
			if (filePath != "") {
				File folder = new File(filePath);
				folder.mkdirs();
			}
			
	        // make file if needed and write XML data to it.
	        try {
	        	FileOutputStream fos = new FileOutputStream(this.file);
	        	this.data.storeToXML(fos, this.file, "UTF-8");
	        	fos.close();
	        	// data has been written to file. So this.dataChanged can be set back to false
	        	this.dataChanged = false;
	        } catch (Exception e) {
	        	System.out.println("SimpleDataStorage - Error: Unable to write to " + this.file);
	        }
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns either true if searched key exists or false if not.
	 * 
	 * @param keyName Properties key (f.e. "buzzjoe.age")
	 * @return boolean
	 */
	public boolean keyExists(String keyName) {
		return this.data.containsKey(keyName);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Returns either true if automatic writing to file is enabled or false
	 * if not.
	 * 
	 * @return boolean
	 */
	public boolean autosaveEnabled() {
		return this.doAutosave;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Increments key's value by amount.<br>
	 * If no amount is given, key will be incremented by 1<br>
	 * ATTENTION! This will cause wrong data if you try to increment a non-int
	 * key!
	 * 
	 * @param key Properties key (f.e. "buzzjoe.age")
	 * @param amount Amount that should be incremented to value
	 * @return new amount of key
	 */
	public int increment(String key, int amount) {
		int newAmount = this.getInt(key) + amount;
		this.set(key, newAmount);
		this.dataChanged = true;
		return newAmount;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Increments key's value by 1<br>
	 * ATTENTION! This will cause wrong data if you try to increment a non-int
	 * key!
	 * 
	 * @param key Properties key (f.e. "buzzjoe.age")
	 * @return new amount of key
	 */
	public int increment(String key) {
		return this.increment(key, 1);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Decrements key's value by amount.<br>
	 * If no amount is given, key will be incremented by 1<br>
	 * ATTENTION! This will cause wrong data if you try to increment a non-int
	 * key!
	 * 
	 * @param key Properties key (f.e. "buzzjoe.age")
	 * @param amount Amount that should be incremented to value
	 * @return new amount of key
	 */
	public int decrement(String key, int amount) {
		int newAmount = this.getInt(key) - amount;
		this.set(key, newAmount);
		this.dataChanged = true;
		return newAmount;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Decrements key's value by 1<br>
	 * ATTENTION! This will cause wrong data if you try to increment a non-int
	 * key!
	 * 
	 * @param key Properties key (f.e. "buzzjoe.age")
	 * @return new amount of key
	 */
	public int decrement(String key) {
		return this.decrement(key, 1);
	}
	
	//-------------------------------------------------------------------------
}
