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

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.plexus.util.StringUtils;

import static org.codehaus.mojo.tidy.task.XMLEventReaderFactory.createEventReaderForPom;

class EnsureSingleLineProjectStartTag implements TidyTask {
    private static final String PROJECT_START_TAG = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" "
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
            + "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\"";

    private static final Collection<QName> PROJECT_4_0_ATTRIBUTES =
            Collections.singleton(new QName(null, "child.project.url.inherit.append.path"));

    private static final Collection<QName> PROJECT_4_1_ATTRIBUTES =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                    new QName(null, "child.project.url.inherit.append.path"),
                    new QName(null, "root"),
                    new QName(null, "preserve.model.version"))));

    private static final String PROJECT_4_1_START_TAG =
            "<project xmlns=\"http://maven.apache.org/POM/4.1.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                    + "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.1.0 https://maven.apache.org/xsd/maven-4.1.0.xsd\"";

    @Override
    public String tidyPom(String pom, Format format) throws XMLStreamException {
        final String modelVersion = identifyModelVersion(pom);
        if ("4.1.0".equals(modelVersion)) {
            return tidy41Pom(pom);
        }
        return tidy40Pom(pom);
    }

    private String tidy40Pom(String pom) throws XMLStreamException {
        XMLEventReader eventReader = createEventReaderForPom(pom);
        try {
            return tidyProjectStartTag(pom, eventReader, PROJECT_START_TAG, PROJECT_4_0_ATTRIBUTES);
        } finally {
            eventReader.close();
        }
    }

    private String tidy41Pom(String pom) throws XMLStreamException {
        XMLEventReader eventReader = createEventReaderForPom(pom);
        try {
            return tidyProjectStartTag(pom, eventReader, PROJECT_4_1_START_TAG, PROJECT_4_1_ATTRIBUTES);
        } finally {
            eventReader.close();
        }
    }

    private String identifyModelVersion(String pom) throws XMLStreamException {
        XMLEventReader eventReader = createEventReaderForPom(pom);
        try {
            return identifyModelVersion(eventReader);
        } finally {
            eventReader.close();
        }
    }

    private String identifyModelVersion(XMLEventReader eventReader) throws XMLStreamException {
        XMLEvent event = eventReader.nextTag();
        while (null != event) {
            if (event.isStartElement()) {
                if (event.asStartElement().getName().getLocalPart().equals("project")) {
                    return resolveModelVersion(eventReader);
                }
                event = skipNestedContent(eventReader);
            } else {
                event = eventReader.nextTag();
            }
        }
        return null;
    }

    private XMLEvent skipNestedContent(XMLEventReader eventReader) throws XMLStreamException {
        int nestedSize = 0;
        XMLEvent xmlEvent = eventReader.nextTag();
        while (xmlEvent != null && nestedSize >= 0) {
            if (xmlEvent.isEndElement()) {
                nestedSize--;
            } else {
                nestedSize++;
            }
            xmlEvent = eventReader.nextTag();
        }
        return xmlEvent;
    }

    private String resolveModelVersion(XMLEventReader eventReader) throws XMLStreamException {
        XMLEvent xmlEvent = eventReader.nextTag();
        while (xmlEvent != null && xmlEvent.isStartElement()) {
            if (xmlEvent.asStartElement().getName().getLocalPart().equals("modelVersion")) {
                return eventReader.getElementText();
            }
            xmlEvent = skipNestedContent(eventReader);
        }
        return null;
    }

    private String tidyProjectStartTag(
            String pom, XMLEventReader eventReader, String projectStartTag, Collection<QName> additionalProperties)
            throws XMLStreamException {
        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();
            if (event.isStartElement()
                    && event.asStartElement().getName().getLocalPart().equals("project")) {
                return replaceProjectStartTag(pom, eventReader, event, projectStartTag, additionalProperties);
            }
        }
        throw new IllegalArgumentException("The POM has no project node.");
    }

    private String replaceProjectStartTag(
            String pom,
            XMLEventReader eventReader,
            XMLEvent event,
            String startTag,
            Collection<QName> additionalProperties)
            throws XMLStreamException {
        int start = event.getLocation().getCharacterOffset();
        final Map<QName, String> additionalPropertiesMap;
        if (additionalProperties.isEmpty()) {
            additionalPropertiesMap = Collections.emptyMap();
        } else {
            additionalPropertiesMap = new HashMap<>(additionalProperties.size());
            final Iterator<Attribute> attributeIterator = event.asStartElement().getAttributes();
            while (attributeIterator.hasNext()) {
                final Attribute currentAttribute = attributeIterator.next();
                final QName attributeQualifiedName = currentAttribute.getName();
                if (additionalProperties.contains(attributeQualifiedName)) {
                    additionalPropertiesMap.put(attributeQualifiedName, currentAttribute.getValue());
                }
            }
        }
        int nextChar = eventReader.nextEvent().getLocation().getCharacterOffset();
        if (additionalPropertiesMap.isEmpty()) {
            return pom.substring(0, start) + startTag + ">" + pom.substring(nextChar);
        }
        StringBuilder result = new StringBuilder(pom.substring(0, start)).append(startTag);
        for (QName additionalProperty : additionalProperties) {
            final String value = additionalPropertiesMap.get(additionalProperty);
            if (value != null) {
                result.append(' ');
                if (additionalProperty.getPrefix().equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                    result.append(additionalProperty.getLocalPart());
                } else {
                    result.append(additionalProperty.getPrefix()).append(':').append(additionalProperty.getLocalPart());
                }
                result.append('=').append(StringUtils.quoteAndEscape(value, '"', new char[] {'"', '\\'}, '\\', true));
            }
        }
        return result.append('>').append(pom.substring(nextChar)).toString();
    }
}
