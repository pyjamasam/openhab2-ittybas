/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.ittybas.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openhab.binding.ittybas.handler.ittyBASLinkHandler;
import org.openhab.binding.ittybas.internal.ittyBASMessage;

public class ittyBASDataProcessor extends Thread  
{
	private Logger logger = LoggerFactory.getLogger(ittyBASDataProcessor.class);

	org.openhab.binding.ittybas.handler.ittyBASLinkHandler _handler = null;

	public ittyBASDataProcessor(ittyBASLinkHandler handler) 
	{
		_handler = handler;
	}

	@Override public void interrupt() 
	{
		super.interrupt();
	}

	public void run() 
	{
		logger.debug("ittyBASDataProcessor for {} started", _handler);	
		try 
		{
			while (true)	//Do this forever
			{
				org.openhab.binding.ittybas.internal.ittyBASMessage message = (org.openhab.binding.ittybas.internal.ittyBASMessage)_handler.messageQueue().take();

				String hexString = bytesToHex(message.data());
				//logger.debug("Hex Bytes: {}", hexString);

				//Lets convert this message data to a reading (this does the parsing)
				ittyBASReading reading = new ittyBASReading(message);
				//logger.debug("Reading is for node: {}, valuetype: {}", reading.nodeIdentifier(), reading.valueType());

				for (ittyBASDataListener dataListener : _handler.dataListeners()) 
				{
					try 
					{
						dataListener.dataUpdate(reading);
					} 
					catch (Exception e) 
					{
						logger.error("An exception occurred while calling dataUpdate",e);
					}
				}
			}
		} 
		catch (InterruptedException e) 
		{			
		}

		logger.debug("ittyBASDataProcessor for {} stopped", _handler);	
	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) 
	{
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) 
		{
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}	
}