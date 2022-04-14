// package lists;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
/**
 * Optimistic List implementation.
 */
public class OptimisticList {
  /**
   * First list entry
   */
  private Entry head;
  /**
   * Constructor
   */
  public OptimisticList() {
    this.head  = new Entry(Integer.MIN_VALUE);
    this.head.next = new Entry(Integer.MAX_VALUE);
  }
  /**
   * Add an element.
   * @param item element to add
   * @return true iff element was not there already
   */
  public boolean add(int value) {
    while (true) {
      Entry pred = this.head;
      Entry curr = pred.next;
      while (curr.value < value) {
        pred = curr; curr = curr.next;
      }
      pred.lock(); curr.lock();
      try {
        if (validate(pred, curr)) {
          if (curr.value == value) { // present
            return false;
          } else {               // not present
            Entry entry = new Entry(value);
            entry.next = curr;
            pred.next = entry;
            return true;
          }
        }
      } finally {                // always unlock
        pred.unlock(); curr.unlock();
      }
    }
  }
  /**
   * Remove an element.
   * @param item element to remove
   * @return true iff element was present
   */
  public boolean remove(int value) {
    while (true) {
      Entry pred = this.head;
      Entry curr = pred.next;
      while (curr.value < value) {
        pred = curr; curr = curr.next;
      }
      pred.lock(); curr.lock();
      try {
        if (validate(pred, curr)) {
          if (curr.value == value) { // present in list
            pred.next = curr.next;
            return true;
          } else {               // not present in list
            return false;
          }
        }
      } finally {                // always unlock
        pred.unlock(); curr.unlock();
      }
    }
  }
  /**
   * Test whether element is present
   * @param item element to test
   * @return true iff element is present
   */
  public boolean contains(int value) {
    while (true) {
      Entry pred = this.head; // sentinel node;
      Entry curr = pred.next;
      while (curr.value < value) {
        pred = curr; curr = curr.next;
      }
      try {
        pred.lock(); curr.lock();
        if (validate(pred, curr)) {
          return (curr.value == value);
        }
      } finally {                // always unlock
        pred.unlock(); curr.unlock();
      }
    }
  }
  /**
   * return number of nodes in list
   * (for testing purposes only)
   * @return size as int
   */
  public int size() {
    Entry entry = head.next;
    int count = 0;
    while (entry.value < Integer.MAX_VALUE) {
      entry = entry.next;
      count++;
    }
    return count;
  }
  /**
     * Check that prev and curr are still in list and adjacent
     * @param pred predecessor node
     * @param curr current node
     * @return whether predecessor and current have changed
     */
  private boolean validate(Entry pred, Entry curr) {
    Entry entry = head;
    while (entry.value <= pred.value) {
      if (entry == pred)
        return pred.next == curr;
      entry = entry.next;
    }
    return false;
  }
  /**
   * list entry
   */
  private class Entry {
    /**
     * serves as key and value
     */
    int value;
    /**
     * next entry in list
     */
    Entry next;
    /**
     * Synchronizes entry.
     */
    Lock lock;
    /**
     * value has concrete type int
     */
    Entry(int value) {
      this.value = value;
      lock = new ReentrantLock();
    }
    /**
     * Lock entry
     */
    void lock() {lock.lock();}
    /**
     * Unlock entry
     */
    void unlock() {lock.unlock();}
  }
}
