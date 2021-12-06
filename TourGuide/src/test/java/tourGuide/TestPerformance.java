package tourGuide;

import static org.junit.Assert.assertTrue;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Ignore;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import tourGuide.beans.AttractionBean;
import tourGuide.beans.VisitedLocationBean;
import tourGuide.helper.InternalTestHelper;
import tourGuide.proxies.GpsUtilProxy;
import tourGuide.proxies.RewardCentralProxy;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;

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
    GpsUtilProxy gpsUtil;

    @Autowired
    RewardsService rewardsService;

//	@Ignore
	@Test
	public void highVolumeTrackLocation() {
//		GpsUtil gpsUtil = new GpsUtil();
//		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		// Users should be incremented up to 100,000, and test finishes within 15 minutes
		InternalTestHelper.setInternalUserNumber(10000);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();

	    StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		for(User user : allUsers) {
		tourGuideService.trackUserLocation(user);
		}
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

//	@Ignore
	@Test
	public void highVolumeGetRewards() {
//		GpsUtil gpsUtil = new GpsUtil();
//		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

		// Users should be incremented up to 100,000, and test finishes within 20 minutes
		InternalTestHelper.setInternalUserNumber(100);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

//		final RateLimiter rateLimiter = RateLimiter.create(1000.0D);
//		rateLimiter.acquire();
//		this.sleepLighter();
//		List<AttractionBean> attractions = new ArrayList();
//		attractions.add(new AttractionBean("Disneyland", "Anaheim", "CA", 33.817595D, -117.922008D));
//		attractions.add(new AttractionBean("Jackson Hole", "Jackson Hole", "WY", 43.582767D, -110.821999D));
//
//		AttractionBean attraction = attractions.get(0);
	    AttractionBean attraction = gpsUtil.getAttractions().get(0);
		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();
		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocationBean(u.getUserId(), attraction, new Date())));

	    allUsers.forEach(u -> rewardsService.calculateRewards(u));

		for(User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

//	private void sleepLighter() {
//		try {
//			TimeUnit.MILLISECONDS.sleep(10L);
//		} catch (InterruptedException var2) {
//		}
//
//	}
}
