package tn.esprit.studentmanagement.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.entities.Status;
import tn.esprit.studentmanagement.exceptions.ResourceNotFoundException;
import tn.esprit.studentmanagement.repositories.EnrollmentRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {
    @Mock
    private EnrollmentRepository enrollmentRepository;

    private EnrollmentService enrollmentService;

    @BeforeEach
    void setUp() {
        enrollmentService = new EnrollmentService(enrollmentRepository);
    }

    @Test
    void getAllEnrollmentsReturnsRepositoryValues() {
        Enrollment enrollment = enrollment();
        when(enrollmentRepository.findAll()).thenReturn(List.of(enrollment));

        assertEquals(List.of(enrollment), enrollmentService.getAllEnrollments());
        verify(enrollmentRepository).findAll();
    }

    @Test
    void getEnrollmentByIdReturnsFoundEnrollment() {
        Enrollment enrollment = enrollment();
        enrollment.setIdEnrollment(4L);
        when(enrollmentRepository.findById(4L)).thenReturn(Optional.of(enrollment));

        assertEquals(enrollment, enrollmentService.getEnrollmentById(4L));
    }

    @Test
    void getEnrollmentByIdThrowsWhenEnrollmentIsMissing() {
        when(enrollmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> enrollmentService.getEnrollmentById(99L));
    }

    @Test
    void saveEnrollmentPersistsEnrollment() {
        Enrollment enrollment = enrollment();
        when(enrollmentRepository.save(enrollment)).thenReturn(enrollment);

        assertEquals(enrollment, enrollmentService.saveEnrollment(enrollment));
        verify(enrollmentRepository).save(enrollment);
    }

    @Test
    void deleteEnrollmentDeletesRequestedEnrollment() {
        enrollmentService.deleteEnrollment(5L);

        verify(enrollmentRepository).deleteById(5L);
    }

    private Enrollment enrollment() {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentDate(LocalDate.of(2026, 9, 15));
        enrollment.setGrade(15.5);
        enrollment.setStatus(Status.ACTIVE);
        return enrollment;
    }
}
