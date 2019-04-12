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
 * Enum to group/sort ship modules within categories and groups of categories.
 */
public enum ModuleCategory {

    LIGHT_SHIP_ENGINE(ModuleCategoryGroup.ENGINE, 1),
    CRUISER_SHIP_ENGINE(ModuleCategoryGroup.ENGINE, 2),
    HEAVY_SHIP_ENGINE(ModuleCategoryGroup.ENGINE, 3),
    CARRIER_SHIP_ENGINE(ModuleCategoryGroup.ENGINE, 4),
    SUB_SHIP_ENGINE(ModuleCategoryGroup.ENGINE, 5),

    SHIP_CRUISER_ARMOR(ModuleCategoryGroup.ARMOR, 1),
    SHIP_HEAVY_ARMOR(ModuleCategoryGroup.ARMOR, 2),
    SHIP_SUPER_HEAVY_ARMOR(ModuleCategoryGroup.ARMOR, 3),
    SHIP_CARRIER_ARMOR(ModuleCategoryGroup.ARMOR, 4),

    SHIP_LIGHT_BATTERY(ModuleCategoryGroup.BATTERY, 1),
    SHIP_MEDIUM_BATTERY(ModuleCategoryGroup.BATTERY, 2),
    SHIP_HEAVY_BATTERY(ModuleCategoryGroup.BATTERY, 3),
    SHIP_SUPER_HEAVY_BATTERY(ModuleCategoryGroup.BATTERY, 4),

    SHIP_ANTI_AIR(ModuleCategoryGroup.BATTERY, 5),
    SHIP_SECONDARIES(ModuleCategoryGroup.BATTERY, 6),

    SHIP_AIRPLANE_LAUNCHER(ModuleCategoryGroup.OTHER, 1),
    SHIP_DECK_SPACE(ModuleCategoryGroup.OTHER, 2),

    SHIP_MINE_LAYER(ModuleCategoryGroup.OTHER, 3),
    SHIP_MINE_LAYER_SUB(ModuleCategoryGroup.OTHER, 4),
    SHIP_MINE_WARFARE(ModuleCategoryGroup.OTHER, 5),

    SHIP_DEPTH_CHARGE(ModuleCategoryGroup.OTHER, 6),
    SHIP_TORPEDO(ModuleCategoryGroup.OTHER, 7),

    SHIP_EXTRA_FUEL_TANK(ModuleCategoryGroup.OTHER, 8),
    SHIP_FIRE_CONTROL_SYSTEM(ModuleCategoryGroup.OTHER, 9),
    SHIP_RADAR(ModuleCategoryGroup.OTHER, 10),
    SHIP_SONAR(ModuleCategoryGroup.OTHER, 11),
    SHIP_SUB_SNORKEL(ModuleCategoryGroup.OTHER, 12),
    SHIP_TORPEDO_SUB(ModuleCategoryGroup.OTHER, 13);

    private final ModuleCategoryGroup group;
    // order within category group
    private final int order;

    ModuleCategory(ModuleCategoryGroup group, int order) {
        this.group = group;
        this.order = order;
    }

    public ModuleCategoryGroup getGroup() {
        return group;
    }

    public int getOrder() {
        return order;
    }

    public static ModuleCategory byName(String name) {
        return valueOf(name.toUpperCase());
    }

}
