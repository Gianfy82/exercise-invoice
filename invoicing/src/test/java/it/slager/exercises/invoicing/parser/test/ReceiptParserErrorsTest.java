package it.slager.exercises.invoicing.parser.test;

import org.junit.Before;
import org.junit.Test;

import it.slager.exercises.invoicing.parser.ReceiptLineParser;
import it.slager.exercises.invoicing.parser.ReceiptParseException;

public class ReceiptParserErrorsTest {

	private ReceiptLineParser parser = null;

	@Before
	public void setupParser() {
		parser = new ReceiptLineParser("foo", "bar");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyLine() throws ReceiptParseException {
		parser.parse("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullLine() throws ReceiptParseException {
		parser.parse(null);
	}

	@Test(expected = ReceiptParseException.class)
	public void testUnexpectedToken() throws ReceiptParseException {
		parser.parse("1 imported at 5.00");
	}

	@Test(expected = ReceiptParseException.class)
	public void testWrongNumberFormat() throws ReceiptParseException {
		parser.parse("1 imported at foo");
	}
}
