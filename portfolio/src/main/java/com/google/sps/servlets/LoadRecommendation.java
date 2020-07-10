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
import com.google.sps.data.FetchedData;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/load-recommendation")
public class LoadRecommendation extends HttpServlet {

    //maximum number of recommendations
    private int maxNumberOfRecommendations = 0;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Recommendation").addSort("timestamp", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Recommendation> recommendations = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      if(recommendations.size() >= maxNumberOfRecommendations){
          break;
      }
      long id = entity.getKey().getId();
      String name = (String) entity.getProperty("name");
      String relationship = (String) entity.getProperty("relationship");
      String comment = (String) entity.getProperty("comment");
      long timestamp = (long) entity.getProperty("timestamp");

      Recommendation recommendation = new Recommendation(id, name, relationship, comment);
      recommendations.add(recommendation);
    }

    FetchedData fetchedData = new FetchedData(recommendations, this.maxNumberOfRecommendations);

    Gson gson = new Gson();

    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(fetchedData));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
      this.maxNumberOfRecommendations = Integer.parseInt(request.getParameter("maxNumberofRecommendationsDisplayed"));
      response.sendRedirect("/recommendations.html");

  }
}