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
## Tidy Maven Plugin

The Tidy Plugin tidies up a project's `pom.xml` and optionally verifies
that it is tidy.

### Goals Overview

The Tidy Plugin has two goals.

* [tidy:pom](./pom-mojo.html) tidies up the project's `pom.xml`.
* [tidy:check](./check-mojo.html) checks that the project's `pom.xml` is tidy.

### Usage

General instructions on how to use the different goals of the Tidy Plugin can
be found on the [usage page](./usage.html).

To tidy up the `pom.xml`, simply invoke it on your project, e.g.

    mvn tidy:pom

The `pom.xml` file will

* be rewritten according to the
[POM Code Convention](http://maven.apache.org/developers/conventions/code.html#POM_Code_Convention)
of the Maven team.
* have the nodes `groupId`, `artifactId` and `version` always in this order.

