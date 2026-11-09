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

/**
 * Blank lines between the last child of dependencies, plugins or modules and the end tag are removed (#4).
 */
class TrailingBlankLinesTest {

    private static final String PATH = "fixes/trailing-blank-lines/";

    @Test
    void shouldTrimBlankLinesBeforeTheEndTag() throws Exception {
        String tidyPom = new PomTidy().tidy(read("pom.xml"));
        assertEquals(read("pom-expected.xml"), tidyPom);
        assertEquals(tidyPom, new PomTidy().tidy(tidyPom), "tidy must be idempotent");
    }

    @Test
    void shouldKeepWindowsLineEndings() throws Exception {
        String pom = read("pom.xml").replace("\n", "\r\n");
        String expected = read("pom-expected.xml").replace("\n", "\r\n");
        assertEquals(expected, new PomTidy().tidy(pom));
    }

    /**
     * Reads a fixture with LF line endings, whatever line endings the checkout gave it.
     */
    private String read(String name) throws Exception {
        return IOUtil.toString(Objects.requireNonNull(getClass().getResourceAsStream(PATH + name)))
                .replace("\r\n", "\n");
    }
}
