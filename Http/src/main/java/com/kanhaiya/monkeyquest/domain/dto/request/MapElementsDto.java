package com.kanhaiya.monkeyquest.domain.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MapElementsDto {
    private String elementId;
    private int x;
    private int y;
}
