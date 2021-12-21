package tourGuide.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tourGuide.beans.LocationBean;
import tourGuide.beans.VisitedLocationBean;
import tourGuide.model.Dto.NearbyAttractionListByUserDto;
import tourGuide.model.user.User;
import tourGuide.model.user.UserPreferences;
import tourGuide.model.user.UserReward;
import tourGuide.service.RewardsServiceImpl;
import tourGuide.service.TourGuideServiceImpl;
import tourGuide.service.TripPricerService;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestTourGuideController {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    private TourGuideServiceImpl tourGuideService;
    @Autowired
    private RewardsServiceImpl rewardsService;
    @Autowired
    private TripPricerService tripPricerService;
    @Autowired
    TourGuideController tourGuideController;

    @Test
    @DisplayName("GET request (/) must return an HTTP 200 response")
    public void testGetIndex() throws Exception {

        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        assertEquals("Greetings from TourGuide!", tourGuideController.index());
    }

    @Test
    @DisplayName("GET request (/getLocation) with existing user must return an HTTP 200 response")
    public void testGetLocationWithExistingUser() throws Exception {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        tourGuideService.internalUserMap.put("jon", user);
        VisitedLocationBean visitedLocationBean = tourGuideService.trackUserLocation(user).get();

        mockMvc.perform(get("/getLocation").param("userName", "jon"))
                .andExpect(status().isOk());

        assertEquals(visitedLocationBean.locationBean, user.getLastVisitedLocation().locationBean);
    }

    @Test
    @DisplayName("GET request (/getLocation) with unknown user must return an HTTP 404 response")
    public void testGetLocationWithUnknownUser() throws Exception {

        mockMvc.perform(get("/getLocation").param("userName", "unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET request (/getLocation) with empty param must return an HTTP 400 response")
    public void testGetLocationWithEmptyParam() throws Exception {

        mockMvc.perform(get("/getLocation"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET request (/getNearbyAttractions) with existing user must return the closest five tourist attractions to the user sorted in ascending order with all user and attractions information and an HTTP 200 response")
    public void testGetNearbyAttractionsWithExistingUser() throws Exception {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        tourGuideService.internalUserMap.put("jon", user);
        tourGuideService.trackUserLocation(user).get();
        VisitedLocationBean visitedLocationBean = user.getLastVisitedLocation();
        NearbyAttractionListByUserDto nearByAttractionListByUserDto = tourGuideService.nearbyAttractionListByUserDto(visitedLocationBean);

        mockMvc.perform(get("/getNearbyAttractions").param("userName", "jon"))
                .andExpect(MockMvcResultMatchers.jsonPath("userLocation").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("nearbyAttractionsDto").isArray())
                .andExpect(jsonPath("$[5].nearbyAttractionsDto").doesNotExist())
                .andExpect(status().isOk())
                .andDo(print());

        assertEquals(5, nearByAttractionListByUserDto.getNearbyAttractionsDto().size());
        assertTrue(nearByAttractionListByUserDto.getNearbyAttractionsDto().get(0).getDistanceDto() < nearByAttractionListByUserDto.getNearbyAttractionsDto().get(1).getDistanceDto());
    }

    @Test
    @DisplayName("GET request (/getNearbyAttractions) with unknown user must return an HTTP 404 response")
    public void testGetNearbyAttractionsWithUnknownUser() throws Exception {

        mockMvc.perform(get("/getNearbyAttractions").param("userName", "unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET request (/getNearbyAttractions) with empty param must return an HTTP 400 response")
    public void testGetNearbyAttractionsWithEmptyParam() throws Exception {

        mockMvc.perform(get("/getNearbyAttractions"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET request (/getRewards) with existing user must return an HTTP 200 response")
    public void testGetRewardWithExistingUser() throws Exception {

        User user = tourGuideService.getUser("userCustom");
        user.clearVisitedLocations();
        user.addToVisitedLocations(new VisitedLocationBean(user.getUserId(), new LocationBean(33.817595D, -117.922008D), new Date()));
        VisitedLocationBean visitedLocationBean = user.getLastVisitedLocation();
        List<UserReward> userRewards = rewardsService.getUserRewards(user);

        mockMvc.perform(get("/getRewards").param("userName", "userCustom"))
                .andExpect(jsonPath("$[0].visitedLocation.userId").value(user.getUserId().toString()))
                .andExpect(jsonPath("$[0].visitedLocation.locationBean.longitude").value(visitedLocationBean.getLocationBean().getLongitude()))
                .andExpect(jsonPath("$[0].visitedLocation.locationBean.latitude").value(visitedLocationBean.getLocationBean().getLatitude()))
                .andExpect(jsonPath("$[0].attraction").isNotEmpty())
                .andExpect(jsonPath("$[0].rewardPoints").isNotEmpty())
                .andExpect(status().isOk());

        assertTrue(rewardsService.nearAttraction(visitedLocationBean, (userRewards.get(0).attraction)));
    }

    @Test
    @DisplayName("GET request (/getRewards) with unknown user must return an HTTP 404 response")
    public void testGetRewardsWithUnknownUser() throws Exception {

        mockMvc.perform(get("/getRewards").param("userName", "unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET request (/getAllCurrentLocations) must return a map with for each user key = userId and value = {latitude, longitude} and an HTTP 200 response")
    public void testGetAllCurrentLocations() throws Exception {

        Map<String, LocationBean> allLocations = tourGuideService.getAllCurrentLocations();
        LocationBean lastLocation = tourGuideService.getUser("userCustom").getLastVisitedLocation().getLocationBean();

        mockMvc.perform(get("/getAllCurrentLocations"))
                .andExpect(status().isOk());

        assertEquals(tourGuideService.internalUserMap.size(), allLocations.size());
        assertTrue(allLocations.containsValue(lastLocation));
    }

    @Test
    @DisplayName("GET request (/getTripDeals) with existing user must return an HTTP 200 response")
    public void testGetTripDealsWithExistingUser() throws Exception {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        tourGuideService.internalUserMap.put("jon", user);

        mockMvc.perform(get("/getTripDeals")
                        .param("userName", "jon")
                        .param("attractionName", "Disneyland"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET request (/getTripDeals) with unknown user must return an HTTP 404 response")
    public void testGetTripDealsWithUnknownUser() throws Exception {

        mockMvc.perform(get("/getTripDeals")
                        .param("userName", "unknown")
                        .param("attractionName", "Disneyland"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET request (/getTripDeals) with unknown attraction name must return an HTTP 404 response")
    public void testGetTripDealsWithUnknownAttraction() throws Exception {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        tourGuideService.internalUserMap.put("jon", user);

        mockMvc.perform(get("/getTripDeals")
                        .param("userName", "jon")
                        .param("attractionName", "unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET request (/getUser) with existing user must return an HTTP 200 response")
    public void testGetUserWithExistingUser() throws Exception {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        tourGuideService.internalUserMap.put("jon", user);
        tourGuideService.trackUserLocation(user).get();

        mockMvc.perform(get("/getUser").param("userName", "jon"))
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("GET request (/getUser) with unknown user must return an HTTP 404 response")
    public void testGetUserWithUnknownUser() throws Exception {

        mockMvc.perform(get("/getUser").param("userName", "unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT request (/updateUserPreferences) must update user preferences and return an HTTP 200 response")
    public void updateUserPreferencesWithExistingUserTest() throws Exception {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        tourGuideService.internalUserMap.put("jon", user);
        UserPreferences userPreferencesUpdated = UserPreferences.builder().tripDuration(15).numberOfAdults(2).numberOfChildren(3).build();

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/updateUserPreferences")
                        .param("userName", "jon")
                        .content(asJsonString(userPreferencesUpdated))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("tripDuration", is(15)))
                .andExpect(jsonPath("numberOfAdults", is(2)))
                .andExpect(jsonPath("numberOfChildren", is(3)));
    }

    @Test
    @DisplayName("PUT request (/updateUserPreferences) with unknown user must return an HTTP 404 response")
    public void updateUserPreferencesWithUnknownUserTest() throws Exception {

        UserPreferences userPreferencesUpdated = UserPreferences.builder().tripDuration(15).numberOfAdults(2).numberOfChildren(3).build();

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/updateUserPreferences")
                        .param("userName", "unknown")
                        .content(asJsonString(userPreferencesUpdated))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT request (/updateUserPreferences) with empty body must return an HTTP 400 response")
    public void updateUserPreferencesWithExistingUserAndEmptyBodyTest() throws Exception {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        tourGuideService.internalUserMap.put("jon", user);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/updateUserPreferences")
                        .param("userName", "jon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
