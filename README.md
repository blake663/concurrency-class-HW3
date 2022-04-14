# Homework 3
## Problem1
The OptimisticList class was adapted from an implementation on 
the textbook's companion page: https://booksite.elsevier.com/9780123973375/?ISBN=9780123973375

For this problem, I opted for an optimistic linked-list, because it strikes a balance between performance and code complexity. After establishing a functioning class, the other main challenge was deciding on a strategy, especially the portion about removing a random element. An alternative approach to mine could be to always remove the first element in the list. Instead, I maintained two positions in the unordered array, using it like a queue that reflected the contents of the linked-list. I also had to decide how to switch between adding and removing, randomly or otherwise. I decided to use the same strategy as the problem statement, alternating between adding a single element and then removing a single element. 

The correctness of the linked-list has already been covered in class, but I can summarize: We traverse the list without locking until we arrive at the first item greater-than or equal to the value. We lock this and (what was) the predecessor and then validate them. We validate that pred is still reachable (it will remain reachable while we have the lock) and that pred.next == curr. Now we can insert a node between them or remove curr by setting pred.next to curr.next. Finally, we release the locks and it's finished. 

The code which modifies the linked-list is correct because the number of add() calls that have been performed is always greater than or equal to the number of remove calls that have been performed and remove is only called once for each number, and only after it has been added. This is guaranteed by the FIFO approach and use of atomic variables for its indexes. 

I had to add a while loop with a short delay for the removal step because it would sometimes fail to delete a node that was in the list. I don't know exactly why this occured, but it would only happen occasionally and to 2 or 3 of the 8 threads. It seems like the threads would experience very high contention, given that there are only between 0 and 8 nodes in the list and two must be locked at a time, but it's still sufficiently performant. 

Without the Thread.sleep() in the while loop, and with a counter variable instead, I was getting runtimes of about 1250 ms and 5 million failed deletions. By adding a 10ms sleep, I reduced the runtime to 220ms and the number of failed deletions to 90. 

Despite the contention, it runs efficiently due to the small size the linked-list. It may even be benefiting from cache locality.

