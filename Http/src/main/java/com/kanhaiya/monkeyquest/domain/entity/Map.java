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
public class Map {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private int height;
    private int width;

    @OneToMany(mappedBy = "map", fetch = FetchType.EAGER, cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JsonManagedReference
    private Set<MapElements> mapElementsSet = new HashSet<>();
}
