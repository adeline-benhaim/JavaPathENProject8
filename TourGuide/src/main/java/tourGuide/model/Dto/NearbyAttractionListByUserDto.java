package tourGuide.model.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class NearbyAttractionListByUserDto {

    private String userLocation;
    private List<NearbyAttractionDto> nearbyAttractionsDto;

    public NearbyAttractionListByUserDto(String locationBean, List<NearbyAttractionDto> nearbyAttractions) {
        this.userLocation = locationBean;
        this.nearbyAttractionsDto = nearbyAttractions;
    }
}
