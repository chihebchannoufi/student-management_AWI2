package tn.esprit.studentmanagement;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.entities.Status;
import tn.esprit.studentmanagement.services.IEnrollment;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Slf4j
class EnrollmentServiceImplIntegrationTest {

    @Autowired
    private IEnrollment enrollmentService;

    private final List<Long> enrollmentIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        enrollmentIds.forEach(enrollmentService::deleteEnrollment);
        enrollmentIds.clear();
    }

    @Test
    void testAddEnrollment() {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentDate(LocalDate.of(2025, 10, 1));
        enrollment.setGrade(16.5);
        enrollment.setStatus(Status.ACTIVE);

        Enrollment savedEnrollment = enrollmentService.saveEnrollment(enrollment);
        assertNotNull(savedEnrollment);
        enrollmentIds.add(savedEnrollment.getIdEnrollment());

        log.info("Enrollment added: {}", savedEnrollment);

        assertNotNull(savedEnrollment.getIdEnrollment());
        assertEquals(enrollment.getEnrollmentDate(), savedEnrollment.getEnrollmentDate());
        assertEquals(enrollment.getGrade(), savedEnrollment.getGrade());
        assertEquals(Status.ACTIVE, savedEnrollment.getStatus());
    }

    @Test
    void testGetEnrollmentById() {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentDate(LocalDate.of(2025, 10, 2));
        enrollment.setGrade(14.0);
        enrollment.setStatus(Status.COMPLETED);

        Enrollment savedEnrollment = enrollmentService.saveEnrollment(enrollment);
        enrollmentIds.add(savedEnrollment.getIdEnrollment());

        Enrollment foundEnrollment =
                enrollmentService.getEnrollmentById(savedEnrollment.getIdEnrollment());

        log.info("Enrollment found: {}", foundEnrollment);

        assertNotNull(foundEnrollment);
        assertEquals(savedEnrollment.getIdEnrollment(), foundEnrollment.getIdEnrollment());
        assertEquals(savedEnrollment.getEnrollmentDate(), foundEnrollment.getEnrollmentDate());
        assertEquals(savedEnrollment.getGrade(), foundEnrollment.getGrade());
        assertEquals(Status.COMPLETED, foundEnrollment.getStatus());
    }

    @Test
    void testGetAllEnrollments() {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentDate(LocalDate.of(2025, 10, 3));
        enrollment.setGrade(12.5);
        enrollment.setStatus(Status.ACTIVE);

        Enrollment savedEnrollment = enrollmentService.saveEnrollment(enrollment);
        enrollmentIds.add(savedEnrollment.getIdEnrollment());

        List<Enrollment> enrollments = enrollmentService.getAllEnrollments();

        log.info("Number of enrollments found: {}", enrollments.size());

        assertNotNull(enrollments);
        assertFalse(enrollments.isEmpty());
        assertTrue(enrollments.stream()
                .anyMatch(item -> savedEnrollment.getIdEnrollment().equals(item.getIdEnrollment())));
    }

    @Test
    void testDeleteEnrollment() {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentDate(LocalDate.of(2025, 10, 4));
        enrollment.setGrade(10.0);
        enrollment.setStatus(Status.WITHDRAWN);

        Enrollment savedEnrollment = enrollmentService.saveEnrollment(enrollment);
        Long enrollmentId = savedEnrollment.getIdEnrollment();
        enrollmentIds.add(enrollmentId);

        enrollmentService.deleteEnrollment(enrollmentId);
        enrollmentIds.remove(enrollmentId);

        boolean enrollmentExists = enrollmentService.getAllEnrollments().stream()
                .anyMatch(item -> enrollmentId.equals(item.getIdEnrollment()));

        log.info("Enrollment {} deleted: {}", enrollmentId, !enrollmentExists);

        assertFalse(enrollmentExists);
    }
}
