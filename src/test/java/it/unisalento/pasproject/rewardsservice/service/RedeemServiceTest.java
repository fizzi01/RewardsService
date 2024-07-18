package it.unisalento.pasproject.rewardsservice.service;

import it.unisalento.pasproject.rewardsservice.exceptions.RedeemException;
import it.unisalento.pasproject.rewardsservice.repositories.RedeemRepository;
import it.unisalento.pasproject.rewardsservice.domain.Redeem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.mockito.InjectMocks;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {RedeemService.class})
@ExtendWith(MockitoExtension.class)
class RedeemServiceTest {

    @MockBean
    private RedeemRepository redeemRepository;

    @InjectMocks
    private RedeemService redeemService;

    private Redeem redeem;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        redeemRepository = mock(RedeemRepository.class);
        redeemService = new RedeemService(redeemRepository);

        redeem = new Redeem();
        redeem.setRedeemCode("validCode");
        redeem.setUserEmail("user@example.com");
        redeem.setUsed(false);
    }

    @Test
    void useRedeemWithValidCodeAndEmailMarksRedeemAsUsed() {
        when(redeemRepository.findByRedeemCode("validCode")).thenReturn(Optional.of(redeem));
        given(redeemRepository.save(any(Redeem.class))).willAnswer(invocation -> invocation.getArgument(0));

        Redeem updatedRedeem = redeemService.useRedeem("validCode", "user@example.com");

        assertTrue(updatedRedeem.isUsed());
        assertNotNull(updatedRedeem.getUsedDate());
        verify(redeemRepository).save(any(Redeem.class));
    }

    @Test
    void useRedeemWithInvalidCodeThrowsRedeemException() {
        when(redeemRepository.findByRedeemCode("invalidCode")).thenReturn(Optional.empty());

        assertThrows(RedeemException.class, () -> redeemService.useRedeem("invalidCode", "user@example.com"));
    }

    @Test
    void useRedeemWithInvalidEmailThrowsRedeemException() {
        when(redeemRepository.findByRedeemCode("validCode")).thenReturn(Optional.of(redeem));

        assertThrows(RedeemException.class, () -> redeemService.useRedeem("validCode", "wrong@example.com"));
    }

    @Test
    void useRedeemWithAlreadyUsedCodeThrowsRedeemException() {
        redeem.setUsed(true);
        when(redeemRepository.findByRedeemCode("validCode")).thenReturn(Optional.of(redeem));

        assertThrows(RedeemException.class, () -> redeemService.useRedeem("validCode", "user@example.com"));
    }

    @Test
    void useRedeemWithUncompletedTransactionThrowsRedeemException() {
        redeem.setRedeemDate(LocalDateTime.now());
        redeem.setRedeemed(false);
        when(redeemRepository.findByRedeemCode("validCode")).thenReturn(Optional.of(redeem));

        assertThrows(RedeemException.class, () -> redeemService.useRedeem("validCode", "user@example.com"));
    }
}
