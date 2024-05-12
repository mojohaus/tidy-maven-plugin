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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PomTidyFixesTest {

    protected static final String PATH = "fixes/order-and-indent-start-element-of-scope/";

    @ValueSource(strings = {"property-ending-with-plugin.pom.xml", "property-ending-with-dependency.pom.xml"})
    @ParameterizedTest(name = "{0}")
    void shouldThrowNoError(String name) throws Exception {
        String pom = IOUtil.toString(Objects.requireNonNull(getClass().getResourceAsStream(PATH + name)));
        String tidyPom = new PomTidy().tidy(pom);
        assertEquals(pom, tidyPom, "nothing to tidy here");
    }
}
