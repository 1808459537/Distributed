package com.Paxos;

import java.util.*;

public class Paxos {
    // 提案类，包含提案编号和提案值
    private static class Proposal {
        int proposalNumber; // 提案编号
        String value; // 提案值

        Proposal(int proposalNumber, String value) {
            this.proposalNumber = proposalNumber;
            this.value = value;
        }
    }

    // 接受者类，记录承诺的提案编号和已接受的提案
    private static class Acceptor {
        int promisedProposal = -1; // 承诺的提案编号
        Proposal acceptedProposal = null; // 已接受的提案

        // 准备阶段方法
        public synchronized boolean prepare(int proposalNumber) {
            if (proposalNumber > promisedProposal) {
                promisedProposal = proposalNumber; // 更新承诺的提案编号
                System.out.println("接受者承诺提案编号 " + proposalNumber);
                return true;
            }
            return false;
        }

        // 接受阶段方法
        public synchronized boolean accept(Proposal proposal) {
            if (proposal.proposalNumber >= promisedProposal) {
                acceptedProposal = proposal; // 接受提案
                System.out.println("接受者接受提案编号 " + proposal.proposalNumber + " 提案值 " + proposal.value);
                return true;
            }
            return false;
        }


    }

    // 提议者类，包含提案编号、提案值和接受者列表
    private static class Proposer {
        int proposalNumber = 0; // 提案编号
        String value; // 提案值
        List<Acceptor> acceptors; // 接受者列表

        Proposer(String value, List<Acceptor> acceptors) {
            this.value = value;
            this.acceptors = acceptors;
        }

        // 提议方法
        public void propose() {
            proposalNumber++; // 增加提案编号
            int promises = 0;

            // 准备阶段
            for (Acceptor acceptor : acceptors) {
                if (acceptor.prepare(proposalNumber)) {
                    promises++;
                }
            }

            // 如果获得多数接受者的承诺
            if (promises > acceptors.size() / 2) {
                int accepts = 0;

                // 提案阶段
                Proposal proposal = new Proposal(proposalNumber, value);
                for (Acceptor acceptor : acceptors) {
                    if (acceptor.accept(proposal)) {
                        accepts++;
                    }
                }

                // 如果获得多数接受者的接受
                if (accepts > acceptors.size() / 2) {
                    System.out.println("提案被接受: " + value);
                } else {
                    System.out.println("提案被拒绝");
                }
            } else {
                System.out.println("准备阶段提案被拒绝");
            }
        }
    }

    // 主方法，模拟 Paxos 算法
    public static void main(String[] args) {
        // 创建三个接受者
        List<Acceptor> acceptors = Arrays.asList(new Acceptor(), new Acceptor(), new Acceptor());
        // 创建一个提议者，提案值为 "Value1"
        Proposer proposer = new Proposer("Value1", acceptors);
        // 提议者发起提案
        proposer.propose();
    }
}
