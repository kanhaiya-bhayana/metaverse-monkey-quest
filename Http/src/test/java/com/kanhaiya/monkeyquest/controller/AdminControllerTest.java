package com.kanhaiya.monkeyquest.controller;

import com.kanhaiya.monkeyquest.domain.dto.request.CreateAvatarDto;
import com.kanhaiya.monkeyquest.domain.dto.request.CreateElementDto;
import com.kanhaiya.monkeyquest.domain.dto.request.CreateMapDto;
import com.kanhaiya.monkeyquest.domain.dto.request.SignUpDto;
import com.kanhaiya.monkeyquest.domain.dto.response.LoginResponse;
import com.kanhaiya.monkeyquest.domain.entity.Element;
import com.kanhaiya.monkeyquest.domain.entity.GameMap;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.PUT;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AdminControllerTest{
    @Value("${local.server.port}")
    void setServerPort(int serverPort) {
        AdminControllerTest.serverPort = serverPort;
    }

    @Value("${local.urls.baseurl}")
    void setBaseUrl(String url){
        AdminControllerTest.baseUrl = url;
    }
    private static int serverPort;
    private static String baseUrl;
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
    }

    @DisplayName("User is not able to hit admin endpoints")
    @Test
    void userIsNotAbleToHitAdminEndpoints(){
        String url = baseUrl+":"+serverPort+"/api/v1/admin/element";
        String elementUrl = baseUrl+":"+serverPort+"/"+"/api/v1/admin/element";
        String mapUrl = baseUrl+":"+serverPort+"/"+"/api/v1/admin/map";
        String creatAvatarUrl = baseUrl+":"+serverPort+"/"+"/api/v1/admin/avatar";

        // Create elements
        CreateElementDto createElement1Request = new CreateElementDto();
        createElement1Request.setImageUrl("");
        createElement1Request.setHeight(100);
        createElement1Request.setWidth(200);
        createElement1Request.setStatic(true);

        HttpEntity<CreateElementDto> element1HttpRequest = new HttpEntity<>(createElement1Request, userHeaders);
        ResponseEntity<Element> element1Response = restTemplate.postForEntity(elementUrl, element1HttpRequest, Element.class);

        // Create map
        CreateMapDto mapRequest = new CreateMapDto();
        mapRequest.setThumbnail("");
        mapRequest.setHeight(100);
        mapRequest.setWidth(100);

        HttpEntity<CreateMapDto> httpMapRequest = new HttpEntity<>(mapRequest, userHeaders);
        ResponseEntity<GameMap> mapResponseEntity = restTemplate.postForEntity(mapUrl, httpMapRequest, GameMap.class);

       
        // Create avatar
        CreateAvatarDto avatarRequest = new CreateAvatarDto();
        avatarRequest.setImageUrl("https://encrypted-tbn0.gstatic.com/images?" +
                "q=tbn:ANd9GcReDcgAZWsQXdjCrEsbEDPOcZE2-qlrrNxftQ&s");
        avatarRequest.setName("Timmy");

        HttpEntity<CreateAvatarDto> httpAvatarRequest = new HttpEntity<>(avatarRequest, userHeaders);
        ResponseEntity<String> avatarResponse = restTemplate.postForEntity(creatAvatarUrl, httpAvatarRequest, String.class);

        // update elements
        CreateElementDto updateElementRequest = new CreateElementDto();
        updateElementRequest.setImageUrl("test");
        updateElementRequest.setHeight(200);
        updateElementRequest.setWidth(200);
        updateElementRequest.setStatic(true);

        HttpEntity<CreateElementDto> updateElementHttpRequest = new HttpEntity<>(updateElementRequest, userHeaders);
        ResponseEntity<Element> updateElementResponse = restTemplate.exchange(
                elementUrl,
                PUT,
                updateElementHttpRequest,
                Element.class);
        

        assertEquals(HttpStatus.UNAUTHORIZED, element1Response.getStatusCode(),
        "Status code should be 403");

        assertEquals(HttpStatus.UNAUTHORIZED, mapResponseEntity.getStatusCode(),
        "Status code should be 403");
        
        assertEquals(HttpStatus.UNAUTHORIZED, avatarResponse.getStatusCode(),
        "Status code should be 403");

        assertEquals(HttpStatus.UNAUTHORIZED, updateElementResponse.getStatusCode(),
        "Status code should be 403");
    }

    @DisplayName("Admin is able to hit admin endpoints")   
    @Test
    void adminIsAbleToHitAdminEndpoints(){
        String url = baseUrl+":"+serverPort+"/api/v1/admin/element";
        String elementUrl = baseUrl+":"+serverPort+"/"+"/api/v1/admin/element";
        String mapUrl = baseUrl+":"+serverPort+"/"+"/api/v1/admin/map";
        String creatAvatarUrl = baseUrl+":"+serverPort+"/"+"/api/v1/admin/avatar";

        // Create elements
        CreateElementDto createElement1Request = new CreateElementDto();
        createElement1Request.setImageUrl("");
        createElement1Request.setHeight(100);
        createElement1Request.setWidth(200);
        createElement1Request.setStatic(true);

        HttpEntity<CreateElementDto> element1HttpRequest = new HttpEntity<>(createElement1Request, adminHeaders);
        ResponseEntity<Element> element1Response = restTemplate.postForEntity(elementUrl, element1HttpRequest, Element.class);
        String elementId = Objects.requireNonNull(element1Response).getBody().getId();

        // Create map
        CreateMapDto mapRequest = new CreateMapDto();
        mapRequest.setThumbnail("");
        mapRequest.setHeight(100);
        mapRequest.setWidth(100);

        HttpEntity<CreateMapDto> httpMapRequest = new HttpEntity<>(mapRequest, adminHeaders);
        ResponseEntity<GameMap> mapResponseEntity = restTemplate.postForEntity(mapUrl, httpMapRequest, GameMap.class);

       
        // Create avatar
        CreateAvatarDto avatarRequest = new CreateAvatarDto();
        avatarRequest.setImageUrl("https://encrypted-tbn0.gstatic.com/images?" +
                "q=tbn:ANd9GcReDcgAZWsQXdjCrEsbEDPOcZE2-qlrrNxftQ&s");
        avatarRequest.setName("Timmy");

        HttpEntity<CreateAvatarDto> httpAvatarRequest = new HttpEntity<>(avatarRequest, adminHeaders);
        ResponseEntity<String> avatarResponse = restTemplate.postForEntity(creatAvatarUrl, httpAvatarRequest, String.class);

        // update elements
        CreateElementDto updateElementRequest = new CreateElementDto();
        updateElementRequest.setImageUrl("test");
        updateElementRequest.setHeight(200);
        updateElementRequest.setWidth(200);
        updateElementRequest.setStatic(true);


        HttpEntity<CreateElementDto> updateElementHttpRequest = new HttpEntity<>(updateElementRequest, adminHeaders);
        ResponseEntity<Element> updateElementResponse = restTemplate.exchange(
                elementUrl+"/"+elementId,
                HttpMethod.Put,
                updateElementHttpRequest,
                Element.class);
        

        assertEquals(HttpStatus.OK, element1Response.getStatusCode(),
        "Status code should be 200");

        assertEquals(HttpStatus.OK, mapResponseEntity.getStatusCode(),
        "Status code should be 200");
        
        assertEquals(avatarResponse.OK, mapResponseEntity.getStatusCode(),
        "Status code should be 200");
        
        assertEquals(HttpStatus.OK, updateElementResponse.getStatusCode(),
        "Status code should be 200");
    }

    @DisplayName("Admin is able to update the imageUrl for an element")
    @Test
    void adminIsAbleToUpdateTheImageUrlForAnElement(){

        String elementUrl = baseUrl+":"+serverPort+"/"+"/api/v1/admin/element";

        // Create elements
        CreateElementDto createElement1Request = new CreateElementDto();
        createElement1Request.setImageUrl("");
        createElement1Request.setHeight(100);
        createElement1Request.setWidth(200);
        createElement1Request.setStatic(true);

        HttpEntity<CreateElementDto> element1HttpRequest = new HttpEntity<>(createElement1Request, adminHeaders);
        ResponseEntity<Element> element1Response = restTemplate.postForEntity(elementUrl, element1HttpRequest, Element.class);

        String elementId = Objects.requireNonNull(element1Response).getBody().getId();

        // update element
        CreateElementDto updateElementRequest = new CreateElementDto();
        updateElementRequest.setImageUrl("test");
        updateElementRequest.setHeight(200);
        updateElementRequest.setWidth(200);
        updateElementRequest.setStatic(true);

        HttpEntity<CreateElementDto> updateElementHttpRequest = new HttpEntity<>(updateElementRequest, adminHeaders);
        ResponseEntity<Element> updateElementResponse = restTemplate.exchange(
                elementUrl+"/"+elementId,
                HttpMethod.Put,
                updateElementHttpRequest,
                Element.class);
    
        assertEquals(HttpStatus.OK, updateElementResponse.getStatusCode(),
                "Status code should be 200");        
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