<!--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

Usage
=====

The following examples describe the basic usage of the Tidy Plugin.

Format the POM
--------------

To format the `pom.xml` execute the `pom` goal manually.

        mvn tidy:pom

The `pom.xml` file will

* be rewritten according to the
  [POM Code Convention](https://maven.apache.org/developers/conventions/code.html#POM_Code_Convention)
  of the Maven team;
* start with the XML declaration, and have the `project` start tag, with the schema attributes, on one line;
* have the children of `project` in the order of the convention, in groups separated by a blank line: for example
  `modelVersion`, `parent`, then `groupId`, `artifactId`, `version` and `packaging` in this order;
* have the children of `build`, `parent`, `dependency`, `exclusion`, `extension`, `plugin` and
  `distributionManagement/relocation` in the order of the convention, for example `groupId`, `artifactId`,
  `version`, `type`, `classifier`, `scope` for a dependency;
* have the blank lines before the end tag of `dependencies`, `plugins` and `modules` removed;
* end with a new line.

The entries of a list, such as the dependencies, plugins, licenses or developers, are not sorted: they stay in the
order you wrote them.

Note: By default only the children of the sorted sections are indented, and they keep the
indentation the `pom.xml` already has. Set `enforceIndent` to `true` (or run with
`-Dtidy.enforceIndent=true`) to indent every line with two spaces per level, and a line that
continues a tag, such as a wrapped attribute, with four spaces more than the tag. The text of an
element, comments and CDATA sections keep their lines as they are.

Note: To format another file, set `pomFile` (`-Dtidy.pomFile=path/to/pom.xml`); it defaults to the project's
`pom.xml`. Set `tidy.skip` to `true` to skip a goal.

Checking for Tidy POM as Part of the Build
------------------------------------------

If you want to fail the build for a POM that is not formatted according to the
`pom` goal, you must add an execution of `tidy:check` to the `build` element. The goal is bound to the
`verify` phase by default; the example moves it to `validate`.

        <plugin>
          <groupId>org.codehaus.mojo</groupId>
          <artifactId>tidy-maven-plugin</artifactId>
          <version><!-- latest release --></version>
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

