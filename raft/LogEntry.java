package com.raft;

public class LogEntry {
    int term;
    String command;
    public LogEntry(int term, String command) {
        this.term = term; // 初始化任期
        this.command = command; // 初始化命令
    }
}
