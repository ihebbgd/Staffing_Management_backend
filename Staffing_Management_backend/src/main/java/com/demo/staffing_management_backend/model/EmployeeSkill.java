package com.demo.staffing_management_backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "employee_skills")
@CompoundIndex(name = "emp_skill_uniq", def = "{'employeeId': 1, 'skillId': 1}", unique = true)
public class EmployeeSkill {
    @Id
    private String id;

    private String employeeId;

    private String skillId;

    private int proficiencyLevel;
}
