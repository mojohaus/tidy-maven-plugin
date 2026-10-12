# Agent guide for tidy-maven-plugin

This file is for automated agents and human contributors working on this repository. It records
what the code and the POMs do not make obvious.

## Layout

- One module with two goals in `org.codehaus.mojo.tidy`: `pom` (`PomMojo`) rewrites a POM in place,
  and `check` (`CheckMojo`, bound to `verify`) fails the build when the POM is not tidy. The
  default file is the project's POM (`${project.file}`); `-Dtidy.pomFile` selects another one.
- `tidy.task.PomTidy` runs the `TidyTask` steps over the POM text: `EnsureXmlHeader`,
  `EnsureOrderAndIndent`, `EnsureSingleLineProjectStartTag` and `EnsureTrailingNewLine`, in that order. The tidy
  rules, such as element order and indentation, live in `EnsureOrderAndIndent`.
- The site is built from `src/site/markdown`.

## Build gates

- On a local build Spotless applies the formatting; it also reformats Markdown, so run
  `mvn spotless:apply` after editing the README or this file.
- The parent, `mojo-parent`, sets `maven.compiler.release` to 8, so main code must use the Java 8
  API.
- The plugin compiles against Maven 3.9.16 (`mavenVersion`) and declares a literal
  `<prerequisites><maven>3.9.0</maven>`; keep the two apart, so raising `mavenVersion` does not
  raise the prerequisite.

## Unit tests

- JUnit 5 only. `PomTidyTest` is parameterized over directories in
  `src/test/resources/org/codehaus/mojo/tidy/task/`: each holds a `pom.xml` and the `pom-expected.xml`
  that `PomTidy.tidy` must produce byte for byte, including line endings (`pom-with-crlf`). To add a
  case, add a directory and its name to the `@ValueSource` list.

## Integration tests

- They live in `src/it`, one directory per test, and run with maven-invoker-plugin in the `run-its`
  profile. The plugin is installed into `target/local-repo` first, and `src/it/settings.xml` points
  the forked builds at it. `check-*` tests run `verify` and expect success or failure through
  `invoker.buildResult`; `pom` and `separate-pom` run the `pom` goal and compare the result with
  `pom-expected.xml` in `verify.groovy`.
- Run a subset:

  ```shell
  mvn -B -P run-its verify -Dinvoker.test=check-fails,pom
  ```

  The tests have no `setup-*` projects.

- They run in parallel, one per CPU core (`invoker.parallelThreads` 1C), so a test must not depend
  on another test's output.

- The forked builds get `JAVA_TOOL_OPTIONS=-XX:TieredStopAtLevel=1 -XX:+UseSerialGC`, so every JVM
  prints a `Picked up JAVA_TOOL_OPTIONS` line on stderr.

- Write `verify.groovy` as `assert` statements. Invoker fails a script whose last expression is
  anything other than `true` or `null`, so a trailing `list.each { }` fails the test; use a `for`
  loop.

## Dependencies

- Dependabot excludes `src/it`, `src/it-repo` and `src/test/resources` from version updates, so the
  fixture POMs stay as they are; security updates can still propose them, so close those pull
  requests.
- Deliberate pins in `.github/dependabot.yml`: `maven-core` and `maven-plugin-api` stay below
  3.10.0, because the plugin baseline is the Maven 3.9.x line.

## Continuous integration and pull requests

- `.github/workflows/maven.yml` calls the shared `maven-verify.yml@v5` workflow from
  `apache/maven-gh-actions-shared`. Branches in this repository build under the push event, so look
  at the push run for a pull request from a same-repository branch.
- Pull requests are squash- or rebase-merged; merge commits are disabled. Release Drafter builds
  the release notes from the titles and labels, so the title should describe the change for a
  user.

