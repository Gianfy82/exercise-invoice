package it.slager.exercises.invoicing.processor;

import java.math.BigDecimal;

import it.slager.exercises.invoicing.model.ReceiptItem;

/**
 * Basic sales tax is applicable at a rate of &lt;taxRatio&gt; on all goods,
 * except books, food, and medical products that are exempt. Import duty is an
 * additional sales tax applicable on all imported goods at a rate of
 * &lt;taxRatioImportedGoods&gt;, with no exemptions.<br/>
 * 
 * The rounding rules for sales tax are that for a tax rate of n%, a shelf price
 * of p contains (np/100 rounded up to the nearest 0.05) amount of sales tax.
 * 
 * @see #ReceiptItemTaxEvaluator(BigDecimal, BigDecimal)
 * 
 * @author g.slager
 *
 */
public class ReceiptItemTaxEvaluator {
	private final BigDecimal taxRatio;
	private final BigDecimal taxRatioImportedGoods;
	private final BigDecimal taxPrecision;

	/**
	 * @param taxRatio
	 *            the ratio to calculate taxation as
	 *            <code>tax = net_price * ratio</code>
	 * @param taxRatioImportedGoods
	 *            the ratio to calculate taxation added to imported goods as
	 *            <code>import_tax = net_price * ratio</code>
	 */
	public ReceiptItemTaxEvaluator(String taxRatio, String taxRatioImportedGoods, String taxPrecision) {
		this.taxRatio = new BigDecimal(taxRatio);
		this.taxRatioImportedGoods = new BigDecimal(taxRatioImportedGoods);
		this.taxPrecision = new BigDecimal(taxPrecision);
	}

	/**
	 * Evaluate taxes for the given item.<br/>
	 * Taxes apply to the unit price, without considering the good amount.<br/>
	 * The method updates the item adding taxes.
	 * 
	 * @param item
	 */
	public void evaluateTaxation(ReceiptItem item) {
		BigDecimal tax;

		// Evaluate tax for goods
		if (isBasicGood(item)) {
			tax = BigDecimal.ZERO;
		} else {
			tax = item.getNetUnitPrice().multiply(taxRatio);
		}

		// Add taxation for imported items
		if (item.isImported()) {
			BigDecimal importedTax = item.getNetUnitPrice().multiply(taxRatioImportedGoods);
			tax = tax.add(importedTax);
		}

		// Set taxation with rounding
		item.setTaxUnitPrice(roundTaxation(tax));
	}

	/**
	 * @param tax
	 * @return the given tax, rounded up to the first multiple of taxPrecision
	 */
	private BigDecimal roundTaxation(BigDecimal tax) {
		BigDecimal remainder = tax.remainder(taxPrecision);
		if (remainder.compareTo(BigDecimal.ZERO) == 0) {
			return tax;
		} else {
			return tax.add(taxPrecision).subtract(remainder);
		}
	}

	/**
	 * The methods evaluates against item description field, searching a keyword
	 * among those describing basic goods.
	 * 
	 * @param inputItem
	 * @return true if the item is a basic good
	 */
	private boolean isBasicGood(ReceiptItem inputItem) {
		String[] basicGoods = new String[] { "book", "chocolate", "pills" };
		// Very simple and ugly for the purpose
		for (String basicGood : basicGoods) {
			if (inputItem.getDescription().contains(basicGood)) {
				return true;
			}
		}

		return false;
	}

}
