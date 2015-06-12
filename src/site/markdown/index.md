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

The Tidy Plugin is used when you want to sort the sections of a `pom.xml` into the canonical order.

### Goals Overview

The Tidy Plugin has two goals.

* [tidy:pom](./pom-mojo.html) sorts the main sections of the `pom.xml` into the canonical order.
* [tidy:check](./check-mojo.html}tidy:check) checks that the main sections of the `pom.xml` are in the canonical order.

### Usage

General instructions on how to use the different goals of the Tidy Plugin can
be found on the [usage page](./usage.html).

To format the `pom.xml`, simply invoke it on your project, e.g.

    mvn tidy:pom

The `pom.xml` file will be rewritten into the
[canonical ordering](http://maven.apache.org/developers/conventions/code.html#POM_Code_Convention),
keeping as much as possible of the remainder of the `pom.xml` as is.