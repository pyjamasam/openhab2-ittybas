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

import org.openhab.binding.jeelabs.internal.connector.JeeLinkConnectorInterface;
import org.openhab.binding.jeelabs.internal.connector.JeeLinkSerialConnector;
import org.openhab.binding.jeelabs.internal.connector.JeeLinkTCPConnector;

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

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_JEELINK);

    private Logger logger = LoggerFactory.getLogger(JeeLinkHandler.class);

    private JeeLinkConnectorInterface _connector = null;

	public JeeLinkHandler(Bridge bridge) {
		super(bridge);
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

        if (_connector != null) {
			_connector.disconnect();
            _connector = null;
		}
    }
}
