package com.raft;

public enum RaftState {
    FOLLOWER,   // 跟随者
    CANDIDATE,  // 候选人
    LEADER;     // 领导者
}
