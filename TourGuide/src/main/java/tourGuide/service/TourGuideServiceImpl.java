package tourGuide.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tourGuide.beans.AttractionBean;
import tourGuide.beans.LocationBean;
import tourGuide.beans.VisitedLocationBean;
import tourGuide.exceptions.AttractionNotFoundException;
import tourGuide.exceptions.UserNotFoundException;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.Dto.NearbyAttractionDto;
import tourGuide.model.Dto.NearbyAttractionListByUserDto;
import tourGuide.model.user.User;
import tourGuide.model.user.UserReward;
import tourGuide.proxies.GpsUtilProxy;
import tourGuide.tracker.Tracker;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@Service
public class TourGuideServiceImpl implements TourGuideService {
    private final Logger logger = LoggerFactory.getLogger(TourGuideServiceImpl.class);
    private final GpsUtilProxy gpsUtil;
    private final RewardsServiceImpl rewardsServiceImpl;
    public final Tracker tracker;
    public boolean testMode = true;

    public TourGuideServiceImpl(GpsUtilProxy gpsUtil, RewardsServiceImpl rewardsServiceImpl) {
        this.gpsUtil = gpsUtil;
        this.rewardsServiceImpl = rewardsServiceImpl;

        if (testMode) {
            logger.info("TestMode enabled");
            logger.debug("Initializing users");
            initializeInternalUsers();
            logger.debug("Finished initializing users");
        }
        tracker = new Tracker(this);
        addShutDownHook();
    }

    /**
     * Get a visitedLocation by user
     *
     * @param user the user whose location is sought
     * @return actual user location if its list of visitedLocation is empty otherwise its last visitedLocation
     * @throws ExecutionException   can be thrown when attempting to retrieve the result of trackUserLocation that aborted by throwing an exception.
     * @throws InterruptedException can be thrown when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted, either before or during the activity.
     */
    @Override
    public VisitedLocationBean getUserLocation(User user) throws ExecutionException, InterruptedException {
        logger.info("Get location for user : {}", user.getUserName());
        if (!isExistingUser(user)) throw new UserNotFoundException("No user found with this username");
        return (user.getVisitedLocations().size() > 0) ?
                user.getLastVisitedLocation() :
                trackUserLocation(user).get();
    }

    /**
     * Get a user by userName
     *
     * @param userName of user sought
     * @return the user found
     */
    @Override
    public User getUser(String userName) {
        logger.info("Get user by user name : {}", userName);
        if (!internalUserMap.containsKey(userName)) throw new UserNotFoundException("No user found with this username");
        return internalUserMap.get(userName);
    }

    /**
     * Get a list of all users
     *
     * @return a list with all users found
     */
    public List<User> getAllUsers() {
        logger.info("Get all users");
        return new ArrayList<>(internalUserMap.values());
    }

    /**
     * Add a new user
     *
     * @param user to save
     */
    public void addUser(User user) {
        logger.info("Add user : {}", user.getUserName());
        if (!internalUserMap.containsKey(user.getUserName())) {
            internalUserMap.put(user.getUserName(), user);
        }
    }

    /**
     * Track user location, updates its visitedLocation list with the new location and calculates its rewords
     *
     * @param user the user whose visitedLocation is sought
     * @return a completableFuture of visitedLocation
     */
    public CompletableFuture<VisitedLocationBean> trackUserLocation(User user) {
        logger.info("Track location for user name : {}", user.getUserName());
        ExecutorService executorService = Executors.newFixedThreadPool(1000);
        return CompletableFuture.supplyAsync(() -> gpsUtil.getUserLocation(user.getUserId()), executorService)
                .thenApply(visitedLocationBean -> {
                    user.addToVisitedLocations(visitedLocationBean);
                    rewardsServiceImpl.calculateRewards(user);
                    return visitedLocationBean;
                });
    }

    /**
     * Get a user by id
     *
     * @param userId the id whose user is sought
     * @return user found
     */
    public User getUserById(UUID userId) {
        logger.info("Get user by id : {}", userId);
        List<User> userList = getAllUsers();
        User userByID = null;
        for (User user : userList) {
            if (user.getUserId().equals(userId)) {
                userByID = user;
            }
        }
        return userByID;
    }

    /**
     * Check if user exist
     *
     * @param user to check
     * @return  true if user exist
     * @throws UserNotFoundException if user doesn't exist
     */
    @Override
    public Boolean isExistingUser(User user) throws UserNotFoundException {
        logger.info("Check if existing user : {}", user.getUserName());
        List<User> allUsers = getAllUsers();
        if(!allUsers.contains(user)) throw new UserNotFoundException("No user found with this username");
        return allUsers.contains(user);
    }

    /**
     * Get the closest five tourist attractions to the user sorted in ascending order
     *
     * @param visitedLocation a user location
     * @return a list with the closest five tourist attractions to the user sorted in ascending order
     */
    public List<AttractionBean> getNearByAttractions(VisitedLocationBean visitedLocation) {
        logger.info("Get near attractions for visited location : latitude {}, longitude {}", visitedLocation.getLocationBean().getLatitude(), visitedLocation.getLocationBean().getLongitude());
        List<AttractionBean> nearbyAttractions = new ArrayList<>();
        List<AttractionBean> attractionBeanList = gpsUtil.getAttractions();
        attractionBeanList.stream()
                .sorted(Comparator.comparingDouble(attractionBean -> rewardsServiceImpl.getDistance(new LocationBean(attractionBean.getLatitude(), attractionBean.getLatitude()), visitedLocation.locationBean)))
                .limit(5)
                .forEach(nearbyAttractions::add);
        return nearbyAttractions;
    }

