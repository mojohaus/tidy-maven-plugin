package org.codehaus.mojo.tidy.task;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.util.IOUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test class dedicated to the validation of {@link EnsureSingleLineProjectStartTag}.
 *
 * @author Antoine Malliarakis
 */
class EnsureSingleLineProjectStartTagTest {
    @ParameterizedTest(name = "{0}")
    @ValueSource(
            strings = {
                "project-single-line",
                "project-support-4-1-0-attributes",
                "project-support-4-1-0-attributes-with-unordered-nodes",
                "project-support-4-1-0-model-version"
            })
    void applyTidying(String name) throws Exception {
        String pom = readPom(name, "pom.xml");
        String tidyPom = new EnsureSingleLineProjectStartTag().tidyPom(pom, new FormatIdentifier().identifyFormat(pom));
        assertEquals(readPom(name, "pom-expected.xml"), tidyPom);
    }

    private String readPom(String test, String filename) throws IOException {
        InputStream is = getClass().getResourceAsStream(getClass().getSimpleName() + "/" + test + "/" + filename);
        return IOUtil.toString(is);
    }
}
