package it.slager.exercises.invoicing.parser.test;

import org.junit.Before;
import org.junit.Test;

import it.slager.exercises.invoicing.model.ReceiptItem;
import it.slager.exercises.invoicing.parser.ReceiptLineParser;

public class ReceiptParserTest {
	
	public static String test1 = "1 book at 12.49"; 

	private ReceiptLineParser parser = null;
	
	@Before
	public void SetupParser() {
		parser = new ReceiptLineParser();
	}
	
	@Test
	public void test() {
		ReceiptItem item = parser.parse(test1);
	}

}
