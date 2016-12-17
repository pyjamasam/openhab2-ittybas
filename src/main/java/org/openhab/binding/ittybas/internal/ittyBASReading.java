/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.ittybas.internal;

import org.openhab.binding.ittybas.internal.ittyBASMessage;
import java.nio.ByteBuffer;


public class ittyBASReading
{
	public enum ittyBASReadingDataType {
	    UNKNOWN, UNSIGNED_LONG, LONG, FLOAT, BINARY
	}

	private int _nodeIdentifier;
	private int _valueType;
	private ittyBASReadingDataType _dataType;

	private long _longValue;
	private double _doubleValue;


	public ittyBASReading(ittyBASMessage message) {
		//	formatString = 'xBHH4s'

		//	nodeIdentifier = parsedLine[0]
		Byte nodeByte = message.data()[1];
		_nodeIdentifier = nodeByte.intValue();

		//	valueType = parsedLine[1]
		_valueType = ((message.data()[3] & 0xff) << 8) | (message.data()[2] & 0xff);

		//	dataType = parsedLine[2]
		_dataType = ittyBASReadingDataType.values()[((message.data()[5] & 0xff) << 8) | (message.data()[4] & 0xff)];

		switch (_dataType)
		{
			//# 1 = Unsigned Long
			//# 2 = Long
			//# 3 = Float
			//# 4 = Binary (4 bytes)
			case UNSIGNED_LONG:
			case LONG: {
				ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
			    buffer.put(message.data()[6]);
				buffer.put(message.data()[7]);
				buffer.put(message.data()[8]);
				buffer.put(message.data()[9]);
			    buffer.flip();//need flip
			    _longValue = buffer.getLong();
				break;
			}
			case FLOAT: {
				int doubleBits = ((message.data()[9] & 0xff) << 24 |
								(message.data()[8] & 0xff) << 16 |
								(message.data()[7] & 0xff) << 8 |
								(message.data()[6] & 0xff));

				_doubleValue = Float.intBitsToFloat(doubleBits);
				break;
			}
			default: {
				break;
			}
		}
		//	rawData = parsedLine[3]
	}

	public int nodeIdentifier() {
		return _nodeIdentifier;
	}

	public int valueType() {
		return _valueType;
	}

	public ittyBASReadingDataType dataType() {
		return _dataType;
	}

	public long longValue() {
		return _longValue;
	}

	public double doubleValue() {
		return _doubleValue;
	}
}
