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
package grgr.hoi4db.model.upgrades;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import grgr.hoi4db.model.HasId;

import static grgr.hoi4db.model.Stat.BD_100;

/**
 * A naval upgrade in Vanilla. All values are percentage changes.
 */
public class NavalUpgrade extends HasId implements Comparable<NavalUpgrade> {

    private int maxLevel;

    private BigDecimal lightArmorPiercing;
    private BigDecimal lightAttack;
    private BigDecimal heavyArmorPiercing;
    private BigDecimal heavyAttack;
    private BigDecimal torpedoAttack;
    private BigDecimal subAttack;
    private BigDecimal antiAirAttack;

    private BigDecimal armor;

    private BigDecimal subDetection;
    private BigDecimal subVisibility;

    private BigDecimal navalSpeed;
    private BigDecimal navalRange;

    private BigDecimal reliability;
    private BigDecimal hp;

    private BigDecimal carrierSize;

    public NavalUpgrade(String id) {
        super(id);
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
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

    public BigDecimal getSubDetection() {
        return subDetection;
    }

    public void setSubDetection(BigDecimal subDetection) {
        this.subDetection = subDetection;
    }

    public BigDecimal getSubVisibility() {
        return subVisibility;
    }

    public void setSubVisibility(BigDecimal subVisibility) {
        this.subVisibility = subVisibility;
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

    public BigDecimal getCarrierSize() {
        return carrierSize;
    }

    public void setCarrierSize(BigDecimal carrierSize) {
        this.carrierSize = carrierSize;
    }

    @Override
    public int compareTo(NavalUpgrade o) {
        return getId().compareTo(o.getId());
    }

    @Override
    public String toString() {
        List<String> values = new LinkedList<>();
        if (lightAttack != null) {
            values.add(String.format("l=%+.02f%%/%+.02f%%", lightAttack.multiply(BD_100), lightArmorPiercing.multiply(BD_100)));
        }
        if (heavyAttack != null) {
            values.add(String.format("h=%+.02f%%/%+.02f%%", heavyAttack.multiply(BD_100), heavyArmorPiercing.multiply(BD_100)));
        }
        if (torpedoAttack != null) {
            values.add(String.format("t=%+.02f%%", torpedoAttack.multiply(BD_100)));
        }
        if (subAttack != null) {
            values.add(String.format("s=%+.02f%%", subAttack.multiply(BD_100)));
        }
        if (antiAirAttack != null) {
            values.add(String.format("a=%+.02f%%", antiAirAttack.multiply(BD_100)));
        }
        if (subDetection != null) {
            values.add(String.format("subd=%+.02f%%", subDetection.multiply(BD_100)));
        }
        if (subVisibility != null) {
            values.add(String.format("subv=%+.02f%%", subVisibility.multiply(BD_100)));
        }
        if (navalSpeed != null) {
            values.add(String.format("nspeed=%+.02f%%", navalSpeed.multiply(BD_100)));
        }
        if (navalRange != null) {
            values.add(String.format("nrange=%+.02f%%", navalRange.multiply(BD_100)));
        }
        if (armor != null) {
            values.add(String.format("armor=%+.02f%%", armor.multiply(BD_100)));
        }
        if (hp != null) {
            values.add(String.format("hp=%+.02f%%", hp.multiply(BD_100)));
        }
        if (reliability != null) {
            values.add(String.format("r=%+.02f%%", reliability.multiply(BD_100)));
        }
        if (carrierSize != null) {
            values.add(String.format("carrier size=%+.02f%%", carrierSize.multiply(BD_100)));
        }
        return String.format("%s(%d) (%s)",
                getId(), maxLevel,
                String.join(", ", values)
        );
    }

}
