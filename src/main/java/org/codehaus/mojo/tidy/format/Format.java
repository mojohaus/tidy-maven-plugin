package org.codehaus.mojo.tidy.format;

/**
 * Specification of the output format used by the Tidy plugin.
 */
public class Format
{
    private final String lineSeparator;

    /**
     * Creates a new output format.
     *
     * @param lineSeparator the line separator.
     */
    public Format( String lineSeparator )
    {
        this.lineSeparator = lineSeparator;
    }

    /**
     * Returns the characters that should be used as line separator.
     *
     * @return the characters that should be used as line separator.
     */
    public String getLineSeparator()
    {
        return lineSeparator;
    }
}
