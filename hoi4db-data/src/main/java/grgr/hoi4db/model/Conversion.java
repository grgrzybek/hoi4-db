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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import grgr.hoi4db.model.naval.ModuleCategory;

/**
 * Information about possible conversion. If {@link grgr.hoi4db.model.naval.Module} has given conversion,
 * it means this module may be converted <em>from</em> given {@link grgr.hoi4db.model.naval.ModuleCategory}
 * or from particular module (by ID).
 */
public class Conversion {

    private String module;
    private ModuleCategory category;
    private Number cost;
    private List<ResourceAmount> resources = new LinkedList<>();

    public Conversion(String module, Number cost) {
        this.module = module;
        this.cost = cost;
    }

    public Conversion(ModuleCategory category, Number cost) {
        this.category = category;
        this.cost = cost;
    }

    public ModuleCategory getCategory() {
        return category;
    }

    public void setCategory(ModuleCategory category) {
        this.category = category;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public Number getCost() {
        return cost;
    }

    public void setCost(Number cost) {
        this.cost = cost;
    }

    public List<ResourceAmount> getResources() {
        return resources;
    }

    @Override
    public String toString() {
        return String.format("%s(%.02f%s)",
                category != null ? "@" + category.toString() : module,
                cost instanceof BigDecimal ? (BigDecimal)cost : new BigDecimal((BigInteger)cost),
                resources.size() == 0 ? "" : resources.stream().map(ResourceAmount::toString).collect(Collectors.joining(", ", " {", "}")));
    }

}
