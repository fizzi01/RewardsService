package it.unisalento.pasproject.rewardsservice.controllers;

import it.unisalento.pasproject.rewardsservice.domain.Redeem;
import it.unisalento.pasproject.rewardsservice.domain.Reward;
import it.unisalento.pasproject.rewardsservice.dto.*;
import it.unisalento.pasproject.rewardsservice.exceptions.*;
import it.unisalento.pasproject.rewardsservice.repositories.RedeemRepository;
import it.unisalento.pasproject.rewardsservice.repositories.RewardRepository;
import it.unisalento.pasproject.rewardsservice.service.CreateTransactionSaga;
import it.unisalento.pasproject.rewardsservice.service.RedeemService;
import it.unisalento.pasproject.rewardsservice.service.RewardService;
import it.unisalento.pasproject.rewardsservice.service.UserCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

import static it.unisalento.pasproject.rewardsservice.security.SecurityConstants.ROLE_ADMIN;
import static it.unisalento.pasproject.rewardsservice.security.SecurityConstants.ROLE_MEMBRO;

@RestController
@RequestMapping("/api/rewards")
public class RewardController {
    private final RewardService rewardService;
    private final RewardRepository rewardRepository;
    private final RedeemRepository redeemRepository;
    private final CreateTransactionSaga createTransactionSaga;
    private final UserCheckService userCheckService;
    private final RedeemService redeemService;

    @Autowired
    public RewardController(RewardService rewardService, RewardRepository rewardRepository, CreateTransactionSaga createTransactionSaga,
                            RedeemRepository redeemRepository, UserCheckService userCheckService, RedeemService redeemService) {
        this.rewardService = rewardService;
        this.rewardRepository = rewardRepository;
        this.createTransactionSaga = createTransactionSaga;
        this.redeemRepository = redeemRepository;
        this.userCheckService = userCheckService;
        this.redeemService = redeemService;
    }

