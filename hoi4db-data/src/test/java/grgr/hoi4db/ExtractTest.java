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
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import grgr.hoi4db.databind.Hoi4DbNodeFactory;
import grgr.hoi4db.dataformat.Hoi4DbFactory;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.Test;

public class ExtractTest {

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

    @Test
    public void readOperators() throws Exception {
        JsonFactory factory = new Hoi4DbFactory();
        JsonParser parser = factory.createParser(getClass().getResourceAsStream("/samples/operators.txt"));
        prettyPrint(parser);
        parser.close();
    }

    @Test
    public void readNoRootAndFirstFieldIsScope() throws Exception {
        JsonFactory factory = new Hoi4DbFactory();
        JsonParser parser = factory.createParser(getClass().getResourceAsStream("/samples/noroot.txt"));
        prettyPrint(parser);
        parser.close();
    }

    @Test
    public void objectsInArray() throws Exception {
        JsonFactory factory = new Hoi4DbFactory();
        JsonParser parser = factory.createParser(getClass().getResourceAsStream("/samples/objectinarray.txt"));
        prettyPrint(parser);
        parser.close();
    }

    @Test
    public void readNonStandardArrayItems() throws Exception {
        JsonFactory factory = new Hoi4DbFactory();
        JsonParser parser = factory.createParser(getClass().getResourceAsStream("/samples/arrays.txt"));
        prettyPrint(parser);
        parser.close();
    }

    @Test
    public void readDuplicates() throws Exception {
        JsonFactory factory = new Hoi4DbFactory();
        JsonParser parser = factory.createParser(getClass().getResourceAsStream("/samples/duplicates.txt"));
        prettyPrint(parser);
        parser.close();
    }

    @Test
    @Ignore
    public void hoiToJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper(new Hoi4DbFactory());
        mapper.setNodeFactory(new Hoi4DbNodeFactory());

        File[] files = new File("/data/tmp/cruisers").listFiles((dir, name) -> {
            return name.endsWith(".txt");
        });
        for (File file : files == null ? new File[0] : files) {
            JsonNode tree = mapper.readTree(file);
            try (FileWriter fw = new FileWriter(file.getAbsolutePath() + ".json")) {
                fw.write(tree.toPrettyString());
            }
        }
    }

    /**
     * Prints the tree structure of events and associated names.
     * <em>current name</em> is the name associated with the current token.
     * For {@link JsonToken#FIELD_NAME}s it will be the same as what {@code #getText} returns.
     * For field values it will be preceding field name.
     * For others (array values, root-level values) it will be null.
     *
     * @param parser
     * @throws IOException
     */
    private void prettyPrint(JsonParser parser) throws IOException {
        int ind = 0;
        while (parser.nextToken() != null) {
            JsonToken t = parser.currentToken();
            if (t == JsonToken.START_OBJECT) {
                indent(ind);
                System.out.println("{ " + "<" + parser.currentName() + ">");
                ind += 3;
            } else if (t == JsonToken.FIELD_NAME) {
                String n = parser.getCurrentName();
                t = parser.nextToken();
                indent(ind);
                if (t != JsonToken.START_OBJECT && t != JsonToken.START_ARRAY) {
                    System.out.printf("\"%s\" = \"%s\" " + "<" + parser.currentName() + ">" + "\n", n, parser.getCurrentValue());
                } else if (t == JsonToken.START_OBJECT) {
                    System.out.printf("\"%s\" = { " + "<" + parser.currentName() + ">" + "\n", n);
                    ind += 3;
                } else {
                    System.out.printf("\"%s\" = [ " + "<" + parser.currentName() + ">" + "\n", n);
                    ind += 3;
                }
            } else if (t.toString().startsWith("VALUE_")) {
                indent(ind);
                System.out.printf("\"%s\" " + "<" + parser.currentName() + ">" + "\n", parser.getCurrentValue());
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
