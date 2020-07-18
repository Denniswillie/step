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

package com.google.sps.servlets;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.sps.data.Recommendation;
import com.google.sps.data.RecommendationsResponse;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@WebServlet("/load-recommendations")
public class LoadRecommendationsServlet extends HttpServlet {

  private int maxNumberOfRecommendations = 0;
  private final DatastoreService dataStoreService;
  private final UserService userService;

  //for testing
  public LoadRecommendationsServlet(DatastoreService dataStoreService, UserService userService){
    this.dataStoreService = dataStoreService;
    this.userService = userService;
  } 

  //for production
  public LoadRecommendationsServlet(){
      dataStoreService = DatastoreServiceFactory.getDatastoreService();
      userService = UserServiceFactory.getUserService();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    Gson gson = new Gson();
    response.setContentType("application/json;");
    
    //user is not logged in, then will send login url without reading from database
    if(!userService.isUserLoggedIn()){
        RecommendationsResponse recommendationsResponse = new RecommendationsResponse(false, userService.createLoginURL("/recommendations.html"));
        response.getWriter().println(gson.toJson(recommendationsResponse)); 
    }

    else{
        Query query = new Query("Recommendation").addSort("timestamp", SortDirection.DESCENDING);
        PreparedQuery results = dataStoreService.prepare(query);

        if(request.getQueryString() != null){
            maxNumberOfRecommendations = Integer.parseInt(request.getParameterMap().get("max")[0]);
        }

        List<Recommendation> recommendations = new ArrayList<>();
        for (Entity entity : results.asIterable()) {
            if(recommendations.size() >= maxNumberOfRecommendations){
                break;
            }
            long id = entity.getKey().getId();
            String name = (String) entity.getProperty("name");
            String relationship = (String) entity.getProperty("relationship");
            String comment = (String) entity.getProperty("comment");
            String email = (String) entity.getProperty("email");
            long timestamp = (long) entity.getProperty("timestamp");

            Recommendation recommendation = new Recommendation(id, name, relationship, comment, email);
            recommendations.add(recommendation);
        }

        RecommendationsResponse recommendationsResponse = new RecommendationsResponse(recommendations, maxNumberOfRecommendations, true, userService.createLogoutURL("/recommendations.html"));
        response.getWriter().println(gson.toJson(recommendationsResponse)); 
    }
  }

}