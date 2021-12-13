package tourGuide;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.user.User;
import tourGuide.model.user.UserPreferences;
import tourGuide.proxies.GpsUtilProxy;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideServiceImpl;
import tourGuide.service.TripPricerService;
import tripPricer.Provider;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestTripPricerService {

    @Autowired
    GpsUtilProxy gpsUtilProxy;
    @Autowired
    RewardsService rewardsService;
    @Autowired
    TourGuideServiceImpl tourGuideService;
    @Autowired
    TripPricerService tripPricerService;

    @Before
    public void init() {
        InternalTestHelper.setInternalUserNumber(0);
        tourGuideService = new TourGuideServiceImpl(gpsUtilProxy, rewardsService);
    }

    @Test
    @DisplayName("Get trip deal")
    public void getTripDeals() {

        //GIVEN
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        TourGuideServiceImpl.internalUserMap.put("jon",user);

        //WHEN
        List<Provider> providers = tripPricerService.getTripDeals(user);
        tourGuideService.tracker.stopTracking();

        //THEN
        assertEquals(5, providers.size());

//		assertEquals(10, providers.size());
    }

    @Test
    @DisplayName("Update the user's travel preferences")
    public void updateUserPreferences() {

        //GIVEN
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        TourGuideServiceImpl.internalUserMap.put("jon",user);
        UserPreferences userPreferences = UserPreferences.builder().numberOfAdults(2).numberOfChildren(5).tripDuration(7).build();

        //WHEN
        tripPricerService.updateUserPreferences(user, userPreferences);

        //THEN
        assertEquals(7, user.getUserPreferences().getTripDuration());
    }
}
