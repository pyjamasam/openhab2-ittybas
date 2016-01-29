/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelabs.handler;

import static org.openhab.binding.jeelabs.JeeLabsBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.openhab.binding.jeelabs.internal.connector.JeeLinkConnectorInterface;
import org.openhab.binding.jeelabs.internal.connector.JeeLinkSerialConnector;
import org.openhab.binding.jeelabs.internal.connector.JeeLinkTCPConnector;
import org.openhab.binding.jeelabs.internal.JeeLinkMessage;
import org.openhab.binding.jeelabs.internal.JeeNodeReading;
import org.openhab.binding.jeelabs.internal.JeeNodeDataListener;

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
 * The {@link JeeLinkHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Chris Whiteford - Initial contribution
 */
public class JeeLinkHandler extends BaseBridgeHandler {

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
	private Logger logger = LoggerFactory.getLogger(JeeLinkHandler.class);

	private List<JeeNodeDataListener> _dataListeners = new CopyOnWriteArrayList<>();

	private JeeLinkConnectorInterface _connector = null;
	private DataProcessor _dataProcessor = null;

	private class DataProcessor extends Thread  {
		private Logger logger = LoggerFactory.getLogger(DataProcessor.class);

		JeeLinkHandler _handler = null;
		DataProcessor(JeeLinkHandler handler) {
			_handler = handler;
		}

		@Override
		public void interrupt() {
			super.interrupt();

			logger.debug("interrupting...");
		}

		public void run() {
			logger.debug("DataProcessor started");
			try {
				while (true)	//Do this forever
				{
					JeeLinkMessage message = (JeeLinkMessage)_connector.messageQueue().take();

					String hexString = bytesToHex(message.data());
					//logger.debug("Hex Bytes: {}", hexString);

					//Lets convert this message data to a reading (this does the parsing)
					JeeNodeReading reading = new JeeNodeReading(message);
					//logger.debug("Reading is for node: {}, valuetype: {}", reading.nodeIdentifier(), reading.valueType());

					for (JeeNodeDataListener dataListener : _dataListeners) {
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
			logger.debug("DataProcessor done");
		}
	}

	public JeeLinkHandler(Bridge bridge) {
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
				_connector = new JeeLinkSerialConnector();
			}

			if (_connector != null) {
				_connector.disconnect();
				try {
					_connector.connect((String) getConfig().get(SERIAL_PORT));

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
				updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Connection == null");
			}
		}
		else if (getConfig().get(IP_ADDRESS) != null)
		{
			updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "TCP connection not yet supported.");
		}
		else
		{
			updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Cannot connect to jeelink. serialPort or ipAddress is not set.");
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
			_connector.disconnect();
			_connector = null;
		}
	}

	public Boolean registerNode(JeeNodeDataListener dataListener) {
		if (dataListener == null) {
			throw new NullPointerException("It's not allowed to pass a null dataListener.");
		}
		return _dataListeners.add(dataListener);
	}

	public Boolean unregisterNode(JeeNodeDataListener dataListener) {
		 return _dataListeners.remove(dataListener);
	}
}
