package com.ru.usty.scheduling;

public class RoundRobinTimeSlicer implements Runnable {
	@Override
	public void run() {
			
		while (!Scheduler.scheduler.timerMayDie) {
			try {
				Thread.sleep(Scheduler.scheduler.quantumRR);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			long offset = System.currentTimeMillis() - Scheduler.scheduler.systemTime;
			
			while (offset < Scheduler.scheduler.quantumRR) {	
				// We have an offset
				try {
					Thread.sleep(Scheduler.scheduler.quantumRR - offset);
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
			if (!Scheduler.scheduler.readyQueue.isEmpty()) {
				// If the ready queue isn't empty the time slicer swaps running processes on processor
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
				
				Scheduler.scheduler.readyQueue.add(Scheduler.scheduler.currentRunningProcessID);
				Scheduler.scheduler.currentRunningProcessID = Scheduler.scheduler.readyQueue.remove();
				Scheduler.scheduler.processExecution.switchToProcess(Scheduler.scheduler.currentRunningProcessID);
				Scheduler.scheduler.systemTime = System.currentTimeMillis();		
			}	
		}
		Scheduler.scheduler.switchMutex.release();
	}
	
	

}
