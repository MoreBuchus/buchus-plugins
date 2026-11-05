package com.tzhaarhptracker.info;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DamageHandlerTest
{
	@Test
	public void hpXpInvertsToIntegerDamageWithoutOverestimating()
	{
		// true damage d gives 4d/3 xp; displayed drop is within 1 of that
		for (int d = 1; d <= 60; d++)
		{
			double exact = 4.0D * d / 3.0D;
			for (int shown : new int[]{(int) Math.floor(exact), (int) Math.ceil(exact)})
			{
				int est = DamageHandler.hpXpToDamage(shown, 1.0D);
				assertTrue("d=" + d + " shown=" + shown + " est=" + est, est <= d);
			}
			assertEquals(d, DamageHandler.hpXpToDamage(Math.floor(exact), 1.0D), 1);
		}
	}

	@Test
	public void zeroXpIsZeroDamage()
	{
		assertEquals(0, DamageHandler.hpXpToDamage(0, 1.0D));
	}
}
