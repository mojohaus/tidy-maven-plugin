package org.codehaus.mojo.tidy;

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

import java.io.IOException;
import java.io.InputStream;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class PomTidyTest
{
    @Parameters(name = "{0}")
    public static Iterable<Object[]> tests()
    {
        return asList( new Object[]{ "add-xml-declaration" }, new Object[]{ "complete-pom" },
                       new Object[]{ "pom-space-indent" }, new Object[]{ "pom-tab-indent" },
                       new Object[]{ "pom-with-comments" }, new Object[]{ "pom-with-profiles" },
                       new Object[]{ "pom-with-reporting" } );
    }

    @Parameter( 0 )
    public String name;

    @Test
    public void generatesTidyPom()
        throws Exception
    {
        String pom = readPom( "pom.xml" );
        String tidyPom = new PomTidy().tidy( pom );
        assertEquals( readPom( "pom-expected.xml" ), tidyPom );
    }

    private String readPom( String filename )
        throws IOException
    {
        InputStream is = getClass().getResourceAsStream( name + "/" + filename );
        return IOUtil.toString( is );
    }
}
