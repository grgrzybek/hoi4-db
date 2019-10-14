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
package grgr.hoi4db.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Definition of naming rules for units, cities, states, ...
 */
public class NamingRules extends HasId {

    // for example when id=GER_BB_HISTORICAL, name=NAME_THEME_HISTORICAL_BB
    private String name;

    // printf-friendly (I hope) pattern that includes "%d"
    private String fallbackPattern;
    // I'm not yet sure
    private String prefix;

    // e.g.,:
    // ordered = {
    //  301 = {"Shch-%d"}
    //  302 = {"Shch-%d"}
    //  303 = {"Shch-%d"}
    //  ...
    private Map<String, String> orderedNames = new HashMap<>();

    public NamingRules(String id) {
        super(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFallbackPattern() {
        return fallbackPattern;
    }

    public void setFallbackPattern(String fallbackPattern) {
        this.fallbackPattern = fallbackPattern;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Map<String, String> getOrderedNames() {
        return orderedNames;
    }

}
