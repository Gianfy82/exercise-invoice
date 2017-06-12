package it.slager.exercises.invoicing.parser;

public enum ReceiptLineParserState {
	INIT,
	AMOUNT_READ,
	CONSUMING_DESCRIPTION,
	CONSUMING_PRICE,
	END;
}
