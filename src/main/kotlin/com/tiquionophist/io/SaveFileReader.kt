package com.tiquionophist.io

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.core.Subject
import com.tiquionophist.core.Teacher
import com.tiquionophist.util.prettyName
import java.io.ByteArrayInputStream
import java.io.File
import java.util.Base64
import java.util.zip.GZIPInputStream

/**
 * Wraps the top-level save file XML contents. This contains some metadata on the save file (ignored here) and the main
 * [data] blob, which is stored as gzipped and base64-encoded XML.
 */
private data class SaveFile(
    @JsonProperty("Data")
    val data: String,
)

/**
 * Wraps the main contents of a save file, from [SaveFile.data].
 */
data class SaveData(
    @JsonProperty("ListSchoolClasses1")
    val schoolClasses: List<SchoolClass>,

    @JsonProperty("ListPerson1")
    val people: List<Person>,
) {
    /**
     * A single class in the school, with a schedule determined by [monday], [tuesday], etc. Each day of the week has a
     * (length 4) list of the subject names being taught at each period in the day, and the associated locations in
     * [mondayLocation], [tuesdayLocation], etc.
     *
     * [classIndex] is 0 for the "class" of overflow students, which is always empty.
     */
    data class SchoolClass(
        @JsonProperty("ClassIndex")
        val classIndex: Int,

        @JsonProperty("Monday")
        val monday: List<String>,

        @JsonProperty("Tuesday")
        val tuesday: List<String>,

        @JsonProperty("Wednesday")
        val wednesday: List<String>,

        @JsonProperty("Thursday")
        val thursday: List<String>,

        @JsonProperty("Friday")
        val friday: List<String>,

        @JsonProperty("MondayLocation")
        val mondayLocation: List<String>,

        @JsonProperty("TuesdayLocation")
        val tuesdayLocation: List<String>,

        @JsonProperty("WednesdayLocation")
        val wednesdayLocation: List<String>,

        @JsonProperty("ThursdayLocation")
        val thursdayLocation: List<String>,

        @JsonProperty("FridayLocation")
        val fridayLocation: List<String>,
    ) {
        /**
         * Gets the frequency of [Subject]s taught in this [SchoolClass], i.e. how many times they occur in [monday],
         * [tuesday], etc.
         */
        fun toSubjectFrequency(): Map<Subject, Int> {
            val subjects = listOf(monday, tuesday, wednesday, thursday, friday)
                .flatten()
                .map { subjectNameToEnum(it) }

            return subjects.fold(mapOf()) { map, subject ->
                map.plus(subject to (map[subject] ?: 0) + 1)
            }
        }
    }

    /**
     * A single person in the game; if they are a teacher then [teacherSubjects] will be set.
     */
    data class Person(
        @JsonProperty("Forename")
        val firstName: String,

        @JsonProperty("Lastname")
        val lastName: String,

        @JsonProperty("TeacherSubjects")
        val teacherSubjects: List<String>?,
    )

    /**
     * Converts this [SaveData] to a [ScheduleConfiguration] with teacher assignments based on [people] and subject
     * frequency based on the class with index [frequencyFromClassIndex].
     */
    fun toScheduleConfiguration(frequencyFromClassIndex: Int = 1): ScheduleConfiguration {
        val teachers = people.filter { !it.teacherSubjects.isNullOrEmpty() }
        return ScheduleConfiguration(
            classes = schoolClasses.size - 1,
            teacherAssignments = teachers.associate { person ->
                val teacher = Teacher(firstName = person.firstName, lastName = person.lastName)
                val subjects = person.teacherSubjects!!
                    .map { subjectNameToEnum(it) }
                    .toSet()

                teacher to subjects
            },
            subjectFrequency = schoolClasses.find { it.classIndex == frequencyFromClassIndex }!!.toSubjectFrequency()
        )
    }

    companion object {
        /**
         * Convert [subjectName] from game save XML to a [Subject], which requires a bit of transformation from its
         * [Subject.prettyName].
         */
        private fun subjectNameToEnum(subjectName: String): Subject {
            if (subjectName.isEmpty()) return Subject.EMPTY

            val transformedName = subjectName.replace("Sex Education", "Sex Ed")
            return Subject.values().firstOrNull { it.prettyName == transformedName }
                ?: error("no Subject matching $subjectName")
        }
    }
}

object SaveFileReader {
    private val xmlMapper = XmlMapper.builder()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .addModule(kotlinModule()) // allow non-empty constructors and other Kotlin compatibility
        .build()

    /**
     * Reads the game save at [file] into a [SaveData] which wraps the XML data in the save file.
     */
    fun read(file: File): SaveData {
        val saveFile = xmlMapper.readValue(file, SaveFile::class.java)

        val decoded = Base64.getDecoder().decode(saveFile.data)
        val unzippedStream = GZIPInputStream(ByteArrayInputStream(decoded))
        return xmlMapper.readValue(unzippedStream, SaveData::class.java)
    }
}
