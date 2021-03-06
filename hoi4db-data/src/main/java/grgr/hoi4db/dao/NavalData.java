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

import com.fasterxml.jackson.databind.JsonNode;
import grgr.hoi4db.model.Constraint;
import grgr.hoi4db.model.Conversion;
import grgr.hoi4db.model.Resource;
import grgr.hoi4db.model.ResourceAmount;
import grgr.hoi4db.model.Stat;
import grgr.hoi4db.model.naval.Module;
import grgr.hoi4db.model.naval.ModuleCategory;
import grgr.hoi4db.model.naval.ModuleCountLimit;
import grgr.hoi4db.model.naval.ShipCategory;
import grgr.hoi4db.model.naval.ShipHull;
import grgr.hoi4db.model.naval.ShipHullCategory;
import grgr.hoi4db.model.naval.Slot;
import grgr.hoi4db.model.upgrades.NavalUpgrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static grgr.hoi4db.dao.Utils.asList;
import static grgr.hoi4db.dao.Utils.withFileSet;

/**
 * Accesses naval-related data
 */
public class NavalData {

    public static Logger LOG = LoggerFactory.getLogger(NavalData.class);

    private static String[] MODULE_DEFINITIONS = new String[] {
            "common/units/equipment/modules/00_ship_modules.txt"
    };

    private static String[] UPGRADES = new String[] {
            "common/units/equipment/upgrades/naval_upgrades.txt"
    };

    private static String[] HULL_DEFINITIONS = new String[] {
            "common/units/equipment/ship_hull_light.txt",
            "common/units/equipment/ship_hull_cruiser.txt",
            "common/units/equipment/ship_hull_heavy.txt",
            "common/units/equipment/ship_hull_carrier.txt",
            "common/units/equipment/ship_hull_submarine.txt"
    };

    private File hoi4Dir;

    public NavalData(File hoi4Dir) {
        this.hoi4Dir = hoi4Dir;
    }

