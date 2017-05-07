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

	/**
	 * @param taxRatio
	 *            the ratio to calculate taxation as
	 *            <code>tax = net_price * ratio</code>
	 * @param taxRatioImportedGoods
	 *            the ratio to calculate taxation added to imported goods as
	 *            <code>import_tax = net_price * ratio</code>
	 */
	public ReceiptItemTaxEvaluator(BigDecimal taxRatio, BigDecimal taxRatioImportedGoods) {
		this.taxRatio = taxRatio;
		this.taxRatioImportedGoods = taxRatioImportedGoods;
	}

	public void evaluateTaxation(ReceiptItem inputItem) {
		BigDecimal tax;
		if (isBasicGood(inputItem)) {
			tax = BigDecimal.ZERO;
		} else {
			tax = inputItem.getNetUnitPrice().multiply(taxRatio);
		}

		if (inputItem.isImported()) {
			BigDecimal importedTax = inputItem.getNetUnitPrice().multiply(taxRatioImportedGoods);
			tax = tax.add(importedTax);
		}

		inputItem.setTaxUnitPrice(roundTaxation(tax));
	}

	private BigDecimal roundTaxation(BigDecimal tax) {
		BigDecimal remainder = tax.remainder(new BigDecimal("0.05"));
		if (remainder.compareTo(BigDecimal.ZERO) == 0) {
			return tax;
		} else {
			return tax.add(new BigDecimal("0.05")).subtract(remainder);
		}
	}

	/**
	 * The methods evaluates against item description field, searching a keyword
	 * among those describing basic goods.
	 * 
	 * @param inputItem
	 * @return true if the item is a basoc good
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
