package com.kanhaiya.monkeyquest.controller;

import com.kanhaiya.monkeyquest.domain.dto.request.LoginDto;
import com.kanhaiya.monkeyquest.domain.dto.request.SignUpDto;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

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

    @BeforeAll
    static void setUp() {
        restTemplate = new RestTemplate();
    }

    @Test
    void User_Is_Able_To_SignUp(){
        // Arrange
        SignUpDto signUpRequest = new SignUpDto();
        signUpRequest.setUserName("abc");
        signUpRequest.setPassword("12345");

        String url = baseUrl+":"+serverPort+"/"+"/api/v1/auth";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<SignUpDto> httpRequest = new HttpEntity<>(signUpRequest, headers);

        // Act
        ResponseEntity<String> createdUser = restTemplate.postForEntity( url, httpRequest,String.class);

        // Assert
        assertEquals(HttpStatus.OK, createdUser.getStatusCode(), "Expected a 200 StatusCode");
    }
}