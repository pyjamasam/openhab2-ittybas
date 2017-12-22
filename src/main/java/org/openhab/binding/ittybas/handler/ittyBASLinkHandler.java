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
import org.openhab.binding.ittybas.internal.ittyBASDataProcessor;


import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.BlockingQueue;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link ittyBASHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Chris Whiteford - Initial contribution
 */
@NonNullByDefault public class ittyBASLinkHandler extends BaseBridgeHandler 
{
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_JEELINK);
    private final Logger logger = LoggerFactory.getLogger(ittyBASLinkHandler.class);

    private List<ittyBASDataListener> _dataListeners = new CopyOnWriteArrayList<>();
    private org.openhab.binding.ittybas.internal. @Nullable ittyBASDataProcessor _dataProcessor = null;
    private org.openhab.binding.ittybas.internal.connector. @Nullable ittyBASConnectorInterface _connector = null;    

    public ittyBASLinkHandler(Bridge bridge) 
    {
        super(bridge);

        logger.debug("Created an instance of a LinkHandler: {}", this);
    }

    @Override public void handleCommand(ChannelUID channelUID, Command command) 
    {
    }

    @Override public void initialize() 
    {
        logger.debug("Starting initialize of LinkHandler: {}", this);

        Thing thisThing = this.getThing();
        thisThing.setLabel("ittyBAS Jeelink - " + thisThing.getUID().getId()); 
        
        String portString = "";
        if (getConfig().get(SERIAL_PORT) != null) 
		{
            String serialPort = (String) getConfig().get(SERIAL_PORT);
            logger.info("Creating a serial connector for port: {}", serialPort);
            
            _connector = new org.openhab.binding.ittybas.internal.connector.ittyBASSerialConnector();
            portString = serialPort;
        }
        else if (getConfig().get(LISTEN_PORT) != null)
		{
            String listenPort = (String) getConfig().get(LISTEN_PORT);
            logger.info("Creating a network connector for port: {}", listenPort);

           	_connector = new org.openhab.binding.ittybas.internal.connector.ittyBASNetworkConnector();
            portString = listenPort;
        }
        else
        {
            logger.error("Missing serialPort or listenPort");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Cannot connect.  serialPort or listenPort is not set in configuration");
        }

        if (_connector != null && portString.length() > 0) 
        {
            try 
            {
                _connector.setup(portString);

                _dataProcessor = new org.openhab.binding.ittybas.internal.ittyBASDataProcessor(this);
                _dataProcessor.start();
                updateStatus(ThingStatus.ONLINE);                
            }
            catch (Exception e) 
            {
                logger.error("Exception trying to setup connection manager: {}", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Unable to create connection manager");                
            }
        }
        else
        {
            logger.error("Connection manager was null after create");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Unable to create connection manager");  
        }

        logger.debug("Finished initialize of LinkHandler: {}", this);
    }

    @Override public void dispose()
    {
        logger.debug("Starting dispose of LinkHandler: {}", this);

        if (_dataProcessor != null) 
        {
            _dataProcessor.interrupt();
            try
            {
                _dataProcessor.join();
            }
            catch (InterruptedException e)
            {
            }

            _dataProcessor = null;
        }

        if (_connector != null) 
        {
			_connector.shutdown();
			_connector = null;
		}

        updateStatus(ThingStatus.OFFLINE);

        logger.debug("Finished dispose of LinkHandler: {}", this);
    }

    @Override
    public void childHandlerInitialized(@NonNull ThingHandler childHandler, @NonNull Thing childThing)
    {
        if (ittyBASDataListener.class.isInstance(childHandler))
        {
            _dataListeners.add((ittyBASDataListener)childHandler);
        }
        super.childHandlerInitialized(childHandler, childThing);
    }
    
    @Override
    public void childHandlerDisposed(@NonNull ThingHandler childHandler, @NonNull Thing childThing)
    {
        if (ittyBASDataListener.class.isInstance(childHandler))
        {
            _dataListeners.remove((ittyBASDataListener)childHandler);
        }
        super.childHandlerDisposed(childHandler, childThing);
    }

    public BlockingQueue messageQueue()
    {
        return _connector.messageQueue();
    }

    public List<ittyBASDataListener> dataListeners()
    {
        return _dataListeners;
    }
}
