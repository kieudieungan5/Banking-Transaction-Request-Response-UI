package vn.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

// Run Spring Boot so server can receive HTTP request & HTTP response
// Application & MySql connects using JDBC API so turn off data source configuration of Spring Boot
@SpringBootApplication()
public class BankingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankingApplication.class, args);
    }

}
