package tn.esprit.studentmanagement.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.repositories.EnrollmentRepository;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class EnrollmentServiceMockTest {

    @Mock
    EnrollmentRepository enrollmentRepository;

    @InjectMocks
    EnrollmentService enrollmentService;

    // 1. Test Ajout (Create)
    @Test
    public void testAddEnrollment() {
        Enrollment enrollment = new Enrollment();

        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);

        Enrollment saved = enrollmentService.saveEnrollment(enrollment);

        assertNotNull(saved);
        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
    }

    // 2. Test Affichage (Read)
    @Test
    public void testGetEnrollmentById() {
        Enrollment enrollment = new Enrollment();
        Long idToFind = 1L;

        // N'simuliw elli base de données bech traja3lna l'objet ki nlawjou 3lih
        when(enrollmentRepository.findById(idToFind)).thenReturn(Optional.of(enrollment));

        Enrollment found = enrollmentService.getEnrollmentById(idToFind);

        assertNotNull(found);
        verify(enrollmentRepository, times(1)).findById(idToFind);
    }

    // 3. Test Modification (Update)
    @Test
    public void testUpdateEnrollment() {
        Enrollment enrollment = new Enrollment();
        // Fel Spring Boot, l'modification w l'ajout yesta3mlou nafs l'methode save()

        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);

        Enrollment updated = enrollmentService.saveEnrollment(enrollment);

        assertNotNull(updated);
        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
    }

    // 4. Test Suppression (Delete)
    @Test
    public void testDeleteEnrollment() {
        Long idToDelete = 1L;

        // N'simuliw l'action mta3 l'suppression (men ghir ma traja3 chay)
        doNothing().when(enrollmentRepository).deleteById(idToDelete);

        // N3aytou lel methode mta3 l'service
        enrollmentService.deleteEnrollment(idToDelete);

        // Nthabtou elli methode l'suppression mta3 l'repository t3aytelha marra barka
        verify(enrollmentRepository, times(1)).deleteById(idToDelete);
    }
}