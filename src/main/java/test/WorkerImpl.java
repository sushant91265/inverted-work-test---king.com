package test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class WorkerImpl implements WorkerApi {

    private final Map<Priority, List<Job<?>>> jobMap = new HashMap<>();
    private volatile List<Thread> workers;

    //1 []
    //2 []
    @Override
    public <T> void addJob(Priority priority, Callable<T> call) {
        // synchronization or volatile var
        // concurrent hashmap with computerifAbsent
        List<Job<?>> jobList = jobMap.get(priority);
        if (jobList == null) {
            jobList = new ArrayList<>();
            jobMap.put(priority, jobList);
        }
        jobList.add(new Job<>(priority, call));
    }

    @Override
    public void startWorkers(int workerCount) {
        // error if called multiple times
        // fetch the jobs from the map using 3 threads
        // based on the priority we create new threads and execute the jobs
        workers = new ArrayList<>();

        for (int i = 0; i < workerCount; i++) {
            Thread t = new Thread(this::workerLoop);
            t.start();
            workers.add(t);
        }
    }

    @Override
    public void stopWorkers() {
        // stoprequested
        workers.forEach(Thread::stop);
    }

    private void workerLoop() {
        while (true) {
            doWork(Priority.HIGH);
            doWork(Priority.MEDIUM);
            doWork(Priority.LOW);
        }
    }

    private void doWork(Priority prio) {
            //System.out.println(jobMap+" "+prio);
            List<Job<?>> jobs = jobMap.remove(prio);
            if (jobs == null || jobs.isEmpty()) {
                return;
            }

            Job<?> currentJob = jobs.get(0);
            jobs.remove(0);
            jobMap.put(prio, jobs);
            currentJob.invoke();
    }

    class Job<T> {
        public final Priority priority;
        public final Callable<T> call;

        public Job(Priority priority, Callable<T> call) {
            this.priority = priority;
            this.call = call;
        }

        public T invoke() {
            try {
                return call.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
