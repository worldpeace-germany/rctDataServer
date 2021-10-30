package com.opensource.rct.application;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public class Inverter 
{
	private static Logger logger = LogManager.getLogger();
	private Socket inverterSocket;
	private Thread tcpConnectionThread = null;
	private JsonObject dataStorage;
	int requestedDataArrayPosition;
	
	public Inverter()
	{
		Configurator.setLevel("com.opensource.rct", Level.toLevel(Constants.logLevel));
	}

	void connect()
	{
		try 
		{
			inverterSocket = new Socket(Constants.hostname, Constants.port);
			logger.info("<<<<< INFO >>>>> Inverter::connect Connected successfully to inverter.");
			
			tcpConnectionThread = new Thread(
					new Runnable()
					{
						public void run()
						{
							int bufferSize = 4000;
							while (!Thread.interrupted()) 
							{
								byte[] bBuffer = new byte[bufferSize];	//TODO: Check if 4000 is enough or if we can use a dynamic size
						        try 
						        {
									int size = inverterSocket.getInputStream().read(bBuffer);
									
									logger.debug("<<<<< DEBUG >>>>> Inverter::tcpConnectionThread Receiving data on TCP socket. Size received: " + size);
									if(size == bufferSize)
									{
										logger.error("<<<<< ERROR >>>>> Inverter::tcpConnectionThread Buffer size is too small.");
									}

									if(size != -1)
									{
										
										byte[] bBufferFinal = new byte[size];
										for (int j = 0; j < bBufferFinal.length; j++) 
										{
											bBufferFinal[j] = bBuffer[j];
										}
										
										dataStorage = new JsonObject(); //initialize the JSON which returns the data later
										
										String finalString = Helper.byteArray2String(bBufferFinal);
										
										logger.debug("<<<<< DEBUG >>>>> Inverter::tcpConnectionThread raw string received: " + finalString);
										
										//Cut stream into possible messages (frames)
										List<String> resultStrings2 = split2B(finalString);	//split input string according to spec at 2B unless preceeded by 2D as escape sign
										
										//clean the string array, only keep entries which seem to be
										if(resultStrings2 != null && resultStrings2.size() > 0)
										{
											List<String> resultStrings3 = cleanResultArray(resultStrings2);
											
											for (int j = 0; j < resultStrings3.size(); j++) 
											{
												processResultString(resultStrings3.get(j));
											}
										}
									}
									else
									{
										logger.debug("<<<<< DEBUG >>>>> Inverter::tcpConnectionThread Network stream to inverter was closed.");
										inverterSocket = new Socket(Constants.hostname, Constants.port);
									}
									
								} catch (IOException e) {
									logger.error("<<<<< ERROR >>>>> Inverter::tcpConnectionThread IO Exception when connecting to inverter.");
									e.printStackTrace();
								}
							}
							logger.info("<<<<< INFO >>>>> Inverter::tcpConnectionThread Thread connecting to inverter has been stopped.");
						}
					});
			tcpConnectionThread.setName("TCPConnectionThreadRctInverter");
			tcpConnectionThread.start();
			
		} 
		catch (IOException e) 
		{
			logger.error("<<<<< ERROR >>>>> Inverter::connect Cannot connect to inverter. Check hostname " + Constants.hostname + " and port. " + Constants.port);
			//TODO: implement reconnect if connection fails
		}
	}
	
	void disconnect()
	{
		tcpConnectionThread.interrupt();
	}
	

	
	public byte[] buildRequestByteArray(String magicNumber)
	{
		String requestString =  "0104" + magicNumber; //01 = read request, 04 = length of data (=magic number)
		
		String crc = Helper.calcCRC(requestString);
		
		requestString = requestString.replaceAll("2D", "2D2D"); //escape 2B and 2D with a proceeding 2D for escaping
		requestString = requestString.replaceAll("2B", "2D2B"); //escape 2B and 2D with a proceeding 2D for escaping
		
		requestString = "2B" + requestString + crc; //2B = start frame
		
		int lenghtByteArray = requestString.length() / 2;	//half the string length is the number of bytes
		
		byte[] requestBytes = new byte[lenghtByteArray];
		
		for (int i = 0; i < requestString.length()/2; i++) 
		{	
			Integer v = ((Integer.parseInt(requestString.substring(2*i, 2*i+2), 16)) & 0xFF); //Bytes in Java are signed so do & 0xFF for correct conversion
			requestBytes[i] = v.byteValue();
		}
		
		return requestBytes;
	}
	
	/**
	 * Method which takes a raw byte input stream (converted to a string) and splits it into different messages (=frames) according to RCT spec.
	 * 
	 * Start of stream character is 2B, this will be removed. If string doesn't start with 2B then null will be returned. The input string
	 * will be split at every 2B unless preceded by a 2D (=escape character).
	 * 
	 * @param inputString, this is the raw input string received from the TCP/IP stream. The byte stream needs to be converted to string first.
	 * @return an array of Strings, each string potentially represents a set of data. Further filtering is still necessary.
	 */
	public List<String> split2B(String inputString)
	{
		int splitStartPosition = 0;
		List<String> resultList = new ArrayList<>();
		
		if(inputString.length() < 4)	//length must be even higher than 4 to have some useful content, just make sure that substring is not running into out of bounds
		{
			logger.debug("<<<<< DEBUG >>>>> Inverter::split2B Received TCP stream not valid, it is too short: " + inputString);
			return null;
		}

		//Stream should always start with 2B, sometimes for unknown reasons it starts with 002B. Consider the rest as garbage.
		if ((inputString.substring(0, 2).equalsIgnoreCase("2B")))	
		{
			inputString = inputString.substring(2, inputString.length()); // remove the first start trigger 2B before splitting
		}	
		else if((inputString.substring(0, 4).equalsIgnoreCase("002B"))) //It seems that newer revisions of the inverter stream starts with 002B
		{
			inputString = inputString.substring(4, inputString.length()); // remove the first start trigger 002B before splitting
		}
		else
		{
			logger.debug("<<<<< DEBUG >>>>> Inverter::split2B Received TCP stream not valid, it does not start with 2B nor 002B. " + inputString);
			return null;
		}

	
		for (int i = 0; i < inputString.length(); i = i+2) 
		{
			if(Character.compare(inputString.charAt(i),'2') == 0)
			{
				if(Character.compare(inputString.charAt(i+1),'B') == 0 || Character.compare(inputString.charAt(i+1),'b') == 0)
				{
					//found a split 2B at the right position, make sure that the preceding byte is not 2D as this would be an escape signal
					if(i>1)	//If the first byte is 2B, then it cannot be an escaped character
					{
						if(Character.compare(inputString.charAt(i-2),'2') == 0 && (Character.compare(inputString.charAt(i-1),'D') == 0 || Character.compare(inputString.charAt(i-1),'D') == 0))
						{
								//found an escape, don't split but remove the 2D from the string
						}
						else
						{
							//split here and remove 2B
							resultList.add(inputString.substring(splitStartPosition, i));
							splitStartPosition = i+2;
						}
					}
				}
			}
		}
		resultList.add(inputString.substring(splitStartPosition, inputString.length()));
		
		logger.debug("<<<<< DEBUG >>>>> Inverter::split2B raw string split into substrings, amount of substrings " + resultList.size());
		
		return resultList;
	}
	
	
	/**
	 * The list array with raw potential messages (=frames) is being cleaned and garbage is being removed.
	 * 
	 * - Messages which have a size < 8 (=4 bytes) will be removed, because the key for each message is already 4 bytes.
	 * - Message must start with byte "05" (= short response), or "06" (=long response) we only want to have responses
	 * - Mentioned data length must be as large as the received response bytes. Sometimes too few bytes are received
	 * 
	 * 
	 * The following response codes exist:
	 * 
	 * READ 		0x01
	 * WRITE 		0x02
	 * LONG WRITE 	0x03
	 * reserved 	0x04
	 * RESPONSE 	0x05
	 * LONG RESPONSE 0x06
	 * EXTENSION 	0x3C
	 * 
	 * @return a List<String> containing only entries which represents valid responses
	 */
	public List<String> cleanResultArray(List<String> inputArray)
	{
		//clean the string array, only keep entries which
		//start with 05
		for (int j = 0; j < inputArray.size(); j++) 
		{
			//System.out.println("self made " + j + " "+ inputArray.get(j));
			if(inputArray.get(j).length() < 8)	
			{
				logger.debug("<<<<< DEBUG >>>>> Inverter::cleanResultArray string in array too small (smaller 8) string removed: " + inputArray.get(j));
				//need minimum of 4 bytes (=string length 8), sometimes receive 2B2B then the split is empty
				inputArray.remove(j);
				j--;	//next entry slips one forward so ensure not to miss that
				
			}
			else if(!inputArray.get(j).substring(0, 2).equalsIgnoreCase("05") && !inputArray.get(j).substring(0, 2).equalsIgnoreCase("06"))	
			{
				logger.debug("<<<<< DEBUG >>>>> Inverter::cleanResultArray string in array not of type 05 or type 06: " + inputArray.get(j));
				//05 and 06 are codes for a response. If array entry doesn't represent a response then remove it from the list
				inputArray.remove(j);
				j--;	//next entry slips one forward so ensure not to miss that
			}
			else
			{
				/*
				 * Check for escaped characters and remove escape sign (=2D)
				 * */
				if (inputArray.get(j).contains("2D2B") || inputArray.get(j).contains("2d2b"))
				{
					String valueWithoutEscapes = inputArray.get(j).replaceAll("2D2B", "2B");
					valueWithoutEscapes = valueWithoutEscapes.replaceAll("2d2b", "2b");
					inputArray.set(j, valueWithoutEscapes);
				}
				if(inputArray.get(j).contains("2D2D") || inputArray.get(j).contains("2d2d"))
				{
					String valueWithoutEscapes = inputArray.get(j).replaceAll("2D2D", "2D");
					valueWithoutEscapes = valueWithoutEscapes.replaceAll("2d2d", "2d");
					inputArray.set(j, valueWithoutEscapes);
				}
				
				/* Check that the mentioned length in string is not shorter or longer than the length of the string. 
				 * A frame has the following structure
				 * 1 start byte (2B) already removed at this point in time
				 * 1 command byte, 05 for short read or 06 for long read
				 * 1 byte for the length (2 bytes for long read) value: 4+n, 4 bytes for the command, n bytes of data
				 * 4 bytes for the command
				 * n bytes of data
				 * 2 bytes CRC
				 * 
				 * Be aware that the string contains characters and two characters are one byte. Hence there is a factor
				 * of 2 between string size and byte size.
				 * 
				 * */
				
				int dataLength = 0;
				int stringLength = 0;
				int bytesForLength = 2;
				
				if(inputArray.get(j).substring(0, 2).equalsIgnoreCase("05")) //short response, only one byte for length of frame
				{
					dataLength = Integer.valueOf(inputArray.get(j).substring(2, 4),16); //stringLength in characters
					stringLength = inputArray.get(j).length();
				}
				else if (inputArray.get(j).substring(0, 2).equalsIgnoreCase("06")) //long response, two bytes for length of frame
				{
					dataLength = Integer.valueOf(inputArray.get(j).substring(2, 6),16); //stringLength specified in stream
					stringLength = inputArray.get(j).length();			  //length of the received string
					bytesForLength = 4;
				}
				
				if(stringLength != 2*dataLength + bytesForLength + 2 + 4)	//dataLength (in bytes!) includes data and command key, so add 2 for the length byte and 4 for CRC
				{
					inputArray.remove(j);
					j--;	//next entry slips one forward so ensure not to miss that
					logger.debug("<<<<< DEBUG >>>>> Inverter::cleanResultArray Length of data frame doesn't fit to expected length. Ignoring frame.");
				}
				else
				{
					/*
					 * Finally do a CRC check to see that transmission was correct
					 * */
					
					String calculatedCRC = Helper.calcCRC(inputArray.get(j).substring(0, inputArray.get(j).length() - 4));
					String transmittedCRC = inputArray.get(j).substring( inputArray.get(j).length() - 4, inputArray.get(j).length()) ;
					if(!calculatedCRC.equalsIgnoreCase(transmittedCRC))
					{
						inputArray.remove(j);
						j--;	//next entry slips one forward so ensure not to miss that
						logger.debug("<<<<< DEBUG >>>>> Inverter::cleanResultArray CRC error. Ignoring frame.");
					}
				}
			}
		}
		
		logger.debug("<<<<< DEBUG >>>>> Inverter::cleanResultArray array to be further processed is of size " + inputArray.size());
		
		return inputArray;
	}
	
	void processResultString(String inputString)
	{
		try
		{
			//check if short (05) or long (06) response
			String responseType = inputString.substring(0,2);
			
			String key = "";
			int dataLength = -1;
			String valueString = "";
			
			if(responseType.equalsIgnoreCase("05"))
			{
				key = inputString.substring(4, 12);	//key is 4 bytes, thus 8 characters in String
				dataLength = Integer.valueOf(inputString.substring(2, 4),16) - 4;	//substract 4 the key is included in the length value
				valueString = inputString.substring(12, 12+2*dataLength);	//2*datalength since length is given in bytes and we need characters here
			}
			else if (responseType.equalsIgnoreCase("06"))
			{
				key = inputString.substring(6, 14);	//key is 4 bytes, thus 8 characters in String
				dataLength = Integer.valueOf(inputString.substring(2, 6),16) - 4;	//substract 4 the key is included in the length value
				valueString = inputString.substring(14, 14+2*dataLength);	//for long response the data starts 2 bytes later in the frame since the lenght field is 2 bytes. 2*datalength since length is given in bytes and we need characters here
			}
			else
			{
				logger.error("<<<<< ERROR >>>>> Inverter::processResultString Non existing response type, this shouldn't happen. Received type " + responseType);
			}
			
			if(Constants.magicNumberObjectMap.containsKey(key))
			{
				logger.debug("<<<<< DEBUG >>>>> Inverter::processResultString key listed: " + key);
				if(Constants.magicNumberObjectMap.get(key).getDataType().equalsIgnoreCase("short"))
				{
					Long i = Long.parseLong(valueString, 16);
			        Float valueFloat = Float.intBitsToFloat(i.intValue());
		        	dataStorage.addProperty("type", "short");
		        	dataStorage.addProperty("magicNumber", key);
		        	dataStorage.addProperty("value", valueFloat);
		        	dataStorage.addProperty("text", Constants.magicNumberObjectMap.get(key).getDescription());
		        	dataStorage.addProperty("timestamp", Calendar.getInstance().getTimeInMillis() / 1000);
		        	
		        	boolean validValue = runSanityCheck(key, valueFloat);
		        	
		        	dataStorage.addProperty("sucess", validValue);
		        	
			        Constants.magicNumberObjectMap.get(key).setDataJson(dataStorage);
			        Constants.magicNumberObjectMap.get(key).setDataReady(true);
				}
				else if(Constants.magicNumberObjectMap.get(key).getDataType().equalsIgnoreCase("long"))
				{
		        	JsonArray dataArray = new JsonArray();
		        	
		        	valueString = valueString.substring(8, valueString.length()); //The first 4 bytes are the requsted timestamp, not needed for the response
		        	
		        	int numberOfEntries = valueString.length() / 16;
		        	
		        	for(int i = 0; i < numberOfEntries; i++) 
		        	{
		        		String elementTimestamp = valueString.substring(16*i, 16*i+8); //timestamp given in seconds but need ms
		        		String elementValue = valueString.substring(16*i+8, 16*i+16); 
		        		long asLong = Long.parseLong(elementValue, 16);
		        		int asInt = (int) asLong;
		        		
		        		Long currentDateLong = (Long.parseLong(elementTimestamp, 16)) * 1000;	//timestamp in seconds, but need to return in ms
		        		Double valueFloat = Math.round(Float.intBitsToFloat(asInt) * 100.0) / 100.0;	//integer to float and rouding to 2 digits
				        JsonObject dataEntry = new JsonObject();
				        dataEntry.addProperty("timestamp", currentDateLong);
				        dataEntry.addProperty("value", valueFloat);
				        
				        dataArray.add(dataEntry); 
		        	}
		        	
		        	dataStorage.add("valueArray", dataArray);
		        	dataStorage.addProperty("magicNumber", key);
		        	dataStorage.addProperty("type", "long");
		        	dataStorage.addProperty("text", Constants.magicNumberObjectMap.get(key).getDescription());
			        Constants.magicNumberObjectMap.get(key).setDataJson(dataStorage);
			        Constants.magicNumberObjectMap.get(key).setDataReady(true);
				}
				else if(Constants.magicNumberObjectMap.get(key).getDataType().equalsIgnoreCase("unknown"))	//to cover the case that we know that a magic number exists but we don't know its meaning
				{
		        	logger.debug("<<<<< DEBUG >>>>> Inverter::processResultString string contains unknown magic number: " + key);
		        	dataStorage.addProperty("magicNumber", key);
		        	dataStorage.addProperty("type", "unknown");
			        Constants.magicNumberObjectMap.get(key).setDataJson(dataStorage);
			        Constants.magicNumberObjectMap.get(key).setDataReady(true);
				}
				else if(Constants.magicNumberObjectMap.get(key).getDataType().equalsIgnoreCase("string"))
				{
		        	logger.debug("<<<<< DEBUG >>>>> Inverter::processResultString string contains magic number representing a string value not a number: " + key);
		        	dataStorage.addProperty("magicNumber", key);
		        	dataStorage.addProperty("type", "String");
		        	dataStorage.addProperty("text", Constants.magicNumberObjectMap.get(key).getDescription());
			        Constants.magicNumberObjectMap.get(key).setDataJson(dataStorage);
			        Constants.magicNumberObjectMap.get(key).setDataReady(true);
		        	//don't do anything, we know that this value is a string //TODO: Implement later
				}
			}
			else
			{
				logger.warn("<<<<< DEBUG >>>>> Inverter::processResultString string contains unknown magic number (not listed in CSV): " + key);
	        	//magic number is unknown so I cannot set any data in the JSON which contains the results since it is not contained in Constants.magicNumberObjectMap
			}	

	        logger.debug("<<<<< DEBUG >>>>> Inverter::processResultString string is valid, json produced is: " + dataStorage);
		}
		catch(IndexOutOfBoundsException e)
		{
			logger.error("<<<<< ERROR >>>>> Inverter::processResultString Index out of bounds for input " + inputString + " Exception: " + e.getLocalizedMessage());
		}
		catch(NumberFormatException e)
		{
			logger.error("<<<<< ERROR >>>>> Inverter::processResultString Cannot convert string containing the data length to a number.");
		}
	}
	
	//sometime panels show higher wattage than theoretical max values, check for these to be able to filter them out
	boolean runSanityCheck(String key, Float value)
	{
		if(key.equalsIgnoreCase("DB11855B") && Constants.panelsA != 0 && Constants.panelPower != 0)	//DC input power panel A configured for sanity check
		{
			if(value > Constants.panelPower * Constants.panelsA * 1.2)	// value 20% above the theoretical value, probably wrong response from inverter
			{
				logger.error("<<<<< ERROR >>>>> Inverter::runSanityCheck Input power panel A is too high, value {} not possible based on configuration.", value);
				return false;
			}
		}
		if(key.equalsIgnoreCase("0CB5D21B") && Constants.panelsB != 0 && Constants.panelPower != 0)	//DC input power panel B configured for sanity check
		{
			if(value > Constants.panelPower * Constants.panelsB * 1.2)	// value 20% above the theoretical value, probably wrong response from inverter
			{
				logger.error("<<<<< ERROR >>>>> Inverter::runSanityCheck Input power panel B is too high, value {} not possible based on configuration.", value);
				return false;
			}
		}
		return true;
	}
}
