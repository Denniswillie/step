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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
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
        login("denniswillie", "google.com", true);
    }

    @Test
    public void dataStoreHasCorrectDataTest() throws IOException {

        // create mock objects
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        //create datastore
        DatastoreService dataStoreService = DatastoreServiceFactory.getDatastoreService();

        //create userservice
        UserService userService = UserServiceFactory.getUserService();

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
        
        //assert that the datastore has 1 entity with the kind name = "Recommendation"
        assertEquals(1, dataStoreService.prepare(new Query("Recommendation")).countEntities(FetchOptions.Builder.withLimit(10)));

        
    }

    // @Test
    // public void responseRedirectTest(){

    // }    

    // @Test
    // public void doPostHandlesParametersCorrectlyTest(){

    // }

    @After
    public void tearDown() {
        helper.tearDown();
    }
}