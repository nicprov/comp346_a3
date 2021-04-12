package com.concordia;

/**
 * Class Monitor
 * To synchronize dining philosophers.
 *
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca
 */
public class Monitor
{
	/**
	 * ------------
	 * Data members
	 * ------------
	 */
	private enum status{EATING, THINKING, HUNGRY};
	private final int piNumberOfPhilosophers;
	private static status[] philosophersStatus;
	private static boolean isTalking; // True if any philosopher is currently talking

	/**
	 * Constructor
	 */
	public Monitor(int piNumberOfPhilosophers)
	{
		this.piNumberOfPhilosophers = piNumberOfPhilosophers;
		philosophersStatus = new status[piNumberOfPhilosophers];
		isTalking = false;

		// Initialize each philosopher's status to Thinking
		for (int i=0; i<piNumberOfPhilosophers; i++){
			philosophersStatus[i] = status.THINKING;
		}
	}

	/*
	 * -------------------------------
	 * User-defined monitor procedures
	 * -------------------------------
	 */

	/**
	 * Grants request (returns) to eat when both chopsticks/forks are available.
	 * Else forces the philosopher to wait()
	 */
	public synchronized void pickUp(final int piTID) throws InterruptedException {
		// Decrement piTID because array is 0 based
		int realPiTID = piTID - 1;
		philosophersStatus[realPiTID] = status.HUNGRY;
		testEating(realPiTID);
		if (philosophersStatus[realPiTID] != status.EATING){
			wait();
			pickUp(piTID);
		}
	}

	/**
	 * When a given philosopher's done eating, they put the chopstiks/forks down
	 * and let others know they are available.
	 */
	public synchronized void putDown(final int piTID)
	{
		// Decrement piTID because array is 0 based
		int realPiTID = piTID - 1;
		philosophersStatus[realPiTID] = status.THINKING;

		// Test left and right neighbours to notify them if possible
		testEating((realPiTID - 1 + piNumberOfPhilosophers) % piNumberOfPhilosophers);
		testEating((realPiTID + 1) % piNumberOfPhilosophers);
	}

	/**
	 * Only one philosopher at a time is allowed to philosophy
	 * (while she is not eating).
	 */
	public synchronized void requestTalk() throws InterruptedException {
		if (isTalking) {
			try {
				wait();
				requestTalk();
			} catch (InterruptedException e){
				System.err.println("Philosopher.requestTalk():");
				DiningPhilosophers.reportException(e);
				System.exit(1);
			}
		}
		isTalking = true;
	}

	/**
	 * When one philosopher is done talking stuff, others
	 * can feel free to start talking.
	 */
	public synchronized void endTalk()
	{
		isTalking = false;
		notifyAll();
	}

	private synchronized void testEating(final int piTID){
		if (philosophersStatus[(piTID + 1) % piNumberOfPhilosophers] != status.EATING &&
				philosophersStatus[(piTID - 1 + piNumberOfPhilosophers) % piNumberOfPhilosophers] != status.EATING &&
				philosophersStatus[piTID] == status.HUNGRY){
			philosophersStatus[piTID] = status.EATING;
			notifyAll();
		}
	}
}

// EOF
