package com.kanhaiya.monkeyquest.controller;

import com.kanhaiya.monkeyquest.domain.dto.request.LoginDto;
import com.kanhaiya.monkeyquest.domain.dto.request.SignUpDto;
import com.kanhaiya.monkeyquest.domain.dto.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@RequiredArgsConstructor
class AuthControllerTest {

    private static RestTemplate restTemplate;
    @Value("${local.server.port}")
    void setServerPort(int serverPort) {
        AuthControllerTest.serverPort = serverPort;
    }

    @Value("${local.urls.baseurl}")
    void setBaseUrl(String url){
        AuthControllerTest.baseUrl = url;
    }
    private static int serverPort;
    private static String baseUrl;
    private static Random random;

    @BeforeAll
    static void setUp() {
        restTemplate = new RestTemplate();
        random = new Random();
    }


    @Test
    @DisplayName("User is able to sing up only once")
    void User_Is_Able_To_SignUp(){
        // Arrange
        SignUpDto signUpRequest = new SignUpDto();
        signUpRequest.setUserName("abc"+random.nextInt(100));
        signUpRequest.setPassword("12345");

        SignUpDto signUpRequest2 = new SignUpDto();
        signUpRequest.setUserName("abc"+random.nextInt(100));
        signUpRequest.setPassword("12345");

        String url = baseUrl+":"+serverPort+"/"+"/api/v1/auth/signup";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<SignUpDto> httpRequest = new HttpEntity<>(signUpRequest, headers);
        HttpEntity<SignUpDto> httpRequest2 = new HttpEntity<>(signUpRequest2, headers);

        // Act
        ResponseEntity<String> createdUser = restTemplate.postForEntity( url, httpRequest,String.class);
        ResponseEntity<String> createdUser2 = restTemplate.postForEntity( url, httpRequest2,String.class);

        // Assert
        assertEquals(HttpStatus.OK, createdUser.getStatusCode(), "Expected a 200 StatusCode");
        assertEquals(HttpStatus.BAD_REQUEST, createdUser2.getStatusCode(), "Expected a 400 StatusCode");
    }

    @Test
    @DisplayName("Signup request fails if the username is empty")
    void Signup_Request_Fails_If_Username_Is_Empty(){
        // Arrange
        SignUpDto signUpRequest = new SignUpDto();
//        signUpRequest.setUserName("");
        signUpRequest.setPassword("12345");

        String url = baseUrl+":"+serverPort+"/"+"/api/v1/auth/signup";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<SignUpDto> httpRequest = new HttpEntity<>(signUpRequest, headers);

        // Act
        ResponseEntity<String> createdUser = restTemplate.postForEntity( url, httpRequest,String.class);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, createdUser.getStatusCode(), "Username can not be empty");
    }

    @Test
    @DisplayName("Signin succeeds if the username and password are correct")
    void Signin_Succeeds_If_The_Username_And_Password_Are_Correct(){
        // Arrange
        SignUpDto signUpRequest = new SignUpDto();
        signUpRequest.setUserName("abc"+random.nextInt(100));
        signUpRequest.setPassword("12345");

        String signUpUrl = baseUrl+":"+serverPort+"/"+"/api/v1/auth/signup";
        String signInUrl = baseUrl+":"+serverPort+"/"+"/api/v1/auth/signin";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<SignUpDto> httpRequest = new HttpEntity<>(signUpRequest, headers);

        // Act
        restTemplate.postForEntity( signUpUrl, httpRequest,String.class);
        ResponseEntity<LoginResponse> signInUser = restTemplate.postForEntity( signInUrl, httpRequest,LoginResponse.class);

        // Assert
        assertEquals(HttpStatus.OK, signInUser.getStatusCode(), "Status code should be 200");
        assertNotNull(signInUser.getBody().getJwtToken(), "token can not be empty");
    }

    @Test
    @DisplayName("Signin fails if the username and password are incorrect")
    void Signin_Fails_If_The_Username_And_Password_Are_Incorrect(){
        // Arrange
        SignUpDto signUpRequest = new SignUpDto();
        signUpRequest.setUserName("abc"+random.nextInt(100));
        signUpRequest.setPassword("12345");

        String signInUrl = baseUrl+":"+serverPort+"/"+"/api/v1/auth/signin";
        String signUpUrl = baseUrl+":"+serverPort+"/"+"/api/v1/auth/signup";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<SignUpDto> httpRequest = new HttpEntity<>(signUpRequest, headers);

        // Act
        restTemplate.postForEntity( signUpUrl, httpRequest,String.class);
        ResponseEntity<LoginResponse> signInUser = restTemplate.postForEntity( signInUrl, httpRequest,LoginResponse.class);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, signInUser.getStatusCode(), "Status code should be 403");
        assertNull(signInUser.getBody().getJwtToken(), "token should be null");
    }
}