package tn.esprit.studentmanagement.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.exceptions.ResourceNotFoundException;
import tn.esprit.studentmanagement.repositories.StudentRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {
    @Mock
    private StudentRepository studentRepository;

    private StudentService studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentService(studentRepository);
    }

    @Test
    void getAllStudentsReturnsRepositoryValues() {
        Student student = new Student();
        student.setFirstName("Alice");
        when(studentRepository.findAll()).thenReturn(List.of(student));

        assertEquals(List.of(student), studentService.getAllStudents());
        verify(studentRepository).findAll();
    }

    @Test
    void getStudentByIdReturnsFoundStudent() {
        Student student = new Student();
        student.setIdStudent(6L);
        when(studentRepository.findById(6L)).thenReturn(Optional.of(student));

        assertEquals(student, studentService.getStudentById(6L));
    }

    @Test
    void getStudentByIdThrowsWhenStudentIsMissing() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> studentService.getStudentById(99L));
    }

    @Test
    void saveStudentPersistsStudent() {
        Student student = new Student();
        student.setEmail("alice@example.com");
        when(studentRepository.save(student)).thenReturn(student);

        assertEquals(student, studentService.saveStudent(student));
        verify(studentRepository).save(student);
    }

    @Test
    void deleteStudentDeletesRequestedStudent() {
        studentService.deleteStudent(7L);

        verify(studentRepository).deleteById(7L);
    }
}
