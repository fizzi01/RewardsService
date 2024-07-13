package it.unisalento.pasproject.rewardsservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.unisalento.pasproject.rewardsservice.TestSecurityConfig;
import it.unisalento.pasproject.rewardsservice.controllers.RewardController;
import it.unisalento.pasproject.rewardsservice.domain.Redeem;
import it.unisalento.pasproject.rewardsservice.dto.CompleteRedeemDTO;
import it.unisalento.pasproject.rewardsservice.dto.RedeemDTO;
import it.unisalento.pasproject.rewardsservice.dto.RedeemRewardDTO;
import it.unisalento.pasproject.rewardsservice.dto.RewardDTO;
import it.unisalento.pasproject.rewardsservice.exceptions.RewardNotFoundException;
import it.unisalento.pasproject.rewardsservice.domain.Reward;
import it.unisalento.pasproject.rewardsservice.exceptions.WrongUserException;
import it.unisalento.pasproject.rewardsservice.repositories.RedeemRepository;
import it.unisalento.pasproject.rewardsservice.repositories.RewardRepository;
import it.unisalento.pasproject.rewardsservice.service.RedeemService;
import it.unisalento.pasproject.rewardsservice.service.UserCheckService;
import it.unisalento.pasproject.rewardsservice.service.RewardService;
import it.unisalento.pasproject.rewardsservice.service.CreateTransactionSaga;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static it.unisalento.pasproject.rewardsservice.security.SecurityConstants.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RewardController.class)
@AutoConfigureMockMvc()
@ExtendWith(MockitoExtension.class)
@Import(TestSecurityConfig.class)
class RewardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RewardRepository rewardRepository;

    @MockBean
    private RewardService rewardService;

    @MockBean
    private RedeemRepository redeemRepository;

    @MockBean
    private UserCheckService userCheckService;

    @MockBean
    private CreateTransactionSaga createTransactionSaga;

    @MockBean
    private RedeemService redeemService;

    @InjectMocks
    private RewardController rewardController;

    @BeforeEach
    void setUp() {
        when(rewardService.getReward(any(RewardDTO.class))).thenCallRealMethod();
        when(rewardService.getRewardDTO(any(Reward.class))).thenCallRealMethod();
        when(rewardService.getRedeemDTO(any(Redeem.class))).thenCallRealMethod();
        given(rewardRepository.save(any(Reward.class))).willAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void createRewardWithValidDataReturnsRewardDTO() throws Exception {
        RewardDTO rewardDTO = new RewardDTO();
        rewardDTO.setName("New Reward");
        rewardDTO.setCost(50.0);
        rewardDTO.setDescription("Test Description");
        rewardDTO.setOldCost(0.0);
        rewardDTO.setSold(0);
        rewardDTO.setQuantity(10);
        rewardDTO.setCategory("Test Category");
        rewardDTO.setSubcategory("Test Subcategory");
        rewardDTO.setActive(true);

        String rewardJson = new ObjectMapper().writeValueAsString(rewardDTO);

        mockMvc.perform(post("/api/rewards/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rewardJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("New Reward")))
                .andExpect(jsonPath("$.cost", is(50.0)))
                .andExpect(jsonPath("$.description", is("Test Description")))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void updateRewardWithNonExistingIdThrowsRewardNotFoundException() throws Exception {
        RewardDTO rewardDTO = new RewardDTO();
        rewardDTO.setId("nonExistingId");
        rewardDTO.setName("Updated Reward");

        String rewardJson = new ObjectMapper().writeValueAsString(rewardDTO);

        when(rewardRepository.findById("nonExistingId")).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/rewards/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rewardJson))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(RewardNotFoundException.class, result.getResolvedException()));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void updateRewardWithValidDataReturnsUpdatedRewardDTO() throws Exception {
        Reward reward = new Reward();
        reward.setId("validId");
        reward.setName("Old Reward");
        reward.setCost(50.0);
        reward.setDescription("Old Description");
        reward.setOldCost(0.0);
        reward.setSold(0);
        reward.setQuantity(10);
        reward.setCategory("Old Category");
        reward.setSubcategory("Old Subcategory");
        reward.setActive(true);

        RewardDTO rewardDTO = new RewardDTO();
        rewardDTO.setId("validId");
        rewardDTO.setName("Updated Reward");
        rewardDTO.setCost(60.0);
        rewardDTO.setDescription("Updated Description");
        rewardDTO.setOldCost(0.0);
        rewardDTO.setSold(0);
        rewardDTO.setQuantity(10);
        rewardDTO.setCategory("Updated Category");
        rewardDTO.setSubcategory("Updated Subcategory");
        rewardDTO.setActive(true);

        String rewardJson = new ObjectMapper().writeValueAsString(rewardDTO);

        when(rewardRepository.findById("validId")).thenReturn(Optional.of(reward));

        mockMvc.perform(patch("/api/rewards/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rewardJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Reward")))
                .andExpect(jsonPath("$.cost", is(60.0)))
                .andExpect(jsonPath("$.description", is("Updated Description")))
                .andExpect(jsonPath("$.category", is("Updated Category")))
                .andExpect(jsonPath("$.subcategory", is("Updated Subcategory")));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void activateRewardWithValidIdChangesStatusToActive() throws Exception {
        Reward reward = new Reward();
        reward.setId("validId");
        reward.setActive(false);
        when(rewardRepository.findById("validId")).thenReturn(Optional.of(reward));

        mockMvc.perform(patch("/api/rewards/activate/validId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void deactivateRewardWithValidIdChangesStatusToInactive() throws Exception {
        Reward reward = new Reward();
        reward.setId("validId");
        reward.setActive(true);
        when(rewardRepository.findById("validId")).thenReturn(Optional.of(reward));

        mockMvc.perform(patch("/api/rewards/deactivate/validId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(false)));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void deleteRewardWithNonExistingIdThrowsRewardNotFoundException() throws Exception {
        when(rewardRepository.findById("nonExistingId")).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/rewards/delete/nonExistingId"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(RewardNotFoundException.class, result.getResolvedException()));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {ROLE_ADMIN,ROLE_MEMBRO})
    void getRewardById() throws Exception {
        Reward reward = new Reward();
        reward.setId("validId");
        reward.setName("Test Reward");
        reward.setCost(50.0);
        reward.setDescription("Test Description");
        reward.setOldCost(0.0);
        reward.setSold(0);
        reward.setQuantity(10);
        reward.setCategory("Test Category");
        reward.setSubcategory("Test Subcategory");
        reward.setActive(true);

        when(rewardRepository.findById("validId")).thenReturn(Optional.of(reward));

        mockMvc.perform(get("/api/rewards/{id}", "validId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Test Reward")))
                .andExpect(jsonPath("$.cost", is(50.0)))
                .andExpect(jsonPath("$.description", is("Test Description")))
                .andExpect(jsonPath("$.category", is("Test Category")))
                .andExpect(jsonPath("$.subcategory", is("Test Subcategory")));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void getAllRewardsAdmin() throws Exception {
        Reward reward1 = new Reward();
        reward1.setId("validId1");
        reward1.setName("Test Reward 1");
        reward1.setCost(50.0);
        reward1.setDescription("Test Description 1");
        reward1.setOldCost(0.0);
        reward1.setSold(0);
        reward1.setQuantity(10);
        reward1.setCategory("Test Category 1");
        reward1.setSubcategory("Test Subcategory 1");
        reward1.setActive(true);

        Reward reward2 = new Reward();
        reward2.setId("validId2");
        reward2.setName("Test Reward 2");
        reward2.setCost(60.0);
        reward2.setDescription("Test Description 2");
        reward2.setOldCost(0.0);
        reward2.setSold(0);
        reward2.setQuantity(10);
        reward2.setCategory("Test Category 2");
        reward2.setSubcategory("Test Subcategory 2");
        reward2.setActive(true);

        when(userCheckService.isAdministrator()).thenReturn(true);
        when(rewardRepository.findAll()).thenReturn(List.of(reward1, reward2));

        mockMvc.perform(get("/api/rewards/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rewards").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void getAllRewardMember() throws Exception {
        Reward reward1 = new Reward();
        reward1.setId("validId1");
        reward1.setName("Test Reward 1");
        reward1.setCost(50.0);
        reward1.setDescription("Test Description 1");
        reward1.setOldCost(0.0);
        reward1.setSold(0);
        reward1.setQuantity(10);
        reward1.setCategory("Test Category 1");
        reward1.setSubcategory("Test Subcategory 1");
        reward1.setActive(true);

        Reward reward2 = new Reward();
        reward2.setId("validId2");
        reward2.setName("Test Reward 2");
        reward2.setCost(60.0);
        reward2.setDescription("Test Description 2");
        reward2.setOldCost(0.0);
        reward2.setSold(0);
        reward2.setQuantity(10);
        reward2.setCategory("Test Category 2");
        reward2.setSubcategory("Test Subcategory 2");
        reward2.setActive(false);

        List<Reward> rewards = new ArrayList<>(List.of(reward1, reward2));

        when(userCheckService.isAdministrator()).thenReturn(false);
        when(rewardRepository.findAllByActive(anyBoolean())).thenAnswer(invocationOnMock -> rewards.stream().filter(reward -> reward.isActive() == (boolean)invocationOnMock.getArgument(0)).toList());

        mockMvc.perform(get("/api/rewards/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rewards").isNotEmpty())
                .andExpect(jsonPath("$.rewards[0].active", is(true)))
                .andExpect(jsonPath("$.rewards[1]").doesNotExist());

    }

    @Test
    @WithMockUser(username = "member@example.com", roles = {"MEMBRO"})
    void redeemRewardWithValidDataReturnsRedeemRewardDTO() throws Exception {
        RedeemRewardDTO redeemRewardDTO = new RedeemRewardDTO();
        redeemRewardDTO.setRedeemId("validRedeemId");
        redeemRewardDTO.setRewardId("validRewardId");
        redeemRewardDTO.setUserEmail("member@example.com");
        redeemRewardDTO.setQuantity(1);

        String redeemJson = new ObjectMapper().writeValueAsString(redeemRewardDTO);

        given(userCheckService.isCorrectUser(anyString())).willCallRealMethod();
        when(createTransactionSaga.redeemReward(any(RedeemRewardDTO.class))).thenReturn(redeemRewardDTO);

        mockMvc.perform(post("/api/rewards/redeem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(redeemJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redeemId", notNullValue()))
                .andExpect(jsonPath("$.rewardId", is("validRewardId")))
                .andExpect(jsonPath("$.userEmail", is("member@example.com")))
                .andExpect(jsonPath("$.quantity", is(1)));
    }

    @Test
    @WithMockUser(username = "member@example.com", roles = {"MEMBRO"})
    void redeemRewardWithInvalidUserThrowsWrongUserException() throws Exception {
        RedeemRewardDTO redeemRewardDTO = new RedeemRewardDTO();
        redeemRewardDTO.setRedeemId("validRedeemId");
        redeemRewardDTO.setRewardId("validRewardId");
        redeemRewardDTO.setUserEmail("other@example.com");
        redeemRewardDTO.setQuantity(1);

        when(userCheckService.getCurrentUserEmail()).thenReturn("member@example.com");
        when(userCheckService.isCorrectUser("other@example.com")).thenReturn(false);

        mockMvc.perform(post("/api/rewards/redeem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(redeemRewardDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "member@example.com", roles = {"MEMBRO"})
    void redeemRewardWithInvalidQuantityThrowsRedeemException() throws Exception {
        RedeemRewardDTO redeemRewardDTO = new RedeemRewardDTO();
        redeemRewardDTO.setRedeemId("validRedeemId");
        redeemRewardDTO.setRewardId("validRewardId");
        redeemRewardDTO.setUserEmail("member@example.com");
        redeemRewardDTO.setQuantity(0);

        mockMvc.perform(post("/api/rewards/redeem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(redeemRewardDTO)))
                .andExpect(status().isExpectationFailed());
    }

    @Test
    @WithMockUser(username = "member@example.com", roles = {"MEMBRO"})
    void redeemRewardWithoutRedeemIdThrowsRedeemException() throws Exception {
        RedeemRewardDTO redeemRewardDTO = new RedeemRewardDTO();
        redeemRewardDTO.setRewardId("validRewardId");
        redeemRewardDTO.setUserEmail("member@example.com");
        redeemRewardDTO.setQuantity(1);

        mockMvc.perform(post("/api/rewards/redeem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(redeemRewardDTO)))
                .andExpect(status().isExpectationFailed());
    }

    @Test
    @WithMockUser(username = "member@example.com", roles = {"MEMBRO"})
    void redeemRewardWithNullUserEmailUsesSessionEmail() throws Exception {
        RedeemRewardDTO redeemRewardDTO = new RedeemRewardDTO();
        redeemRewardDTO.setRedeemId("validRedeemId");
        redeemRewardDTO.setRewardId("validRewardId");
        redeemRewardDTO.setUserEmail(null);
        redeemRewardDTO.setQuantity(1);

        when(userCheckService.getCurrentUserEmail()).thenReturn("member@example.com");
        when(userCheckService.isCorrectUser("member@example.com")).thenReturn(true);
        when(createTransactionSaga.redeemReward(any(RedeemRewardDTO.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/rewards/redeem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(redeemRewardDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userEmail", is("member@example.com")));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void deleteRewardWithValidIdRemovesReward() throws Exception {
        Reward reward = new Reward();
        reward.setId("validIdToDelete");
        when(rewardRepository.findById("validIdToDelete")).thenReturn(Optional.of(reward));

        mockMvc.perform(delete("/api/rewards/delete/validIdToDelete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("validIdToDelete")));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void getRedeemByIdAsAdminReturnsRedeemDTO() throws Exception {
        Redeem redeem = new Redeem();
        redeem.setRewardId("validRewardId");
        redeem.setRedeemId("validRedeemId");
        when(redeemRepository.findById("validRedeemId")).thenReturn(Optional.of(redeem));
        when(userCheckService.isCorrectUser(anyString())).thenReturn(true);
        when(userCheckService.isAdministrator()).thenReturn(true);

        mockMvc.perform(get("/api/rewards/redeems/{id}", "validRedeemId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redeemId", is("validRedeemId")));
    }

    @Test
    @WithMockUser(username = "member@example.com", roles = {"MEMBRO"})
    void getRedeemByIdAsMemberNotOwnerThrowsWrongUserException() throws Exception {
        Redeem redeem = new Redeem();
        redeem.setRedeemId("validRedeemId");
        redeem.setUserEmail("other@example.com");
        when(redeemRepository.findById("validRedeemId")).thenReturn(Optional.of(redeem));
        when(userCheckService.isCorrectUser("other@example.com")).thenReturn(false);
        when(userCheckService.isAdministrator()).thenReturn(false);

        mockMvc.perform(get("/api/rewards/redeems/{id}", "validRedeemId"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void getAllRedeemsAsAdminReturnsListOfRedeemDTO() throws Exception {
        when(redeemRepository.findAll()).thenReturn(List.of(new Redeem(), new Redeem()));
        when(rewardService.getRedeemDTO(any(Redeem.class))).thenReturn(new RedeemDTO());

        mockMvc.perform(get("/api/rewards/redeems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redeems", hasSize(2)));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN", "MEMBRO"})
    void getUserRedeemsAsAdminReturnsListOfRedeemDTO() throws Exception {
        Redeem redeem = new Redeem();
        redeem.setUserEmail("first@example.com");
        redeem.setQuantity(1);
        redeem.setUsed(false);
        Redeem redeem2 = new Redeem();
        redeem2.setUserEmail("second@example.com");
        redeem2.setQuantity(2);
        redeem2.setUsed(false);

        when(redeemRepository.findAllByUserEmail("user@example.com")).thenReturn(List.of(redeem, redeem2));
        when(userCheckService.isCorrectUser(anyString())).thenReturn(true);
        when(userCheckService.isAdministrator()).thenReturn(true);

        mockMvc.perform(get("/api/rewards/redeems/user/{email}", "user@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redeems", hasSize(2)));
    }

    @Test
    @WithMockUser(username = "member@example.com", roles = {"MEMBRO"})
    void getUserRedeemsAsMemberForSelfReturnsListOfRedeemDTO() throws Exception {
        Redeem redeem = new Redeem();
        redeem.setUserEmail("member@example.com");
        redeem.setUsed(false);

        when(userCheckService.getCurrentUserEmail()).thenReturn("member@example.com");
        when(userCheckService.isCorrectUser("member@example.com")).thenReturn(true);
        when(redeemRepository.findAllByUserEmail("member@example.com")).thenReturn(List.of(redeem));

        mockMvc.perform(get("/api/rewards/redeems/user/{email}", "member@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redeems", hasSize(1)));
    }

    @Test
    @WithMockUser(username = "member@example.com", roles = {"MEMBRO"})
    void getUserRedeemsAsMemberForOtherUserThrowsWrongUserException() throws Exception {
        when(userCheckService.getCurrentUserEmail()).thenReturn("member@example.com");
        when(userCheckService.isCorrectUser("other@example.com")).thenReturn(false);

        mockMvc.perform(get("/api/rewards/redeems/user/{email}", "other@example.com"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void getRewardRedeemsAsAdminReturnsListOfRedeemDTO() throws Exception {
        when(redeemRepository.findAllByRewardId("validRewardId")).thenReturn(List.of(new Redeem(), new Redeem()));
        when(rewardService.getRedeemDTO(any(Redeem.class))).thenReturn(new RedeemDTO());

        mockMvc.perform(get("/api/rewards/redeems/reward/{id}", "validRewardId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redeems", hasSize(2)));
    }

    @Test
    @WithMockUser(username = "member@example.com", roles = {"MEMBRO"})
    void redeemCodeWithValidCodeReturnsRedeemDTO() throws Exception {
        Redeem redeem = new Redeem();
        redeem.setRedeemId("validRedeemCode");

        when(userCheckService.getCurrentUserEmail()).thenReturn("member@example.com");
        when(redeemService.useRedeem("validRedeemCode", "member@example.com")).thenReturn(redeem);

        mockMvc.perform(patch("/api/rewards/redeems/use/{redeemCode}", "validRedeemCode"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redeemId", is("validRedeemCode")));
    }

    @Test
    @WithMockUser(username = "member@example.com", roles = {"MEMBRO"})
    void redeemCodeWithInvalidUserThrowsWrongUserException() throws Exception {
        when(userCheckService.getCurrentUserEmail()).thenReturn("member@example.com");
        doThrow(new WrongUserException("User not correct")).when(redeemService).useRedeem(anyString(), eq("member@example.com"));

        mockMvc.perform(patch("/api/rewards/redeems/use/{redeemCode}", "invalidRedeemCode"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "member@example.com", roles = {"MEMBRO"})
    void redeemCodeWithRequestBodyAndValidDataReturnsRedeemDTO() throws Exception {
        CompleteRedeemDTO completeRedeemDTO = new CompleteRedeemDTO();
        completeRedeemDTO.setRedeemCode("validRedeemCode");
        completeRedeemDTO.setUserEmail("member@example.com");

        Redeem redeem = new Redeem();
        when(userCheckService.isCorrectUser("member@example.com")).thenReturn(true);
        when(redeemService.useRedeem("validRedeemCode", "member@example.com")).thenReturn(redeem);

        mockMvc.perform(patch("/api/rewards/redeems/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(completeRedeemDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    @WithMockUser(username = "member@example.com", roles = {"MEMBRO"})
    void redeemCodeWithRequestBodyAndMissingRedeemCodeThrowsRedeemException() throws Exception {
        CompleteRedeemDTO completeRedeemDTO = new CompleteRedeemDTO();
        completeRedeemDTO.setUserEmail("member@example.com");

        mockMvc.perform(patch("/api/rewards/redeems/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(completeRedeemDTO)))
                .andExpect(status().isExpectationFailed());
    }

    @Test
    @WithMockUser(username = "member@example.com", roles = {"MEMBRO"})
    void redeemCodeWithRequestBodyAndWrongUserThrowsWrongUserException() throws Exception {
        CompleteRedeemDTO completeRedeemDTO = new CompleteRedeemDTO();
        completeRedeemDTO.setRedeemCode("validRedeemCode");
        completeRedeemDTO.setUserEmail("other@example.com");

        when(userCheckService.isCorrectUser("other@example.com")).thenReturn(false);

        mockMvc.perform(patch("/api/rewards/redeems/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(completeRedeemDTO)))
                .andExpect(status().isForbidden());
    }
}