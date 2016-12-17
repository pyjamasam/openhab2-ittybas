/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ittybas.handler;

import static org.openhab.binding.ittybas.ittyBASBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.openhab.binding.ittybas.internal.connector.ittyBASConnectorInterface;
import org.openhab.binding.ittybas.internal.connector.ittyBASSerialConnector;
import org.openhab.binding.ittybas.internal.connector.ittyBASNetworkConnector;
import org.openhab.binding.ittybas.internal.ittyBASMessage;
import org.openhab.binding.ittybas.internal.ittyBASReading;
import org.openhab.binding.ittybas.internal.ittyBASDataListener;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ittyBASLinkHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Chris Whiteford - Initial contribution
 */
public class ittyBASLinkHandler extends BaseBridgeHandler {

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}

	public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_JEELINK);
	private Logger logger = LoggerFactory.getLogger(ittyBASLinkHandler.class);

	private List<ittyBASDataListener> _dataListeners = new CopyOnWriteArrayList<>();

	private ittyBASConnectorInterface _connector = null;
	private DataProcessor _dataProcessor = null;

	private class DataProcessor extends Thread  {
		private Logger logger = LoggerFactory.getLogger(DataProcessor.class);

		ittyBASLinkHandler _handler = null;
		DataProcessor(ittyBASLinkHandler handler) {
			_handler = handler;
		}

		@Override
		public void interrupt() {
			super.interrupt();

			logger.debug("interrupting...");
		}

		public void run() {
			logger.debug("DataProcessor for " + _handler.getClass().getSimpleName() + " started");
			try {
				while (true)	//Do this forever
				{
					ittyBASMessage message = (ittyBASMessage)_connector.messageQueue().take();

					String hexString = bytesToHex(message.data());
					//logger.debug("Hex Bytes: {}", hexString);

					//Lets convert this message data to a reading (this does the parsing)
					ittyBASReading reading = new ittyBASReading(message);
					//logger.debug("Reading is for node: {}, valuetype: {}", reading.nodeIdentifier(), reading.valueType());

					for (ittyBASDataListener dataListener : _dataListeners) {
						try {
							dataListener.dataUpdate(reading);
						} catch (Exception e) {
							logger.error(
									"An exception occurred while calling dataUpdate",
									e);
						}
					}
				}
			} catch (InterruptedException e) {
				logger.debug("interrupted");
			}
			logger.debug("DataProcessor for " + _handler.getClass().getSimpleName() + " done");
		}
	}

	public ittyBASLinkHandler(Bridge bridge) {
		super(bridge);

		_dataProcessor = new DataProcessor(this);
	}

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
	}

	@Override
	public void initialize() {
		if (getConfig().get(SERIAL_PORT) != null) {
			if (_connector == null) {
				_connector = new ittyBASSerialConnector();
			}

			if (_connector != null) {
				_connector.shutdown();
				try {
					_connector.setup((String) getConfig().get(SERIAL_PORT));

					_dataProcessor.start();

					updateStatus(ThingStatus.ONLINE);
				} catch (Exception e) {
					logger.error("Exception trying to connect: {}", e);
					updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Error Connecting");
				} catch (UnsatisfiedLinkError e) {
					logger.error(
							"Error occured when trying to load native library for OS '{}' version '{}', processor '{}'",
							System.getProperty("os.name"),
							System.getProperty("os.version"),
							System.getProperty("os.arch"), e);
				}
			}
			else
			{
				updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Connection == null after trying to create it");
			}
		}
		else if (getConfig().get(LISTEN_PORT) != null)
		{
			if (_connector == null) {
				_connector = new ittyBASNetworkConnector();
			}

			if (_connector != null) {
				_connector.shutdown();
				try {
					_connector.setup((String) getConfig().get(LISTEN_PORT));

					_dataProcessor.start();

					updateStatus(ThingStatus.ONLINE);
				} catch (Exception e) {
					logger.error("Exception trying to connect: {}", e);
					updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Error Connecting");
				} catch (UnsatisfiedLinkError e) {
					logger.error(
							"Error occured when trying to load native library for OS '{}' version '{}', processor '{}'",
							System.getProperty("os.name"),
							System.getProperty("os.version"),
							System.getProperty("os.arch"), e);
				}
			}
			else
			{
				updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Connection == null after trying to create it");
			}
		}
		else
		{
			updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Cannot connect to jeelink. serialPort or listenPort is not set.");
		}
	}

	@Override
	public void dispose() {
		logger.debug("disposed");

		if (_dataProcessor != null)
		{
			_dataProcessor.interrupt();
			try
			{
				_dataProcessor.join();
			} catch (InterruptedException e) {
			}

			_dataProcessor = null;
		}

		if (_connector != null) {
			_connector.shutdown();
			_connector = null;
		}
	}

	public Boolean registerNode(ittyBASDataListener dataListener) {
		if (dataListener == null) {
			throw new NullPointerException("It's not allowed to pass a null dataListener.");
		}
		return _dataListeners.add(dataListener);
	}

	public Boolean unregisterNode(ittyBASDataListener dataListener) {
		 return _dataListeners.remove(dataListener);
	}
}
