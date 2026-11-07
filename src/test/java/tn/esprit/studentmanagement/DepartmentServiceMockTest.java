package tn.esprit.studentmanagement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import lombok.extern.slf4j.Slf4j;

import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.repositories.DepartmentRepository;
import tn.esprit.studentmanagement.services.DepartmentService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class DepartmentServiceMockTest {

    @Mock
    DepartmentRepository departmentRepository; // Faux Repository

    @InjectMocks
    DepartmentService departmentService; // Vrai Service dans lequel on injecte le Mock

    private Department department;

    @BeforeEach
    public void setUp() {
        // Initialisation d'un objet commun avant chaque test
        department = new Department();
        department.setIdDepartment(1L);
        department.setName("Télécommunications");
        department.setLocation("Bloc B");
        department.setPhone("71111111");
        department.setHead("Mme Sarra");
    }

    // 1. Test de saveDepartment()
    @Test
    public void testSaveDepartmentWithMock() {
        when(departmentRepository.save(Mockito.any(Department.class))).thenReturn(department);

        Department savedDept = departmentService.saveDepartment(department);
        log.info("Department sauvegardé (Mock) : " + savedDept);

        assertNotNull(savedDept);
        assertEquals(1L, savedDept.getIdDepartment());
        assertEquals("Télécommunications", savedDept.getName());
        verify(departmentRepository, times(1)).save(department);
    }

    // 2. Test de getDepartmentById()
    @Test
    public void testGetDepartmentByIdWithMock() {
        // findById retourne un Optional<Department> car le service appelle .get()
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        Department retrievedDept = departmentService.getDepartmentById(1L);
        log.info("Department récupéré par ID (Mock) : " + retrievedDept);

        assertNotNull(retrievedDept);
        assertEquals("Bloc B", retrievedDept.getLocation());
        assertEquals("Mme Sarra", retrievedDept.getHead());
        verify(departmentRepository, times(1)).findById(1L);
    }

    // 3. Test de getAllDepartments()
    @Test
    public void testGetAllDepartmentsWithMock() {
        Department d2 = new Department();
        d2.setIdDepartment(2L);
        d2.setName("Génie Civil");
        d2.setLocation("Bloc C");
        d2.setPhone("71222222");
        d2.setHead("Mr Karim");

        when(departmentRepository.findAll()).thenReturn(Arrays.asList(department, d2));

        List<Department> departments = departmentService.getAllDepartments();
        log.info("Liste des départements (Mock) - Taille : " + departments.size());

        assertNotNull(departments);
        assertEquals(2, departments.size());
        assertEquals("Télécommunications", departments.get(0).getName());
        assertEquals("Génie Civil", departments.get(1).getName());
        verify(departmentRepository, times(1)).findAll();
    }

    // 4. Test de deleteDepartment()
    @Test
    public void testDeleteDepartmentWithMock() {
        doNothing().when(departmentRepository).deleteById(1L);

        departmentService.deleteDepartment(1L);
        log.info("Suppression simulée pour le département ID : 1");

        // Vérifie que deleteById a bien été appelé 1 seule fois avec l'ID 1L
        verify(departmentRepository, times(1)).deleteById(1L);
    }
}