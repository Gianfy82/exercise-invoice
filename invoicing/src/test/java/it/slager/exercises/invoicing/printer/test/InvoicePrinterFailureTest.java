package it.slager.exercises.invoicing.printer.test;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;

import org.junit.BeforeClass;
import org.junit.Test;

import it.slager.exercises.invoicing.model.ReceiptItem;
import it.slager.exercises.invoicing.printer.InvoicePrinter;

public class InvoicePrinterFailureTest {

	/**
	 * The instance is common across cases, to simulate its reuse
	 */
	private static InvoicePrinter printer;

	@BeforeClass
	public static void setupPrinter() {
		printer = new InvoicePrinter();
	}

	@Test(expected = IllegalStateException.class)
	public void testCloseTooEarly() throws IOException {

		printer.init();

		printer.closeInvoice();
	}

	@Test(expected = IllegalStateException.class)
	public void testPrintAfterClose() throws IOException {
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

		printer.closeInvoice();

		// Composite description
		ReceiptItem item2 = new ReceiptItem();
		item2.setAmount(1);
		item2.setDescription("bottle of perfume");
		item2.setImported(true);
		item2.setNetUnitPrice(new BigDecimal("47.50"));
		item2.setTaxUnitPrice(new BigDecimal("7.15"));

		printer.printItem(item2);
	}
}
