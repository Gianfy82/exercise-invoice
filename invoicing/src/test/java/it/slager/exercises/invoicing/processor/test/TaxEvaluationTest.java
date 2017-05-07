package it.slager.exercises.invoicing.processor.test;

import static org.junit.Assert.assertTrue;

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
import it.slager.exercises.invoicing.processor.ReceiptItemTaxEvaluator;

@RunWith(Parameterized.class)
public class TaxEvaluationTest {

	@Parameter(value = 0)
	public ReceiptItem inputItem;

	@Parameter(value = 1)
	public BigDecimal expectedTaxation;

	private ReceiptItemTaxEvaluator evaluator = null;

	@Parameters(name = "{0}, taxShouldBe={1}")
	public static Collection<Object[]> data() {
		ArrayList<Object[]> testCases = new ArrayList<>();

		// Not taxed good, not imported - 1 book : 12.49
		ReceiptItem item1 = new ReceiptItem();
		testCases.add(new Object[] { item1, BigDecimal.ZERO });
		item1.setAmount(1);
		item1.setDescription("book");
		item1.setImported(false);
		item1.setNetUnitPrice(new BigDecimal("12.49"));

		// Taxed good, not imported - 1 music CD: 16.49
		ReceiptItem item2 = new ReceiptItem();
		testCases.add(new Object[] { item2, new BigDecimal("1.50") });
		item2.setAmount(1);
		item2.setDescription("music CD");
		item2.setImported(false);
		item2.setNetUnitPrice(new BigDecimal("14.99"));

		// Not taxed good, imported - 1 imported box of chocolates: 10.50
		ReceiptItem item3 = new ReceiptItem();
		testCases.add(new Object[] { item3, new BigDecimal("0.50") });
		item3.setAmount(1);
		item3.setDescription("box of chocolates");
		item3.setImported(true);
		item3.setNetUnitPrice(new BigDecimal("10.00"));

		// Taxed good, imported - 1 imported bottle of perfume: 32.19
		ReceiptItem item4 = new ReceiptItem();
		testCases.add(new Object[] { item4, new BigDecimal("4.20") });
		item4.setAmount(1);
		item4.setDescription("bottle of perfume");
		item4.setImported(true);
		item4.setNetUnitPrice(new BigDecimal("27.99"));

		// More than one element for a given item
		ReceiptItem item5 = new ReceiptItem();
		testCases.add(new Object[] { item5, new BigDecimal("4.20") });
		item5.setAmount(2);
		item5.setDescription("bottle of perfume");
		item5.setImported(true);
		item5.setNetUnitPrice(new BigDecimal("27.99"));

		return testCases;
	}

	@Before
	public void setupParser() {
		evaluator = new ReceiptItemTaxEvaluator("0.10", "0.05", "0.05");
	}

	@Test
	public void test() {

		evaluator.evaluateTaxation(inputItem);

		assertTrue("expectedTaxation=" + expectedTaxation + " actualTaxation=" + inputItem.getTaxUnitPrice(),
				expectedTaxation.compareTo(inputItem.getTaxUnitPrice()) == 0);
	}
}
