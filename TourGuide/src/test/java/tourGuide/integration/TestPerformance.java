package tourGuide.integration;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import tourGuide.beans.AttractionBean;
import tourGuide.beans.VisitedLocationBean;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.user.User;
import tourGuide.proxies.GpsUtilProxy;
import tourGuide.proxies.RewardCentralProxy;
import tourGuide.service.RewardsServiceImpl;
import tourGuide.service.TourGuideServiceImpl;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestPerformance {

    /*
     * A note on performance improvements:
     *
     *     The number of users generated for the high volume tests can be easily adjusted via this method:
     *
     *     		InternalTestHelper.setInternalUserNumber(100000);
     *
     *
     *     These tests can be modified to suit new solutions, just as long as the performance metrics
     *     at the end of the tests remains consistent.
     *
     *     These are performance metrics that we are trying to hit:
     *
     *     highVolumeTrackLocation: 100,000 users within 15 minutes:
     *     		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     *
     *     highVolumeGetRewards: 100,000 users within 20 minutes:
     *          assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     */

    @Autowired
    GpsUtilProxy gpsUtilProxy;
    @Autowired
    RewardCentralProxy rewardCentralProxy;

    private static final Locale locale = new Locale("en", "US");

    @Before
    public void init() {
        Locale.setDefault(locale);
        InternalTestHelper.setInternalUserNumber(100);
    }

    @Test
    public void highVolumeTrackLocation() {
        // Users should be incremented up to 100,000, and test finishes within 15 minutes

        RewardsServiceImpl rewardsServiceImpl = new RewardsServiceImpl(gpsUtilProxy, rewardCentralProxy);
        TourGuideServiceImpl tourGuideService = new TourGuideServiceImpl(gpsUtilProxy, rewardsServiceImpl);
        List<User> allUsers = tourGuideService.getAllUsers();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (User user : allUsers) {
            tourGuideService.trackUserLocation(user);
        }
        stopWatch.stop();
        tourGuideService.tracker.stopTracking();

        System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }


    @Test
    public void highVolumeGetRewards() {
        // Users should be incremented up to 100,000, and test finishes within 20 minutes

        RewardsServiceImpl rewardsServiceImpl = new RewardsServiceImpl(gpsUtilProxy, rewardCentralProxy);
        TourGuideServiceImpl tourGuideService = new TourGuideServiceImpl(gpsUtilProxy, rewardsServiceImpl);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        AttractionBean attraction = gpsUtilProxy.getAttractions().get(0);
        List<User> allUsers = tourGuideService.getAllUsers();
        allUsers.forEach(u -> {
            u.clearVisitedLocations();
            u.addToVisitedLocations(new VisitedLocationBean(u.getUserId(), attraction, new Date()));
            rewardsServiceImpl.calculateRewards(u);
        });

        for (User user : allUsers) {
            while (user.getUserRewards().isEmpty()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    e.getCause();
                }
            }
        }
        stopWatch.stop();
        tourGuideService.tracker.stopTracking();

        System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }
}
