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

/**
 * Enum to sort ship hull types. Light (CL) and heavy (CA) cruisers use the same hull category, but have to be treated
 * separately. Same for battlecruisers (BC), battleships (BB) and super heavy battleships (SHBB).
 */
public enum ShipHullCategory {

    LIGHT_HULL(1),
    CRUISER_HULL(2),
    HEAVY_HULL(3),
    CARRIER_HULL(4),
    SUB_HULL(5);

    private final int order;

    ShipHullCategory(int order) {
        this.order = order;
    }

    public static ShipHullCategory fromHullFile(String name) {
        switch (name) {
            case "ship_hull_light.txt":
                return ShipHullCategory.LIGHT_HULL;
            case "ship_hull_cruiser.txt":
                return ShipHullCategory.CRUISER_HULL;
            case "ship_hull_heavy.txt":
                return ShipHullCategory.HEAVY_HULL;
            case "ship_hull_carrier.txt":
                return ShipHullCategory.CARRIER_HULL;
            case "ship_hull_submarine.txt":
                return ShipHullCategory.SUB_HULL;
            default:
                return null;
        }
    }

    public int getOrder() {
        return order;
    }

}
