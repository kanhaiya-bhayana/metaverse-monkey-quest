package com.kanhaiya.monkeyquest.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MapElements {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "map_id", referencedColumnName = "id")
    @JsonBackReference
    private GameMap gameMap;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "element_id", referencedColumnName = "id")
    @JsonBackReference
    private Element element;
    int x;
    int y;
}
