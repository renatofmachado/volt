package com.github.oxyzero.volt.support;

import java.util.TimerTask;

public class Task extends TimerTask {

    /**
     *
     */
    private boolean kill = false;
    
    @Override
    public void run() {
        if (this.kill) {
            this.cancel();
            return;
        }
        
        this.fire();
    }
    
    public void fire() {
        
    }
    
    public void kill()
    {
        this.kill = true;
    }
    
}
