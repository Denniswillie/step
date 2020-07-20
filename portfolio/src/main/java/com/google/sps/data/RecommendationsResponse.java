// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.data;

import com.google.sps.data.Recommendation;
import java.util.List;
import java.util.ArrayList;

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

    public RecommendationsResponse(boolean isLoggedIn, String urlForLoginOrLogout){
        this.recommendationsList = new ArrayList<Recommendation>();
        this.maxNumberofRecommendationsDisplayed = 0;
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

    public String getUrlForLoginOrLogout(){
        return urlForLoginOrLogout;
    }
}