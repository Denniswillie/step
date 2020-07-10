package com.google.sps.data;

public class Recommendation{
    private final long id;
    private final String name;
    private final String relationship;
    private final String comment;
    public Recommendation(long id, String name, String relationship, String comment){
        this.id = id;
        this.name = name;
        this.relationship = relationship;
        this.comment = comment;
    }

    public long getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

    public String getRelationship(){
        return this.relationship;
    }

    public String getComment(){
        return this.comment;
    }

}