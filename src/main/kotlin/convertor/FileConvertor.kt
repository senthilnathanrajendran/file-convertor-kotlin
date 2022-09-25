package convertor


import com.opencsv.CSVWriter
import com.opencsv.ICSVWriter
import extension.createEndElement
import extension.createStartElement
import model.Sentence
import java.io.File
import java.io.FileWriter
import java.util.*
import java.util.regex.Pattern
import javax.xml.stream.XMLEventFactory
import javax.xml.stream.XMLEventWriter
import javax.xml.stream.XMLOutputFactory


class FileConvertor {

    companion object {
        private const val REGEX_PATTERN_TO_SPLIT_TEXT_FILE = "(?<!Mr|Ms|Mrs|Dr|Jr)[.?!]"
        private const val REGEX_PATTERN_TO_SPLIT_SENTENCE = "[\\-_*#%&()|\\[\\]{}:+, @\\t\\\"\\r\\n]+"
        val PATTERN_TO_SPLIT_TEXT_FILE: Pattern = Pattern.compile(REGEX_PATTERN_TO_SPLIT_TEXT_FILE)
        val PATTERN_TO_SPLIT_SENTENCE: Pattern = Pattern.compile(REGEX_PATTERN_TO_SPLIT_SENTENCE)
    }

    fun generateXMLAndCSV(path: String) {
        val file = File(path)
        val parentPath = file.parent
        val fileName = file.name.split(".")[0]

        val (xef, xew) = createXML(parentPath, fileName)

        val csvWriter = createCSV(parentPath, fileName, path)

        var sentenceCount = 1
        val sc = Scanner(File(path)).useDelimiter(PATTERN_TO_SPLIT_TEXT_FILE)
        while (sc.hasNext()) {
            val line = sc.next().trim()
            if (line.isNotBlank()) {
                val words = PATTERN_TO_SPLIT_SENTENCE.split(line).toList()
                val sentence = Sentence(words.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it }))
                generateCSV(csvWriter, sentenceCount, sentence)
                generateXML(xef, xew, sentence)
                sentenceCount++
            }
        }
        closeXML(xef, xew)
        closeCSV(csvWriter)
        println("Generated CSV and XML files are stored in $parentPath location")
    }

    private fun createCSV(parentPath: String, fileName: String, path: String): CSVWriter {
        val csvWriter = CSVWriter(
            FileWriter("$parentPath/$fileName.csv"),
            ICSVWriter.DEFAULT_SEPARATOR,
            ICSVWriter.NO_QUOTE_CHARACTER,
            ICSVWriter.NO_ESCAPE_CHARACTER,
            ICSVWriter.DEFAULT_LINE_END
        )

        var maxLength = 0
        val sc = Scanner(File(path)).useDelimiter(PATTERN_TO_SPLIT_TEXT_FILE)
        while (sc.hasNext()) {
            val line = sc.next().trim()
            if (line.isNotBlank()) {
                val words = PATTERN_TO_SPLIT_SENTENCE.split(line).toList()
                if (maxLength < words.size) {
                    maxLength = words.size
                }
            }
        }
        val csvHeaders = (1..maxLength).mapIndexed { index, _ -> "Word ${index + 1}" }.toTypedArray()
        csvWriter.writeNext(arrayOf(" ") + csvHeaders)
        return csvWriter
    }

    private fun generateCSV(csvWriter: CSVWriter, sentenceCount: Int, sentence: Sentence) {
        csvWriter.writeNext(arrayOf("Sentence $sentenceCount") + sentence.words)
    }

    private fun closeCSV(csvWriter: CSVWriter) {
        csvWriter.close()
    }

    private fun createXML(parentPath: String, fileName: String): Pair<XMLEventFactory, XMLEventWriter> {
        val xof: XMLOutputFactory = XMLOutputFactory.newInstance()
        val xef = XMLEventFactory.newInstance()
        val xew = xof.createXMLEventWriter(FileWriter("$parentPath/$fileName.xml"))
        val xeo = xef.createStartDocument("UTF-8", "1.0", true)
        xew.add(xeo)
        val textStartElement = xef.createStartElement("text")
        xew.add(textStartElement)
        return Pair(xef, xew)
    }

    private fun generateXML(xef: XMLEventFactory, xew: XMLEventWriter, sentence: Sentence) {
        val sentenceStartElement = xef.createStartElement("sentence")
        xew.add(sentenceStartElement)

        sentence.words.forEach { word ->
            val wordStartElement = xef.createStartElement("word")
            xew.add(wordStartElement)
            val content = xef.createCharacters(word)
            xew.add(content)
            val wordEndElement = xef.createEndElement("word")
            xew.add(wordEndElement)
        }
        val sentenceEndElement = xef.createEndElement("sentence")
        xew.add(sentenceEndElement)
    }

    private fun closeXML(xef: XMLEventFactory, xew: XMLEventWriter) {
        val textEndElement = xef.createEndElement("text")
        xew.add(textEndElement)
        val ed = xef.createEndDocument()
        xew.add(ed)
        xew.close()
    }
}