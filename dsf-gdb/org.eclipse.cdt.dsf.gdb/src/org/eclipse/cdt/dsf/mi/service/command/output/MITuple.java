/*******************************************************************************
 * Copyright (c) 2000, 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Vladimir Prus (Mentor Graphics) - Add getMIValue method.
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.HashMap;
import java.util.Map;

/**
 * GDB/MI tuple value.
 */
public class MITuple extends MIValue {

    final private static MIResult[] NULL_RESULTS = new MIResult[0];
    final private static MIValue[] NULL_VALUES = new MIValue[0];

    private MIResult[] results = NULL_RESULTS;
    private MIValue[] values = NULL_VALUES;
    private Map<String, MIValue> name2value;

    public MIResult[] getMIResults() {
        return results;
    }

    public void setMIResults(MIResult[] res) {
        results = res;
        name2value = null;
    }

    public MIValue[] getMIValues() {
        return values;
    }

    /** Return the value of the specified field of this tuple.
     *
	 * @since 4.6
	 */
    public MIValue getField(String name) {
        if (name2value == null) {
            name2value = new HashMap<String, MIValue>();
            for (MIResult r : results) {
                name2value.put(r.getVariable(), r.getMIValue());
            }
        }
        return name2value.get(name);
    }

    public void setMIValues(MIValue[] vals) {
        values = vals;
    }

    @Override
    public String toString() {
        return toString("{", "}"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    // Return comma-separated values, with start and end prepended and appended
    // Intentionally package private, should only be used by ourselves and
    // MIResultRecord.
    String toString(String start, String end)
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(start);
        for (int i = 0; i < results.length; i++) {
            if (i != 0) {
                buffer.append(',');
            }
            buffer.append(results[i].toString());
        }
        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                buffer.append(',');
            }
            buffer.append(values[i].toString());
        }
        buffer.append(end);
        return buffer.toString();
    }
}
