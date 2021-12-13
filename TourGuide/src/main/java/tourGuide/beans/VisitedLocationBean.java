package tourGuide.beans;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
public class VisitedLocationBean {
    public final UUID userId;
    public final LocationBean locationBean;
    public final Date timeVisited;

    public VisitedLocationBean(UUID userId, LocationBean location, Date timeVisited) {
        this.userId = userId;
        this.locationBean = location;
        this.timeVisited = timeVisited;
    }
}
