package guru.springframework.spring6resttemplate.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.web.client.HttpClientErrorException;

import guru.springframework.spring6resttemplate.model.BeerDTO;
import guru.springframework.spring6resttemplate.model.BeerStyle;

@SpringBootTest
public class BeerClientImplTest {
	@Autowired
	BeerClientImpl beerClient;
	
	@Test
	void testDeleteBeer() {
		BeerDTO newdto = BeerDTO.builder()
				.price(new BigDecimal("10.99"))
				.beerName("Mango Bobs 2")
				.beerStyle(BeerStyle.IPA)
				.quantityOnHand(500)
				.upc("123245")
				.build();
		BeerDTO beerdto = beerClient.createBeer(newdto);
		beerClient.deleteBeer(beerdto.getId());
		assertThrows(HttpClientErrorException.class, () -> {
			beerClient.getBeerById(beerdto.getId());
		});
	}
	
	@Test
	void testUpdateBeer() {
		BeerDTO newdto = BeerDTO.builder()
				.price(new BigDecimal("10.99"))
				.beerName("Mango Bobs 2")
				.beerStyle(BeerStyle.IPA)
				.quantityOnHand(500)
				.upc("123245")
				.build();
		
		BeerDTO beerdto = beerClient.createBeer(newdto);
		
		final String newName = "Mango Bobs 3";
		beerdto.setBeerName(newName);
		BeerDTO updatedBeer = beerClient.updateBeer(beerdto);
		
		assertEquals(newName, updatedBeer.getBeerName());
	}
	
	@Test
	void testCreateBeer() {
		BeerDTO newdto = BeerDTO.builder()
				.price(new BigDecimal("10.99"))
				.beerName("Mango Bobs")
				.beerStyle(BeerStyle.IPA)
				.quantityOnHand(500)
				.upc("123245")
				.build();
		BeerDTO saved = beerClient.createBeer(newdto);
		assertNotNull(saved);
	}
	
	@Test
	void getBeerById() {
		Page<BeerDTO> beerdtos = beerClient.listBeers();
		BeerDTO dto = beerdtos.getContent().get(0);
		BeerDTO byId = beerClient.getBeerById(dto.getId());
		
		assertNotNull(byId);
	}
	
	@Test
	void listBeersNoBeerName() {
		beerClient.listBeers();
	}
	
	@Test
	void listBeers() {
		beerClient.listBeers("ALE", null, null, null, null);
	}
}
