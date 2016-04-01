package com.company;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class PhaserImpl implements SquareSum{

    public static void main(String[] args) throws InterruptedException {

        int [] array = new int[] {1, 1, 1 , 1, 2, 1, 1, 1};
        final int numberOfThreads = 3;
        PhaserImpl arraySumSquare = new PhaserImpl();
        arraySumSquare.getSquareSum(array, numberOfThreads);
        Thread.sleep(3000);
        System.out.println("Total result is" + PhaserImpl.result);
    }


    final Phaser phaser = new Phaser();
    List<CalculateThread> taskList = new ArrayList<>();
    static volatile long result = 0;

    private int [] splitToArrays(int [] values, int indexStart, int indexStop) {
        int [] result = Arrays.copyOfRange(values, indexStart, indexStop);
        return result;
    }


    public void getSquareSum(int[] values, int numberOfThreads) throws InterruptedException {

        fillRunableList(values, numberOfThreads);

        System.out.println("Threads are starting");

        phaser.register();

        for (CalculateThread task : taskList) {
            phaser.register();
            new Thread() {
                public void run() {
                    System.out.println(Thread.currentThread().getName() + " start running");
                    task.run();
                    System.out.println(Thread.currentThread().getName() + " arrive and await. Current result " + task.getCurrentResult());
                    phaser.arriveAndAwaitAdvance();
                    result += task.getCurrentResult();
                }
            }.start();
        }
        phaser.arriveAndDeregister();

    }

    private void fillRunableList(int[] values, int numberOfThreads) {
        int step = values.length / numberOfThreads;
        int startIndex = 0;
        int stopIndex = step;
        int tail = values.length % numberOfThreads;
        int amountOfRunnables = tail == 0 ? numberOfThreads : numberOfThreads-1;
        int i;


        for (i = 0 ; i < amountOfRunnables ; i++) {
            int[] newArray = splitToArrays(values, startIndex, stopIndex);
            taskList.add(new CalculateThread(newArray));
            System.out.println(i + " task added");
            startIndex += step;
            stopIndex += step;
        }

        if (tail != 0) {
            stopIndex += tail;
            int[] newArray = splitToArrays(values, startIndex, stopIndex);
            taskList.add(new CalculateThread(newArray));
            System.out.println(i + " task added");
        }
    }

    public class CalculateThread implements Runnable {

        private int[] array;
        private long currentResult;


        public CalculateThread(int[] array) {
            this.array = array;
        }

        @Override
        public void run() {
            for (int i = 0; i < array.length ; i++) {
                int square = array[i] * array[i];
               currentResult += square;
            }
        }

        public int[] getArray() {
            return array;
        }

        public long getCurrentResult() {
            return currentResult;
        }
    }
}
