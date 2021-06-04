package com.springRaft.testexamples.reactivekeyvaluestore.config;

import com.springRaft.testexamples.reactivekeyvaluestore.node.NodeController;
import com.springRaft.testexamples.reactivekeyvaluestore.node.service.ServiceLock;
import com.springRaft.testexamples.reactivekeyvaluestore.node.service.ServicePublisher;
import com.springRaft.testexamples.reactivekeyvaluestore.node.service.ServiceUnsafe;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@AllArgsConstructor
public class Startup implements ApplicationRunner {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(Startup.class);

    /* Application Context for getting beans */
    private final ApplicationContext applicationContext;

    /* Properties that define the Node Service to use */
    private final ServiceProperties serviceProperties;

    /* --------------------------------------------------- */

    @Override
    public void run(ApplicationArguments args) {

        switch (serviceProperties.getStrategy()) {

            case "Publisher":
                ServicePublisher publisher = this.applicationContext.getBean(ServicePublisher.class);
                this.applicationContext.getBean(NodeController.class).setService(publisher);
                publisher.servicePublisher().subscribe();
                break;

            case "Unsafe":
                ServiceUnsafe unsafe = this.applicationContext.getBean(ServiceUnsafe.class);
                this.applicationContext.getBean(NodeController.class).setService(unsafe);
                break;

            case "Lock":
                ServiceLock lock = this.applicationContext.getBean(ServiceLock.class);
                this.applicationContext.getBean(NodeController.class).setService(lock);
                break;

        }

        log.info(this.serviceProperties.toString());

    }

}
