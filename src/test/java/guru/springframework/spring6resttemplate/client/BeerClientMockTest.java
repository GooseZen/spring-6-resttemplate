package guru.springframework.spring6resttemplate.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestToUriTemplate;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withAccepted;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import guru.springframework.spring6resttemplate.config.OAuthClientInterceptor;
import guru.springframework.spring6resttemplate.config.RestTemplateBuilderConfig;
import guru.springframework.spring6resttemplate.model.BeerDTO;
import guru.springframework.spring6resttemplate.model.BeerDTOPageImpl;
import guru.springframework.spring6resttemplate.model.BeerStyle;

@RestClientTest
@Import(RestTemplateBuilderConfig.class)
public class BeerClientMockTest {

    static final String URL = "http://localhost:8080";

    BeerClient beerClient;

    MockRestServiceServer server;

    @Autowired
    RestTemplateBuilder restTemplateBuilderConfigured;

    @Autowired
    ObjectMapper objectMapper;

    @Mock
    RestTemplateBuilder mockRestTemplateBuilder = new RestTemplateBuilder(new MockServerRestTemplateCustomizer());
    
    BeerDTO dto;
    String dtoJson;
    static final String AUTH = "Authorization";
    static final String AUTHVAL = "Bearer test";
    
    @MockBean
    OAuth2AuthorizedClientManager manager;
    
    @TestConfiguration
    public static class TestConfig {
    	@Bean
    	ClientRegistrationRepository clientRegistrationRepository() {
    		return new InMemoryClientRegistrationRepository(ClientRegistration
    				.withRegistrationId("springauth")
    				.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
    				.clientId("test")
    				.tokenUri("test")
    				.build());
    	}
    	
    	@Bean
    	OAuth2AuthorizedClientService auth2AuthorizedClientService(ClientRegistrationRepository clientRegistrationRepository) {
    		return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    	}
    	
    	@Bean
    	OAuthClientInterceptor oAuthClientInterceptor(OAuth2AuthorizedClientManager manager, ClientRegistrationRepository clientRegistrationRepository) {
    		return new OAuthClientInterceptor(manager, clientRegistrationRepository);
    	}
    }
    
    @Autowired
    ClientRegistrationRepository clientRegistrationRepository;

    @BeforeEach
    void setUp() throws JsonProcessingException {
    	ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId("springauth");
    	OAuth2AccessToken token = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, " test", Instant.MIN, Instant.MAX);
    	when(manager.authorize(any())).thenReturn(new OAuth2AuthorizedClient(clientRegistration, "test", token));
    
        RestTemplate restTemplate = restTemplateBuilderConfigured.build();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        when(mockRestTemplateBuilder.build()).thenReturn(restTemplate);
        beerClient = new BeerClientImpl(mockRestTemplateBuilder);
        
        dto = getBeerDto();
		dtoJson = objectMapper.writeValueAsString(dto);
    }
    
    @Test
    void testListBeersWithQueryParam() throws JsonProcessingException {
    	String response = objectMapper.writeValueAsString(getPage());
    	
    	URI uri = UriComponentsBuilder.fromUriString(URL + BeerClientImpl.GET_BEER_PATH).queryParam("beerName", "ALE").build().toUri();
    	
    	server.expect(method(HttpMethod.GET))
    			.andExpect(requestTo(uri))
    			.andExpect(header(AUTH, AUTHVAL))
    			.andExpect(queryParam("beerName", "ALE"))
    			.andRespond(withSuccess(response, MediaType.APPLICATION_JSON));
    	
    	Page<BeerDTO> responsePage = beerClient.listBeers("ALE", null, null, null, null);
    	
    	assertThat(responsePage.getContent().size()).isEqualTo(1);
    }
    
    @Test
    void testDeleteNotFound() {
    	server.expect(method(HttpMethod.DELETE))
				.andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH, dto.getId()))
    			.andExpect(header(AUTH, AUTHVAL))
				.andRespond(withResourceNotFound());   	
    	
    	assertThrows(HttpClientErrorException.class, () -> {
    		beerClient.deleteBeer(dto.getId());
    	});
    	
    	server.verify();        
    }
    
    @Test
    void testDeleteBeer() {
    	server.expect(method(HttpMethod.DELETE))
    			.andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH, dto.getId()))
    			.andExpect(header(AUTH, AUTHVAL))
    			.andRespond(withNoContent());
    	
    	beerClient.deleteBeer(dto.getId());
    	
    	server.verify();
    }
    
    @Test
    void testUpdateBeer() {
    	server.expect(method(HttpMethod.PUT))
    			.andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH, dto.getId()))
    			.andExpect(header(AUTH, AUTHVAL))
    			.andRespond(withNoContent());
    	
    	mockGetOperation();
    	
    	BeerDTO responseDto = beerClient.updateBeer(dto);
    	assertThat(responseDto.getId()).isEqualTo(dto.getId());
    }

	@Test
	void testCreateBeer() throws JsonProcessingException {
		
		URI uri = UriComponentsBuilder.fromPath(BeerClientImpl.GET_BEER_BY_ID_PATH).build(dto.getId());
		
		server.expect(method(HttpMethod.POST))
				.andExpect(requestTo(URL + BeerClientImpl.GET_BEER_PATH))
    			.andExpect(header(AUTH, AUTHVAL))
				.andRespond(withAccepted().location(uri));
		
		mockGetOperation();
		
		BeerDTO responseDto = beerClient.createBeer(dto);
		assertThat(responseDto.getId()).isEqualTo(dto.getId());
	}
	
    @Test
    void testListBeers() throws JsonProcessingException {
    	String payload = objectMapper.writeValueAsString(getPage());
    	
        server.expect(method(HttpMethod.GET))
                .andExpect(requestTo(URL + BeerClientImpl.GET_BEER_PATH))
    			.andExpect(header(AUTH, AUTHVAL))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        Page<BeerDTO> dtos = beerClient.listBeers();
        assertThat(dtos.getContent().size()).isGreaterThan(0);
    }
    
    @Test
    void testGetBeerById() {
        mockGetOperation();

        BeerDTO responseDto = beerClient.getBeerById(dto.getId());
        assertThat(responseDto.getId()).isEqualTo(dto.getId());
    }

	private void mockGetOperation() {
		server.expect(method(HttpMethod.GET))
                .andExpect(requestToUriTemplate(URL +
                        BeerClientImpl.GET_BEER_BY_ID_PATH, dto.getId()))
    			.andExpect(header(AUTH, AUTHVAL))
                .andRespond(withSuccess(dtoJson, MediaType.APPLICATION_JSON));
	}

    BeerDTO getBeerDto(){
        return BeerDTO.builder()
                .id(UUID.randomUUID())
                .price(new BigDecimal("10.99"))
                .beerName("Mango Bobs")
                .beerStyle(BeerStyle.IPA)
                .quantityOnHand(500)
                .upc("123245")
                .build();
    }

    BeerDTOPageImpl getPage() {
        return new BeerDTOPageImpl(Arrays.asList(getBeerDto()), 1, 25, 1);
    }
}