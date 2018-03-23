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
	public final int NUMBER_OF_FEEDBACK_QUEUES = 7;
	
	public Queue<Integer> readyQueue;
	public Queue<Integer> tempQueue;
	public Queue<Integer>[] feedbackQueues;
	public static boolean someoneRunning;
	public long systemTime;
	public Integer currentRunningProcessID;
	public int currentQueueOfRunningProcess;	// Used in FB 
	public static Scheduler scheduler;
	public Thread roundRobinTimeSlicerThread;
	public Thread feedbackTimeSlicerThread;
	public int quantumRR;
	public int quantumFeedback;
	public boolean timerMayDie;
	public Semaphore switchMutex;
	


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
			someoneRunning = false;
			this.timerMayDie = false;
			this.quantumFeedback = quantum;
			switchMutex = new Semaphore(1);
			
			this.feedbackQueues = new Queue[NUMBER_OF_FEEDBACK_QUEUES];
			for (int i = 0; i < NUMBER_OF_FEEDBACK_QUEUES; i++) {
				// Need to initialize all queues
				this.feedbackQueues[i] = new LinkedList<Integer>(); 
			}
			
			// Create new thread for timer
			if (this.feedbackTimeSlicerThread != null && this.feedbackTimeSlicerThread.isAlive()) {
				// Need to reset timer for Round robin protocol because the timer thread already exists
				this.timerMayDie = true;
				try {
					this.feedbackTimeSlicerThread.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.timerMayDie = false;
			} 
			this.feedbackTimeSlicerThread = new Thread(new FeedbackTimeSlicer());
			this.feedbackTimeSlicerThread.start();
			
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
			try {
				this.switchMutex.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
			if (!someoneRunning) {
				someoneRunning = !someoneRunning;
				currentRunningProcessID = processID;
				this.currentQueueOfRunningProcess = 0;
				this.processExecution.switchToProcess(currentRunningProcessID);	
				this.systemTime = System.currentTimeMillis();
			} else {
				this.feedbackQueues[0].add(processID);	// When a process is spawned it goes to the queue with the highest priority
			}
			this.switchMutex.release();
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
			try {
				this.switchMutex.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			boolean processSwapped = false;
			for (int i = 0; i < NUMBER_OF_FEEDBACK_QUEUES; i++) {
				if (!this.feedbackQueues[i].isEmpty()) {
					processSwapped = true;
					int currentQueueNumber = i;
					
					this.currentQueueOfRunningProcess = currentQueueNumber;													// Updating current queue number
					this.currentRunningProcessID = this.feedbackQueues[this.currentQueueOfRunningProcess].remove();			// Fetching next process to run
					this.processExecution.switchToProcess(this.currentRunningProcessID);	
					this.systemTime = System.currentTimeMillis();
					break;
				} 
			}
			if (!processSwapped) {
				// All queues are empty
				someoneRunning = false;
			}
			this.switchMutex.release();
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
