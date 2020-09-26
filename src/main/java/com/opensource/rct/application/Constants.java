package com.opensource.rct.application;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonObject;
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
	
	public static int panelPower = 0;
	public static int panelsA = 0;
	public static int panelsB = 0;

	
	public static HashMap<String, MagicNumber> magicNumberObjectMap = new HashMap<String, MagicNumber>(); //contains data for each magic number
	
	public static List<String> magicNumbersToBeRead = new LinkedList<String>(); // all the magic numbers which should be polled for, will be filled from config csv file upon startup
	
	/**
	 * This HashMap contains all magic numbers which are known to the system and which respond with a short response.
	 * If new magic numbers responding with a short response are known then these need to be added here.
	 */
	public static HashMap<String, String> keyDescriptionMapNumber = new HashMap<String, String>() 
	{
		{
			//values self-observed
			put("1AC87AA0", "Current house power usage in W");
			put("91617C58", "Public grid power in W, negativ if feed in");
			put("DB11855B", "DC input power A in W");
			put("0CB5D21B", "DC input power B in W");
			put("B55BA2CE", "DC input voltage A in V");  //documentation says key is B298395D, documentation seems to be wrong
			put("B0041187", "DC input voltage B in V"); //documentation says key is 5BB8075A, documentation seems to be wrong
			put("A7FA5C5D", "Battery voltage in V");
			put("BD55905F", "Eac - Energy today in Wh"); 
			put("10970E9D", "Eac - Energy this month in Wh");
			put("C0CC81B6", "Eac - Energy this year in Wh"); 
			put("B1EF67CE", "Eac - Total energy in Wh"); 
			put("DB2D69AE", "Current AC power of inverter in W");
			put("959930BF", "Battery state of charge (SoC)");
			put("8B9FF008", "Upper load boundary of battery in %"); //max charge level of battery
			put("400F015B", "Battery power in W (positiv discharge, negative charge)");
			put("7F813D73", "Bit coded fault word 3"); //unclear what information is given here
			put("E96F1844", "External power (additional inverters/generators in house internal grid) in W");
			
			//values from github
			put("CF053085", "Phase L1 Voltage in V");
			put("54B4684E", "Phase L2 Voltage in V");
			put("2545E22D", "Phase L3 Voltage in V");
			put("B408E40A", "Battery current measured by inverter, low pass filter with Tau = 1s in A");
			put("902AFAFB", "Battery temperature in C");
			put("2AE703F2", "Energy today in Wh input A");
			put("FBF3CE97", "Energy today in Wh input B");
			put("3C87C4F5", "Energy feed into public grid today in Wh");
			put("867DEF7D", "Energy taken from public grid today in Wh");
			put("2F3C1D7D", "Energy used today in Wh");
			put("81AE960B", "Energy this month input A in Wh");
			put("7AB9B045", "Energy this month input B in Wh");
			put("65B624AB", "Energy feed into public grid this month in Wh");
			put("126ABC86", "Energy taken from public grid this month in Wh");
			put("F0BE6429", "Energy used this month in Wh");			
			put("AF64D0FE", "Energy this year input A in Wh");
			put("BD55D796", "Energy this year input B in Wh");
			put("26EFFC2F", "Energy feed into public grid this year in Wh");
			put("DE17F021", "Energy taken from public grid this year in Wh");
			put("C7D3B479", "Energy used this year in Wh");
			put("FC724A9E", "Energy total input A in Wh");
			put("68EEFD3D", "Energy total input B in Wh");
			put("44D4C533", "Energy feed into public grid total in Wh");
			put("62FBE7DC", "Energy taken from public grid total in Wh");
			put("EFF4B537", "Energy used total in Wh");
			put("FE1AA500", "External Power Limit");
			put("BD008E29", "External Battery power target in W, positive is discharge");
			put("872F380B", "External load demand in W (positive = feed in / 0=internal )");
			put("4BC0F974", "gross battery capacity kWh");
			put("37F9D5CA", "Bit coded fault word 0");
			put("234B4736", "Bit coded fault word 1");
			put("3B7FCD47", "Bit coded fault word 2");
			
			//RCT Documentation
			put("8FC89B10", "Service Variable");
			put("1C4A665F", "Public grid frequency in Hz");
			put("63476DBE", "Public grid phase to phase L1-L2 voltage [V]");
			put("485AD749", "Public grid phase to phase L2-L3 voltage [V]");
			put("F25C339B", "Public grid phase to phase L3-L1 voltage [V]");
			put("C717D1FB", "Total system insulation in Ohm");
			put("5F33284E", "Inverter actual state");
			put("9D785E8C","Battery BMS main software version");
			put("1B39A3A3","Battery BMS power software version");
			put("6388556C","Battery stack 0 software version");
			put("A54C4685","Battery stack 1 software version");
			put("C8BA1729","Battery stack 2 software version");
			put("086C75B0","Battery stack 3 software version");	//one digit missing in magic number, bug in RCT documentation, added a 0 at the start seems to be the most obvious cause
			put("A40906BF","Battery stack 4 software version");
			put("EEA3F59B","Battery stack 5 software version");
			put("6974798A","Battery stack 6 software version");
		}
	};
	
	/**
	 * This HashMap contains all magic numbers which are known to the system and which respond with a long response.
	 * If new magic numbers responding with a long response are known then these need to be added here.
	 */
	
	public static HashMap<String, String> keyDescriptionMapArray = new HashMap<String, String>() 
	{
		{
			put("B20D1AD6","Daily grid feed energy (Egrid feed) [Wh]");
			put("05C7CFB1","Daily grid load energy (Egrid load) [Wh]"); // leading zero added, seem to be missing in documentation
			put("FCF4E78D","Daily energy on DC input A (Edc A) [Wh]");
			put("0DF164DE","Daily energy on DC input B (Edc B) [Wh]"); // leading zero added, seem to be missing in documentation
			put("60A9A532","Daily external energy (Eext) [Wh]");
			put("CA6D6472","Daily haushold energy (Eload) [Wh]");
			put("E04C3900","Daily energy on AC output (Eac) [Wh]");
			
			put("921997EE","Monthly grid feed energy (Egrid feed) [Wh]");
			put("5D34D09D","Monthly grid load energy (Egrid load) [Wh]"); 
			put("2F0A6B15","Monthly energy on DC input A (Edc A) [Wh]");
			put("6B5A56C2","Monthly energy on DC input B (Edc B) [Wh]"); 
			put("E4DC040A","Monthly external energy (Eext) [Wh]");
			put("431509D1","Monthly haushold energy (Eload) [Wh]");
			put("F28341E2","Monthly energy on AC output (Eac) [Wh]");
			
			put("19B814F2","Yearly grid feed energy (Egrid feed) [Wh]");
			put("C55EF32E","Yearly grid load energy (Egrid load) [Wh]"); 
			put("4C14CC7C","Yearly energy on DC input A (Edc A) [Wh]");
			put("34ECA9CA","Yearly energy on DC input B (Edc B) [Wh]"); 
			put("4E9D95A6","Yearly external energy (Eext) [Wh]");
			put("E5FBCC6F","Yearly haushold energy (Eload) [Wh]");
			put("70BD7C46","Yearly energy on AC output (Eac) [Wh]");
			
			put("A60082A9","5-minute grid feed energy (Egrid feed) [Wh]");
			put("9247DB99","5-minute grid load energy (Egrid load) [Wh]"); 
			put("50B441C1","5-minute energy on DC input A (Edc A) [Wh]");
			put("1D49380A","5-minute energy on DC input B (Edc B) [Wh]"); 
			put("3906A1D0","5-minute external energy (Eext) [Wh]");
			put("A7C708EB","5-minute haushold energy (Eload) [Wh]");
			put("669D02FE","5-minute energy on AC output (Eac) [Wh]");
			put("5293B668","5-minute value SoC");
			put("76C9A0BD","5-minute target value SoC");
			put("D3E94E6B","5-minute Last known battery temperature BMS sent (Tempbat) [°C]");
			put("132AA71E","5-minute Last known heat sink temperature close to the battery boost transistors (Temp2) [°C]");
			put("064E4340","5-minute Average battery voltage (Ubat) [V]"); // leading zero added, seem to be missing in documentation
			put("5411CE1B","5-minute Average RMS voltage on phase 1 (UL1) [V]");
			put("488052BA","5-minute Average RMS voltage on phase 2 (UL2) [V]");
			put("095AFAA8","5-minute Average RMS voltage on phase 3 (UL3) [V]"); // leading zero added, seem to be missing in documentation
			put("72ACC0BF","5-minute Average voltage on DC input A [V]");
			put("0FA29566","5-minute Average voltage on DC input B [V]"); // leading zero added, seem to be missing in documentation
			put("CBDAD315","5-minute Energy on battery terminal (Ebat) [Wh]");
			put("21879805","5-minute Energy on AC phase 1 output (Eac1) [Wh]");
			put("554D8FEE","5-minute Energy on AC phase 2 output (Eac1) [Wh]");
			put("E29C24EB","5-minute Energy on AC phase 3 output (Eac1) [Wh]");
		}
	
	};
	
	/**
	 * This HashMap contains all magic numbers which are known to the system and which respond with a string response.
	 * If new magic numbers responding with a string response are known then these need to be added here.
	 */
	public static HashMap<String, String> keyDescriptionMapString = new HashMap<String, String>() 
	{

		{
			//values self-observed
			put("EBC62737", "Inverter discription");	//String value
			
			//values from github

			//RCT Documentation
			put("7924ABD9", "Inverter serial number");	//String value
			put("68BC034D", "Actual Norm");				//String value
			put("DDD1C2D0", "Inverter software version"); //String value
			put("16A1F844","Battery BMS serial number"); //String value
			put("FBF6D834","Battery stack 0 serial number"); //String value
			put("99396810","Battery stack 1 serial number"); //String value
			put("73489528","Battery stack 2 serial number"); //String value
			put("257B7612","Battery stack 3 serial number"); //String value
			put("4E699086","Battery stack 4 serial number"); //String value
			put("162491E8","Battery stack 5 serial number"); //String value
			put("5939EC5D","Battery stack 6 serial number"); //String value
		}
	};
	
	
	/*
	 * These are the magic numbers which appear on the stream but which seem not to be documented. List these magic numbers here that we
	 * can ignore them until they are known.
	 * */
	public static String[] unknownMagicNumbers = {	"3623D82A", "DB2D2D69", "DC667958", "36A9E9A6", "5F332858", "701A0482", 
													"FED51BD2", "99EE89CB", "70A2AF4F", "5B10CE81", "682CDDA1", "97E203F9", 
													"4E3CB7F8", "437B8122"};
		
}
