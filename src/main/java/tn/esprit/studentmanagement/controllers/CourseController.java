package tn.esprit.studentmanagement.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.esprit.studentmanagement.dto.CourseDTO;
import tn.esprit.studentmanagement.mappers.CourseMapper;
import tn.esprit.studentmanagement.services.ICourseService;

import java.util.List;

@RestController
@RequestMapping("/courses")
@CrossOrigin(origins = "http://localhost:4200")
public class CourseController {
    private final ICourseService courseService;

    public CourseController(ICourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public List<CourseDTO> getAllCourses() {
        return courseService.getAllCourses().stream()
                .map(CourseMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public CourseDTO getCourse(@PathVariable Long id) {
        return CourseMapper.toDto(courseService.getCourseById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseDTO createCourse(@RequestBody CourseDTO dto) {
        return CourseMapper.toDto(courseService.saveCourse(CourseMapper.toEntity(dto)));
    }

    @PutMapping("/{id}")
    public CourseDTO updateCourse(@PathVariable Long id, @RequestBody CourseDTO dto) {
        CourseDTO update = new CourseDTO(id, dto.name(), dto.code(), dto.credit(), dto.description());
        return CourseMapper.toDto(courseService.saveCourse(CourseMapper.toEntity(update)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
    }
}
