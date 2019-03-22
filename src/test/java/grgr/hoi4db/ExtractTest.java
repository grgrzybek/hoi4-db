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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import grgr.hoi4db.dataformat.Hoi4DbFactory;
import org.junit.jupiter.api.Test;

public class ExtractTest {

    /*
     * I want to be able to extract all ship designs available to all countries.
     * For example, Japan in 1936 (MtG) has 2 ships (not designs) of "Chikuma" class.
     *
     * "history/countries/JAP - Japan.txt" has:
     *  create_equipment_variant = {
     *      name = "Chikuma Class"
     *      type = ship_hull_cruiser_1
     *      name_group = JAP_CA_HISTORICAL
     *      parent_version = 0
     *      modules = {
     *          fixed_ship_battery_slot = ship_light_medium_battery_1
     *          fixed_ship_anti_air_slot = empty
     *          fixed_ship_fire_control_system_slot = ship_fire_control_system_0
     *          fixed_ship_radar_slot = empty
     *          fixed_ship_engine_slot = cruiser_ship_engine_1
     *          fixed_ship_armor_slot = ship_armor_cruiser_1
     *          fixed_ship_secondaries_slot = empty
     *          mid_1_custom_slot = empty
     *          mid_2_custom_slot = empty
     *          rear_1_custom_slot = empty
     *      }
     *      obsolete = yes
     *  }
     *
     * "history/units/JAP_1936_naval.txt" has:
     *  fleet = {
     *      name = "Kaigun Yobikantai"
     *      naval_base = 9859  #Ominato
     *  ...
     *      #Pusan
     *      task_force = {
     *          name = "Bogojunyokan Kantai"
     *          location = 4056  # Pusan
     *          #Mutsuki-class destroyers
     *          ...
     *          ship = { name = "Hirado" definition = light_cruiser equipment = { ship_hull_cruiser_1 = {amount = 1 owner = JAP version_name = "Chikuma Class"} } }
     *          ship = { name = "Yahagi" definition = light_cruiser equipment = { ship_hull_cruiser_1 = {amount = 1 owner = JAP version_name = "Chikuma Class"} } }
     *      }
     *  }
     */

    @Test
    public void readHoiData() throws Exception {
        JsonFactory factory = new Hoi4DbFactory();
        JsonParser parser = factory.createParser(new File("/data/steam/steamapps/common/Hearts of Iron IV/history/countries/JAP - Japan.txt"));
        prettyPrint(parser);
        parser.close();
    }

    @Test
    public void readAnonymousRootScope() throws Exception {
        JsonFactory factory = new Hoi4DbFactory();

        JsonParser parser = factory.createParser(getClass().getResourceAsStream("/samples/values.txt"));
        prettyPrint(parser);
        parser.close();
    }

    @Test
    public void readAcrossBuffers() throws Exception {
        JsonFactory factory = new Hoi4DbFactory();

        JsonParser parser = factory.createParser(getClass().getResourceAsStream("/samples/whitespace.txt"));
        prettyPrint(parser);
        parser.close();
    }

    @Test
    public void readNestedScopes() throws Exception {
        JsonFactory factory = new Hoi4DbFactory();

        JsonParser parser = factory.createParser(getClass().getResourceAsStream("/samples/scopes.txt"));
        prettyPrint(parser);
        parser.close();
    }

    private void prettyPrint(JsonParser parser) throws IOException {
        int ind = 0;
        while (parser.nextToken() != null) {
            JsonToken t = parser.currentToken();
            if (t == JsonToken.START_OBJECT) {
                indent(ind);
                System.out.println("{");
                ind += 3;
            } else if (t == JsonToken.FIELD_NAME) {
                String n = parser.getCurrentName();
                t = parser.nextToken();
                indent(ind);
                if (t != JsonToken.START_OBJECT && t != JsonToken.START_ARRAY) {
                    System.out.printf("%s = %s\n", n, parser.getCurrentValue());
                } else if (t == JsonToken.START_OBJECT) {
                    System.out.printf("%s = {\n", n);
                    ind += 3;
                } else {
                    System.out.printf("%s = [\n", n);
                    ind += 3;
                }
            } else if (t.toString().startsWith("VALUE_")) {
                indent(ind);
                System.out.printf("%s\n", parser.getCurrentValue());
            } else if (t == JsonToken.END_OBJECT) {
                ind -= 3;
                indent(ind);
                System.out.print("}\n");
            } else if (t == JsonToken.END_ARRAY) {
                ind -= 3;
                indent(ind);
                System.out.print("]\n");
            } else {
                System.out.printf("%s: %s = %s\n", t, parser.currentName(), parser.getCurrentValue());
            }
        }
    }

    private void indent(int ind) {
        for (int i = 0; i < ind; i++) {
            System.out.print(" ");
        }
    }

}
