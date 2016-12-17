/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ittybas.handler;

import static org.openhab.binding.ittybas.ittyBASBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.openhab.binding.ittybas.handler.ittyBASLinkHandler;
import org.openhab.binding.ittybas.internal.ittyBASDataListener;
import org.openhab.binding.ittybas.internal.ittyBASReading;


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
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ittyBASNodeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Chris Whiteford - Initial contribution
 */
public class ittyBASNodeHandler extends BaseThingHandler implements ittyBASDataListener {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_JEENODE);
    private Logger logger = LoggerFactory.getLogger(ittyBASNodeHandler.class);

    private ittyBASLinkHandler _bridgeHandler;
    private int _nodeId = 0;

	public ittyBASNodeHandler(Thing thing) {
		super(thing);
	}

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
	}

    @Override
    public void initialize() {
		updateState("temperature", UnDefType.UNDEF);
		updateState("relativehumidity", UnDefType.UNDEF);

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


    private synchronized ittyBASLinkHandler _getBridgeHandler() {
        if (this._bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof ittyBASLinkHandler) {
                this._bridgeHandler = (ittyBASLinkHandler) handler;
                this._bridgeHandler.registerNode(this);
            } else {
                return null;
            }
        }
        return this._bridgeHandler;
    }

	public void dataUpdate(ittyBASReading reading)
	{
		if (reading.nodeIdentifier() == this._nodeId)
		{
			//deal with this reading and update the appropriate channel with the data
			//Map our valueTypes to openhabs channels
			switch (reading.valueType())
			{
				case 4: {
          //Temperature
					//This data should come in as a float
					if (reading.dataType() == ittyBASReading.ittyBASReadingDataType.FLOAT) {
						BigDecimal decimalValue = new BigDecimal(Double.toString(reading.doubleValue())).setScale(2, RoundingMode.HALF_UP);
						updateState("temperature", new DecimalType(decimalValue));
					}
					else {
						//This is an unexpected data type for this channel. Update the state to reflect such
						updateState("temperature", UnDefType.UNDEF);
					}
					break;
				}

        case 6: {
					//Battery Voltage
					if (reading.dataType() == ittyBASReading.ittyBASReadingDataType.FLOAT) {
						BigDecimal decimalValue = new BigDecimal(Double.toString(reading.doubleValue())).setScale(2, RoundingMode.HALF_UP);
						updateState("battery-voltage", new DecimalType(decimalValue));
					}
					else {
						//This is an unexpected data type for this channel. Update the state to reflect such
						updateState("battery-voltage", UnDefType.UNDEF);
					}
					break;
				}

				case 8: {
					//Humidity
					if (reading.dataType() == ittyBASReading.ittyBASReadingDataType.FLOAT) {
						BigDecimal decimalValue = new BigDecimal(Double.toString(reading.doubleValue())).setScale(2, RoundingMode.HALF_UP);
						updateState("relativehumidity", new DecimalType(decimalValue));
					}
					else {
						//This is an unexpected data type for this channel. Update the state to reflect such
						updateState("relativehumidity", UnDefType.UNDEF);
					}
					break;
				}
			}
		}
	}
}
