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
 * Enum to sort ship/hull types
 */
public enum ShipCategory {

    LIGHT_SHIP(1),
    CRUISER_SHIP(2),
    HEAVY_SHIP(3),
    CARRIER_SHIP(4),
    SUB_SHIP(5);

    private final int order;

    ShipCategory(int order) {
        this.order = order;
    }

    public static ShipCategory fromHullFile(String name) {
        switch (name) {
            case "ship_hull_light.txt":
                return ShipCategory.LIGHT_SHIP;
            case "ship_hull_cruiser.txt":
                return ShipCategory.CRUISER_SHIP;
            case "ship_hull_heavy.txt":
                return ShipCategory.HEAVY_SHIP;
            case "ship_hull_carrier.txt":
                return ShipCategory.CARRIER_SHIP;
            case "ship_hull_submarine.txt":
                return ShipCategory.SUB_SHIP;
            default:
                return null;
        }
    }

    public int getOrder() {
        return order;
    }

}
