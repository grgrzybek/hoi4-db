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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import grgr.hoi4db.databind.Hoi4DbNodeFactory;
import grgr.hoi4db.dataformat.Hoi4DbFactory;
import grgr.hoi4db.model.Constraint;
import grgr.hoi4db.model.Conversion;
import grgr.hoi4db.model.Resource;
import grgr.hoi4db.model.ResourceAmount;
import grgr.hoi4db.model.Stat;
import grgr.hoi4db.model.naval.Module;
import grgr.hoi4db.model.naval.ModuleCategory;
import grgr.hoi4db.model.naval.ModuleCountLimit;
import grgr.hoi4db.model.naval.ShipHull;
import grgr.hoi4db.model.naval.Slot;
import grgr.hoi4db.model.upgrades.NavalUpgrade;

/**
 * Accesses naval-related data
 */
public class NavalData {

    private static String[] MODULE_DEFINITIONS = new String[] {
            "common/units/equipment/modules/00_ship_modules.txt"
    };

    private static String[] UPGRADES = new String[] {
            "common/units/equipment/upgrades/naval_upgrades.txt"
    };

    private static String[] HULL_DEFINITIONS = new String[] {
            "common/units/equipment/ship_hull_light.txt",
//            "common/units/equipment/ship_hull_cruiser.txt",
//            "common/units/equipment/ship_hull_heavy.txt",
//            "common/units/equipment/ship_hull_carrier.txt",
//            "common/units/equipment/ship_hull_submarine.txt"
    };

    private File hoi4Dir;

    public NavalData(File hoi4Dir) {
        this.hoi4Dir = hoi4Dir;
    }

