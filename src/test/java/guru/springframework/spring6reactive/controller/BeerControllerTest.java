package guru.springframework.spring6reactive.controller;

import guru.springframework.spring6reactive.domain.Beer;
import guru.springframework.spring6reactive.domain.Customer;
import guru.springframework.spring6reactive.mapper.BeerMapper;
import guru.springframework.spring6reactive.model.BeerDTO;
import guru.springframework.spring6reactive.repository.BeerRepository;
import guru.springframework.spring6reactive.repository.BeerRepositoryTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static guru.springframework.spring6reactive.controller.BeerController.BEER_PATH;
import static guru.springframework.spring6reactive.controller.BeerController.BEER_PATH_ID;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOAuth2Login;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class BeerControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    BeerMapper beerMapper;

    @Autowired
    BeerRepository beerRepository;

    Beer savedBeer;

    @BeforeEach
    void setUp() {
        beerRepository.deleteAll().block();

        List<Beer> beers = beerRepository.saveAll(createBeers())
                .collectList()
                .block();

        Assertions.assertNotNull(beers);
        savedBeer = beers.getFirst();
    }

    @Test
    @Order(1)
    void testListBeers() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .get()
                .uri(BEER_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-type", "application/json")
                .expectBody().jsonPath("$.size()").isEqualTo(3);
    }

    @Test
    @Order(2)
    void testGetBeerById() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .get()
                .uri(BEER_PATH_ID, savedBeer.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-type", "application/json")
                .expectBody(BeerDTO.class);
    }

    @Test
    void testGetBeerByIdNotFound() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .get()
                .uri(BEER_PATH_ID, 999)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(3)
    void testCreateBeer() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .post()
                .uri(BEER_PATH)
                .body(Mono.just(beerMapper.beerToBeerDto(BeerRepositoryTest.createTestBeer())), BeerDTO.class)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isCreated()
                //.expectHeader().location("http://localhost:8080/api/v2/beer/4")
                .expectHeader().valueMatches("Location", ".*/api/v2/beer/\\d+");
    }

    @Test
    void testCreateBeerBadRequest() {
        Beer testBeer = BeerRepositoryTest.createTestBeer();
        testBeer.setBeerName("");

        webTestClient
                .mutateWith(mockOAuth2Login())
                .post()
                .uri(BEER_PATH)
                .body(Mono.just(beerMapper.beerToBeerDto(testBeer)), BeerDTO.class)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Order(4)
    void testUpdateBeer() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .put()
                .uri(BEER_PATH_ID, savedBeer.getId())
                .body(Mono.just(beerMapper.beerToBeerDto(BeerRepositoryTest.createTestBeer())), BeerDTO.class)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void testUpdateBeerBadRequest() {
        BeerDTO testBeer = beerMapper.beerToBeerDto(BeerRepositoryTest.createTestBeer());
        testBeer.setBeerStyle("");

        webTestClient
                .mutateWith(mockOAuth2Login())
                .put()
                .uri(BEER_PATH_ID, 1)
                .body(Mono.just(testBeer), BeerDTO.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testUpdateBeerNotFound() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .put()
                .uri(BEER_PATH_ID, 999)
                .body(Mono.just(beerMapper.beerToBeerDto(BeerRepositoryTest.createTestBeer())), BeerDTO.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(5)
    void testPatchBeer() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .patch()
                .uri(BEER_PATH_ID, savedBeer.getId())
                .body(Mono.just(beerMapper.beerToBeerDto(BeerRepositoryTest.createTestBeer())), BeerDTO.class)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void testPatchBeerNotFound() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .patch()
                .uri(BEER_PATH_ID, 999)
                .body(Mono.just(beerMapper.beerToBeerDto(BeerRepositoryTest.createTestBeer())), BeerDTO.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(6)
    void testDeleteBeer() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .delete()
                .uri(BEER_PATH_ID, savedBeer.getId())
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void testDeleteBeerNotFound() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .delete()
                .uri(BEER_PATH_ID, 999)
                .exchange()
                .expectStatus().isNotFound();
    }

    private List<Beer> createBeers() {
        Beer beer1 = Beer.builder()
                .beerName("Galaxy Cat")
                .beerStyle("Pale Ale")
                .upc("12356")
                .price(new BigDecimal("12.99"))
                .quantityOnHand(122)
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();

        Beer beer2 = Beer.builder()
                .beerName("Crank")
                .beerStyle("Pale Ale")
                .upc("12356222")
                .price(new BigDecimal("11.99"))
                .quantityOnHand(392)
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();

        Beer beer3 = Beer.builder()
                .beerName("Sunshine City")
                .beerStyle("IPA")
                .upc("12356")
                .price(new BigDecimal("13.99"))
                .quantityOnHand(144)
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();

        return List.of(beer1, beer2, beer3);
    }
}