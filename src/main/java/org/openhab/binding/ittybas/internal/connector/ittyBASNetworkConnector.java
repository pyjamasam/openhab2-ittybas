/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ittybas.internal.connector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openhab.binding.ittybas.internal.ittyBASMessage;

import org.openhab.binding.ittybas.internal.connector.UdpServer;

/**
 * ittyBAS connector for remote communication.
 *
 * @author Chris Whiteford - Initial contribution
 */
public class ittyBASNetworkConnector implements ittyBASConnectorInterface {

	private static final Logger logger = LoggerFactory.getLogger(ittyBASNetworkConnector.class);

	BlockingQueue _queue = new ArrayBlockingQueue(1024);

	public ittyBASNetworkConnector() {
	}

	private UdpServer _us = null;

	@Override
	public void setup(String listenport) {
		int port = 44662;
		try
		{
			port = Integer.parseInt(listenport);
		}
    catch( Exception exc )
		{
    	logger.error("No port, or bad port, provided. Will use " + port );
    }   // end catch

		logger.debug("Starting UDP listener on port " + port);
		_us = new UdpServer();                             // Create the server
  	_us.setPort( port );
		_us.setLoggingLevel(java.util.logging.Level.WARNING);

		_us.addUdpServerListener( new UdpServer.Listener() {         // Add listener
            @Override
            public void packetReceived( UdpServer.Event evt )
						{     // Packet received
								ittyBASMessage msg = new ittyBASMessage(evt.getPacketAsBytes(), false);
								try {
									_queue.put(msg);
								}
								catch (InterruptedException e) {
								}
                //logger.debug( evt.getPacketAsString() );      // Write to console
            }   // end packetReceived
        }); // end Listener

		//Spin the server up
		_us.start();
	}

	@Override
	public void shutdown() {
		if (_us != null)
		{
			_us.stop();
			_us = null;
		}
	}

	public BlockingQueue messageQueue() {
		return _queue;
	}
}
