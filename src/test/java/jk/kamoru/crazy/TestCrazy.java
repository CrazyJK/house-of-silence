package jk.kamoru.crazy;

import static org.junit.Assert.assertEquals;

import java.util.regex.Pattern;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import jk.kamoru.util.ArrayUtils;

@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations={"file:src/main/webapp/WEB-INF/spring/video-context.xml"})
@ContextConfiguration
public class TestCrazy {

	@Test
	public void test() {
		assertEquals("UTF-8", CRAZY.CHARSET);
	}

	@Test
	public void testPattern() {
		assertEquals(Pattern.matches("[a-z|A-Z]", "2015.05.13"), false);
		assertEquals(Pattern.matches("^[a-zA-Z\\s,]+", "Yuko Shiraki, aaa"), true);
	}

	@Test
	public void testArrayUtils_toStringComma() {
		// 빈 배열이나 null일 경우에 공백 리턴인지 검사
		String[] someArray = new String[]{};
		assertEquals(ArrayUtils.toStringComma(someArray), "");
		assertEquals(ArrayUtils.toStringComma(null), "");
	}
}
