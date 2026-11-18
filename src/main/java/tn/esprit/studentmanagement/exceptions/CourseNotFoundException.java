package tn.esprit.studentmanagement.exceptions;

public class CourseNotFoundException extends IllegalArgumentException {
    public CourseNotFoundException(Long id) {
        super("Course not found with id: " + id);
    }
}
