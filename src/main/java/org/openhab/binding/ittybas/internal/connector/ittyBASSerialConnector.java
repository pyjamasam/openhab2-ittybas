/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

 //This code is based very heavly on the code for the rfxcom binding. I say heavly, but really its copy and paste
package org.openhab.binding.ittybas.internal.connector;

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
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;

import java.io.DataInputStream;

import javax.xml.bind.DatatypeConverter;

import org.openhab.binding.ittybas.internal.ittyBASMessage;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ittyBAS JeeLink connector for serial port communication.
 *
 * @author Chris Whiteford - Initial contribution
 */
public class ittyBASSerialConnector implements ittyBASConnectorInterface, SerialPortEventListener {

	private static final Logger logger = LoggerFactory.getLogger(ittyBASSerialConnector.class);

	BlockingQueue _queue = new ArrayBlockingQueue(1024);

	SerialPort _serialPort = null;
	MessageBuffer _messageBuffer = null;

	private class MessageBuffer {
		private final Logger logger = LoggerFactory.getLogger(MessageBuffer.class);

		private LinkedList _buffer = null;
		public MessageBuffer() {
			_buffer = new LinkedList<Byte>();
		}

		public void addBytes(byte[] byteArray, int numBytesRead) {
			for (int i = 0; i < numBytesRead; i++)
			{
				_buffer.add(byteArray[i]);
			}
		}

		public ittyBASMessage getNextMessage()
		{
			//logger.debug("Checking {} bytes", _buffer.size());
			//Lets scan through our buffer and pull out any valid message (we are searching for our possible start characters (# or .))
			while( _buffer.size() > 0)
			{
				byte byteToInspect = (byte)_buffer.getFirst();
				if (byteToInspect == 0x23)		//#
				{
					int removeToIndex = 0;
					//We have a comment.  Read to the end of the line
					for (int i = 0; i < _buffer.size(); i++)
					{
						//Check to see if we have a \n that we can read up to
						byte byteToCheck = (byte)_buffer.get(i);
						if (byteToCheck == 0x0A)
						{
							//We found our line terminator.  Eat all the data up to here
							removeToIndex = i;
							break;
						}
					}

					if (removeToIndex > 0)
					{
						//logger.trace("Comment found - removing bytes from 0 to {}", removeToIndex);
						byte[] commentBytes = new byte[removeToIndex + 2];
						for (int i = 0; i < removeToIndex + 1; i++)
						{
							commentBytes[i] = (byte)_buffer.getFirst();
							_buffer.removeFirst();
						}
						return new ittyBASMessage(commentBytes, true);
					}
					else
					{
						//We didn't find the end ff the comment.  So lets just return
						return null;
					}
				}
				else if (byteToInspect == 0x2E)		//.
				{
					//Check to see if we have 11 more bytes in the buffer
					if (_buffer.size() >= 12)		//A value packet should be 12 bytes long (10 for data and 2 for \r\n)
					{
						//We have enough data for a packet.  So lets snag it all.
						byte[] readingBytes = new byte[10];
						for (int i = 0; i < 10; i++)
						{
							readingBytes[i] = (byte)_buffer.getFirst();
							_buffer.removeFirst();
						}

						//Consume the last two bytes in the packet (they are \r and \n)
						_buffer.removeFirst();
						_buffer.removeFirst();

						//logger.trace("Reading found");
						return new ittyBASMessage(readingBytes, false);
					}
					else
					{
						//We don't have enough data.  So lets just return.
						return null;
					}
				}
				else
				{
					//Unknown byte.  Just eat it.
					//logger.trace("Unknown Byte Found: {}", String.format("0x%02X", (byte)_buffer.getFirst()));
					_buffer.removeFirst();
				}
			}

			return null;
		}
	}

	public ittyBASSerialConnector() {
	}

	public BlockingQueue messageQueue() {
		return _queue;
	}

	@Override
	public void setup(String device) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException {

		logger.debug("Setting Up... ({})", device);

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

		_messageBuffer = new MessageBuffer();

		logger.debug("Set Up");
	}

	@Override
	public void serialEvent(SerialPortEvent arg0) {
		switch (arg0.getEventType())
		{
			case SerialPortEvent.DATA_AVAILABLE:
			{
				byte[] readBuffer = new byte[40];
				int byteCountRead = 0;
				try
				{
					while (_serialPort.getInputStream().available() > 0)
					{
						int numBytesRead = _serialPort.getInputStream().read(readBuffer);
						_messageBuffer.addBytes(readBuffer, numBytesRead);
						byteCountRead += numBytesRead;
					}
					//logger.debug("Done chunck of reads for this event callback (Read {} bytes)", byteCountRead);

					//Now see if there are any messages to pull out of the buffer we have
					ittyBASMessage msg = _messageBuffer.getNextMessage();
					while (msg != null)
					{
						if (!msg.isComment()) {
							try {
								_queue.put(msg);
							}
							catch (InterruptedException e) {
							}
						}
						else
						{
							logger.trace("Comment: {}", new String(msg.data(), StandardCharsets.US_ASCII).trim());
						}

						//Try and find another message
						msg = _messageBuffer.getNextMessage();
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
	public void shutdown() {
		logger.debug("Shutting down...");

		if (_serialPort != null) {
			logger.debug("Closing serial port");
			_serialPort.close();
		}
		_serialPort = null;

		_messageBuffer = null;

		logger.debug("Shutdown");
	}

}
