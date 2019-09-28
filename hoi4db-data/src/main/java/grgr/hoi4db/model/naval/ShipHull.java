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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import grgr.hoi4db.model.HasId;
import grgr.hoi4db.model.ResourceAmount;

import static grgr.hoi4db.model.Stat.BD_100;

/**
 * Represents an entry from {@code /equipments/*} nodes in {@code common/units/equipment/(ship_hull_|)*.txt}.
 */
public class ShipHull extends HasId implements Comparable<ShipHull> {

    private int year = 1936;

    private ShipCategory category;

    private boolean archetype;
    private boolean buildable;

    private ShipHull parent;

    // https://hoi4.paradoxwikis.com/Equipment_modding
    // - archetype: Which archetype equipment this equipment inherits from.
    // - parent: Which equipment is parent to this equipment (i.e. which does it supercede)
    private String archetypeId;

    private String interfaceCategory;

//    private int priority;
//    private String visual_level;
//    private String alias;

    private List<String> types = new LinkedList<>();
    private List<String> upgrades = new LinkedList<>();

    private BigDecimal lightArmorPiercing;
    private BigDecimal lightAttack;
    private BigDecimal heavyArmorPiercing;
    private BigDecimal heavyAttack;
    private BigDecimal torpedoAttack;
    private BigDecimal subAttack;
    private BigDecimal antiAirAttack;

    private BigDecimal armor;

    private BigDecimal surfaceDetection;
    private BigDecimal subDetection;
    private BigDecimal surfaceVisibility;

    private BigDecimal navalSpeed;
    private BigDecimal navalRange;

    private BigDecimal reliability;
    private BigDecimal hp;

    private BigDecimal fuelConsumption;

    private BigDecimal buildCost;
    private BigInteger manpower;

    private List<ResourceAmount> resources = new LinkedList<>();

    private List<ModuleCountLimit> moduleLimits = new LinkedList<>();

    private Map<String, Slot> slots = new LinkedHashMap<>();

    // mapping from slot Id to actual module used in given slot
    private Map<String, Module> modules = new LinkedHashMap<>();

    public ShipHull(String id) {
        super(id);
    }

    public ShipHull getParent() {
        return parent;
    }

