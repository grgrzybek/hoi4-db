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
package grgr.hoi4db.model.naval;

import grgr.hoi4db.model.Constraint;

/**
 * Limit on number of modules from given category used in ship hull's slots
 */
public class ModuleCountLimit {

    private ModuleCategory moduleCategory;
    private Constraint limit;

    public ModuleCategory getModuleCategory() {
        return moduleCategory;
    }

    public void setModuleCategory(ModuleCategory moduleCategory) {
        this.moduleCategory = moduleCategory;
    }

    public Constraint getLimit() {
        return limit;
    }

    public void setLimit(Constraint limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return String.format("%s %s", moduleCategory, limit);
    }

}
