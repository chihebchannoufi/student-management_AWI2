package tn.esprit.studentmanagement.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.studentmanagement.entities.Student;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // each test is rolled back, so tests stay independent
class StudentServiceIntegrationTest {

    @Autowired
    private StudentService studentService;

    private Student buildStudent(String first, String email) {
        Student s = new Student();
        s.setFirstName(first);
        s.setLastName("Test");
        s.setEmail(email);
        s.setDateOfBirth(LocalDate.of(2000, 1, 1));
        return s;
    }

    @Test
    void saveAndFindStudent() {
        Student saved = studentService.saveStudent(buildStudent("Youssef", "y@test.com"));

        assertNotNull(saved.getIdStudent());
        Student found = studentService.getStudentById(saved.getIdStudent());
        assertEquals("Youssef", found.getFirstName());
    }

    @Test
    void getAllStudents_containsSavedOnes() {
        int before = studentService.getAllStudents().size();
        studentService.saveStudent(buildStudent("A", "a@test.com"));
        studentService.saveStudent(buildStudent("B", "b@test.com"));

        assertEquals(before + 2, studentService.getAllStudents().size());
    }

    @Test
    void deleteStudent_removesIt() {
        Student saved = studentService.saveStudent(buildStudent("ToDelete", "d@test.com"));

        studentService.deleteStudent(saved.getIdStudent());

        assertNull(studentService.getStudentById(saved.getIdStudent()));
    }
}