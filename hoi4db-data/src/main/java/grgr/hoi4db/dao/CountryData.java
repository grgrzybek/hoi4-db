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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import grgr.hoi4db.model.DLC;
import grgr.hoi4db.model.NamingRules;
import grgr.hoi4db.model.Vanguard;
import grgr.hoi4db.model.naval.Fleet;
import grgr.hoi4db.model.naval.Module;
import grgr.hoi4db.model.naval.ShipCategory;
import grgr.hoi4db.model.naval.ShipHull;
import grgr.hoi4db.model.naval.ShipHullVariant;
import grgr.hoi4db.model.naval.TaskForce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static grgr.hoi4db.dao.Utils.asList;
import static grgr.hoi4db.dao.Utils.isMtG;
import static grgr.hoi4db.dao.Utils.withFileSet;

/**
 * Accesses country-related data - uses naval/air/land data underneath
 */
public class CountryData {

    private static Logger LOG = LoggerFactory.getLogger(CountryData.class);

    private static String COUNTRIES = "history/countries";
    private static Pattern COUNTRIES_RE = Pattern.compile("(?<ccode>[A-Z]+) - (?<cname>[A-Za-z -]+)\\.txt");

    private static String UNITS = "history/units";
    private static Pattern[] UNITS_NAVAL_RE = new Pattern[] {
            Pattern.compile("(?<ccode>[A-Z]+)_(?<year>[0-9]{4})_naval_(?<dlc>(?:legacy|mtg))\\.txt", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?<ccode>[A-Z]+)_(?<year>[0-9]{4})_naval\\.txt", Pattern.CASE_INSENSITIVE), // here no dlc means MtG
            Pattern.compile("(?<ccode>[A-Z]+)_destroyers_for_bases_(?<dlc>mtg)\\.txt"),
            Pattern.compile("(?<ccode>[A-Z]+)_destroyers_for_bases\\.txt"), // here no dlc means Vanilla
            Pattern.compile("(?<ccode>ENG)_vanguard_communist\\.txt"), // MtG, year 1944
            Pattern.compile("(?<ccode>ENG)_vanguard_hms\\.txt"), // MtG, year 1944
    };

    private static String SHIP_NAMES = "common/units/names_ships";
    private static Pattern SHIP_NAMES_RE = Pattern.compile("(?<ccode>[A-Z]+)_ship_names?\\.txt");

    private static Pattern YEAR_RE = Pattern.compile("(?<year>[0-9]{4})\\.\\d+\\.\\d+");

    private final NavalData navalData;

    private File hoi4Dir;

    // cached from NavalData
    private Map<String, ShipHull> shipHulls = new LinkedHashMap<>();
    private Map<String, Module> shipModules = new LinkedHashMap<>();

    // CountryData caches keyed by country
    private Map<String, Map<ShipHullVariant.Key, ShipHullVariant>> shipHullVariants = new TreeMap<>();
    private Map<String, Set<Fleet>> fleets = new TreeMap<>();

    // cache of country-specific variants...

    // countr -> naming rule name -> naming rule
    private Map<String, Map<String, NamingRules>> naming = new HashMap<>();

    public CountryData(File hoi4Dir, NavalData navalData) {
        this.hoi4Dir = hoi4Dir;
        this.navalData = navalData;

        cacheShipNames();
        cacheNavalData();
        cacheShipHullVariants();
        cacheFleets();
    }

