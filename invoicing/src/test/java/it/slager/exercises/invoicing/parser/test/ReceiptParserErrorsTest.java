package it.slager.exercises.invoicing.parser.test;

import org.junit.Before;
import org.junit.Test;

import it.slager.exercises.invoicing.parser.ReceiptLineParser;

public class ReceiptParserErrorsTest {

	private ReceiptLineParser parser = null;

	@Before
	public void setupParser() {
		parser = new ReceiptLineParser("foo", "bar");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyLine() {
		parser.parse("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullLine() {
		parser.parse(null);
	}
}
