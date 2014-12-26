package jk.kamoru.crazy;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations={"file:src/main/webapp/WEB-INF/spring/video-context.xml"})
@ContextConfiguration

public class TestCrazy {

	@Test
	public void test() {
		assertTrue("UTF-8".equals(CRAZY.CHARSET));
	}

}
