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

import org.codehaus.mojo.tidy.format.Format;
import org.codehaus.mojo.tidy.format.FormatIdentifier;
import org.codehaus.stax2.XMLInputFactory2;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static java.util.Arrays.fill;
import static org.codehaus.plexus.util.StringUtils.countMatches;
import static org.codehaus.plexus.util.StringUtils.isWhitespace;
import static org.codehaus.plexus.util.StringUtils.repeat;

/**
 * Tidy up a POM into the canonical order.
 */
public class PomTidy
{
    private static final FormatIdentifier FORMAT_IDENTIFIER = new FormatIdentifier();

    private static final List<SectionSorter> SECTION_SORTERS = asList(
        new SectionSorter( "/project", new NodeGroup( "modelVersion" ), new NodeGroup( "parent" ),
                           new NodeGroup( "groupId", "artifactId", "version", "packaging" ),
                           new NodeGroup( "name", "description", "url", "inceptionYear", "organization", "licenses" ),
                           new NodeGroup( "developers", "contributors" ), new NodeGroup( "mailingLists" ),
                           new NodeGroup( "prerequisites" ), new NodeGroup( "modules" ),
                           new NodeGroup( "scm", "issueManagement", "ciManagement", "distributionManagement" ),
                           new NodeGroup( "properties" ), new NodeGroup( "repositories", "pluginRepositories" ),
                           new NodeGroup( "dependencyManagement", "dependencies" ), new NodeGroup( "build" ),
                           new NodeGroup( "reporting" ), new NodeGroup( "profiles" ) ),
        new SectionSorter( "/project/build", new NodeGroup( "defaultGoal", "sourceDirectory", "scriptSourceDirectory",
                                                            "testSourceDirectory", "directory", "outputDirectory",
                                                            "testOutputDirectory", "finalName", "filters", "resources",
                                                            "testResources", "pluginManagement", "plugins",
                                                            "extensions" ) ), new SectionSorter( "dependency",
                                                                                                 new NodeGroup(
                                                                                                     "groupId",
                                                                                                     "artifactId",
                                                                                                     "version", "type",
                                                                                                     "classifier",
                                                                                                     "scope",
                                                                                                     "systemPath",
                                                                                                     "exclusions",
                                                                                                     "optional" ) ),
        new SectionSorter( "exclusion", new NodeGroup( "groupId", "artifactId" ) ),
        new SectionSorter( "extension", new NodeGroup( "groupId", "artifactId", "version" ) ),
        new SectionSorter( "parent", new NodeGroup( "groupId", "artifactId", "version", "relativePath" ) ),
        new SectionSorter( "plugin", new NodeGroup( "groupId", "artifactId", "version" ) ),
        new SectionSorter( "relocation", new NodeGroup( "groupId", "artifactId", "version" ) ) );

    public String tidy( String pom )
        throws XMLStreamException
    {
        Format format = FORMAT_IDENTIFIER.identifyFormat( pom );
        pom = addXmlHeader( pom, format );
        pom = applySectionSorters( pom, format );
        return pom.trim() + format.getLineSeparator();
    }

    private String addXmlHeader( String input, Format format )
        throws XMLStreamException
    {
        if ( input.indexOf( "<?xml" ) != 0 )
        {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + format.getLineSeparator() + input;
        }

        return input;
    }

    private String applySectionSorters( String pom, Format format )
        throws XMLStreamException
    {
        for ( SectionSorter sorter : SECTION_SORTERS )
        {
            pom = sorter.sortSections( pom, format );
        }
        return pom;
    }

    private static class SectionSorter
    {
        final String scope;

        final NodeGroup[] groups;

        final String[] sequence;

        SectionSorter( String scope, NodeGroup... groups )
        {
            this.scope = scope;
            this.groups = groups;
            this.sequence = calculateSequence( groups );
        }

        String[] calculateSequence( NodeGroup[] groups )
        {
            List<String> sequence = new ArrayList<String>();
            for ( NodeGroup group : groups )
            {
                sequence.addAll( group.nodes );
            }
            return sequence.toArray( new String[sequence.size()] );
        }

