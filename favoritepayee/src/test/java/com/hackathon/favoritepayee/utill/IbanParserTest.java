package com.hackathon.favoritepayee.utill;

import com.hackathon.favoritepayee.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IbanParserTest {

    private final IbanParser ibanParser = new IbanParser();

    @Test
    void extractBankCodeReturnsCharactersAtPositionsFourToSeven() {
        assertEquals("NEDS", ibanParser.extractBankCode("GB12NEDS1234567890"));
    }

    @Test
    void extractBankCodeThrowsForShortOrNullIban() {
        assertEquals("Invalid IBAN format",
                assertThrows(BusinessException.class, () -> ibanParser.extractBankCode("SHORT")).getMessage());
        assertEquals("Invalid IBAN format",
                assertThrows(BusinessException.class, () -> ibanParser.extractBankCode(null)).getMessage());
    }
}
