package it.unisalento.pasproject.rewardsservice.service;

import it.unisalento.pasproject.rewardsservice.domain.Reward;
import it.unisalento.pasproject.rewardsservice.domain.Redeem;
import it.unisalento.pasproject.rewardsservice.dto.RedeemRewardDTO;
import it.unisalento.pasproject.rewardsservice.dto.RedeemTransactionDTO;
import it.unisalento.pasproject.rewardsservice.exceptions.OutOfStockException;
import it.unisalento.pasproject.rewardsservice.exceptions.RedeemNotFoundException;
import it.unisalento.pasproject.rewardsservice.exceptions.RewardNotFoundException;
import it.unisalento.pasproject.rewardsservice.repositories.RedeemRepository;
import it.unisalento.pasproject.rewardsservice.repositories.RewardRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CreateTransactionSaga {

    private final RewardRepository rewardRepository;
    private final RedeemRepository redeemRepository;

    private final RewardService rewardService;

    @Autowired
    public CreateTransactionSaga(RewardRepository rewardRepository, RewardService rewardService, RedeemRepository redeemRepository) {
        this.rewardRepository = rewardRepository;
        this.rewardService = rewardService;
        this.redeemRepository = redeemRepository;
    }

    public RedeemRewardDTO redeemReward(RedeemRewardDTO redeemRewardDTO) throws RewardNotFoundException, OutOfStockException {
        Optional<Reward> reward = rewardRepository.findById(redeemRewardDTO.getRewardId());
        if (reward.isEmpty()) {
            throw new RewardNotFoundException("Reward not found with id: " + redeemRewardDTO.getRewardId() );
        }
        Reward rewardEntity = reward.get();

        //Controlla che il reward sia attivo e che ci siano abbastanza pezzi
        if (!rewardEntity.isActive() || rewardEntity.getQuantity() < redeemRewardDTO.getQuantity())
            throw new OutOfStockException("Reward not available or out of stock");

        Redeem redeem = new Redeem();
        redeem.setRewardId(redeemRewardDTO.getRewardId());
        redeem.setUserEmail(redeemRewardDTO.getUserEmail());
        redeem.setQuantity(redeemRewardDTO.getQuantity());
        redeem.setRedeemDate(LocalDateTime.now());
        redeem.setRedeemed(false); //Wait for transaction

        redeem = redeemRepository.save(redeem);

        redeemRewardDTO.setRedeemId(redeem.getRedeemId());

        //Invia richiesta transazione
        rewardService.sendTransaction(redeem, rewardEntity);

        return redeemRewardDTO;
    }

    @RabbitListener(queues = "${rabbitmq.queue.notifyTransaction.name}")
    public void transactionNotification(RedeemTransactionDTO redeemTransactionDTO) throws RedeemNotFoundException, RewardNotFoundException {
        //Riceve notifica transazione
        //Aggiorna stato transazione
        //Aggiorna stato reward
        Optional<Redeem> redeem = redeemRepository.findById(redeemTransactionDTO.getTransactionOwner());
        if (redeem.isEmpty())
            throw new RedeemNotFoundException("Redeem not found with id: " + redeemTransactionDTO.getReceiverEmail());

        Redeem redeemEntity = redeem.get();
        redeemEntity.setRedeemed(redeemTransactionDTO.isCompleted());
        redeemEntity.setRedeemCode("CODE HERE"); //TODO: Generate univoque code for email
        redeemEntity.setUsed(false);
        redeemEntity.setUsedDate(null);

        redeemRepository.save(redeemEntity);

        //Se la transazione non è completata, non aggiornare il reward
        if (!redeemTransactionDTO.isCompleted())
            return;

        //Aggiorna reward
        Optional<Reward> reward = rewardRepository.findById(redeemEntity.getRewardId());
        if (reward.isEmpty())
            throw new RewardNotFoundException("Reward not found with id: " + redeemEntity.getRewardId() );

        Reward rewardEntity = reward.get();
        rewardEntity.setQuantity(rewardEntity.getQuantity() - redeemEntity.getQuantity());
        if (rewardEntity.getQuantity() == 0)
            rewardEntity.setActive(false);
        rewardEntity.setSold(rewardEntity.getSold() + redeemEntity.getQuantity());

        rewardRepository.save(rewardEntity);

    }
}