    private void cacheShipNames() {
        new File(hoi4Dir, SHIP_NAMES).list((dir, name) -> {
            Matcher m = SHIP_NAMES_RE.matcher(name);
            if (!m.matches()) {
                return false;
            }
            String ccode = m.group("ccode");
            final Map<String, NamingRules> rules = new HashMap<>();
            naming.put(ccode, rules);
            withFileSet(new File(hoi4Dir, SHIP_NAMES), new String[] { name }, (file, tree) -> {
                tree.fields().forEachRemaining(e -> {
                    // rules may be duplicated for example one with
                    // "can_use = { not = { has_government = communism } }" and one with
                    // "can_use = { has_government = communism }" and one with
                    // maybe I'll handle it, for now I assume that "ordered" don't have to be handled
                    // conditionally
                    asList(e.getValue()).forEach(rule -> {
                        NamingRules namingRule = new NamingRules(rule.required("name").asText());
                        if (rule.has("fallback_name")) {
                            namingRule.setFallbackPattern(rule.get("fallback_name").asText());
                        }
                        if (!rules.containsKey(e.getKey())) {
                            rules.put(e.getKey(), namingRule);
                            if (rule.has("ordered")) {
                                rule.get("ordered").fields().forEachRemaining(e2 -> {
                                    String index = e2.getKey();
                                    String pattern = e2.getValue().get(0).asText();
                                    namingRule.getOrderedNames().put(index, pattern);
                                });
                            }
                        }
                    });
                });
            });
            return false;
        });
    }

    public Map<String, Set<ShipHullVariant>> allVariants() {
        Map<String, Set<ShipHullVariant>> result = new TreeMap<>();
        shipHullVariants.forEach((k, v) -> {
            result.put(k, new TreeSet<>(v.values()));
        });
        return result;
    }

    public Set<ShipHullVariant> variants(String country) {
        return new TreeSet<>(shipHullVariants.get(country).values());
    }

    public Map<String, Set<Fleet>> allFleets() {
        return fleets;
    }

    public Set<Fleet> fleets(String country) {
        return fleets.getOrDefault(country, Collections.emptySet());
    }

    /**
     * Cache data from {@link NavalData} to use later
     */
    private void cacheNavalData() {
        navalData.hulls().forEach(hull -> {
            shipHulls.put(hull.getId(), hull);
        });
        navalData.modules().forEach(module -> {
            shipModules.put(module.getId(), module);
        });
    }

