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

import com.google.sps.data.RecommendationsResponse;
import com.google.sps.data.Recommendation;
import org.json.*;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.*;
import static io.restassured.RestAssured.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.sps.servlets.LoadRecommendationsServlet;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.mockito.Mockito.*;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.FetchOptions.Builder;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Entity;

@RunWith(JUnit4.class)
public class LoadRecommendationsServletTest extends LoadRecommendationsServlet{

    private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    private HttpServletRequest request;
    private HttpServletResponse response;
    private UserService userService;
    private DatastoreService dataStoreService;
    private LoadRecommendationsServlet loadRecommendationsServlet;
    
    public void login(String username, String domain, boolean isAdmin) {
        helper.setEnvAuthDomain(domain);
        helper.setEnvEmail(username + "@" + domain);
        helper.setEnvIsLoggedIn(true);
        helper.setEnvIsAdmin(isAdmin);
    }

    @Before
    public void setUp() {
        helper.setUp();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dataStoreService = DatastoreServiceFactory.getDatastoreService();
        userService = UserServiceFactory.getUserService();

        //create testing entity
        Entity recommendationEntity = new Entity("Recommendation");
        recommendationEntity.setProperty("name", "Dennis");
        recommendationEntity.setProperty("relationship", "Myself");
        recommendationEntity.setProperty("comment", "I have very tiny legs");
        recommendationEntity.setProperty("email", "denniswillie@google.com");
        recommendationEntity.setProperty("timestamp", 100);
        dataStoreService.put(recommendationEntity);

        loadRecommendationsServlet = new LoadRecommendationsServlet(dataStoreService, userService);
    }

    @Test
    public void DoGetMethod_WhenUserLoggedOut_ResponseContainCorrectRecommendationsResponseFields() throws IOException{

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        loadRecommendationsServlet.doGet(request, response);
        String testObject = stringWriter.toString();

        Gson gson = new Gson();
        RecommendationsResponse obj = gson.fromJson(testObject, RecommendationsResponse.class);  

        String expectedLoginUrl = userService.createLoginURL("/recommendations.html");
        String resultLoginUrl = obj.getUrlForLoginOrLogout();

        assertTrue(testObject.contains("\"recommendationsList\":[]")&&
                        testObject.contains("\"maxNumberofRecommendationsDisplayed\":0")&&
                        testObject.contains("\"isLoggedIn\":false")&&
                        expectedLoginUrl.equals(resultLoginUrl));
    }


    @Test
    public void DoGetMethod_WhenUserLoggedInAndQueryStringIsNull_ResponseContainCorrectRecommendationsResponseFields() throws IOException{
        login("denniswillie", "google.com", true);

        //stubbing
        when(request.getQueryString()).thenReturn(null);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        loadRecommendationsServlet.doGet(request, response);
        String testObject = stringWriter.toString();
        Gson gson = new Gson();
        RecommendationsResponse obj = gson.fromJson(testObject, RecommendationsResponse.class);  
        String expectedLogoutUrl = userService.createLogoutURL("/recommendations.html");
        String resultLogoutUrl = obj.getUrlForLoginOrLogout();

        assertTrue(testObject.contains("\"recommendationsList\":[]")&&
                        testObject.contains("\"maxNumberofRecommendationsDisplayed\":0")&&
                        testObject.contains("\"isLoggedIn\":true")&&
                        expectedLogoutUrl.equals(resultLogoutUrl));

    }

    @Test
    public void DoGetMethod_WhenUserLoggedInAndQueryStringIsNotNull_ResponseContainCorrectRecommendationsResponseFields() throws IOException{
        login("denniswillie", "google.com", true);

        //stubbing
        when(request.getQueryString()).thenReturn("?max=1");
        
        Map <String, String[]> map = new HashMap<String, String[]>();
        String[] maxValueStub = {"1"};
        map.put("max", maxValueStub);
        when(request.getParameterMap()).thenReturn(map);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        loadRecommendationsServlet.doGet(request, response);
        String testObject = stringWriter.toString();
        Gson gson = new Gson();
        RecommendationsResponse obj = gson.fromJson(testObject, RecommendationsResponse.class);  
        String expectedLogoutUrl = userService.createLogoutURL("/recommendations.html");
        String resultLogoutUrl = obj.getUrlForLoginOrLogout();

        assertTrue(testObject.contains("\"maxNumberofRecommendationsDisplayed\":1")&&
                        testObject.contains("\"isLoggedIn\":true")&&
                        expectedLogoutUrl.equals(resultLogoutUrl));

        Recommendation resultRecommendation = obj.getRecommendationsList().get(0);

        assertTrue(resultRecommendation.getName().equals("Dennis")&&
                        resultRecommendation.getRelationship().equals("Myself")&&
                        resultRecommendation.getComment().equals("I have very tiny legs")&&
                        resultRecommendation.getEmail().equals("denniswillie@google.com"));

        assertEquals(resultRecommendation.getName(),"Dennis");
        assertEquals(resultRecommendation.getRelationship(),"Myself");
        assertEquals(resultRecommendation.getComment(),"I have very tiny legs");
        assertEquals(resultRecommendation.getEmail(),"denniswillie@google.com");
    }

    @After
    public void tearDown(){
        helper.tearDown();
    }
}