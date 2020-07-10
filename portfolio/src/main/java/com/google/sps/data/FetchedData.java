package com.google.sps.data;

import com.google.sps.data.Recommendation;
import java.util.List;

public class FetchedData{
    private final List<Recommendation> recommendationsList;
    private final int maxNumberofRecommendationsDisplayed;

    public FetchedData(List<Recommendation> recommendationsList, int maxNumberofRecommendationsDisplayed){
        this.recommendationsList = recommendationsList;
        this.maxNumberofRecommendationsDisplayed = maxNumberofRecommendationsDisplayed;
    }

    public List<Recommendation> getRecommendationsList(){
        return this.recommendationsList;
    }

    public int maxNumberofRecommendationsDisplayed(){
        return this.maxNumberofRecommendationsDisplayed;
    }
}