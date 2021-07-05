package com.springRaft.reactive.stateMachine;

import com.springRaft.reactive.communication.outbound.OutboundContext;
import com.springRaft.reactive.config.RaftProperties;
import lombok.AllArgsConstructor;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@AllArgsConstructor
@ToString
public class SMStrategyContext implements StateMachineStrategy {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(StateMachineWorker.class);

    /* Raft properties that need to be accessed */
    protected final RaftProperties raftProperties;

    /* Outbound context for communication to other servers */
    protected final OutboundContext outbound;

    /* Name of the application server to invoke requests */
    private final String targetServerName;

    /* --------------------------------------------------- */

    @Override
    public Mono<Object> apply(String command) {

        AtomicReference<Object> reply = new AtomicReference<>(null);

        return this.handleRequest(this.outbound.request(command, this.targetServerName))
                    .doOnNext(reply::set)
                .repeat(() -> reply.get() == null)
                .next()
                .cast(Object.class);

    }

    /* --------------------------------------------------- */

    private Mono<?> handleRequest(Mono<?> requestMono) {

        AtomicLong start = new AtomicLong();

        return requestMono
                .doFirst(() -> start.set(System.currentTimeMillis()))
                .onErrorResume(error -> {

                    if (error instanceof WebClientRequestException) {
                        if (((WebClientRequestException)error).contains(ConnectException.class)) {
                            // If target server is not alive

                            log.warn("Server " + this.targetServerName + " is not up!!");

                            // sleep for the remaining time, if any
                            return Mono.delay(Duration.ofMillis(
                                    this.raftProperties.getHeartbeat().toMillis() - (System.currentTimeMillis() - start.get())
                            ))
                                    .then(Mono.empty());

                        }

                    } else {
                        // If another exception occurs
                        log.error("Exception not expected in sendRPCHandler method\nError: " + error);
                    }

                    return Mono.empty();

                });

    }

}
