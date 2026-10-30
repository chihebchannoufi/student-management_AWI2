package tn.esprit.studentmanagement.services;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.repositories.EnrollmentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor // 1. Ça remplace le @Autowired et corrige l'Issue 2
public class EnrollmentService implements IEnrollment {

    // On ajoute "final" pour l'injection par constructeur (recommandé par Sonar)
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public Enrollment saveEnrollment(Enrollment enrollment) {
        return enrollmentRepository.save(enrollment);
    }

    @Override
    public Enrollment getEnrollmentById(Long idEnrollment) {
        // 2. On utilise orElse(null) au lieu de .get() pour corriger l'Issue 3
        return enrollmentRepository.findById(idEnrollment).orElse(null);
    }

    @Override
    public List<Enrollment> getAllEnrollments() {
        // Na7ina (List<Enrollment>) elli kenet maktouba 9bal enrollmentRepository
        return enrollmentRepository.findAll();
    }

    @Override
    public void deleteEnrollment(Long idEnrollment) {
        enrollmentRepository.deleteById(idEnrollment);
    }
}