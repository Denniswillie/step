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

@WebServlet("/create-recommendation")
public class CreateRecommendation extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    UserService userService = UserServiceFactory.getUserService();
    
    String name = request.getParameter("name");
    String relationship = request.getParameter("relationship");
    String comment = request.getParameter("comment");
    String email = userService.getCurrentUser().getEmail();
    System.out.print(email);
    long timestamp = System.currentTimeMillis();

    Entity recommendationEntity = new Entity("Recommendation");
    recommendationEntity.setProperty("name", name);
    recommendationEntity.setProperty("relationship", relationship);
    recommendationEntity.setProperty("comment", comment);
    recommendationEntity.setProperty("email", email);
    recommendationEntity.setProperty("timestamp", timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(recommendationEntity);

    response.sendRedirect("/recommendations.html");
  }
}