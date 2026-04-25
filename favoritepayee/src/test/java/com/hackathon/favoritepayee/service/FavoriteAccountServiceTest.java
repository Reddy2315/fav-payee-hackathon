package com.hackathon.favoritepayee.service;

import com.hackathon.favoritepayee.dto.FavoriteAccountRequest;
import com.hackathon.favoritepayee.dto.FavoriteAccountResponse;
import com.hackathon.favoritepayee.entity.BankCodeMapping;
import com.hackathon.favoritepayee.entity.Customer;
import com.hackathon.favoritepayee.entity.FavoriteAccount;
import com.hackathon.favoritepayee.exception.BusinessException;
import com.hackathon.favoritepayee.mapper.FavoriteAccountMapper;
import com.hackathon.favoritepayee.repository.BankCodeMappingRepository;
import com.hackathon.favoritepayee.repository.CustomerRepository;
import com.hackathon.favoritepayee.repository.FavoriteAccountRepository;
import com.hackathon.favoritepayee.util.IbanParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteAccountServiceTest {

    @Mock
    private FavoriteAccountRepository favoriteAccountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BankCodeMappingRepository bankCodeMappingRepository;

    @Mock
    private FavoriteAccountMapper favoriteAccountMapper;

    @Mock
    private IbanParser ibanParser;

    @InjectMocks
    private FavoriteAccountService favoriteAccountService;

    private Customer customer;
    private FavoriteAccountRequest request;
    private BankCodeMapping bankMapping;
    private FavoriteAccount account;
    private FavoriteAccountResponse response;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setUsername("testuser");

        request = new FavoriteAccountRequest();
        request.setAccountName("Test Account");
        request.setIban("DE89DEUT100200300400");

        bankMapping = new BankCodeMapping();
        bankMapping.setCode("DEUT");
        bankMapping.setBankName("Deutsche Bank");

        account = new FavoriteAccount();
        account.setId(1L);
        account.setCustomer(customer);
        account.setAccountName("Test Account");
        account.setIban("DE89DEUT100200300400");
        account.setBankCodeMapping(bankMapping);
        account.setBankName("Deutsche Bank");

        response = new FavoriteAccountResponse();
        response.setId(1L);
        response.setAccountName("Test Account");
        response.setIban("DE89DEUT100200300400");
        response.setBankName("Deutsche Bank");
    }

    @Test
    void createFavoriteAccount_success() {
        when(customerRepository.findByUsername("testuser")).thenReturn(Optional.of(customer));
        when(favoriteAccountRepository.countByCustomerId(1L)).thenReturn(5L);
        when(favoriteAccountRepository.existsByCustomerIdAndIban(1L, "DE89DEUT100200300400")).thenReturn(false);
        when(ibanParser.extractBankCode("DE89DEUT100200300400")).thenReturn("DEUT");
        when(bankCodeMappingRepository.findById("DEUT")).thenReturn(Optional.of(bankMapping));
        when(favoriteAccountMapper.toEntity(request)).thenReturn(new FavoriteAccount());
        when(favoriteAccountRepository.save(any(FavoriteAccount.class))).thenReturn(account);
        when(favoriteAccountMapper.toResponse(account)).thenReturn(response);

        FavoriteAccountResponse result = favoriteAccountService.createFavoriteAccount("testuser", request);

        assertNotNull(result);
        assertEquals("Deutsche Bank", result.getBankName());
        verify(favoriteAccountRepository, times(1)).save(any(FavoriteAccount.class));
    }

    @Test
    void createFavoriteAccount_limitReached_throwsException() {
        when(customerRepository.findByUsername("testuser")).thenReturn(Optional.of(customer));
        when(favoriteAccountRepository.countByCustomerId(1L)).thenReturn(20L);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                favoriteAccountService.createFavoriteAccount("testuser", request));

        assertEquals("Maximum limit of 20 favorite accounts reached", exception.getMessage());
        verify(favoriteAccountRepository, never()).save(any());
    }

    @Test
    void createFavoriteAccount_duplicateIban_throwsException() {
        when(customerRepository.findByUsername("testuser")).thenReturn(Optional.of(customer));
        when(favoriteAccountRepository.countByCustomerId(1L)).thenReturn(5L);
        when(favoriteAccountRepository.existsByCustomerIdAndIban(1L, "DE89DEUT100200300400")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                favoriteAccountService.createFavoriteAccount("testuser", request));

        assertEquals("Favorite account with this IBAN already exists", exception.getMessage());
        verify(favoriteAccountRepository, never()).save(any());
    }
}
