package com.kanhaiya.monkeyquest.domain.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.kanhaiya.monkeyquest.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String userName;
    private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "avatar_id", referencedColumnName = "id") // foreign key of avatar entity
    @JsonBackReference
    private Avatar avatar;

    private UserRole role = UserRole.USER;
}
