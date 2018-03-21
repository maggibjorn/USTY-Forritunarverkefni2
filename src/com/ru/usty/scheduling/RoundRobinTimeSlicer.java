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
				Integer nextProcessIDToRun = Scheduler.scheduler.readyQueue.remove();
				Scheduler.scheduler.readyQueue.add(Scheduler.scheduler.currentRunningProcessID);
				Scheduler.scheduler.currentRunningProcessID = nextProcessIDToRun;
				Scheduler.scheduler.processExecution.switchToProcess(nextProcessIDToRun);
				Scheduler.scheduler.systemTime = System.currentTimeMillis();
			}	
		}
		Scheduler.scheduler.switchMutex.release();
	}
	
	

}
