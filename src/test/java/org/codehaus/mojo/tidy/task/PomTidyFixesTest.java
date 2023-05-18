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

import org.codehaus.plexus.util.IOUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(Parameterized.class)
public class PomTidyFixesTest {

    protected static final String PATH = "fixes/order-and-indent-start-element-of-scope/";

    @Parameters(name = "{0}")
    public static Iterable<String> tests() {
        return asList("property-ending-with-plugin.pom.xml", "property-ending-with-dependency.pom.xml");
    }

    @SuppressWarnings("checkstyle:VisibilityModifier")
    @Parameter(0)
    public String name;

    @Test
    public void shouldThrowNoError() throws Exception {
        final String pom = IOUtil.toString(getClass().getResourceAsStream(PATH + name));
        String tidyPom = new PomTidy().tidy(pom);
        assertEquals(pom, tidyPom, "nothing to tidy here");
    }
}
