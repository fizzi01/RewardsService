package it.unisalento.pasproject.rewardsservice.service;


import it.unisalento.pasproject.rewardsservice.business.io.exchanger.MessageExchangeStrategy;
import it.unisalento.pasproject.rewardsservice.business.io.exchanger.MessageExchanger;
import it.unisalento.pasproject.rewardsservice.dto.UserDetailsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static it.unisalento.pasproject.rewardsservice.security.SecurityConstants.ROLE_ADMIN;


@Service
public class UserCheckService {


    private final MessageExchanger messageExchanger;

    @Value("${rabbitmq.exchange.security.name}")
    private String securityExchange;

    @Value("${rabbitmq.routing.security.key}")
    private String securityRequestRoutingKey;

    @Autowired
    public UserCheckService(MessageExchanger messageExchanger, @Qualifier("RabbitMQExchange") MessageExchangeStrategy messageExchangeStrategy) {
        this.messageExchanger = messageExchanger;
        this.messageExchanger.setStrategy(messageExchangeStrategy);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(UserCheckService.class);

    /**
     * Load the user details by email
     * @param email the email of the user
     * @return the user details
     * @throws UsernameNotFoundException if the user is not found
     */
    public UserDetailsDTO loadUserByUsername(String email) throws UsernameNotFoundException {

        //Chiamata MQTT a CQRS per ottenere i dettagli dell'utente
        UserDetailsDTO user = null;

        try {
            user = messageExchanger.exchangeMessage(email,securityRequestRoutingKey,securityExchange,UserDetailsDTO.class);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        return user;
    }


    public Boolean isEnable(Boolean enable) {
        return enable;
    }

    /**
     * Check if the current user is the user with the given email
     * @param email the email of the user to check
     * @return true if the current user is the user with the given email, false otherwise
     */
    public Boolean isCorrectUser(String email){
        return email.equals(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    /**
     * Get the email of the current user
     * @return the email of the current user
     */
    public String getCurrentUserEmail(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * Check if the current user is an administrator
     * @return true if the current user is an administrator, false otherwise
     */
    public Boolean isAdministrator(){
        String currentRole = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toArray()[0].toString();
        return currentRole.equalsIgnoreCase(ROLE_ADMIN);
    }

}
