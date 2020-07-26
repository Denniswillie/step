package com.google.sps;

public class ModifiableTimeRange{
    
    private int start;
    private int duration;

    public ModifiableTimeRange(int start, int duration){
        this.start = start;
        this.duration = duration;
    }

    public int start(){
        return start;
    }

    public int duration(){
        return duration;
    }

    public int end(){
        return start + duration;
    }

    public void setStart(int start){
        this.start = start;
    }

    public void setDuration(int duration){
        this.duration = duration;
    }
}