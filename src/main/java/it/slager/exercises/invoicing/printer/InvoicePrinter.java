package it.slager.exercises.invoicing.printer;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.Formatter;

import it.slager.exercises.invoicing.model.ReceiptItem;

/**
 * An instance of InvoicePrinter is reusable, after calling the
 * {@link#init()} method.
 * 
 * @author g.slager
 *
 */
public class InvoicePrinter {
	/**
	 * The Writer instance the InvoicePrinter uses to print the invoice
	 */
	private Writer output;

	/**
	 * Encapsulate the way to format currencies to the output.
	 */
	private Formatter currencyPrinter;

	/**
	 * An InvoicePrinter implements a state machine, to ensure the caller not to
	 * bring the printer to an inconsistent state.
	 */
	private PrinterState state;

	/**
	 * Accumulates the total net price for all the goods in the invoice
	 */
	private BigDecimal netTotal;

	/**
	 * Accumulates the total tax amount for all the goods in the invoice
	 */
	private BigDecimal taxTotal;

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
		writeCurrency(netItem.add(taxItem));

		output.write(System.lineSeparator());
	}

	/**
	 * Mark the invoice as closed and print the receipt summary.
	 * 
	 * @throws IOException
	 */
	public void closeInvoice() throws IOException {
		// Update state machine
		switch (state) {
		case PRINTING_ITEMS:
			state = PrinterState.CLOSED_INVOICE;
			break;
		case CLOSED_INVOICE:
			// Do nothing and return
			return;
		default:
			throw new IllegalStateException("Cannot close an invoice when in state " + state);
		}

		output.write("Sales Taxes: ");
		writeCurrency(taxTotal);
		output.write(System.lineSeparator());
		output.write("Total: ");
		writeCurrency(netTotal.add(taxTotal));

	}

	/**
	 * Print the given value with 2 decimal places.
	 * 
	 * @param value
	 */
	private void writeCurrency(BigDecimal value) {
		currencyPrinter.format("%.2f", value);
	}

	/**
	 * The stream to print the invoice to. When set, the stream should be ready
	 * get used.
	 * 
	 * @param out
	 */
	public void setWriter(Writer out) {
		this.output = out;
		this.currencyPrinter = new Formatter(output);
	}

}
