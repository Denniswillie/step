import com.google.sps.servlets.CreateRecommendation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.mockito.Mockito.*;
import static org.junit.Assert.assertTrue;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

@RunWith(JUnit4.class)
public class CreateRecommendationTest extends CreateRecommendation{
    private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
     new LocalTaskQueueTestConfig(), new LocalDatastoreServiceTestConfig());
    
    @Before
    public void setUp() {
        helper.setUp();
    }

    @Test
    public void dataStoreHasCorrectDataTest{

    }

    @Test
    public void {

    }    

    @After
    public void tearDown() {
        helper.tearDown();
    }
}