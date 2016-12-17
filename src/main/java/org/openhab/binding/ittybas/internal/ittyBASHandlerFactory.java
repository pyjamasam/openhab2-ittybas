/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ittybas.internal;

import static org.openhab.binding.ittybas.ittyBASBindingConstants.*;

import java.util.Collections;
import java.util.Set;

import org.openhab.binding.ittybas.handler.ittyBASLinkHandler;
import org.openhab.binding.ittybas.handler.ittyBASNodeHandler;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link ittyBASHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Chris Whiteford - Initial contribution
 */
public class ittyBASHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.union(ittyBASLinkHandler.SUPPORTED_THING_TYPES, ittyBASNodeHandler.SUPPORTED_THING_TYPES);

    private Logger logger = LoggerFactory.getLogger(ittyBASHandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }


    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (ittyBASLinkHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            ittyBASLinkHandler handler = new ittyBASLinkHandler((Bridge) thing);
            return handler;
        } else if (ittyBASNodeHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            ittyBASNodeHandler handler = new ittyBASNodeHandler(thing);
            return handler;
        } else {
            return null;
        }
    }
}
