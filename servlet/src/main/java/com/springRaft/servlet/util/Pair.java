package com.springRaft.servlet.util;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public record Pair<F,S>(F first, S second) {}