    @PostMapping(value = "/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Secured({ROLE_ADMIN})
    public RewardDTO createReward(@RequestBody RewardDTO rewardDTO) {
        Reward reward = rewardService.getReward(rewardDTO);

        reward = rewardRepository.save(reward);

        //Inviare notifica al wallet per aggiungere il reward come wallet per eseguire le transazioni
        rewardService.createWallet(reward.getId());

        rewardDTO.setId(reward.getId());
        return rewardDTO;
    }

    @PatchMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Secured({ROLE_ADMIN})
    public RewardDTO updateReward(@RequestBody RewardDTO rewardDTO) throws RewardNotFoundException {
        Optional<Reward> reward = rewardRepository.findById(rewardDTO.getId());
        if (reward.isEmpty()) {
           throw new RewardNotFoundException("Reward not found with id: " + rewardDTO.getId() );
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
    @Secured({ROLE_ADMIN})
    public RewardDTO activateReward(@PathVariable String id) throws RewardNotFoundException {
        Optional<Reward> reward = rewardRepository.findById(id);
        if (reward.isEmpty()) {
            throw new RewardNotFoundException("Reward not found with id: " + id );
        }

        Reward rewardEntity = reward.get();
        rewardEntity.setActive(true);

        rewardRepository.save(rewardEntity);

        return rewardService.getRewardDTO(rewardEntity);
    }

    @PatchMapping(value = "/deactivate/{id}")
    @Secured({ROLE_ADMIN})
    public RewardDTO deactivateReward(@PathVariable String id) throws RewardNotFoundException {
        Optional<Reward> reward = rewardRepository.findById(id);
        if (reward.isEmpty()) {
            throw new RewardNotFoundException("Reward not found with id: " + id );
        }

        Reward rewardEntity = reward.get();
        rewardEntity.setActive(false);

        rewardRepository.save(rewardEntity);

        return rewardService.getRewardDTO(rewardEntity);
    }

    @GetMapping(value = "/{id}")
    @Secured({ROLE_ADMIN, ROLE_MEMBRO})
    public RewardDTO getReward(@PathVariable String id) throws RewardNotFoundException {
        Optional<Reward> reward = rewardRepository.findById(id);
        if (reward.isEmpty()) {
            throw new RewardNotFoundException("Reward not found with id: " + id);
        }

        return rewardService.getRewardDTO(reward.get());
    }

    @GetMapping(value = "/all")
    @Secured({ROLE_ADMIN, ROLE_MEMBRO})
    public ListRewardDTO getAllRewards() {
        ListRewardDTO listRewardDTO = new ListRewardDTO();
        listRewardDTO.setRewards(rewardRepository.findAll().stream().map(rewardService::getRewardDTO).toList());

        return listRewardDTO;
    }

    @GetMapping(value = "/find")
    @Secured({ROLE_ADMIN, ROLE_MEMBRO})
    public ListRewardDTO findRewards(@RequestParam(required = false) String name,
                                     @RequestParam(required = false) String category,
                                     @RequestParam(required = false) String subcategory,
                                     @RequestParam(required = false) Integer minQuantity,
                                     @RequestParam(required = false) Integer maxQuantity,
                                     @RequestParam(required = false) Integer maxSold,
                                     @RequestParam(required = false) Integer minSold,
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
    @Secured({ROLE_MEMBRO})
    public RedeemRewardDTO redeemReward(@RequestBody RedeemRewardDTO redeemDTO) throws OutOfStockException, RewardNotFoundException, WrongUserException {

        String userEmail = redeemDTO.getUserEmail();

        // Fallback: if userEmail is null, get the current session user email (as it should be)
        if(userEmail == null){
            userEmail = userCheckService.getCurrentUserEmail();
            redeemDTO.setUserEmail(userEmail);
        }

        if (!userCheckService.isCorrectUser(redeemDTO.getUserEmail())){
            throw new WrongUserException("User not correct");
        }

        return createTransactionSaga.redeemReward(redeemDTO);
    }

    @GetMapping(value = "/redeems/{id}")
    @Secured({ROLE_ADMIN, ROLE_MEMBRO})
    public RedeemDTO getRedeem(@PathVariable String id) throws RedeemNotFoundException {
        Optional<Redeem> redeem = redeemRepository.findById(id);

        if (redeem.isEmpty()) {
          throw new RedeemNotFoundException("Redeem not found with id: " + id);
        }

        //Se ROLE_MEMBRO controllo che l'utente sia il proprietario del riscatto, se ROLE_ADMIN non controllo
        if (!userCheckService.isCorrectUser(redeem.get().getUserEmail()) && !userCheckService.isAdministrator()){
            throw new WrongUserException("User not correct");
        }

        return rewardService.getRedeemDTO(redeem.get());

    }

    @GetMapping(value = "/redeems")
    @Secured({ROLE_ADMIN})
    public ListRedeemDTO getAllRedeems() {
        ListRedeemDTO listRedeemDTO = new ListRedeemDTO();
        listRedeemDTO.setRedeems(redeemRepository.findAll().stream().map(rewardService::getRedeemDTO).toList());

        return listRedeemDTO;
    }

    /**
     * Restituisce tutti i riscatti di un utente
     * @param email email dell'utente (se null, prende l'utente corrente)
     * @return lista di riscatti
     */
    @GetMapping(value = "/redeems/user/{email}")
    @Secured({ROLE_ADMIN,ROLE_MEMBRO})
    public ListRedeemDTO getUserRedeems(@PathVariable(required = false) String email) throws WrongUserException {

        // Fallback: if email is null, get the current session user email (as it should be)
        if(email == null){
            email = userCheckService.getCurrentUserEmail();
        }

        //il ROLE_ADMIN può vedere i riscatti di tutti, il ROLE_MEMBRO solo i propri
        if (!userCheckService.isCorrectUser(email) && !userCheckService.isAdministrator()){
            throw new WrongUserException("User not correct");
        }

        ListRedeemDTO listRedeemDTO = new ListRedeemDTO();
        listRedeemDTO.setRedeems(redeemRepository.findAllByUserEmail(email).stream().map(rewardService::getRedeemDTO).toList());

        return listRedeemDTO;
    }

    /**
     * Restituisce tutti i riscatti di un reward
     * @param id id del reward
     * @return lista di riscatti
     */
    @GetMapping(value = "/redeems/reward/{id}")
    @Secured({ROLE_ADMIN})
    public ListRedeemDTO getRewardRedeems(@PathVariable String id) {
        ListRedeemDTO listRedeemDTO = new ListRedeemDTO();
        listRedeemDTO.setRedeems(redeemRepository.findAllByRewardId(id).stream().map(rewardService::getRedeemDTO).toList());

        return listRedeemDTO;
    }

    /**
     * Riscatta un redeem code appartenente all'utente che esegue la richiesta
     * @param redeemCode redeem code da riscattare
     * @return resoconto del riscatto
     */
    @PatchMapping(value = "/redeems/use/{redeemCode}")
    @Secured({ROLE_MEMBRO})
    public RedeemDTO riscattoRedeemCode(@PathVariable String redeemCode) {

        if ( redeemCode == null){
            throw new RedeemException("Missing redeem code");
        }

        Redeem redeem = redeemService.useRedeem(redeemCode, userCheckService.getCurrentUserEmail());

        return rewardService.getRedeemDTO(redeem);
    }

    @PatchMapping(value = "/redeems/use")
    @Secured({ROLE_MEMBRO})
    public RedeemDTO riscattoRedeemCode(@RequestBody CompleteRedeemDTO completeRedeemDTO) {

        if (completeRedeemDTO.getRedeemCode() == null){
            throw new RedeemException("Missing redeem code");
        }

        //Verifica che l'utente sia il proprietario del riscatto
        if (!userCheckService.isCorrectUser(completeRedeemDTO.getUserEmail())){
            throw new WrongUserException("User not correct");
        }

        Redeem redeem = redeemService.useRedeem(completeRedeemDTO.getRedeemCode(), completeRedeemDTO.getUserEmail());

        return rewardService.getRedeemDTO(redeem);
    }


}
