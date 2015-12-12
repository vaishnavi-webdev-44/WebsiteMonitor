import org.junit.Test;
import java.io.IOException;

public class WebsiteFetcherTest {

    // Fetch the content of a site. This has a dependency on wikipedia.org, which
    // makes this not a unit test.
    @Test
    public void TestFetchWebsite()
    {
        try
        {
            String content = WebsiteFetcher.FetchContent("https://www.wikipedia.org/");
            assert(content != null);
        }
        catch (IOException ex)
        {
            assert(false);
        }
    }
}
