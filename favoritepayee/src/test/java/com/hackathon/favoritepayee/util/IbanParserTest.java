package com.hackathon.favoritepayee.util;

import com.hackathon.favoritepayee.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IbanParserTest {

    private IbanParser ibanParser;

    @BeforeEach
    void setUp() {
        ibanParser = new IbanParser();
    }

    @Test
    void extractBankCode_validIban_returnsBankCode() {
        String iban = "DE89DEUT100200300400";
        String bankCode = ibanParser.extractBankCode(iban);
        assertEquals("DEUT", bankCode);
    }

    @Test
    void extractBankCode_shortIban_throwsException() {
        String iban = "DE89DE";
        assertThrows(BusinessException.class, () -> ibanParser.extractBankCode(iban));
    }

    @Test
    void extractBankCode_nullIban_throwsException() {
        assertThrows(BusinessException.class, () -> ibanParser.extractBankCode(null));
    }
}
