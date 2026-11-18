package tn.esprit.studentmanagement.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.exceptions.ResourceNotFoundException;
import tn.esprit.studentmanagement.repositories.DepartmentRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {
    @Mock
    private DepartmentRepository departmentRepository;

    private DepartmentService departmentService;

    @BeforeEach
    void setUp() {
        departmentService = new DepartmentService(departmentRepository);
    }

    @Test
    void getAllDepartmentsReturnsRepositoryValues() {
        Department department = new Department();
        department.setName("Computer Science");
        when(departmentRepository.findAll()).thenReturn(List.of(department));

        assertEquals(List.of(department), departmentService.getAllDepartments());
        verify(departmentRepository).findAll();
    }

    @Test
    void getDepartmentByIdReturnsFoundDepartment() {
        Department department = new Department();
        department.setIdDepartment(2L);
        when(departmentRepository.findById(2L)).thenReturn(Optional.of(department));

        assertEquals(department, departmentService.getDepartmentById(2L));
    }

    @Test
    void getDepartmentByIdThrowsWhenDepartmentIsMissing() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> departmentService.getDepartmentById(99L));
    }

    @Test
    void saveDepartmentPersistsDepartment() {
        Department department = new Department();
        department.setName("Mathematics");
        when(departmentRepository.save(department)).thenReturn(department);

        assertEquals(department, departmentService.saveDepartment(department));
        verify(departmentRepository).save(department);
    }

    @Test
    void deleteDepartmentDeletesRequestedDepartment() {
        departmentService.deleteDepartment(3L);

        verify(departmentRepository).deleteById(3L);
    }
}
