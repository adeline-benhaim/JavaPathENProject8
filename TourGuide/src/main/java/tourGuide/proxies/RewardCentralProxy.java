package tourGuide.proxies;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "reward-central", url = "${reward.central.proxy.url}")
public interface RewardCentralProxy {

    /**
     * Get points awarded to a user for a given attraction
     * @param attractionId the id of the attraction concerned
     * @param userId the id of the user concerned
     * @return the number of points assigned to the user for this attraction
     */
    @GetMapping("/getRewards")
    int getRewards(@RequestParam UUID attractionId, @RequestParam UUID userId);
}
