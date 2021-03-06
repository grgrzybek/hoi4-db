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

import { Component } from '@angular/core';

@Component({
  // A CSS selector that tells Angular to create and insert an instance of this component wherever it finds the
  // corresponding tag in template HTML.
  selector: "hoi4db-app",
  // The module-relative address of this component's HTML template.
  // This template defines the component's host view.
  templateUrl: "./app.component.html",
  styles: [],
  // An array of providers for services that the component requires
  providers: []
})
export class AppComponent {
  title = "Hello!"
}
