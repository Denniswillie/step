package com.google.sps.data;

public class Recommendation{
    private final long id;
    private final String name;
    private final String relationship;
    private final String comment;
    private final String email;
    public Recommendation(long id, String name, String relationship, String comment, String email){
        this.id = id;
        this.name = name;
        this.relationship = relationship;
        this.comment = comment;
        this.email = email;
    }

    public long getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public String getRelationship(){
        return relationship;
    }
    public String getComment(){
        return comment;
    }

    public String getEmail(){
        return email;
    }
}