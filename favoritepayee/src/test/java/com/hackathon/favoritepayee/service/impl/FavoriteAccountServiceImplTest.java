package com.hackathon.favoritepayee.service.impl;

import com.hackathon.favoritepayee.dto.FavoriteAccountRequest;
import com.hackathon.favoritepayee.dto.FavoriteAccountResponse;
import com.hackathon.favoritepayee.dto.PageResponse;
import com.hackathon.favoritepayee.entity.BankCode;
import com.hackathon.favoritepayee.entity.Customer;
import com.hackathon.favoritepayee.entity.FavoriteAccount;
import com.hackathon.favoritepayee.exception.BusinessException;
import com.hackathon.favoritepayee.exception.ResourceNotFoundException;
import com.hackathon.favoritepayee.repository.BankCodeRepository;
import com.hackathon.favoritepayee.repository.CustomerRepository;
import com.hackathon.favoritepayee.repository.FavoriteAccountRepository;
import com.hackathon.favoritepayee.utill.IbanParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FavoriteAccountServiceImplTest {

    @Mock
    private FavoriteAccountRepository favoriteAccountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BankCodeRepository bankCodeRepository;

    @Mock
    private IbanParser ibanParser;

    @InjectMocks
    private FavoriteAccountServiceImpl service;

    @Test
    void createFavoriteAccountCreatesRecordForExistingCustomer() {
        Customer customer = customerWithFavorites(1L);
        BankCode bankCode = BankCode.builder().id(3L).ibanCode("NEDS").bankName("Ned Bank").build();
        FavoriteAccountRequest request = FavoriteAccountRequest.builder()
                .accountName("Salary")
                .iban("GB12NEDS1234567890")
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(favoriteAccountRepository.count()).thenReturn(2L);
        when(ibanParser.extractBankCode("GB12NEDS1234567890")).thenReturn("NEDS");
        when(bankCodeRepository.findByIbanCode("NEDS")).thenReturn(Optional.of(bankCode));
        when(favoriteAccountRepository.save(any(FavoriteAccount.class))).thenAnswer(invocation -> {
            FavoriteAccount account = invocation.getArgument(0);
            account.setId(99L);
            account.setCreatedAt(LocalDateTime.of(2026, 4, 25, 18, 0));
            account.setUpdatedAt(LocalDateTime.of(2026, 4, 25, 18, 5));
            return account;
        });

        FavoriteAccountResponse response = service.createFavoriteAccount(request);

        assertEquals(99L, response.getId());
        assertEquals("Salary", response.getAccountName());
        assertEquals("GB12NEDS1234567890", response.getIban());
        assertEquals("Ned Bank", response.getBankName());
    }

    @Test
    void createFavoriteAccountCreatesDefaultCustomerWhenMissing() {
        Customer persistedCustomer = customerWithFavorites(1L);
        BankCode bankCode = BankCode.builder().id(3L).ibanCode("NEDS").bankName("Ned Bank").build();
        FavoriteAccountRequest request = FavoriteAccountRequest.builder()
                .accountName("Bills")
                .iban("GB12NEDS1234567890")
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(persistedCustomer);
        when(favoriteAccountRepository.count()).thenReturn(0L);
        when(ibanParser.extractBankCode("GB12NEDS1234567890")).thenReturn("NEDS");
        when(bankCodeRepository.findByIbanCode("NEDS")).thenReturn(Optional.of(bankCode));
        when(favoriteAccountRepository.save(any(FavoriteAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FavoriteAccountResponse response = service.createFavoriteAccount(request);

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        assertEquals("Hackathon User", customerCaptor.getValue().getCustomerName());
        assertEquals("Bills", response.getAccountName());
    }

    @Test
    void createFavoriteAccountThrowsWhenLimitReached() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customerWithFavorites(1L)));
        when(favoriteAccountRepository.count()).thenReturn(20L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.createFavoriteAccount(FavoriteAccountRequest.builder()
                        .accountName("Bills")
                        .iban("GB12NEDS1234567890")
                        .build()));

        assertEquals("Maximum limit of 20 favorite accounts reached", exception.getMessage());
    }

    @Test
    void createFavoriteAccountThrowsWhenIbanAlreadyExists() {
        Customer customer = customerWithFavorites(1L);
        customer.addFavoriteAccount(FavoriteAccount.builder().id(2L).iban("GB12NEDS1234567890").accountName("Old").build());

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(favoriteAccountRepository.count()).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.createFavoriteAccount(FavoriteAccountRequest.builder()
                        .accountName("Bills")
                        .iban("GB12NEDS1234567890")
                        .build()));

        assertEquals("Favorite account with this IBAN already exists", exception.getMessage());
    }

    @Test
    void getFavoriteAccountsReturnsMappedPageResponse() {
        BankCode bankCode = BankCode.builder().bankName("Ned Bank").build();
        FavoriteAccount favoriteAccount = FavoriteAccount.builder()
                .id(10L)
                .accountName("Savings")
                .iban("GB12NEDS1234567890")
                .bankCode(bankCode)
                .createdAt(LocalDateTime.of(2026, 4, 25, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 4, 25, 12, 0))
                .build();
        Page<FavoriteAccount> page = new PageImpl<>(List.of(favoriteAccount), PageRequest.of(0, 5), 1);

        when(favoriteAccountRepository.findAll(PageRequest.of(0, 5))).thenReturn(page);

        PageResponse<FavoriteAccountResponse> response = service.getFavoriteAccounts(0, 5);

        assertEquals(1, response.getContent().size());
        assertEquals(1L, response.getTotalElements());
        assertEquals("Ned Bank", response.getContent().getFirst().getBankName());
    }

    @Test
    void updateFavoriteAccountUpdatesBankCodeWhenIbanChanges() {
        Customer customer = customerWithFavorites(1L);
        BankCode oldBankCode = BankCode.builder().ibanCode("OLDB").bankName("Old Bank").build();
        BankCode newBankCode = BankCode.builder().ibanCode("NEWB").bankName("New Bank").build();
        FavoriteAccount account = FavoriteAccount.builder()
                .id(8L)
                .accountName("Old Name")
                .iban("GB12OLDB1234567890")
                .customer(customer)
                .bankCode(oldBankCode)
                .build();
        customer.addFavoriteAccount(account);

        when(favoriteAccountRepository.findById(8L)).thenReturn(Optional.of(account));
        when(ibanParser.extractBankCode("GB12NEWB1234567890")).thenReturn("NEWB");
        when(bankCodeRepository.findByIbanCode("NEWB")).thenReturn(Optional.of(newBankCode));
        when(favoriteAccountRepository.save(account)).thenReturn(account);

        FavoriteAccountResponse response = service.updateFavoriteAccount(8L, FavoriteAccountRequest.builder()
                .accountName("Updated")
                .iban("GB12NEWB1234567890")
                .build());

        assertEquals("Updated", response.getAccountName());
        assertEquals("GB12NEWB1234567890", response.getIban());
        assertEquals("New Bank", response.getBankName());
    }

    @Test
    void updateFavoriteAccountThrowsWhenDuplicateIbanExists() {
        Customer customer = customerWithFavorites(1L);
        FavoriteAccount current = FavoriteAccount.builder()
                .id(8L)
                .accountName("Current")
                .iban("GB12OLDB1234567890")
                .customer(customer)
                .bankCode(BankCode.builder().bankName("Old Bank").build())
                .build();
        FavoriteAccount duplicate = FavoriteAccount.builder()
                .id(9L)
                .accountName("Duplicate")
                .iban("GB12NEWB1234567890")
                .customer(customer)
                .bankCode(BankCode.builder().bankName("New Bank").build())
                .build();
        customer.setFavoriteAccounts(new ArrayList<>(List.of(current, duplicate)));

        when(favoriteAccountRepository.findById(8L)).thenReturn(Optional.of(current));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.updateFavoriteAccount(8L, FavoriteAccountRequest.builder()
                        .accountName("Updated")
                        .iban("GB12NEWB1234567890")
                        .build()));

        assertEquals("Favorite account with this IBAN already exists", exception.getMessage());
    }

    @Test
    void updateFavoriteAccountThrowsWhenAccountMissing() {
        when(favoriteAccountRepository.findById(123L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.updateFavoriteAccount(123L, FavoriteAccountRequest.builder()
                        .accountName("Updated")
                        .iban("GB12NEWB1234567890")
                        .build()));

        assertEquals("Favorite account not found", exception.getMessage());
    }

    @Test
    void deleteFavoriteAccountDeletesLoadedEntity() {
        FavoriteAccount account = FavoriteAccount.builder().id(5L).build();
        when(favoriteAccountRepository.findById(5L)).thenReturn(Optional.of(account));

        service.deleteFavoriteAccount(5L);

        verify(favoriteAccountRepository).delete(account);
    }

    @Test
    void deleteFavoriteAccountThrowsWhenMissing() {
        when(favoriteAccountRepository.findById(5L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.deleteFavoriteAccount(5L));

        assertEquals("Favorite account not found", exception.getMessage());
    }

    @Test
    void createFavoriteAccountThrowsWhenBankCodeIsUnknown() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customerWithFavorites(1L)));
        when(favoriteAccountRepository.count()).thenReturn(0L);
        when(ibanParser.extractBankCode("GB12MISS1234567890")).thenReturn("MISS");
        when(bankCodeRepository.findByIbanCode("MISS")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.createFavoriteAccount(FavoriteAccountRequest.builder()
                        .accountName("Bills")
                        .iban("GB12MISS1234567890")
                        .build()));

        assertEquals("Invalid bank code. Bank not found for the provided IBAN", exception.getMessage());
    }

    private Customer customerWithFavorites(Long id) {
        Customer customer = Customer.builder()
                .id(id)
                .customerName("Customer " + id)
                .favoriteAccounts(new ArrayList<>())
                .build();
        assertNotNull(customer.getFavoriteAccounts());
        return customer;
    }
}
