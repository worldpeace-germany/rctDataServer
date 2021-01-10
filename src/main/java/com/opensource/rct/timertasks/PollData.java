package com.opensource.rct.timertasks;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Calendar;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.opensource.rct.application.Constants;
import com.opensource.rct.application.Helper;
import com.opensource.rct.storage.Influxdb;


/**
 * 
 * @author JR
 * 
 * Timer task to poll data regularly from inverter to store them in a database, currently only influx is supported. Data which is being polled
 * for is configured in Constants.magicNumbersToBeRead where this map is being is being filled from the csv with all magic numbers having a 
 * measurement assigned. 
 *
 */
public class PollData extends TimerTask{
	
	private static Logger logger = LogManager.getLogger();

	@Override
	public void run() 
	{
		logger.debug("<<<<< DEBUG >>>>> PollData::run Timer triggered.");
		
		for (String magicNumber : Constants.magicNumbersToBeRead) 
		{
			Constants.magicNumberObjectMap.get(magicNumber).setDataReady(false);	//before requesting data set data ready to false

    		byte[] requestBytes = Helper.buildRequestByteArrayDataLoggerShort(magicNumber);
    		boolean newDataReceived = sendRequestToInverter(requestBytes, magicNumber); //finally communicate with inverter
    		
    		if(newDataReceived)
    		{
    			logger.debug("<<<<< DEBUG >>>>> PollData::run Received new data to be sent to Influx DB.");
    			Influxdb.postData2Influx(
    					Constants.magicNumberObjectMap.get(magicNumber).getDataJson().get("timestamp").getAsLong(), 
    					Constants.magicNumberObjectMap.get(magicNumber).getDatabaseName(), 
    					Constants.magicNumberObjectMap.get(magicNumber).getMeasurementName(), 
    					Constants.magicNumberObjectMap.get(magicNumber).getDescription(),
    					Constants.magicNumberObjectMap.get(magicNumber).getDataJson().get("value").getAsFloat() 
    					);
    		}
		}	
    }

	
	private boolean sendRequestToInverter(byte[] inputByteArray, String magicNumber)
	{
		Socket inverterSocket = null;
		DataOutputStream dOut = null;
		try 
		{
			inverterSocket = new Socket(Constants.hostname, Constants.port);
			dOut = new DataOutputStream(inverterSocket.getOutputStream());
			dOut.write(inputByteArray);
			dOut.flush();
			logger.debug("<<<<< DEBUG >>>>> RequestData::getData Data has been requested from inverter.");
		} catch (IOException e) 
		{
			logger.error("<<<<< ERROR >>>>> RequestData::getData Cannot connect to inverter. Check hostname and port.");
		}
		finally 
		{
			try 
			{
				dOut.close();
			} 
			catch (IOException e) 
			{
			}
			finally
			{
				try 
				{
					inverterSocket.close();
				} 
				catch (IOException e) 
				{
				}	
			}
		}
		
		Long startWaitTime = Calendar.getInstance().getTimeInMillis();
		boolean requestTimeOut = false;
			
		while(!Constants.magicNumberObjectMap.get(magicNumber).isDataReady() && !requestTimeOut)
		{
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger.error("<<<<< ERROR >>>>> RequestData::getData Thread sleep interrupted.");
				e.printStackTrace();
			}
			
			if(startWaitTime + Constants.timeoutConverter < Calendar.getInstance().getTimeInMillis())
			{
				requestTimeOut = true;
				logger.warn("<<<<< DEBUG >>>>> RequestData::getRctData " + magicNumber + " timeout " + (Calendar.getInstance().getTimeInMillis() - startWaitTime));
			}	
		}
		
		if(requestTimeOut)
		{
			return false;
		}
		else if(!Constants.magicNumberObjectMap.get(magicNumber).getDataJson().get("sucess").getAsBoolean())
		{
			return false;	//sanity check failed so don't write to InfluxDB
		}
		else
		{	
			logger.debug("<<<<< DEBUG >>>>> RequestData::getRctData Writing data to database. {}", Constants.magicNumberObjectMap.get(magicNumber).getDataJson().toString());
			
			return true;
		}
	}
}
