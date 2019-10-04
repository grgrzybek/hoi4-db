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

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import grgr.hoi4db.model.DLC;
import grgr.hoi4db.model.HasName;
import grgr.hoi4db.model.Vanguard;

/**
 * A fleet consists of {@link TaskForce task forces}.
 */
public class Fleet extends HasName implements Comparable<Fleet> {

    private DLC dlc;
    private int year = 1936;

    private String country;

    private Vanguard vanguard;

    private Set<TaskForce> taskForces = new TreeSet<>();

    public Fleet(String name) {
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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Vanguard getVanguard() {
        return vanguard;
    }

    public void setVanguard(Vanguard vanguard) {
        this.vanguard = vanguard;
    }

    public Set<TaskForce> getTaskForces() {
        return taskForces;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Fleet fleet = (Fleet) o;
        return year == fleet.year &&
                dlc == fleet.dlc &&
                country.equals(fleet.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), dlc, year, country);
    }

    @Override
    public int compareTo(Fleet o) {
        if (dlc != o.dlc) {
            return dlc == DLC.MTG ? -1 : 1;
        }
        if (year != o.year) {
            return year - o.year;
        }
        if (!country.equals(o.country)) {
            return country.compareTo(o.country);
        }

        if (vanguard != o.vanguard) {
            if (vanguard == null) {
                return -1;
            }
            if (o.vanguard == null) {
                return 1;
            }
            return vanguard.ordinal() - o.vanguard.ordinal();
        }

        return getName().compareTo(o.getName());
    }

    @Override
    public String toString() {
        return String.format("%s, %d: %s (task forces: %d)%s", dlc, year, getName(), taskForces.size(), vanguard != null ? " [" + vanguard + "]" : "");
    }

}
