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

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import grgr.hoi4db.model.Conversion;
import grgr.hoi4db.model.HasId;
import grgr.hoi4db.model.ResourceAmount;
import grgr.hoi4db.model.Stat;

/**
 * Represents an entry from {@code /equipment_modules/*} nodes in {@code common/units/equipment/modules/00_ship_modules.txt}.
 */
public class Module extends HasId implements Comparable<Module> {

    private Module parent;

    private ModuleCategory category;
    private String gfx;
    private String sfx;

//    // add_equipment_type = capital_ship | anti_air
//    private String addEquipmentType;

//    // gui_category = ship_light_battery | ship_mine_warfare
//    private String guiCategory;

//    // critical_parts = damaged_heavy_guns | damaged_light_guns | damaged_secondaries | damaged_torpedoes
//    private List<String> criticalParts;

    private List<Stat> stats = new LinkedList<>();
    private List<Conversion> conversions = new LinkedList<>();
    private List<ResourceAmount> resources = new LinkedList<>();

    private BigInteger dismantleCost = BigInteger.ZERO;
    private List<ResourceAmount> dismantleCostResources = new LinkedList<>();

    public Module(String id) {
        super(id);
    }

    public Module getParent() {
        return parent;
    }

    public void setParent(Module parent) {
        this.parent = parent;
    }

    public ModuleCategory getCategory() {
        return category;
    }

    public void setCategory(ModuleCategory category) {
        this.category = category;
    }

    public String getGfx() {
        return gfx;
    }

    public void setGfx(String gfx) {
        this.gfx = gfx;
    }

    public String getSfx() {
        return sfx;
    }

    public void setSfx(String sfx) {
        this.sfx = sfx;
    }

    public BigInteger getDismantleCost() {
        return dismantleCost;
    }

    public void setDismantleCost(BigInteger dismantleCost) {
        this.dismantleCost = dismantleCost;
    }

    public List<Stat> getStats() {
        return stats;
    }

    public List<Conversion> getConversions() {
        return conversions;
    }

    public List<ResourceAmount> getResources() {
        return resources;
    }

    public List<ResourceAmount> getDismantleCostResources() {
        return dismantleCostResources;
    }

    @Override
    public int compareTo(Module o) {
        ModuleCategory c1 = this.getCategory();
        ModuleCategory c2 = o.category;

        if (c1.getGroup().getOrder() != c2.getGroup().getOrder()) {
            return c1.getGroup().getOrder() - c2.getGroup().getOrder();
        }
        if (c1.getOrder() != c2.getOrder()) {
            return c1.getOrder() - c2.getOrder();
        }
        return this.getId().compareTo(o.getId());
    }

    @Override
    public String toString() {
        return String.format("%s/%s (parent=%s, dismantleCost=%d, stats=[%s], conversions=[%s], resources=[%s], dismantleCostResources=[%s])", category, getId(), getParentId(),
                dismantleCost,
                stats.stream().map(Stat::toString).collect(Collectors.joining(", ")),
                conversions.stream().map(Conversion::toString).collect(Collectors.joining(", ")),
                resources.stream().map(ResourceAmount::toString).collect(Collectors.joining(", ")),
                dismantleCostResources.stream().map(ResourceAmount::toString).collect(Collectors.joining(", "))
        );
    }

}
