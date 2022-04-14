import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

class Problem1 {

    static List<Integer> unsortedValues;
    static AtomicInteger rmvPtr = new AtomicInteger();
    static AtomicInteger addPtr   = new AtomicInteger();
    static AtomicInteger cnt   = new AtomicInteger();
    // static boolean finished = false;
    static int numPresents = 500000;

    static final int numThreads = 8;
    static OptimisticList optimisticList = new OptimisticList();


    public static void main(String [] args) {
        unsortedValues = new ArrayList<Integer>(10);
        for (int i = 0; i < numPresents; i++)
            unsortedValues.add(i);
        
        Collections.shuffle(unsortedValues);

        // start timer
		Instant start = Instant.now();

        Thread threads[] = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(new Servant(i));
            threads[i].start();
        }

        for (Thread t : threads)
            try {
                t.join();
            } catch (Exception e) {};
        System.out.println("final amount of inserts and deletions: " + cnt);
        assert cnt.get() == numPresents;
        assert optimisticList.size() == 0;

        // end timer
	    Instant finish = Instant.now();

	    long duration = Duration.between(start, finish).toMillis();
        System.out.println("the execution time was " + duration + " milliseconds");
        System.out.println(Servant.fails.get() + " deletion retries");
    }



    static class Servant implements Runnable {
        static AtomicInteger fails = new AtomicInteger();
        int id;

        Servant(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            while (true) {
                int index = addPtr.getAndIncrement();
                if (index >= numPresents) break;
                int value = unsortedValues.get(index);
                if (!optimisticList.add(value))
                    throw new Error("Failed to add an item");
                // System.out.println("thread " + id + " added " + value);
                
                index = rmvPtr.getAndIncrement();
                if (index >= numPresents) break;
                value = unsortedValues.get(index);
                while (!optimisticList.remove(value)) {
                    fails.getAndIncrement();
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                cnt.getAndIncrement();
            }
            // System.out.println("thread " + id + " ran");
        }
    }

}