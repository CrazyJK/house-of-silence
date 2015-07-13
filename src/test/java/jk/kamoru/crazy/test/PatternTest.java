package jk.kamoru.crazy.test;

import java.util.regex.Pattern;

public class PatternTest {

	public static void main(String[] args) {
		System.out.println(Pattern.matches("[a-z|A-Z]", "2015.05.13"));
		System.out.println(Pattern.matches("^[a-zA-Z\\s,]+", "Yuko Shiraki, aaa"));
	}

}
