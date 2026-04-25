package com.hackathon.favoritepayee.config;

import com.hackathon.favoritepayee.entity.BankCodeMapping;
import com.hackathon.favoritepayee.repository.BankCodeMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final BankCodeMappingRepository bankCodeMappingRepository;

    @Override
    public void run(String... args) throws Exception {
        if (bankCodeMappingRepository.count() == 0) {
            bankCodeMappingRepository.saveAll(List.of(
                    BankCodeMapping.builder().code("DEUT").bankName("Deutsche Bank").build(),
                    BankCodeMapping.builder().code("COMM").bankName("Commerzbank").build(),
                    BankCodeMapping.builder().code("SPAR").bankName("Sparkasse").build(),
                    BankCodeMapping.builder().code("POST").bankName("Postbank").build(),
                    BankCodeMapping.builder().code("INGB").bankName("ING-DiBa").build()
            ));
        }
    }
}
