/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.jeelabs.internal;

import org.openhab.binding.jeelabs.internal.JeeLinkMessage;



public class JeeNodeReading
{
	public enum JeeNodeReadingDataType {
	    UNKNOWN, UNSIGNED_LONG, LONG, FLOAT, BINARY
	}

	private int _nodeIdentifier;
	private int _valueType;
	private JeeNodeReadingDataType _dataType;


	public JeeNodeReading(JeeLinkMessage message) {
		//	formatString = 'xBHH4s'

		//	nodeIdentifier = parsedLine[0]
		Byte nodeByte = message.data()[1];
		_nodeIdentifier = nodeByte.intValue();

		//	valueType = parsedLine[1]
		_valueType = ((message.data()[3] & 0xff) << 8) | (message.data()[2] & 0xff);

		//TODO: data...
		//	dataType = parsedLine[2]
		//	rawData = parsedLine[3]
	}

	public int nodeIdentifier() {
		return _nodeIdentifier;
	}

	public int valueType() {
		return _valueType;
	}
}
