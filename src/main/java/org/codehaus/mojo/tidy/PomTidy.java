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
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.stax2.XMLInputFactory2;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static java.lang.Math.max;
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

    private static final SectionSorter PROJECT_SORTER =
        new SectionSorter( "/project", new NodeGroup( "modelVersion" ), new NodeGroup( "parent" ),
                           new NodeGroup( "groupId", "artifactId", "version", "packaging" ),
                           new NodeGroup( "name", "description", "url", "inceptionYear", "organization", "licenses" ),
                           new NodeGroup( "developers", "contributors" ), new NodeGroup( "mailingLists" ),
                           new NodeGroup( "prerequisites" ), new NodeGroup( "modules" ),
                           new NodeGroup( "scm", "issueManagement", "ciManagement", "distributionManagement" ),
                           new NodeGroup( "properties" ), new NodeGroup( "repositories", "pluginRepositories" ),
                           new NodeGroup( "dependencyManagement", "dependencies" ), new NodeGroup( "build" ),
                           new NodeGroup( "reporting" ), new NodeGroup( "profiles" ) );

    private static final SectionSorter BUILD_SORTER = new SectionSorter( "/project/build", new NodeGroup( "defaultGoal",
                                                                                                          "sourceDirectory",
                                                                                                          "scriptSourceDirectory",
                                                                                                          "testSourceDirectory",
                                                                                                          "directory",
                                                                                                          "outputDirectory",
                                                                                                          "testOutputDirectory",
                                                                                                          "finalName",
                                                                                                          "filters",
                                                                                                          "resources",
                                                                                                          "testResources",
                                                                                                          "pluginManagement",
                                                                                                          "plugins",
                                                                                                          "extensions" ) );

    public String tidy( String pom )
        throws XMLStreamException
    {
        Format format = FORMAT_IDENTIFIER.identifyFormat( pom );
        pom = addXmlHeader( pom, format );
        pom = BUILD_SORTER.sortSections( pom, format );
        return PROJECT_SORTER.sortSections( pom, format );
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

        String sortSections( String input, Format format )
            throws XMLStreamException
        {
            XMLInputFactory inputFactory = XMLInputFactory2.newInstance();
            inputFactory.setProperty( XMLInputFactory2.P_PRESERVE_LOCATION, Boolean.TRUE );
            int first = Integer.MAX_VALUE, last = Integer.MIN_VALUE;
            String outdent = "";
            int[] starts = new int[sequence.length];
            fill( starts, -1 );
            int[] ends = new int[sequence.length];
            fill( ends, -1 );
            Stack<String> stack = new Stack<String>();
            String path = "";
            XMLEventReader pom = inputFactory.createXMLEventReader( new StringReader( input ) );
            while ( pom.hasNext() )
            {
                XMLEvent event = pom.nextEvent();
                if ( event.isStartElement() )
                {
                    stack.push( path );
                    final String elementName = event.asStartElement().getName().getLocalPart();
                    path = path + "/" + elementName;

                    if ( hasToBeSorted( path ) )
                    {
                        int i = getSequenceIndex( path );
                        starts[i] = event.getLocation().getCharacterOffset();
                        first = Math.min( first, starts[i] );
                    }
                }
                if ( event.isEndElement() )
                {
                    if ( hasToBeSorted( path ) )
                    {
                        int i = getSequenceIndex( path );
                        ends[i] = pom.peek().getLocation().getCharacterOffset();
                        last = max( last, ends[i] );
                    }
                    else if ( scope.equals( path ) )
                    {
                        String before = input.substring( 0, event.getLocation().getCharacterOffset() );
                        int posLineStart = max( before.lastIndexOf( '\n' ), before.lastIndexOf( '\n' ) );
                        outdent = before.substring( posLineStart + 1 );
                    }
                    path = stack.pop();
                }
            }

            String indent = calculateIndent( input, starts );

            if ( first > last )
            {
                return input;
            }
            StringBuilder output = new StringBuilder( input.length() + 1024 );
            output.append( input.substring( 0, first ).trim() );
            int i = 0;
            boolean firstGroupStarted = false;
            for ( NodeGroup group : groups )
            {
                boolean groupStarted = false;
                for ( int j = i; j < i + group.nodes.size(); ++j )
                {
                    if ( starts[j] != -1 )
                    {
                        if ( firstGroupStarted && !groupStarted )
                        {
                            output.append( format.getLineSeparator() );
                        }
                        addTextIfNotEmpty( output, indent, getPrecedingText( input, starts[j], ends ), format );
                        addTextIfNotEmpty( output, indent, input.substring( starts[j], ends[j] ), format );
                        firstGroupStarted = groupStarted = true;
                    }
                }
                i += group.nodes.size();
            }
            addTextIfNotEmpty( output, outdent, input.substring( last ), format );
            output.append( format.getLineSeparator() );
            return output.toString();
        }

        private boolean hasToBeSorted( String path )
        {
            for ( String elementName : sequence )
            {
                if ( path.equals( scope + "/" + elementName ) )
                {
                    return true;
                }
            }
            return false;
        }

        private int getSequenceIndex( String path )
        {
            String name = path.substring( path.lastIndexOf( "/" ) + 1 );
            for ( int i = 0; i < sequence.length; i++ )
            {
                if ( name.equals( sequence[i] ) )
                {
                    return i;
                }
            }
            throw new IllegalArgumentException(
                "The path '" + path + " does not specify an element of the sequence " + Arrays.toString( sequence )
                    + "." );
        }

        private String calculateIndent( String input, int[] starts )
        {
            int numNodesWithSpaceIndent = 0;
            int numNodesWithTabIndent = 0;
            int spaceIndentTotal = 0;
            int tabIndentTotal = 0;
            for ( int start : starts )
            {
                if ( start != -1 )
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
