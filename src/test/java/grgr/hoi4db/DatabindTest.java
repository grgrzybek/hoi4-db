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
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import grgr.hoi4db.dataformat.Hoi4DbFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabindTest {

    public static Logger LOG = LoggerFactory.getLogger(DatabindTest.class);

    @Test
    public void bindToMap() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new Hoi4DbFactory());
        mapper.setDefaultMergeable(true);
        JsonNode tree = mapper.readTree(getClass().getResourceAsStream("/samples/noroot.txt"));
        mapper.writer(new DefaultPrettyPrinter()).writeValue(System.out, tree);
    }

    @Test
    public void bindJsonToMap() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        mapper.setDefaultMergeable(true);
        JsonNode tree = mapper.readTree(getClass().getResourceAsStream("/samples/noroot.json"));
        mapper.writer(new DefaultPrettyPrinter()).writeValue(System.out, tree);
    }

}
