package com.monkey;

import java.awt.Canvas;

public class MonkeyUpdateThread extends Thread {

	protected Object _lock = new Object();
	protected Canvas _comp;
	
	protected boolean pause;
	
	public MonkeyUpdateThread(Canvas comp) {
		_comp = comp;
		setDaemon(true);
	}
	
    public void run() {
        while (true) {
        	
        	if (pause) {
        		synchronized (_lock) {
        			try {
						_lock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
        		}
        	}
        	_comp.repaint();
            yield();
        }
    }
    
    public void pause() {
    	pause = true;
    }
    
    public void unPause() {
    	pause = false;
    	synchronized (_lock) {
    		_lock.notifyAll();
    	}
    }
}
