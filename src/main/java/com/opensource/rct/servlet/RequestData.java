package com.opensource.rct.servlet;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;
import com.opensource.rct.application.Constants;
import com.opensource.rct.application.Helper;


@RestController
public class RequestData {

	private static Logger logger = LogManager.getLogger();
	private String content = "";	//content to be returned
	private int responseCode;
	private String magicNumber;
	int requestedDataArrayPosition;
	
	/**
	 * Method to get data from the inverter. A GET request has to be sent with two URL parameters
	 * 
	 * - magicNumber: 	This has to be the magic number of the data you would like to read, a string representing 4 bytes (=8 characters)
	 * - timestamp: 	This time stamp has to be in UNIX epoc time in seconds, not ms! Data will be returned up to this timestamp. Older data
	 * 					might exist than what it is returned, then this has to be requested again.
	 * 
	 * @return
	 * A JSON array containing the requested data.
	 */
	
    //@RequestMapping(value="/getData", produces={"application/json"})
    @RequestMapping(value="/getData")
    public String getRctData(HttpServletRequest request, HttpServletResponse response)
    {
    	if (request.getParameterMap().containsKey("magicNumber")) 
    	{
    		magicNumber = request.getParameter("magicNumber").toUpperCase();    		
    		logger.debug("<<<<< DEBUG >>>>> RequestData::getRctData Received a request for magicNumber " + magicNumber); 
    		
    		byte[] requestBytes = null; //Byte array to be sent to inverter
    		if(!(magicNumber == null || magicNumber.equalsIgnoreCase("") ))
        	{
		    	if(Constants.keyDescriptionMapNumber.containsKey(magicNumber))
		        {
		    		Constants.magicNumberObjectMap.get(magicNumber).setDataReady(false);
		    		requestBytes = Helper.buildRequestByteArrayDataLoggerShort(magicNumber);
		    		sendRequestToInverter(requestBytes); //finally communicate with inverter
		        }
		    	else if(Constants.keyDescriptionMapArray.containsKey(magicNumber))
			    {
		    		if (request.getParameterMap().containsKey("timestamp")) 
		        	{
		    			try
		    			{
		    			Long upToDate = Long.decode(request.getParameter("timestamp"));
		    			if(upToDate > 9999999999L)	//timestamp obviously given in ms, but need s for RCT (10 digits for s, 13 digits for ms)
		    			{
		    				upToDate = upToDate / 1000;
		    			}
			    			if(!( upToDate == null || upToDate == 0L))
			            	{
			    	    		Constants.magicNumberObjectMap.get(magicNumber).setDataReady(false);
			    				requestBytes = Helper.buildRequestByteArrayDataLogger(magicNumber, upToDate);
			    				sendRequestToInverter(requestBytes); //finally communicate with inverter
			            	}
			    			else
			    			{
			            		logger.error("<<<<< ERROR >>>>> RequestData::getRctData: Received a bad request. No timestamp given, but compulsory for array (long) requests. Magic number: " + magicNumber);
			            		responseCode = HttpServletResponse.SC_BAD_REQUEST;
			            		
			            		JsonObject responseContent = new JsonObject();
			            		responseContent.addProperty("status", "error");
			            		responseContent.addProperty("text", "Received bad request, no timestamp given for array (long) requests. Magic number: " + magicNumber);
			            		content = responseContent.toString();
			    			}
		    			}
		    			catch(NumberFormatException e)
		    			{
		            		logger.error("<<<<< ERROR >>>>> RequestData::getRctData: Received a bad request. Timestamp {} is not a number. Magic number: {}." , request.getParameter("timestamp"), magicNumber);
		            		responseCode = HttpServletResponse.SC_BAD_REQUEST;
		            		
		            		JsonObject responseContent = new JsonObject();
		            		responseContent.addProperty("status", "error");
		            		responseContent.addProperty("text", "Received a bad request. Value for timestamp is not a number.");
		            		content = responseContent.toString();
		    			}

		        	}
		    		else
		    		{
			    		logger.error("<<<<< WARN >>>>> RequestData::getRctData: Received a bad request. Request for long data needs to have a timestamp, no timestamp given. Magic number: {}", magicNumber);
			    		responseCode = HttpServletResponse.SC_BAD_REQUEST;
			    		
			    		JsonObject responseContent = new JsonObject();
			    		responseContent.addProperty("status", "error");
			    		responseContent.addProperty("text", "Received bad request, request for long data needs to have a timestamp, no timestamp given. Magic number: " + magicNumber);
			    		content = responseContent.toString();
		    		}
			    }
		    	else if(Arrays.asList(Constants.unknownMagicNumbers).contains(magicNumber))
		        {
		    		logger.warn("<<<<< WARN >>>>> RequestData::getRctData: Received a bad request. Magic number in list of unknown magic numbers.");
		    		responseCode = HttpServletResponse.SC_BAD_REQUEST;
		    		
		    		JsonObject responseContent = new JsonObject();
		    		responseContent.addProperty("status", "error");
		    		responseContent.addProperty("text", "Received bad request, unknown magic number requested: " + magicNumber);
		    		content = responseContent.toString();
		        }
		        else if(Constants.keyDescriptionMapString.containsKey(magicNumber))
		        {
		    		logger.warn("<<<<< WARN >>>>> RequestData::getRctData: Received a request for string data. Not yet implemented.");
		    		responseCode = HttpServletResponse.SC_BAD_REQUEST;
		    		
		    		JsonObject responseContent = new JsonObject();
		    		responseContent.addProperty("status", "error");
		    		responseContent.addProperty("text", "Received a request for string data. Not yet implemented. Magic number " + magicNumber);
		    		content = responseContent.toString();
		        }
		        else
		        {
		    		logger.warn("<<<<< WARN >>>>> RequestData::getRctData: Received a bad request. Unknown magic number in request specified.");
		    		responseCode = HttpServletResponse.SC_BAD_REQUEST;
		    		
		    		JsonObject responseContent = new JsonObject();
		    		responseContent.addProperty("status", "error");
		    		responseContent.addProperty("text", "Received bad request, unknown magic number requested: " + magicNumber);
		    		content = responseContent.toString();
		        }
        	}
    		else
    		{
        		logger.error("<<<<< ERROR >>>>> RequestData::getRctData: Received a bad request. Empty magic number in request specified.");
        		responseCode = HttpServletResponse.SC_BAD_REQUEST;
        		
        		JsonObject responseContent = new JsonObject();
        		responseContent.addProperty("status", "error");
        		responseContent.addProperty("text", "Received bad request, empty magic number set.");
        		content = responseContent.toString();
    		}
    	}
    	else
    	{
    		logger.error("<<<<< ERROR >>>>> RequestData::getRctData: Received a bad request. No magic number in request specified.");
    		responseCode = HttpServletResponse.SC_BAD_REQUEST;
    		
    		JsonObject responseContent = new JsonObject();
    		responseContent.addProperty("status", "error");
    		responseContent.addProperty("text", "Received bad request, no magic number set.");
    		content = responseContent.toString();
    	}
    	//preparing response
    	response.setHeader("content-Type", "application/json");
    	response.setStatus(responseCode);
    	
    	return content;
    }
    

    
	private void sendRequestToInverter(byte[] inputByteArray)
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
				logger.warn("<<<<< DEBUG >>>>> RequestData::getRctData timeout " + (Calendar.getInstance().getTimeInMillis() - startWaitTime));
			}	
		}
		
		if(requestTimeOut)
		{
	
			responseCode = HttpServletResponse.SC_GATEWAY_TIMEOUT;
			JsonObject responseContent = new JsonObject();
			responseContent.addProperty("type", "error");
			responseContent.addProperty("text", "Inverter didn't respond on time.");
			content = responseContent.toString();
		}
		else
		{	
			content = Constants.magicNumberObjectMap.get(magicNumber).getDataJson().toString();
			responseCode = HttpServletResponse.SC_OK;
			logger.debug("<<<<< DEBUG >>>>> RequestData::getRctData Returning data to client RC200."); 
		}
	}

}