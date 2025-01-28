package com.vcib.store.services;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Service;

@Service
public class FailureService {
    private static final double OMISSION_FAILURE_PROBABILITY = 0.2;
    private static final double ERROR_FAILURE_PROBABILITY = 0.1;
    private static final long OMISSION_DURATION_MS = 65 * 1000;
	private static final long FAILURE_DURATION_MS = 5 * 1000;
	private static AtomicBoolean inFailureState = new AtomicBoolean(false);


    public void simulateOmissionFailure() {

        if (Math.random() < OMISSION_FAILURE_PROBABILITY) {
            simulateUnresponsiveness();
        }

        return;
    }

    public void simulateErrorFailure() {
        boolean inFailure = inFailureState.get();

        if(inFailure || Math.random() < ERROR_FAILURE_PROBABILITY) {
			
            if(!inFailure) {
                enterFailureState();
            } 

            throw new RuntimeException("Simulating failure by error");            	
        }
    }

    private void simulateUnresponsiveness() {
        try {
            Thread.sleep(OMISSION_DURATION_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted during omission failure");
        }
    }

    private void enterFailureState() {
		inFailureState.set(true);
		
		new Thread(() -> {
			try {
				Thread.sleep(FAILURE_DURATION_MS);
			} catch(InterruptedException e) {
				Thread.currentThread().interrupt();
			} finally {
				System.out.println("Saindo do estado de falha...");
				inFailureState.set(false);
			}
			
		}).start();
	}

}
