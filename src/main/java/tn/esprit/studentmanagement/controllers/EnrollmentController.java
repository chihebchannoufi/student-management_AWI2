package tn.esprit.studentmanagement.controllers;

import org.springframework.web.bind.annotation.*;
import tn.esprit.studentmanagement.dto.EnrollmentDTO;
import tn.esprit.studentmanagement.mappers.EnrollmentMapper;
import tn.esprit.studentmanagement.services.IEnrollment;

import java.util.List;

@RestController
@RequestMapping("/Enrollment")
@CrossOrigin(origins = "http://localhost:4200")
public class EnrollmentController {
    private final IEnrollment enrollmentService;

    public EnrollmentController(IEnrollment enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/getAllEnrollment")
    public List<EnrollmentDTO> getAllEnrollment() {
        return enrollmentService.getAllEnrollments().stream().map(EnrollmentMapper::toDto).toList();
    }

    @GetMapping("/getEnrollment/{id}")
    public EnrollmentDTO getEnrollment(@PathVariable Long id) {
        return EnrollmentMapper.toDto(enrollmentService.getEnrollmentById(id));
    }

    @PostMapping("/createEnrollment")
    public EnrollmentDTO createEnrollment(@RequestBody EnrollmentDTO dto) {
        return EnrollmentMapper.toDto(enrollmentService.saveEnrollment(EnrollmentMapper.toEntity(dto)));
    }

    @PutMapping("/updateEnrollment")
    public EnrollmentDTO updateEnrollment(@RequestBody EnrollmentDTO dto) {
        return EnrollmentMapper.toDto(enrollmentService.saveEnrollment(EnrollmentMapper.toEntity(dto)));
    }

    @DeleteMapping("/deleteEnrollment/{id}")
    public void deleteEnrollment(@PathVariable Long id) {
        enrollmentService.deleteEnrollment(id); }
}
