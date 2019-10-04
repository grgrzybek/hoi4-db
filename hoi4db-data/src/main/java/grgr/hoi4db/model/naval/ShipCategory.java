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

import static grgr.hoi4db.model.naval.ShipHullCategory.CARRIER_HULL;
import static grgr.hoi4db.model.naval.ShipHullCategory.CRUISER_HULL;
import static grgr.hoi4db.model.naval.ShipHullCategory.HEAVY_HULL;
import static grgr.hoi4db.model.naval.ShipHullCategory.LIGHT_HULL;
import static grgr.hoi4db.model.naval.ShipHullCategory.SUB_HULL;

/**
 * Enum to sort ship types. These are specializations of {@link ShipHullCategory ship hull types}, to distinguish
 * light (CL) and heavy (CA) cruisers or battlecruisers (BC), battleships (BB) and super heavy battleships (SHBB).
 */
public enum ShipCategory {

    DESTROYER(1, LIGHT_HULL, "DD"),
    LIGHT_CRUISER(2, CRUISER_HULL, "CL"),
    HEAVY_CRUISER(3, CRUISER_HULL, "CA"),
    BATTLECRUISER(4, HEAVY_HULL, "BC"),
    BATTLESHIP(5, HEAVY_HULL, "BB"),
    SUPER_HEAVY_BATTLESHIP(6, HEAVY_HULL, "SHBB"),
    CARRIER(7, CARRIER_HULL, "CV"),
    SUBMARINE(8, SUB_HULL, "SS");

    private final int order;
    private final ShipHullCategory hullCategory;
    private final String symbol;

    ShipCategory(int order, ShipHullCategory hullCategory, String symbol) {
        this.order = order;
        this.hullCategory = hullCategory;
        this.symbol = symbol;
    }

    /**
     * Checks what kind of ship we have and set the category inside passed {@link ShipHull}
     * @param hull
     * @param modules
     */
    public static ShipCategory determineCategory(ShipHull hull, Map<String, Module> modules) {
        switch (hull.getHullCategory()) {
            case LIGHT_HULL:
                return DESTROYER;
            case CRUISER_HULL: {
                // we have to look at modules
                for (Module m : modules.values()) {
                    if (m.getCategory() == ModuleCategory.SHIP_MEDIUM_BATTERY) {
                        if (m.getId().startsWith("ship_light_medium_battery_")) {
                            return LIGHT_CRUISER;
                        } else if (m.getId().startsWith("ship_medium_battery_")) {
                            return HEAVY_CRUISER;
                        }
                    }
                }
                // no baterries included, so assume a not buildable light cruiser, which (when filled with proper
                // mandatory batteries) may be turned into buildable light or heavy cruiser
                return LIGHT_CRUISER;
            }
            case HEAVY_HULL: {
                // we have to look at modules
                for (Module m : modules.values()) {
                    if (m.getCategory() == ModuleCategory.SHIP_SUPER_HEAVY_ARMOR) {
                        return SUPER_HEAVY_BATTLESHIP;
                    }
                    if (m.getCategory() == ModuleCategory.SHIP_HEAVY_ARMOR) {
                        if (m.getId().startsWith("ship_armor_bc_")) {
                            return BATTLECRUISER;
                        } else if (m.getId().startsWith("ship_armor_bb_")) {
                            return BATTLESHIP;
                        }
                    }
                }
                // no armor, so assume not buildable battleship
                return BATTLESHIP;
            }
            case CARRIER_HULL:
                return CARRIER;
            case SUB_HULL:
                return SUBMARINE;
            default:
                return null;
        }
    }

    public int getOrder() {
        return order;
    }

    public ShipHullCategory getHullCategory() {
        return hullCategory;
    }

    public String getSymbol() {
        return symbol;
    }

}
