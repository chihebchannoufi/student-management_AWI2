package tn.esprit.studentmanagement.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import tn.esprit.studentmanagement.entities.Enrollment;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EnrollmentServiceImplTest {

    @Autowired
    IEnrollment enrollmentService;

    // Variable globale bech nkhabiw fiha l'ID
    static Long idToTest;

    // 1. Test Ajout (Create)
    @Test
    @Order(1)
    public void testAddEnrollment() {
        Enrollment enrollment = new Enrollment();
        Enrollment saved = enrollmentService.saveEnrollment(enrollment);

        assertNotNull(saved);
        idToTest = saved.getIdEnrollment();
    }

    // 2. Test Affichage (Read)
    @Test
    @Order(2)
    public void testGetEnrollmentById() {
        Enrollment found = enrollmentService.getEnrollmentById(idToTest);
        assertNotNull(found);
    }

    // 3. Test Modification (Update)
    @Test
    @Order(3)
    public void testUpdateEnrollment() {
        Enrollment found = enrollmentService.getEnrollmentById(idToTest);
        Enrollment updated = enrollmentService.saveEnrollment(found);
        assertNotNull(updated);
    }

    // 4. Test Suppression (Delete)
    @Test
    @Order(4)
    public void testDeleteEnrollment() {
        enrollmentService.deleteEnrollment(idToTest);

        // Nthabtou elli ki nlawjou 3lih marra okhra yal9ah null
        Enrollment deleted = enrollmentService.getEnrollmentById(idToTest);
        assertNull(deleted);
    }
}