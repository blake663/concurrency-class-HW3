import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;

class Problem1 {

    static List<Integer> unsortedValues;
    static AtomicInteger start = new AtomicInteger();
    static AtomicInteger end   = new AtomicInteger();
    static volatile AtomicInteger cnt   = new AtomicInteger();
    static boolean finished = false;
    static int numPresents = 5000;

    static final int numThreads = 8;
    static OptimisticList a = new OptimisticList();


    public static void main(String [] args) {
        unsortedValues = new ArrayList<Integer>(10);
        for (int i = 0; i < numPresents; i++)
            unsortedValues.add(i);
        
        Collections.shuffle(unsortedValues);
        // for (int i = 0; i < 10; i++)
        // System.out.println(values.get(i));

        Thread threads[] = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(new Servant(i));
            threads[i].start();
        }

        for (Thread t : threads)
            try {
                t.join();
            } catch (Exception e) {};
        System.out.println("final amt == " + cnt);

    }



    static class Servant implements Runnable {
        int id;

        Servant(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            while (true) {
                int index = start.getAndIncrement();
                if (index >= numPresents) break;
                int value = unsortedValues.get(index);
                a.add(value);
                // System.out.println("thread " + id + " added " + value);
                cnt.getAndIncrement();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println("thread " + id + " ran");
            return;
        }
    }

}