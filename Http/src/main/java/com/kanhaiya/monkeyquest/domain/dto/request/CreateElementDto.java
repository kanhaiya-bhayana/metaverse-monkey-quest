package com.kanhaiya.monkeyquest.domain.dto.request;

import lombok.Data;

@Data
public class CreateElementDto {
    private int width;
    private int height;
    private String imageUrl;
    private boolean isStatic;
}
