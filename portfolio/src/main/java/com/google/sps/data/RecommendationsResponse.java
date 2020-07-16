package com.google.sps.data;

import com.google.sps.data.Recommendation;
import java.util.List;

public class RecommendationsResponse{
    private final List<Recommendation> recommendationsList;
    private final int maxNumberofRecommendationsDisplayed;
    private final boolean isLoggedIn;
    private final String urlForLoginOrLogout;

    public RecommendationsResponse(List<Recommendation> recommendationsList, int maxNumberofRecommendationsDisplayed, boolean isLoggedIn, String urlForLoginOrLogout){
        this.recommendationsList = recommendationsList;
        this.maxNumberofRecommendationsDisplayed = maxNumberofRecommendationsDisplayed;
        this.isLoggedIn = isLoggedIn;
        this.urlForLoginOrLogout = urlForLoginOrLogout;
    }

    public List<Recommendation> getRecommendationsList(){
        return recommendationsList;
    }
    
    public int getMaxNumberofRecommendationsDisplayed(){
        return maxNumberofRecommendationsDisplayed;
    }

    public boolean isLoggedIn(){
        return isLoggedIn;
    }

    public String getURLforLoginOrLogout(){
        return urlForLoginOrLogout;
    }
}