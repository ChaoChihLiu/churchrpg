package com.cpbpc;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ProducerConsumerExample {
    public static void main(String[] args) {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(2); // Create a bounded queue with a maximum size

        // Create a producer and a consumer
        Thread producerThread = new Thread(new Producer(queue));
        Thread consumerThread = new Thread(new Consumer(queue));

        // Start the producer and consumer threads
        producerThread.start();
        consumerThread.start();
    }

    static class Producer implements Runnable {
        private final BlockingQueue<Integer> queue;

        Producer(BlockingQueue<Integer> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            try {
                int i = 0;
                while (true) {
                    i++;
                    System.out.println("Producing: " + (i));
                    queue.put(i); // Put an item into the queue
//                    Thread.sleep(100); // Simulate some work
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class Consumer implements Runnable {
        private final BlockingQueue<Integer> queue;

        Consumer(BlockingQueue<Integer> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Thread.sleep(2000); // Simulate some work
                    int value = queue.take(); // Take an item from the queue (blocks if the queue is empty)
                    System.out.println("Consuming: " + value);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
