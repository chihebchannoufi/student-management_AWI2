package tn.esprit.studentmanagement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.entities.Status;
import tn.esprit.studentmanagement.repositories.EnrollmentRepository;
import tn.esprit.studentmanagement.services.EnrollmentService;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceImplUnitTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    @Test
    void testAddEnrollment() {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentDate(LocalDate.of(2025, 10, 1));
        enrollment.setGrade(16.5);
        enrollment.setStatus(Status.ACTIVE);

        Enrollment savedEnrollment = new Enrollment();
        savedEnrollment.setIdEnrollment(1L);
        savedEnrollment.setEnrollmentDate(enrollment.getEnrollmentDate());
        savedEnrollment.setGrade(enrollment.getGrade());
        savedEnrollment.setStatus(enrollment.getStatus());

        when(enrollmentRepository.save(enrollment)).thenReturn(savedEnrollment);

        Enrollment result = enrollmentService.saveEnrollment(enrollment);

        assertNotNull(result);
        assertEquals(1L, result.getIdEnrollment());
        assertEquals(Status.ACTIVE, result.getStatus());
        verify(enrollmentRepository).save(enrollment);
    }

    @Test
    void testGetEnrollmentById() {
        Enrollment enrollment = new Enrollment();
        enrollment.setIdEnrollment(1L);
        enrollment.setStatus(Status.COMPLETED);

        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));

        Enrollment result = enrollmentService.getEnrollmentById(1L);

        assertEquals(enrollment, result);
        verify(enrollmentRepository).findById(1L);
    }

    @Test
    void testGetEnrollmentByIdWhenEnrollmentDoesNotExist() {
        when(enrollmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> enrollmentService.getEnrollmentById(99L));
        verify(enrollmentRepository).findById(99L);
    }

    @Test
    void testGetAllEnrollments() {
        Enrollment firstEnrollment = new Enrollment();
        Enrollment secondEnrollment = new Enrollment();
        List<Enrollment> expectedEnrollments = List.of(firstEnrollment, secondEnrollment);

        when(enrollmentRepository.findAll()).thenReturn(expectedEnrollments);

        List<Enrollment> result = enrollmentService.getAllEnrollments();

        assertEquals(expectedEnrollments, result);
        verify(enrollmentRepository).findAll();
    }

    @Test
    void testDeleteEnrollment() {
        doNothing().when(enrollmentRepository).deleteById(1L);

        enrollmentService.deleteEnrollment(1L);

        verify(enrollmentRepository).deleteById(1L);
    }
}