    public List<ShipHull> hulls() throws IOException {
        Set<ShipHull> hulls = new TreeSet<>();
        Map<String, ShipHull> byId = new HashMap<>();

        // take modules - we'll use them for default_modules
        Map<String, Module> modules = new HashMap<>();
        modules().forEach(m -> {
            modules.put(m.getId(), m);
        });

        // first collect archetypes as template
        withFileSet(HULL_DEFINITIONS, (tree) -> {
            tree.get("equipments").fields().forEachRemaining(e -> {
                JsonNode v = e.getValue();
                if (!(v.has("is_archetype") && v.get("is_archetype").asBoolean())) {
                    return;
                }
                ShipHull sh = new ShipHull(e.getKey());
                sh.setArchetype(true);
                sh.setBuildable(false);

                processShipHull(sh, v, modules);
                byId.put(sh.getId(), sh);
            });
        });

        // and now non-archetypes
        withFileSet(HULL_DEFINITIONS, (tree) -> {
            tree.get("equipments").fields().forEachRemaining(e -> {
                JsonNode v = e.getValue();
                if (v.has("is_archetype") && v.get("is_archetype").asBoolean()) {
                    return;
                }
                ShipHull sh = v.has("archetype")
                        ? byId.get(v.get("archetype").asText()).copy(e.getKey())
                        : new ShipHull(e.getKey());
                sh.setArchetype(false);
                sh.setBuildable(true);

                processShipHull(sh, v, modules);
                byId.put(sh.getId(), sh);
                hulls.add(sh);
            });
        });

        hulls.forEach(sh -> {
            if (sh.getParentId() != null && byId.containsKey(sh.getParentId())) {
                sh.setParent(byId.get(sh.getParentId()));
            }
        });

        // this list won't contain archetypes
        return new LinkedList<>(hulls);
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

    public List<NavalUpgrade> upgrades() throws IOException {
        Set<NavalUpgrade> upgrades = new TreeSet<>();

        withFileSet(UPGRADES, (tree) -> {
            tree.get("upgrades").fields().forEachRemaining(e -> {
                JsonNode v = e.getValue();
                NavalUpgrade upgrade = new NavalUpgrade(e.getKey());
                if (v.has("max_level")) {
                    upgrade.setMaxLevel(v.get("max_level").asInt());
                }
                if (v.has("lg_attack")) {
                    upgrade.setLightAttack(v.get("lg_attack").decimalValue());
                }
                if (v.has("lg_armor_piercing")) {
                    upgrade.setLightArmorPiercing(v.get("lg_armor_piercing").decimalValue());
                }
                if (v.has("hg_attack")) {
                    upgrade.setHeavyAttack(v.get("hg_attack").decimalValue());
                }
                if (v.has("hg_armor_piercing")) {
                    upgrade.setHeavyArmorPiercing(v.get("hg_armor_piercing").decimalValue());
                }
                if (v.has("torpedo_attack")) {
                    upgrade.setTorpedoAttack(v.get("torpedo_attack").decimalValue());
                }
                if (v.has("sub_attack")) {
                    upgrade.setSubAttack(v.get("sub_attack").decimalValue());
                }
                if (v.has("anti_air_attack")) {
                    upgrade.setAntiAirAttack(v.get("anti_air_attack").decimalValue());
                }
                if (v.has("armor_value")) {
                    upgrade.setArmor(v.get("armor_value").decimalValue());
                }
                if (v.has("sub_detection")) {
                    upgrade.setSubDetection(v.get("sub_detection").decimalValue());
                }
                if (v.has("sub_visibility")) {
                    upgrade.setSubVisibility(v.get("sub_visibility").decimalValue());
                }
                if (v.has("naval_speed")) {
                    upgrade.setNavalSpeed(v.get("naval_speed").decimalValue());
                }
                if (v.has("naval_range")) {
                    upgrade.setNavalRange(v.get("naval_range").decimalValue());
                }
                if (v.has("reliability")) {
                    upgrade.setReliability(v.get("reliability").decimalValue());
                }
                if (v.has("max_strength")) {
                    upgrade.setHp(v.get("max_strength").decimalValue());
                }
                if (v.has("carrier_size")) {
                    upgrade.setCarrierSize(v.get("carrier_size").decimalValue());
                }
                upgrades.add(upgrade);
            });
        });

        return new LinkedList<>(upgrades);
    }

    private void processShipHull(ShipHull sh, JsonNode v, Map<String, Module> modules) {
        if (v.has("parent")) {
            sh.setParentId(v.get("parent").asText());
        }
        if (v.has("year")) {
            sh.setYear(v.get("year").asInt());
        }
        if (v.has("type")) {
            sh.getTypes().clear();
            if (v.get("type").isArray()) {
                v.get("type").elements().forEachRemaining(t -> {
                    sh.getTypes().add(t.asText());
                });
            } else {
                sh.getTypes().add(v.get("type").asText());
            }
        }
        if (v.has("upgrades")) {
            sh.getUpgrades().clear();
            v.get("upgrades").elements().forEachRemaining(t -> {
                sh.getUpgrades().add(t.asText());
            });
        }
        if (v.has("interface_category")) {
            sh.setInterfaceCategory(v.get("interface_category").asText());
        }
        if (v.has("lg_attack")) {
            sh.setLightAttack(v.get("lg_attack").decimalValue());
        }
        if (v.has("lg_armor_piercing")) {
            sh.setLightArmorPiercing(v.get("lg_armor_piercing").decimalValue());
        }
        if (v.has("hg_attack")) {
            sh.setHeavyAttack(v.get("hg_attack").decimalValue());
        }
        if (v.has("hg_armor_piercing")) {
            sh.setHeavyArmorPiercing(v.get("hg_armor_piercing").decimalValue());
        }
        if (v.has("torpedo_attack")) {
            sh.setTorpedoAttack(v.get("torpedo_attack").decimalValue());
        }
        if (v.has("sub_attack")) {
            sh.setSubAttack(v.get("sub_attack").decimalValue());
        }
        if (v.has("anti_air_attack")) {
            sh.setAntiAirAttack(v.get("anti_air_attack").decimalValue());
        }
        if (v.has("armor_value")) {
            sh.setArmor(v.get("armor_value").decimalValue());
        }
        if (v.has("surface_detection")) {
            sh.setSurfaceDetection(v.get("surface_detection").decimalValue());
        }
        if (v.has("sub_detection")) {
            sh.setSubDetection(v.get("sub_detection").decimalValue());
        }
        if (v.has("surface_visibility")) {
            sh.setSurfaceVisibility(v.get("surface_visibility").decimalValue());
        }
        if (v.has("naval_speed")) {
            sh.setNavalSpeed(v.get("naval_speed").decimalValue());
        }
        if (v.has("naval_range")) {
            sh.setNavalRange(v.get("naval_range").decimalValue());
        }
        if (v.has("reliability")) {
            sh.setReliability(v.get("reliability").decimalValue());
        }
        if (v.has("max_strength")) {
            sh.setHp(v.get("max_strength").decimalValue());
        }
        if (v.has("fuel_consumption")) {
            sh.setFuelConsumption(v.get("fuel_consumption").decimalValue());
        }
        if (v.has("build_cost_ic")) {
            sh.setBuildCost(v.get("build_cost_ic").decimalValue());
        }
        if (v.has("manpower")) {
            sh.setManpower(v.get("manpower").bigIntegerValue());
        }
        JsonNode value = v.get("resources");
        if (value != null) {
            sh.getResources().clear();
            value.fields().forEachRemaining(e -> {
                sh.getResources().add(new ResourceAmount(Resource.byName(e.getKey()), e.getValue().bigIntegerValue()));
            });
        }
        if (v.has("module_count_limit")) {
            v.get("module_count_limit").elements().forEachRemaining(limit -> {
                ModuleCountLimit mcl = new ModuleCountLimit();
                Constraint c = new Constraint("count", limit.get("count").asText());
                mcl.setLimit(c);
                mcl.setModuleCategory(ModuleCategory.byName(limit.get("category").asText()));
                sh.getModuleLimits().add(mcl);
            });
        }
        if (v.has("module_slots")) {
            Map<String, Slot> slots = new LinkedHashMap<>();
            Map<String, String> refs = new HashMap<>();
            JsonNode declaredSlots = v.get("module_slots");

            // first, inherit
            Map<String, Slot> inherited = new LinkedHashMap<>(sh.getSlots());
            sh.getSlots().clear();

            if (declaredSlots != null && declaredSlots.isTextual() && "inherit".equals(declaredSlots.asText())) {
                sh.getSlots().putAll(inherited);
            } else if (declaredSlots != null) {
                declaredSlots.fields().forEachRemaining(s -> {
                    Slot slot = new Slot(s.getKey());
                    if (s.getValue().isTextual()) {
                        if (s.getValue().asText().equals("inherit")) {
                            // inherited from parent hull
                            slot = inherited.get(s.getKey());
                            if (slot == null) {
                                throw new IllegalArgumentException("Can't find slot with id=" + s.getKey() + " in inherited slot definitions");
                            }
                        } else {
                            // a reference to slot from current hull - to be resolved later
                            refs.put(s.getKey(), s.getValue().asText());
                        }
                    } else if (s.getValue().isObject()) {
                        // overriden
                        final Slot sl = new Slot(s.getKey());
                        s.getValue().get("allowed_module_categories").elements().forEachRemaining(e -> {
                            sl.getCategoriesAllowed().add(ModuleCategory.byName(e.asText()));
                        });
                        sl.setRequired(s.getValue().get("required").asBoolean());
                        slot = sl;
                    }
                    slots.put(slot.getId(), slot);
                });
                refs.forEach((k, ref) -> {
                    slots.get(k).getCategoriesAllowed().addAll(slots.get(ref).getCategoriesAllowed());
                });
            }
            sh.getSlots().putAll(slots);
        }
        if (v.has("default_modules")) {
            v.get("default_modules").fields().forEachRemaining(e -> {
                String slotId = e.getKey();
                if (!sh.getSlots().containsKey(slotId)) {
                    throw new IllegalArgumentException("Can't find slot ID \"" + slotId + "\" for ship hull \"" + sh.getId() + "\"");
                }
                String moduleId = e.getValue().asText();
                Module m = modules.get(moduleId);
                if ("empty".equals(moduleId)) {
                    // slot is not used
                    sh.getModules().put(e.getKey(), null);
                } else if (m == null) {
                    throw new IllegalArgumentException("Can't find module \"" + moduleId + "\"");
                } else {
                    Slot slot = sh.getSlots().get(slotId);
                    Optional<ModuleCategory> c = slot.getCategoriesAllowed().stream().filter(mc -> mc.equals(m.getCategory())).findAny();
                    if (!c.isPresent()) {
                        throw new IllegalArgumentException("Module \"" + moduleId + "\" with category \"" + m.getCategory() + "\" can't be installed in slot " + slot);
                    }
                    sh.getModules().put(e.getKey(), m);
                }
            });
        }
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
