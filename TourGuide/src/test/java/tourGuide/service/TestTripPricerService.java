package tourGuide.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import tourGuide.beans.AttractionBean;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.user.User;
import tourGuide.model.user.UserPreferences;
import tourGuide.proxies.GpsUtilProxy;
import tripPricer.Provider;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class TestTripPricerService {

    @Mock
    GpsUtilProxy gpsUtilProxy;
    @Mock
    RewardsServiceImpl rewardsServiceImpl;

    TourGuideServiceImpl tourGuideService;
    TripPricerService tripPricerService;

    @Before
    public void init() {
        InternalTestHelper.setInternalUserNumber(0);
        tripPricerService = new TripPricerServiceImpl();
        tourGuideService = new TourGuideServiceImpl(gpsUtilProxy, rewardsServiceImpl);
    }

    @Test
    @DisplayName("Get trip deal")
    public void getTripDeals() {

        //GIVEN
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        AttractionBean attractionBean = new AttractionBean("Disneyland", "Anaheim", "CA", 33.817595D, -117.922008D);
        tourGuideService.internalUserMap.put("jon",user);

        //WHEN
        List<Provider> providers = tripPricerService.getTripDeals(user, attractionBean);
        tourGuideService.tracker.stopTracking();

        //THEN
        assertEquals(5, providers.size());
    }

    @Test
    @DisplayName("Update the user's travel preferences")
    public void updateUserPreferences() {

        //GIVEN
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        tourGuideService.internalUserMap.put("jon",user);
        UserPreferences userPreferences = UserPreferences.builder().numberOfAdults(2).numberOfChildren(5).tripDuration(7).build();

        //WHEN
        tripPricerService.updateUserPreferences(user, userPreferences);

        //THEN
        assertEquals(7, user.getUserPreferences().getTripDuration());
    }
}
