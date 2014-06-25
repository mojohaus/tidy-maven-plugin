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

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.stax2.XMLInputFactory2;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.StringReader;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * Tidy up a POM into the canonical order.
 */
public class PomTidy
{
    private static final String LS = System.getProperty( "line.separator" );

    private static final IndentCalculator SPACE_INDENT_CALCULATOR = new IndentCalculator( false );

    private static final IndentCalculator TAB_INDENT_CALCULATOR = new IndentCalculator( true );

    private static final String[][] SEQUENCE =
        { { "modelVersion", "" }, { "parent", LS }, { "groupId", LS }, { "artifactId", "" }, { "version", "" },
            { "packaging", "" }, { "name", LS }, { "description", "" }, { "url", "" }, { "inceptionYear", "" },
            { "organization", "" }, { "licenses", "" }, { "developers", LS }, { "contributors", "" },
            { "mailingLists", LS }, { "prerequisites", LS }, { "modules", LS }, { "scm", LS },
            { "issueManagement", "" }, { "ciManagement", "" }, { "distributionManagement", "" }, { "properties", LS },
            { "repositories", LS }, { "pluginRepositories", "" }, { "dependencyManagement", LS },
            { "dependencies", "" }, { "build", LS }, { "reporting", LS }, { "profiles", LS }, };

    private static final String[][] BUILD_SEQUENCE =
        { { "defaultGoal", "" }, { "sourceDirectory", "" }, { "scriptSourceDirectory", "" },
            { "testSourceDirectory", "" }, { "directory", "" }, { "outputDirectory", "" },
            { "testOutputDirectory", "" }, { "finalName", "" }, { "filters", "" }, { "resources", "" },
            { "testResources", "" }, { "pluginManagement", "" }, { "plugins", "" }, { "extensions", "" }, };

    public String tidy( String pom )
        throws XMLStreamException
    {
        pom = addXmlHeader( pom );
        pom = sortSections( pom, "/project/build", BUILD_SEQUENCE );
        return sortSections( pom, "/project", SEQUENCE );
    }

    private String addXmlHeader( String input )
        throws XMLStreamException
    {
        if ( input.indexOf( "<?xml" ) != 0 )
        {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LS + input;
        }

        return input;
    }

    private String sortSections( String input, String scope, String[][] sequence )
        throws XMLStreamException
    {
        XMLInputFactory inputFactory = XMLInputFactory2.newInstance();
        inputFactory.setProperty( XMLInputFactory2.P_PRESERVE_LOCATION, Boolean.TRUE );
        int first = Integer.MAX_VALUE, last = Integer.MIN_VALUE;
        int outdent = -1;
        int[] starts = new int[sequence.length];
        int[] ends = new int[sequence.length];
        for ( int i = 0; i < sequence.length; i++ )
        {
            Pattern matchScopeRegex = Pattern.compile( "\\Q" + scope + "\\E" );
            Pattern matchTargetRegex = Pattern.compile( "\\Q" + scope + "\\E/\\Q" + sequence[i][0] + "\\E" );

            Stack<String> stack = new Stack<String>();
            String path = "";
            boolean inMatchScope = false;
            int start = -1;
            starts[i] = ends[i] = -1;

            XMLEventReader pom = inputFactory.createXMLEventReader( new StringReader( input ) );

            while ( pom.hasNext() )
            {
                XMLEvent event = pom.nextEvent();
                if ( event.isStartElement() )
                {
                    stack.push( path );
                    final String elementName = event.asStartElement().getName().getLocalPart();
                    path = path + "/" + elementName;

                    if ( matchScopeRegex.matcher( path ).matches() )
                    {
                        // we're in a new match scope
                        // reset any previous partial matches
                        inMatchScope = true;
                        start = -1;
                    }
                    else if ( inMatchScope && matchTargetRegex.matcher( path ).matches() )
                    {
                        start = event.getLocation().getCharacterOffset();
                    }
                }
                if ( event.isEndElement() )
                {
                    if ( matchTargetRegex.matcher( path ).matches() && start != -1 )
                    {
                        starts[i] = start;
                        ends[i] = pom.peek().getLocation().getCharacterOffset();
                        first = Math.min( first, starts[i] );
                        last = Math.max( last, ends[i] );
                        break;
                    }
                    else if ( matchScopeRegex.matcher( path ).matches() )
                    {
                        if ( outdent == -1 )
                        {
                            outdent = SPACE_INDENT_CALCULATOR.getIndent( input, start,
                                                                         event.getLocation().getCharacterOffset() - 1 );
                        }
                        inMatchScope = false;
                        start = -1;
                    }
                    path = stack.pop();
                }
            }
        }

        int spaceIndentTotal = 0;
        int tabIndentTotal = 0;
        int indentCount = 0;
        int lastEnd = 0;
        for ( int i = 0; i < sequence.length; i++ )
        {
            if ( starts[i] != -1 )
            {
                int pos = starts[i] - 1;
                spaceIndentTotal += SPACE_INDENT_CALCULATOR.getIndent( input, lastEnd, pos );
                tabIndentTotal += TAB_INDENT_CALCULATOR.getIndent( input, lastEnd, pos );
                indentCount++;
            }
        }
        final int averageSpaceIndent = indentCount == 0 ? 2 : spaceIndentTotal / indentCount;
        final int averageTabIndent = indentCount == 0 ? 0 : tabIndentTotal / indentCount;
        String indent = StringUtils.repeat( "\t", averageTabIndent ) + StringUtils.repeat( " ", averageSpaceIndent );

        if ( first > last )
        {
            return input;
        }
        StringBuilder output = new StringBuilder( input.length() + 1024 );
        output.append( input.substring( 0, first ).trim() );
        String lastSep = null;
        for ( int i = 0; i < sequence.length; i++ )
        {
            if ( lastSep == null || !StringUtils.isWhitespace( sequence[i][1] )
                || lastSep.length() < sequence[i][1].length() )
            {
                output.append( lastSep = sequence[i][1] );
            }
            if ( starts[i] != -1 )
            {
                int l = -1;
                for ( int k = 0; k < sequence.length; k++ )
                {
                    if ( ends[k] != -1 && ( l == -1 || ends[l] < ends[k] ) && ends[k] < starts[i] )
                    {
                        l = k;
                    }
                }
                if ( l != -1 )
                {
                    output.append( input.substring( ends[l], starts[i] ).trim() );
                    lastSep = null;
                }
                output.append( LS );
                output.append( indent );
                output.append( input.substring( starts[i], ends[i] ).trim() );
            }
        }
        output.append( LS );
        if ( outdent > 0 )
        {
            output.append( StringUtils.repeat( " ", outdent ) );
        }
        output.append( input.substring( last ).trim() );
        output.append( LS );
        return output.toString();
    }

    private static class IndentCalculator
    {
        final boolean useTab;

        IndentCalculator( boolean useTab )
        {
            this.useTab = useTab;
        }

        int getIndent( String input, int lastEnd, int pos )
        {
            int indent = 0;
            String posChar;
            while ( pos > lastEnd && StringUtils.isWhitespace( posChar = input.substring( pos, pos + 1 ) ) )
            {
                if ( "\n".equals( posChar ) || "\r".equals( posChar ) )
                {
                    break;
                }
                if ( "\t".equals( posChar ) == useTab )
                {
                    indent++;
                }
                pos--;
            }
            return indent;
        }
    }
}
