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

import static org.codehaus.plexus.util.StringUtils.repeat;

/**
 * Rewrites the leading whitespace of the lines that start a tag, a comment or a processing instruction to two spaces
 * per nesting level, and of the lines that continue a tag to the indentation of the tag plus four spaces. The text of
 * an element, the lines inside a comment or a CDATA section and the lines inside an attribute value stay as they are.
 */
final class PomIndenter {
    private static final String LEVEL = "  ";

    private static final String WRAP = "    ";

    private final String pom;

    private final StringBuilder out;

    private int copied;

    private PomIndenter(String pom) {
        this.pom = pom;
        this.out = new StringBuilder(pom.length());
    }

    static String indent(String pom) {
        return new PomIndenter(pom).run();
    }

    private String run() {
        int n = pom.length();
        int depth = 0;
        int i = 0;
        // true while the text since the last tag holds anything but whitespace; such a line is left alone
        boolean text = false;
        while (i < n) {
            char c = pom.charAt(i);
            if (c != '<') {
                text |= !Character.isWhitespace(c);
                i++;
            } else if (pom.startsWith("<![CDATA[", i)) {
                i = after("]]>", i + 9);
                text = true;
            } else if (pom.startsWith("<!--", i)) {
                reindentLine(i, text, repeat(LEVEL, depth));
                i = after("-->", i + 4);
            } else if (pom.startsWith("<?", i) || pom.startsWith("<!", i)) {
                reindentLine(i, text, repeat(LEVEL, depth));
                i = after(">", i + 2);
            } else {
                boolean closing = pom.startsWith("</", i);
                if (closing) {
                    depth = Math.max(0, depth - 1);
                }
                String tagIndent = reindentLine(i, text, repeat(LEVEL, depth));
                int end = endOfTag(i, tagIndent);
                boolean selfClosing = end > 1 && pom.charAt(end - 2) == '/';
                if (!closing && !selfClosing) {
                    depth++;
                }
                i = end;
                text = false;
            }
        }
        out.append(pom, copied, n);
        return out.toString();
    }

    /**
     * Gives the line that holds the tag at pos the indentation, if the tag is the first thing on it. Returns the
     * indentation the line has in the result.
     */
    private String reindentLine(int pos, boolean text, String indent) {
        int lineStart = pom.lastIndexOf('\n', pos - 1) + 1;
        if (text || leadingEnd(lineStart) < pos) {
            return pom.substring(lineStart, leadingEnd(lineStart));
        }
        out.append(pom, copied, lineStart).append(indent);
        copied = pos;
        return indent;
    }

    /** Returns the position after the closing '>' of the tag at start, re-indenting the lines that wrap it. */
    private int endOfTag(int start, String tagIndent) {
        int n = pom.length();
        char quote = 0;
        for (int j = start + 1; j < n; j++) {
            char d = pom.charAt(j);
            if (quote != 0) {
                quote = d == quote ? 0 : quote;
            } else if (d == '"' || d == '\'') {
                quote = d;
            } else if (d == '>') {
                return j + 1;
            } else if (d == '\n') {
                int first = leadingEnd(j + 1);
                if (first < n && pom.charAt(first) != '\n' && pom.charAt(first) != '\r') {
                    out.append(pom, copied, j + 1).append(tagIndent).append(WRAP);
                    copied = first;
                }
            }
        }
        return n;
    }

    private int after(String token, int from) {
        int index = pom.indexOf(token, from);
        return index < 0 ? pom.length() : index + token.length();
    }

    private int leadingEnd(int lineStart) {
        int k = lineStart;
        while (k < pom.length() && (pom.charAt(k) == ' ' || pom.charAt(k) == '\t')) {
            k++;
        }
        return k;
    }
}
