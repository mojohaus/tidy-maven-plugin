package org.codehaus.mojo.tidy;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.stax2.XMLInputFactory2;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * Tidy up the <code>pom.xml</code> into the canonical order.
 *
 * @goal pom
 */
public class PomMojo extends AbstractMojo {

    /**
     * The Maven Project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    private static final String[][] sequence = {
            {"modelVersion", ""},
            {"parent", "\n"},
            {"groupId", ""},
            {"artifactId", ""},
            {"version", ""},
            {"packaging", ""},
            {"modules", ""},
            {"properties", ""},
            {"dependencyManagement", "\n"},
            {"dependencies", ""},
            {"name", "\n"},
            {"description", ""},
            {"url", ""},
            {"inceptionYear", ""},
            {"developers", "\n"},
            {"contributors", ""},
            {"licenses", "\n"},
            {"organization", "\n"},
            {"build", "\n"},
            {"reporting", "\n"},
            {"issueManagement", "\n"},
            {"ciManagement", "\n"},
            {"mailingLists", "\n"},
            {"scm", "\n"},
            {"prerequisites", "\n"},
            {"repositories", "\n"},
            {"pluginRepositories", "\n"},
            {"distributionManagement", "\n"},
            {"profiles", "\n"},
    };

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            XMLInputFactory inputFactory = XMLInputFactory2.newInstance();
            inputFactory.setProperty(XMLInputFactory2.P_PRESERVE_LOCATION, Boolean.TRUE);

            StringBuffer input = readXmlFile(project.getFile());
            String inputStr = input.toString();
            int first = Integer.MAX_VALUE, last = Integer.MIN_VALUE;
            int[] starts = new int[sequence.length];
            int[] ends = new int[sequence.length];
            for (int i = 0; i < sequence.length; i++) {
                Pattern matchScopeRegex = Pattern.compile("/project");
                Pattern matchTargetRegex = Pattern.compile("/project/\\Q" + sequence[i][0] + "\\E");

                Stack stack = new Stack();
                String path = "";
                boolean inMatchScope = false;
                int start = -1;
                starts[i] = ends[i] = -1;

                XMLEventReader pom = inputFactory.createXMLEventReader(new StringReader(inputStr));

                while (pom.hasNext()) {
                    XMLEvent event = pom.nextEvent();
                    if (event.isStartElement()) {
                        stack.push(path);
                        final String elementName = event.asStartElement().getName().getLocalPart();
                        path = new StringBuffer().append(path).append("/").append(elementName).toString();

                        if (matchScopeRegex.matcher(path).matches()) {
                            // we're in a new match scope
                            // reset any previous partial matches
                            inMatchScope = true;
                            start = -1;
                        } else if (inMatchScope && matchTargetRegex.matcher(path).matches()) {
                            start = event.getLocation().getCharacterOffset();
                        }
                    }
                    if (event.isEndElement()) {
                        if (matchTargetRegex.matcher(path).matches() && start != -1) {
                            starts[i] = start;
                            ends[i] = pom.peek().getLocation().getCharacterOffset();
                            first = Math.min(first, starts[i]);
                            last = Math.max(last, ends[i]);
                            break;
                        } else if (matchScopeRegex.matcher(path).matches()) {
                            inMatchScope = false;
                            start = -1;
                        }
                        path = (String) stack.pop();
                    }
                }

            }
            input = new StringBuffer(input.length() + 1024);
            input.append(inputStr.substring(0, first).trim());
            String lastSep = null;
            for (int i = 0; i < sequence.length; i++) {
                if (lastSep == null || !StringUtils.isWhitespace(sequence[i][1]) || lastSep.length() < sequence[i][1].length()) {
                    input.append(lastSep = sequence[i][1]);
                }
                if (starts[i] != -1) {
                    int l = -1;
                    for (int k = 0; k < sequence.length; k++) {
                        if (ends[k] != -1 && (l == -1 || ends[l] < ends[k]) && ends[k] < starts[i]) {
                            l = k;
                        }
                    }
                    if (l != -1) {
                        input.append(inputStr.substring(ends[l], starts[i]).trim());
                        lastSep = null;
                    }
                    input.append("\n  ");
                    input.append(inputStr.substring(starts[i], ends[i]).trim());
                }
            }
            input.append(inputStr.substring(last));

            writeFile( project.getFile(), input );
        } catch (IOException e) {
            getLog().error(e);
        } catch (XMLStreamException e) {
            getLog().error(e);
        }
    }


    /**
     * Reads a file into a String.
     *
     * @param outFile The file to read.
     * @return String The content of the file.
     * @throws java.io.IOException when things go wrong.
     */
    public static StringBuffer readXmlFile(File outFile)
            throws IOException {
        Reader reader = ReaderFactory.newXmlReader(outFile);

        try {
            return new StringBuffer(IOUtil.toString(reader));
        } finally {
            IOUtil.close(reader);
        }
    }

    /**
     * Writes a StringBuffer into a file.
     *
     * @param outFile The file to read.
     * @param input   The contents of the file.
     * @throws IOException when things go wrong.
     */
    protected final void writeFile(File outFile, StringBuffer input)
            throws IOException {
        Writer writer = WriterFactory.newXmlWriter(outFile);
        try {
            IOUtil.copy(input.toString(), writer);
        } finally {
            IOUtil.close(writer);
        }
    }


}
