package com.ru.usty.scheduling;

public class FeedbackTimeSlicer implements Runnable {
	@Override
	public void run() {
			
		while (!Scheduler.scheduler.timerMayDie) {
			try {
				Thread.sleep(Scheduler.scheduler.quantumFeedback);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			long offset = System.currentTimeMillis() - Scheduler.scheduler.systemTime;
			
			while (offset < Scheduler.scheduler.quantumFeedback) {	
				// We have an offset
				try {
					Thread.sleep(Scheduler.scheduler.quantumFeedback - offset);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				offset = System.currentTimeMillis() - Scheduler.scheduler.systemTime;
			}
			this.timeSliceCurrentRunningProcess();
			
		}
	}
	
	public void timeSliceCurrentRunningProcess() {	
		try {
			Scheduler.scheduler.switchMutex.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (Scheduler.someoneRunning) {
			// There is a process on the processor, need to time slice it
			try {
				long a = Scheduler.scheduler.processExecution.getProcessInfo(Scheduler.scheduler.currentRunningProcessID).totalServiceTime - 
				Scheduler.scheduler.processExecution.getProcessInfo(Scheduler.scheduler.currentRunningProcessID).elapsedExecutionTime;
				if (a <= 1) {
					// This is only done to prevent deadlock
					// A process with less than a ms to go is actually done and the visualization class has called processFinishied 
					// and is waiting for the mutex
					Scheduler.scheduler.switchMutex.release();
					return;
				}
			} catch (NullPointerException e) {
				Scheduler.scheduler.switchMutex.release();
				return;
			}
			for (int i = 0; i < Scheduler.scheduler.NUMBER_OF_FEEDBACK_QUEUES; i++) {
				if (!Scheduler.scheduler.feedbackQueues[i].isEmpty()) {
					int currentQueueNumber = i;
					if (Scheduler.scheduler.currentQueueOfRunningProcess < Scheduler.scheduler.NUMBER_OF_FEEDBACK_QUEUES-1) {
						// Only increment queue of running process if it isn't the last one
						Scheduler.scheduler.currentQueueOfRunningProcess++;
					}
					Scheduler.scheduler.feedbackQueues[Scheduler.scheduler.currentQueueOfRunningProcess].add(Scheduler.scheduler.currentRunningProcessID);				// Placing current process on queue
					Scheduler.scheduler.currentQueueOfRunningProcess = currentQueueNumber;																				// Updating current queue number
					Scheduler.scheduler.currentRunningProcessID = Scheduler.scheduler.feedbackQueues[Scheduler.scheduler.currentQueueOfRunningProcess].remove();		// Fetching next process to run
					Scheduler.scheduler.processExecution.switchToProcess(Scheduler.scheduler.currentRunningProcessID);	
					Scheduler.scheduler.systemTime = System.currentTimeMillis();
					break;
				}
			}
		}
		Scheduler.scheduler.switchMutex.release();
	}
	
	

}
