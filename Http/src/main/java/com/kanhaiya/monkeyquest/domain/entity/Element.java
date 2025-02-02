package com.kanhaiya.monkeyquest.domain.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Element {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private int width;
    private int height;
    private String imageUrl;
    private boolean isStatic;

    @OneToMany(mappedBy = "element", fetch = FetchType.EAGER, cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JsonManagedReference
    private Set<MapElements> mapElementsSet = new HashSet<>();
}
