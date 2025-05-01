package dev.codingstoic.receiptwise;

import org.springframework.boot.SpringApplication;

public class TestReceiptWiseApiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(ReceiptWiseApiServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
