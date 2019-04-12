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

import grgr.hoi4db.dao.NavalData;
import grgr.hoi4db.model.naval.Module;
import grgr.hoi4db.model.naval.ShipHull;
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
                System.out.printf(" - %s: %s\n", slot, m == null ? "<empty>" : m);
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

}
