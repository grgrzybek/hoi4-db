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

/**
 * Special switch for English "ENG_vanguard" focus, where new {@link grgr.hoi4db.model.naval.Fleet}
 * is created with single {@link grgr.hoi4db.model.naval.TaskForce} and single battleship named differently
 * depending on {@code ENG_vanguard_communist.txt} or {@code ENG_vanguard_hms.txt} definition file.
 */
public enum Vanguard {
    HMS, COMMUNIST
}
