import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
@Slf4j
public class SampleTest {

	@Test
	public void checkScheduledTimeDifference() {
		log.info(String
				.valueOf(Duration.between(Instant.ofEpochMilli(1637553459376l), Instant.ofEpochMilli(1637553534375l))));
	}
}
