package com.opensource.rct.application;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Helper 
{
	
	private static Logger logger = LogManager.getLogger();

	public static void outputByteArray(byte[] barray)
	{
		for (int i = 0; i < barray.length; i++)
		{
			 String hexString = Integer.toHexString( (int) (barray[i] & 0xff)).toUpperCase();
			 
			 if(hexString.length()==1) hexString = "0" + hexString;
			 System.out.print(hexString + " ");
			 
			if((i+1) %4 == 0 )
			{
				System.out.println();
			}
		}
	}
	
	public static String byteArray2String(byte[] barray)
	{
		String finalString = "";
		for (int i = 0; i < barray.length; i++)
		{
			 String hexString = Integer.toHexString( (int) (barray[i] & 0xff)).toUpperCase();
			 
			 if(hexString.length()==1) hexString = "0" + hexString;
			 
			 finalString = finalString + hexString;
		}
		
		return finalString;
	}
	
	/**
	 * Calculation of CRC.  needs to have an even byte length if not add 0x00
	 * @param inputString Frame to be calculated, must not contain start byte 2B,
	 * @return 
	 */
	public static String calcCRC(String inputString)
	{		
		int crc = 0xFFFF;	//seed for CRC calculation
		
		int rest = inputString.length() % 4;	
		if (rest!=0)	//have an odd number of bytes, need to pad with 0x00 at the end
		{
			inputString = inputString + "00";
		}
						
		for(int j = 0; j < inputString.length(); j=j+2)
		{
			String character = inputString.substring(j, j+2);
			int b = Integer.valueOf(character,16);
			for (int i = 0; i < 8; i++) 
			{
				boolean bit = ((b >> (7 - i) & 1) == 1);
				boolean c15 = (((crc >> 15) & 1) == 1);
				crc <<= 1;
				if (c15 ^ bit)
				{
					crc ^= 0x1021;	//CRC-CCITT / CRC-16:  0x1021      =  x16 + x12 + x5 + 1
				}
			}
			crc &= 0xFFFF;
		
		}
		return String.format("%04X", crc);
		
	}
	
	public static byte[] buildRequestByteArrayDataLogger(String magicNumber, Long timestamp)
	{
		String requestString =  "0208" + magicNumber + Long.toHexString(timestamp); 
		//02 = write request, 08 = length of data (=magic number + timestamp), request is of type write since we write a timestamp. 01 read request is only to produce short responses.
		String crc = Helper.calcCRC(requestString);
		
		requestString = requestString.replaceAll("2D", "2D2D"); //escape 2B and 2D with a proceeding 2D for escaping
		requestString = requestString.replaceAll("2B", "2D2B"); //escape 2B and 2D with a proceeding 2D for escaping
		
		requestString = "2B" + requestString + crc; //2B = start frame
		
		logger.debug("<<<<< DEBUG >>>>> RequestData::buildRequestByteArrayDataLogger request string is: " + requestString);
			
		int lenghtByteArray = requestString.length() / 2;	//half the string length is the number of bytes
		
		byte[] requestBytes = new byte[lenghtByteArray];
		
		for (int i = 0; i < requestString.length()/2; i++) 
		{	
			Integer v = ((Integer.parseInt(requestString.substring(2*i, 2*i+2), 16)) & 0xFF); //Bytes in Java are signed so do & 0xFF for correct conversion
			requestBytes[i] = v.byteValue();
		}
		
		return requestBytes;
	}
	
	public static byte[] buildRequestByteArrayDataLoggerShort(String magicNumber)
	{
		String requestString =  "0104" + magicNumber; 
		//01 = read request, 04 = length of data (=length of magic number), type 01 read request is only to produce short responses.
		String crc = Helper.calcCRC(requestString);
		
		requestString = requestString.replaceAll("2D", "2D2D"); //escape 2B and 2D with a proceeding 2D for escaping
		requestString = requestString.replaceAll("2B", "2D2B"); //escape 2B and 2D with a proceeding 2D for escaping //TODO: Does escaping have an effect on length 04?
		
		requestString = "2B" + requestString + crc; //2B = start frame
		
		logger.debug("<<<<< DEBUG >>>>> RequestData::buildRequestByteArrayDataLoggerShort request string is: " + requestString);
			
		int lenghtByteArray = requestString.length() / 2;	//half the string length is the number of bytes
		
		byte[] requestBytes = new byte[lenghtByteArray];
		
		for (int i = 0; i < requestString.length()/2; i++) 
		{	
			Integer v = ((Integer.parseInt(requestString.substring(2*i, 2*i+2), 16)) & 0xFF); //Bytes in Java are signed so do & 0xFF for correct conversion
			requestBytes[i] = v.byteValue();
		}
		
		return requestBytes;
	}
	
	
}
