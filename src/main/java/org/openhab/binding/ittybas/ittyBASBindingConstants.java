/**
 * Copyright (c) 2014,2017 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.ittybas;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ittyBASBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Chris Whiteford - Initial contribution
 */
 @NonNullByDefault
public class ittyBASBindingConstants {

    public static final String BINDING_ID = "ittybas";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_JEELINK = new ThingTypeUID(BINDING_ID, "jeelink");
    public final static ThingTypeUID THING_TYPE_JEENODE = new ThingTypeUID(BINDING_ID, "jeenode");

    // List of all Channel ids
    public final static String CHANNEL_TEMPERATURE = "temperature";
    public final static String CHANNEL_RELATIVEHUMIDITY = "relativehumidity";
    public final static String CHANNEL_BATTERY_VOLTAGE = "battery-voltage";

    // Bridge config properties
    public static final String SERIAL_PORT = "serialPort";
    public static final String LISTEN_PORT = "listenPort";
    public static final String NODE_ID = "nodeId";

}
