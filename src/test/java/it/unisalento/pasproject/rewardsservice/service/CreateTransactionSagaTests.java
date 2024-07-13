package it.unisalento.pasproject.rewardsservice.service;

import it.unisalento.pasproject.rewardsservice.domain.Redeem;
import it.unisalento.pasproject.rewardsservice.domain.Reward;
import it.unisalento.pasproject.rewardsservice.dto.NotificationMessageDTO;
import it.unisalento.pasproject.rewardsservice.dto.RedeemRewardDTO;
import it.unisalento.pasproject.rewardsservice.dto.RedeemTransactionDTO;
import it.unisalento.pasproject.rewardsservice.exceptions.OutOfStockException;
import it.unisalento.pasproject.rewardsservice.exceptions.RedeemNotFoundException;
import it.unisalento.pasproject.rewardsservice.exceptions.RewardNotFoundException;
import it.unisalento.pasproject.rewardsservice.repositories.RedeemRepository;
import it.unisalento.pasproject.rewardsservice.repositories.RewardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {CreateTransactionSaga.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateTransactionSagaTests {

    @MockBean
    private RewardRepository rewardRepository;

    @MockBean
    private RedeemRepository redeemRepository;

    @MockBean
    private RewardService rewardService;

    @MockBean
    private NotificationMessageHandler notificationHandler;

    @MockBean
    private Logger logger;

    @InjectMocks
    private CreateTransactionSaga createTransactionSaga;

    private final ArgumentCaptor<Redeem> redeemArgumentCaptor = ArgumentCaptor.forClass(Redeem.class);
    private final ArgumentCaptor<Reward> rewardArgumentCaptor = ArgumentCaptor.forClass(Reward.class);



    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        rewardRepository = mock(RewardRepository.class);
        redeemRepository = mock(RedeemRepository.class);
        rewardService = mock(RewardService.class);
        notificationHandler = mock(NotificationMessageHandler.class);
        createTransactionSaga = new CreateTransactionSaga(rewardRepository, rewardService, redeemRepository, notificationHandler);
    }

    @Test
    void redeemReward_withValidData_createsRedeemAndNotifiesUser() {
        RedeemRewardDTO redeemRewardDTO = new RedeemRewardDTO();
        redeemRewardDTO.setRewardId("validRewardId");
        redeemRewardDTO.setUserEmail("user@example.com");
        redeemRewardDTO.setQuantity(1);

        Reward reward = new Reward();
        reward.setId("validRewardId");
        reward.setQuantity(10);
        reward.setActive(true);

        Redeem redeem = new Redeem();
        redeem.setRewardId("validRewardId");
        redeem.setUserEmail("user@example.com");
        redeem.setQuantity(1);

        when(rewardRepository.findById("validRewardId")).thenReturn(Optional.of(reward));
        when(redeemRepository.save(any(Redeem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        createTransactionSaga.redeemReward(redeemRewardDTO);

        ArgumentCaptor<Redeem> redeemCaptor = ArgumentCaptor.forClass(Redeem.class);
        verify(redeemRepository, times(1)).save(redeemCaptor.capture());

        Redeem savedRedeem = redeemCaptor.getValue();

        assertEquals("validRewardId", savedRedeem.getRewardId());
        assertEquals("user@example.com", savedRedeem.getUserEmail());
        assertEquals(1, savedRedeem.getQuantity());

        verify(rewardService, times(1)).sendTransaction(any(Redeem.class), any(Reward.class));
    }

    @Test
    void redeemReward_withOutOfStock_throwsOutOfStockException() {
        RedeemRewardDTO redeemRewardDTO = new RedeemRewardDTO();
        redeemRewardDTO.setRewardId("validRewardId");
        redeemRewardDTO.setUserEmail("user@example.com");
        redeemRewardDTO.setQuantity(1);

        Reward reward = new Reward();
        reward.setId("validRewardId");
        reward.setQuantity(0);

        when(rewardRepository.findById("validRewardId")).thenReturn(Optional.of(reward));

        assertThrows(OutOfStockException.class, () -> createTransactionSaga.redeemReward(redeemRewardDTO));
    }

    @Test
    void redeemReward_withNonExistingReward_throwsRewardNotFoundException() {
        RedeemRewardDTO redeemRewardDTO = new RedeemRewardDTO();
        redeemRewardDTO.setRewardId("invalidRewardId");
        redeemRewardDTO.setUserEmail("user@example.com");
        redeemRewardDTO.setQuantity(1);

        when(rewardRepository.findById("invalidRewardId")).thenReturn(Optional.empty());

        assertThrows(RewardNotFoundException.class, () -> createTransactionSaga.redeemReward(redeemRewardDTO));
    }

    @Test
    void transactionNotificationWithCompletedTransactionUpdatesRedeemAndSendsNotification() {
        RedeemTransactionDTO dto = new RedeemTransactionDTO();
        dto.setTransactionOwner("redeemId");
        dto.setCompleted(true);

        Redeem redeem = new Redeem();
        redeem.setRewardId("rewardId");
        redeem.setUserEmail("user@example.com");
        redeem.setQuantity(1);

        Reward reward = new Reward();
        reward.setId("rewardId");
        reward.setQuantity(10);

        when(redeemRepository.findById("redeemId")).thenReturn(Optional.of(redeem));
        when(rewardRepository.findById("rewardId")).thenReturn(Optional.of(reward));

        createTransactionSaga.transactionNotification(dto);

        verify(redeemRepository).save(redeemArgumentCaptor.capture());
        Redeem updatedRedeem = redeemArgumentCaptor.getValue();
        assertTrue(updatedRedeem.isRedeemed());

        verify(rewardRepository).save(rewardArgumentCaptor.capture());
        Reward updatedReward = rewardArgumentCaptor.getValue();
        assertEquals(9, updatedReward.getQuantity());
    }

    @Test
    void transactionNotificationWithFailedTransactionDoesNotUpdateReward() {
        RedeemTransactionDTO dto = new RedeemTransactionDTO();
        dto.setTransactionOwner("redeemId");
        dto.setCompleted(false);

        Redeem redeem = new Redeem();
        redeem.setRewardId("rewardId");
        redeem.setUserEmail("user@example.com");

        when(redeemRepository.findById("redeemId")).thenReturn(Optional.of(redeem));

        createTransactionSaga.transactionNotification(dto);

        verify(redeemRepository).save(redeemArgumentCaptor.capture());
        Redeem updatedRedeem = redeemArgumentCaptor.getValue();
        assertFalse(updatedRedeem.isRedeemed());

        verify(rewardRepository, never()).save(any(Reward.class));
    }

    @Test
    void transactionNotificationForNonexistentRedeemPrintError() {
        RedeemTransactionDTO dto = new RedeemTransactionDTO();
        dto.setTransactionOwner("nonexistentRedeemId");
        dto.setCompleted(true);

        when(redeemRepository.findById(anyString())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> createTransactionSaga.transactionNotification(dto));
        verify(redeemRepository, never()).save(any(Redeem.class));
    }

    @Test
    void transactionNotificationForNonexistentRewardThrowsException() {
        RedeemTransactionDTO dto = new RedeemTransactionDTO();
        dto.setTransactionOwner("redeemId");
        dto.setCompleted(true);

        Redeem redeem = new Redeem();
        redeem.setRewardId("nonexistentRewardId");

        when(redeemRepository.findById("redeemId")).thenReturn(Optional.of(redeem));
        when(rewardRepository.findById("nonexistentRewardId")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> createTransactionSaga.transactionNotification(dto));
        verify(rewardRepository, never()).save(any(Reward.class));
    }
}