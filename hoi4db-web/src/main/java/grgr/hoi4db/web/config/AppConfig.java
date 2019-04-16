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
package grgr.hoi4db.web.config;

import java.io.File;
import javax.annotation.PostConstruct;

import grgr.hoi4db.dao.NavalData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({})
public class AppConfig {

    @Value("${steam.dir}")
    private String steamDirName;

    private File steamDir;

    @PostConstruct
    public void init() {
        steamDir = new File(steamDirName, "Hearts of Iron IV");
        if (!steamDir.isDirectory()) {
            throw new IllegalArgumentException("Can't locate Hearts of Iron IV directory");
        }
    }

    @Bean
    public NavalData navalData() {
        return new NavalData(steamDir);
    }

}
