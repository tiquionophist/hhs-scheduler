package com.tiquionophist.io

import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

/**
 * Tests XML serialization and deserialization of [SaveData].
 */
internal class SaveDataTest {
    data class SerializationTestCase(val data: SaveData, val serialized: String)

    @ParameterizedTest
    @MethodSource("serializationTestCases")
    fun testSerialize(testCase: SerializationTestCase) {
        assertEquals(testCase.serialized, SaveFileIO.xmlMapper.writeValueAsString(testCase.data))
    }

    @ParameterizedTest
    @MethodSource("serializationTestCases")
    fun testDeserialize(testCase: SerializationTestCase) {
        assertEquals(testCase.data, SaveFileIO.xmlMapper.readValue<SaveData>(testCase.serialized))
    }

    @Test
    fun testWriteSchoolClass() {
        assertEquals(schoolClassXml, SaveFileIO.xmlMapper.writeValueAsString(schoolClass))
    }

    @Test
    fun testWriteTeacherSubjects() {
        val xml = """
            <TeacherSubjects>
                <string>s1</string>
                <string>s2</string>
                <string>s3</string>
            </TeacherSubjects>
        """.replace("""\s""".toRegex(), "")

        val teacherSubjects = SaveData.TeacherSubjects(subjects = listOf("s1", "s2", "s3"))

        assertEquals(xml, SaveFileIO.xmlMapper.writeValueAsString(teacherSubjects))
    }

    companion object {
        private val schoolClass = SaveData.SchoolClass(
            monday = SaveData.StringList(listOf("m1", "m2", "m3", "m4")),
            tuesday = SaveData.StringList(listOf("t1", "t2", "t3", "t4")),
            wednesday = SaveData.StringList(listOf("w1", "w2", "w3", "w4")),
            thursday = SaveData.StringList(listOf("h1", "h2", "h3", "h4")),
            friday = SaveData.StringList(listOf("", "", "", "")),
            mondayLocation = SaveData.StringList(listOf("m1L", "m2L", "m3L", "m4L")),
            tuesdayLocation = SaveData.StringList(listOf("t1L", "t2L", "t3L", "t4L")),
            wednesdayLocation = SaveData.StringList(listOf("w1L", "w2L", "w3L", "w4L")),
            thursdayLocation = SaveData.StringList(listOf("h1L", "h2L", "h3L", "h4L")),
            fridayLocation = SaveData.StringList(listOf("", "", "", "")),
            classIndex = 1,
        )

        private val schoolClassXml = """
            <SchoolClass>
                <Monday>
                    <string>m1</string><string>m2</string><string>m3</string><string>m4</string>
                </Monday>
                <Tuesday>
                    <string>t1</string><string>t2</string><string>t3</string><string>t4</string>
                </Tuesday>
                <Wednesday>
                    <string>w1</string><string>w2</string><string>w3</string><string>w4</string>
                </Wednesday>
                <Thursday>
                    <string>h1</string><string>h2</string><string>h3</string><string>h4</string>
                </Thursday>
                <Friday>
                    <string></string><string></string><string></string><string></string>
                </Friday>
                
                <MondayLocation>
                    <string>m1L</string><string>m2L</string><string>m3L</string><string>m4L</string>
                </MondayLocation>
                <TuesdayLocation>
                    <string>t1L</string><string>t2L</string><string>t3L</string><string>t4L</string>
                </TuesdayLocation>
                <WednesdayLocation>
                    <string>w1L</string><string>w2L</string><string>w3L</string><string>w4L</string>
                </WednesdayLocation>
                <ThursdayLocation>
                    <string>h1L</string><string>h2L</string><string>h3L</string><string>h4L</string>
                </ThursdayLocation>
                <FridayLocation>
                    <string></string><string></string><string></string><string></string>
                </FridayLocation>
                <ClassIndex>1</ClassIndex>
            </SchoolClass>
        """.replace("""\s""".toRegex(), "")

        @JvmStatic
        fun serializationTestCases(): List<SerializationTestCase> {
            return listOf(
                SerializationTestCase(
                    data = SaveData(
                        schoolClasses = SaveData.SchoolClasses(listOf(schoolClass)),
                        people = SaveData.People(
                            listOf(
                                SaveData.Person(
                                    firstName = "First1",
                                    lastName = "Last1",
                                    teacherSubjects = SaveData.TeacherSubjects(subjects = listOf()),
                                    subjectFamilyExp = SaveData.SubjectExp(items = listOf()),
                                    subjectInstanceExp = SaveData.SubjectExp(items = listOf()),
                                ),
                                SaveData.Person(
                                    firstName = "First2",
                                    lastName = "Last2",
                                    teacherSubjects = SaveData.TeacherSubjects(subjects = listOf("s1", "s2", "s3")),
                                    subjectFamilyExp = SaveData.SubjectExp(items = listOf()),
                                    subjectInstanceExp = SaveData.SubjectExp(
                                        items = listOf(
                                            SaveData.FloatValue("s1", 3.14f),
                                            SaveData.FloatValue("s2", 100f),
                                        )
                                    ),
                                ),
                                SaveData.Person(
                                    firstName = "First3",
                                    lastName = "Last3",
                                    teacherSubjects = SaveData.TeacherSubjects(subjects = listOf("x1")),
                                    subjectFamilyExp = SaveData.SubjectExp(items = listOf()),
                                    subjectInstanceExp = SaveData.SubjectExp(items = listOf()),
                                ),
                            )
                        )
                    ),
                    serialized = """
                        <SaveData>
                            <ListSchoolClasses1>$schoolClassXml</ListSchoolClasses1>

                            <ListPerson1>
                                <Person>
                                    <Forename>First1</Forename>
                                    <Lastname>Last1</Lastname>
                                    <TeacherSubjects/>
                                    <SubjectFamilyExp/>
                                    <SubjectInstanceExp/>
                                </Person>

                                <Person>
                                    <Forename>First2</Forename>
                                    <Lastname>Last2</Lastname>
                                    <TeacherSubjects>
                                        <string>s1</string>
                                        <string>s2</string>
                                        <string>s3</string>
                                    </TeacherSubjects>
                                    <SubjectFamilyExp/>
                                    <SubjectInstanceExp>
                                        <Item>
                                            <Key>s1</Key>
                                            <Value>3.14</Value>
                                        </Item>
                                        <Item>
                                            <Key>s2</Key>
                                            <Value>100.0</Value>
                                        </Item>
                                    </SubjectInstanceExp>
                                </Person>

                                <Person>
                                    <Forename>First3</Forename>
                                    <Lastname>Last3</Lastname>
                                    <TeacherSubjects>
                                        <string>x1</string>
                                    </TeacherSubjects>
                                    <SubjectFamilyExp/>
                                    <SubjectInstanceExp/>
                                </Person>
                            </ListPerson1>
                        </SaveData>
                        """
                        .replace("""\s""".toRegex(), "")
                )
            )
        }
    }
}
