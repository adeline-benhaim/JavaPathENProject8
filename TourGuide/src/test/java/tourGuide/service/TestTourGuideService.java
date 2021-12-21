package tourGuide.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import tourGuide.beans.AttractionBean;
import tourGuide.beans.LocationBean;
import tourGuide.beans.VisitedLocationBean;
import tourGuide.exceptions.AttractionNotFoundException;
import tourGuide.exceptions.UserNotFoundException;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.Dto.NearbyAttractionListByUserDto;
import tourGuide.model.user.User;
import tourGuide.proxies.GpsUtilProxy;
import tourGuide.proxies.RewardCentralProxy;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ExtendWith(MockitoExtension.class)
public class TestTourGuideService {

    @Mock
    GpsUtilProxy gpsUtil;
    @Mock
    RewardCentralProxy rewardCentralProxy;
    @MockBean
    RewardsServiceImpl rewardsService;

    TourGuideServiceImpl tourGuideService;

    @Before
    public void init() {
        InternalTestHelper.setInternalUserNumber(0);
        tourGuideService = new TourGuideServiceImpl(gpsUtil, rewardsService);
        rewardsService = new RewardsServiceImpl(gpsUtil, rewardCentralProxy);
    }

    @Test
    @DisplayName("Get user location without VisitedLocations history")
    public void getUserLocationWithoutVisitedLocation() throws ExecutionException, InterruptedException {

        //GIVEN
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        tourGuideService.internalUserMap.put("jon", user);
        VisitedLocationBean visitedLocationBean = new VisitedLocationBean(user.getUserId(), new LocationBean(456D, 787D), new Date());
        when(gpsUtil.getUserLocation(user.getUserId())).thenReturn(visitedLocationBean);

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
        tourGuideService.internalUserMap.put("jon", user);
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
        tourGuideService.internalUserMap.clear();
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
        tourGuideService.internalUserMap.put("jon", user);
        doReturn(new VisitedLocationBean(user.getUserId(), new LocationBean(33.817595D, -117.922008D), new Date())).when(gpsUtil).getUserLocation(user.getUserId());

        //WHEN
        VisitedLocationBean visitedLocation = tourGuideService.trackUserLocation(user).get();
        tourGuideService.tracker.stopTracking();

        //THEN
        assertEquals(user.getUserId(), visitedLocation.userId);
    }

    @Test
    @DisplayName("Get nearby attractions")
    public void getNearbyAttractions() {

        //GIVEN
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        List<AttractionBean> attractions = new ArrayList();
        attractions.add(new AttractionBean("Disneyland", "Anaheim", "CA", 33.817595D, -117.922008D));
        attractions.add(new AttractionBean("Jackson Hole", "Jackson Hole", "WY", 43.582767D, -110.821999D));
        attractions.add(new AttractionBean("Mojave National Preserve", "Kelso", "CA", 35.141689D, -115.510399D));
        attractions.add(new AttractionBean("Joshua Tree National Park", "Joshua Tree National Park", "CA", 33.881866D, -115.90065D));
        attractions.add(new AttractionBean("Buffalo National River", "St Joe", "AR", 35.985512D, -92.757652D));
        attractions.add(new AttractionBean("Hot Springs National Park", "Hot Springs", "AR", 34.52153D, -93.042267D));
        attractions.add(new AttractionBean("Kartchner Caverns State Park", "Benson", "AZ", 31.837551D, -110.347382D));

        VisitedLocationBean visitedLocation = new VisitedLocationBean(user.getUserId(), new LocationBean(33.817595D, -117.922008D), new Date());
        when(gpsUtil.getAttractions()).thenReturn(attractions);

        //WHEN
        List<AttractionBean> attractionBeanList = tourGuideService.getNearByAttractions(visitedLocation);
        tourGuideService.tracker.stopTracking();

        //THEN
        assertEquals(5, attractionBeanList.size());
    }

