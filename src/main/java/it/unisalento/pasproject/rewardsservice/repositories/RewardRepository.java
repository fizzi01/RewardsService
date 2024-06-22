package it.unisalento.pasproject.rewardsservice.repositories;

import it.unisalento.pasproject.rewardsservice.domain.Reward;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface RewardRepository extends MongoRepository<Reward, String>{
    List<Reward> findAllByActive(boolean b);
}
