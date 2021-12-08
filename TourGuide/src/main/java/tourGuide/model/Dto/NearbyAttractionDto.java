package tourGuide.model.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NearbyAttractionDto {

    private String attractionNameDto;
    private String attractionLocation;
    private Double distanceDto;
    private int rewardPoints;

    public NearbyAttractionDto(String attractionName, String locationAttraction, Double distance, int rewardPoints) {
        this.attractionNameDto = attractionName;
        this.attractionLocation = locationAttraction;
        this.distanceDto = distance;
        this.rewardPoints = rewardPoints;
    }
}
