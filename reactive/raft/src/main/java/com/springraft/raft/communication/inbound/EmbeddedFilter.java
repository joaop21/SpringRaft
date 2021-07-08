package com.springraft.raft.communication.inbound;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

@Component
@ConditionalOnProperty(name = "raft.state-machine-strategy", havingValue = "EMBEDDED")
public class EmbeddedFilter implements WebFilter {

    /* Requests prefix */
    private static final String REQUEST_PREFIX = "/raft";

    /* Endpoints to exclude */
    private static final List<String> excludedEndpoints = Arrays.asList("/raft/appendEntries", "/raft/requestVote");

    /* --------------------------------------------------- */

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {

        return this.shouldNotFilter(serverWebExchange.getRequest())
                .flatMap(shouldNotFilter -> {

                    if (shouldNotFilter) {

                        return webFilterChain.filter(serverWebExchange);

                    } else {

                        if (!new AntPathMatcher().match(REQUEST_PREFIX + "/**", String.valueOf(serverWebExchange.getRequest().getPath()))) {

                            return this.addPrefix(serverWebExchange, webFilterChain, serverWebExchange.getRequest());

                        } else {

                            return this.removePrefix(serverWebExchange, webFilterChain, serverWebExchange.getRequest());

                        }

                    }

                });

    }

    /* --------------------------------------------------- */

    /**
     * Method that evaluates if the request path matches any of the endpoints to exclude.
     *
     * @param request Request that contains the path.
     *
     * @return True  - The request's path match with at least an excluded endpoint;
     *         False - The request's path doesn't match any excluded endpoint;
     * */
    private Mono<Boolean> shouldNotFilter(ServerHttpRequest request) {
        return Flux.fromIterable(excludedEndpoints)
                .any(endpoint -> new AntPathMatcher().match(endpoint, String.valueOf(request.getPath())));
    }

    /**
     * Method that mutates a request, removing "/raft" prefix from the path and the URI.
     *
     * @param serverWebExchange Object which contains the server communication exchange properties.
     * @param webFilterChain Object that represents the chain to pass on the new exchange.
     * @param request Object which represents the request.
     *
     * @return Void Mono, but the new request is passed to the chain.
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
     * Method that mutates a request, adding "/raft" prefix in the path and the URI.
     *
     * @param serverWebExchange Object which contains the server communication exchange properties.
     * @param webFilterChain Object that represents the chain to pass on the new exchange.
     * @param request Object which represents the request.
     *
     * @return Void Mono, but the new request is passed to the chain.
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
