package it.slager.exercises.invoicing.parser;

public class ReceiptParseException extends Exception {

	private static final long serialVersionUID = 4190040664834410797L;

	/**
	 * Describes an illegal event occurred parsing an item.
	 * 
	 * @param description
	 * @param cause
	 */
	public ReceiptParseException(String description, RuntimeException cause) {
		super(description, cause);
	}

}
