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
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.WriterFactory;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * Basic utility methods for reading and writing XML files
 */
public class Utils
{
    /**
     * Reads a file into a String.
     *
     * @param outFile The file to read.
     * @return String The content of the file.
     * @throws java.io.IOException when things go wrong.
     */
    public static StringBuilder readXmlFile( File outFile )
        throws IOException
    {
        Reader reader = ReaderFactory.newXmlReader( outFile );

        try
        {
            return new StringBuilder( IOUtil.toString( reader ) );
        }
        finally
        {
            IOUtil.close( reader );
        }
    }

    /**
     * Writes a StringBuffer into a file.
     *
     * @param outFile The file to read.
     * @param input   The contents of the file.
     * @throws java.io.IOException when things go wrong.
     */
    public static void writeXmlFile( File outFile, StringBuilder input )
        throws IOException
    {
        Writer writer = WriterFactory.newXmlWriter( outFile );
        try
        {
            IOUtil.copy( input.toString(), writer );
        }
        finally
        {
            IOUtil.close( writer );
        }
    }
}
