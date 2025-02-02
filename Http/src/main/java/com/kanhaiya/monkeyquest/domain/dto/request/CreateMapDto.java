package com.kanhaiya.monkeyquest.domain.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CreateMapDto {
    private int height;
    private int width;
    private String thumbnail;
    List<MapElementsDto> mapElementsDtoList;
}
