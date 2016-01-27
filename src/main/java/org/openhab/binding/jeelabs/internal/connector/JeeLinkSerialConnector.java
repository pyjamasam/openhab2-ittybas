/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

 //This code is based very heavly on the code for the rfxcom binding. I say heavly, but really its copy and paste
package org.openhab.binding.jeelabs.internal.connector;

import gnu.io.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TooManyListenersException;

import java.io.DataInputStream;

import javax.xml.bind.DatatypeConverter;

import org.openhab.binding.jeelabs.internal.connector.JeeLinkEventListener;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JeeLink connector for serial port communication.
 *
 * @author Chris Whiteford - Initial contribution
 */
public class JeeLinkSerialConnector implements JeeLinkConnectorInterface, SerialPortEventListener {

	private static final Logger logger = LoggerFactory.getLogger(JeeLinkSerialConnector.class);

	private static List<JeeLinkEventListener> _listeners = new ArrayList<JeeLinkEventListener>();

	SerialPort _serialPort = null;

	public JeeLinkSerialConnector() {
	}

	@Override
	public void connect(String device) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException {

        logger.debug("Connecting... ({})", device);

		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(device);
		CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

		_serialPort = (SerialPort) commPort;
		_serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8,SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		_serialPort.enableReceiveThreshold(1);
		_serialPort.disableReceiveTimeout();

		try {
			_serialPort.addEventListener(this);
		}
		catch (Exception e) {
			logger.error("Exception adding listener: {}", e);
		}
		_serialPort.notifyOnDataAvailable(true);
	}

	@Override
	public void serialEvent(SerialPortEvent arg0) {
		logger.debug("Event Type: {}", arg0.getEventType());

		switch (arg0.getEventType())
		{
			case SerialPortEvent.DATA_AVAILABLE:
			{
				byte[] readBuffer = new byte[40];

				try
				{
					while (_serialPort.getInputStream().available() > 0)
					{
						int numBytes = _serialPort.getInputStream().read(readBuffer);
						logger.debug("Read {} bytes", numBytes);
					}
				}
				catch (IOException e)
				{
					logger.debug("Exception reading {}", e);
				}
			}
		}
	}

	@Override
	public void disconnect() {
		logger.debug("Disconnecting...");

		if (_serialPort != null) {
			logger.debug("Close serial port");
			_serialPort.close();
		}
		_serialPort = null;
	}


	@Override
	public synchronized void addEventListener(JeeLinkEventListener listener) {
		if (!_listeners.contains(listener)) {
			_listeners.add(listener);
		}
	}

	@Override
	public synchronized void removeEventListener(JeeLinkEventListener listener) {
		_listeners.remove(listener);
	}

	private void _sendMsgToListeners(byte[] msg) {
		try {
			Iterator<JeeLinkEventListener> iterator = _listeners.iterator();

			while (iterator.hasNext()) {
				((JeeLinkEventListener) iterator.next()).packetReceived(msg);
			}

		} catch (Exception e) {
			logger.error("Event listener invoking error", e);
		}
	}

	private void _sendErrorToListeners(String error) {
		try {
			Iterator<JeeLinkEventListener> iterator = _listeners.iterator();

			while (iterator.hasNext()) {
				((JeeLinkEventListener) iterator.next()).errorOccured(error);
			}

		} catch (Exception e) {
			logger.error("Event listener invoking error", e);
		}
	}
}
