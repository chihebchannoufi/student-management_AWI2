package tn.esprit.studentmanagement.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.repositories.StudentRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceMockTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    @Test
    void getAllStudents_returnsList() {
        when(studentRepository.findAll()).thenReturn(List.of(new Student(), new Student()));

        List<Student> result = studentService.getAllStudents();

        assertEquals(2, result.size());
        verify(studentRepository).findAll();
    }

    @Test
    void getAllStudents_emptyList() {
        when(studentRepository.findAll()).thenReturn(List.of());

        assertTrue(studentService.getAllStudents().isEmpty());
        verify(studentRepository, times(1)).findAll();
    }

    @Test
    void getStudentById_found() {
        Student s = new Student();
        s.setIdStudent(1L);
        s.setFirstName("Youssef");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(s));

        Student result = studentService.getStudentById(1L);

        assertNotNull(result);
        assertEquals("Youssef", result.getFirstName());
    }

    @Test
    void getStudentById_notFound_returnsNull() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(studentService.getStudentById(99L));
    }

    @Test
    void saveStudent_savesAndReturns() {
        Student s = new Student();
        s.setFirstName("Youssef");
        s.setEmail("youssef@test.com");
        when(studentRepository.save(s)).thenReturn(s);

        Student result = studentService.saveStudent(s);

        assertEquals("youssef@test.com", result.getEmail());
        verify(studentRepository, times(1)).save(s);
    }

    @Test
    void deleteStudent_callsRepository() {
        studentService.deleteStudent(1L);

        verify(studentRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteStudent_neverCallsSave() {
        studentService.deleteStudent(5L);

        verify(studentRepository).deleteById(5L);
        verify(studentRepository, never()).save(any());
    }
}