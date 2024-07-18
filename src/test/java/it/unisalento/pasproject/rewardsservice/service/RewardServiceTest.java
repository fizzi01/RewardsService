package it.unisalento.pasproject.rewardsservice.service;

import it.unisalento.pasproject.rewardsservice.business.io.producer.MessageProducer;
import it.unisalento.pasproject.rewardsservice.business.io.producer.MessageProducerStrategy;
import it.unisalento.pasproject.rewardsservice.business.io.producer.RabbitMQProducer;
import it.unisalento.pasproject.rewardsservice.domain.Reward;
import it.unisalento.pasproject.rewardsservice.repositories.RewardRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest()
@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ActiveProfiles("test")
@Import({RewardService.class, MessageProducer.class, RabbitMQProducer.class})
class RewardServiceTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RewardRepository rewardRepository;

    @MockBean
    private MessageProducer messageProducer;

    @Autowired
    @Qualifier("RabbitMQProducer")
    private MessageProducerStrategy messageProducerStrategy;

    @MockBean
    private RabbitMQProducer rabbitMQProducer;

    @Autowired
    private RewardService rewardService;

    @Test
    void findRewardsWithAllParametersReturnsCorrectRewards() {

        List<Reward> rewards = rewardService.findRewards("TestName", "TestCategory", "TestSubcategory", 10, 5, 1, 0, true);

        assertThat(rewards).hasSize(1);
        assertThat(rewards.getFirst().getName()).isEqualTo("TestName");
    }

    @Test
    void findRewardsWithNullParametersReturnsAllActiveRewards() {

        List<Reward> rewards = rewardService.findRewards(null, null, null, -1, -1, -1, -1, null);

        // Return by default all active rewards
        assertThat(rewards).hasSize(2);
    }

    @Test
    void findRewardsWithActiveFalseReturnsNotActiveRewards() {

        List<Reward> rewards = rewardService.findRewards(null, null, null, -1, -1, -1, -1, false);

        //UNa query del genera ritorna solo i reward attivi, quindi non sarà empty
        assertThat(rewards).isNotEmpty();
        assertThat(rewards.getFirst().isActive()).isFalse();

    }

    @Test
    void findRewardsWithMinMaxQuantityAndSoldFiltersReturnsCorrectRewards() {

        List<Reward> rewards = rewardService.findRewards(null, null, null, 5, 5, 2, 1, true);

        assertThat(rewards).hasSize(2);
        assertThat(rewards.stream().allMatch(reward -> reward.getQuantity() <= 5 && reward.getQuantity() >= 2)).isTrue();
        assertThat(rewards.stream().allMatch(reward -> reward.getSold() <= 5 && reward.getSold() >= 1)).isTrue();
    }

    @BeforeEach
    void setupRewardsInDatabase() {
        Reward reward1 = new Reward();
        reward1.setId("1");
        reward1.setName("TestName");
        reward1.setCategory("TestCategory");
        reward1.setSubcategory("TestSubcategory");
        reward1.setCost(10);
        reward1.setQuantity(5);
        reward1.setDescription("Description");
        reward1.setImage("Image");
        reward1.setActive(true);
        reward1.setSold(3);
        reward1.setQuantity(2);

        Reward reward2 = new Reward();
        reward2.setId("2");
        reward2.setName("AnotherName");
        reward2.setCategory("TestCategory");
        reward2.setSubcategory("AnotherSubcategory");
        reward2.setCost(20);
        reward2.setQuantity(10);
        reward2.setDescription("Description");
        reward2.setImage("Image");
        reward2.setActive(true);
        reward2.setSold(5);
        reward2.setQuantity(3);

        Reward reward3 = new Reward();
        reward3.setId("3");
        reward3.setName("ThirdName");
        reward3.setCategory("ThirdCategory");
        reward3.setSubcategory("ThirdSubcategory");
        reward3.setCost(30);
        reward3.setQuantity(15);
        reward3.setDescription("Description");
        reward3.setImage("Image");
        reward3.setActive(false);
        reward3.setSold(10);
        reward3.setQuantity(5);

        rewardRepository.save(reward1);
        rewardRepository.save(reward2);
        rewardRepository.save(reward3);
    }

    @AfterEach
    void cleanDatabase() {
        rewardRepository.deleteAll();
    }
}
