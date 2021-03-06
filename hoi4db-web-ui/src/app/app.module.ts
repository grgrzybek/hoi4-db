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

/*
 * This is the module that's passed to platformBrowserDynamic().bootstrapModule()
 */

// https://angular.io/guide/frequent-ngmodules

// When you want to run your app in a browser
import { BrowserModule } from '@angular/platform-browser';
// When you want to use NgIf, NgFor
import { CommonModule } from '@angular/common';

import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { AppRoutingModule } from "./app-routing.module";
import { WelcomeComponent } from "./welcome.component";
import { LandComponent } from "./land/land.component";
import { AirComponent } from "./air/air.component";
import { NavalComponent } from "./naval/naval.component";
import { HttpClientModule } from "@angular/common/http";
import { LayoutModule } from "./layout/layout.module";

// https://angular.io/api/core
@NgModule({
  // The set of NgModules whose exported declarables are available to templates in this module.
  imports: [
    BrowserModule,
    CommonModule,
    // Having imported HttpClientModule into the AppModule, you can inject the HttpClient into any application class
    HttpClientModule,
    LayoutModule,
    AppRoutingModule // should be last because of wildcard route (if other modules use forChild() routing)
  ],
  // The subset of declarations that should be visible and usable in the component templates of other NgModules.
  // A root NgModule has no reason to export anything because other modules don't need to import the root NgModule.
  exports: [],
  // The set of components, directives, and pipes (declarables) that belong to this module.
  declarations: [
    AppComponent,
    WelcomeComponent,
    LandComponent,
    AirComponent,
    NavalComponent
  ],
  // The set of injectable objects that are available in the injector of this module.
  // Creators of services that this NgModule contributes to the global collection of services; they become accessible
  // in all parts of the app. (You can also specify providers at the component level, which is often preferred.)
  providers: [],
  // The set of components that are bootstrapped when this module is bootstrapped. The components listed here
  // are automatically added to entryComponents.
  // The main application view, called the root component, which hosts all other app views. Only the root NgModule
  // should set the bootstrap property.
  bootstrap: [ AppComponent ]
})
export class AppModule {
}
