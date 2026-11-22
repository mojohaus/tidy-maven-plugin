# MojoHaus Tidy Maven Plugin

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/mojohaus/tidy-maven-plugin.svg?label=License)](https://www.apache.org/licenses/)
[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.mojo/tidy-maven-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/org.codehaus.mojo/tidy-maven-plugin)
[![Build Status](https://github.com/mojohaus/tidy-maven-plugin/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/mojohaus/tidy-maven-plugin/actions/workflows/maven.yml)

The [Tidy Maven Plugin](https://www.mojohaus.org/tidy-maven-plugin/) rewrites a project's `pom.xml` according to the
Maven team's POM Code Convention, and can fail the build when the POM is not tidy. It requires Maven 3.9.0 or later
and Java 8 or later.

Tidy up the POM of the current project:

```sh
mvn tidy:pom
```

Fail the build when the POM would change, by adding an execution of `tidy:check` (bound to `verify` by default):

```xml
<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>tidy-maven-plugin</artifactId>
  <version><!-- latest release --></version>
  <executions>
    <execution>
      <goals>
        <goal>check</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

The [usage page](https://www.mojohaus.org/tidy-maven-plugin/usage.html) lists what the goals change and the
`enforceIndent` option. See [AGENTS.md](AGENTS.md) for build, test and pull request conventions.

## Releasing

* Make sure `gpg-agent` is running.
* Execute `mvn -B release:prepare release:perform`

For publishing the site do the following:

```
cd target/checkout
mvn verify site site:stage scm-publish:publish-scm
```

