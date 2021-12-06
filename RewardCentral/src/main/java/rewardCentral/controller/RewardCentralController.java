package rewardCentral.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rewardCentral.service.RewardCentralService;

import java.util.UUID;

@RestController
public class RewardCentralController {

//    @Autowired
    private RewardCentralService rewardCentralService;

    public RewardCentralController(RewardCentralService rewardCentralService){
        this.rewardCentralService = rewardCentralService;
    }

    /**
     * Get points awarded to a user for a given attraction
     * @param attractionId the id of the attraction concerned
     * @param userId the id of the user concerned
     * @return the number of points assigned to the user for this attraction
     */
    @GetMapping("/getRewards")
    public int getRewards(@RequestParam UUID attractionId, @RequestParam UUID userId) {
        return rewardCentralService.getRewardPoints (attractionId, userId);
    }
}
