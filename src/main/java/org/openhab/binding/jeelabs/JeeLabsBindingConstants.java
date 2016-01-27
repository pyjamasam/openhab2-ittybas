/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelabs;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link JeeLabsBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Chris Whiteford - Initial contribution
 */
public class JeeLabsBindingConstants {

    public static final String BINDING_ID = "jeelabs";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_JEELINK = new ThingTypeUID(BINDING_ID, "jeelink");
    public final static ThingTypeUID THING_TYPE_JEENODE = new ThingTypeUID(BINDING_ID, "jeenode");

    // List of all Channel ids
    //public final static String CHANNEL_1 = "channel1";

    // Bridge config properties
    public static final String SERIAL_PORT = "serialPort";
    public static final String IP_ADDRESS = "ipAddress";
    public static final String NODE_ID = "nodeId";

}
