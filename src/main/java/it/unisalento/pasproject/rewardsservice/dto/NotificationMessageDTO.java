package it.unisalento.pasproject.rewardsservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NotificationMessageDTO {

    private String receiver;
    private String message;
    private String subject;

    //type: ricevuta, notifica generica,
    // notifica di errore, notifica di avviso,
    // notifica di conferma, AUTH
    private String type;

    //Entrambi false mai, di default notifiche attive,
    // se si desidera solo email si mette a false uno e email a true
    private boolean email;
    private boolean notification;
}
