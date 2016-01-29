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

import org.openhab.binding.jeelabs.handler.JeeLinkHandler;
import org.openhab.binding.jeelabs.internal.JeeNodeDataListener;
import org.openhab.binding.jeelabs.internal.JeeNodeReading;


import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link JeeNodeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Chris Whiteford - Initial contribution
 */
public class JeeNodeHandler extends BaseThingHandler implements JeeNodeDataListener {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_JEENODE);

    private Logger logger = LoggerFactory.getLogger(JeeNodeHandler.class);

    private JeeLinkHandler _bridgeHandler;
    private int _nodeId = 0;

	public JeeNodeHandler(Thing thing) {
		super(thing);
	}

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
	}

    @Override
    public void initialize() {
        if (getConfig().get(NODE_ID) != null) {
			this._nodeId = Integer.parseInt((String) getConfig().get(NODE_ID));
            if (_getBridgeHandler() != null) {
                updateStatus(ThingStatus.ONLINE);
                logger.debug("initialized");
            }
            else
            {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.BRIDGE_OFFLINE, "nodeId is not set and is required.");
            }
        }
        else
        {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "nodeId is not set and is required.");
        }
    }

    @Override
    public void dispose() {
        logger.debug("disposed");
        if (this._nodeId != 0) {
            if (this._bridgeHandler != null) {
                this._bridgeHandler.unregisterNode(this);
            }
            this._nodeId = 0;
        }
    }


    private synchronized JeeLinkHandler _getBridgeHandler() {
        if (this._bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof JeeLinkHandler) {
                this._bridgeHandler = (JeeLinkHandler) handler;
                this._bridgeHandler.registerNode(this);
            } else {
                return null;
            }
        }
        return this._bridgeHandler;
    }

	public void dataUpdate(JeeNodeReading reading)
	{
		if (reading.nodeIdentifier() == this._nodeId)
		{
			//TODO: deal with this reading and update the appropriate channel with the data

			//Map our valueTypes to openhabs channels
			switch (reading.valueType())
			{
				case 4: {
					//Temperature
					updateState("temperature", new DecimalType(88.4));
					break;
				}
				case 8: {
					//Humidity
					updateState("relativehumidity", new DecimalType(44.8));
					break;
				}
			}
		}
	}
}
