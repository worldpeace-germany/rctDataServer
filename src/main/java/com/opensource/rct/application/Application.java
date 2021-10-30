package com.opensource.rct.application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Timer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.opensource.rct.model.MagicNumber;
import com.opensource.rct.storage.Influxdb;
import com.opensource.rct.timertasks.PollData;

/**
 * 
 * @author JR
 * 
 * TODO: 
 * - requestData servlet returns result as plain/text, somehow returning as JSON object throws an exception. Has to be cleaned up one day.
 * - getData für Strings implementieren
 * - building request strings, does escaping change the length variable?
 * - reconnect for inverter connection
 */


@SpringBootApplication(scanBasePackages = {"com.opensource.rct.servlet"})	//servlet classes in different package
public class Application
{
	private static Properties props;
	private static Logger logger = LogManager.getLogger();
	private static Timer timer = new Timer(true);


	public static void main(String[] args) throws Exception
	{

		ApplicationContext ctx = SpringApplication.run(Application.class, args);

		readConfiguration();
		Configurator.setLevel("com.opensource.rct", Level.toLevel(Constants.logLevel));
		
		createMagicNumberObjects();	//read master data from configuration file
		
		logger.info("<<<<< INFO >>>>> Application::Main: Working directory is " + System.getProperty("user.dir"));
		logger.info("<<<<< INFO >>>>> Application::Main: RCT data server has been started. Going to connect to inverter...");
		
		Inverter inverter = new Inverter();
		inverter.connect();
	
		if(!Constants.hostnameInfluxDb.trim().equalsIgnoreCase("") && Constants.hostnameInfluxDb != null)
		{
			if(Influxdb.initInfluxDB())
			{
				logger.info("<<<<< INFO >>>>> Application::Main: Polling data in regular intervalls to write data to database.");
				timer.schedule(new PollData(), 0, 60000); // poll data at regular intervals
			}
			else
			{
				logger.error("<<<<< ERROR >>>>> Application::Main: No database has been created. Polling data has been disabled.");
			}
		}
		else
		{
			logger.info("<<<<< INFO >>>>> Application::Main: No database configured, hence no polling of data at regular intervalls.");
		}
	}

	static void readConfiguration()
	{
		props = new Properties();
		String storagePath = System.getProperty("user.home");	// Store config data in users app config directoy, location depends on platform
		String osName = System.getProperty("os.name");
		
		if(osName.toLowerCase().indexOf("win") > -1 )
		{
			storagePath = storagePath + "\\AppData\\Local\\rctDataServerConfig\\";
		}
		else if(osName.toLowerCase().indexOf("mac") > -1 )
		{
			storagePath = storagePath + "/Library/Application Support/rctDataServerConfig/";
		}
		else if(osName.toLowerCase().indexOf("linux") > -1 )
		{
			storagePath = storagePath + "/.local/share/rctDataServerConfig/";
		}
		else
		{
			storagePath = "";	//get the rctDataServer.properties file from the same location where you start the jar from
			logger.error("Application::Main: Taking config file from execution directory. Cannot determine the platform, platform has to be Windows or Mac. Your platform: " + System.getProperty("os.name"));
		}
				
		File file = new File( storagePath + "rctDataServer.properties");
			
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(file);
			props.load(fis);
			
			Constants.hostname = (String) props.getProperty("hostnameInverter", "");
			Constants.port = Integer.valueOf(props.getProperty("portInverter", "8899"));
			Constants.logLevel = (String) props.getProperty("logLevel", "error");
			Constants.timeoutConverter = Long.valueOf(props.getProperty("timeoutConverter", "3000"));
			Constants.hostnameInfluxDb = (String) props.getProperty("influxdbServer", ""); //empty influx DB parameter signals that no influx is being used, hence have no default value
			Constants.portInfluxDb = Integer.valueOf(props.getProperty("influxdbPort", "8086"));
			Constants.panelPower = Integer.valueOf(props.getProperty("panelPower", "0"));	//if zero then no settings made, check before usage
			Constants.panelsA = Integer.valueOf(props.getProperty("panelsA", "0"));
			Constants.panelsB = Integer.valueOf(props.getProperty("panelsB", "0"));
			
			fis.close();
		}
		catch(FileNotFoundException e)
		{
			logger.error("<<<<< ERROR >>>>> Application::readConfiguration Config file rctDataServer.properties not found in " + storagePath);
		}
		catch(NumberFormatException e)
		{
			logger.error("<<<<< ERROR >>>>> Application::readConfiguration Port number has to be an integer or omit parameter for default 8899");
		} catch (IOException e) 
		{
			logger.error("<<<<< ERROR >>>>> Application::readConfiguration Config file rctDataServer.properties found, but problems to read it.");
		}
	}

	static void createMagicNumberObjects()
	{
		 InputStream is = com.opensource.rct.application.Application.class.getResourceAsStream("/RCT_magic_numbers.csv");
		 BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		 String line = null;
		 
		 try
		 {
			while ((line = reader.readLine()) != null) 
			 {
				String[] inputArray = line.split(";");
				
				MagicNumber magicNumberObject = new MagicNumber();

				if(inputArray.length >= 3)
				{
					magicNumberObject.setMagicNumber(inputArray[0]);
					magicNumberObject.setDescription(inputArray[1]);
					magicNumberObject.setDataType(inputArray[2]);
					if(inputArray.length >= 4)
					{
						magicNumberObject.setDatabaseName(inputArray[3]);
					}
					if(inputArray.length == 5)
					{
						magicNumberObject.setMeasurementName(inputArray[4]);
						if(inputArray[4] != null && !inputArray[4].trim().equalsIgnoreCase(""))
						{
							Constants.magicNumbersToBeRead.add(inputArray[0]);	//add magic number to be polled for 
						}
					}
					Constants.magicNumberObjectMap.put(inputArray[0], magicNumberObject);
				}
				else
				{
					logger.error("<<<<< ERROR >>>>> Application::createMagicNumberObjects input line in CSV doesn't have the right amount of entries. " + line);
				}
			 }
			 reader.close();
		 } 
		 catch (IOException e)
		 {
			e.printStackTrace();
		 }
	}
}
