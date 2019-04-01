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

import java.math.BigDecimal;

import grgr.hoi4db.model.HasId;

/**
 * Represents an entry from {@code /equipments/*} nodes in {@code common/units/equipment/(ship_hull_|)*.txt}.
 */
public class ShipHull extends HasId {

    private int year;
    private String type;

    private BigDecimal surfaceDetection;

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getSurfaceDetection() {
        return surfaceDetection;
    }

    public void setSurfaceDetection(BigDecimal surfaceDetection) {
        this.surfaceDetection = surfaceDetection;
    }

}
