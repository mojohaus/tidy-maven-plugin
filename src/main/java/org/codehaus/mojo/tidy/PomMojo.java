package org.codehaus.mojo.tidy;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.stax2.XMLInputFactory2;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.StringReader;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * Tidy up the <code>pom.xml</code> into the canonical order.
 */
@Mojo(name = "pom", requiresProject = true, requiresDirectInvocation = true)
public class PomMojo extends AbstractMojo {

    /**
     * The Maven Project.
     */
    @Component
    private MavenProject project;

    private static final String[][] sequence = {
            {"modelVersion", ""},
            {"parent", "\n"},
            {"groupId", "\n"},
            {"artifactId", ""},
            {"version", ""},
            {"packaging", ""},
            {"name", "\n"},
            {"description", ""},
            {"url", ""},
            {"inceptionYear", ""},
            {"organization", ""},
            {"licenses", ""},
            {"developers", "\n"},
            {"contributors", ""},
            {"mailingLists", "\n"},
            {"prerequisites", "\n"},
            {"modules", "\n"},
            {"scm", "\n"},
            {"issueManagement", ""},
            {"ciManagement", ""},
            {"distributionManagement", ""},
            {"properties", "\n"},
            {"repositories", "\n"},
            {"pluginRepositories", ""},
            {"dependencyManagement", "\n"},
            {"dependencies", ""},
            {"build", "\n"},
            {"reporting", "\n"},
            {"profiles", "\n"},
    };

    private static final String[][] buildSequence = {
            {"defaultGoal", "\n"},
            {"sourceDirectory", ""},
            {"scriptSourceDirectory", ""},
            {"testSourceDirectory", "\n"},
            {"directory", ""},
            {"outputDirectory", ""},
            {"testOutputDirectory", "\n"},
            {"finalName", "\n"},
            {"filters", ""},
            {"resources", ""},
            {"testResources", "\n"},
            {"pluginManagement", ""},
            {"plugins", "\n"},
            {"extensions", ""},
    };

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {

            StringBuilder input = Utils.readXmlFile(project.getFile());
            input = sortSections(input, "/project/build", buildSequence);
            input = sortSections(input, "/project", sequence);

            Utils.writeXmlFile(project.getFile(), input);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (XMLStreamException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private StringBuilder sortSections(StringBuilder input, String scope, String[][] sequence)
            throws XMLStreamException {
        XMLInputFactory inputFactory = XMLInputFactory2.newInstance();
        inputFactory.setProperty(XMLInputFactory2.P_PRESERVE_LOCATION, Boolean.TRUE);
        String inputStr = input.toString();
        int first = Integer.MAX_VALUE, last = Integer.MIN_VALUE;
        int outdent = -1;
        int[] starts = new int[sequence.length];
        int[] ends = new int[sequence.length];
        for (int i = 0; i < sequence.length; i++) {
            Pattern matchScopeRegex = Pattern.compile("\\Q" + scope + "\\E");
            Pattern matchTargetRegex = Pattern.compile("\\Q" + scope + "\\E/\\Q" + sequence[i][0] + "\\E");

            Stack<String> stack = new Stack<String>();
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
                    path = path + "/" + elementName;

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
                        if (outdent == -1) {
                            outdent = getIndent(input, start, event.getLocation().getCharacterOffset() - 1);
                        }
                        inMatchScope = false;
                        start = -1;
                    }
                    path = stack.pop();
                }
            }

        }
        int indentTotal = 0;
        int indentCount = 0;
        int lastEnd = 0;
        for (int i = 0; i < sequence.length; i++) {
            if (starts[i] != -1) {
                int pos = starts[i] - 1;
                int indent = getIndent(input, lastEnd, pos);
                indentTotal += indent;
                indentCount++;
            }
        }
        getLog().debug("Average indent: " + (indentCount == 0 ? 2 : indentTotal / indentCount));
        String indent = StringUtils.repeat(" ", indentCount == 0 ? 2 : indentTotal / indentCount);
        if (first > last) {
            return input;
        }
        StringBuilder output = new StringBuilder(input.length() + 1024);
        output.append(inputStr.substring(0, first).trim());
        String lastSep = null;
        for (int i = 0; i < sequence.length; i++) {
            if (lastSep == null || !StringUtils.isWhitespace(sequence[i][1]) || lastSep.length() < sequence[i][1]
                    .length()) {
                output.append(lastSep = sequence[i][1]);
            }
            if (starts[i] != -1) {
                int l = -1;
                for (int k = 0; k < sequence.length; k++) {
                    if (ends[k] != -1 && (l == -1 || ends[l] < ends[k]) && ends[k] < starts[i]) {
                        l = k;
                    }
                }
                if (l != -1) {
                    output.append(inputStr.substring(ends[l], starts[i]).trim());
                    lastSep = null;
                }
                output.append("\n");
                output.append(indent);
                output.append(inputStr.substring(starts[i], ends[i]).trim());
            }
        }
        output.append("\n");
        if (outdent > 0) {
            output.append(StringUtils.repeat(" ", outdent));
        }
        output.append(inputStr.substring(last).trim());
        output.append("\n");
        return output;
    }

    private int getIndent(StringBuilder input, int lastEnd, int pos) {
        int indent = 0;
        String posChar;
        while (pos > lastEnd && StringUtils.isWhitespace(posChar = input.substring(pos, pos + 1))) {
            if ("\n".equals(posChar) || "\r".equals(posChar)) {
                break;
            }
            indent++;
            pos--;
        }
        return indent;
    }

}