    /**
     * List all hull definitions. All countries use these definitions as ships that can be built
     * (most are not complete), but some countries may add country-specific models.
     * @return
     * @throws IOException
     */
    public List<ShipHull> hulls() {
        Set<ShipHull> hulls = new TreeSet<>();
        Map<String, ShipHull> byId = new HashMap<>();

        // get modules - we'll use them for default_modules
        Map<String, Module> modules = new HashMap<>();
        modules().forEach(m -> {
            modules.put(m.getId(), m);
        });

        // first collect archetypes as template for buildable models
        withFileSet(hoi4Dir, HULL_DEFINITIONS, (file, tree) -> {
            ShipHullCategory hullCategory = ShipHullCategory.fromHullFile(file.getName());

            tree.get("equipments").fields().forEachRemaining(e -> {
                JsonNode v = e.getValue();
                if (!(v.has("is_archetype") && v.get("is_archetype").asBoolean())) {
                    return;
                }
                ShipHull sh = new ShipHull(e.getKey());
                sh.setArchetype(true);
                sh.setBuildable(false);

                sh.setHullCategory(hullCategory);

                processShipHull(sh, v);
                processShipHullModuleSlots(sh, v, modules);

                byId.put(sh.getId(), sh);
            });
        });

        // and now non-archetypes - all values except for module_slots are taken from archetype
        // module_slots may be taken from parent models (using "inherit")
        // TODO: seems like "type" comes from parent instead...
        // also ship_hull_cruiser_panzerschiff gets type=screen from archetype, as it has no parent
        withFileSet(hoi4Dir, HULL_DEFINITIONS, (file, tree) -> {
            ShipHullCategory hullCategory = ShipHullCategory.fromHullFile(file.getName());

            tree.get("equipments").fields().forEachRemaining(e -> {
                JsonNode v = e.getValue();
                if (v.has("is_archetype") && v.get("is_archetype").asBoolean()) {
                    return;
                }
                // assumes ordered declaration...
                ShipHull sh = null;
                /*if (v.has("parent")) {
                    sh = byId.get(v.get("parent").asText()).copy(e.getKey());
                } else */
                if (v.has("archetype")) {
                    sh = byId.get(v.get("archetype").asText()).copy(e.getKey());
                } else {
                    sh = new ShipHull(e.getKey());
                }

                sh.setArchetype(false);
                sh.setBuildable(true);

                sh.setHullCategory(hullCategory);

                processShipHull(sh, v);
                processShipHullModuleSlots(sh, v, modules);

                byId.put(sh.getId(), sh);
                hulls.add(sh);
            });
        });

        hulls.forEach(sh -> {
            if (sh.getParentId() != null && byId.containsKey(sh.getParentId())) {
                sh.setParent(byId.get(sh.getParentId()));
            }
        });

        // reorganize module_slots from archetype and parent hulls
        Map<String, ShipHull> toProcess = new HashMap<>(byId);

        // no need to process hulls where no slot is inherited
        toProcess.entrySet().removeIf(e -> !e.getValue().getSlots().isEmpty()
                && e.getValue().getSlots().values().stream().noneMatch(s -> s == Slot.INHERITED));

        while (!toProcess.isEmpty()) {
            ShipHull sh = toProcess.values().iterator().next();
            // start from highest, unprocessed parent
            while (sh.getParentId() != null && toProcess.containsKey(sh.getParentId())) {
                sh = sh.getParent();
            }
            String parentId = null;
            if (sh.getParentId() != null) {
                parentId = sh.getParentId();
            } else if (sh.getArchetypeId() != null) {
                parentId = sh.getArchetypeId();
            } else {
                throw new IllegalStateException("Can't determine slots for ship hull \"" + sh.getId() + "\"");
            }
            final ShipHull parent = byId.get(parentId);
            if (sh.getSlots().isEmpty()) {
                // copy from parent, because of module_slots=inherit
                sh.getSlots().putAll(parent.getSlots());
            } else {
                // copy only inherited ones
                Map<String, Slot> newSlots = new LinkedHashMap<>();
                sh.getSlots().forEach((id, slot) -> {
                    newSlots.put(id, slot == Slot.INHERITED ? parent.getSlots().get(id) : slot);
                });
                sh.getSlots().clear();
                sh.getSlots().putAll(newSlots);
            }
            toProcess.remove(sh.getId());
        }

        // and process default_modules - even for archetype
        withFileSet(hoi4Dir, HULL_DEFINITIONS, (file, tree) -> {
            tree.get("equipments").fields().forEachRemaining(e1 -> {
                JsonNode v = e1.getValue();
                ShipHull sh = byId.get(e1.getKey());

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
                            sh.getModules().put(e.getKey(), Module.EMPTY);
                        } else if (m == null) {
                            LOG.error("Can't find module \"" + moduleId + "\" for slot \"" + slotId + "\" in ship hull \"" + sh.getId() + "\"");
                            sh.getModules().put(e.getKey(), Module.unknown(moduleId));
                        } else {
                            Slot slot = sh.getSlots().get(slotId);
                            Optional<ModuleCategory> c = slot.getCategoriesAllowed().stream().filter(mc -> mc.equals(m.getCategory())).findAny();
                            if (!c.isPresent()) {
                                throw new IllegalArgumentException("Module \"" + moduleId + "\" with category \"" + m.getCategory() + "\" can't be installed in slot " + slot + " of hull " + sh.getId());
                            }
                            sh.getModules().put(e.getKey(), m);
                        }
                    });
                }
            });
        });

        // reorganize default_modules from archetype and parent hulls
        toProcess = new HashMap<>(byId);

        while (!toProcess.isEmpty()) {
            ShipHull sh = toProcess.values().iterator().next();
            // start from highest, unprocessed parent
            while (sh.getParentId() != null && toProcess.containsKey(sh.getParentId())) {
                sh = byId.get(sh.getParentId());
            }
            String parentId = null;
            if (sh.getParentId() != null) {
                parentId = sh.getParentId();
            } else if (sh.getArchetypeId() != null) {
                parentId = sh.getArchetypeId();
            } else {
                // can't find parent - not inheriting anything
                toProcess.remove(sh.getId());
                continue;
            }
            final ShipHull parent = byId.get(parentId);
            if (parent == null) {
                throw new IllegalStateException("Can't determine modules for ship hull \"" + sh.getId() + "\"");
            } else {
                // copy modules from parent and override with ours
                if (sh.getModules().isEmpty()) {
                    // just copy
                    sh.getModules().putAll(parent.getModules());
                } else {
                    // take parent's and override with ours
                    Map<String, Module> ours = new LinkedHashMap<>(sh.getModules());
                    sh.getModules().clear();
                    sh.getModules().putAll(parent.getModules());
                    sh.getModules().putAll(ours);

                    // but retain only these modules, for which we have slots (see "ship_hull_pre_dreadnought")
                    sh.getModules().keySet().retainAll(sh.getSlots().keySet());
                }

                // finally copy the type too
                if (sh.getTypes().isEmpty()) {
                    sh.getTypes().addAll(parent.getTypes());
                }
                toProcess.remove(sh.getId());
            }
        }

        // fill empty slots with EMPTY module
        hulls.forEach(sh -> {
            sh.getSlots().values().forEach(s -> {
                sh.getModules().putIfAbsent(s.getId(), Module.EMPTY);
            });
        });

