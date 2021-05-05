package com.springRaft.reactive.communication.inbound;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

@Component
@ConditionalOnProperty(name = "raft.state-machine-strategy", havingValue = "EMBEDDED")
public class EmbeddedFilter implements WebFilter {

    /* Requests prefix */
    private static final String REQUEST_PREFIX = "/raft";

    /* Endpoints to exclude */
    private static final String[] excludedEndpoints = new String[] {"/raft/appendEntries", "/raft/requestVote"};

    /* --------------------------------------------------- */

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {

        return Mono.defer(() -> Mono.just(serverWebExchange.getRequest()))
                .flatMap(request -> {

                    if (this.shouldNotFilter(request)) {

                        return webFilterChain.filter(serverWebExchange);

                    } else {

                        if (!new AntPathMatcher().match(REQUEST_PREFIX + "/**", String.valueOf(request.getPath()))) {

                            return this.addPrefix(serverWebExchange, webFilterChain, request);

                        } else {

                            return this.removePrefix(serverWebExchange, webFilterChain, request);

                        }

                    }

                });

    }

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    private boolean shouldNotFilter(ServerHttpRequest request) {

        return Arrays.stream(excludedEndpoints)
                .anyMatch(e -> new AntPathMatcher().match(e, String.valueOf(request.getPath())));

    }

    /**
     * TODO
     * */
    private Mono<Void> removePrefix(
            ServerWebExchange serverWebExchange,
            WebFilterChain webFilterChain,
            ServerHttpRequest request)
    {

        try {

            ServerHttpRequest newRequest = request.mutate()
                    .path(String.valueOf(request.getPath()).replaceFirst(REQUEST_PREFIX, ""))
                    .uri(new URI(String.valueOf(request.getURI()).replaceFirst(REQUEST_PREFIX, "")))
                    .build();

            return webFilterChain.filter(serverWebExchange.mutate().request(newRequest).build());

        } catch (URISyntaxException e) {

            return webFilterChain.filter(serverWebExchange);

        }

    }

    /**
     * TODO
     * */
    private Mono<Void> addPrefix(
            ServerWebExchange serverWebExchange,
            WebFilterChain webFilterChain,
            ServerHttpRequest request)
    {

        try {

            ServerHttpRequest newRequest = request.mutate()
                    .path(REQUEST_PREFIX + request.getPath())
                    .uri(new URI(String.valueOf(request.getURI())
                            .replaceFirst(String.valueOf(request.getPath()), REQUEST_PREFIX + request.getPath())))
                    .build();

            return webFilterChain.filter(serverWebExchange.mutate().request(newRequest).build());

        } catch (URISyntaxException e) {

            return webFilterChain.filter(serverWebExchange);

        }

    }

}
