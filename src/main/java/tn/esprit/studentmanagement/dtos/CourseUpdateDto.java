package tn.esprit.studentmanagement.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseUpdateDto {
    private String name;
    private String code;
    private int credit;
    private String description;
}