    /**
     * Get the closest five tourist attractions to the user sorted in ascending order with user location information (longitude and latitude).
     * Each tourist attraction contains :
     * - a name
     * - a location (longitude and latitude)
     * - a distance in miles between the user's location
     * - the reward points for visiting this attraction
     *
     * @param visitedLocationBean a user location
     * @return the closest five tourist attractions to the user sorted in ascending order with all user and attractions information
     */
    @Override
    public NearbyAttractionListByUserDto nearbyAttractionListByUserDto(VisitedLocationBean visitedLocationBean) {
        logger.info("Get near attractions with detail for visited location : latitude {}, longitude {}", visitedLocationBean.getLocationBean().getLatitude(), visitedLocationBean.getLocationBean().getLongitude());
        List<AttractionBean> nearbyAttractions = getNearByAttractions(visitedLocationBean);
        List<NearbyAttractionDto> nearbyAttractionDtos = new ArrayList<>();
        User user = getUserById(visitedLocationBean.userId);
        NearbyAttractionDto nearbyAttractionDto;
        for (AttractionBean attractionBean : nearbyAttractions) {
            nearbyAttractionDto = NearbyAttractionDto.builder()
                    .attractionNameDto(attractionBean.getAttractionName())
                    .attractionLocation("Latitude : " + attractionBean.getLatitude() + ", Longitude : " + attractionBean.longitude)
                    .distanceDto(rewardsServiceImpl.getDistance(new LocationBean(attractionBean.getLatitude(), attractionBean.getLatitude()), visitedLocationBean.locationBean))
                    .rewardPoints(rewardsServiceImpl.getRewardPoints(attractionBean, user))
                    .build();
            nearbyAttractionDtos.add(nearbyAttractionDto);
        }
        return NearbyAttractionListByUserDto.builder()
                .userLocation("Latitude : " + visitedLocationBean.getLocationBean().getLatitude() + ", Longitude : " + visitedLocationBean.getLocationBean().getLongitude())
                .nearbyAttractionsDto(nearbyAttractionDtos)
                .build();
    }

    /**
     * Get a list of every user's most recent location
     *
     * @return a map with for each user key = userId and value = {latitude, longitude}
     */
    @Override
    public Map<String, LocationBean> getAllCurrentLocations() {
        logger.info("Get all current location");
        List<User> allUser = getAllUsers();
        String id;
        LocationBean location;
        Map<String, LocationBean> allLocation = new HashMap<>();
        for (User user : allUser) {
            id = user.getUserId().toString();
            location = user.getLastVisitedLocation().locationBean;
            allLocation.put(id, location);
        }
        return allLocation;
    }

    /**
     * Get an attraction by attraction name
     *
     * @param attractionName the name whose attraction is sought
     * @return attraction found by name
     */
    @Override
    public AttractionBean getAttraction(String attractionName) {
        logger.info("Get attraction by name : {}", attractionName);
        List<AttractionBean> attractionBeanList = gpsUtil.getAttractions();
        AttractionBean attraction = null;
        for (AttractionBean attractionBean : attractionBeanList) {
            if (attractionBean.getAttractionName().equalsIgnoreCase(attractionName)) {
                attraction = attractionBean;
                break;
            } else {
                logger.error("Attraction " + attractionName + " doesn't exist");
                throw new AttractionNotFoundException("Attraction : " + attractionName + " not found");
            }
        }
        return attraction;
    }

    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(tracker::stopTracking));
    }

    /**********************************************************************************
     *
     * Methods Below: For Internal Testing
     *
     **********************************************************************************/
    public static final String tripPricerApiKey = "test-server-api-key";
    // Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
    public static Map<String, User> internalUserMap = new HashMap<>();

    private void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
            String userName = "internalUser" + i;
            String phone = "000";
            String email = userName + "@tourGuide.com";
            User user = new User(UUID.randomUUID(), userName, phone, email);
            generateUserLocationHistory(user);

            internalUserMap.put(userName, user);
        });
        User userCustom = new User(UUID.randomUUID(), "userCustom", "000", "userCustom@tourGuide.com");
        userCustom.addToVisitedLocations(new VisitedLocationBean(userCustom.getUserId(), new LocationBean(33.817595D, -117.922008D), getRandomTime()));
        internalUserMap.put("userCustom", userCustom);
        logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
    }

    public void generateUserLocationHistory(User user) {
        IntStream.range(0, 3).forEach(i -> user.addToVisitedLocations(new VisitedLocationBean(user.getUserId(), new LocationBean(generateRandomLatitude(), generateRandomLongitude()), getRandomTime())));
    }

    private double generateRandomLongitude() {
        double leftLimit = -180;
        double rightLimit = 180;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private double generateRandomLatitude() {
        double leftLimit = -85.05112878;
        double rightLimit = 85.05112878;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private Date getRandomTime() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }

}