    /**
     * Cache country-specific variants of generic ship designs
     */
    private void cacheShipHullVariants() {
        new File(hoi4Dir, COUNTRIES).list((dir, name) -> {
            Matcher m = COUNTRIES_RE.matcher(name);
            if (m.matches()) {
                String ccode = m.group("ccode");
                Map<ShipHullVariant.Key, ShipHullVariant> countryShipHullVariants = new TreeMap<>();
                shipHullVariants.put(ccode, countryShipHullVariants);

                withFileSet(new File(hoi4Dir, COUNTRIES), new String[] { name }, (file, tree) -> {
                    List<JsonNode> ifs = asList(tree.get("if"));
                    ifs.forEach(o -> {
                        if (o.has("create_equipment_variant")) {
                            boolean mtg = isMtG(o);
                            List<JsonNode> variants = asList(o.get("create_equipment_variant"));
                            variants.forEach(cev -> {
                                processEquipmentVariant(countryShipHullVariants, ccode, 1936, mtg, cev);
                            });
                        }
                    });

                    tree.fieldNames().forEachRemaining(s -> {
                        Matcher my = YEAR_RE.matcher(s);
                        if (my.matches()) {
                            int year = Integer.parseInt(my.group("year"));

                            List<JsonNode> yearObjects = asList(tree.get(s));
                            yearObjects.forEach(o -> {
                                if (o.has("if")) {
                                    List<JsonNode> ifs2 = asList(o.get("if"));
                                    ifs2.forEach(o2 -> {
                                        if (o2.has("create_equipment_variant")) {
                                            boolean mtg = isMtG(o2);
                                            List<JsonNode> variants = asList(o2.get("create_equipment_variant"));
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

    /**
     * Cache fleet compositions for each contry (fleet → task forces → ships)
     */
    private void cacheFleets() {
        new File(hoi4Dir, UNITS).list((dir, name) -> {
            int pc = -1;
            for (Pattern pattern : UNITS_NAVAL_RE) {
                pc++;
                Matcher m = pattern.matcher(name);
                if (!m.matches()) {
                    continue;
                }
                String ccode = m.group("ccode");

                int year = 1936;
                if (pc < 2 && m.group("year") != null) {
                    year = Integer.parseInt(m.group("year"));
                }
                if (pc == 4 || pc == 5) {
                    // ENG, Vanguard
                    year = 1944;
                }

                DLC dlc = DLC.VANILLA;
                if (pc == 0 || pc == 2) {
                    String _dlc = m.group("dlc");
                    if ("mtg".equalsIgnoreCase(_dlc)) {
                        dlc = DLC.MTG;
                    }
                }
                if (pc == 1 || pc == 4 || pc == 5) {
                    dlc = DLC.MTG;
                }

                Set<Fleet> countryFleets = fleets.computeIfAbsent(ccode, c -> new TreeSet<>());

                final int fyear = year;
                final DLC fdlc = dlc;
                final int fpc = pc;

                withFileSet(new File(hoi4Dir, UNITS), new String[] { name }, (file, tree) -> {
                    if (tree.required("units").size() == 0) {
                        return;
                    }
                    List<JsonNode> fleets = asList(tree.required("units").required("fleet"));

                    fleets.forEach(f -> {
                        Vanguard v = null;
                        if (fpc == 4) {
                            v = Vanguard.COMMUNIST;
                        } else if (fpc == 5) {
                            v = Vanguard.HMS;
                        }
                        processFleet(countryFleets, ccode, fyear, fdlc == DLC.MTG, f, v);
                    });
                });
            }
            return false;
        });
    }

    /**
     * Process single {@code create_equipment_variant}
     * @param variants
     * @param country
     * @param year
     * @param mtg
     * @param cevNode
     */
    private void processEquipmentVariant(Map<ShipHullVariant.Key, ShipHullVariant> variants, String country, int year, boolean mtg, JsonNode cevNode) {
        String type = cevNode.required("type").asText();
        ShipHull hull = shipHulls.get(type);
        if (hull == null) {
            // it may be airplane for example
            return;
        }

        String name = cevNode.required("name").asText();
        ShipHullVariant variant = new ShipHullVariant(name);

        variant.setDlc(mtg ? DLC.MTG : DLC.VANILLA);
        variant.setYear(year);
        variant.setCountry(country);

        variant.setType(hull);
        variant.setCategory(hull.getCategory());

        if (cevNode.has("name_group")) {
            variant.setGroupName(cevNode.get("name_group").asText());
        }

        if (mtg) {
            // process modules in MtG
            // do modules match?
            Set<String> modules = new TreeSet<>();
            JsonNode variantModules = cevNode.get("modules");
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

            // adjust ship category after setting variant-specific modules
            variant.setCategory(ShipCategory.determineCategory(hull, variant.getModules()));
        } else {
            // process upgrades in Vanilla
        }

        variants.put(variant.getKey(), variant);
    }

    /**
     * Process single {@code fleet}
     * @param countryFleets
     * @param country
     * @param year
     * @param mtg
     * @param fleetNode
     * @param vanguard
     */
    private void processFleet(Set<Fleet> countryFleets, String country, int year, boolean mtg, JsonNode fleetNode, Vanguard vanguard) {
        String name = fleetNode.required("name").asText();
        System.out.printf("fleet %s (%s/%d): %s\n", country, mtg ? DLC.MTG : DLC.VANILLA, year, name);

        Fleet fleet = new Fleet(name);
        fleet.setCountry(country);
        fleet.setYear(year);
        fleet.setDlc(mtg ? DLC.MTG : DLC.VANILLA);
        fleet.setVanguard(vanguard);

        countryFleets.add(fleet);

        if (fleetNode.has("task_force")) {
            List<JsonNode> taskForces = asList(fleetNode.required("task_force"));
            taskForces.forEach(tf -> {
                processTaskForce(fleet.getTaskForces(), country, year, mtg, tf);
            });
        }
    }

    private void processTaskForce(Set<TaskForce> taskForces, String country, int year, boolean mtg, JsonNode taskForceNode) {
        String name = taskForceNode.required("name").asText();
        System.out.printf("  task force %s\n", name);


        if (taskForceNode.has("ship")) {
            List<JsonNode> ships = asList(taskForceNode.required("ship"));
            ships.forEach(ship -> {
                processShip(null, country, year, mtg, ship);
            });
        }
    }

    private void processShip(Object o, String country, int year, boolean mtg, JsonNode shipNode) {
        /*
         * shipNode = {grgr.hoi4db.databind.Hoi4DbObjectNode@2660} "{"ordered_name":53,"definition":"submarine","equipment":{"ship_hull_submarine_1":{"amount":1,"owner":"JAP","version_name":"Kaidai III Class"}}}"
         *  serialVersionUID: long  = 1 (0x1)
         *  _children: java.util.Map
         *   "ordered_name" -> {com.fasterxml.jackson.databind.node.BigIntegerNode@10644} "53"
         *   "definition" -> {com.fasterxml.jackson.databind.node.TextNode@10646} ""submarine""
         *   "equipment" -> {grgr.hoi4db.databind.Hoi4DbObjectNode@10648} "{"ship_hull_submarine_1":{"amount":1,"owner":"JAP","version_name":"Kaidai III Class"}}"
         *    key: java.lang.String  = "equipment"
         *    value: grgr.hoi4db.databind.Hoi4DbObjectNode  = {grgr.hoi4db.databind.Hoi4DbObjectNode@10648} "{"ship_hull_submarine_1":{"amount":1,"owner":"JAP","version_name":"Kaidai III Class"}}"
         *     serialVersionUID: long  = 1 (0x1)
         *     _children: java.util.Map
         *      "ship_hull_submarine_1" -> {grgr.hoi4db.databind.Hoi4DbObjectNode@10658} "{"amount":1,"owner":"JAP","version_name":"Kaidai III Class"}"
         *       key: java.lang.String  = "ship_hull_submarine_1"
         *       value: grgr.hoi4db.databind.Hoi4DbObjectNode  = {grgr.hoi4db.databind.Hoi4DbObjectNode@10658} "{"amount":1,"owner":"JAP","version_name":"Kaidai III Class"}"
         *        serialVersionUID: long  = 1 (0x1)
         *        _children: java.util.Map
         *         "amount" -> {com.fasterxml.jackson.databind.node.BigIntegerNode@10668} "1"
         *         "owner" -> {com.fasterxml.jackson.databind.node.TextNode@10670} ""JAP""
         *         "version_name" -> {com.fasterxml.jackson.databind.node.TextNode@10672} ""Kaidai III Class""
         */
        String name = null;
        if (shipNode.has("name")) {
            name = shipNode.required("name").asText();
        } else {
            // it should have ordered name - an index to already cached naming rules
            String orderedName = shipNode.required("ordered_name").asText();

            // a naming rule is taken from grgr.hoi4db.model.naval.ShipHullVariant.groupName
            JsonNode equipment = shipNode.required("equipment");
            String shipHullId = equipment.fieldNames().next();
            String shipVariantName = equipment.get(shipHullId).required("version_name").asText();
            ShipHullVariant.Key key = new ShipHullVariant.Key(shipVariantName);
            key.setCountry(country);

            // TODO: ship instances from e.g., history/units/JAP_1939_naval.txt
            // do not necessarily require ship variants from 1939...
            // however, year is needed as part of the key, because there's Italian, MtG light cruiser named
            // "Duca degli Abruzzi Class" defined both for 1936 and 1939. For example with different
            // "fixed_ship_battery_slot"
            key.setYear(year);
            key.setDlc(mtg ? DLC.MTG : DLC.VANILLA);

            // there should be one field, like "ship_hull_submarine_1"
            String hullType = equipment.fieldNames().next();
            key.setCategory(this.shipHulls.get(hullType).getCategory());

            ShipHullVariant variant = this.shipHullVariants.get(country).get(key);
            if (variant == null && year == 1939) {
                key.setYear(1936);
                variant = this.shipHullVariants.get(country).get(key);
            }

            String namingGroup = variant.getGroupName();
            NamingRules rules = naming.get(country).get(namingGroup);

            int n = Integer.parseInt(orderedName);
            String format = null;
            if (rules.getOrderedNames().containsKey(orderedName)) {
                // everything ok - we can generate the name
                format = rules.getOrderedNames().get(orderedName);
            } else {
                // we have to use fallback name
                format = rules.getFallbackPattern();
            }

            name = String.format(format, n);
        }
        System.out.printf("    ship %s\n", name);
    }

}
