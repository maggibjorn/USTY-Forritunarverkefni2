package com.ru.usty.scheduling;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import com.ru.usty.scheduling.process.ProcessExecution;

public class Scheduler {

	public ProcessExecution processExecution;
	Policy policy;
	int quantum;

	/**
	 * Add any objects and variables here (if needed)
	 */
	
	public Queue<Integer> readyQueue;
	public Queue<Integer> tempQueue;
	public static boolean someoneRunning;
	public long systemTime;
	
	public Integer currentRunningProcessID;
	public static Scheduler scheduler;
	public Thread roundRobinTimeSlicerThread;
	public int quantumRR;
	public boolean timerMayDie;
	public Semaphore switchMutex;
	
	int dummie = 0;

	/**
	 * DO NOT CHANGE DEFINITION OF OPERATION
	 */
	public Scheduler(ProcessExecution processExecution) {
		this.processExecution = processExecution;

		/**
		 * Add general initialization code here (if needed)
		 */
	}

	/**
	 * DO NOT CHANGE DEFINITION OF OPERATION
	 */
	@SuppressWarnings("unchecked")
	public void startScheduling(Policy policy, int quantum) {

		this.policy = policy;
		this.quantum = quantum;
		scheduler = this;	// To be able to access the ProcessExecution instance in other classes

		/**
		 * Add general initialization code here (if needed)
		 */

		switch(policy) {
		case FCFS:	//First-come-first-served
			System.out.println("Starting new scheduling task: First-come-first-served");
			/**
			 * Add your policy specific initialization code here (if needed)
			 */
			readyQueue = new LinkedList<Integer>();
			someoneRunning = false;
			break;
		case RR:	//Round robin
			System.out.println("Starting new scheduling task: Round robin, quantum = " + quantum);
			/**
			 * Add your policy specific initialization code here (if needed)
			 */
			this.readyQueue = new LinkedList<Integer>();
			someoneRunning = false;
			this.timerMayDie = false;
			this.quantumRR = quantum;
			switchMutex = new Semaphore(1);
			
			// Create new thread for timer
			if (this.roundRobinTimeSlicerThread != null && this.roundRobinTimeSlicerThread.isAlive()) {
				// Need to reset timer for Round robin protocol because the timer thread already exists
				this.timerMayDie = true;
				try {
					this.roundRobinTimeSlicerThread.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.timerMayDie = false;
			} 
			this.roundRobinTimeSlicerThread = new Thread(new RoundRobinTimeSlicer());
			this.roundRobinTimeSlicerThread.start();
			
			break;
		case SPN:	//Shortest process next
			System.out.println("Starting new scheduling task: Shortest process next");
			/**
			 * Add your policy specific initialization code here (if needed)
			 */
			if (this.roundRobinTimeSlicerThread != null && this.roundRobinTimeSlicerThread.isAlive()) {
				// Need to reset timer for Round robin protocol because the timer thread already exists
				this.timerMayDie = true;
				try {
					this.roundRobinTimeSlicerThread.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.timerMayDie = false;
			} 
			
			Comparator<Integer> com = new CompareSPN();
			this.readyQueue = new PriorityQueue<Integer>(com);
			someoneRunning = false;
			
			break;
		case SRT:	//Shortest remaining time
			System.out.println("Starting new scheduling task: Shortest remaining time");
			/**
			 * Add your policy specific initialization code here (if needed)
			 */
			Comparator<Integer> comSRT = new CompareSRT();	
			this.readyQueue = new PriorityQueue<Integer>(comSRT);
			someoneRunning = false;
			break;
		case HRRN:	//Highest response ratio next
			System.out.println("Starting new scheduling task: Highest response ratio next");
			/**
			 * Add your policy specific initialization code here (if needed)
			 */
			Comparator<Integer> comHRRN = new CompareHRRN();
			this.readyQueue = new PriorityQueue<Integer>(comHRRN);
			this.tempQueue = new LinkedList<Integer>();
			
			someoneRunning = false;
			break;
		case FB:	//Feedback
			System.out.println("Starting new scheduling task: Feedback, quantum = " + quantum);
			/**
			 * Add your policy specific initialization code here (if needed)
			 */
			break;
		}

		/**
		 * Add general scheduling or initialization code here (if needed)
		 */

	}

	/**
	 * DO NOT CHANGE DEFINITION OF OPERATION
	 */
	public void processAdded(int processID) {
		switch(this.policy) {
		case FCFS:	//First-come-first-served
			this.readyQueue.add(processID);		
			if (!someoneRunning) {
				currentRunningProcessID = this.readyQueue.remove();
				this.processExecution.switchToProcess(currentRunningProcessID);
				someoneRunning = !someoneRunning;	// Now there is a process running on the processor
			}
			break;
		case RR:	//Round robin
			try {
				this.switchMutex.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			this.readyQueue.add(processID);	
			if (!someoneRunning) {
				currentRunningProcessID = this.readyQueue.remove();
				
				this.processExecution.switchToProcess(currentRunningProcessID);
				someoneRunning = !someoneRunning;	// Now there is a process running on the processor
				this.systemTime = System.currentTimeMillis();
			}
			
			this.switchMutex.release();
			break;
		case SPN:	//Shortest process next
			this.readyQueue.add(processID);		
			if (!someoneRunning) {
				currentRunningProcessID = this.readyQueue.remove();
				this.processExecution.switchToProcess(currentRunningProcessID);
				someoneRunning = !someoneRunning;	// Now there is a process running on the processor
			}
			break;
		case SRT:	//Shortest remaining time
		
				if (!someoneRunning) {
					someoneRunning = !someoneRunning;
					currentRunningProcessID = processID;
					this.processExecution.switchToProcess(currentRunningProcessID);
				} else {
					long x = this.processExecution.getProcessInfo(processID).totalServiceTime;
					long y = this.processExecution.getProcessInfo(currentRunningProcessID).totalServiceTime;
					long z = this.processExecution.getProcessInfo(currentRunningProcessID).elapsedExecutionTime;
					if (x < (y-z)) {
						this.readyQueue.add(currentRunningProcessID);
						currentRunningProcessID = processID;
						this.processExecution.switchToProcess(currentRunningProcessID);
					} else {
						this.readyQueue.add(processID);
					}
				}	
		
			break;
		case HRRN:	//Highest response ratio next
			if (!someoneRunning) {
				someoneRunning = !someoneRunning;
				currentRunningProcessID = processID;
				this.processExecution.switchToProcess(currentRunningProcessID);
			} else {
				this.readyQueue.add(processID);
			}	
			break;
		case FB:	//Feedback
			break;
		}
	
	}

	/**
	 * DO NOT CHANGE DEFINITION OF OPERATION
	 */
	public void processFinished(int processID) {
		switch(this.policy) {
		case FCFS:	//First-come-first-served
			if (!this.readyQueue.isEmpty()) {
				// The queue is not empty and there is a process waiting for the processor
				currentRunningProcessID = this.readyQueue.remove();
				this.processExecution.switchToProcess(currentRunningProcessID);
			} else {
				// No process on queue
				someoneRunning = false;
			}
			break;
		case RR:	//Round robin
			try {
				this.switchMutex.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (!this.readyQueue.isEmpty()) {
				// The queue is not empty and there is a process waiting for the processor
				currentRunningProcessID = this.readyQueue.remove();
				this.processExecution.switchToProcess(currentRunningProcessID);
				this.systemTime = System.currentTimeMillis();
			} else {
				// No process on queue
				someoneRunning = false;
			}
			this.switchMutex.release();
			
			break;
		case SPN:	//Shortest process next
			if (!this.readyQueue.isEmpty()) {
				// The queue is not empty and there is a process waiting for the processor
				currentRunningProcessID = this.readyQueue.remove();
				this.processExecution.switchToProcess(currentRunningProcessID);
			} else {
				// No process on queue
				someoneRunning = false;
			}
			break;
		case SRT:	//Shortest remaining time
			if (!this.readyQueue.isEmpty()) {
				// The queue is not empty and there is a process waiting for the processor
				currentRunningProcessID = this.readyQueue.remove();
				this.processExecution.switchToProcess(currentRunningProcessID);
			} else {
				// No process on queue
				someoneRunning = false;
			}
			break;
		case HRRN:	//Highest response ratio next
			if (!this.readyQueue.isEmpty()) {
				// The queue is not empty and there is a process waiting for the processor
				this.recalculateHRRNQueue();	// Need to recalculate all ratios
				currentRunningProcessID = this.readyQueue.remove();	
				this.processExecution.switchToProcess(currentRunningProcessID);
			} else {
				someoneRunning = false;
			}
			break;
		case FB:	//Feedback
			break;
		}
	}
	
	// Only used in HRRN protocol
	private void recalculateHRRNQueue() {
		while (!this.readyQueue.isEmpty()) {
			this.tempQueue.add(this.readyQueue.remove());
		}
		while (!this.tempQueue.isEmpty()) {
			this.readyQueue.add(this.tempQueue.remove());
			
		}
	}
	
}
