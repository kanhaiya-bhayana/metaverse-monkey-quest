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
public class SpaceElements {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String elementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", referencedColumnName = "id")
    @JsonBackReference
    private Space space;

}
