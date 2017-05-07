package it.slager.exercises.invoicing.inttest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import it.slager.exercises.invoicing.model.ReceiptItem;
import it.slager.exercises.invoicing.parser.ReceiptLineParser;
import it.slager.exercises.invoicing.printer.InvoicePrinter;
import it.slager.exercises.invoicing.processor.ReceiptItemTaxEvaluator;

@RunWith(Parameterized.class)
public class InvoicingEndToEndIT {
	private static ReceiptLineParser parser;

	private static ReceiptItemTaxEvaluator taxEvaluator;

	private static InvoicePrinter printer;

	@Parameter(value = 0)
	public String testCaseID;

	@Parameter(value = 1)
	public String[] receiptLines;

	@Parameter(value = 2)
	public String expectedOutput;

	@BeforeClass
	public static void setupEndToEnd() {
		parser = new ReceiptLineParser("imported", "at");
		taxEvaluator = new ReceiptItemTaxEvaluator("0.10", "0.05");
		printer = new InvoicePrinter();
	}

	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		ArrayList<Object[]> testCases = new ArrayList<>();

		// @formatter:off
		testCases.add(new Object[] {
				"1",
				
				new String[] {"1 book at 12.49",
				"1 music CD at 14.99",
				"1 chocolate bar at 0.85"}, 
				
				new String("1 book: 12.49" + System.lineSeparator()
				+ "1 music CD: 16.49" + System.lineSeparator()
				+ "1 chocolate bar: 0.85" + System.lineSeparator()
				+ "Sales Taxes: 1.50" + System.lineSeparator()
				+ "Total: 29.83")});
		
		testCases.add(new Object[] {
				"2",
				
				new String[] {"1 imported box of chocolates at 10.00",
				"1 imported bottle of perfume at 47.50"}, 
				
				new String("1 imported box of chocolates: 10.50" + System.lineSeparator()
				+ "1 imported bottle of perfume: 54.65" + System.lineSeparator()
				+ "Sales Taxes: 7.65" + System.lineSeparator()
				+ "Total: 65.15")});
		
		testCases.add(new Object[] {
				"3",
				
				new String[] {"1 imported bottle of perfume at 27.99",
						"1 bottle of perfume at 18.99",
						"1 packet of headache pills at 9.75",
						"1 imported box of chocolates at 11.25"}, 
				
				new String("1 imported bottle of perfume: 32.19" + System.lineSeparator()
					+ "1 bottle of perfume: 20.89" + System.lineSeparator()
					+ "1 packet of headache pills: 9.75" + System.lineSeparator()
					+ "1 imported box of chocolates: 11.85" + System.lineSeparator()
					+ "Sales Taxes: 6.70" + System.lineSeparator()
					+ "Total: 74.68")});
		// @formatter:on

		return testCases;
	}

	@Test
	public void test() throws IOException {

		// Printer setup
		StringWriter sw = new StringWriter();
		printer.setWriter(sw);
		printer.init();

		// Execution
		System.out.println("Input " + testCaseID + ":");
		ReceiptItem item;
		for (String receiptLine : receiptLines) {
			System.out.println(receiptLine);
			item = parser.parse(receiptLine);
			taxEvaluator.evaluateTaxation(item);
			printer.printItem(item);
		}
		printer.closeInvoice();

		// Assertions
		System.out.println("Output " + testCaseID + ":");
		System.out.println(sw.toString());
		assertEquals("Output", expectedOutput, sw.toString());
	}

}
