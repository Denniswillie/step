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

import org.json.*;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.sps.servlets.LoadRecommendationsServlet;
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
public class LoadRecommendationsServletTest extends LoadRecommendationsServlet{
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

    @Test
    public void userIsNotLoggedInTest() throws IOException{

        //mock objects
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        //create datastore and userservice
        DatastoreService dataStoreService = DatastoreServiceFactory.getDatastoreService();
        UserService userService = UserServiceFactory.getUserService();

        //create servlet
        LoadRecommendationsServlet loadRecommendationsServlet = new LoadRecommendationsServlet(dataStoreService, userService);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        loadRecommendationsServlet.doGet(request, response);
        String testObject = stringWriter.toString();
        JSONObject json = new JSONObject(testObject);
        System.out.println(json."urlForLoginOrLogout");
        // Assert.assertTrue(testObject.contains("\"recommendationsList\":[]")&&
        //                 testObject.contains("\"maxNumberofRecommendationsDisplayed\":0")&&
        //                 testObject.contains("\"isLoggedIn\":false")&&
        //                 json.urlForLoginOrLogout == userService.createLoginURl("/recommendations.html"));
    }

    @After
    public void tearDown(){
        helper.tearDown();
    }
}