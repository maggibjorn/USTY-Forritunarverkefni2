package com.ru.usty.scheduling;

import java.util.LinkedList;
import java.util.Queue;

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
	
	public Integer currentRunningProcessID;
	public static Scheduler scheduler;
	Thread roundRobinTimeSlicerThread;

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
			readyQueue = new LinkedList<Integer>();
			someoneRunning = false;
			
			// Create new thread for timer
			if (this.roundRobinTimeSlicerThread != null && this.roundRobinTimeSlicerThread.isAlive()) {
				this.roundRobinTimeSlicerThread.interrupt();
			}
			this.roundRobinTimeSlicerThread = new Thread(new RoundRobinTimeSlicer(quantum));
			roundRobinTimeSlicerThread.start();
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
		this.readyQueue.add(processID);
		
		if (!someoneRunning) {
			someoneRunning = !someoneRunning;	// Now there is a process running on the processor
			Integer nextProcessIDToRun = this.readyQueue.remove();
			currentRunningProcessID = nextProcessIDToRun;
			System.out.println(currentRunningProcessID);
			this.processExecution.switchToProcess(nextProcessIDToRun);
		}

	}

	/**
	 * DO NOT CHANGE DEFINITION OF OPERATION
	 */
	public void processFinished(int processID) {

		/**
		 * Add scheduling code here
		 */
		if (!this.readyQueue.isEmpty()) {
			// The queue is not empty and there is a process waiting for the processor
			Integer nextProcessIDToRun = this.readyQueue.remove();
			currentRunningProcessID = nextProcessIDToRun;
			System.out.println("Hello from process finished");
			this.processExecution.switchToProcess(nextProcessIDToRun);
		} else {
			// No process on queue
			someoneRunning = false;
		}

	}
}
