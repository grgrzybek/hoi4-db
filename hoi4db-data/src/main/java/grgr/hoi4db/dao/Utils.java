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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import grgr.hoi4db.databind.Hoi4DbNodeFactory;
import grgr.hoi4db.dataformat.Hoi4DbFactory;

public class Utils {

    private Utils() {
    }

    /**
     * Processes set of files inside HoI4 directory. Entire {@link JsonNode} from the file is passed to
     * {@link Consumer}.
     * @param hoi4Dir
     * @param filenames
     * @param processor
     */
    public static void withFileSet(File hoi4Dir, String[] filenames, BiConsumer<File, JsonNode> processor) {
        ObjectMapper mapper = new ObjectMapper(new Hoi4DbFactory());
        mapper.setNodeFactory(new Hoi4DbNodeFactory());

        for (String fileName : filenames) {
            File file = new File(hoi4Dir, fileName);
            JsonNode tree = null;
            try {
                tree = mapper.readTree(file);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            processor.accept(file, tree);
        }
    }

    /**
     * With {@link grgr.hoi4db.databind.Hoi4DbObjectNode}, a field may be normal field or an array. When there's only
     * one field, it's not converted into {@link ArrayNode} during parsing. This method unifies access for single
     * and multi valued fields.
     * @param n
     * @return
     */
    public static List<ObjectNode> asArray(JsonNode n) {
        List<ObjectNode> values = new LinkedList<>();
        if (n == null) {
            return values;
        } else if (n.getNodeType() == JsonNodeType.OBJECT) {
            values.add((ObjectNode) n);
        } else if (n.getNodeType() == JsonNodeType.ARRAY) {
            n.elements().forEachRemaining(n2 -> {
                // special case (history/countries/MAN - Manchukou.txt)
                if (n2.getNodeType() == JsonNodeType.OBJECT) {
                    values.add((ObjectNode) n2);
                }
            });
        }
        return values;
    }

    /**
     * Checks if given {@link grgr.hoi4db.databind.Hoi4DbObjectNode} is declared to be MtG-only:<pre>
     * "limit" : {
     *   "has_dlc" : "Man the Guns"
     * }
     * </pre>
     * @param n
     * @return
     */
    public static boolean isMtG(ObjectNode n) {
        return n.has("limit")
                && n.get("limit").has("has_dlc")
                && "Man the Guns".equals(n.get("limit").get("has_dlc").asText());
    }

}
