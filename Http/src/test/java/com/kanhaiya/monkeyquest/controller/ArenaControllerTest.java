package com.kanhaiya.monkeyquest.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.kanhaiya.monkeyquest.domain.dto.request.*;
import com.kanhaiya.monkeyquest.domain.dto.response.LoginResponse;
import com.kanhaiya.monkeyquest.domain.entity.Avatar;
import com.kanhaiya.monkeyquest.domain.entity.Element;
import com.kanhaiya.monkeyquest.domain.entity.GameMap;
import com.kanhaiya.monkeyquest.domain.entity.Space;
import com.kanhaiya.monkeyquest.domain.enums.UserRole;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static com.kanhaiya.monkeyquest.controller.SpaceControllerTest.createHeaders;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ArenaControllerTest{
    @Value("${local.server.port}")
    void setServerPort(int serverPort) {
        ArenaControllerTest.serverPort = serverPort;
    }

    @Value("${local.urls.baseurl}")
    void setBaseUrl(String url){
        ArenaControllerTest.baseUrl = url;
    }
    private static int serverPort;
    private static String baseUrl;
    private static String mapId;
    private static String spaceId;
    private static String element1Id;
    private static String element2Id;
    private static String userId;
    private static String userToken;

    private static String adminId;
    private static String adminToken;

    private static RestTemplate restTemplate;
    private static HttpHeaders userHeaders;
    private static HttpHeaders adminHeaders;
    private static HttpHeaders headers;
    private static Random random;
    @BeforeAll
    static void setup(){
        restTemplate = new RestTemplate();
        random = new Random();

        // Create HttpHeader
        headers = createHeaders();

        // Arrange
        SignUpDto signUpRequest = new SignUpDto();
        signUpRequest.setUserName("abc"+random.nextInt(100));
        signUpRequest.setPassword("12345");
        signUpRequest.setRole(UserRole.USER);


        SignUpDto signUpRequestAdmin = new SignUpDto();
        signUpRequestAdmin.setUserName("abc"+random.nextInt(100));
        signUpRequestAdmin.setPassword("12345");
        signUpRequestAdmin.setRole(UserRole.ADMIN);

        String signUpUrl = baseUrl+":"+serverPort+"/"+"/api/v1/auth/signup";
        String signInUrl = baseUrl+":"+serverPort+"/"+"/api/v1/auth/signin";
        String elementUrl = baseUrl+":"+serverPort+"/"+"/api/v1/admin/element";
        String mapUrl = baseUrl+":"+serverPort+"/"+"/api/v1/admin/map";




        HttpEntity<SignUpDto> httpRequest = new HttpEntity<>(signUpRequest, headers);
        HttpEntity<SignUpDto> httpRequestAdmin = new HttpEntity<>(signUpRequestAdmin, headers);

        // Act
        ResponseEntity<String> signUpResponse = restTemplate.postForEntity( signUpUrl, httpRequest,String.class);
        ResponseEntity<String> signUpResponseAdmin = restTemplate.postForEntity( signUpUrl, httpRequestAdmin,String.class);
        ResponseEntity<LoginResponse> signInUser = restTemplate.postForEntity( signInUrl, httpRequest,LoginResponse.class);
        ResponseEntity<LoginResponse> signInAdmin = restTemplate.postForEntity( signInUrl, httpRequestAdmin,LoginResponse.class);

        userId = signUpResponse.getBody();
        userToken = signInUser.getBody().getJwtToken();
        userHeaders = createHeaders(userToken);

        adminId = signUpResponseAdmin.getBody();
        adminToken = signInAdmin.getBody().getJwtToken();
        adminHeaders = createHeaders(adminToken);


        // Create elements
        CreateElementDto createElement1Request = new CreateElementDto();
        createElement1Request.setImageUrl("");
        createElement1Request.setHeight(100);
        createElement1Request.setWidth(200);
        createElement1Request.setStatic(true);

        CreateElementDto createElement2Request = new CreateElementDto();
        createElement2Request.setImageUrl("");
        createElement2Request.setHeight(100);
        createElement2Request.setWidth(200);
        createElement2Request.setStatic(true);

        HttpEntity<CreateElementDto> element1HttpRequest = new HttpEntity<>(createElement1Request, adminHeaders);
        HttpEntity<CreateElementDto> element2HttpRequest = new HttpEntity<>(createElement2Request, adminHeaders);
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

        HttpEntity<CreateMapDto> httpMapRequest = new HttpEntity<>(mapRequest, adminHeaders);
        ResponseEntity<GameMap> mapResponseEntity = restTemplate.postForEntity(mapUrl, httpMapRequest, GameMap.class);

        mapId = mapResponseEntity.getBody().getId();

        // Create Space
        String spaceUrl = baseUrl+":"+serverPort+"/api/v1/space";

        // Create space
        CreateSpaceDto spaceDto = CreateSpaceDto.builder()
                .name("Test")
                .height(100)
                .width(200)
                .mapId(mapId)
                .build();


        HttpEntity<CreateSpaceDto> httpSapceRequest = new HttpEntity<>(spaceDto, userHeaders);
        ResponseEntity<Space> spaceResponseEntity  = restTemplate.postForEntity(spaceUrl, httpSapceRequest, Space.class);
        spaceId = Objects.requireNonNull(spaceResponseEntity.getBody()).getId();
    }

    @DisplayName("Incorrect spaceId returns a 400")
    @Test
    void incorrectSpaceIdReturnsCode400(){
        String tempSpaceId = "123sp-id";
        String spaceUrl = baseUrl+":"+serverPort+"/api/v1/space/"+tempSpaceId;

        HttpEntity<Void> httpRequest = new HttpEntity<>(userHeaders);
        ResponseEntity<String> spaceResponse = restTemplate.postForEntity(
                spaceUrl, 
                httpRequest, 
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, spaceResponse.getStatusCode(), 
                "Status code should be 400");
    }

    @DisplayName("Correct spaceId returns all the elements")
    @Test
    void correctSpaceIdReturnsAllTheElements(){
        String spaceUrl = baseUrl+":"+serverPort+"/api/v1/space/"+spaceId;

        HttpEntity<Void> httpRequest = new HttpEntity<>(userHeaders);
        ResponseEntity<List<Element>> spaceResponse = restTemplate.exchange(
                spaceUrl,
                GET,
                httpRequest, 
                new ParameterizedTypeReference<List<Element>>(){}
        );

        assertEquals(2, Objects.requireNonNull(spaceResponse.getBody()).size(),
                "Response list size should be 2");

        assertEquals(100, Objects.requireNonNull(spaceResponse.getBody()).get(0).getHeight(),
                "Height of first element should be 100");
        
        assertEquals(200, Objects.requireNonNull(spaceResponse.getBody()).get(0).getWidth(),
                "Width of first element should be 200");

        assertEquals(HttpStatus.BAD_REQUEST, spaceResponse.getStatusCode(),
                "Status code should be 200");
    }

    @DisplayName("Delete endpoint is able to delete an element")
    @Test
    void deleteEndpointIsAbleToDeleteAnElement(){
        String spaceUrl = baseUrl+":"+serverPort+"/api/v1/space/"+spaceId;
        String delementElementUrl = baseUrl+":"+serverPort+"/api/v1/space/element";

        // get the all elements
        HttpEntity<Void> httpRequest = new HttpEntity<>(userHeaders);
        ResponseEntity<List<Element>> spaceResponse = restTemplate.exchange(
                spaceUrl,
                GET,
                httpRequest, 
                new ParameterizedTypeReference<List<Element>>(){}
        );

        // delete element
//        DeleteElementDto deleteElementRequest = new DeleteElementDto();
//        deleteElementRequest.setSpaceId(spaceId);
//        deleteElementRequest.setElementId(spaceResponse.getBody().get(0).getId());

//        HttpEntity<Void> httpDeleteRequest = new HttpEntity<>(userHeaders);
        ResponseEntity<String> response = restTemplate.exchange(
            delementElementUrl,
            DELETE,
            new HttpEntity<>(userHeaders),
            String.class
        );

        // get the all elements
        HttpEntity<Void> httpRequest2 = new HttpEntity<>(userHeaders);
        ResponseEntity<List<Element>> spaceResponse2 = restTemplate.exchange(
                spaceUrl,
                GET,
                httpRequest2, 
                new ParameterizedTypeReference<List<Element>>(){}
        );

        assertEquals(HttpStatus.OK, spaceResponse2.getStatusCode(),
                "Status code should be 200");
        assertEquals(1, Objects.requireNonNull(spaceResponse2.getBody()).size(),
                "Response list size should be 1");
    }

    @DisplayName("Adding an element works as expected")
    @Test
    void addingAnElementWorksAsExpected(){
        String spaceGetUrl = baseUrl+":"+serverPort+"/api/v1/space/"+spaceId;
        String spacePostUrl = baseUrl+":"+serverPort+"/api/v1/space/element";

        // create space element
        CreateSpaceElementDto createSpaceElementRequest = CreateSpaceElementDto.builder()
                .elementId(element1Id)
                .spaceId(spaceId)
                .x(50)
                .y(20)
                .build();


        HttpEntity<CreateSpaceElementDto> httpPostRequest = new HttpEntity<>(createSpaceElementRequest, userHeaders);
        ResponseEntity<String> response = restTemplate.postForEntity(
                spacePostUrl,
                httpPostRequest,
                String.class
        );

        // get the all elements
        HttpEntity<Void> httpRequest2 = new HttpEntity<>(userHeaders);
        ResponseEntity<List<Element>> spaceResponse = restTemplate.exchange(
                spaceGetUrl,
                GET,
                httpRequest2,
                new ParameterizedTypeReference<List<Element>>(){}
        );

        assertEquals(HttpStatus.OK, spaceResponse.getStatusCode(),
                "Status code should be 200");
        assertEquals(3, Objects.requireNonNull(spaceResponse.getBody()).size(),
                "Response list size should be 3");
    }

    @DisplayName("Adding an element fails if the element lies outside the dimensions")
    @Test
    void addingAnElementFailsIfTheElementLiesOutsideTheDimensions(){
        String spaceGetUrl = baseUrl+":"+serverPort+"/api/v1/space/"+spaceId;
        String spacePostUrl = baseUrl+":"+serverPort+"/api/v1/space/element";

        // create space element
        CreateSpaceElementDto createSpaceElementRequest = CreateSpaceElementDto.builder()
                .elementId(element1Id)
                .spaceId(spaceId)
                .x(50000)
                .y(2000000)
                .build();


        HttpEntity<CreateSpaceElementDto> httpPostRequest = new HttpEntity<>(createSpaceElementRequest, userHeaders);
        ResponseEntity<String> response = restTemplate.postForEntity(
                spacePostUrl,
                httpPostRequest,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
                "Status code should be 400");

    }

    private static HttpHeaders createHeaders(String token){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        httpHeaders.setBearerAuth(token);
        return httpHeaders;
    }
    private static HttpHeaders createHeaders(){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        return httpHeaders;
    }

}