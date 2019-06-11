package blusunrize.aquatweaks.core;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("1.6.4")
@IFMLLoadingPlugin.Name(AquaTweaksCoreLoader.NAME)
@IFMLLoadingPlugin.SortingIndex(2002)
public class AquaTweaksCoreLoader implements IFMLLoadingPlugin
{
	public static final String NAME = "AquaTweaks Core";
	public static boolean optifine = false;

	@Override
	public String[] getASMTransformerClass()
	{
		return new String[]{AquaTweaksCoreTransformer.class.getName()};
	}
	@Override
	public String getModContainerClass()
	{
		return null;
	}
	@Override
	public String getSetupClass()
	{
		return null;
	}
	@Override
	public void injectData(Map<String, Object> data)
	{
		try {
			Class.forName("optifine.OptiFineClassTransformer");
			optifine = true;
		} catch (ClassNotFoundException ignored) {
		}
	}
}