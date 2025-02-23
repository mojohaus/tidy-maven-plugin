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

import javax.xml.stream.XMLStreamException;

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
    void generatesTidyPom(String name) throws IOException, XMLStreamException {
        String pom = readPom(name, "pom.xml");
        String tidyPom = new PomTidy().tidy(pom);
        assertEquals(readPom(name, "pom-expected.xml"), tidyPom);
    }

    private String readPom(String test, String filename) throws IOException {
        InputStream is = getClass().getResourceAsStream(test + "/" + filename);
        return IOUtil.toString(is);
    }
}
