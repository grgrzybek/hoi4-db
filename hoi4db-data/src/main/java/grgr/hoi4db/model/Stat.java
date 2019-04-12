/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package grgr.hoi4db.model;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * A value that's affecting given object's own value when combined. Used for example with ship modules.
 */
public class Stat extends HasId {

    public static final BigDecimal BD_100 = new BigDecimal(100);

    private Number value;
    private Operation operation;

    /**
     * Constructs a stat that's added to original value
     * @param id
     * @param value
     * @return
     */
    public static Stat addedValue(String id, Number value) {
        return new Stat(id, value, Operation.ADD);
    }

    /**
     * Constructs a stat that's added as average value of all stats with the same id
     * @param id
     * @param value
     * @return
     */
    public static Stat addedAverageValue(String id, Number value) {
        return new Stat(id, value, Operation.ADD_AVERAGE);
    }

    /**
     * Constructs a stat that's added as percentage of original value
     * @param id
     * @param value
     * @return
     */
    public static Stat percentageValue(String id, Number value) {
        return new Stat(id, value, Operation.MULTIPLY);
    }

    private Stat(String id, Number value, Operation operation) {
        super(id);
        this.value = value;
        this.operation = operation;
    }

    @Override
    public String toString() {
        switch (operation) {
            case ADD:
                return String.format("%s: %+.02f", getId(), bigDecimalValue());
            case ADD_AVERAGE:
                return String.format("%s: ~%+.02f", getId(), bigDecimalValue());
            case MULTIPLY:
                return String.format("%s: %+.02f%%", getId(), bigDecimalValue().multiply(BD_100));
            default:
                throw new IllegalArgumentException();
        }
    }

    public BigDecimal bigDecimalValue() {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger) value);
        }
        return null;
    }

    public enum Operation {
        ADD, ADD_AVERAGE, MULTIPLY
    }

}
