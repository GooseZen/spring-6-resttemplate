package guru.springframework.spring6resttemplate.client;

import java.util.UUID;

import org.springframework.data.domain.Page;

import guru.springframework.spring6resttemplate.model.BeerDTO;
import guru.springframework.spring6resttemplate.model.BeerStyle;

public interface BeerClient {
	Page<BeerDTO> listBeers();
	Page<BeerDTO> listBeers(String beerName, BeerStyle beerStyle, Boolean showInventory, Integer pageNumber, Integer pageSize);
	public BeerDTO getBeerById(UUID beerId);
	public BeerDTO createBeer(BeerDTO beerdto);
	public BeerDTO updateBeer(BeerDTO beerdto);
	void deleteBeer(UUID beerId);
}
