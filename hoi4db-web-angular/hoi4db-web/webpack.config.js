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

const CleanWebpackPlugin   = require('clean-webpack-plugin');
const HtmlWebpackPlugin    = require('html-webpack-plugin');

module.exports = {
  mode: "development",

  entry: "./src/main.ts",
  output: {
    filename: "bundle.js",
    path: __dirname + "/dist"
  },

  // Enable sourcemaps for debugging webpack's output.
  devtool: "source-map",

  resolve: {
    // Add '.ts' and '.tsx' as resolvable extensions.
    extensions: [".ts", ".js", ".json"]
  },

  // awesome-typescript-loader helps Webpack compile your TypeScript code using the
  // TypeScript’s standard configuration file named tsconfig.json.
  module: {
    rules: [
      {test: /\.ts$/, loader: "awesome-typescript-loader"},
      {test: /\.js$/, loader: "source-map-loader", enforce: "pre"}
    ]
  },

  plugins: [
    new CleanWebpackPlugin({root: ".", verbose: true}),
    new HtmlWebpackPlugin({template: './src/index.html'})
  ]
};
