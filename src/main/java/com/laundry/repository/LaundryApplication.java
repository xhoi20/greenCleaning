package com.laundry.repository;


import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.laundry.entity")
@EnableJpaRepositories(basePackages = {"com.laundry.repository"})
@ComponentScan(basePackages = {"com.laundry.controller","com.laundry.service","com.laundry.repository","com.laundry.tokenlogin"})
@EnableEncryptableProperties

public class LaundryApplication {
    public static void main(String[] args) {
        SpringApplication.run(LaundryApplication.class, args);

    }
}