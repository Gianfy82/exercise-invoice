package it.slager.exercises.invoicing.printer.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;

import org.junit.BeforeClass;
import org.junit.Test;

import it.slager.exercises.invoicing.model.ReceiptItem;
import it.slager.exercises.invoicing.printer.InvoicePrinter;

public class InvoicePrinterTest {

	/**
	 * The instance is common across cases, to simulate its reuse
	 */
	private static InvoicePrinter printer;

	@BeforeClass
	public static void setupPrinter() {
		printer = new InvoicePrinter();
	}

	@Test
	public void test1() throws IOException {
		StringWriter sw = new StringWriter();
		printer.setWriter(sw);

		printer.init();

		// Trivial case
		ReceiptItem item1 = new ReceiptItem();
		item1.setAmount(1);
		item1.setDescription("book");
		item1.setImported(false);
		item1.setNetUnitPrice(new BigDecimal("12.49"));
		item1.setTaxUnitPrice(BigDecimal.ZERO);

		printer.printItem(item1);

		// Composite description
		ReceiptItem item2 = new ReceiptItem();
		item2.setAmount(1);
		item2.setDescription("music CD");
		item2.setImported(false);
		item2.setNetUnitPrice(new BigDecimal("14.99"));
		item2.setTaxUnitPrice(new BigDecimal("1.50"));

		printer.printItem(item2);

		// Imported item
		ReceiptItem item3 = new ReceiptItem();
		item3.setAmount(1);
		item3.setDescription("chocolate bar");
		item3.setImported(false);
		item3.setNetUnitPrice(new BigDecimal("0.85"));
		item3.setTaxUnitPrice(BigDecimal.ZERO);

		printer.printItem(item3);

		printer.closeInvoice();

		// Assertions
		// @formatter:off
		String expectedTest1 = "1 book: 12.49" + System.lineSeparator()
				+ "1 music CD: 16.49" + System.lineSeparator()
				+ "1 chocolate bar: 0.85" + System.lineSeparator()
				+ "Sales Taxes: 1.50" + System.lineSeparator()
				+ "Total: 29.83";
		// @formatter:on

		System.out.println(sw.toString());
		assertEquals("Output", expectedTest1, sw.toString());
	}

	@Test
	public void test2() throws IOException {
		StringWriter sw = new StringWriter();
		printer.setWriter(sw);

		printer.init();

		// Trivial case
		ReceiptItem item1 = new ReceiptItem();
		item1.setAmount(1);
		item1.setDescription("box of chocolates");
		item1.setImported(true);
		item1.setNetUnitPrice(new BigDecimal("10.00"));
		item1.setTaxUnitPrice(new BigDecimal("0.50"));

		printer.printItem(item1);

		// Composite description
		ReceiptItem item2 = new ReceiptItem();
		item2.setAmount(1);
		item2.setDescription("bottle of perfume");
		item2.setImported(true);
		item2.setNetUnitPrice(new BigDecimal("47.50"));
		item2.setTaxUnitPrice(new BigDecimal("7.15"));

		printer.printItem(item2);

		printer.closeInvoice();

		// Calling closeInvoice more than once has no effect
		printer.closeInvoice();

		// Assertions
		// @formatter:off
		String expectedTest2 = "1 imported box of chocolates: 10.50" + System.lineSeparator()
				+ "1 imported bottle of perfume: 54.65" + System.lineSeparator()
				+ "Sales Taxes: 7.65" + System.lineSeparator()
				+ "Total: 65.15";
		// @formatter:on

		System.out.println(sw.toString());
		assertEquals("Output", expectedTest2, sw.toString());
	}
}
