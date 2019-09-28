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
package grgr.hoi4db.databind;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Special {@link ObjectNode} that doesn't break on duplicate fields and converts them to arrays instead.
 */
public class Hoi4DbObjectNode extends ObjectNode {

    public Hoi4DbObjectNode(JsonNodeFactory nc) {
        super(nc);
    }

    public Hoi4DbObjectNode(JsonNodeFactory nc, Map<String, JsonNode> kids) {
        super(nc, kids);
    }

    @Override
    public JsonNode replace(String fieldName, JsonNode value) {
        if (_children.containsKey(fieldName)) {
            // switch to array
            JsonNode current = _children.get(fieldName);
            if (current instanceof ArrayNode) {
                ((ArrayNode) current).add(value);
            } else {
                ArrayNode array = new ArrayNode(this._nodeFactory);
                // move previous value as first item
                array.add(current);
                // add duplicate as next item
                array.add(value);
                _children.put(fieldName, array);
            }
            return null;
        } else {
            return super.replace(fieldName, value);
        }
    }

}
