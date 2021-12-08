package tourGuide.beans;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationBean {

    public final double longitude;
    public final double latitude;

    public LocationBean(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
