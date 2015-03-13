package org.codehaus.mojo.tidy.task;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Identifies the output format for a given POM. It tries to find a
 * format that is similar to the current format.
 */
public class FormatIdentifier
{
    private static final List<String> LINE_SEPARATORS = asList( "\r\n", "\n", "\r" );

    /**
     * Identifies the output format for the given POM.
     *
     * @param pom the POM.
     * @return the output format.
     */
    public Format identifyFormat( String pom )
    {
        for ( String separator : LINE_SEPARATORS )
        {
            if ( pom.contains( separator ) )
            {
                return new Format( separator );
            }
        }

        throw new IllegalArgumentException( "The pom.xml has no known line separator." );
    }
}
