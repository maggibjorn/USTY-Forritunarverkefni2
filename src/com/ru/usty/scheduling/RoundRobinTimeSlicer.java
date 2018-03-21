package com.ru.usty.scheduling;

public class RoundRobinTimeSlicer implements Runnable {
	
	private int quantumRR;	// This is the quantum time used in corresponding round robin algorithm
	
	public RoundRobinTimeSlicer(int quantumTime) {
		this.quantumRR = quantumTime;
	}
	
	@Override
	public void run() {
		while (true) {
			this.ThreadSleepForQuantumTime(this.quantumRR);
			this.timeSliceCurrentRunningProcess();
			
		}
		
	}
	
	public void timeSliceCurrentRunningProcess() {
		if (Scheduler.someoneRunning) {
			// There is a process on the processor, need to time slice it
			if (!Scheduler.scheduler.readyQueue.isEmpty()) {
				// If the ready queue isn't empty the time slicer swaps running processes on processor
				Integer nextProcessIDToRun = Scheduler.scheduler.readyQueue.remove();
				Scheduler.scheduler.readyQueue.add(Scheduler.scheduler.currentRunningProcessID);
				Scheduler.scheduler.currentRunningProcessID = nextProcessIDToRun;
				Scheduler.scheduler.processExecution.switchToProcess(nextProcessIDToRun);
				System.out.println("Process " + Scheduler.scheduler.currentRunningProcessID + " swapped for process " + nextProcessIDToRun);
			}	
		}
	}
	
	public void ThreadSleepForQuantumTime(int quantumTime) {
		System.out.println("Timer about to sleep for: " + quantumTime);
		try {
			Thread.sleep(quantumTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
