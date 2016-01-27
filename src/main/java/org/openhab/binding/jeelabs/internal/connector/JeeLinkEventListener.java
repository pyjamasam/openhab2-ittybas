/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelabs.internal.connector;

/**
 * This interface defines interface to receive data from JeeLink controller.
 *
 * @author Chris Whiteford - Initial contribution
 */
public interface JeeLinkEventListener {

	/**
	 * Procedure for receive raw data from RFXCOM controller.
	 *
	 * @param data
	 *            Received raw data.
	 */
	void packetReceived(byte[] data);

	/**
	 * Procedure for receiving information fatal error.
	 *
	 * @param error
	 *            Error occured.
	 */
	void errorOccured(String error);
}
