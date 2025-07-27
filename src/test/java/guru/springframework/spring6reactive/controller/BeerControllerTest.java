package guru.springframework.spring6reactive.controller;

import guru.springframework.spring6reactive.domain.Beer;
import guru.springframework.spring6reactive.mapper.BeerMapper;
import guru.springframework.spring6reactive.model.BeerDTO;
import guru.springframework.spring6reactive.repository.BeerRepositoryTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static guru.springframework.spring6reactive.controller.BeerController.BEER_PATH;
import static guru.springframework.spring6reactive.controller.BeerController.BEER_PATH_ID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@AutoConfigureWebTestClient
public class BeerControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    BeerMapper beerMapper;

    @Test
    @Order(1)
    void testListBeers() {
        webTestClient.get()
                .uri(BEER_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-type", "application/json")
                .expectBody().jsonPath("$.size()").isEqualTo(3);
    }

    @Test
    @Order(2)
    void testGetBeerById() {
        webTestClient.get()
                .uri(BEER_PATH_ID, 1)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-type", "application/json")
                .expectBody(BeerDTO.class);
    }

    @Test
    void testGetBeerByIdNotFound() {
        webTestClient.get()
                .uri(BEER_PATH_ID, 999)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(3)
    void testCreateBeer() {
        webTestClient.post()
                .uri(BEER_PATH)
                .body(Mono.just(beerMapper.beerToBeerDto(BeerRepositoryTest.createTestBeer())), BeerDTO.class)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location("http://localhost:8080/api/v2/beer/4");
    }

    @Test
    void testCreateBeerBadRequest() {
        Beer testBeer = BeerRepositoryTest.createTestBeer();
        testBeer.setBeerName("");

        webTestClient.post()
                .uri(BEER_PATH)
                .body(Mono.just(beerMapper.beerToBeerDto(testBeer)), BeerDTO.class)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Order(4)
    void testUpdateBeer() {
        webTestClient.put()
                .uri(BEER_PATH_ID, 1)
                .body(Mono.just(beerMapper.beerToBeerDto(BeerRepositoryTest.createTestBeer())), BeerDTO.class)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void testUpdateBeerBadRequest() {
        BeerDTO testBeer = beerMapper.beerToBeerDto(BeerRepositoryTest.createTestBeer());
        testBeer.setBeerStyle("");

        webTestClient.put()
                .uri(BEER_PATH_ID, 1)
                .body(Mono.just(testBeer), BeerDTO.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testUpdateBeerNotFound() {
        webTestClient.put()
                .uri(BEER_PATH_ID, 999)
                .body(Mono.just(beerMapper.beerToBeerDto(BeerRepositoryTest.createTestBeer())), BeerDTO.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(5)
    void testPatchBeer() {
        webTestClient.patch()
                .uri(BEER_PATH_ID, 1)
                .body(Mono.just(beerMapper.beerToBeerDto(BeerRepositoryTest.createTestBeer())), BeerDTO.class)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void testPatchBeerNotFound() {
        webTestClient.patch()
                .uri(BEER_PATH_ID, 999)
                .body(Mono.just(beerMapper.beerToBeerDto(BeerRepositoryTest.createTestBeer())), BeerDTO.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(6)
    void testDeleteBeer() {
        webTestClient.delete()
                .uri(BEER_PATH_ID, 1)
                .exchange()
                .expectStatus().isNoContent();
    }
}