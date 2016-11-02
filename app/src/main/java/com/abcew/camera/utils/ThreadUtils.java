package com.abcew.camera.utils;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import java.io.File;
import java.io.FileFilter;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

/**
 * Created by laputan on 16/10/31.
 */
public class ThreadUtils {

    public enum PRIORITY {
        MIN_PRIORITY(Thread.MIN_PRIORITY),
        NORM_PRIORITY(Thread.NORM_PRIORITY),
        MAX_PRIORITY(Thread.MAX_PRIORITY);

        final int PRIORITY;

        PRIORITY(int value) {
            PRIORITY = value;
        }
    }

    enum SynchronizedAction {
        ADD_TASK,
        REPLACE_TASK,
        REMOVE_AND_GET_FIRST_TASK,
        REMOVE_THREAD
    }
    private static final ConcurrentHashMap<String, Queue<Task>> groupTasksQueue = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, TaskThread> threadGroupMap = new ConcurrentHashMap<>();
    private static final int CPU_CORE_COUNT = getNumberOfCores() + 1;

    /**
     * Max Number of WorkerThreads
     */
    private static final int MAX_THREAD_COUNT = CPU_CORE_COUNT + 1;
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Run Ui operation on the main thread at the next UI Frame.
     *
     * @param runnable the runnable.
     */
    @WorkerThread
    public static void postToMainThread(@NonNull MainThreadRunnable runnable) {
        mainHandler.post(runnable);
    }

    /**
     * Run Ui operation on the main thread at the next UI Frame
     * or instant if the execution thread already the main thread.
     *
     * @param runnable the runnable.
     */
    public static void runOnMainThread(@NonNull MainThreadRunnable runnable) {
        if (thisIsUiThread()) {
            runnable.run();
        } else {
            mainHandler.post(runnable);
        }
    }

    /**
     * Check if this is the Ui thread.
     *
     * @return true if this current thread ist the Ui Thread
     */
    public static boolean thisIsUiThread() {
        return Looper.getMainLooper().getThread().equals(Thread.currentThread());
    }

    /**
     * Run parallel in background but serialized executed in a group.
     *
     * @param serialiseGroup group name for serializing
     * @param priority       thread priority during runnable execution, this will not change the order in the group, it change the priority of the thread during the runnable execution.
     * @param runnable       executing runnable
     */
    public static synchronized void addTaskToWorkerGroup(@NonNull String serialiseGroup, @NonNull PRIORITY priority, @NonNull WorkerThreadRunnable runnable) {
        changeGroupTask(SynchronizedAction.ADD_TASK, serialiseGroup, new Task(priority, runnable));
    }

    private static synchronized Task changeGroupTask(SynchronizedAction action, @NonNull String serialiseGroup, Task task) {

        switch (action) {
            case ADD_TASK:
            case REPLACE_TASK:
                Queue<Task> tasks = groupTasksQueue.get(serialiseGroup);
                if (tasks == null) {
                    tasks = new ConcurrentLinkedQueue<>();
                    groupTasksQueue.put(serialiseGroup, tasks);
                } else if (action == SynchronizedAction.REPLACE_TASK) {
                    tasks.clear();
                }
                tasks.add(task);
                break;
            case REMOVE_AND_GET_FIRST_TASK:
                task = groupTasksQueue.get(serialiseGroup).poll();
                break;
            case REMOVE_THREAD:
                threadGroupMap.remove(serialiseGroup);
                break;
            default:
                throw new RuntimeException("changeGroupTask action unknown");
        }

        for (Map.Entry<String, Queue<Task>> entry : groupTasksQueue.entrySet()) {
            String groupName = entry.getKey();
            Queue<Task> tasks = entry.getValue();
            TaskThread thread = threadGroupMap.get(groupName);
            if ((tasks != null && !tasks.isEmpty()) && (thread == null || !thread.isAlive())) {
                thread = new TaskThread(groupName);
                threadGroupMap.put(groupName, thread);
                thread.start();
            }
        }

        return task;
    }

    /**
     * Run parallel in background but executed only one in a group, if exist a previously task it will be removed.
     *
     * @param serialiseGroup group name for serializing
     * @param priority       thread priority during runnable execution, this will not change the order in the group, it change the priority of the thread during the runnable execution.
     * @param runnable       executing runnable
     */
    public static synchronized void replaceTaskOnWorkerGroup(@NonNull String serialiseGroup, @NonNull PRIORITY priority, @NonNull WorkerThreadRunnable runnable) {
        changeGroupTask(SynchronizedAction.REPLACE_TASK, serialiseGroup, new Task(priority, runnable));
    }

    /**
     * Gets the number of cores / virtual CPU devices available in this device, across all processors.
     *
     * @return The number of cores, or 1 if failed to get result
     */
    private static int getNumberOfCores() {
        if (Build.VERSION.SDK_INT >= 17) {
            return Runtime.getRuntime().availableProcessors();
        } else {
            // Fallback requires ability to peruse the filesystem at "/sys/devices/system/cpu"
            try {
                File dir = new File("/sys/devices/system/cpu/"); // Get directory containing CPU info

                // Filter to display only CPU devices in the directory listing
                File[] files = dir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        // Check if filename is "cpu", followed by a single digit number
                        return Pattern.matches("cpu[0-9]+", pathname.getName());
                    }
                });
                return files.length;
            } catch (Exception e) {
                return 1; // Default fallback to 1 core
            }
        }
    }

    public static abstract class WorkerThreadRunnable implements Runnable {

        @Override
        @WorkerThread
        public abstract void run();

        @WorkerThread
        protected void runOnUi(MainThreadRunnable runnable) {
            ThreadUtils.postToMainThread(runnable);
        }

        protected boolean sleep(int milies) {
            try {
                Thread.sleep(milies);
                return true;
            } catch (InterruptedException e) {
                return false;
            }
        }
    }

    public static abstract class MainThreadRunnable implements Runnable {


        @Override
        @MainThread
        public abstract void run();
    }

    private static class TaskThread extends Thread implements Runnable {
        String groupName;
        long lastExecution;

        TaskThread(String groupName) {
            super(groupName);
            this.groupName = groupName;
            lastExecution = SystemClock.elapsedRealtime();
        }

        @Override
        @WorkerThread
        public void run() {
            while (sleep()) {
                Task task;
                do {
                    task = changeGroupTask(SynchronizedAction.REMOVE_AND_GET_FIRST_TASK, groupName, null);
                } while (doTask(task));
            }
            changeGroupTask(SynchronizedAction.REMOVE_THREAD, groupName, null);
        }

        boolean sleep() {
            try {
                sleep(1);
            } catch (InterruptedException ignored) {
            }

            return lastExecution > SystemClock.elapsedRealtime() - 100;
        }

        boolean doTask(Task task) {
            if (task != null) {
                this.setPriority(task.priority.PRIORITY);
                task.runnable.run();
                lastExecution = SystemClock.elapsedRealtime();
                return true;
            } else {
                return false;
            }
        }
    }

    private static class Task {
        PRIORITY priority;
        Runnable runnable;

        Task(PRIORITY priority, Runnable runnable) {
            this.priority = priority;
            this.runnable = runnable;
        }
    }
}
