package blusunrize.aquatweaks;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.aquatweaks.proxy.CommonProxy;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = AquaTweaks.MODID, name = AquaTweaks.MODNAME, version = AquaTweaks.VERSION)
public class AquaTweaks
{
	public static final String MODID = "AquaTweaks";
	public static final String MODNAME = "AquaTweaks";
	public static final String VERSION = "1.0";

	@Instance(MODID)
	public static AquaTweaks instance = new AquaTweaks();	

	@SidedProxy(clientSide="blusunrize.aquatweaks.proxy.ClientProxy", serverSide="blusunrize.aquatweaks.proxy.CommonProxy")
	public static CommonProxy proxy;


	public static boolean tweakGlass = true;
	public static String[] manualTweaks = new String[]{"cobblestone_wall"};

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		tweakGlass = config.get("tweaks", "tweakGlass", "Set to false to re-enable water rendering its sides towards glass").getBoolean(tweakGlass);
		String[] mT = config.get("tweaks", "manualTweaks", "This string array can be used to add blocks manually to AquaTweaks. Note that these need to be the proper registry names. The cobblestone wall added here can also be removed. They are optional but might be useful for people who use the walls as pillars or something.").getStringList();

		if(mT != null)
			manualTweaks = mT;

		config.save();
	}
	@Mod.EventHandler
	public void init(FMLInitializationEvent event)
	{
		proxy.registerHandlers();
	}
}
