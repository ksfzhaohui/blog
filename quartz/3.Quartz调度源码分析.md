## **系列文章**

[Spring整合Quartz分布式调度](https://my.oschina.net/OutOfMemory/blog/1790200)

[Quartz数据库表分析](https://my.oschina.net/OutOfMemory/blog/1799185)

[Quartz调度源码分析](https://my.oschina.net/OutOfMemory/blog/1800560)

## **前言**

上一篇文章[Quartz数据库表分析](https://my.oschina.net/OutOfMemory/blog/1799185)介绍了Quartz默认提供的11张表，本文将具体分析Quartz是如何调度的，是如何通过数据库的方式来现在分布式调度。

## **调度线程**

Quartz内部提供的调度类是QuartzScheduler，而QuartzScheduler会委托QuartzSchedulerThread去实时调度；当调度完需要去执行job的时候QuartzSchedulerThread并没有直接去执行job，  
而是交给ThreadPool去执行job，具体使用什么ThreadPool，初始化多线线程，可以在配置文件中进行配置：

```
org.quartz.threadPool.class: org.quartz.simpl.SimpleThreadPool
org.quartz.threadPool.threadCount: 10
org.quartz.threadPool.threadPriority: 5
```

常用的线程池是SimpleThreadPool，这里默认启动了10个线程，在SimpleThreadPool会创建10个WorkerThread，由WorkerThread去执行具体的job；

## **调度分析**

QuartzSchedulerThread是调度的核心类，具体Quartz是如何实现调度的，可以查看QuartzSchedulerThread核心源码：

```
public void run() {
    boolean lastAcquireFailed = false;
 
    while (!halted.get()) {
        try {
            // check if we're supposed to pause...
            synchronized (sigLock) {
                while (paused && !halted.get()) {
                    try {
                        // wait until togglePause(false) is called...
                        sigLock.wait(1000L);
                    } catch (InterruptedException ignore) {
                    }
                }
 
                if (halted.get()) {
                    break;
                }
            }
 
            int availThreadCount = qsRsrcs.getThreadPool().blockForAvailableThreads();
            if(availThreadCount > 0) { // will always be true, due to semantics of blockForAvailableThreads...
 
                List<OperableTrigger> triggers = null;
 
                long now = System.currentTimeMillis();
 
                clearSignaledSchedulingChange();
                try {
                    triggers = qsRsrcs.getJobStore().acquireNextTriggers(
                            now + idleWaitTime, Math.min(availThreadCount, qsRsrcs.getMaxBatchSize()), qsRsrcs.getBatchTimeWindow());
                    lastAcquireFailed = false;
                    if (log.isDebugEnabled()) 
                        log.debug("batch acquisition of " + (triggers == null ? 0 : triggers.size()) + " triggers");
                } catch (JobPersistenceException jpe) {
                    if(!lastAcquireFailed) {
                        qs.notifySchedulerListenersError(
                            "An error occurred while scanning for the next triggers to fire.",
                            jpe);
                    }
                    lastAcquireFailed = true;
                    continue;
                } catch (RuntimeException e) {
                    if(!lastAcquireFailed) {
                        getLog().error("quartzSchedulerThreadLoop: RuntimeException "
                                +e.getMessage(), e);
                    }
                    lastAcquireFailed = true;
                    continue;
                }
 
                if (triggers != null && !triggers.isEmpty()) {
 
                    now = System.currentTimeMillis();
                    long triggerTime = triggers.get(0).getNextFireTime().getTime();
                    long timeUntilTrigger = triggerTime - now;
                    while(timeUntilTrigger > 2) {
                        synchronized (sigLock) {
                            if (halted.get()) {
                                break;
                            }
                            if (!isCandidateNewTimeEarlierWithinReason(triggerTime, false)) {
                                try {
                                    // we could have blocked a long while
                                    // on 'synchronize', so we must recompute
                                    now = System.currentTimeMillis();
                                    timeUntilTrigger = triggerTime - now;
                                    if(timeUntilTrigger >= 1)
                                        sigLock.wait(timeUntilTrigger);
                                } catch (InterruptedException ignore) {
                                }
                            }
                        }
                        if(releaseIfScheduleChangedSignificantly(triggers, triggerTime)) {
                            break;
                        }
                        now = System.currentTimeMillis();
                        timeUntilTrigger = triggerTime - now;
                    }
 
                    // this happens if releaseIfScheduleChangedSignificantly decided to release triggers
                    if(triggers.isEmpty())
                        continue;
 
                    // set triggers to 'executing'
                    List<TriggerFiredResult> bndles = new ArrayList<TriggerFiredResult>();
 
                    boolean goAhead = true;
                    synchronized(sigLock) {
                        goAhead = !halted.get();
                    }
                    if(goAhead) {
                        try {
                            List<TriggerFiredResult> res = qsRsrcs.getJobStore().triggersFired(triggers);
                            if(res != null)
                                bndles = res;
                        } catch (SchedulerException se) {
                            qs.notifySchedulerListenersError(
                                    "An error occurred while firing triggers '"
                                            + triggers + "'", se);
                            //QTZ-179 : a problem occurred interacting with the triggers from the db
                            //we release them and loop again
                            for (int i = 0; i < triggers.size(); i++) {
                                qsRsrcs.getJobStore().releaseAcquiredTrigger(triggers.get(i));
                            }
                            continue;
                        }
 
                    }
 
                    for (int i = 0; i < bndles.size(); i++) {
                        TriggerFiredResult result =  bndles.get(i);
                        TriggerFiredBundle bndle =  result.getTriggerFiredBundle();
                        Exception exception = result.getException();
 
                        if (exception instanceof RuntimeException) {
                            getLog().error("RuntimeException while firing trigger " + triggers.get(i), exception);
                            qsRsrcs.getJobStore().releaseAcquiredTrigger(triggers.get(i));
                            continue;
                        }
 
                        // it's possible to get 'null' if the triggers was paused,
                        // blocked, or other similar occurrences that prevent it being
                        // fired at this time...  or if the scheduler was shutdown (halted)
                        if (bndle == null) {
                            qsRsrcs.getJobStore().releaseAcquiredTrigger(triggers.get(i));
                            continue;
                        }
 
                        JobRunShell shell = null;
                        try {
                            shell = qsRsrcs.getJobRunShellFactory().createJobRunShell(bndle);
                            shell.initialize(qs);
                        } catch (SchedulerException se) {
                            qsRsrcs.getJobStore().triggeredJobComplete(triggers.get(i), bndle.getJobDetail(), CompletedExecutionInstruction.SET_ALL_JOB_TRIGGERS_ERROR);
                            continue;
                        }
 
                        if (qsRsrcs.getThreadPool().runInThread(shell) == false) {
                            // this case should never happen, as it is indicative of the
                            // scheduler being shutdown or a bug in the thread pool or
                            // a thread pool being used concurrently - which the docs
                            // say not to do...
                            getLog().error("ThreadPool.runInThread() return false!");
                            qsRsrcs.getJobStore().triggeredJobComplete(triggers.get(i), bndle.getJobDetail(), CompletedExecutionInstruction.SET_ALL_JOB_TRIGGERS_ERROR);
                        }
 
                    }
 
                    continue; // while (!halted)
                }
            } else { // if(availThreadCount > 0)
                // should never happen, if threadPool.blockForAvailableThreads() follows contract
                continue; // while (!halted)
            }
 
            long now = System.currentTimeMillis();
            long waitTime = now + getRandomizedIdleWaitTime();
            long timeUntilContinue = waitTime - now;
            synchronized(sigLock) {
                try {
                  if(!halted.get()) {
                    // QTZ-336 A job might have been completed in the mean time and we might have
                    // missed the scheduled changed signal by not waiting for the notify() yet
                    // Check that before waiting for too long in case this very job needs to be
                    // scheduled very soon
                    if (!isScheduleChanged()) {
                      sigLock.wait(timeUntilContinue);
                    }
                  }
                } catch (InterruptedException ignore) {
                }
            }
 
        } catch(RuntimeException re) {
            getLog().error("Runtime error occurred in main trigger firing loop.", re);
        }
    } // while (!halted)
 
    // drop references to scheduler stuff to aid garbage collection...
    qs = null;
    qsRsrcs = null;
}
```

### 1.halted和paused

这是两个boolean值的标志参数，分别表示：停止和暂停；halted默认为false，当QuartzScheduler执行shutdown()时才会更新为true；paused默认是true，当QuartzScheduler执行start()时  
更新为false；正常启动之后QuartzSchedulerThread就可以往下执行了；

### 2.availThreadCount

查询SimpleThreadPool是否有可用的WorkerThread，如果availThreadCount>0可以往下继续执行其他逻辑，否则继续检查；

### 3.acquireNextTriggers

查询一段时间内将要被调度的triggers，这里有3个比较重要的参数分别是：idleWaitTime，maxBatchSize，batchTimeWindow，这3个参数都可以在配置文件中进行配置：

```
org.quartz.scheduler.idleWaitTime:30000
org.quartz.scheduler.batchTriggerAcquisitionMaxCount:1
org.quartz.scheduler.batchTriggerAcquisitionFireAheadTimeWindow:0
```

idleWaitTime:在调度程序处于空闲状态时，调度程序将在重新查询可用触发器之前等待的时间量（以毫秒为单位），默认是30秒;  
batchTriggerAcquisitionMaxCount:允许调度程序节点一次获取（用于触发）的触发器的最大数量，默认是1;  
batchTriggerAcquisitionFireAheadTimeWindow:允许触发器在其预定的火灾时间之前被获取和触发的时间（毫秒）的时间量，默认是0;

往下继续查看acquireNextTriggers方法源码：

```
public List<OperableTrigger> acquireNextTriggers(final long noLaterThan, final int maxCount, final long timeWindow)
    throws JobPersistenceException {
     
    String lockName;
    if(isAcquireTriggersWithinLock() || maxCount > 1) { 
        lockName = LOCK_TRIGGER_ACCESS;
    } else {
        lockName = null;
    }
    return executeInNonManagedTXLock(lockName, 
            new TransactionCallback<List<OperableTrigger>>() {
                public List<OperableTrigger> execute(Connection conn) throws JobPersistenceException {
                    return acquireNextTrigger(conn, noLaterThan, maxCount, timeWindow);
                }
            },
            ......
            });
}
```

可以发现只有在设置了acquireTriggersWithinLock或者batchTriggerAcquisitionMaxCount>1情况下才使用LOCK\_TRIGGER\_ACCESS锁，也就是说在默认参数配置的情况下，这里是没有使用锁的，  
那么如果多个节点同时去执行acquireNextTriggers，会不会出现同一个trigger在多个节点都被执行？  
注：acquireTriggersWithinLock可以在配置文件中进行配置：

```
org.quartz.jobStore.acquireTriggersWithinLock=true
```

acquireTriggersWithinLock：获取triggers的时候是否需要使用锁，默认是false，如果batchTriggerAcquisitionMaxCount>1最好同时设置acquireTriggersWithinLock为true；

带着问题继续查看TransactionCallback内部的acquireNextTrigger方法源码：

```
protected List<OperableTrigger> acquireNextTrigger(Connection conn, long noLaterThan, int maxCount, long timeWindow)
    throws JobPersistenceException {
    if (timeWindow < 0) {
      throw new IllegalArgumentException();
    }
     
    List<OperableTrigger> acquiredTriggers = new ArrayList<OperableTrigger>();
    Set<JobKey> acquiredJobKeysForNoConcurrentExec = new HashSet<JobKey>();
    final int MAX_DO_LOOP_RETRY = 3;
    int currentLoopCount = 0;
    do {
        currentLoopCount ++;
        try {
            List<TriggerKey> keys = getDelegate().selectTriggerToAcquire(conn, noLaterThan + timeWindow, getMisfireTime(), maxCount);
             
            // No trigger is ready to fire yet.
            if (keys == null || keys.size() == 0)
                return acquiredTriggers;
 
            long batchEnd = noLaterThan;
 
            for(TriggerKey triggerKey: keys) {
                // If our trigger is no longer available, try a new one.
                OperableTrigger nextTrigger = retrieveTrigger(conn, triggerKey);
                if(nextTrigger == null) {
                    continue; // next trigger
                }
                 
                // If trigger's job is set as @DisallowConcurrentExecution, and it has already been added to result, then
                // put it back into the timeTriggers set and continue to search for next trigger.
                JobKey jobKey = nextTrigger.getJobKey();
                JobDetail job;
                try {
                    job = retrieveJob(conn, jobKey);
                } catch (JobPersistenceException jpe) {
                    try {
                        getLog().error("Error retrieving job, setting trigger state to ERROR.", jpe);
                        getDelegate().updateTriggerState(conn, triggerKey, STATE_ERROR);
                    } catch (SQLException sqle) {
                        getLog().error("Unable to set trigger state to ERROR.", sqle);
                    }
                    continue;
                }
                 
                if (job.isConcurrentExectionDisallowed()) {
                    if (acquiredJobKeysForNoConcurrentExec.contains(jobKey)) {
                        continue; // next trigger
                    } else {
                        acquiredJobKeysForNoConcurrentExec.add(jobKey);
                    }
                }
                 
                if (nextTrigger.getNextFireTime().getTime() > batchEnd) {
                  break;
                }
                // We now have a acquired trigger, let's add to return list.
                // If our trigger was no longer in the expected state, try a new one.
                int rowsUpdated = getDelegate().updateTriggerStateFromOtherState(conn, triggerKey, STATE_ACQUIRED, STATE_WAITING);
                if (rowsUpdated <= 0) {
                    continue; // next trigger
                }
                nextTrigger.setFireInstanceId(getFiredTriggerRecordId());
                getDelegate().insertFiredTrigger(conn, nextTrigger, STATE_ACQUIRED, null);
 
                if(acquiredTriggers.isEmpty()) {
                    batchEnd = Math.max(nextTrigger.getNextFireTime().getTime(), System.currentTimeMillis()) + timeWindow;
                }
                acquiredTriggers.add(nextTrigger);
            }
 
            // if we didn't end up with any trigger to fire from that first
            // batch, try again for another batch. We allow with a max retry count.
            if(acquiredTriggers.size() == 0 && currentLoopCount < MAX_DO_LOOP_RETRY) {
                continue;
            }
             
            // We are done with the while loop.
            break;
        } catch (Exception e) {
            throw new JobPersistenceException(
                      "Couldn't acquire next trigger: " + e.getMessage(), e);
        }
    } while (true);
     
    // Return the acquired trigger list
    return acquiredTriggers;
}
```

首先看一下在执行selectTriggerToAcquire方法时引入了新的参数：misfireTime=当前时间-MisfireThreshold，MisfireThreshold可以在配置文件中进行配置：

```
org.quartz.jobStore.misfireThreshold:?60000
```

misfireThreshold：叫触发器超时，比如有10个线程，但是有11个任务，这样就有一个任务被延迟执行了，可以理解为调度引擎可以忍受这个超时的时间；具体的查询SQL如下所示：

```
SELECT TRIGGER_NAME, TRIGGER_GROUP, NEXT_FIRE_TIME, PRIORITY
  FROM qrtz_TRIGGERS
 WHERE SCHED_NAME = 'myScheduler'
   AND TRIGGER_STATE = 'WAITING'
   AND NEXT_FIRE_TIME <= noLaterThan
   AND (MISFIRE_INSTR = -1 OR
       (MISFIRE_INSTR != -1 AND NEXT_FIRE_TIME >= noEarlierThan))
 ORDER BY NEXT_FIRE_TIME ASC, PRIORITY DESC
```

这里的noLaterThan=当前时间+idleWaitTime+batchTriggerAcquisitionFireAheadTimeWindow，  
noEarlierThan=当前时间-MisfireThreshold；  
在查询完之后，会遍历执行updateTriggerStateFromOtherState()方法更新trigger的状态从STATE\_WAITING到STATE\_ACQUIRED，并且会判断rowsUpdated是否大于0，这样就算多个节点都查询到相同的trigger，但是肯定只会有一个节点更新成功；更新完状态之后，往qrtz\_fired\_triggers表中插入一条记录，表示当前trigger已经触发，状态为STATE_ACQUIRED；

### 4.executeInNonManagedTXLock

Quartz的分布式锁被用在很多地方，下面具体看一下Quartz是如何实现分布式锁的，executeInNonManagedTXLock方法源码如下：

```
protected <T> T executeInNonManagedTXLock(
        String lockName, 
        TransactionCallback<T> txCallback, final TransactionValidator<T> txValidator) throws JobPersistenceException {
    boolean transOwner = false;
    Connection conn = null;
    try {
        if (lockName != null) {
            // If we aren't using db locks, then delay getting DB connection 
            // until after acquiring the lock since it isn't needed.
            if (getLockHandler().requiresConnection()) {
                conn = getNonManagedTXConnection();
            }
             
            transOwner = getLockHandler().obtainLock(conn, lockName);
        }
         
        if (conn == null) {
            conn = getNonManagedTXConnection();
        }
         
        final T result = txCallback.execute(conn);
        try {
            commitConnection(conn);
        } catch (JobPersistenceException e) {
            rollbackConnection(conn);
            if (txValidator == null || !retryExecuteInNonManagedTXLock(lockName, new TransactionCallback<Boolean>() {
                @Override
                public Boolean execute(Connection conn) throws JobPersistenceException {
                    return txValidator.validate(conn, result);
                }
            })) {
                throw e;
            }
        }
 
        Long sigTime = clearAndGetSignalSchedulingChangeOnTxCompletion();
        if(sigTime != null && sigTime >= 0) {
            signalSchedulingChangeImmediately(sigTime);
        }
         
        return result;
    } catch (JobPersistenceException e) {
        rollbackConnection(conn);
        throw e;
    } catch (RuntimeException e) {
        rollbackConnection(conn);
        throw new JobPersistenceException("Unexpected runtime exception: "
                + e.getMessage(), e);
    } finally {
        try {
            releaseLock(lockName, transOwner);
        } finally {
            cleanupConnection(conn);
        }
    }
}
```

大致分成3个步骤：获取锁，执行逻辑，释放锁；getLockHandler().obtainLock表示获取锁txCallback.execute(conn)表示执行逻辑，commitConnection(conn)表示释放锁  
Quartz的分布式锁接口类是Semaphore，默认具体的实现是StdRowLockSemaphore，具体接口如下：

```
public interface Semaphore {
    boolean obtainLock(Connection conn, String lockName) throws LockException;
    void releaseLock(String lockName) throws LockException;
    boolean requiresConnection();
}
```

具体看一下obtainLock()是如何获取锁的，源码如下：

```
public boolean obtainLock(Connection conn, String lockName)
    throws LockException {
    if (!isLockOwner(lockName)) {
        executeSQL(conn, lockName, expandedSQL, expandedInsertSQL);
        getThreadLocks().add(lockName);
        
    } else if(log.isDebugEnabled()) {
        
    }
    return true;
}
 
protected void executeSQL(Connection conn, final String lockName, final String expandedSQL, final String expandedInsertSQL) throws LockException {
    PreparedStatement ps = null;
    ResultSet rs = null;
    SQLException initCause = null;
     
    int count = 0;
    do {
        count++;
        try {
            ps = conn.prepareStatement(expandedSQL);
            ps.setString(1, lockName);
             
            rs = ps.executeQuery();
            if (!rs.next()) {
                getLog().debug(
                        "Inserting new lock row for lock: '" + lockName + "' being obtained by thread: " + 
                        Thread.currentThread().getName());
                rs.close();
                rs = null;
                ps.close();
                ps = null;
                ps = conn.prepareStatement(expandedInsertSQL);
                ps.setString(1, lockName);
 
                int res = ps.executeUpdate();
                 
                if(res != 1) {
                   if(count < 3) {
                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException ignore) {
                            Thread.currentThread().interrupt();
                        }
                        continue;
                    }
                }
            }
             
            return; // obtained lock, go
        } catch (SQLException sqle) {
            ......
    } while(count < 4);
 
}
```

obtainLock首先判断是否已经获取到锁，如果没有执行方法executeSQL，其中有两条重要的SQL，分别是：expandedSQL和expandedInsertSQL，以SCHED_NAME = ‘myScheduler’为例：

```
SELECT * FROM QRTZ_LOCKS WHERE SCHED_NAME = 'myScheduler' AND LOCK_NAME = ? FOR UPDATE
INSERT INTO QRTZ_LOCKS(SCHED_NAME, LOCK_NAME) VALUES ('myScheduler', ?)
```

select语句后面添加了FOR UPDATE，如果LOCK_NAME存在，当多个节点去执行此SQL时，只有第一个节点会成功，其他的节点都将进入等待；  
如果LOCK_NAME不存在，多个节点同时执行expandedInsertSQL，只会有一个节点插入成功，执行插入失败的节点将进入重试，重新执行expandedSQL；  
txCallback执行完之后，执行commitConnection操作，这样当前节点就释放了LOCK_NAME，其他节点可以竞争获取锁，最后执行了releaseLock；

### 5.triggersFired

表示触发trigger，具体代码如下：

```
protected TriggerFiredBundle triggerFired(Connection conn,
        OperableTrigger trigger)
    throws JobPersistenceException {
    JobDetail job;
    Calendar cal = null;
 
    // Make sure trigger wasn't deleted, paused, or completed...
    try { // if trigger was deleted, state will be STATE_DELETED
        String state = getDelegate().selectTriggerState(conn,
                trigger.getKey());
        if (!state.equals(STATE_ACQUIRED)) {
            return null;
        }
    } catch (SQLException e) {
        throw new JobPersistenceException("Couldn't select trigger state: "
                + e.getMessage(), e);
    }
 
    try {
        job = retrieveJob(conn, trigger.getJobKey());
        if (job == null) { return null; }
    } catch (JobPersistenceException jpe) {
        try {
            getLog().error("Error retrieving job, setting trigger state to ERROR.", jpe);
            getDelegate().updateTriggerState(conn, trigger.getKey(),
                    STATE_ERROR);
        } catch (SQLException sqle) {
            getLog().error("Unable to set trigger state to ERROR.", sqle);
        }
        throw jpe;
    }
 
    if (trigger.getCalendarName() != null) {
        cal = retrieveCalendar(conn, trigger.getCalendarName());
        if (cal == null) { return null; }
    }
 
    try {
        getDelegate().updateFiredTrigger(conn, trigger, STATE_EXECUTING, job);
    } catch (SQLException e) {
        throw new JobPersistenceException("Couldn't insert fired trigger: "
                + e.getMessage(), e);
    }
 
    Date prevFireTime = trigger.getPreviousFireTime();
 
    // call triggered - to update the trigger's next-fire-time state...
    trigger.triggered(cal);
 
    String state = STATE_WAITING;
    boolean force = true;
     
    if (job.isConcurrentExectionDisallowed()) {
        state = STATE_BLOCKED;
        force = false;
        try {
            getDelegate().updateTriggerStatesForJobFromOtherState(conn, job.getKey(),
                    STATE_BLOCKED, STATE_WAITING);
            getDelegate().updateTriggerStatesForJobFromOtherState(conn, job.getKey(),
                    STATE_BLOCKED, STATE_ACQUIRED);
            getDelegate().updateTriggerStatesForJobFromOtherState(conn, job.getKey(),
                    STATE_PAUSED_BLOCKED, STATE_PAUSED);
        } catch (SQLException e) {
            throw new JobPersistenceException(
                    "Couldn't update states of blocked triggers: "
                            + e.getMessage(), e);
        }
    } 
         
    if (trigger.getNextFireTime() == null) {
        state = STATE_COMPLETE;
        force = true;
    }
 
    storeTrigger(conn, trigger, job, true, state, force, false);
 
    job.getJobDataMap().clearDirtyFlag();
 
    return new TriggerFiredBundle(job, trigger, cal, trigger.getKey().getGroup()
            .equals(Scheduler.DEFAULT_RECOVERY_GROUP), new Date(), trigger
            .getPreviousFireTime(), prevFireTime, trigger.getNextFireTime());
}
```

首先查询trigger的状态是否STATE\_ACQUIRED状态，如果不是直接返回null；然后通过通过jobKey获取对应的jobDetail，更新对应的FiredTrigger为EXECUTING状态；最后判定job的DisallowConcurrentExecution是否开启，如果开启了不能并发执行job，那么trigger的状态为STATE\_BLOCKED状态，否则为STATE\_WAITING；如果状态为STATE\_BLOCKED，那么下次调度  
对应的trigger不会被拉取，只有等对应的job执行完之后，更新状态为STATE_WAITING之后才可以执行，保证了job的串行；

### 6.执行job

通过ThreadPool来执行封装job的JobRunShell；

## **问题解释**

在文章[Spring整合Quartz分布式调度](https://my.oschina.net/OutOfMemory/blog/1790200)中，最后做了几次测试分布式调度，现在可以做出相应的解释

### 1.同一trigger同一时间只会在一个节点执行

上文中可以发现Quartz使用了分布式锁和状态来保证只有一个节点能执行；

### 2.任务没有执行完，可以重新开始

因为调度线程和任务执行线程是分开的，认为执行在Threadpool中执行，互相不影响；

### 3.通过DisallowConcurrentExecution注解保证任务的串行

在triggerFired中如果使用了DisallowConcurrentExecution，会引入STATE_BLOCKED状态，保证任务的串行；

## **总结**

本文从源码的角度大致介绍了一下Quartz调度的流程，当然太细节的东西没有去深入；通过本文大致可以对多节点调度产生的现象做一个合理的解释。

## **系列文章**

[Spring整合Quartz分布式调度](https://my.oschina.net/OutOfMemory/blog/1790200)  
[Quartz数据库表分析](https://my.oschina.net/OutOfMemory/blog/1799185)  
[Quartz调度源码分析](https://my.oschina.net/OutOfMemory/blog/1800560)