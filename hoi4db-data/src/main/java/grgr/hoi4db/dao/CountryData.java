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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import grgr.hoi4db.model.DLC;
import grgr.hoi4db.model.naval.Module;
import grgr.hoi4db.model.naval.ShipHull;
import grgr.hoi4db.model.naval.ShipHullVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static grgr.hoi4db.dao.Utils.asArray;
import static grgr.hoi4db.dao.Utils.isMtG;
import static grgr.hoi4db.dao.Utils.withFileSet;

/**
 * Accesses country-related data - uses naval/air/land data underneath
 */
public class CountryData {

    private static Logger LOG = LoggerFactory.getLogger(CountryData.class);

    private static String COUNTRIES = "history/countries";
    private static Pattern COUNTRIES_RE = Pattern.compile("(?<ccode>[A-Z]+) - (?<cname>[A-Za-z -]+).txt");

    private static Pattern YEAR_RE = Pattern.compile("(?<year>[0-9]{4})\\.\\d+\\.\\d+");

    private final NavalData navalData;

    private File hoi4Dir;

    private Map<String, Set<ShipHullVariant>> shipHullVariants = new TreeMap<>();
    private Map<String, ShipHull> shipHulls = new LinkedHashMap<>();
    private Map<String, Module> shipModules = new LinkedHashMap<>();

    public CountryData(File hoi4Dir, NavalData navalData) {
        this.hoi4Dir = hoi4Dir;
        this.navalData = navalData;

        cacheNavalData();
        cacheShipHullVariants();
    }

    private void cacheNavalData() {
        navalData.hulls().forEach(hull -> {
            shipHulls.put(hull.getId(), hull);
        });
        navalData.modules().forEach(module -> {
            shipModules.put(module.getId(), module);
        });
    }

    public Map<String, Set<ShipHullVariant>> allVariants() {
        return shipHullVariants;
    }

    public Set<ShipHullVariant> variants(String country) {
        return shipHullVariants.get(country);
    }

