package com.springRaft.testexamples.reactivekeyvaluestore.config;

import com.springRaft.testexamples.reactivekeyvaluestore.node.ServicePublisher;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@AllArgsConstructor
public class Startup implements ApplicationRunner {

    /* Application Context for getting beans */
    private final ApplicationContext applicationContext;

    /* --------------------------------------------------- */

    @Override
    public void run(ApplicationArguments args) {
        this.applicationContext.getBean(ServicePublisher.class).servicePublisher().subscribe();
    }

}
