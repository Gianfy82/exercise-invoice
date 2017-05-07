package it.slager.exercises.invoicing.parser;

import java.math.BigDecimal;
import java.util.StringTokenizer;

import it.slager.exercises.invoicing.model.ReceiptItem;

public class ReceiptLineParser {

	private final String importedKeyword;

	/**
	 * Track parser state. Handful to inspect parse errors.
	 */
	private ReceiptLineParserState state;

	private String descriptionTerminator;

	public ReceiptLineParser(String importedKeyword, String descriptionTerminator) {
		this.importedKeyword = importedKeyword;
		this.descriptionTerminator = descriptionTerminator;
		// do nothing for now
	}

	/**
	 * @param receiptLine
	 *            a non empty-line
	 * @return
	 */
	public ReceiptItem parse(String receiptLine) {
		// Preliminary check
		if (receiptLine == null || receiptLine.isEmpty()) {
			throw new IllegalArgumentException("Can't parse an empty or null line");
		}

		state = ReceiptLineParserState.INIT;

		ReceiptItem item = new ReceiptItem();

		StringTokenizer tokenizer = new StringTokenizer(receiptLine);

		// Parsing amount
		String token = tokenizer.nextToken();
		item.setAmount(Integer.parseUnsignedInt(token));
		state = ReceiptLineParserState.AMOUNT_READ;

		token = tokenizer.nextToken();
		StringBuilder descriptionAppender = new StringBuilder();

		// Imported or first description element
		if (importedKeyword.equals(token)) {
			item.setImported(true);

			// Consuming first description token
			state = ReceiptLineParserState.CONSUMING_DESCRIPTION;
			token = tokenizer.nextToken();
			if (descriptionTerminator.equals(token)) {
				// FIXME throw a proper exception
			} else {
				descriptionAppender.append(token);
			}
		} else {
			descriptionAppender.append(token);
		}

		// Consume the (remaining) description, until 'at' token
		while (!descriptionTerminator.equals(token = tokenizer.nextToken())) {
			descriptionAppender.append(" ");
			descriptionAppender.append(token);
		}
		item.setDescription(descriptionAppender.toString());
		state = ReceiptLineParserState.CONSUMING_PRICE;

		// Consume net price
		item.setNetUnitPrice(new BigDecimal(tokenizer.nextToken()));
		state = ReceiptLineParserState.END;

		return item;
	}
}
