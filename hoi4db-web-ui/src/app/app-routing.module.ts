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

import { NgModule } from "@angular/core";
// When you want to use RouterLink, .forRoot(), and .forChild()
import { RouterModule, Routes } from '@angular/router';

import { WelcomeComponent } from "./welcome.component";
import { LandComponent } from "./land/land.component";
import { AirComponent } from "./air/air.component";
import { NavalComponent } from "./naval/naval.component";

const routes: Routes = [
  { path: '', redirectTo: '/welcome', pathMatch: 'full' },
  { path: 'welcome', component: WelcomeComponent },
  { path: 'land', component: LandComponent },
  { path: 'air', component: AirComponent },
  { path: 'naval', component: NavalComponent }
];

@NgModule({
  // The set of NgModules whose exported declarables are available to templates in this module.
  imports: [
    RouterModule.forRoot(routes, { enableTracing: false })
  ],
  // The set of components, directives, and pipes declared in this NgModule that can be used in the template
  // of any component that is part of an NgModule that imports this NgModule. Exported declarations
  // are the module's public API.
  exports: [
    RouterModule
  ]
})
export class AppRoutingModule {
}
