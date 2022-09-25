import convertor.FileConvertor

fun main(args: Array<String>) {
    val fileConvertor = FileConvertor()
    while (true) {
        println(" Please input the source file location !")
        val path = readLine()
        if (path != null) {
            fileConvertor.generateXMLAndCSV(path)
        }
        println(" Do you want continue? If so please enter 'Y' or else click any other key !")
        val enteredString = readLine()
        if (enteredString != "Y") break
    }
}