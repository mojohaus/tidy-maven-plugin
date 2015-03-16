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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import static org.codehaus.mojo.tidy.task.XMLEventReaderFactory.createEventReaderForPom;

class EnsureSingleLineProjectStartTag
    implements TidyTask
{
    private static final String PROJECT_START_TAG = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" "
        + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
        + "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">";

    @Override
    public String tidyPom( String pom, Format format )
        throws XMLStreamException
    {
        XMLEventReader eventReader = createEventReaderForPom( pom );
        try
        {
            return tidyProjectStartTag( pom, eventReader );
        }
        finally
        {
            eventReader.close();
        }
    }

    private String tidyProjectStartTag( String pom, XMLEventReader eventReader )
        throws XMLStreamException
    {
        while ( eventReader.hasNext() )
        {
            XMLEvent event = eventReader.nextEvent();
            if ( event.isStartElement() && event.asStartElement().getName().getLocalPart().equals( "project" ) )
            {
                return replaceProjectStartTag( pom, eventReader, event );
            }
        }
        throw new IllegalArgumentException( "The POM has no project node." );
    }

    private String replaceProjectStartTag( String pom, XMLEventReader eventReader, XMLEvent event )
        throws XMLStreamException
    {
        int start = event.getLocation().getCharacterOffset();
        int nextChar = eventReader.nextEvent().getLocation().getCharacterOffset();
        return pom.substring( 0, start ) + PROJECT_START_TAG + pom.substring( nextChar );
    }
}
