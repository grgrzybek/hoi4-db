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

import java.io.IOException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import grgr.hoi4db.dataformat.Hoi4DbFactory;
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
    public void readNonStandardArrayItems() throws Exception {
        JsonFactory factory = new Hoi4DbFactory();
        JsonParser parser = factory.createParser(getClass().getResourceAsStream("/samples/arrays.txt"));
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
                System.out.printf("\"%s\"\n", parser.getCurrentValue());
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
