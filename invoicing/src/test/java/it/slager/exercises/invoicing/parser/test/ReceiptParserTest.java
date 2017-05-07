package it.slager.exercises.invoicing.parser.test;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import it.slager.exercises.invoicing.model.ReceiptItem;
import it.slager.exercises.invoicing.parser.ReceiptLineParser;

@RunWith(Parameterized.class)
public class ReceiptParserTest {

	@Parameter(value = 0)
	public String inputLine;

	@Parameter(value = 1)
	public ReceiptItem expectedOutcome;

	private ReceiptLineParser parser = null;

	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		ArrayList<Object[]> testCases = new ArrayList<>();

		// Trivial case
		ReceiptItem item1 = new ReceiptItem();
		testCases.add(new Object[] { "1 book at 12.49", item1 });
		item1.setAmount(1);
		item1.setDescription("book");
		item1.setImported(false);
		item1.setNetUnitPrice(new BigDecimal("12.49"));

		// Composite description
		ReceiptItem item2 = new ReceiptItem();
		testCases.add(new Object[] { "1 chocolate bar at 0.85", item2 });
		item2.setAmount(1);
		item2.setDescription("chocolate bar");
		item2.setImported(false);
		item2.setNetUnitPrice(new BigDecimal("0.85"));

		// Imported item
		ReceiptItem item3 = new ReceiptItem();
		testCases.add(new Object[] { "1 imported bottle of perfume at 47.50", item3 });
		item3.setAmount(1);
		item3.setDescription("bottle of perfume");
		item3.setImported(true);
		item3.setNetUnitPrice(new BigDecimal("47.50"));

		// More than one element for a given item
		ReceiptItem item4 = new ReceiptItem();
		testCases.add(new Object[] { "2 imported box of chocolates at 11.85", item4 });
		item4.setAmount(2);
		item4.setDescription("box of chocolates");
		item4.setImported(true);
		item4.setNetUnitPrice(new BigDecimal("11.85"));

		return testCases;
	}

	@Before
	public void setupParser() {
		parser = new ReceiptLineParser("imported", "at");
	}

	@Test
	public void test() {

		ReceiptItem actualItem = parser.parse(inputLine);

		assertEquals("Item content", expectedOutcome, actualItem);
	}
}
