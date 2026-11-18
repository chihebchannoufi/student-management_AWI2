package tn.esprit.studentmanagement.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.esprit.studentmanagement.dtos.CourseCreateDto;
import tn.esprit.studentmanagement.dtos.CourseResponseDto;
import tn.esprit.studentmanagement.dtos.CourseUpdateDto;
import tn.esprit.studentmanagement.entities.Course;
import tn.esprit.studentmanagement.services.ICourseService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/courses")
@CrossOrigin(origins = "http://localhost:4200")
public class CourseController {
    private final ICourseService courseService;

    public CourseController(ICourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public List<CourseResponseDto> getAllCourses() {
        return courseService.getAllCourses().stream()
                .map(CourseResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public CourseResponseDto getCourse(@PathVariable Long id) {
        return CourseResponseDto.fromEntity(courseService.getCourseById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponseDto createCourse(@RequestBody CourseCreateDto dto) {
        Course course = new Course();
        course.setName(dto.getName());
        course.setCode(dto.getCode());
        course.setCredit(dto.getCredit());
        course.setDescription(dto.getDescription());
        return CourseResponseDto.fromEntity(courseService.saveCourse(course));
    }

    @PutMapping("/{id}")
    public CourseResponseDto updateCourse(@PathVariable Long id, @RequestBody CourseUpdateDto dto) {
        Course course = new Course();
        course.setName(dto.getName());
        course.setCode(dto.getCode());
        course.setCredit(dto.getCredit());
        course.setDescription(dto.getDescription());
        course.setIdCourse(id);
        return CourseResponseDto.fromEntity(courseService.saveCourse(course));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
    }
}
