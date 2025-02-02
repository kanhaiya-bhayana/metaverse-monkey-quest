package com.kanhaiya.monkeyquest.domain.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateSpaceDto {
    private String name;
    private int height;
    private int width;
    private String mapId;
}
