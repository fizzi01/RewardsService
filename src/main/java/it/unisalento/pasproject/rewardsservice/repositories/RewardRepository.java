package it.unisalento.pasproject.rewardsservice.repositories;

import it.unisalento.pasproject.rewardsservice.domain.Reward;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RewardRepository extends MongoRepository<Reward, String>{
}
