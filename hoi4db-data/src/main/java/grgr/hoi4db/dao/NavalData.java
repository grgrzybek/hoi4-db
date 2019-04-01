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
package grgr.hoi4db.dao;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import grgr.hoi4db.databind.Hoi4DbNodeFactory;
import grgr.hoi4db.dataformat.Hoi4DbFactory;
import grgr.hoi4db.model.naval.ShipHull;

/**
 * Accesses naval-related data
 */
public class NavalData {

    private static String[] HULL_DEFINITIONS = new String[] {
            "common/units/equipment/ship_hull_light.txt",
            "common/units/equipment/ship_hull_cruiser.txt",
            "common/units/equipment/ship_hull_heavy.txt",
            "common/units/equipment/ship_hull_carrier.txt",
            "common/units/equipment/ship_hull_submarine.txt",
    };

    private File hoi4Dir;

    public NavalData(File hoi4Dir) {
        this.hoi4Dir = hoi4Dir;
    }

    public List<ShipHull> hulls() throws IOException {
        List<ShipHull> hulls = new LinkedList<>();

        ObjectMapper mapper = new ObjectMapper(new Hoi4DbFactory());
        mapper.setNodeFactory(new Hoi4DbNodeFactory());

        for (String fileName : HULL_DEFINITIONS) {
            File file = new File(hoi4Dir, fileName);
            JsonNode tree = mapper.readTree(file);
            tree.get("equipments").fields().forEachRemaining(e -> {
                JsonNode v = e.getValue();
                System.out.printf("%s: %s, %d\n", e.getKey(), v.get("type"), v.get("year").bigIntegerValue());
            });
        }

        return hulls;
    }

}
