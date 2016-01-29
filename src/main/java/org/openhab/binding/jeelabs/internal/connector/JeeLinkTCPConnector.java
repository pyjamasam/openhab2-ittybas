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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;

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

	BlockingQueue _queue = new ArrayBlockingQueue(1024);

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

	public BlockingQueue messageQueue() {
		return _queue;
	}
}
