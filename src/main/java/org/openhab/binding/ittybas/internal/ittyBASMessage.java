/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.ittybas.internal;

import java.util.Arrays;

public class ittyBASMessage {
	private Boolean _isComment = false;
	private byte[] _data = null;

	public ittyBASMessage(byte[] data, Boolean isComment) {
		_isComment = isComment;

		_data = Arrays.copyOf(data, data.length);
	}

	public Boolean isComment() {
		return _isComment;
	}

	public byte[] data() {
		return _data;
	}
}
