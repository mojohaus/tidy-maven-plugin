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

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Identifies the output format for a given POM. It tries to find a
 * format that is similar to the current format.
 */
class FormatIdentifier {
    private static final List<String> LINE_SEPARATORS = asList("\r\n", "\n", "\r");

    /**
     * Identifies the output format for the given POM.
     *
     * @param pom the POM.
     * @return the output format.
     */
    Format identifyFormat(String pom) {
        for (String separator : LINE_SEPARATORS) {
            if (pom.contains(separator)) {
                return new Format(separator);
            }
        }

        throw new IllegalArgumentException("The pom.xml has no known line separator.");
    }
}
