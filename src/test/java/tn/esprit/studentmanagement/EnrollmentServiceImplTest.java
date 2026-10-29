package tn.esprit.studentmanagement;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
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
@TestMethodOrder(OrderAnnotation.class)
class EnrollmentServiceImplTest {

    @Autowired
    IEnrollment enrollmentService;

    private final List<Long> enrollmentIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        enrollmentIds.forEach(id -> {
            if (enrollmentService.getAllEnrollments().stream()
                    .anyMatch(enrollment -> id.equals(enrollment.getIdEnrollment()))) {
                enrollmentService.deleteEnrollment(id);
            }
        });
        enrollmentIds.clear();
    }

    @Test
    @Order(1)
    void testAddEnrollment() {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentDate(LocalDate.of(2025, 9, 15));
        enrollment.setGrade(16.5);
        enrollment.setStatus(Status.ACTIVE);

        Enrollment savedEnrollment = enrollmentService.saveEnrollment(enrollment);
        enrollmentIds.add(savedEnrollment.getIdEnrollment());

        log.info("Enrollment added: {}", savedEnrollment);

        assertNotNull(savedEnrollment.getIdEnrollment());
        assertNotNull(savedEnrollment.getEnrollmentDate());
        assertNotNull(savedEnrollment.getStatus());
        assertTrue(savedEnrollment.getGrade() >= 0);
    }

    @Test
    @Order(2)
    void testGetEnrollmentById() {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentDate(LocalDate.of(2025, 9, 16));
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
    @Order(3)
    void testGetAllEnrollments() {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentDate(LocalDate.of(2025, 9, 17));
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
    @Order(4)
    void testDeleteEnrollment() {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentDate(LocalDate.of(2025, 9, 18));
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
