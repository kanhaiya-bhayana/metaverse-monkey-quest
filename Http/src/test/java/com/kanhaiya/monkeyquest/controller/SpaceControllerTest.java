package com.kanhaiya.monkeyquest.controller;

import com.kanhaiya.monkeyquest.domain.dto.request.*;
import com.kanhaiya.monkeyquest.domain.dto.response.LoginResponse;
import com.kanhaiya.monkeyquest.domain.entity.Element;
import com.kanhaiya.monkeyquest.domain.entity.GameMap;
import com.kanhaiya.monkeyquest.domain.entity.Space;
import com.kanhaiya.monkeyquest.domain.enums.UserRole;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

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



    }


    @DisplayName("User is able to create a space")
    @Test
    void userIsAbleToCreateASpace(){
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

        assertNotNull(Objects.requireNonNull(spaceResponseEntity.getBody()).getId());
    }

    @DisplayName("User is able to create a space without mapId empty space")
    @Test
    void userIsAbleToCreateASpaceWithoutMapId(){
        String spaceUrl = baseUrl+":"+serverPort+"/api/v1/space";

        // Create space
        CreateSpaceDto spaceDto = CreateSpaceDto.builder()
                .name("Test")
                .height(100)
                .width(200)
                .build();

        HttpEntity<CreateSpaceDto> httpSapceRequest = new HttpEntity<>(spaceDto, userHeaders);
        ResponseEntity<Space> spaceResponseEntity  = restTemplate.postForEntity(spaceUrl, httpSapceRequest, Space.class);

        assertNotNull(Objects.requireNonNull(spaceResponseEntity.getBody()).getId());
    }

    @DisplayName("User is not able to create a space without mapId and dimensions")
    @Test
    void userIsNotAbleToCreateASpaceWithoutMapAndDimensions(){
        String spaceUrl = baseUrl+":"+serverPort+"/api/v1/space";

        // Create space
        CreateSpaceDto spaceDto = CreateSpaceDto.builder()
                .name("Test")
                .build();

        HttpEntity<CreateSpaceDto> httpSapceRequest = new HttpEntity<>(spaceDto, userHeaders);
        ResponseEntity<Space> spaceResponseEntity  = restTemplate.postForEntity(spaceUrl, httpSapceRequest, Space.class);

        assertEquals(HttpStatus.BAD_REQUEST, spaceResponseEntity.getStatusCode(), "Status code should be 400");
    }

    @DisplayName("User is not able to delete random space that doesnt exist")
    @Test
    void userIsNotAbleToDeleteRandomSpaceThatDoesntExist(){
        String spaceId = "sps-1-d3";
        String spaceUrl = baseUrl+":"+serverPort+"/api/v1/space/"+spaceId;

        HttpEntity<Void> httpSpaceRequest = new HttpEntity<>(userHeaders);
        ResponseEntity<String> spaceResponseEntity  = restTemplate.exchange(spaceUrl, HttpMethod.DELETE ,httpSpaceRequest, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, spaceResponseEntity.getStatusCode(), "Status code should be 400");
    }

    @DisplayName("User is able to delete a space that does exist")
    @Test
    void userIsAbleToDeleteASpaceThatDoesExist(){

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

        String spaceId = Objects.requireNonNull(spaceResponseEntity.getBody()).getId();
        String spaceDeleteUrl = baseUrl+":"+serverPort+"/api/v1/space/"+spaceId;

        // Delete space you just created
        HttpEntity<Void> httpSpaceRequest = new HttpEntity<>(userHeaders);
        ResponseEntity<String> spaceDeleteResponseEntity  = restTemplate.exchange(spaceDeleteUrl, HttpMethod.DELETE ,httpSpaceRequest, String.class);

        assertEquals(HttpStatus.OK, spaceResponseEntity.getStatusCode(), "Status code should be 200");
    }

    @DisplayName("User should not be able to delete a space created by another user")
    @Test
    void userCannotDeleteSpaceOwnedByAnotherUser(){
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

        String spaceId = Objects.requireNonNull(spaceResponseEntity.getBody()).getId();
        String spaceDeleteUrl = baseUrl+":"+serverPort+"/api/v1/space/"+spaceId;

        // Delete space you just created
        HttpEntity<Void> httpSpaceRequest = new HttpEntity<>(adminHeaders);
        ResponseEntity<String> spaceDeleteResponseEntity  = restTemplate.exchange(
                spaceDeleteUrl,
                HttpMethod.DELETE,
                httpSpaceRequest,
                String.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, spaceResponseEntity.getStatusCode(), "Status code should be 403");
    }

    @DisplayName("Admin has no space initially")
    @Test
    void adminHasNoSpaceInitially(){
        String spaceUrl = baseUrl+":"+serverPort+"/api/v1/space/all";

        ResponseEntity<List<Space>> responseEntity = restTemplate.exchange(
                spaceUrl,
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders),
                new ParameterizedTypeReference<List<Space>>() {}
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode(), "StatusCode should be 200");
        assertEquals(0, Objects.requireNonNull(responseEntity.getBody()).size(), "Length should be 0");
    }

    @DisplayName("Admin has no space initially but can create")
    @Test
    void adminHasNoSpaceInitiallyButCanCreate(){
        String spaceUrl = baseUrl+":"+serverPort+"/api/v1/space/all";

        // Create space
        CreateSpaceDto spaceDto = CreateSpaceDto.builder()
                .name("Test")
                .height(100)
                .width(200)
                .build();

        HttpEntity<CreateSpaceDto> httpSpaceRequest = new HttpEntity<>(spaceDto, userHeaders);
        ResponseEntity<Space> spaceResponseEntity  = restTemplate.postForEntity(spaceUrl, httpSpaceRequest, Space.class);

        String spaceId = Objects.requireNonNull(spaceResponseEntity.getBody()).getId();

        ResponseEntity<List<Space>> responseEntity = restTemplate.exchange(
                spaceUrl,
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders),
                new ParameterizedTypeReference<List<Space>>() {}
        );

        Optional<Space> space = Objects.requireNonNull(responseEntity.getBody())
                        .stream()
                        .filter(s -> spaceId.equals(s.getId()))
                        .findFirst();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode(), "StatusCode should be 200");
        assertTrue(space.isPresent(), "Space with id: "+spaceId + " should be present");
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