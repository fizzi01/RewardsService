package it.unisalento.pasproject.rewardsservice.repositories;

import it.unisalento.pasproject.rewardsservice.domain.Redeem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RedeemRepository extends MongoRepository<Redeem, String> {
    List<Redeem> findAllByUserEmail(String email);
    List<Redeem> findAllByRewardId(String rewardId);
}
