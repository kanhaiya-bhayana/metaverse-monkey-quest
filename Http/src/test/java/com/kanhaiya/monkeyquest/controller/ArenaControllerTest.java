package com.kanhaiya.monkeyquest.controller;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AreneControllerTest{
    @Value("${local.server.port}")
    void setServerPort(int serverPort) {
        AreneControllerTest.serverPort = serverPort;
    }

    @Value("${local.urls.baseurl}")
    void setBaseUrl(String url){
        AreneControllerTest.baseUrl = url;
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
        spaceId = Object.requireNonNull(spaceResponseEntity.getBody()).getId();
    }

    @DisplayName("Incorrect spaceId returns a 400")
    @Test
    void incorrectSpaceIdReturnsCode400(){
        String tempSpaceId = "123sp-id";
        String spaceUrl = baseUrl+":"+serverPort+"/api/v1/space/"+tempSpaceId;

        HttpEntity<Void> httpRequest = new HttpEntity<>(headers);
        ResponseEntity<String> spaceResponse = restTemplate.postForEntity(
                spaceUrl, 
                httpRequest, 
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, spaceResponse.getStatusCode(), 
                "Status code should be 400");
    }
}