package org.codehaus.mojo.tidy.task;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Objects;

import org.codehaus.plexus.util.IOUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * The indentation of the whole POM is rewritten only when asked for (#18).
 */
class EnforceIndentTest {

    private static final String PATH = "fixes/enforce-indent/";

    @Test
    void shouldIndentTwoSpacesPerLevelAndFourForWrappedLines() throws Exception {
        assertEquals(read("pom-expected.xml"), new PomTidy(true).tidy(read("pom.xml")));
    }

    @Test
    void shouldKeepLineSeparators() throws Exception {
        String crlf = read("pom.xml").replace("\n", "\r\n");
        assertEquals(read("pom-expected.xml").replace("\n", "\r\n"), new PomTidy(true).tidy(crlf));
    }

    @Test
    void shouldBeIdempotent() throws Exception {
        String expected = read("pom-expected.xml");
        assertEquals(expected, new PomTidy(true).tidy(expected));
    }

    @Test
    void shouldKeepIndentationOfUnsortedSectionsByDefault() throws Exception {
        String pom = read("pom.xml");
        String tidy = new PomTidy().tidy(pom);
        assertEquals(tidy, new PomTidy(false).tidy(pom));
        assertEquals(read("pom-default-expected.xml"), tidy);
        assertNotEquals(read("pom-expected.xml"), tidy);
    }

    private String read(String name) throws Exception {
        return IOUtil.toString(Objects.requireNonNull(getClass().getResourceAsStream(PATH + name)));
    }
}
