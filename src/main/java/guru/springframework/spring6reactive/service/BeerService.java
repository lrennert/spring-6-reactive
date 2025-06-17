package guru.springframework.spring6reactive.service;

import guru.springframework.spring6reactive.model.BeerDTO;
import reactor.core.publisher.Flux;

public interface BeerService {

    Flux<BeerDTO> listBeers();
}
