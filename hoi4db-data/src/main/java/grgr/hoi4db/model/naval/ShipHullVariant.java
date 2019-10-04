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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import grgr.hoi4db.model.DLC;
import grgr.hoi4db.model.HasName;
import grgr.hoi4db.model.upgrades.NavalUpgrade;

/**
 * <p>A particular variant/design of a {@link ShipHull} defined in {@code create_equipment_variant} objects in
 * files like {@code /history/countries/ENG - Britain.txt}. It's generally a {@link ShipHull} with specific set
 * of {@link Module modules}.</p>
 * <p>After checking, the unique key is {@code country:dlc:year:type:name}</p>
 */
public class ShipHullVariant extends HasName implements Comparable<ShipHullVariant> {

    private DLC dlc;
    private int year = 1936;

    private ShipCategory category;

    private String country;

    // name_group, like "ENG_CLAA_HISTORICAL"
    private String groupName;

    // a ShipHull which is changed by adding modules (in MtG) or naval upgrades (Vanilla)
    private ShipHull type;

    // MtG: mapping from slot Id to actual module used in given slot - completely overrides modules from the origin hull
    // initially the set of modules match the template type
    private Map<String, Module> modules = new LinkedHashMap<>();

    // Vanilla: different numbers of naval upgrades
    private Map<NavalUpgrade, Integer> upgrades = new LinkedHashMap<>();

    public ShipHullVariant(String name) {
        super(name);
    }

    public DLC getDlc() {
        return dlc;
    }

    public void setDlc(DLC dlc) {
        this.dlc = dlc;
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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public ShipHull getType() {
        return type;
    }

    public void setType(ShipHull type) {
        this.type = type;
    }

    public Map<String, Module> getModules() {
        return modules;
    }

    public Map<NavalUpgrade, Integer> getUpgrades() {
        return upgrades;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ShipHullVariant that = (ShipHullVariant) o;
        return year == that.year &&
                dlc == that.dlc &&
                country.equals(that.country) &&
                type.getId().equals(that.type.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), dlc, year, country, type, type.getId());
    }

    @Override
    public int compareTo(ShipHullVariant o) {
        if (!country.equals(o.country)) {
            return country.compareTo(o.country);
        }
        if (dlc != o.dlc) {
            return dlc == DLC.MTG ? -1 : 1;
        }
        if (year != o.year) {
            return year - o.year;
        }
        // category is equivalent of type.getId()
        if (category != o.category) {
            return category.ordinal() - o.category.ordinal();
        }

        return getName().compareTo(o.getName());
    }

    @Override
    public String toString() {
        return String.format("%s, %d: [%s/%s]: %s", dlc, year, category, groupName, getName());
    }

}
