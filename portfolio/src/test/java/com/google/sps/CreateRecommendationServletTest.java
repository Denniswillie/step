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
public class CreateRecommendationServletTest extends CreateRecommendationServlet{

    private HttpServletRequest request;
    private HttpServletResponse response;
    private DatastoreService dataStoreService;
    private UserService userService;
    private CreateRecommendationServlet createRecommendationServlet;

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
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dataStoreService = DatastoreServiceFactory.getDatastoreService();
        userService = UserServiceFactory.getUserService();

        //stubbing
        when(request.getParameter("name")).thenReturn("Dennis");
        when(request.getParameter("relationship")).thenReturn("Myself");
        when(request.getParameter("comment")).thenReturn("I have very tiny legs");

        createRecommendationServlet = new CreateRecommendationServlet(userService, dataStoreService);
    }

    @Test
    public void DoPostMethod_WhenUserLoggedIn_DatabaseHasOneEntityWithKindNameRecommendation() throws IOException {

        login("denniswillie", "google.com", true);

        createRecommendationServlet.doPost(request, response);

        //create and send query to database
        Query testQuery = new Query("Recommendation");
        Entity testResult = dataStoreService.prepare(testQuery).asSingleEntity();
        
        //assert that the datastore has 1 entity with the kind name = "Recommendation" with all the properties
        assertEquals(testResult.getProperty("name"),"Dennis");
        assertEquals(testResult.getProperty("relationship"),"Myself");
        assertEquals(testResult.getProperty("comment"),"I have very tiny legs");
        assertEquals(testResult.getProperty("email"),"denniswillie@google.com");

    }

    @Test
    public void DoPostMethod_WhenUserLoggedOut_DatabaseHasNoEntities() throws IOException {

        createRecommendationServlet.doPost(request, response);

        //create and send query to database
        Query testQuery = new Query("Recommendation");
        PreparedQuery testResult = dataStoreService.prepare(testQuery);
        
        //assert that the datastore has no entity 
        assertEquals(0, testResult.countEntities(FetchOptions.Builder.withLimit(10)));
    }


    @Test
    public void DoPostMethod_WhenUserLoggedIn_ResponseSendRedirectToRecommendationsHtml() throws IOException{
        login("denniswillie", "google.com", true);

        createRecommendationServlet.doPost(request, response);

        verify(response).sendRedirect("/recommendations.html");
    }    

    @Test
    public void DoPostMethod_WhenUserLoggedOut_ResponseRedirectToLoginURLWithDestinationURLRecommendationsHtml() throws IOException{

        createRecommendationServlet.doPost(request, response);

        verify(response).sendRedirect(userService.createLoginURL("/recommendations.html"));
    }    

    @After
    public void tearDown(){
        helper.tearDown();
    }
}