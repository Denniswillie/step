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
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.users.User;

@WebServlet("/create-recommendation")
public class CreateRecommendationServlet extends HttpServlet {

  private final UserService userService;
  private final DatastoreService dataStoreService;

  //for production
  public CreateRecommendationServlet(){
      userService = UserServiceFactory.getUserService();
      dataStoreService = DatastoreServiceFactory.getDatastoreService();
  }

  //for testing
  public CreateRecommendationServlet(UserService userService, DatastoreService dataStoreService){
      this.userService = userService;
      this.dataStoreService = dataStoreService;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    if(userService.isUserLoggedIn()){
        String name = request.getParameter("name");
        String relationship = request.getParameter("relationship");
        String comment = request.getParameter("comment");
        String email = userService.getCurrentUser().getEmail();
        long timestamp = System.currentTimeMillis();

        Entity recommendationEntity = new Entity("Recommendation");
        recommendationEntity.setProperty("name", name);
        recommendationEntity.setProperty("relationship", relationship);
        recommendationEntity.setProperty("comment", comment);
        recommendationEntity.setProperty("email", email);
        recommendationEntity.setProperty("timestamp", timestamp);

        dataStoreService.put(recommendationEntity);

        response.sendRedirect("/recommendations.html");
    }

    else{
        //user will be prompted to login
        response.sendRedirect(userService.createLoginURL("/recommendations.html"));
    }
    
  }
}