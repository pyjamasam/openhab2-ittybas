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
package org.openhab.binding.ittybas.handler;

import static org.openhab.binding.ittybas.ittyBASBindingConstants.*;

import org.openhab.binding.ittybas.internal.ittyBASDataListener;
import org.openhab.binding.ittybas.internal.ittyBASReading;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.Collections;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.eclipse.jdt.annotation.Nullable;


/**
 * The {@link ittyBASHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Chris Whiteford - Initial contribution
 */
@NonNullByDefault public class ittyBASNodeHandler extends BaseThingHandler implements ittyBASDataListener
{
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_JEENODE);
    private final Logger logger = LoggerFactory.getLogger(ittyBASNodeHandler.class);
    
    private int _nodeId = 0;

    public ittyBASNodeHandler(Thing thing) 
    {
        super(thing);
    }

    @Override public void handleCommand(ChannelUID channelUID, Command command) 
    {
    }

    @Override public void initialize() 
    {
        logger.debug("Starting initialize of NodeHandler: {}", this);

        Thing thisThing = this.getThing();
        thisThing.setLabel("ittyBAS Jeenode - " + thisThing.getUID().getId()); 

        updateState(CHANNEL_TEMPERATURE, UnDefType.UNDEF);
		updateState(CHANNEL_RELATIVEHUMIDITY, UnDefType.UNDEF);
        updateState(CHANNEL_BATTERY_VOLTAGE, UnDefType.UNDEF);

        if (getConfig().get(NODE_ID) != null) 
        {
            this._nodeId = Integer.parseInt((String) getConfig().get(NODE_ID));
            updateStatus(ThingStatus.ONLINE);
        }
        else
        {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "nodeId is not set and is required.");
        }

        logger.debug("Finished initialize of NodeHandler: {}", this);
    }

    @Override public void dispose() 
    {
        logger.debug("Starting dispose of NodeHandler: {}", this);        
        
        if (this._nodeId != 0) 
        {
            this._nodeId = 0;
        }

        logger.debug("Finished dispose of NodeHandler: {}", this);
    }
    
    @Override
    public void channelLinked(ChannelUID channelUID) {
        
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        
    }

    public void dataUpdate(org.openhab.binding.ittybas.internal.ittyBASReading reading)
	{
        //logger.debug("Checking passed in reading node identifier ({}) against local node identifier ({})", reading.nodeIdentifier(), this._nodeId);
		if (reading.nodeIdentifier() == this._nodeId)
		{
            //logger.debug("Reading is for this node");
			//deal with this reading and update the appropriate channel with the data
			//Map our valueTypes to openhabs channels
			switch (reading.valueType())
			{
				case 4: 
                {
                    //Temperature
					//This data should come in as a float
					if (reading.dataType() == ittyBASReading.ittyBASReadingDataType.FLOAT) 
                    {
						BigDecimal decimalValue = new BigDecimal(Double.toString(reading.doubleValue())).setScale(2, RoundingMode.HALF_UP);
						updateState(CHANNEL_TEMPERATURE, new DecimalType(decimalValue));
					}
					else {
						//This is an unexpected data type for this channel. Update the state to reflect such
						updateState(CHANNEL_TEMPERATURE, UnDefType.UNDEF);
					}
					break;
				}

                case 6: 
                {
					//Battery Voltage
					if (reading.dataType() == ittyBASReading.ittyBASReadingDataType.FLOAT) 
                    {
						BigDecimal decimalValue = new BigDecimal(Double.toString(reading.doubleValue())).setScale(2, RoundingMode.HALF_UP);
						updateState(CHANNEL_BATTERY_VOLTAGE, new DecimalType(decimalValue));
					}
					else {
						//This is an unexpected data type for this channel. Update the state to reflect such
						updateState(CHANNEL_BATTERY_VOLTAGE, UnDefType.UNDEF);
					}
					break;
				}

				case 8: 
                {
					//Humidity
					if (reading.dataType() == ittyBASReading.ittyBASReadingDataType.FLOAT) 
                    {
						BigDecimal decimalValue = new BigDecimal(Double.toString(reading.doubleValue())).setScale(2, RoundingMode.HALF_UP);
						updateState(CHANNEL_RELATIVEHUMIDITY, new DecimalType(decimalValue));
					}
					else {
						//This is an unexpected data type for this channel. Update the state to reflect such
						updateState(CHANNEL_RELATIVEHUMIDITY, UnDefType.UNDEF);
					}
					break;
				}
			}
		}
	}
}
