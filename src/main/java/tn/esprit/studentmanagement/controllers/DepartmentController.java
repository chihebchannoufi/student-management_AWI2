package tn.esprit.studentmanagement.controllers;

import org.springframework.web.bind.annotation.*;
import tn.esprit.studentmanagement.dto.DepartmentDTO;
import tn.esprit.studentmanagement.mappers.DepartmentMapper;
import tn.esprit.studentmanagement.services.IDepartmentService;

import java.util.List;

@RestController
@RequestMapping("/Depatment")
@CrossOrigin(origins = "http://localhost:4200")
public class DepartmentController {
    private final IDepartmentService departmentService;

    public DepartmentController(IDepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping("/getAllDepartment")
    public List<DepartmentDTO> getAllDepartment() {
        return departmentService.getAllDepartments().stream().map(DepartmentMapper::toDto).toList();
    }

    @GetMapping("/getDepartment/{id}")
    public DepartmentDTO getDepartment(@PathVariable Long id) {
        return DepartmentMapper.toDto(departmentService.getDepartmentById(id));
    }

    @PostMapping("/createDepartment")
    public DepartmentDTO createDepartment(@RequestBody DepartmentDTO dto) {
        return DepartmentMapper.toDto(departmentService.saveDepartment(DepartmentMapper.toEntity(dto)));
    }

    @PutMapping("/updateDepartment")
    public DepartmentDTO updateDepartment(@RequestBody DepartmentDTO dto) {
        return DepartmentMapper.toDto(departmentService.saveDepartment(DepartmentMapper.toEntity(dto)));
    }

    @DeleteMapping("/deleteDepartment/{id}")
    public void deleteDepartment(@PathVariable Long id) {
      departmentService.deleteDepartment(id); }
}
