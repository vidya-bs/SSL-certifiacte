import org.junit.Test;

import java.time.Duration;
import java.time.Instant;

public class SampleTest {


    @Test
    public void checkScheduledTimeDifference() {
        System.out.println(Duration.between(Instant.ofEpochMilli(1637553459376l), Instant.ofEpochMilli(1637553534375l)));
    }
}
