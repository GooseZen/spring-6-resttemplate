package guru.springframework.spring6resttemplate.model;

import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("hiding")
@JsonIgnoreProperties(ignoreUnknown = true, value = "pageable")
public class BeerDTOPageImpl<BeerDTO> extends PageImpl<guru.springframework.spring6resttemplate.model.BeerDTO> {
	private static final long serialVersionUID = 1073736516092057325L;

	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	public BeerDTOPageImpl(@JsonProperty("content") List<guru.springframework.spring6resttemplate.model.BeerDTO> list, @JsonProperty("number") int page, 
			@JsonProperty("size") int size, @JsonProperty("totalElements") long total) {
		super (list, PageRequest.of(page, size), total);
	}

	public BeerDTOPageImpl(List<guru.springframework.spring6resttemplate.model.BeerDTO> content, Pageable pageable, long total) {
		super(content, pageable, total);
	}
	
	public BeerDTOPageImpl(List<guru.springframework.spring6resttemplate.model.BeerDTO> content) {
		super(content);
	}
}
