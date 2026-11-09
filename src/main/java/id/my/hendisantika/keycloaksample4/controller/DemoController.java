package id.my.hendisantika.keycloaksample4.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by IntelliJ IDEA.
 * Project : spring-boot-keycloak-sample4
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 14/10/24
 * Time: 14.36
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/demo")
public class DemoController {
    @GetMapping("/all")
    public String helloAll() {
        log.info("hello All from Spring boot & Keycloak");
        return "Hello All from Spring boot & Keycloak";
    }

    @GetMapping("/user")
//    @PreAuthorize("hasRole('client_user')")
    public String hello() {
        log.info("hello from Spring boot & Keycloak");
        return "Hello from Spring boot & Keycloak";
    }

    @GetMapping("/admin")
//    @PreAuthorize("hasRole('client_admin')")
    public String hello2() {
        log.info("Hello from admin");
        return "Hello from Spring boot & Keycloak - ADMIN";
    }
}
