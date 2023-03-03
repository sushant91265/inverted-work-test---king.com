package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import test.WorkerApi.Priority;

public class WorkerImplTest {

    @Test
    public void testWorkersWorkInOrder() throws InterruptedException {
        StringBuilder log = new StringBuilder();

        WorkerImpl worker = new WorkerImpl();

        worker.addJob(Priority.LOW, () -> log.append("1"));
        worker.addJob(Priority.HIGH, () -> log.append("2"));
        worker.addJob(Priority.HIGH, () -> log.append("3"));
        worker.addJob(Priority.LOW, () -> log.append("4"));

        worker.startWorkers(1);

        Thread.sleep(100);

        assertEquals("2314", log.toString());

        worker.stopWorkers();
    }
    
    @Test
    public void testConcurrency() throws InterruptedException {
        WorkerImpl worker = new WorkerImpl();
        AtomicInteger counter = new AtomicInteger(0);
        //System.out.println(Thread.currentThread().getName() + " " + LocalDateTime.now());
        Callable<Void> sleep = () -> {
            try {
                Thread.sleep(100);
                counter.incrementAndGet();
            } catch (InterruptedException e) {}
            return null;
        };
        
        worker.addJob(Priority.LOW, sleep);
        worker.addJob(Priority.LOW, sleep);
        worker.addJob(Priority.LOW, sleep);
        worker.addJob(Priority.LOW, sleep);

        worker.startWorkers(2);

        Thread.sleep(150);
        assertEquals(2, counter.get());

        Thread.sleep(100);
        assertEquals(4, counter.get());
        
        worker.stopWorkers();
    }
}
