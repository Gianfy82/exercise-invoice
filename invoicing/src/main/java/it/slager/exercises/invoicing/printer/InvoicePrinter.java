package it.slager.exercises.invoicing.printer;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;

import it.slager.exercises.invoicing.model.ReceiptItem;

/**
 * An instance of InvoicePrinter is reusable, after calling the
 * {@link#init()} method.
 * 
 * @author g.slager
 *
 */
public class InvoicePrinter {
	private Writer output;

	private PrinterState state;

	private BigDecimal taxTotal;

	private BigDecimal netTotal;

	/**
	 * Reset the InvoicePrinter to a steady state, able to print a new invoice.
	 */
	public void init() {
		state = PrinterState.INIT;
		taxTotal = BigDecimal.ZERO;
		netTotal = BigDecimal.ZERO;
	}

	/**
	 * Prints an item to the output stream, using the sintax<br/>
	 * &lt;amount&gt; [imported] &lt;box of chocolates&gt;: &lt;gross price&gt;
	 * 
	 * @param item
	 * @throws IOException
	 */
	public void printItem(ReceiptItem item) throws IOException {
		// Update state machine
		switch (state) {
		case INIT:
		case PRINTING_ITEMS:
			state = PrinterState.PRINTING_ITEMS;
			break;
		default:
			throw new IllegalStateException("Cannot print more items when in state " + state);
		}

		// Update totals with item
		BigDecimal taxItem = item.getTaxUnitPrice().multiply(new BigDecimal(item.getAmount()));
		taxTotal = taxTotal.add(taxItem);
		BigDecimal netItem = item.getNetUnitPrice().multiply(new BigDecimal(item.getAmount()));
		netTotal = netTotal.add(netItem);

		// Print the item
		output.write(String.valueOf(item.getAmount()));

		if (item.isImported()) {
			output.write(' ');
			output.write("imported");
		}

		output.write(' ');
		output.write(item.getDescription());

		output.write(": ");
		output.write(netItem.add(taxItem).toString());

		output.write(System.lineSeparator());
	}

	public void closeInvoice() throws IOException {
		// Update state machine
		switch (state) {
		case PRINTING_ITEMS:
			state = PrinterState.CLOSED_INVOICE;
			break;
		default:
			throw new IllegalStateException("Cannot close an invoice when in state " + state);
		}

		output.write("Sales Taxes: " + taxTotal);
		output.write(System.lineSeparator());
		output.write("Total: " + netTotal.add(taxTotal));

	}

	/**
	 * The stream to print the invoice to. When set, the stream should be ready
	 * get used.
	 * 
	 * @param out
	 */
	public void setWriter(Writer out) {
		this.output = out;
	}

}