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
package grgr.hoi4db.model.naval;

import java.util.Map;

import grgr.hoi4db.model.HasName;

/**
 * Ship is named instance of some {@link ShipHull} or {@link ShipHullVariant}.
 */
public class Ship extends HasName implements Comparable<Ship> {

    private TaskForce taskForce;

    public Ship(String name) {
        super(name);
    }

    /**
     * Creates named {@link Ship} using country-specific {@link ShipHullVariant}
     * @param name
     * @param variant
     * @return
     */
    public static Ship fromShipHullVariant(String name, ShipHullVariant variant) {
        return fromShipHullAndModules(name, variant.getType(), variant.getModules());
    }

    /**
     * Creates named {@link Ship} using generic {@link ShipHull hull design}
     * @param name
     * @param hull
     * @return
     */
    public static Ship fromShipHull(String name, ShipHull hull) {
        return fromShipHullAndModules(name, hull, hull.getModules());
    }

    private static Ship fromShipHullAndModules(String name, ShipHull hull, Map<String, Module> modules) {
        Ship s = new Ship(name);

        return s;
    }

    @Override
    public int compareTo(Ship o) {
        return 0;
    }

}
