package com.kanhaiya.monkeyquest.domain.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Avatar {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String imageUrl;
    private String name;

    @OneToMany(mappedBy = "avatar", fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JsonManagedReference
    private Set<User> users = new HashSet<>();
}
