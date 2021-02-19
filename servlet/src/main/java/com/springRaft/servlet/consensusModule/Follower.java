package com.springRaft.servlet.consensusModule;

import com.springRaft.servlet.config.RaftProperties;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class Follower implements RaftState {

    private final RaftProperties raftProperties;

    /* --------------------------------------------------- */

    public Follower(RaftProperties raftProperties) {
        this.raftProperties = raftProperties;
    }

    /* --------------------------------------------------- */

    @Override
    public void appendEntries() {

    }

    @Override
    public void requestVote() {

    }

    @SneakyThrows
    @Override
    public void work() {
        long millis = this.raftProperties.getElectionTimeout().toMillis();
        System.out.println(millis);
        Thread.sleep(millis);
        System.out.println("WORKED");
    }

}
