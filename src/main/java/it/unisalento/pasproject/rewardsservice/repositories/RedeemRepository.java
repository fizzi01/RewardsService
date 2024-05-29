package it.unisalento.pasproject.rewardsservice.repositories;

import it.unisalento.pasproject.rewardsservice.domain.Redeem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RedeemRepository extends MongoRepository<Redeem, String> {
    List<Redeem> findAllByUserEmail(String email);
    List<Redeem> findAllByRewardId(String rewardId);

    Optional<Redeem> findByRedeemCode(String redeemCode);
}
