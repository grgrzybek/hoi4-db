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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class with great potential - could be treated as filter, validator, ...
 */
public class Constraint {

    private static final Pattern pattern = Pattern.compile("^\\(\\(([><=!]+)\\s*([^)]+)\\)\\)$");
    private static final Pattern decimal = Pattern.compile("^-?[0-9]*\\.[0-9]+$");
    private static final Pattern integer = Pattern.compile("^-?[0-9]+$");

    private final String name;
    private BigDecimal decimalValue;
    private BigInteger integerValue;

    private Operation operation;

    public Constraint(String name, String rawValue) {
        this.name = name;
        parse(rawValue);
    }

    /**
     * Parses value from JSON parser, like {@code ((<2))}
     * @param rawValue
     */
    public void parse(String rawValue) {
        Matcher m = pattern.matcher(rawValue);
        if (!m.find()) {
            throw new IllegalArgumentException("Can't parse \"" + rawValue + "\"");
        }
        String op = m.group(1);
        String value = m.group(2);

        switch (op) {
            case "<":
                operation = Operation.LT;
                break;
            case ">":
                operation = Operation.GT;
                break;
            case "<=":
                operation = Operation.LE;
                break;
            case ">=":
                operation = Operation.GE;
                break;
            case "=":
                operation = Operation.EQ;
                break;
            case "!=":
                operation = Operation.NE;
                break;
            default:
                throw new IllegalArgumentException("Can't determine operator for \"" + rawValue + "\"");
        }

        if (decimal.matcher(value).matches()) {
            decimalValue = new BigDecimal(value);
        } else if (integer.matcher(value).matches()) {
            integerValue = new BigInteger(value);
        }
    }

    public String getName() {
        return name;
    }

    public BigDecimal getDecimalValue() {
        return decimalValue;
    }

    public void setDecimalValue(BigDecimal decimalValue) {
        this.decimalValue = decimalValue;
    }

    public BigInteger getIntegerValue() {
        return integerValue;
    }

    public void setIntegerValue(BigInteger integerValue) {
        this.integerValue = integerValue;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public enum Operation {
        LT("<"),
        GT(">"),
        LE("<="),
        GE(">="),
        EQ("="),
        NE("!=");

        private String op;

        Operation(String op) {
            this.op = op;
        }

        @Override
        public String toString() {
            return op;
        }
    }

    @Override
    public String toString() {
        if (decimalValue != null) {
            return String.format("%s %.02f", operation, decimalValue);
        }
        if (integerValue != null) {
            return String.format("%s %d", operation, integerValue);
        }
        return "?";
    }
}
