package tourGuide;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import tourGuide.beans.AttractionBean;
import tourGuide.beans.LocationBean;
import tourGuide.beans.VisitedLocationBean;
import tourGuide.exceptions.UserNotFoundException;
import tourGuide.model.Dto.NearbyAttractionListByUserDto;
import tourGuide.model.user.User;
import tourGuide.model.user.UserReward;
import tourGuide.proxies.GpsUtilProxy;
import tourGuide.proxies.RewardCentralProxy;
import tourGuide.service.TourGuideServiceImpl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static tourGuide.service.TourGuideServiceImpl.internalUserMap;

@SpringBootTest
@RunWith(SpringRunner.class)
@ExtendWith(MockitoExtension.class)
public class TestTourGuideService {

    @Autowired
    TourGuideServiceImpl tourGuideService;
    @Mock
    GpsUtilProxy gpsUtil;
    @Mock
    RewardCentralProxy rewardCentralProxy;

    @Test
    @DisplayName("Get user location without VisitedLocations history")
    public void getUserLocationWithoutVisitedLocation() throws ExecutionException, InterruptedException {

        //GIVEN
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        internalUserMap.put("jon",user);
        Mockito.when(gpsUtil.getUserLocation(user.getUserId())).thenReturn(new VisitedLocationBean(user.getUserId(), null, new Date()));

        //WHEN
        VisitedLocationBean visitedLocation = tourGuideService.getUserLocation(user);
        tourGuideService.tracker.stopTracking();

        //THEN
        assertEquals(visitedLocation.userId, user.getUserId());
    }

    @Test
    @DisplayName("Get user location with VisitedLocations history")
    public void getUserLocationWithVisitedLocation() throws ExecutionException, InterruptedException {

        //GIVEN
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        internalUserMap.put("jon",user);
        VisitedLocationBean visitedLocationBean = new VisitedLocationBean(user.getUserId(), new LocationBean(33.817595D, -117.922008D), new Date());
        user.addToVisitedLocations(visitedLocationBean);

        //WHEN
        VisitedLocationBean visitedLocation = tourGuideService.getUserLocation(user);
        tourGuideService.tracker.stopTracking();

        //THEN
        assertEquals(visitedLocation.userId, user.getUserId());
    }

    @Test
    @DisplayName("Get user location with an no existing user")
    public void getUserLocationWithNoExistingUser() {

        //GIVEN
        User user = new User(null, null, null, null);

        //THEN
        assertThrows(UserNotFoundException.class, () -> tourGuideService.getUserLocation(user));
    }

    @Test
    @DisplayName("Get user reward with existing user")
    public void getUserRewardsWithExistingUser() {

        //GIVEN
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        internalUserMap.put("jon",user);
        UserReward userReward = new UserReward(new VisitedLocationBean(user.getUserId(),null,null), new AttractionBean(null, null, null, 33.817595D, -117.922008D));
        user.addUserReward(userReward);

        //WHEN
        tourGuideService.getUserRewards(user);

        //THEN
        assertEquals(userReward.visitedLocation.userId,user.getUserId());
    }

    @Test
    @DisplayName("Get user reward with no existing user")
    public void getUserRewardsWithNoExistingUser() {

        //GIVEN
        User user = new User(null, null, null, null);

        //THEN
        assertThrows(UserNotFoundException.class, () -> tourGuideService.getUserRewards(user));
    }

    @Test
    @DisplayName("Add a new user")
    public void addUser() {

        //GIVEN
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");
        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        //WHEN
        User retrivedUser = tourGuideService.getUser(user.getUserName());
        User retrivedUser2 = tourGuideService.getUser(user2.getUserName());
        tourGuideService.tracker.stopTracking();

        //THEN
        assertEquals(user, retrivedUser);
        assertEquals(user2, retrivedUser2);
    }

    @Test
    @DisplayName("Get all users")
    public void getAllUsers() {

        //GIVEN
        internalUserMap.clear();
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");
        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        //WHEN
        List<User> allUsers = tourGuideService.getAllUsers();
        tourGuideService.tracker.stopTracking();

        //THEN
        assertTrue(allUsers.contains(user));
        assertTrue(allUsers.contains(user2));
    }

    @Test
    @DisplayName("Track user location")
    public void trackUser() throws ExecutionException, InterruptedException {

        //GIVEN
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        //WHEN
        VisitedLocationBean visitedLocation = tourGuideService.trackUserLocation(user).get();
        tourGuideService.tracker.stopTracking();

        //THEN
        assertEquals(user.getUserId(), visitedLocation.userId);
    }

    @Test
    @DisplayName("Get nearby attractions")
    public void getNearbyAttractions() throws ExecutionException, InterruptedException {

        //GIVEN
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocationBean visitedLocation = tourGuideService.trackUserLocation(user).get();

        //WHEN
        List<AttractionBean> attractions = tourGuideService.getNearByAttractions(visitedLocation);
        tourGuideService.tracker.stopTracking();

        //THEN
        assertEquals(5, attractions.size());
    }

    @Test
    @DisplayName("Get the closest five tourist attractions to the user sorted in ascending order with user location information")
    public void getNearbyAttractionListByUserDto() throws ExecutionException, InterruptedException {

        //GIVEN
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        internalUserMap.put("jon",user);
        VisitedLocationBean visitedLocation = tourGuideService.trackUserLocation(user).get();
        Mockito.when(rewardCentralProxy.getRewards(UUID.randomUUID(), user.getUserId())).thenReturn(500);

        //WHEN
        NearbyAttractionListByUserDto attractions = tourGuideService.nearbyAttractionListByUserDto(visitedLocation);
        tourGuideService.tracker.stopTracking();

        //THEN
        assertEquals(5, attractions.getNearbyAttractionsDto().size());
        assertEquals("Latitude : " + visitedLocation.getLocationBean().getLatitude() + ", Longitude : " + visitedLocation.getLocationBean().getLongitude(), attractions.getUserLocation());
    }

    @Test
    @DisplayName("Get all current locations")
    public void getAllCurrentLocations() {

        //GIVEN
        internalUserMap.clear();
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");
        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);
        tourGuideService.generateUserLocationHistory(user);
        tourGuideService.generateUserLocationHistory(user2);

        //WHEN
        Map<String, LocationBean> allCurrentLocations = tourGuideService.getAllCurrentLocations();
        tourGuideService.tracker.stopTracking();

        //THEN
        assertEquals(2, allCurrentLocations.size());
        assertTrue(allCurrentLocations.containsKey(user.getUserId().toString()));
        assertTrue(allCurrentLocations.containsValue(user.getLastVisitedLocation().locationBean));
    }
}
