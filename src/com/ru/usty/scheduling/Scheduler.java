package com.ru.usty.scheduling;

import java.util.LinkedList;
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
	public static boolean someoneRunning;
	public long systemTime;
	
	public Integer currentRunningProcessID;
	public static Scheduler scheduler;
	public Thread roundRobinTimeSlicerThread;
	public int quantumRR;
	public boolean timerMayDie;
	public Semaphore switchMutex = null;

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
			break;
		case SRT:	//Shortest remaining time
			System.out.println("Starting new scheduling task: Shortest remaining time");
			/**
			 * Add your policy specific initialization code here (if needed)
			 */
			break;
		case HRRN:	//Highest response ratio next
			System.out.println("Starting new scheduling task: Highest response ratio next");
			/**
			 * Add your policy specific initialization code here (if needed)
			 */
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

		/**
		 * Add scheduling code here
		 */
		if (this.switchMutex != null) {
			try {
				this.switchMutex.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Process arrived -- : " + processID);
		this.readyQueue.add(processID);	
		if (!someoneRunning) {
			Integer nextProcessIDToRun = this.readyQueue.remove();
			currentRunningProcessID = nextProcessIDToRun;
			
			this.processExecution.switchToProcess(nextProcessIDToRun);
			someoneRunning = !someoneRunning;	// Now there is a process running on the processor
			this.systemTime = System.currentTimeMillis();
		}
		if (this.switchMutex != null) {
			this.switchMutex.release();
		}
	}

	/**
	 * DO NOT CHANGE DEFINITION OF OPERATION
	 */
	public void processFinished(int processID) {

		/**
		 * Add scheduling code here
		 */
		if (this.switchMutex != null) {
			try {
				this.switchMutex.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (!this.readyQueue.isEmpty()) {
			// The queue is not empty and there is a process waiting for the processor
			Integer nextProcessIDToRun = this.readyQueue.remove();
			
			currentRunningProcessID = nextProcessIDToRun;
			this.processExecution.switchToProcess(nextProcessIDToRun);
			this.systemTime = System.currentTimeMillis();
			System.out.println("Process has left: " + processID);
		} else {
			// No process on queue
			someoneRunning = false;
		}
		if (this.switchMutex != null) {
			this.switchMutex.release();
		}

	}
}
