package com.hackathon.favoritepayee.util;

import com.hackathon.favoritepayee.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class IbanParser {

    public String extractBankCode(String iban) {
        if (iban == null || iban.length() < 8) {
            throw new BusinessException("Invalid IBAN format");
        }
        // Extract substring (index 4-8) - this means indices 4,5,6,7 which is length 4
        return iban.substring(4, 8);
    }
}