//        // handle remaining template/archetype/parent/inherit data
//        hulls.stream().filter(sh -> sh.getTypes().isEmpty()).forEach(sh -> {
//            ShipHull parent = null;
//            while ((parent = byId.get(sh.getParentId())) != null) {
//                if (!parent.getTypes().isEmpty()) {
//                    sh.getTypes().addAll(parent.getTypes());
//                    break;
//                }
//                parent = byId.get(parent.getParentId());
//            }
//        });

        // for "ship_hull_cruiser.txt" there's only one archetype: "ship_hull_cruiser"
        // according to https://hoi4.paradoxwikis.com/Equipment_modding:
        // - "archetype": Archetype equipment is used to assign more general attributes that regular equipment then
        //   inherits via the archetype attribute.
        // - "parent": Which equipment is parent to this equipment (i.e. which does it supercede)
        //
        // all but "ship_hull_cruiser" have "archetype = ship_hull_cruiser"
        // there are 3 parent-child relationships:
        // - light_cruiser_4 -> light_cruiser_3 -> light_cruiser_2 -> light_cruiser_1 (Vanilla)
        // - heavy_cruiser_4 -> heavy_cruiser_3 -> heavy_cruiser_2 -> heavy_cruiser_1 (Vanilla)
        // - ship_hull_cruiser_4 -> ship_hull_cruiser_3 -> ship_hull_cruiser_2 -> ship_hull_cruiser_1 (MtG)
        // these ships are parentless:
        // - torpedo_cruiser (Vanilla)
        // - ship_hull_cruiser_coastal_defense_ship (MtG)
        // - ship_hull_cruiser_panzerschiff (MtG)
        // - ship_hull_torpedo_cruiser (MtG)

        // what distinguishes the DLCs is:
        // - ids prefixed with "ship_hull_" are from MtG (because they're refenced from
        //   create_equipment_variant objects in blocks with limit = { has_dlc = "Man the Guns" }
        // - remaining ids are from Vanilla
        //
        // now, what distinguishes light cruisers from heavy cruisers is:
        // - for Vanilla models:
        //   - light: it's "screen_ship" among types. light_cruiser_* all have [screen_ship, anti_air]
        //   - heavy: it's "capital_ship" type, but only for heavy_cruiser_1, so I assume type is inherited from parent
        // - for MtG models:
        //   - ship_hull_cruiser archetype has "screen_ship" type, so all ship_hull_* (including
        //     e.g., ship_hull_cruiser_panzerschiff) are assumed to be light - subject to specialization as
        //     ship hull variant in create_equipment_variant
        //
        // later, when reading create_equipment_variant, presence of medium battery in one of the slots
        // "changes" a cruiser hull into heavy cruiser (CA) - both for Vanilla and MtG. For Vanilla, medium batteries
        // are already present in heavy_cruiser_* in common/units/equipment/ship_hull_cruiser.txt
        // so that's how we'll distinguish them.
        // - CL: has ship_light_medium_battery_* as one of modules (of ship_medium_battery category)
        // - CA: has ship_medium_battery_* as one of modules (of ship_medium_battery category)
        //
        // for heavy hulls, we have battlecruisers (BC), battleships (BB) and super heavy battleships (SHBB)
        // all have type=capital_ship, so again, modules distinguish the ships based on heavy hull:
        // - BC: has ship_armor_bc_* as one of modules (of ship_heavy_armor category)
        // - BB: has ship_armor_bb_* as one of modules (of ship_heavy_armor category)
        // - SHBB: has ship_armor_shbb as one of modules (of ship_super_heavy_armor category)

        hulls.forEach(h -> {
            h.setCategory(ShipCategory.determineCategory(h, h.getModules()));
        });

        // this list won't contain archetypes
        return new LinkedList<>(hulls);
    }

    /**
     * List of all ship modules that can be used in specialized ship hull slots
     * @return
     * @throws IOException
     */
    public List<Module> modules() {
        Set<Module> modules = new TreeSet<>();
        Map<String, Module> byId = new HashMap<>();

        withFileSet(hoi4Dir, MODULE_DEFINITIONS, (file, tree) -> {
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

    /**
     * List of upgrades that are possible to make in ship designs in Vanilla (non-MtG) game.
     * @return
     * @throws IOException
     */
    public List<NavalUpgrade> upgrades() throws IOException {
        Set<NavalUpgrade> upgrades = new TreeSet<>();

        withFileSet(hoi4Dir, UPGRADES, (file, tree) -> {
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

    /**
     * Process all basic stats and Vanilla upgrades, but not modules/slots
     * @param sh
     * @param v
     */
    private void processShipHull(ShipHull sh, JsonNode v) {
        if (v.has("parent")) {
            sh.setParentId(v.get("parent").asText());
        }
        if (v.has("archetype")) {
            sh.setArchetypeId(v.get("archetype").asText());
        }
        if (v.has("year")) {
            sh.setYear(v.get("year").asInt());
        }
        // types seem to be then taken from parent
        if (v.has("type")) {
            sh.getTypes().clear();
            asList(v.get("type")).forEach(t -> {
                sh.getTypes().add(t.asText());
            });
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
        if (v.has("max_strength")) {
            sh.setHp(v.get("max_strength").decimalValue());
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

        if (v.has("fuel_consumption")) {
            sh.setFuelConsumption(v.get("fuel_consumption").decimalValue());
        }

        if (v.has("build_cost_ic")) {
            sh.setBuildCost(v.get("build_cost_ic").decimalValue());
        }

        if (v.has("manpower")) {
            sh.setManpower(v.get("manpower").bigIntegerValue());
        }

        if (v.has("resources")) {
            sh.getResources().clear();
            v.get("resources").fields().forEachRemaining(e -> {
                sh.getResources().add(new ResourceAmount(Resource.byName(e.getKey()), e.getValue().bigIntegerValue()));
            });
        }

        if (v.has("module_count_limit")) {
            sh.getModuleLimits().clear();
            asList(v.get("module_count_limit")).forEach(limit -> {
                ModuleCountLimit mcl = new ModuleCountLimit();
                Constraint c = new Constraint("count", limit.get("count").asText());
                mcl.setLimit(c);
                mcl.setModuleCategory(ModuleCategory.byName(limit.get("category").asText()));
                sh.getModuleLimits().add(mcl);
            });
        }
    }

    /**
     * Process {@code modules_slots} of a hull
     * @param sh
     * @param v
     * @param modules
     */
    private void processShipHullModuleSlots(ShipHull sh, JsonNode v, Map<String, Module> modules) {
        if (v.has("module_slots")) {
            // we may have:
            // 1. module_slots = inherit # everything is inherited from parent (or archetype if there's no parent)
            // 2. module_slots = { x1 = yy x2 = inherit ... } # selected slot is inherited, but the ones not present
            //                                                # are not inherited from parent modules_slots
            //                                                # see "ship_hull_pre_dreadnought" for example
            // 3. module_slots = { x1 = yy x2 = x1 ... } # copy of slot from the same module_slots

            Map<String, Slot> slots = new LinkedHashMap<>();
            Map<String, String> refs = new HashMap<>();
            JsonNode declaredSlots = v.get("module_slots");

            if (declaredSlots != null) {
                if (declaredSlots.isTextual() && "inherit".equals(declaredSlots.asText())) {
                    // we may inherit later from parent, instead of from archetype
                    sh.getSlots().clear();
                } else if (declaredSlots.isArray()) {
                    // a duplicate "module_slots = inherit" in some definitions
                    declaredSlots.elements().forEachRemaining(e -> {
                        if ("inherit".equals(e.asText())) {
                            // we may inherit later from parent, instead of from archetype
                            sh.getSlots().clear();
                        }
                    });
                } else {
                    declaredSlots.fields().forEachRemaining(s -> {
                        Slot slot = new Slot(s.getKey());
                        if (s.getValue().isTextual()) {
                            if (s.getValue().asText().equals("inherit")) {
                                // inherited from parent hull
                                slot = Slot.INHERITED;
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
                        slots.put(s.getKey(), slot);
                    });
                    // deep copy instead of reference
                    refs.forEach((k, ref) -> {
                        slots.get(k).setRequired(slots.get(ref).isRequired());
                        slots.get(k).getCategoriesAllowed().addAll(slots.get(ref).getCategoriesAllowed());
                    });
                }
                sh.getSlots().putAll(slots);
            }
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

}
