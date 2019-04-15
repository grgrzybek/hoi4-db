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
package grgr.hoi4db.web;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import grgr.hoi4db.web.config.AppConfig;
import grgr.hoi4db.web.config.WebConfig;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.accesslog.AccessLogHandler;
import io.undertow.server.handlers.cache.DirectBufferCache;
import io.undertow.server.handlers.resource.CachingResourceManager;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.util.ImmediateInstanceFactory;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.context.support.StandardServletEnvironment;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.DispatcherServlet;

public class Main {

    public static Logger LOG = LoggerFactory.getLogger(Main.class);

    private static ConfigurableApplicationContext applicationContext;
    private static Undertow server;

    public static void main(String[] args) throws Exception {
        ConfigurableEnvironment environment = new StandardServletEnvironment();

        ClassPathResource properties = new ClassPathResource("/application.properties");
        ResourcePropertySource appPropertySource = new ResourcePropertySource("hoi4db", properties);
        environment.getPropertySources().addLast(appPropertySource);

        PropertySourcesPlaceholderConfigurer propertyConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertyConfigurer.setEnvironment(environment);

        // main Spring context
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.setEnvironment(environment);
        context.register(AppConfig.class);
        context.addBeanFactoryPostProcessor(propertyConfigurer);
        context.refresh();
        applicationContext = context;

        // web
        ServletContainer container = Servlets.defaultContainer();

        // web Spring context
        AnnotationConfigWebApplicationContext webContext = new AnnotationConfigWebApplicationContext();
        webContext.setEnvironment(environment);
        webContext.register(WebConfig.class);
        webContext.addBeanFactoryPostProcessor(propertyConfigurer);
        webContext.setParent(applicationContext);

        DispatcherServlet mainServlet = new DispatcherServlet();
        mainServlet.setApplicationContext(webContext);

        CharacterEncodingFilter cef = new CharacterEncodingFilter(StandardCharsets.UTF_8.name());

        // WWW deployment WWW for Undertow
        DeploymentInfo supportWar = Servlets.deployment()
                .setContextPath("/app")
                .setDeploymentName("support.war")
                .setResourceManager(new CachingResourceManager(200, 1024 * 1024, new DirectBufferCache(1, 1, 1),
                        new ClassPathResourceManager(Main.class.getClassLoader()), 1000 * 60))
                .setClassLoader(Main.class.getClassLoader())
                .addFilter(Servlets.filter("character-encoding", CharacterEncodingFilter.class, new ImmediateInstanceFactory<>(cef)))
                .addFilterServletNameMapping("character-encoding", "main", DispatcherType.REQUEST)
                .addServlet(Servlets.servlet("main", DispatcherServlet.class, new ImmediateInstanceFactory<>(mainServlet))
                        .addMapping("/")
                        .setLoadOnStartup(0));

        DeploymentManager dm = container.addDeployment(supportWar);
        dm.deploy();
        ServletContext servletContext = dm.getDeployment().getServletContext();

        webContext.setServletContext(servletContext);
        webContext.refresh();

        // io.undertow.servlet.api.DeploymentManager.start() will run web application
        HttpHandler webHandler = dm.start();

        // web application to HttpHandler
        PathHandler pathHandler = Handlers.path()
                .addPrefixPath("/", Handlers.redirect("/app"))
                .addPrefixPath("/version", exchange -> {
                    exchange.setStatusCode(HttpServletResponse.SC_OK);
                    exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "text/plain; charset=UTF-8");
                    exchange.getResponseSender().send(String.format("%s: %s", environment.getProperty("title"), environment.getProperty("version")));
                })
                .addPrefixPath("/app", webHandler);

        AccessLogHandler.Builder builder = new AccessLogHandler.Builder();
        Map<String, Object> config = new HashMap<>();
        config.put(builder.defaultParameter(), "common");
        HandlerWrapper accessLog = builder.build(config);

        Integer port = environment.getProperty("http.port", Integer.TYPE, 1936);
        server = Undertow.builder()
                .addHttpListener(port, "0.0.0.0")
//                .setHandler(accessLog.wrap(Handlers.requestDump(pathHandler)))
                .setHandler(accessLog.wrap(pathHandler))
                .build();
        LOG.info("Starting HTTP server on port {}", port);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Thread.currentThread().setName("shutdown");
            System.out.println();
            LOG.info("Stopping application...");
            server.stop();
            applicationContext.stop();
            LOG.info("Bye.");
        }));
    }

}
