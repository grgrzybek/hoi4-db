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

import java.util.LinkedList;
import java.util.List;

import grgr.hoi4db.model.HasName;

/**
 * A fleet consists of {@link TaskForce task forces}.
 */
public class Fleet extends HasName {

    private List<TaskForce> taskForces = new LinkedList<>();

    public Fleet(String name) {
        super(name);
    }

    public List<TaskForce> getTaskForces() {
        return taskForces;
    }

}
