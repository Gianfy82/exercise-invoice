package it.slager.exercises.invoicing.model;

import java.math.BigDecimal;

/**
 * A POJO representing a receipt item.
 * 
 * @author g.slager
 *
 */
public class ReceiptItem {
	private int amount;
	private String description;
	private BigDecimal netUnitPrice;

	/**
	 * true if the item is imported
	 */
	private boolean imported;

	private BigDecimal taxUnitPrice;

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getNetUnitPrice() {
		return netUnitPrice;
	}

	public void setNetUnitPrice(BigDecimal netUnitPrice) {
		this.netUnitPrice = netUnitPrice;
	}

	public boolean isImported() {
		return imported;
	}

	public void setImported(boolean imported) {
		this.imported = imported;
	}

	public BigDecimal getTaxUnitPrice() {
		return taxUnitPrice;
	}

	public void setTaxUnitPrice(BigDecimal taxUnitPrice) {
		this.taxUnitPrice = taxUnitPrice;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + amount;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + (imported ? 1231 : 1237);
		result = prime * result + ((netUnitPrice == null) ? 0 : netUnitPrice.hashCode());
		result = prime * result + ((taxUnitPrice == null) ? 0 : taxUnitPrice.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReceiptItem other = (ReceiptItem) obj;
		if (amount != other.amount)
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (imported != other.imported)
			return false;
		if (netUnitPrice == null) {
			if (other.netUnitPrice != null)
				return false;
		} else if (!netUnitPrice.equals(other.netUnitPrice))
			return false;
		if (taxUnitPrice == null) {
			if (other.taxUnitPrice != null)
				return false;
		} else if (!taxUnitPrice.equals(other.taxUnitPrice))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ReceiptItem [amount=" + amount + ", description=" + description + ", netUnitPrice=" + netUnitPrice
				+ ", imported=" + imported + ", taxUnitPrice=" + taxUnitPrice + "]";
	}

}
