package com.springRaft.servlet.communication.inbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

@Component
@ConditionalOnProperty(name = "raft.state-machine-strategy", havingValue = "EMBEDDED")
public class EmbeddedFilter extends OncePerRequestFilter {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(EmbeddedFilter.class);

    /* Requests prefix */
    private static final String REQUEST_PREFIX = "/raft";

    /* Endpoints to exclude */
    private static final String[] excludedEndpoints = new String[] {"/raft/appendEntries", "/raft/requestVote"};

    /* --------------------------------------------------- */

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String changedURI;
        StringBuffer changedURL;

        if (!new AntPathMatcher().match(REQUEST_PREFIX + "/**", request.getServletPath())) {

            changedURI = REQUEST_PREFIX + request.getRequestURI();
            changedURL = new StringBuffer(request.getRequestURL().toString().replaceFirst(request.getRequestURI(), REQUEST_PREFIX + request.getRequestURI()));

        } else {

            changedURI = request.getRequestURI().replaceFirst(REQUEST_PREFIX, "");
            changedURL = new StringBuffer(request.getRequestURL().toString().replaceFirst(REQUEST_PREFIX, ""));

        }

        log.info("\n\nChanged URI: {}\nChanged URL: {}\n\n", changedURI, changedURL.toString());

        filterChain.doFilter(this.createHttpServletRequestWrapper(request, changedURI, changedURL), response);

    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        return Arrays.stream(excludedEndpoints).anyMatch(e -> new AntPathMatcher().match(e, request.getServletPath()));

    }

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    private HttpServletRequestWrapper createHttpServletRequestWrapper(HttpServletRequest request, String uri, StringBuffer url) {

        return new HttpServletRequestWrapper(request) {

            @Override
            public String getRequestURI() {
                return uri;
            }

            @Override
            public StringBuffer getRequestURL() {
                return url;
            }

            @Override
            public Object getAttribute(String name) {
                if(WebUtils.INCLUDE_SERVLET_PATH_ATTRIBUTE.equals(name))
                    return uri;
                return super.getAttribute(name);
            }

        };

    }
}
