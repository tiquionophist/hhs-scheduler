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
            private const val DEFAULT_PERIODS_PER_DAY = 4

            fun fromSchedule(schedule: Schedule): List<SchoolClass> {
                val emptyClass = SchoolClass(
                    classIndex = 0,
                    monday = StringList(List(DEFAULT_PERIODS_PER_DAY) { "" }),
                    tuesday = StringList(List(DEFAULT_PERIODS_PER_DAY) { "" }),
                    wednesday = StringList(List(DEFAULT_PERIODS_PER_DAY) { "" }),
                    thursday = StringList(List(DEFAULT_PERIODS_PER_DAY) { "" }),
                    friday = StringList(List(DEFAULT_PERIODS_PER_DAY) { "" }),
                    mondayLocation = StringList(List(DEFAULT_PERIODS_PER_DAY) { "" }),
                    tuesdayLocation = StringList(List(DEFAULT_PERIODS_PER_DAY) { "" }),
                    wednesdayLocation = StringList(List(DEFAULT_PERIODS_PER_DAY) { "" }),
                    thursdayLocation = StringList(List(DEFAULT_PERIODS_PER_DAY) { "" }),
                    fridayLocation = StringList(List(DEFAULT_PERIODS_PER_DAY) { "" }),
                )

                val classroomNames = schedule.classroomNames()
                val classes = schedule.lessons.mapIndexed { index, classLessons ->
                    val lessonsChunked = classLessons.chunked(DEFAULT_PERIODS_PER_DAY)
                    val mondayLessons = lessonsChunked[0]
                    val tuesdayLessons = lessonsChunked[1]
                    val wednesdayLessons = lessonsChunked[2]
                    val thursdayLessons = lessonsChunked[3]
                    val fridayLessons = lessonsChunked[4]

                    val classroomsChunked = classroomNames[index].chunked(DEFAULT_PERIODS_PER_DAY)
                    val mondayClassrooms = classroomsChunked[0]
                    val tuesdayClassrooms = classroomsChunked[1]
                    val wednesdayClassrooms = classroomsChunked[2]
                    val thursdayClassrooms = classroomsChunked[3]
                    val fridayClassrooms = classroomsChunked[4]

                    SchoolClass(
                        classIndex = index + 1,
                        monday = StringList(mondayLessons.map { enumToSubjectName(it.subject) }),
                        tuesday = StringList(tuesdayLessons.map { enumToSubjectName(it.subject) }),
                        wednesday = StringList(wednesdayLessons.map { enumToSubjectName(it.subject) }),
                        thursday = StringList(thursdayLessons.map { enumToSubjectName(it.subject) }),
                        friday = StringList(fridayLessons.map { enumToSubjectName(it.subject) }),
                        mondayLocation = StringList(mondayClassrooms),
                        tuesdayLocation = StringList(tuesdayClassrooms),
                        wednesdayLocation = StringList(wednesdayClassrooms),
                        thursdayLocation = StringList(thursdayClassrooms),
                        fridayLocation = StringList(fridayClassrooms),
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
         * Maps the lessons in this [Schedule] to the canonical names of the locations where they take place, i.e. the
         * location names used in the save data.
         *
         * For explicit [Classroom]s this is similar to their [Classroom.prettyName] (but not always identical, since
         * some have a prefix "Classroom XXX"), but in cases where the classroom is null this function also fills it in
         * which a generic numbered classroom, i.e. "Classroom 1", "Classroom 2", etc.
         */
        private fun Schedule.classroomNames(): List<List<String>> {
            // [periodIndex -> highest classroom number that's in use for that period]
            val numberedClassrooms = mutableMapOf<Int, Int>()

            return lessons.map { classLessons ->
                classLessons.mapIndexed { periodIndex, lesson ->
                    when (lesson.classroom) {
                        Classroom.ART -> "Classroom Art"
                        Classroom.BASEMENT -> "Basement"
                        Classroom.BIOLOGY -> "Classroom Biology"
                        Classroom.CHEMISTRY -> "Classroom Chemistry"
                        Classroom.COMPUTER -> "Computer Room"
                        Classroom.GYM -> "Gym"
                        Classroom.MUSIC -> "Classroom Music"
                        Classroom.SPORTS_AREA -> "Sports Area"
                        Classroom.SWIMMING_POOL -> "Swimming Pool"
                        null -> {
                            val classroomNumber = (numberedClassrooms[periodIndex] ?: 0) + 1
                            numberedClassrooms[periodIndex] = classroomNumber

                            check(classroomNumber <= 10) { "exceeded numbered classrooms" }
                            "Classroom $classroomNumber"
                        }
                    }
                }
            }
        }

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
    }
}