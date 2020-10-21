package com.BPlusTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BPlusTree {
    private final int SplitThres;
    private final int MergeThres;

    Node root;
    int nodeCount;

    public BPlusTree() {
        this.SplitThres = 3;
        this.MergeThres = 2;
        nodeCount = 0;
        root = new LeafNode();
    }

    public BPlusTree(int SplitThres, int MergeThres) throws Exception {
        this.SplitThres = SplitThres;
        this.MergeThres = MergeThres;
        nodeCount = 0;
        root = new LeafNode();

        if (SplitThres <= MergeThres) {
            throw new Exception("SplitThreshold is less than MergeThreshold");
        }
    }

    public void insert(String key, String value) {
        log("*** insert: " + key);
        root.insertValue(key, value);
        root = root.parent == null ? root : root.parent;
    }

    public String search(String key) {
        log("?? search: " + key);
        return root.getValue(key);
    }

    public void log(String msg) {
        System.out.println(msg);
    }

    private abstract class Node {
        List<String> keys;
        InternalNode parent;
        boolean isLeafNode;
        int nodeId;

        Node() {
            nodeId = nodeCount++;
            keys = new ArrayList<>(); // may use TreeSet for performance of insert and delete.
            log("Create node: #" + nodeId);
        }

        void setParent(InternalNode parent) {
            this.parent = parent;
        }

        Node getParent() {
            return parent;
        }

        abstract String getValue(String key);

        abstract void insertValue(String key, String value);

        abstract void deleteValue(String key);

        boolean isCrowd() {
            return keys != null && keys.size() > SplitThres;
        }

        boolean isSparse() {
            return keys == null || keys.size() < MergeThres;
        }

        boolean isLendable() {
            return keys != null && keys.size() > MergeThres;
        }

        boolean isMergable(int otherNodeSize) {
            return keys == null || (this.keys.size() + otherNodeSize) <= SplitThres;
        }

        abstract void merge(Node sibling);

        abstract Node split();

        void splitIfCrowd() {
            log("check to split");
            if (isCrowd()) {
                Node sibling = split();
                String newKey = sibling.keys.get(0);
                if (parent == null) {
                    log("create parent node");
                    parent = new InternalNode();
                    parent.addFirstChild(this);
                    parent.keys.add(newKey);
                    parent.children.add(sibling);
                    if (!isLeafNode) {
                        log("Raise first key in sibling: " + newKey);
                        sibling.keys.remove(0);
                        ((InternalNode) sibling).children.remove(0);
                        log("parent keys: " + parent.keys.toString() + " children size=" + parent.children.size());
                        log("first child keys: " + keys.toString());
                        log("second child keys: " + sibling.keys.toString());
                    }
                } else {
                    parent.insertChild(newKey, sibling);
                    log("parent keys: " + parent.keys.toString() + " children size=" + parent.children.size());
                }
                sibling.parent = parent;
            }
        }

        abstract void mergeIfSparse();

        List<String> getKeys() {
            return keys;
        }
    }

    private class InternalNode extends Node {
        List<Node> children;

        InternalNode() {
            super();
            parent = null;
            isLeafNode = false;
            children = new ArrayList<>();
        }

        @Override
        String getValue(String key) {
            log("get child in internal node");
            return getChild(key).getValue(key);
        }

        @Override
        void insertValue(String key, String value) {
            log("internal insertValue key=" + key);
            Node child = getChild(key);
            child.insertValue(key, value);
            splitIfCrowd();
        }

        @Override
        void deleteValue(String key) {
            log("internal deleteValue key=" + key);
            Node child = getChild(key);
            child.deleteValue(key);
            mergeIfSparse();
        }

        @Override
        void merge(Node sibling) {

        }

        @Override
        Node split() {
            log("begin internal split");
            log("before split keys: " + keys.toString() + " children size=" + children.size());
            InternalNode sibling = new InternalNode();
            int numOfKeys = this.keys.size();
            int copyStartIndex = MergeThres;
            int numOfChildren = this.children.size();
            log("internal Split at copyStartIndex=" + copyStartIndex + " numOfKeys=" + numOfKeys);
            sibling.keys.addAll(keys.subList(copyStartIndex, numOfKeys));
            sibling.children.addAll(children.subList(copyStartIndex, numOfChildren));
            for (Node child: children.subList(copyStartIndex + 1, numOfChildren)) {
                child.parent = sibling;
            }
            keys.subList(copyStartIndex, numOfKeys).clear();
            children.subList(copyStartIndex + 1, numOfChildren).clear();
            log("after split first keys: " + keys.toString() + " children size=" + children.size());
            log("after split second keys: " + sibling.keys.toString() + " children size=" + sibling.children.size());
            return sibling;
        }

        @Override
        void mergeIfSparse() {

        }

        Node getChild(String key) {
            int index = Collections.binarySearch(keys, key);
            log("getChild index=" + index + " by key=" + key);
            log("keys:" + keys.toString());
            return index >= 0 ? children.get(index + 1) : children.get(-index - 1);
        }

        void insertChild(String key, Node child) {
            int index = Collections.binarySearch(keys, key);
            log("internal insertChild index=" + index);
            if (index >= 0) {
                children.set(index, child);
            } else {
                index = -index - 1;
                keys.add(index, key);
                children.add(index + 1, child);
            }
        }

        void addFirstChild(Node child) {
            children.add(0, child);
        }

        void deleteChild(String key) {

        }
    }

    private class LeafNode extends Node {
        List<String> values;
        LeafNode prevNode, nextNode;

        LeafNode() {
            super();
            isLeafNode = true;
            prevNode = null;
            nextNode = null;
            values = new ArrayList<>();
        }

        @Override
        String getValue(String key) { // need UTF-8 comparator in HS
            int index = Collections.binarySearch(keys, key);
            log("getValue index=" + index + " by key=" + key);
            log("keys:" + keys.toString());
            return index >= 0 ? values.get(index) : null;
        }

        @Override
        void insertValue(String key, String value) {
            int index = Collections.binarySearch(keys, key);
            log("Leaf insertValue index=" + index + " by key=" + key);
            log("keys:" + keys.toString());
            if (index >= 0) {
                values.set(index, value);
            } else {
                index = -index - 1;
                keys.add(index, key);
                values.add(index, value);
                splitIfCrowd();
            }
        }

        @Override
        void deleteValue(String key) {
            int index = Collections.binarySearch(keys, key);
            if (index >= 0) {
                keys.remove(index);
                values.remove(index);
                mergeIfSparse();
            }
            // if not found, do nothing
        }

        @Override
        void merge(Node sibling) {
            // need to consider detailed cases
            String deleteKey = sibling.getKeys().get(0);
            if (this.keys.get(keys.size() - 1).compareTo(deleteKey) < 0) {
                this.keys.addAll(sibling.getKeys());
                this.values.addAll(sibling.getKeys());
                this.nextNode = ((LeafNode) sibling).nextNode;
            }
        }

        @Override
        Node split() {
            log("begin leaf split");
            log("before split keys: " + keys.toString());
            LeafNode sibling = new LeafNode();
            int numOfKeys = this.keys.size();
            int copyStartIndex = MergeThres;
            log("Leaf Split at copyStartIndex=" + copyStartIndex + " numOfKeys=" + numOfKeys);
            sibling.keys.addAll(keys.subList(copyStartIndex, numOfKeys));
            sibling.values.addAll(values.subList(copyStartIndex, numOfKeys));
            keys.subList(copyStartIndex, numOfKeys).clear();
            values.subList(copyStartIndex, numOfKeys).clear();
            sibling.setNextNode(this.nextNode);
            sibling.setPrevNode(this);
            this.nextNode = sibling;
            log("after split first keys: " + keys.toString());
            log("after split second keys: " + sibling.keys.toString());
            return sibling;
        }

        @Override
        void mergeIfSparse() {
            // Case II: if node has enough key, then do nothing further.
            if (this.isSparse()) {

                if (this.keys.isEmpty()) {
                    // Case I, III: leaf node is empty, borrow one key from sibling
                    if (this.nextNode.parent == this.parent && this.nextNode.isLendable()) {
                        String lendKey = this.nextNode.keys.remove(0);
                        String lendValue = this.nextNode.values.remove(0);
                        this.keys.add(lendKey);
                        this.values.add(lendValue);
                    }
                    // Case IV, V: leaf node empty, but not able to borrow key from sibling
                } else {
                    // Case I, III: leaf node is empty, borrow one key from sibling
                    // Case IV, V: leaf node empty, but not able to borrow key from sibling
                }
            }
        }

        Node getPrevNode() {
            return prevNode;
        }

        Node getNextNode() {
            return nextNode;
        }

        void setPrevNode(LeafNode prevNode) { // need to check the order of keys in prev or next Node
            this.prevNode = prevNode;
        }

        void setNextNode(LeafNode nextNode) {
            this.nextNode = nextNode;
        }
    }
}


