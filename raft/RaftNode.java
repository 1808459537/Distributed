package com.raft;
import java.util.*;
import java.util.concurrent.*;

public class RaftNode {
    private int id; // 节点ID
    public RaftState state; // 节点状态
    public int currentTerm; // 当前任期
    private int votedFor; // 当前任期内投票给的候选人ID
    private List<LogEntry> log; // 日志条目列表
    private int commitIndex; // 已提交的最高日志条目索引
    private int lastApplied; // 最后应用到状态机的日志条目索引
    public Map<Integer, RaftNode> peers; // 集群中的其他节点

    private int electionTimeout; // 选举超时时间
    private int heartbeatInterval; // 心跳间隔时间
    private ScheduledExecutorService scheduler; // 调度器

    private ScheduledFuture<?> electionTimeoutTask; // 选举超时任务

    public RaftNode(int id, List<RaftNode> peers) {
        this.id = id; // 初始化节点ID
        this.state = RaftState.FOLLOWER; // 初始状态为跟随者
        this.currentTerm = 0; // 初始任期为0
        this.votedFor = -1; // 初始未投票
        this.log = new ArrayList<>(); // 初始化日志条目列表
        this.commitIndex = 0; // 初始化提交索引为0
        this.lastApplied = 0; // 初始化最后应用索引为0
        this.peers = new ConcurrentHashMap<>(); // 初始化其他节点的映射
        for (RaftNode peer : peers) {
            this.peers.put(peer.getId(), peer); // 将其他节点加入映射
        }

        resetElectionTimeout(); // 设置选举超时时间
        this.heartbeatInterval = 50; // 设置心跳间隔时间
        this.scheduler = Executors.newScheduledThreadPool(1); // 创建调度器
        startElectionTimer(); // 启动选举计时器
    }

    public int getId() {
        return id; // 获取节点ID
    }

    private void resetElectionTimeout() {
        this.electionTimeout = new Random().nextInt(150) + 150; // 随机生成选举超时时间
    }

    private void restartElectionTimer() {
        if (electionTimeoutTask != null) {
            electionTimeoutTask.cancel(true); // 取消之前的选举超时任务
        }
        electionTimeoutTask = scheduler.schedule(() -> {
            if (state != RaftState.LEADER) {
                startElection(); // 如果当前不是领导者，启动选举
            }
        }, electionTimeout, TimeUnit.MILLISECONDS); // 定时检查是否需要启动选举
    }

    public synchronized void startElection() {
        System.out.println("节点 " + id + " 启动选举。");
        state = RaftState.CANDIDATE; // 将状态设置为候选人
        currentTerm++; // 增加当前任期
        votedFor = id; // 投票给自己
        int votesReceived = 1; // 初始化收到的选票数为1（自己的票）

        for (RaftNode peer : peers.values()) {
            boolean voteGranted = peer.requestVote(currentTerm, id, log.size() - 1, log.isEmpty() ? 0 : log.get(log.size() - 1).term); // 请求投票
            if (voteGranted) {
                votesReceived++; // 如果获得投票，增加票数
            }
        }

        if (votesReceived > peers.size() / 2) {
            System.out.println("节点 " + id + " 成为领导者。");
            becomeLeader(); // 如果获得多数票，成为领导者
        } else {
            System.out.println("节点 " + id + " 选举失败，重新启动选举计时器。");
            resetElectionTimeout(); // 重置选举超时时间
            restartElectionTimer(); // 重新启动选举计时器
        }
    }

    public synchronized boolean requestVote(int term, int candidateId, int lastLogIndex, int lastLogTerm) {
        if (term > currentTerm) {
            currentTerm = term; // 更新当前任期
            state = RaftState.FOLLOWER; // 重置状态为跟随者
            votedFor = -1; // 重置投票记录
        }

        boolean voteGranted = (votedFor == -1 || votedFor == candidateId) &&
                (lastLogTerm > (log.isEmpty() ? 0 : log.get(log.size() - 1).term) ||
                        (lastLogTerm == (log.isEmpty() ? 0 : log.get(log.size() - 1).term) && lastLogIndex >= log.size() - 1)); // 判断是否可以投票

        if (voteGranted) {
            votedFor = candidateId; // 记录投票给的候选人
           // System.out.println("节点 " + id + " 投票给候选人 " + candidateId + "。");
        }

        return voteGranted; // 返回是否投票
    }

    public synchronized void appendEntries(int term, int leaderId, int prevLogIndex, int prevLogTerm, List<LogEntry> entries, int leaderCommit) {
        if (term >= currentTerm) {
            currentTerm = term; // 更新当前任期
            state = RaftState.FOLLOWER; // 重置状态为跟随者
            votedFor = -1; // 重置投票记录
            //System.out.println("节点 " + id + " 收到来自领导者 " + leaderId + " 的心跳消息。");
            resetElectionTimeout(); // 重置选举超时时间
            restartElectionTimer(); // 重新启动选举计时器
            // 日志追加逻辑
        }
        // 其他应用日志和更新提交索引的逻辑
    }

    private void becomeLeader() {
        state = RaftState.LEADER; // 将状态设置为领导者
        if (electionTimeoutTask != null) {
            electionTimeoutTask.cancel(true); // 取消选举超时任务
        }
        scheduler.scheduleAtFixedRate(() -> {
            for (RaftNode peer : peers.values()) {
                peer.appendEntries(currentTerm, id, log.size() - 1, log.isEmpty() ? 0 : log.get(log.size() - 1).term, new ArrayList<>(), commitIndex); // 发送心跳消息
            }
            //System.out.println("领导者 " + id + " 发送心跳消息。");
        }, 0, heartbeatInterval, TimeUnit.MILLISECONDS); // 定期发送心跳
    }

    private void startElectionTimer() {
        restartElectionTimer(); // 启动或重新启动选举计时器
    }
}



