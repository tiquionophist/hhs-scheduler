package com.tiquionophist.io

import javax.xml.stream.XMLStreamReader
import javax.xml.stream.XMLStreamWriter

/**
 * Writes all the contents of this [XMLStreamReader] to [writer] element-by-element. Calls [onEvent] after mirroring
 * element, which may do its own processing to transform the XML.
 */
fun XMLStreamReader.mirrorTo(writer: XMLStreamWriter, intercept: () -> Boolean = { false }, onEvent: () -> Unit) {
    while (hasNext()) {
        if (!intercept()) {
            when (eventType) {
                XMLStreamReader.START_DOCUMENT -> writer.writeStartDocument()
                XMLStreamReader.END_DOCUMENT -> writer.writeEndDocument()
                XMLStreamReader.START_ELEMENT -> {
                    writer.writeStartElement(prefix, localName, namespaceURI)

                    for (attributeIndex in 0 until attributeCount) {
                        val prefix = getAttributePrefix(attributeIndex)
                        val namespaceURI = getAttributeNamespace(attributeIndex)
                        val localName = getAttributeLocalName(attributeIndex)
                        val value = getAttributeValue(attributeIndex)

                        writer.writeAttribute(prefix, namespaceURI, localName, value)
                    }

                    for (namespaceIndex in 0 until namespaceCount) {
                        val prefix = getNamespacePrefix(namespaceIndex)
                        val namespaceURI = getNamespaceURI(namespaceIndex)

                        writer.writeNamespace(prefix, namespaceURI)
                    }
                }
                XMLStreamReader.END_ELEMENT -> writer.writeEndElement()
                XMLStreamReader.CHARACTERS -> writer.writeCharacters(text)
                // TODO add remaining event types to be safe
                else -> error("unhandled XML event type: $eventType")
            }
        }

        onEvent()

        next()
    }

    close()
    writer.close()
}

/**
 * Reads this [XMLStreamReader] past the current start element. That is, continues reading events until an end tag is
 * found matching the current start element tag (accounting for nested elements with the same type).
 *
 * [onChildElement] is invoked for each child START_ELEMENT event within the current tag with the child's prefix, local
 * name, and namespace URI (respectively).
 */
fun XMLStreamReader.readUntilElementEnd(
    onChildElement: (prefix: String, localName: String, namespaceURI: String) -> Unit = { _, _, _ -> }
) {
    require(eventType == XMLStreamReader.START_ELEMENT)
    val startLocalName = localName
    val startPrefix = prefix
    val startNamespaceURI = namespaceURI

    var depth = 1

    while (hasNext()) {
        next()

        @Suppress("ComplexCondition")
        if (isStartElement &&
            prefix == startPrefix &&
            localName == startLocalName &&
            namespaceURI == startNamespaceURI
        ) {
            depth += 1
        } else if (isStartElement) {
            onChildElement(prefix, localName, namespaceURI)
        } else if (isEndElement &&
            prefix == startPrefix &&
            localName == startLocalName &&
            namespaceURI == startNamespaceURI
        ) {
            depth -= 1
            if (depth < 0) error("mismatching end elements for $prefix $localName $namespaceURI")
            if (depth == 0) return
        }
    }
}
