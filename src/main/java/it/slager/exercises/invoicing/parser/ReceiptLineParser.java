package it.slager.exercises.invoicing.parser;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import it.slager.exercises.invoicing.model.ReceiptItem;

public class ReceiptLineParser {

	private final String importedKeyword;

	private String descriptionTerminator;

	public ReceiptLineParser(String importedKeyword, String descriptionTerminator) {
		this.importedKeyword = importedKeyword;
		this.descriptionTerminator = descriptionTerminator;
		// do nothing for now
	}

	/**
	 * @param receiptElement
	 *            a non empty-line
	 * @return
	 * @throws ReceiptParseException
	 */
	public ReceiptItem parse(String receiptElement) throws ReceiptParseException {
		// Preliminary check
		if (receiptElement == null || receiptElement.isEmpty()) {
			throw new IllegalArgumentException("Can't parse an empty or null line");
		}

		ReceiptItem item = new ReceiptItem();
		ReceiptLineParserState state = ReceiptLineParserState.INIT;
		String token = null;
		try {

			StringTokenizer tokenizer = new StringTokenizer(receiptElement);

			// Parsing amount
			token = tokenizer.nextToken();
			item.setAmount(Integer.parseInt(token));
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
					throw new IllegalStateException("Unexpected token");
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
		} catch (NumberFormatException | NoSuchElementException e) {
			throw new ReceiptParseException("Exception occurred on token:" + token + " and parser state:" + state, e);
		}

		return item;
	}
}
