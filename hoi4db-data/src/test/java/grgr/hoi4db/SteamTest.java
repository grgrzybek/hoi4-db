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
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import grgr.hoi4db.databind.Hoi4DbNodeFactory;
import grgr.hoi4db.dataformat.Hoi4DbFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SteamTest {

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
    public void readHoiData() throws Exception {
        File[] dirs = new File[] {
                new File(HOI4_DIR, "common"),
                new File(HOI4_DIR, "history")
        };
        final List<String> problems = new LinkedList<>();
        for (File dir : dirs) {
            Files.walk(dir.toPath())
                    .filter(p -> p.toFile().isFile() && p.toFile().getName().endsWith(".txt"))
                    .filter(p -> p.toFile().isFile() && !p.toFile().getName().equals("graphicalculturetype.txt"))
                    .filter(p -> p.toFile().isFile() && !p.toFile().getName().equals("00_ITA_names.txt"))
                    .filter(p -> p.toFile().isFile() && !p.toFile().getName().equals("BRA_names_divisions.txt"))
                    .filter(p -> p.toFile().isFile() && !p.toFile().getParentFile().getAbsolutePath().contains("common/countries"))
                    .forEach(p -> {
                        try {
    //                        LOG.info("Parsing {}", p);
                            JsonFactory factory = new Hoi4DbFactory();
                            JsonParser parser = factory.createParser(p.toFile());
                            prettyPrint(parser, false);
                            parser.close();
                        } catch (Exception e) {
                            problems.add("Problem processing " + p + ": " + e.getMessage());
                        }
                    });
        }

        for (String ex : problems) {
            LOG.info(ex);
        }
        assertEquals(0, problems.size());
    }

    @Test
    public void bindToMap() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new Hoi4DbFactory());
        mapper.setNodeFactory(new Hoi4DbNodeFactory());
        JsonNode tree = mapper.readTree(new File(HOI4_DIR, "common/units/equipment/modules/00_ship_modules.txt"));
        mapper.writer(new DefaultPrettyPrinter()).writeValue(System.out, tree);
    }

    @Test
    public void readAcrossBuffers() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new Hoi4DbFactory());
        mapper.setNodeFactory(new Hoi4DbNodeFactory());
        JsonNode tree = mapper.readTree(new File(HOI4_DIR, "history/units/SOV_1939_naval_mtg.txt"));
        mapper.writer(new DefaultPrettyPrinter()).writeValue(System.out, tree);
    }

    private void prettyPrint(JsonParser parser, boolean print) throws IOException {
        int ind = 0;
        while (parser.nextToken() != null) {
            JsonToken t = parser.currentToken();
            if (t == JsonToken.START_OBJECT) {
                if (print) {
                    indent(ind);
                    System.out.println("{");
                }
                ind += 3;
            } else if (t == JsonToken.FIELD_NAME) {
                String n = parser.getCurrentName();
                t = parser.nextToken();
                if (print) {
                    indent(ind);
                }
                if (t != JsonToken.START_OBJECT && t != JsonToken.START_ARRAY) {
                    if (print) {
                        System.out.printf("%s = \"%s\"\n", n, parser.getCurrentValue());
                    }
                } else if (t == JsonToken.START_OBJECT) {
                    if (print) {
                        System.out.printf("%s = {\n", n);
                    }
                    ind += 3;
                } else {
                    if (print) {
                        System.out.printf("%s = [\n", n);
                    }
                    ind += 3;
                }
            } else if (t.toString().startsWith("VALUE_")) {
                if (print) {
                    indent(ind);
                    System.out.printf("\"%s\"\n", parser.getCurrentValue());
                }
            } else if (t == JsonToken.END_OBJECT) {
                ind -= 3;
                if (print) {
                    indent(ind);
                    System.out.print("}\n");
                }
            } else if (t == JsonToken.END_ARRAY) {
                ind -= 3;
                if (print) {
                    indent(ind);
                    System.out.print("]\n");
                }
            }
        }
    }

    private void indent(int ind) {
        for (int i = 0; i < ind; i++) {
            System.out.print(" ");
        }
    }

}
