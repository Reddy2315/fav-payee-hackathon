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
import com.hackathon.favoritepayee.service.FavoriteAccountService;
import com.hackathon.favoritepayee.utill.IbanParser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteAccountServiceImpl implements FavoriteAccountService {

    private final FavoriteAccountRepository favoriteAccountRepository;
    private final CustomerRepository customerRepository;
    private final BankCodeRepository bankCodeRepository;
    private final IbanParser ibanParser;

    private static final int MAX_FAVORITE_ACCOUNTS = 20;

    // since the payload does not contain a customerId and JWT is removed.
    private static final Long DEFAULT_CUSTOMER_ID = 1L;

    @Override
    @Transactional
    public FavoriteAccountResponse createFavoriteAccount(FavoriteAccountRequest request) {
        Customer customer = getCustomer(DEFAULT_CUSTOMER_ID);

        long currentCount = favoriteAccountRepository.count(); // Assuming it's small, or use a custom query
        if (currentCount >= MAX_FAVORITE_ACCOUNTS) {
            throw new BusinessException("Maximum limit of " + MAX_FAVORITE_ACCOUNTS + " favorite accounts reached");
        }

        // The user's entity has an IBAN field and we must check for uniqueness per customer
        boolean exists = customer.getFavoriteAccounts().stream()
                .anyMatch(acc -> acc.getIban().equals(request.getIban()));
        if (exists) {
            throw new BusinessException("Favorite account with this IBAN already exists");
        }

        String code = ibanParser.extractBankCode(request.getIban());
        BankCode bankCode = getBankCode(code);

        FavoriteAccount account = FavoriteAccount.builder()
                .accountName(request.getAccountName())
                .iban(request.getIban())
                .customer(customer)
                .bankCode(bankCode)
                .isDeleted(false)
                .build();

        FavoriteAccount savedAccount = favoriteAccountRepository.save(account);
        return mapToResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FavoriteAccountResponse> getFavoriteAccounts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<FavoriteAccount> accountPage = favoriteAccountRepository.findAll(pageable);

        List<FavoriteAccountResponse> content = accountPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                accountPage.getNumber(),
                accountPage.getSize(),
                accountPage.getTotalElements(),
                accountPage.getTotalPages(),
                accountPage.isLast()
        );
    }

    @Override
    @Transactional
    public FavoriteAccountResponse updateFavoriteAccount(Long id, FavoriteAccountRequest request) {
        FavoriteAccount account = favoriteAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Favorite account not found"));

        if (!account.getIban().equals(request.getIban())) {
            Customer customer = account.getCustomer();
            boolean exists = customer.getFavoriteAccounts().stream()
                    .anyMatch(acc -> !acc.getId().equals(id) && acc.getIban().equals(request.getIban()));
            if (exists) {
                throw new BusinessException("Favorite account with this IBAN already exists");
            }
            
            String code = ibanParser.extractBankCode(request.getIban());
            BankCode bankCode = getBankCode(code);
            account.setBankCode(bankCode);
        }

        account.setAccountName(request.getAccountName());
        account.setIban(request.getIban());

        FavoriteAccount updatedAccount = favoriteAccountRepository.save(account);
        return mapToResponse(updatedAccount);
    }

    @Override
    @Transactional
    public void deleteFavoriteAccount(Long id) {
        FavoriteAccount account = favoriteAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Favorite account not found"));

        favoriteAccountRepository.delete(account);
    }

    private Customer getCustomer(Long id) {
        return customerRepository.findById(id).orElseGet(() -> {
            // Create default customer if it doesn't exist
            Customer c = Customer.builder().customerName("Hackathon User").build();
            return customerRepository.save(c);
        });
    }

    private BankCode getBankCode(String code) {
        return bankCodeRepository.findByIbanCode(code)
                .orElseThrow(() -> new BusinessException("Invalid bank code. Bank not found for the provided IBAN"));
    }

    private FavoriteAccountResponse mapToResponse(FavoriteAccount entity) {
        return FavoriteAccountResponse.builder()
                .id(entity.getId())
                .accountName(entity.getAccountName())
                .iban(entity.getIban())
                .bankName(entity.getBankCode().getBankName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
