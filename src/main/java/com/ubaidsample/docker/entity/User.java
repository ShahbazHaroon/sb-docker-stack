/*
 * @author Muhammad Ubaid Ur Raheem Ahmad AKA Shahbaz Haroon
 * Email: shahbazhrn@gmail.com
 * Cell: +923002585925
 * GitHub: https://github.com/ShahbazHaroon
 */

package com.ubaidsample.docker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Comment("Stores user information")
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_user_username", columnNames = "user_name")
        })
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false, updatable = false)
    @Comment("Unique identifier for each user")
    private Long userId;

    @Column(name = "user_name", nullable = false, length = 50)
    @Comment("User name")
    private String userName;

    @Column(name = "email", nullable = false, length = 50)
    @Comment("User email")
    private String email;

    @Column(name = "date_of_birth", nullable = false)
    @Comment("User dateOfBirth")
    private LocalDate dateOfBirth;

    @Column(name = "date_of_leaving", nullable = false)
    @Comment("User dateOfLeaving")
    private LocalDate dateOfLeaving;

    @Column(name = "postal_code", nullable = false)
    @Comment("User postalCode")
    private Integer postalCode;
}