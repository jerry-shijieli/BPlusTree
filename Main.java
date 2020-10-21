package com.BPlusTree;

import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
	    // write your code here
//        System.out.println("B+ Tree");
//        BPlusTree bptree = new BPlusTree();
//
//        bptree.insert("abc", "abcVal");
//        String key = "abc";
//        log(key + ": " + bptree.search(key));
//        bptree.insert("1bc", "1bcVal");
//        key = "1bc";
//        log(key + ": " + bptree.search(key));
//        bptree.insert("aaa", "aaaVal");
//        key = "aaa";
//        log(key + ": " + bptree.search(key));
//        bptree.insert("xx", "xxVal");
//        key = "xx";
//        log(key + ": " + bptree.search(key));
//        bptree.insert("kk", "kkVal");
//        key = "kk";
//        log(key + ": " + bptree.search(key));
//        bptree.insert("456", "456Val");
//        key = "456";
//        log(key + ": " + bptree.search(key));
//        bptree.insert("8a/9/cc", "8a/9/ccVal");
//        key = "8a/9/cc";
//        log(key + ": " + bptree.search(key));
//
//        key = "aaa";
//        log(key + ": " + bptree.search(key));
//        key = "xx";
//        log(key + ": " + bptree.search(key));
//        key = "a";
//        log(key + ": " + bptree.search(key));
//        key = "8a/9/cc";
//        log(key + ": " + bptree.search(key));
//        key = "";
//        log(key + ": " + bptree.search(key));

        testBPlusTree(1000);

//        BPlusTree bptree = new BPlusTree(2, 1);
//        String[] keys = new String[]{"06", "16", "26", "36", "46", "56", "66"};
//        for (int i=0; i<keys.length; i++) {
//            bptree.insert(keys[i], keys[i]);
//            String res = bptree.search(keys[i]);
//            if (!keys[i].equals(res)) {
//                log("key=" + keys[i] + " got error search as res=" + res);
//            }
//        }

    }

    public static void log(String msg) {
        System.out.println(msg);
    }

    public static void testBPlusTree(int sizeOfData) throws Exception {
        sizeOfData = sizeOfData <= 0 ? 10 : sizeOfData;
        String[] keys = new String[sizeOfData];
        String[] values = new String[sizeOfData];
        Map<String, String> keyValMap = new HashMap<>();
        Random random = new Random();
        BPlusTree bptree = new BPlusTree(5, 3);
        for (int i=0; i<sizeOfData; i++) {
            keys[i] = generateRandomString(random.nextInt(10) + 1);
            values[i] = keys[i]; // generateRandomString(random.nextInt(10) + 1);
            log("###" + i + ": key=" + keys[i] + " val=" + values[i]);
            keyValMap.put(keys[i], values[i]);
            bptree.insert(keys[i], values[i]);
        }

        List<List<String>> errEntries = new LinkedList<>();
        log("\n>>> Verify results");
        for (int i=0; i<sizeOfData; i++) {
            log(">>");
            int index = random.nextInt(sizeOfData);
            String key = keys[index];
            String val = keyValMap.get(key);
            String res = bptree.search(key);
            if (val.equals(res)) {
                log("key=" + key + " -- val=" + val + " == res=" + res);
            } else {
                log("key=" + key + " -- val=" + val + " xxx res=" + res);
                errEntries.add(Arrays.asList(key, val, res));
            }
        }

        log("\nError entries:");
        for (List<String> err: errEntries) {
            log(err.toString());
        }
    }

    public static String generateRandomString(int strlen) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = strlen;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return generatedString;
    }
}
