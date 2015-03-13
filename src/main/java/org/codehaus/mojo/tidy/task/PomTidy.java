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
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Tidy up a POM into the canonical order.
 */
public class PomTidy
{
    private static final FormatIdentifier FORMAT_IDENTIFIER = new FormatIdentifier();

    private static final List<TidyTask> TIDY_TASKS =
        asList( new EnsureXmlHeader(), new EnsureOrderAndIndent(), new EnsureTrailingNewLine() );

    public String tidy( String pom )
        throws XMLStreamException
    {
        Format format = FORMAT_IDENTIFIER.identifyFormat( pom );
        for ( TidyTask task : TIDY_TASKS )
        {
            pom = task.tidyPom( pom, format );
        }
        return pom;
    }
}
