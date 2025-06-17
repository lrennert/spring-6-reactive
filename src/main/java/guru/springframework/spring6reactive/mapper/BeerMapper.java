package guru.springframework.spring6reactive.mapper;

import guru.springframework.spring6reactive.domain.Beer;
import guru.springframework.spring6reactive.model.BeerDTO;
import org.mapstruct.Mapper;

@Mapper
public interface BeerMapper {

    Beer beerDtoToBeer(BeerDTO dto);
    BeerDTO beerToBeerDto(Beer beer);
}