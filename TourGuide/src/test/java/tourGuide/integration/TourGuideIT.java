package tourGuide.integration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import tourGuide.beans.AttractionBean;
import tourGuide.beans.LocationBean;
import tourGuide.beans.VisitedLocationBean;
import tourGuide.exceptions.UserNotFoundException;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.user.User;
import tourGuide.proxies.GpsUtilProxy;
import tourGuide.proxies.RewardCentralProxy;
import tourGuide.service.RewardsServiceImpl;
import tourGuide.service.TourGuideServiceImpl;
import tourGuide.service.TripPricerServiceImpl;
import tripPricer.Provider;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TourGuideIT {

    @Autowired
    private GpsUtilProxy gpsUtilProxy;
    @Autowired
    private RewardCentralProxy rewardCentralProxy;
    @Autowired
    private RewardsServiceImpl rewardsService;
    @Autowired
    private TourGuideServiceImpl tourGuideService;
    @Autowired
    private TripPricerServiceImpl tripPricerService;

    ExecutorService executorService;

    @Before
    public void init() {
        InternalTestHelper.setInternalUserNumber(0);
        tourGuideService = new TourGuideServiceImpl(gpsUtilProxy, rewardsService);
        rewardsService = new RewardsServiceImpl(gpsUtilProxy, rewardCentralProxy);
    }

    @After
    public void closeTracker() {
        tourGuideService.tracker.stopTracking();
    }

    /**
     * Try to get location of unknown user thrown a user not found exception
     */
    @Test
    @DisplayName("Get user location with unknown user")
    public void getUserLocationWithUnknownUser() {

        //GIVEN
        User user = new User(null, null, null, null);

        //THEN
        assertThrows(UserNotFoundException.class, () -> tourGuideService.getUserLocation(user));
    }

    /**
     * Get the location of a user whose history is empty will start to track user through gpsUtilProxy
     * @throws ExecutionException can be thrown when attempting to retrieve the result of trackUserLocation that aborted by throwing an exception
     * @throws InterruptedException can be thrown when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted, either before or during the activity
     */
    @Test
    @DisplayName("Get user location without VisitedLocations history")
    public void getUserLocationWithoutVisitedLocationHistory() throws ExecutionException, InterruptedException {

        //GIVEN
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        tourGuideService.addUser(user);

        //WHEN
        VisitedLocationBean visitedLocation = tourGuideService.getUserLocation(user);

        //THEN
        assertEquals(visitedLocation.getUserId(), user.getUserId());
    }

    /**
     * Get the location of a user whose history isn't empty will retrieve the most recent position of the user
     * @throws ExecutionException can be thrown when attempting to retrieve the result of trackUserLocation that aborted by throwing an exception
     * @throws InterruptedException can be thrown when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted, either before or during the activity
     */
    @Test
    @DisplayName("Get user location with VisitedLocations history")
    public void getUserLocationWithVisitedLocationHistory() throws ExecutionException, InterruptedException {

        //GIVEN
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        tourGuideService.addUser(user);
        VisitedLocationBean visitedLocationBean = VisitedLocationBean.builder().userId(user.getUserId()).locationBean(new LocationBean(455D, 950D)).build();
        VisitedLocationBean lastVisitedLocationBean = VisitedLocationBean.builder().userId(user.getUserId()).locationBean(new LocationBean(400D, 900D)).build();
        user.addToVisitedLocations(visitedLocationBean);
        user.addToVisitedLocations(lastVisitedLocationBean);

        //WHEN
        VisitedLocationBean getUserLocation = tourGuideService.getUserLocation(user);

        //THEN
        assertEquals(getUserLocation, user.getLastVisitedLocation());
        assertEquals(lastVisitedLocationBean.getUserId(), user.getUserId());
    }

    @Test
    @DisplayName("Track user location")
    public void trackUserLocation() throws ExecutionException, InterruptedException {

        //GIVEN
        User user1 = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        tourGuideService.addUser(user1);
        user1.clearVisitedLocations();
        user1.addToVisitedLocations(new VisitedLocationBean(user1.getUserId(), gpsUtilProxy.getAttractions().get(1), new Date()));
        int userRewards = user1.getUserRewards().size();

        //WHEN
        CompletableFuture<VisitedLocationBean> visitedLocation = tourGuideService.trackUserLocation(user1);
        TimeUnit.MILLISECONDS.sleep(5000);
        int userRewardsAfterTrack = user1.getUserRewards().size();

        //THEN
        assertEquals(visitedLocation.get().getUserId(), user1.getUserId());
        assertTrue(userRewards < userRewardsAfterTrack);
    }

    @Test
    public void addUser() {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        User retrivedUser = tourGuideService.getUser(user.getUserName());
        User retrivedUser2 = tourGuideService.getUser(user2.getUserName());

        tourGuideService.tracker.stopTracking();

        assertEquals(user, retrivedUser);
        assertEquals(user2, retrivedUser2);
    }

    @Test
    public void getAllUsers() {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        List<User> allUsers = tourGuideService.getAllUsers();

        tourGuideService.tracker.stopTracking();

        assertTrue(allUsers.contains(user));
        assertTrue(allUsers.contains(user2));
    }

    @Test
    public void trackUser() throws ExecutionException, InterruptedException {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        CompletableFuture<VisitedLocationBean> visitedLocation = tourGuideService.trackUserLocation(user);

        tourGuideService.tracker.stopTracking();

        assertEquals(user.getUserId(), visitedLocation.get().getUserId());
    }

    @Test
    public void getNearbyAttractions() throws ExecutionException, InterruptedException {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        CompletableFuture<VisitedLocationBean> visitedLocation = tourGuideService.trackUserLocation(user);

        List<AttractionBean> attractions = tourGuideService.getNearByAttractions(visitedLocation.get());

        tourGuideService.tracker.stopTracking();

        assertEquals(5, attractions.size());
    }

    @Test
    public void getTripDeals() {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        AttractionBean attractionBean = new AttractionBean("Disneyland", "Anaheim", "CA", 33.817595D, -117.922008D);

        List<Provider> providers = tripPricerService.getTripDeals(user, attractionBean);

        tourGuideService.tracker.stopTracking();

        assertEquals(5, providers.size());
    }
}
