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

import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.sps.servlets.CreateRecommendationServlet;
import org.junit.Assert;
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

@RunWith(JUnit4.class)
public class CreateRecommendationServletTest extends CreateRecommendationServlet{
    private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    
    public void login(String username, String domain, boolean isAdmin) {
        helper.setEnvAuthDomain(domain);
        helper.setEnvEmail(username + "@" + domain);
        helper.setEnvIsLoggedIn(true);
        helper.setEnvIsAdmin(isAdmin);
    }

    @Before
    public void setUp() {
        helper.setUp();
    }

    //test if datastore has correct data when user is logged in
    @Test
    public void dataStoreHasCorrectDataIfLoggedInTest() throws IOException {

        login("denniswillie", "google.com", true);//true indicates user is an admin

        // create mock objects
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        //create datastore
        DatastoreService dataStoreService = DatastoreServiceFactory.getDatastoreService();

        //stubbing
        when(request.getParameter("name")).thenReturn("Dennis");
        when(request.getParameter("relationship")).thenReturn("Myself");
        when(request.getParameter("comment")).thenReturn("I have very tiny legs");

        //create servlet and call doPost method
        CreateRecommendationServlet createRecommendationServlet = new CreateRecommendationServlet(userService, dataStoreService);
        createRecommendationServlet.doPost(request, response);

        //create and send query to database
        Query testQuery = new Query("Recommendation");
        PreparedQuery testResult = dataStoreService.prepare(testQuery);
        
        //assert that the datastore has 1 entity with the kind name = "Recommendation" with all the properties
        Gson gson = new Gson();
        String testObject = gson.toJson(testResult.asSingleEntity());
        System.out.println(testObject);
        Assert.assertTrue(testObject.contains("\"name\":\"Dennis\"")&&
                        testObject.contains("\"comment\":\"I have very tiny legs\"")&&
                        testObject.contains("\"relationship\":\"Myself\"")&&
                        testObject.contains("\"email\":\"denniswillie@google.com\""));

    }

    //assert datastore doesn't have any entities if user is logged out
    @Test
    public void dataStoreHasNoEntitiesIfLoggedOutTest() throws IOException {

        // create mock objects
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        //create datastore
        DatastoreService dataStoreService = DatastoreServiceFactory.getDatastoreService();

        //stubbing
        when(request.getParameter("name")).thenReturn("Dennis");
        when(request.getParameter("relationship")).thenReturn("Myself");
        when(request.getParameter("comment")).thenReturn("I have very tiny legs");

        //create servlet and call doPost method
        CreateRecommendationServlet createRecommendationServlet = new CreateRecommendationServlet(userService, dataStoreService);
        createRecommendationServlet.doPost(request, response);

        //create and send query to database
        Query testQuery = new Query("Recommendation");
        PreparedQuery testResult = dataStoreService.prepare(testQuery);
        
        //assert that the datastore has no entity 
        Assert.assertEquals(0, testResult.countEntities(FetchOptions.Builder.withLimit(10)));
    }


    @Test
    public void responseRedirectIfLoggedInTest() throws IOException{
        login("denniswillie", "google.com", true);//true indicates user is an admin

        // create mock objects
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        //create datastore
        DatastoreService dataStoreService = DatastoreServiceFactory.getDatastoreService();

        //stubbing
        when(request.getParameter("name")).thenReturn("Dennis");
        when(request.getParameter("relationship")).thenReturn("Myself");
        when(request.getParameter("comment")).thenReturn("I have very tiny legs");

        //create servlet and call doPost method
        CreateRecommendationServlet createRecommendationServlet = new CreateRecommendationServlet(userService, dataStoreService);
        createRecommendationServlet.doPost(request, response);
    }    

    // @Test
    // public void responseRedirectIfLoggedOutTest() throws IOException{
    //     login("denniswillie", "google.com", true);//true indicates user is an admin

    //     // create mock objects
    //     HttpServletRequest request = mock(HttpServletRequest.class);
    //     HttpServletResponse response = mock(HttpServletResponse.class);

    //     //create datastore
    //     DatastoreService dataStoreService = DatastoreServiceFactory.getDatastoreService();

    //     //stubbing
    //     when(request.getParameter("name")).thenReturn("Dennis");
    //     when(request.getParameter("relationship")).thenReturn("Myself");
    //     when(request.getParameter("comment")).thenReturn("I have very tiny legs");

    //     //create servlet and call doPost method
    //     CreateRecommendationServlet createRecommendationServlet = new CreateRecommendationServlet(userService, dataStoreService);
    //     createRecommendationServlet.doPost(request, response);
    // }    

    // @Test
    // public void doPostHandlesParametersCorrectlyTest(){

    // }

    @After
    public void tearDown() {
        helper.tearDown();
    }
}