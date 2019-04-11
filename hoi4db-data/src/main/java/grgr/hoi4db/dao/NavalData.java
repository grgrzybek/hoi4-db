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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import grgr.hoi4db.databind.Hoi4DbNodeFactory;
import grgr.hoi4db.dataformat.Hoi4DbFactory;
import grgr.hoi4db.model.Conversion;
import grgr.hoi4db.model.Resource;
import grgr.hoi4db.model.ResourceAmount;
import grgr.hoi4db.model.Stat;
import grgr.hoi4db.model.naval.Module;
import grgr.hoi4db.model.naval.ModuleCategory;
import grgr.hoi4db.model.naval.ShipHull;

/**
 * Accesses naval-related data
 */
public class NavalData {

    private static String[] MODULE_DEFINITIONS = new String[] {
            "common/units/equipment/modules/00_ship_modules.txt"
    };

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

        withFileSet(HULL_DEFINITIONS, (tree) -> {
            tree.get("equipments").fields().forEachRemaining(e -> {
                JsonNode v = e.getValue();
                System.out.printf("%s: %s, %d\n", e.getKey(), v.get("type"), v.get("year").bigIntegerValue());
            });
        });

        return hulls;
    }

    public List<Module> modules() throws IOException {
        Set<Module> modules = new TreeSet<>();
        Map<String, Module> byId = new HashMap<>();

        withFileSet(MODULE_DEFINITIONS, (tree) -> {
            tree.get("equipment_modules").fields().forEachRemaining(e -> {
                JsonNode v = e.getValue();
                Module m = new Module(e.getKey());
                if (v.has("gfx")) {
                    m.setSfx(v.get("gfx").asText());
                }
                if (v.has("sfx")) {
                    m.setSfx(v.get("sfx").asText());
                }
                if (v.has("parent")) {
                    m.setParentId(v.get("parent").asText());
                }
                if (v.has("dismantle_cost_ic")) {
                    m.setDismantleCost(v.get("dismantle_cost_ic").bigIntegerValue());
                }
                m.setCategory(ModuleCategory.byName(v.get("category").asText()));

                processStats(m, v);
                processConversions(m, v);
                processResources(m, v);
                processDismantleCostResources(m, v);

                byId.put(m.getId(), m);
                modules.add(m);
            });
        });

        modules.forEach(m -> {
            if (m.getParentId() != null && byId.containsKey(m.getParentId())) {
                m.setParent(byId.get(m.getParentId()));
            }
        });

        return new LinkedList<>(modules);
    }

    private void processStats(Module m, JsonNode v) {
        if (v.has("add_stats")) {
            processStats(m, v.get("add_stats"), Stat::addedValue);
        }
        if (v.has("add_average_stats")) {
            processStats(m, v.get("add_average_stats"), Stat::addedAverageValue);
        }
        if (v.has("multiply_stats")) {
            processStats(m, v.get("multiply_stats"), Stat::percentageValue);
        }
    }

    private void processStats(Module m, JsonNode v, BiFunction<String, Number, Stat> producer) {
        if (v.isObject()) {
            v.fields().forEachRemaining(e -> {
                m.getStats().add(producer.apply(e.getKey(), e.getValue().numberValue()));
            });
        } else if (v.isArray()) {
            v.elements().forEachRemaining(e -> {
                e.fields().forEachRemaining(e2 -> {
                    m.getStats().add(producer.apply(e2.getKey(), e2.getValue().numberValue()));
                });
            });
        }
    }

    private void processConversions(Module m, JsonNode v) {
        JsonNode value = v.get("can_convert_from");
        if (value != null) {
            if (value.isObject()) {
                processConversion(m, value);
            } else if (value.isArray()) {
                value.elements().forEachRemaining(e -> {
                    processConversion(m, e);
                });
            }
        }
    }

    private void processConversion(Module m, JsonNode v) {
        final Conversion[] c = new Conversion[1];
        if (v.has("module_category")) {
            c[0] = new Conversion(ModuleCategory.byName(v.get("module_category").asText()),
                    v.get("convert_cost_ic").numberValue());
        } else {
            c[0] = new Conversion(v.get("module").asText(),
                    v.get("convert_cost_ic").numberValue());
        }
        if (v.has("convert_cost_resources")) {
            v.get("convert_cost_resources").fields().forEachRemaining(e -> {
                c[0].getResources().add(new ResourceAmount(Resource.byName(e.getKey()), e.getValue().bigIntegerValue()));
            });
        }
        m.getConversions().add(c[0]);
    }

    private void processResources(Module m, JsonNode v) {
        JsonNode value = v.get("build_cost_resources");
        if (value != null) {
            value.fields().forEachRemaining(e -> {
                m.getResources().add(new ResourceAmount(Resource.byName(e.getKey()), e.getValue().bigIntegerValue()));
            });
        }
    }

    private void processDismantleCostResources(Module m, JsonNode v) {
        JsonNode value = v.get("dismantle_cost_resources");
        if (value != null) {
            value.fields().forEachRemaining(e -> {
                m.getDismantleCostResources().add(new ResourceAmount(Resource.byName(e.getKey()), e.getValue().bigIntegerValue()));
            });
        }
    }

    private void withFileSet(String[] filenames, Consumer<JsonNode> processor) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new Hoi4DbFactory());
        mapper.setNodeFactory(new Hoi4DbNodeFactory());

        for (String fileName : filenames) {
            File file = new File(hoi4Dir, fileName);
            JsonNode tree = mapper.readTree(file);
            processor.accept(tree);
        }
    }

}
