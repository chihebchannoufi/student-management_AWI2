package tn.esprit.studentmanagement;

import org.junit.jupiter.api.Test;
import tn.esprit.studentmanagement.controllers.DepartmentController;
import tn.esprit.studentmanagement.controllers.EnrollmentController;
import tn.esprit.studentmanagement.controllers.StudentController;
import tn.esprit.studentmanagement.dto.DepartmentDTO;
import tn.esprit.studentmanagement.dto.EnrollmentDTO;
import tn.esprit.studentmanagement.dto.StudentDTO;
import tn.esprit.studentmanagement.entities.*;
import tn.esprit.studentmanagement.repositories.CourseRepository;
import tn.esprit.studentmanagement.repositories.StudentRepository;
import tn.esprit.studentmanagement.services.IDepartmentService;
import tn.esprit.studentmanagement.services.IEnrollment;
import tn.esprit.studentmanagement.services.IStudentService;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ControllerUnitTest {

    @Test
    void departmentControllerCrudUsesDtoFields() {
        IDepartmentService service = mock(IDepartmentService.class);
        DepartmentController controller = new DepartmentController(service);
        DepartmentDTO dto = new DepartmentDTO();
        dto.setName("Computer Science");
        dto.setLocation("A1");
        dto.setPhone("111");
        dto.setHead("Head");
        Department saved = new Department();
        when(service.saveDepartment(any(Department.class))).thenReturn(saved);

        assertEquals(saved, controller.createDepartment(dto));
        verify(service).saveDepartment(argThat(department -> "Computer Science".equals(department.getName())));

        Department existing = new Department();
        when(service.getDepartmentById(1L)).thenReturn(existing);
        controller.updateDepartment(1L, dto);
        assertEquals("Head", existing.getHead());
        controller.getAllDepartment();
        controller.getDepartment(1L);
        controller.deleteDepartment(1L);
        verify(service).deleteDepartment(1L);
    }

    @Test
    void studentControllerMapsDepartmentId() {
        IStudentService studentService = mock(IStudentService.class);
        IDepartmentService departmentService = mock(IDepartmentService.class);
        StudentController controller = new StudentController(studentService, departmentService);
        Department department = new Department();
        when(departmentService.getDepartmentById(2L)).thenReturn(department);
        StudentDTO dto = new StudentDTO();
        dto.setFirstName("Ada");
        dto.setDateOfBirth(LocalDate.of(2000, 1, 1));
        dto.setDepartmentId(2L);
        Student saved = new Student();
        when(studentService.saveStudent(any(Student.class))).thenReturn(saved);

        assertEquals(saved, controller.createStudent(dto));
        verify(studentService).saveStudent(argThat(student ->
                "Ada".equals(student.getFirstName()) && student.getDepartment() == department));

        Student existing = new Student();
        when(studentService.getStudentById(3L)).thenReturn(existing);
        controller.updateStudent(3L, dto);
        assertEquals(department, existing.getDepartment());
        controller.getAllStudents();
        controller.getStudent(3L);
        controller.deleteStudent(3L);
        verify(studentService).deleteStudent(3L);
    }

    @Test
    void enrollmentControllerMapsRelationIds() {
        IEnrollment enrollmentService = mock(IEnrollment.class);
        StudentRepository studentRepository = mock(StudentRepository.class);
        CourseRepository courseRepository = mock(CourseRepository.class);
        EnrollmentController controller = new EnrollmentController(
                enrollmentService, studentRepository, courseRepository);
        Student student = new Student();
        Course course = new Course();
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(3L)).thenReturn(Optional.of(course));
        EnrollmentDTO dto = new EnrollmentDTO();
        dto.setEnrollmentDate(LocalDate.of(2025, 1, 1));
        dto.setGrade(15.0);
        dto.setStatus(Status.ACTIVE);
        dto.setStudentId(2L);
        dto.setCourseId(3L);
        Enrollment saved = new Enrollment();
        when(enrollmentService.saveEnrollment(any(Enrollment.class))).thenReturn(saved);

        assertEquals(saved, controller.createEnrollment(dto));
        verify(enrollmentService).saveEnrollment(argThat(enrollment ->
                enrollment.getStudent() == student && enrollment.getCourse() == course));

        Enrollment existing = new Enrollment();
        when(enrollmentService.getEnrollmentById(4L)).thenReturn(existing);
        controller.updateEnrollment(4L, dto);
        assertEquals(Status.ACTIVE, existing.getStatus());
        controller.getAllEnrollment();
        controller.getEnrollment(4L);
        controller.deleteEnrollment(4L);
        verify(enrollmentService).deleteEnrollment(4L);
    }
}
