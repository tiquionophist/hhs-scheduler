package com.tiquionophist.io

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.tiquionophist.core.Teacher
import com.tiquionophist.ui.ComputedSchedule
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.io.File
import java.io.InputStream
import java.sql.Connection
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamReader
import javax.xml.stream.XMLStreamWriter

object SaveFileIO {
    private object SaveInfoTable : Table(name = "hhs_SaveInfo") {
        val data: Column<ExposedBlob> = blob("Data")
    }

    /**
     * The required number of periods per week in save game files, since they are hardcoded to 5 days/week and 4
     * periods/day.
     */
    private const val PERIODS_PER_WEEK = 20

    val xmlMapper: XmlMapper = XmlMapper.builder()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .addModule(kotlinModule()) // allow non-empty constructors and other Kotlin compatibility
        .build()

    /**
     * Extracts the game "data" from the given [file]; this data is the main contents of the game save which is encoded
     * as a blob in either the files top-level XML structure or SQLite database.
     */
    fun extractData(file: File, onReadSaveFile: () -> Unit = {}): InputStream {
        return when (SaveFileType.ofFile(file)) {
            SaveFileType.XML -> {
                val saveFile = xmlMapper.readValue(file, SaveFile::class.java)

                onReadSaveFile()

                EncodingUtil.decodeAndUnzip(saveFile.data)
            }

            SaveFileType.SQL -> {
                val blob = transaction(openSqliteDb(file)) {
                    val saveInfos = SaveInfoTable
                        .selectAll()
                        .toList()

                    require(saveInfos.size == 1) {
                        "expecting exactly one ${SaveInfoTable.tableName} row; found ${saveInfos.size}"
                    }

                    saveInfos.first()[SaveInfoTable.data]
                }

                onReadSaveFile()

                EncodingUtil.unzip(blob.inputStream)
            }
        }
    }

    /**
     * Reads the game save at [file] into a [SaveData] which wraps the XML data in the save file.
     */
    fun read(file: File, onReadSaveFile: () -> Unit = {}, onDecodeAndUnzip: () -> Unit = {}): SaveData {
        extractData(file, onReadSaveFile = onReadSaveFile).use { inputStream ->
            onDecodeAndUnzip()
            return xmlMapper.readValue(inputStream, SaveData::class.java)
        }
    }

    /**
     * Write a copy of the game save [sourceFile] into [destinationFile] which incorporates the schedule and teacher
     * assignments from [ComputedSchedule].
     */
    fun write(schedule: ComputedSchedule, sourceFile: File, destinationFile: File) {
        require(schedule.configuration.periodsPerWeek == PERIODS_PER_WEEK) {
            "must have $PERIODS_PER_WEEK periods/week"
        }

        @Suppress("TooGenericExceptionCaught")
        try {
            when (SaveFileType.ofFile(sourceFile)) {
                SaveFileType.XML -> copyXmlSaveFile(schedule, sourceFile, destinationFile)
                SaveFileType.SQL -> copySqlSaveFile(schedule, sourceFile, destinationFile)
            }
        } catch (ex: Throwable) {
            // attempt to delete partially-created destination file if an exception was thrown
            try {
                destinationFile.delete()
            } catch (ignored: Throwable) {}

            throw ex
        }
    }