        String sortSections( String pom, Format format )
            throws XMLStreamException
        {
            XMLEventReader reader = createEventReaderForPom( pom );
            String path = "";
            int posFirstUnformatted = 0;
            StringBuilder tidyPom = new StringBuilder();
            while ( reader.hasNext() )
            {
                XMLEvent event = reader.nextEvent();
                if ( event.isStartElement() )
                {
                    path += "/" + event.asStartElement().getName().getLocalPart();
                    if ( isStartElementOfScope( path ) )
                    {
                        int pos = getPosOfNextEvent( reader );
                        tidyPom.append( pom.substring( posFirstUnformatted, pos ) );
                        tidyPom.append( formatSection( reader, pom, format ) );
                        path = substringAfterLast( path, "/" );
                        posFirstUnformatted = getPosOfNextEvent( reader );
                    }
                }
                else if ( event.isEndElement() )
                {
                    path = substringAfterLast( path, "/" );
                }
            }
            tidyPom.append( pom.substring( posFirstUnformatted ) );
            return tidyPom.toString();
        }

        private XMLEventReader createEventReaderForPom( String pom )
            throws XMLStreamException
        {
            XMLInputFactory inputFactory = XMLInputFactory2.newInstance();
            inputFactory.setProperty( XMLInputFactory2.P_PRESERVE_LOCATION, true );
            return inputFactory.createXMLEventReader( new StringReader( pom ) );
        }

        private boolean isStartElementOfScope( String path )
        {
            if ( scope.startsWith( "/" ) )
            {
                return path.equals( scope );
            }
            else
            {
                return path.endsWith( scope );
            }
        }

        private String formatSection( XMLEventReader reader, String pom, Format format )
            throws XMLStreamException
        {
            int startOfSection = getPosOfNextEvent( reader );
            int[] starts = new int[sequence.length];
            int[] ends = new int[sequence.length];
            XMLEvent endScope = calculateStartsAndEnds( reader, starts, ends );
            return formatSection( reader, pom, format, startOfSection, starts, ends, endScope );
        }

        private XMLEvent calculateStartsAndEnds( XMLEventReader reader, int[] starts, int[] ends )
            throws XMLStreamException
        {
            fill( starts, Integer.MAX_VALUE );
            fill( ends, -1 );
            int level = 0;
            while ( reader.hasNext() )
            {
                XMLEvent event = reader.nextEvent();
                if ( event.isStartElement() )
                {
                    ++level;
                    if ( level == 1 )
                    {
                        QName name = event.asStartElement().getName();
                        if ( hasToBeSorted( name ) )
                        {
                            int i = getSequenceIndex( name );
                            starts[i] = event.getLocation().getCharacterOffset();
                        }
                    }
                }
                else if ( event.isEndElement() )
                {
                    if ( level == 0 )
                    {
                        return event;
                    }
                    else if ( level == 1 )
                    {
                        QName name = event.asEndElement().getName();
                        if ( hasToBeSorted( name ) )
                        {
                            int i = getSequenceIndex( name );
                            ends[i] = getPosOfNextEvent( reader );
                        }
                    }
                    --level;
                }
            }
            throw new RuntimeException( "End element missing." );
        }

        private boolean hasToBeSorted( QName nodeName )
        {
            String name = nodeName.getLocalPart();
            for ( String elementName : sequence )
            {
                if ( name.equals( elementName ) )
                {
                    return true;
                }
            }
            return false;
        }

        private int getSequenceIndex( QName nodeName )
        {
            String name = nodeName.getLocalPart();
            for ( int i = 0; i < sequence.length; i++ )
            {
                if ( name.equals( sequence[i] ) )
                {
                    return i;
                }
            }
            throw new IllegalArgumentException(
                "The path '" + nodeName + " does not specify an element of the sequence " + Arrays.toString( sequence )
                    + "." );
        }

