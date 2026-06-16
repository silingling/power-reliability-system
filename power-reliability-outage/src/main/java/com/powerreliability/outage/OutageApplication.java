package com.powerreliability.outage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
@SpringBootApplication
@EnableDiscoveryClient
public class OutageApplication {
    public static void main(String[] args) { SpringApplication.run(OutageApplication.class, args); }
}