    private fun copyXmlSaveFile(schedule: ComputedSchedule, sourceFile: File, destinationFile: File) {
        sourceFile.inputStream().use { inputStream ->
            val reader: XMLStreamReader = XMLInputFactory.newFactory().createXMLStreamReader(inputStream)
            destinationFile.outputStream().use { outputStream ->
                val writer = XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream)
                reader.mirrorTo(writer) {
                    if (reader.isStartElement && reader.localName == "Data") {
                        val elementText = reader.elementText
                        EncodingUtil.decodeAndUnzip(elementText).use { unzippedStream ->
                            val encoded = EncodingUtil.zipAndEncode { zippedStream ->
                                val dataReader = XMLInputFactory.newFactory().createXMLStreamReader(unzippedStream)
                                val dataWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(zippedStream)

                                transformSaveData(schedule = schedule, reader = dataReader, writer = dataWriter)
                            }

                            writer.writeCharacters(encoded)
                            writer.writeEndElement()
                        }

                        require(reader.isEndElement)
                        reader.next()
                    }
                }
            }
        }
    }

    private fun copySqlSaveFile(schedule: ComputedSchedule, sourceFile: File, destinationFile: File) {
        sourceFile.copyTo(destinationFile, overwrite = true)

        val newData = extractData(sourceFile).use { originalStream ->
            EncodingUtil.zip { zippedStream ->
                val dataReader = XMLInputFactory.newFactory().createXMLStreamReader(originalStream)
                val dataWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(zippedStream)

                transformSaveData(schedule = schedule, reader = dataReader, writer = dataWriter)
            }
        }

        transaction(openSqliteDb(destinationFile)) {
            SaveInfoTable.update {
                it[data] = ExposedBlob(newData)
            }
        }
    }

    /**
     * Writes a copy of the decoded and unzipped save data from [reader] to [writer], replacing schedule and teacher
     * assignment elements according to [schedule].
     */
    private fun transformSaveData(
        schedule: ComputedSchedule,
        reader: XMLStreamReader,
        writer: XMLStreamWriter
    ) {
        var inPersonList = false
        var inForename = false
        var inLastname = false

        var currentFirstName: String? = null
        var currentLastName: String? = null

        reader.mirrorTo(
            writer = writer,
            intercept = {
                reader.isStartElement && reader.localName == "TeacherSubjects"
            }
        ) {
            when {
                reader.isStartElement -> when (reader.localName) {
                    "ListPerson1" -> inPersonList = true
                    "Forename" -> inForename = true
                    "Lastname" -> inLastname = true
                    "Person" -> {
                        require(inPersonList)
                        currentFirstName = ""
                        currentLastName = ""
                    }
                    "ListSchoolClasses1" -> {
                        val schoolClasses = SaveData.SchoolClass.fromSchedule(schedule.schedule)
                        schoolClasses.forEach { xmlMapper.writeValue(writer, it) }

                        writer.writeEndElement()

                        var classes = 0
                        reader.readUntilElementEnd(onChildElement = { _, localName, _ ->
                            if (localName == "SchoolClass") {
                                classes++
                            }
                        })

                        // subtract 1 since there's always a class of unassigned students (even if it's empty)
                        if (schedule.configuration.classes < classes - 1) {
                            error(
                                "Save file has ${classes - 1} classes; cannot write " +
                                    "${schedule.configuration.classes} since this would lose students."
                            )
                        }
                    }
                    "TeacherSubjects" -> {
                        require(inPersonList)
                        requireNotNull(currentFirstName)
                        requireNotNull(currentLastName)

                        val teacher = Teacher(firstName = currentFirstName!!, lastName = currentLastName!!)
                        val subjects = schedule.configuration.teacherAssignments[teacher].orEmpty()

                        xmlMapper.writeValue(
                            writer,
                            SaveData.TeacherSubjects(subjects = subjects.map { SaveData.enumToSubjectName(it) })
                        )

                        reader.readUntilElementEnd()
                    }
                }

                reader.isEndElement -> when (reader.localName) {
                    "ListPerson1" -> inPersonList = false
                    "Forename" -> inForename = false
                    "Lastname" -> inLastname = false
                    "Person" -> {
                        require(inPersonList)
                        currentFirstName = null
                        currentLastName = null
                    }
                }

                reader.isCharacters -> {
                    if (inForename) {
                        currentFirstName = currentFirstName!! + reader.text
                    }

                    if (inLastname) {
                        currentLastName = currentLastName!! + reader.text
                    }
                }
            }
        }
    }

    private fun openSqliteDb(file: File): Database {
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        return Database.connect("jdbc:sqlite:${file.absolutePath}", "org.sqlite.JDBC")
    }
}
