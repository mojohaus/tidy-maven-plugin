[//]: # (Licensed to the Apache Software Foundation (ASF) under one)
[//]: # (or more contributor license agreements.  See the NOTICE file)
[//]: # (distributed with this work for additional information)
[//]: # (regarding copyright ownership. The ASF licenses this file)
[//]: # (to you under the Apache License, Version 2.0 (the)
[//]: # ("License"); you may not use this file except in compliance)
[//]: # (with the License.  You may obtain a copy of the License at)
[//]: # (  http://www.apache.org/licenses/LICENSE-2.0)
[//]: # (Unless required by applicable law or agreed to in writing,)
[//]: # (software distributed under the License is distributed on an)
[//]: # ("AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY)
[//]: # (KIND, either express or implied.  See the License for the)
[//]: # (specific language governing permissions and limitations)
[//]: # (under the License.)
## Usage

The following examples describe the basic usage of the Tidy Plugin.

### Format the POM

To format the `pom.xml` execute the `pom` goal manually.

    mvn tidy:pom

The `pom.xml` file will

* be rewritten according to the
[POM Code Convention](http://maven.apache.org/developers/conventions/code.html#POM_Code_Convention)
of the Maven team.
* have the nodes `groupId`, `artifactId` and `version` always in this order.

Note: It is recommended that you use your IDE or other formatting tools to
ensure that your `pom.xml` is indented correctly.

Note: The following sections can have their child elements reordered without affecting the build:

* `/project/licenses`
* `/project/developers`
* `/project/contributors`
* `/project/mailingLists`
* `/project/properties`
* `/project/build/pluginManagement/plugins`
* `/project/profiles`

Note: The following sections potentially can affect the build process if the child elements are reordered:

* `/project/repositories`
* `/project/pluginRepositories`
* `/project/dependencyManagement/dependencies`
* `/project/dependencies`
* `/project/build/plugins`

### Checking for Tidy POM as Part of the Build

If you want to fail the build for a POM that is not formatted according to the
`pom` goal, you must add an execution of `tidy:check` to the `build` element.

    <plugin>
      <groupId>org.codehaus.mojo</groupId>
      <artifactId>tidy-maven-plugin</artifactId>
      <version>${project.version}</version>
      <executions>
        <execution>
          <id>validate</id>
          <phase>validate</phase>
          <goals>
            <goal>check</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