    @Test
    @DisplayName("Get the closest five tourist attractions to the user sorted in ascending order with user location information")
    public void getNearbyAttractionListByUserDto() {

        //GIVEN
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        tourGuideService.internalUserMap.put("jon", user);
        List<AttractionBean> attractionBeans = new ArrayList();
        attractionBeans.add(new AttractionBean("Disneyland", "Anaheim", "CA", 33.817595D, -117.922008D));
        attractionBeans.add(new AttractionBean("Jackson Hole", "Jackson Hole", "WY", 43.582767D, -110.821999D));
        attractionBeans.add(new AttractionBean("Mojave National Preserve", "Kelso", "CA", 35.141689D, -115.510399D));
        attractionBeans.add(new AttractionBean("Joshua Tree National Park", "Joshua Tree National Park", "CA", 33.881866D, -115.90065D));
        attractionBeans.add(new AttractionBean("Buffalo National River", "St Joe", "AR", 35.985512D, -92.757652D));

        VisitedLocationBean visitedLocationBean = new VisitedLocationBean(user.getUserId(), new LocationBean(33.817595D, -117.922008D), new Date());
        user.addToVisitedLocations(visitedLocationBean);
        doReturn(attractionBeans).when(gpsUtil).getAttractions();
        doReturn(500).when(rewardCentralProxy).getRewards(UUID.randomUUID(), user.getUserId());

        //WHEN
        NearbyAttractionListByUserDto attractions = tourGuideService.nearbyAttractionListByUserDto(visitedLocationBean);
        tourGuideService.tracker.stopTracking();

        //THEN
        assertEquals(5, attractions.getNearbyAttractionsDto().size());
        assertEquals("Latitude : " + visitedLocationBean.getLocationBean().getLatitude() + ", Longitude : " + visitedLocationBean.getLocationBean().getLongitude(), attractions.getUserLocation());
    }

    @Test
    @DisplayName("Get all current locations")
    public void getAllCurrentLocations() {

        //GIVEN
        tourGuideService.internalUserMap.clear();
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

    @Test
    @DisplayName("Get attraction by name")
    public void getAttraction() {

        //GIVEN
        AttractionBean attractionBean1 = new AttractionBean("attraction1", "city", "state", 485.0, 8596.0);
        AttractionBean attractionBean2 = new AttractionBean("attraction2", "city", "state", 45.0, 96.0);
        AttractionBean attractionBean3 = new AttractionBean("attraction3", "city", "state", -56.0, 86.0);
        List<AttractionBean> attractionBeanList = new ArrayList<>();
        attractionBeanList.add(attractionBean1);
        attractionBeanList.add(attractionBean2);
        attractionBeanList.add(attractionBean3);
        when(gpsUtil.getAttractions()).thenReturn(attractionBeanList);

        //WHEN
        AttractionBean attractionBean = tourGuideService.getAttraction("ATTRACTION1");

        //THEN
        assertEquals(attractionBean, attractionBean1);
    }

    @Test
    @DisplayName("Get attraction by unknown name")
    public void getAttractionWithUnknownName() {

        //GIVEN
        AttractionBean attractionBean1 = new AttractionBean("attraction1", "city", "state", 485.0, 8596.0);
        AttractionBean attractionBean2 = new AttractionBean("attraction2", "city", "state", 45.0, 96.0);
        AttractionBean attractionBean3 = new AttractionBean("attraction3", "city", "state", -56.0, 86.0);
        List<AttractionBean> attractionBeanList = new ArrayList<>();
        attractionBeanList.add(attractionBean1);
        attractionBeanList.add(attractionBean2);
        attractionBeanList.add(attractionBean3);
        when(gpsUtil.getAttractions()).thenReturn(attractionBeanList);

        //THEN
        assertThrows(AttractionNotFoundException.class, () -> tourGuideService.getAttraction("unknown"));
    }
}
