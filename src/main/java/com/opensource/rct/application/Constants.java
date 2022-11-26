package com.opensource.rct.application;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.opensource.rct.model.MagicNumber;

/**
 * 
 * This class hold all kind of static constants being used for the communication with the inverter.
 * The constants are like default ports, magic numbers for sending request and templates for the byte
 * arrays to be sent.
 * */

public class Constants
{
	//communication parameters
	public static String hostname = "";
	public static int port = 8899;
	public static Long timeoutConverter = 3000L;
	public static String hostnameInfluxDb = "";
	public static int portInfluxDb = 8086;
	
	public static String logLevel = "error"; 
	public static String logFile = "";
	
	public static int panelPower = 0;
	public static int panelsA = 0;
	public static int panelsB = 0;

	
	public static HashMap<String, MagicNumber> magicNumberObjectMap = new HashMap<String, MagicNumber>(); //contains data for each magic number, filled through CSV file
	
	public static List<String> magicNumbersToBeRead = new LinkedList<String>(); // all the magic numbers which should be polled for, will be filled from config csv file upon startup
		
}
