package it.unisalento.pasproject.rewardsservice.controllers;

import it.unisalento.pasproject.rewardsservice.domain.Redeem;
import it.unisalento.pasproject.rewardsservice.domain.Reward;
import it.unisalento.pasproject.rewardsservice.dto.*;
import it.unisalento.pasproject.rewardsservice.exceptions.OutOfStockException;
import it.unisalento.pasproject.rewardsservice.exceptions.RedeemNotFoundException;
import it.unisalento.pasproject.rewardsservice.exceptions.RewardNotFoundException;
import it.unisalento.pasproject.rewardsservice.repositories.RedeemRepository;
import it.unisalento.pasproject.rewardsservice.repositories.RewardRepository;
import it.unisalento.pasproject.rewardsservice.service.CreateTransactionSaga;
import it.unisalento.pasproject.rewardsservice.service.RewardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/rewards")
public class RewardController {
    private final RewardService rewardService;
    private final RewardRepository rewardRepository;
    private final RedeemRepository redeemRepository;
    private final CreateTransactionSaga createTransactionSaga;

    @Autowired
    public RewardController(RewardService rewardService, RewardRepository rewardRepository, CreateTransactionSaga createTransactionSaga, RedeemRepository redeemRepository) {
        this.rewardService = rewardService;
        this.rewardRepository = rewardRepository;
        this.createTransactionSaga = createTransactionSaga;
        this.redeemRepository = redeemRepository;
    }

    @PostMapping(value = "/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RewardDTO createReward(@RequestBody RewardDTO rewardDTO) {
        Reward reward = rewardService.getReward(rewardDTO);

        reward = rewardRepository.save(reward);

        rewardDTO.setId(reward.getId());
        return rewardDTO;
    }

    @PatchMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RewardDTO updateReward(@RequestBody RewardDTO rewardDTO) throws RewardNotFoundException {
        Optional<Reward> reward = rewardRepository.findById(rewardDTO.getId());
        if (reward.isEmpty()) {
           throw new RewardNotFoundException();
        }

        Reward rewardEntity = reward.get();
        Optional.ofNullable(rewardDTO.getName()).ifPresent(rewardEntity::setName);
        Optional.of(rewardDTO.getCost()).ifPresent(rewardEntity::setCost);
        Optional.ofNullable(rewardDTO.getDescription()).ifPresent(rewardEntity::setDescription);
        Optional.ofNullable(rewardDTO.getImage()).ifPresent(rewardEntity::setImage);
        Optional.ofNullable(rewardDTO.getCategory()).ifPresent(rewardEntity::setCategory);
        Optional.ofNullable(rewardDTO.getSubcategory()).ifPresent(rewardEntity::setSubcategory);

        rewardEntity.setAddDate(LocalDateTime.now());

        Optional.of(rewardDTO.isActive()).ifPresent(rewardEntity::setActive);
        Optional.of(rewardDTO.getQuantity()).ifPresent(rewardEntity::setQuantity);
        //Optional.of(rewardDTO.getSold()).ifPresent(rewardEntity::setSold); Solo il sistema può modificare sold

        rewardRepository.save(rewardEntity);

        return rewardDTO;
    }

    @PatchMapping(value = "/activate/{id}")
    public RewardDTO activateReward(@PathVariable String id) throws RewardNotFoundException {
        Optional<Reward> reward = rewardRepository.findById(id);
        if (reward.isEmpty()) {
            throw new RewardNotFoundException();
        }

        Reward rewardEntity = reward.get();
        rewardEntity.setActive(true);

        rewardRepository.save(rewardEntity);

        return rewardService.getRewardDTO(rewardEntity);
    }

    @PatchMapping(value = "/deactivate/{id}")
    public RewardDTO deactivateReward(@PathVariable String id) throws RewardNotFoundException {
        Optional<Reward> reward = rewardRepository.findById(id);
        if (reward.isEmpty()) {
            throw new RewardNotFoundException();
        }

        Reward rewardEntity = reward.get();
        rewardEntity.setActive(false);

        rewardRepository.save(rewardEntity);

        return rewardService.getRewardDTO(rewardEntity);
    }

    @GetMapping(value = "/{id}")
    public RewardDTO getReward(@PathVariable String id) throws RewardNotFoundException {
        Optional<Reward> reward = rewardRepository.findById(id);
        if (reward.isEmpty()) {
            throw new RewardNotFoundException();
        }

        return rewardService.getRewardDTO(reward.get());
    }

    @GetMapping(value = "/all")
    public ListRewardDTO getAllRewards() {
        ListRewardDTO listRewardDTO = new ListRewardDTO();
        listRewardDTO.setRewards(rewardRepository.findAll().stream().map(rewardService::getRewardDTO).toList());

        return listRewardDTO;
    }

    @GetMapping(value = "/find")
    public ListRewardDTO findRewards(@RequestParam(required = false) String name,
                                     @RequestParam(required = false) String category,
                                     @RequestParam(required = false) String subcategory,
                                     @RequestParam(required = false) int minQuantity,
                                     @RequestParam(required = false) int maxQuantity,
                                     @RequestParam(required = false) int maxSold,
                                     @RequestParam(required = false) int minSold,
                                     @RequestParam(required = false) Boolean active){
        ListRewardDTO listRewardDTO = new ListRewardDTO();
        listRewardDTO.setRewards(rewardService.findRewards(name,
                category, subcategory, minQuantity, minSold,
                maxQuantity, maxSold, active)
                .stream()
                .map(rewardService::getRewardDTO)
                .toList());

        return listRewardDTO;
    }

    @PostMapping(value = "/redeem", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RedeemRewardDTO redeemReward(@RequestBody RedeemRewardDTO redeemDTO) throws OutOfStockException, RewardNotFoundException {
       return createTransactionSaga.redeemReward(redeemDTO);
    }

    @GetMapping(value = "/redeems/{id}")
    public RedeemDTO getRedeem(@PathVariable String id) throws RedeemNotFoundException {
        Optional<Redeem> redeem = redeemRepository.findById(id);
        if (redeem.isEmpty()) {
          throw new RedeemNotFoundException();
        }

        return rewardService.getRedeemDTO(redeem.get());

    }

    @GetMapping(value = "/redeems")
    public ListRedeemDTO getAllRedeems() {
        ListRedeemDTO listRedeemDTO = new ListRedeemDTO();
        listRedeemDTO.setRedeems(redeemRepository.findAll().stream().map(rewardService::getRedeemDTO).toList());

        return listRedeemDTO;
    }

    @GetMapping(value = "/redeems/user/{email}")
    public ListRedeemDTO getUserRedeems(@PathVariable String email) {
        ListRedeemDTO listRedeemDTO = new ListRedeemDTO();
        listRedeemDTO.setRedeems(redeemRepository.findAllByUserEmail(email).stream().map(rewardService::getRedeemDTO).toList());

        return listRedeemDTO;
    }

    @GetMapping(value = "/redeems/reward/{id}")
    public ListRedeemDTO getRewardRedeems(@PathVariable String id) {
        ListRedeemDTO listRedeemDTO = new ListRedeemDTO();
        listRedeemDTO.setRedeems(redeemRepository.findAllByRewardId(id).stream().map(rewardService::getRedeemDTO).toList());

        return listRedeemDTO;
    }

    //TODO: Riscatto codice redeem


}