        private String formatSection( XMLEventReader reader, String pom, Format format, int startOfSection,
                                      int[] starts, int[] ends, XMLEvent endScope )
            throws XMLStreamException
        {
            String outdent = calculateOutdent( pom, endScope );
            String indent = calculateIndent( pom, starts );
            int first = calculateFirst( starts, pom );
            StringBuilder output = new StringBuilder();
            output.append( pom.substring( startOfSection, first ).trim() );
            int i = 0;
            boolean firstGroupStarted = false;
            for ( NodeGroup group : groups )
            {
                boolean groupStarted = false;
                for ( String node : group.nodes )
                {
                    if ( starts[i] != Integer.MAX_VALUE )
                    {
                        if ( firstGroupStarted && !groupStarted )
                        {
                            output.append( format.getLineSeparator() );
                        }
                        addTextIfNotEmpty( output, indent, getPrecedingText( pom, starts[i], ends ), format );
                        addTextIfNotEmpty( output, indent, pom.substring( starts[i], ends[i] ), format );
                        firstGroupStarted = groupStarted = true;
                    }
                    ++i;
                }
            }
            int last = calculateLast( ends );
            int afterSection = getPosOfNextEvent( reader );
            int offsetEndElement = endScope.getLocation().getCharacterOffset();
            addTextIfNotEmpty( output, indent, pom.substring( last, offsetEndElement ), format );
            addTextIfNotEmpty( output, outdent, pom.substring( offsetEndElement, afterSection ), format );
            return output.toString();
        }

        private String calculateOutdent( String pom, XMLEvent endScope )
        {
            String before = pom.substring( 0, endScope.getLocation().getCharacterOffset() );
            return substringAfterLast( before, "\n" );
        }

        private int calculateFirst( int[] starts, String pom )
        {
            int first = pom.length();
            for ( int start : starts )
            {
                first = min( first, start );
            }
            return first;
        }

        private int calculateLast( int[] ends )
        {
            int last = 0;
            for ( int end : ends )
            {
                last = max( last, end );
            }
            return last;
        }

        private String calculateIndent( String input, int[] starts )
        {
            int numNodesWithSpaceIndent = 0;
            int numNodesWithTabIndent = 0;
            int spaceIndentTotal = 0;
            int tabIndentTotal = 0;
            for ( int start : starts )
            {
                if ( start != Integer.MAX_VALUE )
                {
                    String indent = calculateIndent( input, start );
                    if ( !indent.isEmpty() )
                    {
                        int numTabs = countMatches( indent, "\t" );
                        if ( numTabs == indent.length() )
                        {
                            ++numNodesWithTabIndent;
                            tabIndentTotal += numTabs;
                        }
                        else if ( !indent.contains( "\t" ) )
                        {
                            ++numNodesWithSpaceIndent;
                            spaceIndentTotal += indent.length();
                        }
                    }
                }
            }
            if ( numNodesWithSpaceIndent == 0 && numNodesWithTabIndent == 0 )
            {
                return "  ";
            }
            else if ( numNodesWithSpaceIndent > numNodesWithTabIndent )
            {
                int averageIndent = spaceIndentTotal / numNodesWithSpaceIndent;
                return repeat( " ", averageIndent );
            }
            else
            {
                int averageIndent = tabIndentTotal / numNodesWithTabIndent;
                return repeat( "\t", averageIndent );
            }
        }

        private String calculateIndent( String input, int startOfTag )
        {
            for ( int i = startOfTag; i > 1; --i )
            {
                String character = input.substring( i - 1, i );
                if ( !isWhitespace( character ) || "\n".equals( character ) || "\r".equals( character ) )
                {
                    return input.substring( i, startOfTag );
                }
            }
            return input;
        }

        private String getPrecedingText( String pom, int start, int[] ends )
        {
            int startPrecedingText = -1;
            for ( int end : ends )
            {
                if ( end < start )
                {
                    startPrecedingText = max( startPrecedingText, end );
                }
            }
            if ( startPrecedingText != -1 )
            {
                return pom.substring( startPrecedingText, start );
            }
            else
            {
                return "";
            }
        }

        private void addTextIfNotEmpty( StringBuilder output, String indent, String text, Format format )
        {
            String trimmedText = text.trim();
            if ( trimmedText.length() != 0 )
            {
                output.append( format.getLineSeparator() );
                output.append( indent );
                output.append( trimmedText );
            }
        }

        private int getPosOfNextEvent( XMLEventReader reader )
            throws XMLStreamException
        {
            return reader.peek().getLocation().getCharacterOffset();
        }

        private String substringAfterLast( String str, String separator )
        {
            int beginIndex = str.lastIndexOf( separator ) + 1;
            return str.substring( beginIndex );
        }
    }

    private static class NodeGroup
    {
        final List<String> nodes;

        NodeGroup( String... nodes )
        {
            this.nodes = asList( nodes );
        }
    }
}
