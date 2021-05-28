# MojoHaus Tidy Maven Plugin

This is the [tidy-maven-plugin](http://www.mojohaus.org/tidy-maven-plugin/).
 
[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/mojohaus/tidy-maven-plugin.svg?label=License)](http://www.apache.org/licenses/)
[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.mojo/tidy-maven-plugin.svg?label=Maven%20Central)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.codehaus.mojo%22%20AND%20a%3A%22tidy-maven-plugin%22)
[![Build Status](https://github.com/mojohaus/tidy-maven-plugin/actions/workflows/maven.yml/badge.svg)](https://github.com/mojohaus/tidy-maven-plugin/actions/workflows/maven.yml)

## Releasing

* Make sure `gpg-agent` is running.
* Execute `mvn -B release:prepare release:perform`

For publishing the site do the following:

```
cd target/checkout
mvn verify site site:stage scm-publish:publish-scm
```
