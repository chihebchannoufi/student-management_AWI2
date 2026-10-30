package tn.esprit.studentmanagement.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.studentmanagement.dto.EnrollmentDTO;
import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.repositories.CourseRepository;
import tn.esprit.studentmanagement.repositories.StudentRepository;
import tn.esprit.studentmanagement.services.IEnrollment;

import java.util.List;

@RestController
@RequestMapping("/Enrollment")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class EnrollmentController {
    private final IEnrollment enrollmentService;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    @GetMapping("/getAllEnrollment")
    public List<Enrollment> getAllEnrollment() { return enrollmentService.getAllEnrollments(); }

    @GetMapping("/getEnrollment/{id}")
    public Enrollment getEnrollment(@PathVariable Long id) { return enrollmentService.getEnrollmentById(id); }

    @PostMapping("/createEnrollment")
    public Enrollment createEnrollment(@RequestBody EnrollmentDTO enrollmentDTO) {
        return enrollmentService.saveEnrollment(toEntity(enrollmentDTO));
    }

    @PutMapping("/updateEnrollment/{id}")
    public Enrollment updateEnrollment(@PathVariable Long id, @RequestBody EnrollmentDTO enrollmentDTO) {
        Enrollment enrollment = enrollmentService.getEnrollmentById(id);
        enrollment.setEnrollmentDate(enrollmentDTO.getEnrollmentDate());
        enrollment.setGrade(enrollmentDTO.getGrade());
        enrollment.setStatus(enrollmentDTO.getStatus());
        setRelations(enrollmentDTO, enrollment);
        return enrollmentService.saveEnrollment(enrollment);
    }

    @DeleteMapping("/deleteEnrollment/{id}")
    public void deleteEnrollment(@PathVariable Long id) {
        enrollmentService.deleteEnrollment(id); }

    private Enrollment toEntity(EnrollmentDTO enrollmentDTO) {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentDate(enrollmentDTO.getEnrollmentDate());
        enrollment.setGrade(enrollmentDTO.getGrade());
        enrollment.setStatus(enrollmentDTO.getStatus());
        setRelations(enrollmentDTO, enrollment);
        return enrollment;
    }

    private void setRelations(EnrollmentDTO enrollmentDTO, Enrollment enrollment) {
        enrollment.setStudent(studentRepository.findById(enrollmentDTO.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Student not found: " + enrollmentDTO.getStudentId())));
        enrollment.setCourse(courseRepository.findById(enrollmentDTO.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Course not found: " + enrollmentDTO.getCourseId())));
    }
}
