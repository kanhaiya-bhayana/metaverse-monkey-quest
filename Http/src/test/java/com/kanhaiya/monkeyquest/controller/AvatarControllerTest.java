package com.kanhaiya.monkeyquest.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.kanhaiya.monkeyquest.domain.dto.request.CreateAvatarDto;
import com.kanhaiya.monkeyquest.domain.dto.request.SignUpDto;
import com.kanhaiya.monkeyquest.domain.dto.response.LoginResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AvatarControllerTest {
    @Value("${local.server.port}")
    void setServerPort(int serverPort) {
        AvatarControllerTest.serverPort = serverPort;
    }

    @Value("${local.urls.baseurl}")
    void setBaseUrl(String url){
        AvatarControllerTest.baseUrl = url;
    }
    private static int serverPort;
    private static String baseUrl;
    private static String token;
    private static String avatarId;
    private static String userId;

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

        CreateAvatarDto avatarRequest = new CreateAvatarDto();
        avatarRequest.setImageUrl("https://encrypted-tbn0.gstatic.com/images?" +
                "q=tbn:ANd9GcReDcgAZWsQXdjCrEsbEDPOcZE2-qlrrNxftQ&s");
        avatarRequest.setName("Timmy");

        String signInUrl = baseUrl+":"+serverPort+"/"+"/api/v1/auth/signin";
        String signUpUrl = baseUrl+":"+serverPort+"/"+"/api/v1/auth/signup";
        String creatAvatarUrl = baseUrl+":"+serverPort+"/"+"/api/v1/admin/avatar";

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));


        HttpEntity<SignUpDto> httpRequest = new HttpEntity<>(signUpRequest, headers);
        HttpEntity<CreateAvatarDto> httpAvatarRequest = new HttpEntity<>(avatarRequest, headers);

        // Act
        ResponseEntity<String> signUpResponse = restTemplate.postForEntity( signUpUrl, httpRequest,String.class);
        ResponseEntity<LoginResponse> signInUser = restTemplate.postForEntity( signInUrl, httpRequest,LoginResponse.class);
        ResponseEntity<String> avatarResponse = restTemplate.postForEntity(creatAvatarUrl, httpAvatarRequest, String.class);

        userId = signUpResponse.getBody();
        token = signInUser.getBody().getJwtToken();
        headers.setBearerAuth(token);
        avatarId = avatarResponse.getBody();


    }

    @DisplayName("Get back avatar information for a user")
    @Test
    void getBackAvatarInformationForUser() {
        String[] ids = new String[] { userId };

        // Construct the URL with query parameters
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + ":" + serverPort + "/api/v1/user/metadata/bulk")
                .queryParam("ids", (Object[]) ids)
                .toUriString();

        HttpEntity<Void> httpRequest = new HttpEntity<>(headers);

        // Use exchange to retrieve JSON response
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, httpRequest, JsonNode.class);

        // Assertions for validation
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size(), "response array length should be 1");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status code should be 200");
        assertEquals(userId, response.getBody(), "User id should be correct");

        // Example: Checking if the JSON response contains the expected userid
        assertTrue(response.getBody().has("userId"), "Response should contain an 'userId' ");

    }

}