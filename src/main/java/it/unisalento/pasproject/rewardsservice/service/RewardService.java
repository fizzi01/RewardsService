package it.unisalento.pasproject.rewardsservice.service;

import it.unisalento.pasproject.rewardsservice.business.io.producer.MessageProducer;
import it.unisalento.pasproject.rewardsservice.business.io.producer.MessageProducerStrategy;
import it.unisalento.pasproject.rewardsservice.domain.Redeem;
import it.unisalento.pasproject.rewardsservice.domain.Reward;
import it.unisalento.pasproject.rewardsservice.dto.RedeemDTO;
import it.unisalento.pasproject.rewardsservice.dto.RedeemTransactionDTO;
import it.unisalento.pasproject.rewardsservice.dto.RewardDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RewardService {
    // Quando viene acquistato un reward si incrementa il valore di sold, se sold == quantity il reward viene disattivato
    // Si deve sincronizzare con il wallet, solo ad acquisto effettuato si deve decrementare il valore di quantity

    // Deve agganciarsi con il saga pattern al PaymentService per creare transazione
    // Se la transazione va a buon fine gli ritorna dal Payment l'id del reward, e decrementa la quantità
    // Se la transazione fallisce non ritorna niente e quindi non viene fatto niente sul servizio
    //Il tutto deve essere asincrono (aspetta si ma va avanti indipedentemente)

    private final MessageProducer messageProducer;
    private MongoTemplate mongoTemplate;

    @Value("${rabbitmq.routing.sendTransaction.name}")
    private String sendTransactionRoutingKey;

    @Value("${rabbitmq.exchange.transaction.name}")
    private String transactionExchange;

    @Value("${rabbitmq.routing.sendRewardData.key}")
    private String sendRewardDataRoutingKey;

    @Value("${rabbitmq.exchange.data.name}")
    private String dataExchange;


    @Autowired
    public RewardService(MessageProducer messageProducer,@Qualifier("RabbitMQProducer") MessageProducerStrategy messageProducerStrategy, MongoTemplate mongoTemplate) {
        this.messageProducer = messageProducer;
        this.messageProducer.setStrategy(messageProducerStrategy);
        this.mongoTemplate = mongoTemplate;
    }

    public void sendTransaction(Redeem redeem, Reward rewardEntity) {
        RedeemTransactionDTO transaction = new RedeemTransactionDTO();
        transaction.setSenderEmail(redeem.getUserEmail());
        transaction.setReceiverEmail(redeem.getRedeemId()); //Inserisco l'id della transazione locale
        transaction.setAmount(redeem.getQuantity()*rewardEntity.getCost());
        transaction.setDescription("Redeem reward "+rewardEntity.getName());
        messageProducer.sendMessage(transaction, sendTransactionRoutingKey, transactionExchange);
    }

    public Reward getReward(RewardDTO rewardDTO) {
        Reward reward = new Reward();
        reward.setName(rewardDTO.getName());
        reward.setCost(rewardDTO.getCost());
        reward.setDescription(rewardDTO.getDescription());
        reward.setImage(rewardDTO.getImage());
        reward.setCategory(rewardDTO.getCategory());
        reward.setSubcategory(rewardDTO.getSubcategory());
        reward.setAddDate(rewardDTO.getAddDate());
        reward.setActive(rewardDTO.isActive());
        reward.setQuantity(rewardDTO.getQuantity());
        reward.setSold(rewardDTO.getSold());
        return reward;
    }

    public RewardDTO getRewardDTO(Reward reward) {
        RewardDTO rewardDTO = new RewardDTO();
        rewardDTO.setId(reward.getId());
        rewardDTO.setName(reward.getName());
        rewardDTO.setCost(reward.getCost());
        rewardDTO.setDescription(reward.getDescription());
        rewardDTO.setImage(reward.getImage());
        rewardDTO.setCategory(reward.getCategory());
        rewardDTO.setSubcategory(reward.getSubcategory());
        rewardDTO.setAddDate(reward.getAddDate());
        rewardDTO.setActive(reward.isActive());
        rewardDTO.setQuantity(reward.getQuantity());
        rewardDTO.setSold(reward.getSold());
        return rewardDTO;
    }

    public List<Reward> findRewards(String name, String category, String subcategory, int maxQuantity, int maxSold, int minQuantity, int minSold, Boolean active) {
        Query query = new Query();
        if (name != null) {
            query.addCriteria(Criteria.where("name").is(name));
        }
        if (category != null) {
            query.addCriteria(Criteria.where("category").is(category));
        }
        if (subcategory != null) {
            query.addCriteria(Criteria.where("subcategory").is(subcategory));
        }
        if (active != null) {
            query.addCriteria(Criteria.where("active").is(active));
        } else {
            query.addCriteria(Criteria.where("active").is(true));
        }
        if (minQuantity >= 0) {
            query.addCriteria(Criteria.where("quantity").gte(minQuantity));
        }
        if (maxQuantity >= 0) {
            query.addCriteria(Criteria.where("quantity").lte(maxQuantity));
        }
        if (minSold >= 0) {
            query.addCriteria(Criteria.where("sold").gte(minSold));
        }
        if (maxSold >= 0) {
            query.addCriteria(Criteria.where("sold").lte(maxSold));
        }

        return mongoTemplate.find(query, Reward.class);
    }

    public RedeemDTO getRedeemDTO(Redeem redeem) {
        RedeemDTO redeemDTO = new RedeemDTO();
        redeemDTO.setRedeemId(redeem.getRedeemId());
        redeemDTO.setRewardId(redeem.getRewardId());
        redeemDTO.setUserEmail(redeem.getUserEmail());
        redeemDTO.setQuantity(redeem.getQuantity());
        redeemDTO.setRedeemDate(redeem.getRedeemDate());
        redeemDTO.setRedeemed(redeem.isRedeemed());
        redeemDTO.setRedeemCode(redeem.getRedeemCode());
        redeemDTO.setUsed(redeem.isUsed());
        redeemDTO.setUsedDate(redeem.getUsedDate());
        return redeemDTO;
    }

    public void createWallet(String id) {
        messageProducer.sendMessage(id, sendRewardDataRoutingKey, dataExchange);
    }
}
