/*******************************************************************************
 * Copyright (c) 2009-2013 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   * Jurgen Vinju - interface and implementation
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
 *******************************************************************************/
package org.eclipse.imp.pdb.facts.impl;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.io.StandardTextWriter;

import java.io.IOException;
import java.io.StringWriter;

public abstract class AbstractValue implements IValue {

	protected AbstractValue() {
		super();
	}

	public String toString() {
		try {
			StringWriter stream = new StringWriter();
			new StandardTextWriter().write(this, stream);
			return stream.toString();
		} catch (IOException ioex) {
			// this never happens
		}
		return "";
	}
}
