package com.powerreliability.ledger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
@SpringBootApplication
@EnableDiscoveryClient
public class LedgerApplication {
    public static void main(String[] args) { SpringApplication.run(LedgerApplication.class, args); }
}
