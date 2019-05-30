package blusunrize.aquatweaks;

import java.util.logging.Logger;

public class ATLog
{
	public static final Logger logger = Logger.getLogger("AquaTweaks");

	public static void info(String s)
	{
		logger.info(s);
	}
}
