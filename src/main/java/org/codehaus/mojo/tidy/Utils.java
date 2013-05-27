package org.codehaus.mojo.tidy;

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
public class Utils {
    /**
     * Reads a file into a String.
     *
     * @param outFile The file to read.
     * @return String The content of the file.
     * @throws java.io.IOException when things go wrong.
     */
    public static StringBuilder readXmlFile(File outFile)
            throws IOException {
        Reader reader = ReaderFactory.newXmlReader(outFile);

        try {
            return new StringBuilder(IOUtil.toString(reader));
        } finally {
            IOUtil.close(reader);
        }
    }

    /**
     * Writes a StringBuffer into a file.
     *
     * @param outFile The file to read.
     * @param input   The contents of the file.
     * @throws java.io.IOException when things go wrong.
     */
    public static void writeXmlFile(File outFile, StringBuilder input)
            throws IOException {
        Writer writer = WriterFactory.newXmlWriter(outFile);
        try {
            IOUtil.copy(input.toString(), writer);
        } finally {
            IOUtil.close(writer);
        }
    }
}