    public void setParent(ShipHull parent) {
        this.parent = parent;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public ShipCategory getCategory() {
        return category;
    }

    public void setCategory(ShipCategory category) {
        this.category = category;
    }

    public boolean isArchetype() {
        return archetype;
    }

    public void setArchetype(boolean archetype) {
        this.archetype = archetype;
    }

    public boolean isBuildable() {
        return buildable;
    }

    public void setBuildable(boolean buildable) {
        this.buildable = buildable;
    }

    public String getArchetypeId() {
        return archetypeId;
    }

    public void setArchetypeId(String archetypeId) {
        this.archetypeId = archetypeId;
    }

    public String getInterfaceCategory() {
        return interfaceCategory;
    }

    public void setInterfaceCategory(String interfaceCategory) {
        this.interfaceCategory = interfaceCategory;
    }

    public List<String> getTypes() {
        return types;
    }

    public List<String> getUpgrades() {
        return upgrades;
    }

    public BigDecimal getLightArmorPiercing() {
        return lightArmorPiercing;
    }

    public void setLightArmorPiercing(BigDecimal lightArmorPiercing) {
        this.lightArmorPiercing = lightArmorPiercing;
    }

    public BigDecimal getLightAttack() {
        return lightAttack;
    }

    public void setLightAttack(BigDecimal lightAttack) {
        this.lightAttack = lightAttack;
    }

    public BigDecimal getHeavyArmorPiercing() {
        return heavyArmorPiercing;
    }

    public void setHeavyArmorPiercing(BigDecimal heavyArmorPiercing) {
        this.heavyArmorPiercing = heavyArmorPiercing;
    }

    public BigDecimal getHeavyAttack() {
        return heavyAttack;
    }

    public void setHeavyAttack(BigDecimal heavyAttack) {
        this.heavyAttack = heavyAttack;
    }

    public BigDecimal getTorpedoAttack() {
        return torpedoAttack;
    }

    public void setTorpedoAttack(BigDecimal torpedoAttack) {
        this.torpedoAttack = torpedoAttack;
    }

    public BigDecimal getSubAttack() {
        return subAttack;
    }

    public void setSubAttack(BigDecimal subAttack) {
        this.subAttack = subAttack;
    }

    public BigDecimal getAntiAirAttack() {
        return antiAirAttack;
    }

    public void setAntiAirAttack(BigDecimal antiAirAttack) {
        this.antiAirAttack = antiAirAttack;
    }

    public BigDecimal getArmor() {
        return armor;
    }

    public void setArmor(BigDecimal armor) {
        this.armor = armor;
    }

    public BigDecimal getSurfaceDetection() {
        return surfaceDetection;
    }

    public void setSurfaceDetection(BigDecimal surfaceDetection) {
        this.surfaceDetection = surfaceDetection;
    }

    public BigDecimal getSubDetection() {
        return subDetection;
    }

    public void setSubDetection(BigDecimal subDetection) {
        this.subDetection = subDetection;
    }

    public BigDecimal getSurfaceVisibility() {
        return surfaceVisibility;
    }

    public void setSurfaceVisibility(BigDecimal surfaceVisibility) {
        this.surfaceVisibility = surfaceVisibility;
    }

    public BigDecimal getNavalSpeed() {
        return navalSpeed;
    }

    public void setNavalSpeed(BigDecimal navalSpeed) {
        this.navalSpeed = navalSpeed;
    }

    public BigDecimal getNavalRange() {
        return navalRange;
    }

    public void setNavalRange(BigDecimal navalRange) {
        this.navalRange = navalRange;
    }

    public BigDecimal getReliability() {
        return reliability;
    }

    public void setReliability(BigDecimal reliability) {
        this.reliability = reliability;
    }

    public BigDecimal getHp() {
        return hp;
    }

    public void setHp(BigDecimal hp) {
        this.hp = hp;
    }

    public BigDecimal getFuelConsumption() {
        return fuelConsumption;
    }

    public void setFuelConsumption(BigDecimal fuelConsumption) {
        this.fuelConsumption = fuelConsumption;
    }

    public BigDecimal getBuildCost() {
        return buildCost;
    }

    public void setBuildCost(BigDecimal buildCost) {
        this.buildCost = buildCost;
    }

    public BigInteger getManpower() {
        return manpower;
    }

    public void setManpower(BigInteger manpower) {
        this.manpower = manpower;
    }

    public List<ResourceAmount> getResources() {
        return resources;
    }

    public List<ModuleCountLimit> getModuleLimits() {
        return moduleLimits;
    }

    public Map<String, Slot> getSlots() {
        return slots;
    }

    public Map<String, Module> getModules() {
        return modules;
    }

    @Override
    public int compareTo(ShipHull o) {
        if (archetype != o.archetype) {
            return archetype ? -1 : 1;
        }
        return getId().compareTo(o.getId());
    }

    @Override
    public String toString() {
//        return String.format("%s/%s (parent=%s, dismantleCost=%d, stats=[%s], conversions=[%s], resources=[%s], dismantleCostResources=[%s])", category, getName(), getParentId(),
//                dismantleCost,
//                stats.stream().map(Stat::toString).collect(Collectors.joining(", ")),
//                conversions.stream().map(Conversion::toString).collect(Collectors.joining(", ")),
//                resources.stream().map(ResourceAmount::toString).collect(Collectors.joining(", ")),
//                dismantleCostResources.stream().map(ResourceAmount::toString).collect(Collectors.joining(", "))
//        );
        return String.format("[%s]/%s [%s%s] (parent=%s, year=%d, l=%.02f/%.02f, h=%.02f/%.02f, t=%.02f, s=%.02f, a=%.02f, " +
                        "surf=%.02f/%.02f, sub=%.02f/%.02f, armor=%.02f, hp=%.02f, naval=%.02f/%.02f, r=%+.02f%%, " +
                        "fuel=%.02f, cost=%.02f, mp=%d, %s%s%s)",
                String.join(", ", types),
                getId(),
                archetype ? "A" : ".",
                buildable ? "B" : ".",
                getParentId(),
                year,
                lightAttack, lightArmorPiercing,
                heavyAttack, heavyArmorPiercing,
                torpedoAttack, subAttack, antiAirAttack,
                surfaceDetection, surfaceVisibility,
                subDetection, subAttack,
                armor, hp,
                navalSpeed, navalRange, reliability.multiply(BD_100),
                fuelConsumption, buildCost, manpower,
                resources.stream().map(ResourceAmount::toString).collect(Collectors.joining(", ", "{", "}")),
                upgrades.size() == 0 ? "" : upgrades.stream().collect(Collectors.joining(", ", ", upgrades=(", ")")),
                moduleLimits.size() == 0 ? "" : moduleLimits.stream().map(ModuleCountLimit::toString).collect(Collectors.joining(", ", ", limits=(", ")"))
        );
    }

    /**
     * Used to base ship hull on archetype
     * @param newId
     * @return
     */
    public ShipHull copy(String newId) {
        ShipHull sh = new ShipHull(newId);

        sh.setCategory(getCategory());
        sh.setYear(getYear());
        sh.setInterfaceCategory(getInterfaceCategory());
        sh.getTypes().addAll(getTypes());
        sh.getUpgrades().addAll(getUpgrades());

        sh.setLightArmorPiercing(getLightArmorPiercing());
        sh.setLightAttack(getLightAttack());
        sh.setHeavyArmorPiercing(getHeavyArmorPiercing());
        sh.setHeavyAttack(getHeavyAttack());
        sh.setTorpedoAttack(getTorpedoAttack());
        sh.setSubAttack(getSubAttack());
        sh.setAntiAirAttack(getAntiAirAttack());

        sh.setArmor(getArmor());

        sh.setSurfaceDetection(getSurfaceDetection());
        sh.setSubDetection(getSubDetection());
        sh.setSurfaceVisibility(getSurfaceVisibility());

        sh.setNavalSpeed(getNavalSpeed());
        sh.setNavalRange(getNavalRange());

        sh.setReliability(getReliability());
        sh.setHp(getHp());

        sh.setFuelConsumption(getFuelConsumption());

        sh.setBuildCost(getBuildCost());
        sh.setManpower(getManpower());

        sh.getResources().addAll(getResources());
        sh.getModuleLimits().addAll(getModuleLimits());

        // don't copy slots
//        sh.getSlots().putAll(getSlots());

        sh.getModules().putAll(getModules());

        return sh;
    }

}
