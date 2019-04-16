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

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import grgr.hoi4db.dao.NavalData;
import grgr.hoi4db.web.rest.NavalApi;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.spring.JAXRSServerFactoryBeanDefinitionParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource(/*{ "classpath:/META-INF/cxf/cxf.xml", "classpath:/META-INF/cxf/cxf-servlet.xml" }*/)
public class CxfConfig {

    @Bean("cxf")
    public Bus bus() {
        SpringBus bus = new SpringBus();
        bus.setProperty("skip.default.json.provider.registration", "true");
        return bus;
    }

    @Bean(initMethod = "init")
    public JAXRSServerFactoryBean navalApiServer(Bus bus, NavalApi navalApi) {
        JAXRSServerFactoryBeanDefinitionParser.SpringJAXRSServerFactoryBean factory = new JAXRSServerFactoryBeanDefinitionParser.SpringJAXRSServerFactoryBean();
        factory.setBus(bus);
        factory.setAddress("/naval");
        factory.setServiceBean(navalApi);
        factory.setProvider(new JacksonJsonProvider());
        return factory;
    }

    @Bean
    public NavalApi navalApi(NavalData navalData) {
        return new NavalApi(navalData);
    }

}
