package com.siemens.internship.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Pattern(regexp = "^[a-zA-Z0-9_ ]{3,50}$", message = "Name must be 3-50 characters long and can only contain letters, numbers, spaces, and underscores")
    private String name;

    @Pattern(regexp = "^[a-zA-Z0-9_ ]{3,50}$", message = "Description must be 3-50 characters long and can only contain letters, numbers, spaces, and underscores")
    private String description;

    @Pattern(regexp = "^(UNPROCESSED|PROCESSED)$", message = "Status must be either 'UNPROCESSED' or 'PROCESSED'")
    private String status;

    // @Email(message = "Invalid email format")
    @Pattern(regexp = "^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,6}$", message = "Invalid email format")
    private String email;
}