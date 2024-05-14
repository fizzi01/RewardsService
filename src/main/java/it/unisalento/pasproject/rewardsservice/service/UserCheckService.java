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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static it.unisalento.pasproject.rewardsservice.security.SecurityConstants.ROLE_ADMIN;


@Service
public class UserCheckService {

    @Autowired
    private MessageExchanger messageExchanger;

    @Autowired
    @Qualifier("RabbitMQExchange")
    private MessageExchangeStrategy messageExchangeStrategy;

    @Value("${rabbitmq.exchange.security.name}")
    private String securityExchange;

    @Value("${rabbitmq.routing.security.key}")
    private String securityRequestRoutingKey;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserCheckService.class);

    /**
     * Load the user details by email
     * @param email the email of the user
     * @return the user details
     * @throws UsernameNotFoundException if the user is not found
     */
    public UserDetailsDTO loadUserByUsername(String email) throws UsernameNotFoundException {

        messageExchanger.setStrategy(messageExchangeStrategy);

        //Chiamata MQTT a CQRS per ottenere i dettagli dell'utente
        UserDetailsDTO user = messageExchanger.exchangeMessage(email,securityRequestRoutingKey,securityExchange,UserDetailsDTO.class);

        if(user == null) {
            throw new UsernameNotFoundException(email);
        }

        LOGGER.info(String.format("User %s found with role: %s and enabled %s", user.getEmail(), user.getRole(), user.getEnabled()));

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
     * Check if the current user is an administrator
     * @return true if the current user is an administrator, false otherwise
     */
    public Boolean isAdministrator(){
        String currentRole = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();
        return currentRole.equalsIgnoreCase(ROLE_ADMIN);
    }

}