    private void cacheShipHullVariants() {
        new File(hoi4Dir, COUNTRIES).list((dir, name) -> {
            Matcher m = COUNTRIES_RE.matcher(name);
            if (m.matches()) {
                String ccode = m.group("ccode");
                Set<ShipHullVariant> countryShipHullVariants = new TreeSet<>();
                shipHullVariants.put(ccode, countryShipHullVariants);

                withFileSet(new File(hoi4Dir, COUNTRIES), new String[] { name }, (file, tree) -> {
                    List<ObjectNode> ifs = asArray(tree.get("if"));
                    ifs.forEach(o -> {
                        if (o.has("create_equipment_variant")) {
                            boolean mtg = isMtG(o);
                            List<ObjectNode> variants = asArray(o.get("create_equipment_variant"));
                            variants.forEach(cev -> {
                                processEquipmentVariant(countryShipHullVariants, ccode, 1936, mtg, cev);
                            });
                        }
                    });

                    tree.fieldNames().forEachRemaining(s -> {
                        Matcher my = YEAR_RE.matcher(s);
                        if (my.matches()) {
                            int year = Integer.parseInt(my.group("year"));

                            List<ObjectNode> yearObjects = asArray(tree.get(s));
                            yearObjects.forEach(o -> {
                                if (o.has("if")) {
                                    List<ObjectNode> ifs2 = asArray(o.get("if"));
                                    ifs2.forEach(o2 -> {
                                        if (o2.has("create_equipment_variant")) {
                                            boolean mtg = isMtG(o2);
                                            List<ObjectNode> variants = asArray(o2.get("create_equipment_variant"));
                                            variants.forEach(cev -> {
                                                processEquipmentVariant(countryShipHullVariants, ccode, year, mtg, cev);
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                });
            }
            return false;
        });
    }

    private void processEquipmentVariant(Set<ShipHullVariant> variants, String country, int year, boolean mtg, JsonNode cev) {
        String type = cev.required("type").asText();
        ShipHull hull = shipHulls.get(type);
        if (hull == null) {
            // it may be airplane for example
            return;
        }

        String name = cev.required("name").asText();
        ShipHullVariant variant = new ShipHullVariant(name);

        variant.setDlc(mtg ? DLC.MTG : DLC.VANILLA);
        variant.setYear(year);
        variant.setCountry(country);

        variant.setCategory(hull.getCategory());

        if (cev.has("name_group")) {
            variant.setGroupName(cev.get("name_group").asText());
        }

        if (mtg) {
            // process modules
            // do modules match?
            Set<String> modules = new TreeSet<>();
            JsonNode variantModules = cev.get("modules");
            variantModules.fieldNames().forEachRemaining(modules::add);

            // additional verification of generic hull
            Set<String> hslots = new TreeSet<>(hull.getSlots().keySet());
            Set<String> hmodules = new TreeSet<>(hull.getModules().keySet());
            if (!hslots.equals(hmodules)) {
                throw new RuntimeException("Generic ship " + hull.getId() + " has wrong definition of slots and modules");
            }

            /*
             * ship_hull_cruiser_1 has (inherited from ship_hull_cruiser):
             * module_slots = {
             *   fixed_ship_battery_slot = ...
             *   fixed_ship_anti_air_slot = ...
             *   fixed_ship_fire_control_system_slot = ...
             *   fixed_ship_radar_slot = ...
             *   fixed_ship_engine_slot = ...
             *   fixed_ship_secondaries_slot = ...
             *   fixed_ship_armor_slot = ...
             *   mid_1_custom_slot = ...
             *   mid_2_custom_slot = ...
             *   rear_1_custom_slot = ...
             * }
             *
             * and:
             * default_modules = {
             *   fixed_ship_battery_slot = empty
             *   fixed_ship_anti_air_slot = empty
             *   fixed_ship_fire_control_system_slot = empty
             *   fixed_ship_radar_slot = empty
             *   fixed_ship_engine_slot = cruiser_ship_engine_1
             *   mid_1_custom_slot = empty
             *   mid_2_custom_slot = empty
             *   rear_1_custom_slot = empty
             * }
             *
             * When parsing common/units/equipment/ship_hull_cruiser.txt I assume that slot declared in
             * module_slots, but not present in default_modules is <empty>
             *
             * However, in history/countries/NZL - New Zealand.txt there's:
             * create_equipment_variant = {
             *   name = "Danae Class"
             *   type = ship_hull_cruiser_1
             *   name_group = NZL_CL_HISTORICAL
             *   parent_version = 0
             *   modules = {
             *     fixed_ship_battery_slot = ship_light_medium_battery_1
             *     fixed_ship_anti_air_slot = ship_anti_air_1
             *     fixed_ship_fire_control_system_slot = ship_fire_control_system_0
             *     fixed_ship_radar_slot = empty
             *     fixed_ship_engine_slot = cruiser_ship_engine_1
             *     mid_1_custom_slot = ship_torpedo_1
             *     mid_2_custom_slot = ship_torpedo_1
             *     rear_1_custom_slot = empty
             *   }
             * }
             *
             * I can assume two things:
             * 1) fixed_ship_secondaries_slot and fixed_ship_armor_slot are always empty if not present in "modules"
             * 2) fixed_ship_secondaries_slot and fixed_ship_armor_slot are inherited from type of ship - it may
             *    (and probably will be) <empty> or not
             *
             * For now, I chose #1.
             */

            // fill first using default_modules from design
            variant.getModules().clear();
            variant.getModules().putAll(hull.getModules());

            // then use variant-specific modules
            modules.forEach(slotId -> {
                String moduleId = variantModules.required(slotId).asText();
                if (moduleId.equals(Module.EMPTY.getId())) {
                    variant.getModules().put(slotId, Module.EMPTY);
                } else {
                    variant.getModules().put(slotId, shipModules.get(moduleId));
                }
            });
        } else {
            // process upgrades
        }

        variants.add(variant);
    }

}
