package com.opensource.rct.servlet;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.opensource.rct.application.Constants;
import com.opensource.rct.model.MagicNumber;


@RestController
public class RequestAllData{

	private static Logger logger = LogManager.getLogger();
	private int responseCode;
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
	
    @RequestMapping(value="/getAllData", produces={"application/json"})
    public String getRctData(HttpServletRequest request, HttpServletResponse response)
    {
    	JsonArray jsonArray = new JsonArray(); 
    	
    	for (HashMap.Entry<String, MagicNumber> entry : Constants.magicNumberObjectMap.entrySet()) 
    	{     		
    		if(entry.getValue().getDataJson() != null)
    		{
    			jsonArray.add(entry.getValue().getDataJson());
    		}
    	}
    	
    	JsonObject returnData = new JsonObject();
    	returnData.add("result", jsonArray);
    	     
    	responseCode = HttpServletResponse.SC_OK;
    	
    	response.setContentType("application/json");
      	response.setHeader("content-Type", "application/json");
    	response.setStatus(responseCode);
    	
    	return returnData.toString();
    }
}