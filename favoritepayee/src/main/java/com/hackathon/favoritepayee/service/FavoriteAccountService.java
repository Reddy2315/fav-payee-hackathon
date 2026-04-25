package com.hackathon.favoritepayee.service;

import com.hackathon.favoritepayee.dto.FavoriteAccountRequest;
import com.hackathon.favoritepayee.dto.FavoriteAccountResponse;
import com.hackathon.favoritepayee.dto.PageResponse;
import com.hackathon.favoritepayee.entity.BankCodeMapping;
import com.hackathon.favoritepayee.entity.Customer;
import com.hackathon.favoritepayee.entity.FavoriteAccount;
import com.hackathon.favoritepayee.exception.BusinessException;
import com.hackathon.favoritepayee.exception.ResourceNotFoundException;
import com.hackathon.favoritepayee.mapper.FavoriteAccountMapper;
import com.hackathon.favoritepayee.repository.BankCodeMappingRepository;
import com.hackathon.favoritepayee.repository.CustomerRepository;
import com.hackathon.favoritepayee.repository.FavoriteAccountRepository;
import com.hackathon.favoritepayee.util.IbanParser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteAccountService {

    private final FavoriteAccountRepository favoriteAccountRepository;
    private final CustomerRepository customerRepository;
    private final BankCodeMappingRepository bankCodeMappingRepository;
    private final FavoriteAccountMapper favoriteAccountMapper;
    private final IbanParser ibanParser;

    private static final int MAX_FAVORITE_ACCOUNTS = 20;

    @Transactional
    public FavoriteAccountResponse createFavoriteAccount(String username, FavoriteAccountRequest request) {
        Customer customer = getCustomer(username);

        if (favoriteAccountRepository.countByCustomerId(customer.getId()) >= MAX_FAVORITE_ACCOUNTS) {
            throw new BusinessException("Maximum limit of " + MAX_FAVORITE_ACCOUNTS + " favorite accounts reached");
        }

        if (favoriteAccountRepository.existsByCustomerIdAndIban(customer.getId(), request.getIban())) {
            throw new BusinessException("Favorite account with this IBAN already exists");
        }

        String bankCode = ibanParser.extractBankCode(request.getIban());
        BankCodeMapping bankMapping = getBankCodeMapping(bankCode);

        FavoriteAccount account = favoriteAccountMapper.toEntity(request);
        account.setCustomer(customer);
        account.setBankCodeMapping(bankMapping);
        account.setBankName(bankMapping.getBankName());

        FavoriteAccount savedAccount = favoriteAccountRepository.save(account);
        return favoriteAccountMapper.toResponse(savedAccount);
    }

    @Transactional(readOnly = true)
    public PageResponse<FavoriteAccountResponse> getFavoriteAccounts(String username, int page, int size) {
        Customer customer = getCustomer(username);
        Pageable pageable = PageRequest.of(page, size);

        Page<FavoriteAccount> accountPage = favoriteAccountRepository.findAllByCustomerId(customer.getId(), pageable);

        return new PageResponse<>(
                favoriteAccountMapper.toResponseList(accountPage.getContent()),
                accountPage.getNumber(),
                accountPage.getSize(),
                accountPage.getTotalElements(),
                accountPage.getTotalPages(),
                accountPage.isLast()
        );
    }

    @Transactional
    public FavoriteAccountResponse updateFavoriteAccount(String username, Long id, FavoriteAccountRequest request) {
        Customer customer = getCustomer(username);

        FavoriteAccount account = favoriteAccountRepository.findByIdAndCustomerId(id, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Favorite account not found"));

        if (!account.getIban().equals(request.getIban())) {
            if (favoriteAccountRepository.existsByCustomerIdAndIban(customer.getId(), request.getIban())) {
                throw new BusinessException("Favorite account with this IBAN already exists");
            }
            String bankCode = ibanParser.extractBankCode(request.getIban());
            BankCodeMapping bankMapping = getBankCodeMapping(bankCode);
            account.setBankCodeMapping(bankMapping);
            account.setBankName(bankMapping.getBankName());
        }

        account.setAccountName(request.getAccountName());
        account.setIban(request.getIban());

        FavoriteAccount updatedAccount = favoriteAccountRepository.save(account);
        return favoriteAccountMapper.toResponse(updatedAccount);
    }

    @Transactional
    public void deleteFavoriteAccount(String username, Long id) {
        Customer customer = getCustomer(username);
        FavoriteAccount account = favoriteAccountRepository.findByIdAndCustomerId(id, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Favorite account not found"));

        favoriteAccountRepository.delete(account);
    }

    private Customer getCustomer(String username) {
        return customerRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    private BankCodeMapping getBankCodeMapping(String bankCode) {
        return bankCodeMappingRepository.findById(bankCode)
                .orElseThrow(() -> new BusinessException("Invalid bank code. Bank not found for the provided IBAN"));
    }
}
