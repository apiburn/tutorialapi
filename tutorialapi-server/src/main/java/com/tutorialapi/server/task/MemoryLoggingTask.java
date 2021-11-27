package com.tutorialapi.server.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

public class MemoryLoggingTask implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger("memory-log");

    @Override
    public void run() {
        MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        double usedHeapMiB = heap.getUsed() / 1024d / 1024d;
        double maxHeapMiB = heap.getMax() / 1024d / 1024d;
        double heapPercentageUsed = usedHeapMiB / maxHeapMiB * 100d;

        LOGGER.info(String.format("Memory Usage: Heap %.0fMiB of %.0fMiB (%.2f%%)",
                usedHeapMiB, maxHeapMiB, heapPercentageUsed));
    }
}
