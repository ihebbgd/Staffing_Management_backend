package com.demo.staffing_management_backend.model;

import com.demo.staffing_management_backend.model.enums.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "projects")
public class Project {
    @Id
    private String id;

    private String name;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    @Indexed
    private ProjectStatus status;

    @Indexed
    @Builder.Default
    private List<String> requiredSkillIds=new ArrayList<>();
}
