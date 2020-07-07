package com.google.sps.data;

public class Recommendation{
    public String name;
    public String relationship;
    public String comment;
    public Recommendation(String name, String relationship, String comment){
        this.name = name;
        this.relationship = relationship;
        this.comment = comment;
    }
}