package jk.kamoru.crazy;

import static org.junit.Assert.*;

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
	
	@Test
	public void testSubstring() {
		String releaseDate = "2015.11.11";
		String subPath = releaseDate.substring(0, 7).replace(".", "-");
		assertEquals(subPath, "2015-11");
	}
	
	@Test
	public void testDatePattern() {
		String regex = CRAZY.REGEX_DATE;

		assertFalse("Date: matched.", Pattern.matches(regex, "2011.1.1"));
		assertFalse("Date: matched.", Pattern.matches(regex, "2011.01.1"));
		assertFalse("Date: matched.", Pattern.matches(regex, "2011.1.01"));
		assertTrue("Date: matched.", Pattern.matches(regex, "2011.01.01"));
		assertFalse("Date (month): not matched.", Pattern.matches(regex, "2011.13.1"));
	}
}
