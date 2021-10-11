package com.tiquionophist.io

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.tiquionophist.core.Classroom
import com.tiquionophist.core.Schedule
import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.core.Subject
import com.tiquionophist.core.Teacher
import com.tiquionophist.util.prettyName

/**
 * Wraps the main contents of a save file, from [SaveFile.data].
 */
data class SaveData(
    @JacksonXmlProperty(localName = "ListSchoolClasses1")
    val schoolClasses: SchoolClasses,

    @JacksonXmlProperty(localName = "ListPerson1")
    @JacksonXmlElementWrapper
    val people: People,
) {
    /**
     * Wrapper class for the list of [SchoolClass]es to avoid Jackson weirdness between the wrapper element name and the
     * name of each class in the list.
     */
    data class SchoolClasses(
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "SchoolClass")
        val classes: List<SchoolClass> = listOf()
    )

    /**
     * Wrapper class for the list of [Person]s to avoid Jackson weirdness between the wrapper element name and the name
     * of each class in the list.
     */
    data class People(
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "Person")
        val people: List<Person> = listOf()
    )

    /**
     * A single class in the school, with a schedule determined by [monday], [tuesday], etc. Each day of the week has a
     * (length 4) list of the subject names being taught at each period in the day, and the associated locations in
     * [mondayLocation], [tuesdayLocation], etc.
     *
     * [classIndex] is 0 for the "class" of overflow students, which is always empty.
     */
    data class SchoolClass(
        @JacksonXmlProperty(localName = "Monday")
        val monday: StringList,

        @JacksonXmlProperty(localName = "Tuesday")
        val tuesday: StringList,

        @JacksonXmlProperty(localName = "Wednesday")
        val wednesday: StringList,

        @JacksonXmlProperty(localName = "Thursday")
        val thursday: StringList,

        @JacksonXmlProperty(localName = "Friday")
        val friday: StringList,

        @JacksonXmlProperty(localName = "MondayLocation")
        val mondayLocation: StringList,

        @JacksonXmlProperty(localName = "TuesdayLocation")
        val tuesdayLocation: StringList,

        @JacksonXmlProperty(localName = "WednesdayLocation")
        val wednesdayLocation: StringList,

        @JacksonXmlProperty(localName = "ThursdayLocation")
        val thursdayLocation: StringList,

        @JacksonXmlProperty(localName = "FridayLocation")
        val fridayLocation: StringList,

        @JsonProperty("ClassIndex")
        val classIndex: Int,
    ) {
        /**
         * Gets the frequency of [Subject]s taught in this [SchoolClass], i.e. how many times they occur in [monday],
         * [tuesday], etc.
         */
        fun toSubjectFrequency(): Map<Subject, Int> {
            val subjects = listOf(monday.strings, tuesday.strings, wednesday.strings, thursday.strings, friday.strings)
                .flatten()
                .map { subjectNameToEnum(it) }

            return subjects.fold(mapOf()) { map, subject ->
                map.plus(subject to (map[subject] ?: 0) + 1)
            }
        }

        companion object {
            fun fromSchedule(schedule: Schedule): List<SchoolClass> {
                val emptyClass = SchoolClass(
                    classIndex = 0,
                    monday = StringList(List(4) { "" }),
                    tuesday = StringList(List(4) { "" }),
                    wednesday = StringList(List(4) { "" }),
                    thursday = StringList(List(4) { "" }),
                    friday = StringList(List(4) { "" }),
                    mondayLocation = StringList(List(4) { "" }),
                    tuesdayLocation = StringList(List(4) { "" }),
                    wednesdayLocation = StringList(List(4) { "" }),
                    thursdayLocation = StringList(List(4) { "" }),
                    fridayLocation = StringList(List(4) { "" }),
                )

                val classes = schedule.lessons.mapIndexed { index, classLessons ->
                    val chunked = classLessons.chunked(4)
                    val mondayLessons = chunked[0]
                    val tuesdayLessons = chunked[1]
                    val wednesdayLessons = chunked[2]
                    val thursdayLessons = chunked[3]
                    val fridayLessons = chunked[4]

                    // TODO add numbered classrooms ("Classroom 1", etc)
                    SchoolClass(
                        classIndex = index + 1,
                        monday = StringList(mondayLessons.map { enumToSubjectName(it.subject) }),
                        tuesday = StringList(tuesdayLessons.map { enumToSubjectName(it.subject) }),
                        wednesday = StringList(wednesdayLessons.map { enumToSubjectName(it.subject) }),
                        thursday = StringList(thursdayLessons.map { enumToSubjectName(it.subject) }),
                        friday = StringList(fridayLessons.map { enumToSubjectName(it.subject) }),
                        mondayLocation = StringList(mondayLessons.map { enumToLocationName(it.classroom) }),
                        tuesdayLocation = StringList(tuesdayLessons.map { enumToLocationName(it.classroom) }),
                        wednesdayLocation = StringList(wednesdayLessons.map { enumToLocationName(it.classroom) }),
                        thursdayLocation = StringList(thursdayLessons.map { enumToLocationName(it.classroom) }),
                        fridayLocation = StringList(fridayLessons.map { enumToLocationName(it.classroom) }),
                    )
                }

                return classes.plus(emptyClass)
            }
        }
    }

    /**
     * Wrapper class for a list of strings to avoid Jackson weirdness between the wrapper element name and the name of
     * each string in the list.
     */
    data class StringList(
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "string")
        val strings: List<String> = listOf()
    )

    /**
     * A single person in the game; if they are a teacher then [teacherSubjects] will be set.
     */
    data class Person(
        @JacksonXmlProperty(localName = "Forename")
        val firstName: String,

        @JacksonXmlProperty(localName = "Lastname")
        val lastName: String,

        @JacksonXmlProperty(localName = "TeacherSubjects")
        val teacherSubjects: TeacherSubjects?,
    )

    /**
     * Wrapper class for the list of subjects a teacher teaches; this cannot use [StringList] to allow writing it
     * individually.
     */
    data class TeacherSubjects(
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "string")
        val subjects: List<String> = listOf()
    )

    /**
     * Converts this [SaveData] to a [ScheduleConfiguration] with teacher assignments based on [people] and subject
     * frequency based on the class with index [frequencyFromClassIndex].
     */
    fun toScheduleConfiguration(frequencyFromClassIndex: Int = 1): ScheduleConfiguration {
        val teachers = people.people.filter { !it.teacherSubjects?.subjects.isNullOrEmpty() }
        return ScheduleConfiguration(
            classes = schoolClasses.classes.size - 1,
            teacherAssignments = teachers.associate { person ->
                val teacher = Teacher(firstName = person.firstName, lastName = person.lastName)
                val subjects = person.teacherSubjects!!.subjects
                    .map { subjectNameToEnum(it) }
                    .toSet()

                teacher to subjects
            },
            subjectFrequency = schoolClasses.classes
                .find { it.classIndex == frequencyFromClassIndex }!!.toSubjectFrequency()
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

        /**
         * Convert [subject] to its name in the game save XML, which requires a bit of transformation from its
         * [Subject.prettyName].
         */
        fun enumToSubjectName(subject: Subject): String {
            if (subject == Subject.EMPTY) return ""

            return subject.prettyName.replace("Sex Ed", "Sex Education")
        }

        /**
         * Convert [classroom] to its name in the game save XML, which requires a bit of transformation from its
         * [Classroom.prettyName].
         */
        private fun enumToLocationName(classroom: Classroom?): String {
            return classroom?.prettyName.orEmpty() // TODO classroom names are a bit different
        }
    }
}
