/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelabs.internal.connector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TooManyListenersException;

import javax.xml.bind.DatatypeConverter;

import org.openhab.binding.jeelabs.internal.connector.JeeLinkEventListener;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JeeLink connector for remote TCP communication.
 *
 * @author Chris Whiteford - Initial contribution
 */
public class JeeLinkTCPConnector implements JeeLinkConnectorInterface {

	private static final Logger logger = LoggerFactory.getLogger(JeeLinkSerialConnector.class);

	private static List<JeeLinkEventListener> _listeners = new ArrayList<JeeLinkEventListener>();

	public JeeLinkTCPConnector() {
	}

	@Override
	public void connect(String device) {

        logger.debug("Connecting...");
	}

	@Override
	public void disconnect() {
		logger.debug("Disconnecting...");
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
