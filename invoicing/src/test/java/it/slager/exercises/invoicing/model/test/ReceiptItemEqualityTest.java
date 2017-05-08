package it.slager.exercises.invoicing.model.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Test;

import it.slager.exercises.invoicing.model.ReceiptItem;

public class ReceiptItemEqualityTest {

	@Test
	public void testEqualItems() {
		// First item
		ReceiptItem item1 = new ReceiptItem();
		item1.setAmount(1);
		item1.setDescription("book");
		item1.setImported(false);
		item1.setNetUnitPrice(new BigDecimal("12.49"));
		item1.setTaxUnitPrice(BigDecimal.ZERO);

		// Second item
		ReceiptItem item2 = new ReceiptItem();
		item2.setAmount(1);
		item2.setDescription("book");
		item2.setImported(false);
		item2.setNetUnitPrice(new BigDecimal("12.49"));
		item2.setTaxUnitPrice(BigDecimal.ZERO);

		assertTrue("Items are equal", item1.equals(item2));
		assertEquals("Items have the same hash", item1.hashCode(), item2.hashCode());
	}

	@Test
	public void testNotEqualItems() {
		// First item
		ReceiptItem item1 = new ReceiptItem();
		item1.setAmount(1);
		item1.setDescription("book");
		item1.setImported(false);
		item1.setNetUnitPrice(new BigDecimal("5.00"));
		item1.setTaxUnitPrice(BigDecimal.ZERO);

		// Second item
		ReceiptItem item2 = new ReceiptItem();
		item2.setAmount(1);
		item2.setDescription("book");
		item2.setImported(true);
		item2.setNetUnitPrice(new BigDecimal("5.00"));
		item2.setTaxUnitPrice(new BigDecimal("0.05"));

		assertFalse("Items are not equal", item1.equals(item2));
		assertNotEquals("Items have not the same hash", item1.hashCode(), item2.hashCode());
	}

}
