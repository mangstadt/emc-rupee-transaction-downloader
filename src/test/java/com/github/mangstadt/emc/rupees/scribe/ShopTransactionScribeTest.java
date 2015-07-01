package com.github.mangstadt.emc.rupees.scribe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.github.mangstadt.emc.rupees.dto.ShopTransaction.Builder;

/**
 * @author Michael Angstadt
 */
public class ShopTransactionScribeTest {
	private final ShopTransactionScribe scribe = new ShopTransactionScribe();

	@Test
	public void parse() {
		Builder builder = scribe.parse("Player shop sold 50 Diamond to Notch");
		assertEquals("Diamond", builder.item());
		assertEquals("Notch", builder.shopCustomer());
		assertEquals(-50, builder.quantity());
		assertNull(builder.shopOwner());

		builder = scribe.parse("Your player shop bought 50 Diamond from Notch");
		assertEquals("Diamond", builder.item());
		assertEquals("Notch", builder.shopCustomer());
		assertEquals(50, builder.quantity());
		assertNull(builder.shopOwner());

		builder = scribe.parse("Sold to player shop 50 Diamond to Notch");
		assertEquals("Diamond", builder.item());
		assertNull(builder.shopCustomer());
		assertEquals(-50, builder.quantity());
		assertEquals("Notch", builder.shopOwner());

		builder = scribe.parse("Player shop purchased 50 Diamond from Notch");
		assertEquals("Diamond", builder.item());
		assertNull(builder.shopCustomer());
		assertEquals(50, builder.quantity());
		assertEquals("Notch", builder.shopOwner());
	}

	@Test
	public void parse_quantity_has_comma() {
		Builder builder = scribe.parse("Player shop sold 1,000 Diamond to Notch");
		assertEquals("Diamond", builder.item());
		assertEquals("Notch", builder.shopCustomer());
		assertEquals(-1000, builder.quantity());
		assertNull(builder.shopOwner());

		builder = scribe.parse("Your player shop bought 1,000 Diamond from Notch");
		assertEquals("Diamond", builder.item());
		assertEquals("Notch", builder.shopCustomer());
		assertEquals(1000, builder.quantity());
		assertNull(builder.shopOwner());

		builder = scribe.parse("Sold to player shop 1,000 Diamond to Notch");
		assertEquals("Diamond", builder.item());
		assertNull(builder.shopCustomer());
		assertEquals(-1000, builder.quantity());
		assertEquals("Notch", builder.shopOwner());

		builder = scribe.parse("Player shop purchased 1,000 Diamond from Notch");
		assertEquals("Diamond", builder.item());
		assertNull(builder.shopCustomer());
		assertEquals(1000, builder.quantity());
		assertEquals("Notch", builder.shopOwner());
	}

	@Test
	public void parse_item_has_spaces() {
		Builder builder = scribe.parse("Player shop sold 50 Diamond Barding to Notch");
		assertEquals("Diamond Barding", builder.item());
		assertEquals("Notch", builder.shopCustomer());
		assertEquals(-50, builder.quantity());
		assertNull(builder.shopOwner());

		builder = scribe.parse("Your player shop bought 50 Diamond Barding from Notch");
		assertEquals("Diamond Barding", builder.item());
		assertEquals("Notch", builder.shopCustomer());
		assertEquals(50, builder.quantity());
		assertNull(builder.shopOwner());

		builder = scribe.parse("Sold to player shop 50 Diamond Barding to Notch");
		assertEquals("Diamond Barding", builder.item());
		assertNull(builder.shopCustomer());
		assertEquals(-50, builder.quantity());
		assertEquals("Notch", builder.shopOwner());

		builder = scribe.parse("Player shop purchased 50 Diamond Barding from Notch");
		assertEquals("Diamond Barding", builder.item());
		assertNull(builder.shopCustomer());
		assertEquals(50, builder.quantity());
		assertEquals("Notch", builder.shopOwner());
	}

	@Test
	public void parse_not_a_shop_transaction() {
		Builder builder = scribe.parse("Foo Bar");
		assertNull(builder);
	}
}
