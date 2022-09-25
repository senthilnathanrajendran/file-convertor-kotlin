package extension

import javax.xml.stream.XMLEventFactory
import javax.xml.stream.events.EndElement
import javax.xml.stream.events.StartElement


fun XMLEventFactory.createStartElement(name: String): StartElement {
    return this.createStartElement("", "", name)
}

fun XMLEventFactory.createEndElement(name: String): EndElement {
    return this.createEndElement("", "", name)
}