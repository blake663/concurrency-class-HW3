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

As for the problem of having fewer cards than the original number of gifts, it's hard to say without knowing the exact algorithm used, but I suspect it would be from one thread inserting a gift before a particular item while another thread deletes that item and sets the old predecessor to curr.next, skipping the newly added item. 

## Problem 2

For this problem I wasn't sure if we were allowed to use some kind of centralized synchronization, for example, having all threads wait on an object and have the main function notifyAll(). So I made the threads periodically sleep for a small random length of time and then wake to check if they were in a new minute yet. They also throw an error if they miss a minute. 

My approach for finding the interval with the largest difference used two arrays of minimum and maximum inputs so far, respectively, three variables for the start, end, and difference, and three locks: one for each array and one for the variables. First it separately minimizes the current minimum for that minute with the thread's input for that minute, using its lock.
Then it maximizes the current maximum for that minute with the thread's input for that minute, using its lock. Then without a lock it iterates through the possible starting times for an interval with a length of 10 or less, with the ending time being the current minute. In other words, for current time i and every previous time j, with j >= 0, i-10 < j <= i, we consider |max[i]-min[j]| and |max[j]-min[i]| . If the best one of these provide an improvement, we lock and update our interval data. 

For the top 5 highest and lowest temperatures, I maintain an array of up to 5 elements, the highest or lowest unique values respectively so far. Modification of these requires acquiring their respective lock first. 

I think the reasoning behind these approaches is sound, but I wasn't able to rigorously test them. By comparing the reports with the data from each sensor, I've become pretty convinced that there are no obvious flaws. 

Both programs can be invoked by calling their main function. 