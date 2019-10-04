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
package grgr.hoi4db;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import grgr.hoi4db.dao.CountryData;
import grgr.hoi4db.dao.NavalData;
import grgr.hoi4db.model.naval.Module;
import grgr.hoi4db.model.naval.ShipHull;
import grgr.hoi4db.model.naval.ShipHullVariant;
import grgr.hoi4db.model.naval.Slot;
import grgr.hoi4db.model.upgrades.NavalUpgrade;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelTest {

    public static Logger LOG = LoggerFactory.getLogger(SteamTest.class);

    private static final String DIR = System.getProperty("steam.dir");
    private static File HOI4_DIR;

    @BeforeAll
    public static void init() {
        HOI4_DIR = new File(DIR, "Hearts of Iron IV");
        if (!HOI4_DIR.isDirectory()) {
            throw new IllegalArgumentException("Can't locate Hearts of Iron IV directory");
        }
    }

    @Test
    public void readShipHulls() throws IOException {
        NavalData nd = new NavalData(HOI4_DIR);
        for (ShipHull sh : nd.hulls()) {
            System.out.println(sh);
            System.out.println("slots:");
            sh.getSlots().forEach((id, slot) -> {
                Module m = sh.getModules().get(id);
                System.out.printf(" %s %s: %s\n", m == Module.EMPTY ? "-" : (m.isUnknown() ? "?" : "+"),
                        slot == Slot.INHERITED ? id + " (inherited)" : slot.getId(), m.getId());
            });
        }
    }

    @Test
    public void readNavalModules() throws IOException {
        NavalData nd = new NavalData(HOI4_DIR);
        for (Module m : nd.modules()) {
            System.out.println(m);
        }
    }

    @Test
    public void readNavalUpgrades() throws IOException {
        NavalData nd = new NavalData(HOI4_DIR);
        for (NavalUpgrade nu : nd.upgrades()) {
            System.out.println(nu);
        }
    }

    @Test
    public void readCountryShipVariants() throws IOException {
        CountryData cd = new CountryData(HOI4_DIR, new NavalData(HOI4_DIR));
//        cd.variants("ENG").forEach(v -> {
//            System.out.println(v.toString());
//        });

        Map<String, ShipHullVariant> byName = new HashMap<>();
        Map<String, ShipHullVariant> byCountryName = new HashMap<>();
        Map<String, ShipHullVariant> byCountryYearName = new HashMap<>();
        Map<String, ShipHullVariant> byCountryDlcName = new HashMap<>();
        Map<String, ShipHullVariant> byCountryDlcNameType = new HashMap<>();
        Map<String, ShipHullVariant> byCountryYearDlcNameType = new HashMap<>();

        // just checking where can we expect uniqueness
        cd.allVariants().forEach((c, variants) -> {
            System.out.println("--- " + c);
            variants.forEach(v -> {
//                String k1 = v.getName();
//                String k2 = String.format("%s|%s", v.getCountry(), v.getName());
//                String k3 = String.format("%s|%d|%s", v.getCountry(), v.getYear(), v.getName());
//                String k4 = String.format("%s|%s|%s", v.getCountry(), v.getDlc(), v.getName());
//                String k5 = String.format("%s|%s|%s|%s", v.getCountry(), v.getDlc(), v.getName(), v.getType().getId());
                String k6 = String.format("%s|%d|%s|%s|%s", v.getCountry(), v.getYear(), v.getDlc(), v.getName(), v.getType().getId());

//                ShipHullVariant v1 = byName.put(k1, v);
//                ShipHullVariant v2 = byCountryName.put(k2, v);
//                ShipHullVariant v3 = byCountryYearName.put(k3, v);
//                ShipHullVariant v4 = byCountryDlcName.put(k4, v);
//                ShipHullVariant v5 = byCountryDlcNameType.put(k5, v);
                ShipHullVariant v6 = byCountryYearDlcNameType.put(k6, v);
//                if (v1 != null) {
//                    throw new IllegalStateException("(" + v + ") there's already a name variant " + v1);
//                }
//                if (v2 != null) {
//                    throw new IllegalStateException("(" + v + ") there's already a country/name variant " + v2);
//                }
//                if (v3 != null) {
//                    throw new IllegalStateException("(" + v + ") there's already a country/year/name variant " + v3);
//                }
//                if (v4 != null) {
//                    throw new IllegalStateException("(" + v + ") there's already a country/dlc/name variant " + v4);
//                }
//                if (v5 != null) {
//                    throw new IllegalStateException("(" + v + ") there's already a country/dlc/name/type variant " + v5);
//                }
                if (v6 != null) {
                    throw new IllegalStateException("(" + v + ") there's already a country/year/dlc/name/type variant " + v6);
                }
                System.out.println(v.toString());
            });
        });
    }

    @Test
    public void readFleets() throws IOException {
        CountryData cd = new CountryData(HOI4_DIR, new NavalData(HOI4_DIR));
        cd.fleets("ENG").forEach(v -> {
            System.out.println(v.toString());
        });
//        cd.allFleets().forEach((c, variants) -> {
//            System.out.println("--- " + c);
//            variants.forEach(v -> {
//                System.out.println(v.toString());
//            });
//        });
    }

}
