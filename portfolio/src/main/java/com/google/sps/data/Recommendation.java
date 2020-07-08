package com.google.sps.data;

public class Recommendation{
    public long id;
    public String name;
    public String relationship;
    public String comment;
    public Recommendation(long id, String name, String relationship, String comment){
        this.id = id;
        this.name = name;
        this.relationship = relationship;
        this.comment = comment;
    }
}