package dev.codingstoic.receiptwise.It;

import dev.codingstoic.receiptwise.model.User;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan(basePackages = {"dev.codingstoic.receiptwise"})
@EnableJpaRepositories(basePackages = "dev.codingstoic.receiptwise")
@EntityScan(basePackageClasses = {User.class })
//@EntityScan(basePackages = "com.test.app.entity")
public class ApplicantTestConfig {

}