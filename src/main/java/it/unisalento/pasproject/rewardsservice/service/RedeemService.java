package it.unisalento.pasproject.rewardsservice.service;

import it.unisalento.pasproject.rewardsservice.domain.Redeem;
import it.unisalento.pasproject.rewardsservice.exceptions.RedeemException;
import it.unisalento.pasproject.rewardsservice.repositories.RedeemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RedeemService {

    //Classe che espone i metodi per andare a riscattare i reedems acquistato utilizzando il redeemCode

    private final RedeemRepository redeemRepository;

    @Autowired
    public RedeemService(RedeemRepository redeemRepository) {
        this.redeemRepository = redeemRepository;
    }


    public Redeem useRedeem(String redeemCode, String userEmail) {
        Optional<Redeem> ret = redeemRepository.findByRedeemCode(redeemCode);

        if(ret.isEmpty()){
            throw new RedeemException("Redeem not found");
        }

        Redeem redeem = ret.get();

        if (!redeem.getUserEmail().equals(userEmail)) {
            throw new RedeemException("Redeem not found");
        }

        //Se data di riscatto è presente e redeemed è false, allora la transazione non è stata completata
        if(redeem.getRedeemDate() != null && !redeem.isRedeemed()){
            throw new RedeemException("Invalid redeem code");
        }

        if(redeem.isUsed()){
            throw new RedeemException("Redeem already used");
        }

        redeem.setUsed(true);
        redeem.setUsedDate(LocalDateTime.now());

        return redeemRepository.save(redeem);
    }


}
