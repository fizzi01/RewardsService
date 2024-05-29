package it.unisalento.pasproject.rewardsservice.business.reedem;

import java.security.SecureRandom;
import java.util.Base64;

public class RedeemUtils {

    private RedeemUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String generateSafeToken(){
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return encoder.encodeToString(bytes);
    }

}
