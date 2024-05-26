package com.raft;

import java.util.ArrayList;
import java.util.List;

public class test {
    public static void main(String[] args) {
        List<RaftNode> nodes = new ArrayList<>(); // 创建节点列表
        for (int i = 0; i < 5; i++) {
            nodes.add(new RaftNode(i, new ArrayList<>())); // 创建5个节点
        }

        // 模拟分布式服务发现
        for (RaftNode node : nodes) {
            for (RaftNode peer : nodes) {
                if (node.getId() != peer.getId()) {
                    node.peers.put(peer.getId(), peer); // 将其他节点加入每个节点的peers映射
                }
            }
        }

        try {
            Thread.sleep(5000); // 暂停5秒
        } catch (InterruptedException e) {
            e.printStackTrace(); // 打印异常
        }

        // 显示每个节点的状态
        for (RaftNode node : nodes) {
            System.out.println("节点 " + node.getId() + " 处于状态 " + node.state + " 任期 " + node.currentTerm); // 打印节点状态和任期
        }
    }

}
