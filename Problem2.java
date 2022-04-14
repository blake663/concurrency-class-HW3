import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Problem2 {
    final static int numSensors = 8;
    int input[][] = new int[60][numSensors];
    // int data[][]  = new int[60][numSensors];
    Thread threads[];
    Random ran = new Random();
    double minuteLength = 200;
    Instant startTime;
    List<Integer> minuteMin = new ArrayList<>();
    List<Integer> minuteMax = new ArrayList<>();
    List<Integer> min5 = new ArrayList<>();
    List<Integer> max5 = new ArrayList<>();
    Lock minLock = new ReentrantLock();
    Lock maxLock = new ReentrantLock();
    Lock min5Lock = new ReentrantLock();
    Lock max5Lock = new ReentrantLock();

    int interval[] = new int[2];
    int intervalDiff = 0;
    Lock intervaLock = new ReentrantLock();

    // Generate one hour of data 
    void generateInput() {
        for (int hour = 0; hour < 60; hour++)
            for (int i = 0; i < numSensors; i++)
                input[hour][i] = ran.nextInt(171) - 100;
        for (int i = 0; i < 60; i++) {
            minuteMin.add(1000);
            minuteMax.add(-1000);
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        Problem2 p = new Problem2();
        p.generateInput();

        p.threads = new Thread[numSensors];
        for (int i = 0; i < numSensors; i++)
            p.threads[i] = new Thread(p.new Sensor(i, p));

        p.startTime = Instant.now();
        for (Thread th : p.threads) th.start();
        for (Thread th : p.threads) th.join();

        p.generateReport();
    }

    void generateReport() {
        System.out.print("The " + min5.size() + " lowest unique temperatures: ");
        min5.forEach(x -> System.out.print(x + " "));
        System.out.println();

        System.out.print("The " + max5.size() + " highest unique temperatures: ");
        max5.forEach(x -> System.out.print(x + " "));
        System.out.println();

        System.out.println("the largest difference of " + intervalDiff + 
        " occurred from " + interval[0] + " to " + interval[1]);
    }

    class Sensor implements Runnable {
        int id;
        // the most recent minute the sensor turned on
        int previousMinute = -1;
        Problem2 p;

        Sensor(int id, Problem2 p) {
            this.id = id;
            this.p = p;
        }

        @Override
        public void run() {
            int t = getCurrentMinute();
            do {
                if (t != previousMinute+1)
                    throw new Error("thread " + id + " skipped minute #" + previousMinute+1);
                
                System.out.println("at time " + t + " sensor " + id + " read " + p.input[t][id]);
                processInput(p.input[t][id], t);

                previousMinute++;

                try {
                    do {
                        int busyTime = p.ran.nextInt(50);
                        Thread.sleep(busyTime);
                        t = getCurrentMinute();
                    } while (previousMinute + 1 > t);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            } while (t < 60);

        }

        // add the temperature data to the data structures
        void processInput(int temp, int minute) {
            // add it to the minimum 5 if it belongs
            try {
                min5Lock.lock();
                int size = min5.size();
                if (!min5.contains(temp)) {
                    int i;
                    for (i = 0; i < size; i++)
                        if (min5.get(i) > temp) break;
                    min5.add(i, temp);
                    if (size >= 5)
                        min5.remove(size);
                }
            } finally {
                min5Lock.unlock();
            }

            // add it to the maximum 5 if it belongs
            try {
                max5Lock.lock();
                int size = max5.size();
                if (!max5.contains(temp)) {
                    int i;
                    for (i = 0; i < size; i++)
                        if (max5.get(i) < temp) break;
                    max5.add(i, temp);
                    if (size >= 5)
                        max5.remove(size);
                }
            } finally {
                max5Lock.unlock();
            }

            // contribute to finding the maximum for this minute
            try {
                maxLock.lock();
                minuteMax.set(minute, Math.max(minuteMax.get(minute), temp));
            } finally {
                maxLock.unlock();
            }

            // contribute to finding the minimum for this minute
            try {
                minLock.lock();
                minuteMin.set(minute, Math.min(minuteMin.get(minute), temp));
            } finally {
                minLock.unlock();
            }

            // find the best interval ending on the current minute so far.
            int best = 0, a=0, b=0;
            for (int i = minute; i >= 0 && i > minute - 10; i--) {
                int diff = Math.max(
                    Math.abs(
                        p.minuteMax.get(minute) -
                        p.minuteMin.get(i)),
                    Math.abs(
                        p.minuteMin.get(minute) -
                        p.minuteMax.get(i)));
                if (diff > best) {
                    a = i;
                    b = minute;
                    best = diff;
                }
            }

            if (best > intervalDiff) {
                try {
                    intervaLock.lock();
                    interval[0] = a;
                    interval[1] = b;
                    intervalDiff = best;
                } finally {
                    intervaLock.unlock();
                }
            }
        }

        // gets the current minute as an integer [0,60)
        int getCurrentMinute() {
            Instant now = Instant.now();
            long elapsed = Duration.between(p.startTime, now).toMillis();
            int virtualTime = (int) Math.floor(elapsed / p.minuteLength);
            return virtualTime;
        }
    }
}
