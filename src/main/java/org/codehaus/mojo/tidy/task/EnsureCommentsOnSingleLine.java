/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.codehaus.mojo.tidy.task;

import static org.codehaus.mojo.tidy.task.XMLEventReaderFactory.createEventReaderForPom;

import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.Collections;

public class EnsureCommentsOnSingleLine implements TidyTask
{

    @Override
    public String tidyPom( String pom, Format format ) throws XMLStreamException
    {
        XMLEventReader eventReader = createEventReaderForPom( pom );
        try
        {
            return moveTrailingComments( pom, eventReader, format );
        }
        finally
        {
            eventReader.close();
        }
    }

    private String moveTrailingComments( String pom, XMLEventReader reader, Format format ) throws XMLStreamException
    {
        StringBuilder outputPom = new StringBuilder();
        XMLEvent previousEvent = null;
        int indent = 0;
        int startPrevEl = 0;
        int startCurrentEl;

        while ( reader.hasNext() )
        {
            XMLEvent event = reader.nextEvent();
            final Location location = event.getLocation();
            startCurrentEl = location.getCharacterOffset();
            switch ( event.getEventType() )
            {
                case XMLEvent.COMMENT:
                    final int startOfNextElement = getPosOfNextEvent( reader );

                    if ( previousElementInSameRow( location, previousEvent ))
                    {
                        final String whitespace = String.join( "", Collections.nCopies( indent, " " ) );
                        final String textToInsert =
                                pom.substring( startCurrentEl, startOfNextElement )
                                        + format.getLineSeparator()
                                        + whitespace;
                        outputPom.insert( startPrevEl, textToInsert );
                    }
                    else
                    {
                        outputPom.append( pom, startCurrentEl, startOfNextElement );
                    }

                    break;

                case XMLEvent.END_DOCUMENT:
                    outputPom.append( pom.substring( startCurrentEl ) );
                    break;

                case XMLEvent.START_ELEMENT:
                    startPrevEl = startCurrentEl;
                    indent = location.getColumnNumber() - 1;
                    // pass-through
                default:
                    final int next = getPosOfNextEvent( reader );
                    previousEvent = event;

                    outputPom.append( pom, startCurrentEl, next );
                    break;
            }
        }

        return outputPom.toString();
    }

    private boolean previousElementInSameRow( Location location, XMLEvent previousElement )
    {
        if ( previousElement == null )
        {
            return false;
        }

        return location.getLineNumber() == previousElement.getLocation().getLineNumber();
    }

    private boolean nextElementInSameRow( Location location, XMLEventReader reader ) throws XMLStreamException
    {
        final XMLEvent peek = reader.peek();

        if ( peek == null )
        {
            return false;
        }

        if ( peek.getEventType() != XMLEvent.START_ELEMENT )
        {
            return false;
        }

        return peek.getLocation().getLineNumber() == location.getLineNumber();
    }

    private int getPosOfNextEvent( XMLEventReader reader )
            throws XMLStreamException
    {
        return reader.peek().getLocation().getCharacterOffset();
    }
}
