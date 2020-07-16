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
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@WebServlet("/load-recommendations")
public class LoadRecommendationsServlet extends HttpServlet {

  private int maxNumberOfRecommendations = 0;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    Gson gson = new Gson();
    response.setContentType("application/json;");
    
    UserService userService = UserServiceFactory.getUserService();
    
    Query query = new Query("Recommendation").addSort("timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    if(request.getQueryString() != null){
        maxNumberOfRecommendations = queryStringParser(request.getQueryString());
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

    if(userService.isUserLoggedIn()){
        RecommendationsResponse recommendationsResponse = new RecommendationsResponse(recommendations, maxNumberOfRecommendations, true, userService.createLogoutURL("/recommendations.html"));
        response.getWriter().println(gson.toJson(recommendationsResponse));
    }
    else{
        RecommendationsResponse recommendationsResponse = new RecommendationsResponse(recommendations, maxNumberOfRecommendations, false, userService.createLoginURL("/recommendations.html"));
        response.getWriter().println(gson.toJson(recommendationsResponse));
    }

  }

  public int queryStringParser(String queryString){
      String keyValuePair[] = queryString.split("=");
      return Integer.parseInt(keyValuePair[1]);
  }

}