package com.ru.usty.scheduling;
import java.util.Comparator;

// Overrides compare for Shortest Process Next priority queue
public class CompareSRT implements Comparator<Integer> {
   public int compare(Integer x, Integer y) {
      long c1 = Scheduler.scheduler.processExecution.getProcessInfo(x).totalServiceTime - Scheduler.scheduler.processExecution.getProcessInfo(x).elapsedExecutionTime;
      long c2 = Scheduler.scheduler.processExecution.getProcessInfo(y).totalServiceTime - Scheduler.scheduler.processExecution.getProcessInfo(y).elapsedExecutionTime;
      if (c1 < c2) {
    	  return -1;
      } else if (c1 == c2) {
    	  return 0;
      } else {
    	  return 1;
      }
   }
}