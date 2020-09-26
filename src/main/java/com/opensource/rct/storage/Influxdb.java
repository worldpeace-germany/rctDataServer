package com.opensource.rct.storage;

import java.awt.Dimension;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.opensource.rct.application.Constants;
import com.opensource.rct.model.MagicNumber;

public class Influxdb {
	
	private static Logger logger = LogManager.getLogger();
	
	
	public static void postData2Influx(long timestamp, String database, String measurement, String tag, double value )	//tags in Influx are the dimensions
	{
		final String url = "http://" + Constants.hostnameInfluxDb + ":" + Constants.portInfluxDb + "/write?db=" + database + "&precision=s";
		
		tag = tag.replaceAll(" ", "\\\\ ");	//Influx needs to escape spaces in tags with a single \ to have it correctly we need double escape the java character
		String postString = measurement + ",dimension=" + tag + " value=" + value + " " + timestamp;
		
		try
		{
			HttpURLConnection HttpSessionToken = (HttpURLConnection)new URL(url).openConnection();	
			
			HttpSessionToken.setRequestMethod("POST"); 
			HttpSessionToken.setDoOutput(true);			
			HttpSessionToken.setRequestProperty("Accept", "*/*");
			
			//Preparing the output stream for POST
			OutputStream postContent = (OutputStream)HttpSessionToken.getOutputStream();
            postContent.write(postString.getBytes("UTF-8"));
            postContent.flush();
            postContent.close();
			
			int returnCode = HttpSessionToken.getResponseCode(); 
			
			if (returnCode == 204)	//Influx DB sends a 204 and not a 200 upon success
			{
				logger.debug("<<<<< DEBUG >>>>> Influxdb::postData2Influx Data sent successfully to Influx DB. Measurement {}, dimension {}, value {}, time {}", measurement, tag, value, timestamp);
			}
			else
			{
				logger.debug("<<<<< ERROR >>>>> Influxdb::postData2Influx sending data {} to Influx DB {} returned {}", postContent, url, returnCode);
			}
		}
		catch(IOException e)
		{
			logger.error("<<<<< ERROR >>>>> Influxdb::postData2Influx Execption when communicating to server. " + e.getMessage() );
		}
	}
	
	/**
	 * Method to initialize the Influx DB. 
	 * 
	 * Cycles through the Hashmap containing all magic numbers with their configuration data. For each magic number which should be stored
	 * in influx a database needs to be created. The database names are give in that configuration data.
	 * 
	 * returns true if at least one database in Influx was created successfully, returns false if no database was created.
	 */
	public static boolean initInfluxDB()
	{
		List<String> listOfDatabases = new LinkedList<String>();
		
    	for (HashMap.Entry<String, MagicNumber> entry : Constants.magicNumberObjectMap.entrySet()) 
    	{     		
    		if(entry.getValue().getDatabaseName() != null && !entry.getValue().getDatabaseName().trim().equalsIgnoreCase(""))
    		{
    			String dbName = entry.getValue().getDatabaseName().trim();
    			if(listOfDatabases.indexOf(dbName) == -1)
    			{
    				final String url = "http://" + Constants.hostnameInfluxDb + ":" + Constants.portInfluxDb + "/query?q=create%20database%20" + dbName;
    				
    				try
    				{
    					HttpURLConnection HttpSessionToken = (HttpURLConnection)new URL(url).openConnection();	
    					
    					HttpSessionToken.setRequestMethod("POST"); 
    					HttpSessionToken.setDoOutput(true);			
    					HttpSessionToken.setRequestProperty("Accept", "*/*");
    					
    					//Preparing the output stream for POST
    					OutputStream postContent = (OutputStream)HttpSessionToken.getOutputStream();
    		            postContent.flush();
    		            postContent.close();
    					
    					int returnCode = HttpSessionToken.getResponseCode(); 
    					
    					if (returnCode == 200)	
    					{
    						logger.debug("<<<<< DEBUG >>>>> Influxdb::initInfluxDB Database {} successfully created.", dbName);
    						listOfDatabases.add(dbName);
    					}
    					else
    					{
    						logger.debug("<<<<< ERROR >>>>> Influxdb::initInfluxDB Creating database {} with URL {} returned {}", dbName, url, returnCode);
    					}
    				}
    				catch(IOException e)
    				{
    					logger.error("<<<<< ERROR >>>>> Influxdb::postData2Influx Execption when communicating to server. " + e.getMessage() );
    				}
    			}
    		}
    		
    	}
    	if(listOfDatabases.size() == 0)
    	{
    		return false;
    	}
		return true;
	}

}
