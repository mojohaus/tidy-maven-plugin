package org.codehaus.mojo.tidy.task;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.util.IOUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PomTidyTest {

    @ParameterizedTest(name = "{0}")
    @ValueSource(
            strings = {
                "add-xml-declaration",
                "complete-pom",
                "do-not-mix-tab-and-spaces",
                "groupid-artifactid-version",
                "plugin-config-with-maven-element-names",
                "pom-space-indent",
                "pom-tab-indent",
                "pom-with-comments",
                "pom-with-crlf",
                "pom-with-line-without-indent",
                "pom-with-profiles",
                "pom-with-reporting",
                "project-single-line",
                "project-support-4-1-0-attributes",
                "project-support-4-1-0-attributes-with-unordered-nodes",
                "project-support-4-1-0-model-version"
            })
    void generatesTidyPom(String name) throws Exception {
        String pom = readPom(name, "pom.xml");
        String tidyPom = new PomTidy().tidy(pom);
        assertEquals(readPom(name, "pom-expected.xml"), tidyPom);
    }

    private String readPom(String test, String filename) throws IOException {
        InputStream is = getClass().getResourceAsStream(test + "/" + filename);
        return IOUtil.toString(is);
    }
}
