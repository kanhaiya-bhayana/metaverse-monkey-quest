package com.kanhaiya.monkeyquest.domain.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateSpaceElementDto {
    private String elementId;
    private String spaceId;
    private int x;
    private int y;
}
