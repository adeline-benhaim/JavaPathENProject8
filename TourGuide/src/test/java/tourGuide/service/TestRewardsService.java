package tourGuide.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit4.SpringRunner;
import tourGuide.beans.AttractionBean;
import tourGuide.beans.LocationBean;
import tourGuide.beans.VisitedLocationBean;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.user.User;
import tourGuide.model.user.UserReward;
import tourGuide.proxies.GpsUtilProxy;
import tourGuide.proxies.RewardCentralProxy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;

@RunWith(SpringRunner.class)
@ExtendWith(MockitoExtension.class)
public class TestRewardsService {

    @Mock
    GpsUtilProxy gpsUtil;
    @Mock
    RewardCentralProxy rewardCentralProxy;

    TourGuideServiceImpl tourGuideService;
    RewardsServiceImpl rewardsService;

    @Before
    public void init() {
        InternalTestHelper.setInternalUserNumber(0);
        tourGuideService = new TourGuideServiceImpl(gpsUtil, rewardsService);
        rewardsService = new RewardsServiceImpl(gpsUtil, rewardCentralProxy);
    }

    @Test
    @DisplayName("Get reward for attraction near")
    public void getRewardForNearAttraction() throws InterruptedException {

        //GIVEN
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        List<AttractionBean> attractionBeanList = new ArrayList<>();
        AttractionBean attractionBean = new AttractionBean("name1", "city", "state", 33.917595D, -117.922008D);
        attractionBeanList.add(attractionBean);
        VisitedLocationBean visitedLocationBean = new VisitedLocationBean(user.getUserId(), attractionBean, new Date());
        user.addToVisitedLocations(visitedLocationBean);
        tourGuideService.internalUserMap.put("jon", user);
        doReturn(attractionBeanList).when(gpsUtil).getAttractions();

        //WHEN
        rewardsService.calculateRewards(user);
        Thread.sleep(1000);
        tourGuideService.tracker.stopTracking();

        //THEN
        assertEquals(gpsUtil.getAttractions().size(), user.getUserRewards().size());
    }

	@Test
    @DisplayName("Get user reward")
	public void userGetRewards() {

        //GIVEN
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        tourGuideService.internalUserMap.put("jon", user);
        UserReward userReward = new UserReward(new VisitedLocationBean(user.getUserId(), null, null), new AttractionBean(null, null, null, 33.817595D, -117.922008D));
        user.addUserReward(userReward);

        //WHEN
        List<UserReward> userRewards = rewardsService.getUserRewards(user);
        tourGuideService.tracker.stopTracking();

        //THEN
        assertEquals(userReward.visitedLocation.userId, user.getUserId());
        assertEquals(1, userRewards.size());
	}

    @Test
    @DisplayName("Check if near attraction with near attraction")
    public void nearAttraction() {

        //GIVEN
        VisitedLocationBean visitedLocationBean = VisitedLocationBean.builder().locationBean(new LocationBean(1.0, 2.0)).build();
        AttractionBean attractionBean = new AttractionBean("name", "city", "state", 1.0, 2.0);

        //WHEN
        boolean nearAttraction = rewardsService.nearAttraction(visitedLocationBean, attractionBean);
        tourGuideService.tracker.stopTracking();

        //THEN
        assertTrue(nearAttraction);
    }

    @Test
    @DisplayName("Check if near attraction with not near attraction")
    public void notNearAttraction() {

        //GIVEN
        VisitedLocationBean visitedLocationBean = VisitedLocationBean.builder().locationBean(new LocationBean(111.0, -222.0)).build();
        AttractionBean attractionBean = new AttractionBean("name", "city", "state", 1.0, 2.0);

        //WHEN
        boolean nearAttraction = rewardsService.nearAttraction(visitedLocationBean, attractionBean);
        tourGuideService.tracker.stopTracking();

        //THEN
        assertFalse(nearAttraction);
    }

    @Test
    @DisplayName("Get reward points")
    public void getRewardPoints() {

        //GIVEN
        AttractionBean attractionBean = new AttractionBean("name", "city", "state", 1.0, 2.0);
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        //WHEN
        rewardsService.getRewardPoints(attractionBean, user);
        tourGuideService.tracker.stopTracking();

        //THEN
        Mockito.verify(rewardCentralProxy, times(1)).getRewards(attractionBean.getAttractionId(), user.getUserId());
    }

}
