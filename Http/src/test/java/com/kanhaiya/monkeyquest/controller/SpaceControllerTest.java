package com.kanhaiya.monkeyquest.controller;

import com.kanhaiya.monkeyquest.domain.dto.request.CreateElementDto;
import com.kanhaiya.monkeyquest.domain.dto.request.CreateMapDto;
import com.kanhaiya.monkeyquest.domain.dto.request.MapElementsDto;
import com.kanhaiya.monkeyquest.domain.dto.request.SignUpDto;
import com.kanhaiya.monkeyquest.domain.dto.response.LoginResponse;
import com.kanhaiya.monkeyquest.domain.entity.Element;
import com.kanhaiya.monkeyquest.domain.entity.GameMap;
import com.kanhaiya.monkeyquest.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SpaceControllerTest {
    @Value("${local.server.port}")
    void setServerPort(int serverPort) {
        SpaceControllerTest.serverPort = serverPort;
    }

    @Value("${local.urls.baseurl}")
    void setBaseUrl(String url){
        SpaceControllerTest.baseUrl = url;
    }
    private static int serverPort;
    private static String baseUrl;
    private static String mapId;
    private static String element1Id;
    private static String element2Id;
    private static String userId;
    private static String userToken;

    private static String adminId;
    private static String adminToken;

    private static RestTemplate restTemplate;
    private static HttpHeaders headers;
    private static Random random;
    @BeforeAll
    static void setup(){
        restTemplate = new RestTemplate();
        random = new Random();

        // Arrange
        SignUpDto signUpRequest = new SignUpDto();
        signUpRequest.setUserName("abc"+random.nextInt(100));
        signUpRequest.setPassword("12345");
        signUpRequest.setRole(UserRole.USER);


        SignUpDto signUpRequestAdmin = new SignUpDto();
        signUpRequestAdmin.setUserName("abc"+random.nextInt(100));
        signUpRequestAdmin.setPassword("12345");
        signUpRequestAdmin.setRole(UserRole.ADMIN);

        CreateElementDto createElement1Request = new CreateElementDto();
        createElement1Request.setImageUrl("");
        createElement1Request.setHeight(1);
        createElement1Request.setWidth(1);
        createElement1Request.setStatic(true);

        CreateElementDto createElement2Request = new CreateElementDto();
        createElement2Request.setImageUrl("");
        createElement2Request.setHeight(1);
        createElement2Request.setWidth(1);
        createElement2Request.setStatic(true);

        String signUpUrl = baseUrl+":"+serverPort+"/"+"/api/v1/auth/signup";
        String signInUrl = baseUrl+":"+serverPort+"/"+"/api/v1/auth/signin";
        String elementUrl = baseUrl+":"+serverPort+"/"+"/api/v1/admin/element";
        String mapUrl = baseUrl+":"+serverPort+"/"+"/api/v1/admin/map";


        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<SignUpDto> httpRequest = new HttpEntity<>(signUpRequest, headers);
        HttpEntity<SignUpDto> httpRequestAdmin = new HttpEntity<>(signUpRequestAdmin, headers);

        // Act
        ResponseEntity<String> signUpResponse = restTemplate.postForEntity( signUpUrl, httpRequest,String.class);
        ResponseEntity<String> signUpResponseAdmin = restTemplate.postForEntity( signUpUrl, httpRequestAdmin,String.class);
        ResponseEntity<LoginResponse> signInUser = restTemplate.postForEntity( signInUrl, httpRequest,LoginResponse.class);
        ResponseEntity<LoginResponse> signInAdmin = restTemplate.postForEntity( signInUrl, httpRequestAdmin,LoginResponse.class);

        userId = signUpResponse.getBody();
        userToken = signInUser.getBody().getJwtToken();

        adminId = signUpResponseAdmin.getBody();
        adminToken = signInAdmin.getBody().getJwtToken();

        headers.setBearerAuth(adminToken);  // Uncomment this line to make the endpoints authenticated

        // Create elements
        HttpEntity<CreateElementDto> element1HttpRequest = new HttpEntity<>(createElement1Request, headers);
        HttpEntity<CreateElementDto> element2HttpRequest = new HttpEntity<>(createElement2Request, headers);
        ResponseEntity<Element> element1Response = restTemplate.postForEntity(elementUrl, element1HttpRequest, Element.class);
        ResponseEntity<Element> element2Response = restTemplate.postForEntity(elementUrl, element2HttpRequest, Element.class);

        element1Id = element1Response.getBody().getId();
        element2Id = element2Response.getBody().getId();

        // Create map
        CreateMapDto mapRequest = new CreateMapDto();
        mapRequest.setThumbnail("");
        mapRequest.setHeight(100);
        mapRequest.setWidth(100);

        List<MapElementsDto> defaultElements = new ArrayList<>();
        defaultElements.add(MapElementsDto.builder()
                        .x(20)
                        .y(20)
                        .elementId(element1Id)
                        .build()
        );

        defaultElements.add(MapElementsDto.builder()
                .x(18)
                .y(20)
                .elementId(element2Id)
                .build()
        );

        mapRequest.setMapElementsDtoList(defaultElements);

        HttpEntity<CreateMapDto> httpMapRequest = new HttpEntity<>(mapRequest, headers);
        ResponseEntity<GameMap> mapResponseEntity = restTemplate.postForEntity(mapUrl, httpMapRequest, GameMap.class);

        mapId = mapResponseEntity.getBody().getId();



    }



}