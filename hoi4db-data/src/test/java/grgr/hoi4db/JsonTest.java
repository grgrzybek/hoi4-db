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
import java.io.StringReader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import org.junit.jupiter.api.Test;

import static com.fasterxml.jackson.core.JsonFactory.Feature.USE_THREAD_LOCAL_FOR_BUFFER_RECYCLING;

public class JsonTest {

    @Test
    public void generate() throws IOException {
        JsonFactory factory = new JsonFactory();
        factory.enable(JsonParser.Feature.ALLOW_COMMENTS);

        JsonGenerator gen = factory.createGenerator(System.out);
        gen.useDefaultPrettyPrinter();
        gen.writeStartObject();
        gen.writeFieldName("id");
        gen.writeNumber(42);
        gen.writeFieldName("name");
        gen.writeString("Mlanos");
        gen.writeEndObject();
        gen.close();

        System.out.flush();
    }

    @Test
    public void read() throws IOException {
        JsonFactory factory = new JsonFactoryBuilder()
                .configure(USE_THREAD_LOCAL_FOR_BUFFER_RECYCLING, false)
                .build();

        try (JsonParser parser = factory.createParser(new StringReader("{\"id\": 1, \"name\": \"Grzegorz\", \"address\": {\"street\": \"xyz\", \"v\": 1.2}, \"code\": \"green\"}"))) {
            while (parser.nextToken() != null) {
                System.out.println(parser.getCurrentToken());
                System.out.println(parser.getCurrentName());
            }
        }
    }

    @Test
    public void readString() throws IOException {
        JsonFactory factory = new JsonFactoryBuilder()
                .configure(USE_THREAD_LOCAL_FOR_BUFFER_RECYCLING, false)
                .build();

        JsonParser parser = factory.createParser("{\"id\": 1, \"name\": \"Grzegorz\", \"address\": {\"street\": \"xyz\", \"v\": [1.2, \"x\"]}, \"code\": \"green\"}");
        while (parser.nextToken() != null) {
            System.out.println(parser.currentToken());
        }
        parser.close();
    }

}
