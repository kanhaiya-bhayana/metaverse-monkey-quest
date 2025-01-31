package com.kanhaiya.monkeyquest.controller;

import com.kanhaiya.monkeyquest.domain.dto.request.CreateAvatarDto;
import com.kanhaiya.monkeyquest.domain.dto.request.SignUpDto;
import com.kanhaiya.monkeyquest.domain.dto.response.LoginResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {
    @Value("${local.server.port}")
    void setServerPort(int serverPort) {
        UserControllerTest.serverPort = serverPort;
    }

    @Value("${local.urls.baseurl}")
    void setBaseUrl(String url){
        UserControllerTest.baseUrl = url;
    }
    private static int serverPort;
    private static String baseUrl;
    private static String token;
    private static String avatarId;

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
        restTemplate.postForEntity( signUpUrl, httpRequest,String.class);
        ResponseEntity<LoginResponse> signInUser = restTemplate.postForEntity( signInUrl, httpRequest,LoginResponse.class);
        ResponseEntity<String> avatarResponse = restTemplate.postForEntity(creatAvatarUrl, httpAvatarRequest, String.class);

        token = signInUser.getBody().getJwtToken();
        headers.setBearerAuth(token);
        avatarId = avatarResponse.getBody();


    }

    @DisplayName("User can not update their metadata with a wrong avatar id")
    @Test
    void User_Cannot_Update_Their_Metadata_With_A_Wrong_AvatarId(){
        String metaDataUrl = baseUrl+":"+serverPort+"/api/v1/user/metadata";
        String avatarId = "123123";

        HttpEntity<String> httpRequest = new HttpEntity<>(avatarId, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(metaDataUrl, httpRequest, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Status code should be 400");
    }

    @DisplayName("User can update their metadata with a right avatar id")
    @Test
    void User_Can_Update_Their_Metadata_With_A_Right_AvatarId(){
        String metaDataUrl = baseUrl+":"+serverPort+"/api/v1/user/metadata";

        HttpEntity<String> httpRequest = new HttpEntity<>(avatarId, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(metaDataUrl, httpRequest, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status code should be 200");
    }

    @DisplayName("User is not able to update their metadata if the auth header is not present")
    @Test
    void User_Is_Not_Able_To_Update_Their_Metadata_If_The_Auth_Header_Is_Not_Present(){
        String metaDataUrl = baseUrl+":"+serverPort+"/api/v1/user/metadata";

        HttpEntity<String> httpRequest = new HttpEntity<>(avatarId);

        ResponseEntity<String> response = restTemplate.postForEntity(metaDataUrl, httpRequest, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(), "Status code should be 403");
    }


}