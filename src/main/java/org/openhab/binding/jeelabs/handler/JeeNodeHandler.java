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

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link JeeNodeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Chris Whiteford - Initial contribution
 */
public class JeeNodeHandler extends BaseThingHandler {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_JEENODE);

    private Logger logger = LoggerFactory.getLogger(JeeNodeHandler.class);

    private JeeLinkHandler _bridgeHandler;
    private String _nodeId;

	public JeeNodeHandler(Thing thing) {
		super(thing);
	}

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
	}

    @Override
    public void initialize() {
        this._nodeId = (String) getConfig().get(NODE_ID);
        if (this._nodeId != null) {
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
        if (this._nodeId != null) {
            JeeLinkHandler bridgeHandler = _getBridgeHandler();
            if (bridgeHandler != null) {
                //getHueBridgeHandler().unregisterLightStatusListener(this);
            }
            this._nodeId = null;
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
                //this.bridgeHandler.registerLightStatusListener(this);
            } else {
                return null;
            }
        }
        return this._bridgeHandler;
    }
}
