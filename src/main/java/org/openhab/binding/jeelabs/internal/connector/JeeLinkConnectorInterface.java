/**
 * Copyright (c) 2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelabs.internal.connector;

import org.openhab.binding.jeelabs.internal.connector.JeeLinkEventListener;

import java.io.IOException;

/**
 * This interface defines interface to communicate JeeLink controller.
 *
 * @author Chris Whiteford - Initial contribution
 */
public interface JeeLinkConnectorInterface {

	/**
	 * Procedure for connecting to JeeLink controller.
	 *
	 * @param device
	 *            Controller connection parameters (e.g. serial port name or IP
	 *            address).
	 */
	public void connect(String device) throws Exception;


	/**
	 * Procedure for disconnecting to JeeLink controller.
	 *
	 */
	public void disconnect();

	/**
	 * Procedure for register event listener.
	 *
	 * @param listener
	 *            Event listener instance to handle events.
	 */
	public void addEventListener(JeeLinkEventListener listener);

	/**
	 * Procedure for remove event listener.
	 *
	 * @param listener
	 *            Event listener instance to remove.
	 */
	public void removeEventListener(JeeLinkEventListener listener);

}
