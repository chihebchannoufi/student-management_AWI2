package tn.esprit.studentmanagement;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import lombok.extern.slf4j.Slf4j;

import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.services.IDepartmentService;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
@Slf4j
public class DepartmentServiceTest {

    @Autowired
    IDepartmentService departmentService;

    // Stocker l'ID généré à l'étape 1 pour l'utiliser dans les étapes 2, 3 et 4
    private static Long savedDeptId;

    // 1. Test de saveDepartment()
    @Test
    @Order(1)
    public void testSaveDepartment() {
        Department d = new Department();
        d.setName("Informatique");
        d.setLocation("Bloc A");
        d.setPhone("71000000");
        d.setHead("Mr Ahmed");

        Department savedDept = departmentService.saveDepartment(d);
        savedDeptId = savedDept.getIdDepartment(); // Sauvegarde de l'ID pour les tests suivants

        log.info("Department ajouté en BD : " + savedDept);

        assertNotNull(savedDeptId);
        assertEquals("Informatique", savedDept.getName());
    }

    // 2. Test de getDepartmentById()
    @Test
    @Order(2)
     void testGetDepartmentById() {
        assertNotNull(savedDeptId, "L'ID ne doit pas être null (le test 1 doit avoir réussi)");

        Department retrievedDept = departmentService.getDepartmentById(savedDeptId);
        log.info("Department récupéré depuis la BD : " + retrievedDept.getName());

        assertNotNull(retrievedDept);
        assertEquals("Informatique", retrievedDept.getName());
        assertEquals("Bloc A", retrievedDept.getLocation());
        assertEquals("Mr Ahmed", retrievedDept.getHead());
    }

    // 3. Test de getAllDepartments()
    @Test
    @Order(3)
    public void testGetAllDepartments() {
        List<Department> departments = departmentService.getAllDepartments();
        log.info("Nombre total de départements en BD : " + departments.size());

        assertNotNull(departments);
        assertFalse(departments.isEmpty(), "La liste des départements ne doit pas être vide");
    }

    // 4. Test de deleteDepartment()
    @Test
    @Order(4)
    public void testDeleteDepartment() {
        assertNotNull(savedDeptId, "L'ID ne doit pas être null avant la suppression");

        // Suppression dans la base MySQL
        departmentService.deleteDepartment(savedDeptId);
        log.info("Department supprimé de la BD, ID : " + savedDeptId);

        // Comme getDepartmentById() utilise .findById(id).get(), 
        // chercher un ID supprimé doit lever une NoSuchElementException
        assertThrows(NoSuchElementException.class, () -> {
            departmentService.getDepartmentById(savedDeptId);
        });
    }
}