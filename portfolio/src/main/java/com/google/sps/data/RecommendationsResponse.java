package com.google.sps.data;

import com.google.sps.data.Recommendation;
import java.util.List;

public class RecommendationsResponse{
    private final List<Recommendation> recommendationsList;
    private final int maxNumberofRecommendationsDisplayed;

    public RecommendationsResponse(List<Recommendation> recommendationsList, int maxNumberofRecommendationsDisplayed){
        this.recommendationsList = recommendationsList;
        this.maxNumberofRecommendationsDisplayed = maxNumberofRecommendationsDisplayed;
    }

    public List<Recommendation> getRecommendationsList(){
        return recommendationsList;
    }

    public int maxNumberofRecommendationsDisplayed(){
        return maxNumberofRecommendationsDisplayed;
    }
}