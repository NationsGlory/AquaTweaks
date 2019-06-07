package blusunrize.aquatweaks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import org.lwjgl.opengl.GL11;

import static blusunrize.aquatweaks.FluidUtils.blockIsOpaque;

public class AquaEventHandler
{
	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void onWorldRenderMid(RenderWorldEventMid event)
	{
		handleWaterRendering(event);
	}

	@SideOnly(Side.CLIENT)
	public static void handleWaterRendering(RenderWorldEventMid event)
	{
		if(event.pass==1)
		{
			for(int yy=0; yy<event.chunkCache.getHeight(); yy++)
				for(int xx=0; xx<16; xx++)
					for(int zz=0; zz<16; zz++)
					{
						int x = event.renderer.posX+xx;
						int y = event.renderer.posY+yy;
						int z = event.renderer.posZ+zz;
						if(FluidUtils.shouldRenderAquaConnectable(event.chunkCache, x,y,z))
							FluidUtils.tessellateFluidBlock(event.renderBlocks.blockAccess, x, y, z, event.renderBlocks, Tessellator.instance);
						if(AquaTweaks.tweakGlass && event.chunkCache.getBlockMaterial(x,y,z)==Material.glass)
							FluidUtils.renderTowardsGlass(event.chunkCache, x,y,z, event.renderBlocks);
					}
		}
	}

	private static final ResourceLocation WATER_TEXTURE = new ResourceLocation("textures/misc/underwater.png");
	@SideOnly(Side.CLIENT)
	@ForgeSubscribe
	public void renderGameOverlay(RenderGameOverlayEvent.Pre event)
	{
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;
		if(player!=null && !player.isInsideOfMaterial(Material.water) && isInFakeWater(player))
		{
			//if(!net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new RenderBlockOverlayEvent(player, event.partialTicks, net.minecraftforge.client.event.RenderBlockOverlayEvent.OverlayType.WATER, Block.waterStill, MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ))))
			//{
				GL11.glDisable(GL11.GL_ALPHA_TEST);
				mc.getTextureManager().bindTexture(WATER_TEXTURE);
				Tessellator tessellator = Tessellator.instance;
				float f1 = mc.thePlayer.getBrightness(event.partialTicks)*.75f;
				GL11.glColor4f(f1, f1, f1, .5F);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(770, 771);
				GL11.glPushMatrix();
				float f2 = 4.0F;
				float f3 = -1.0F;
				float f4 = 1.0F;
				float f5 = -1.0F;
				float f6 = 1.0F;
				float f7 = -0.5F;
				float f8 = -mc.thePlayer.rotationYaw / 64.0F;
				float f9 = mc.thePlayer.rotationPitch / 64.0F;
				tessellator.startDrawingQuads();
				tessellator.addVertexWithUV((double)f3, (double)f5, (double)f7, (double)(f2 + f8), (double)(f2 + f9));
				tessellator.addVertexWithUV((double)f4, (double)f5, (double)f7, (double)(0.0F + f8), (double)(f2 + f9));
				tessellator.addVertexWithUV((double)f4, (double)f6, (double)f7, (double)(0.0F + f8), (double)(0.0F + f9));
				tessellator.addVertexWithUV((double)f3, (double)f6, (double)f7, (double)(f2 + f8), (double)(0.0F + f9));
				tessellator.draw();
				GL11.glPopMatrix();
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				GL11.glDisable(GL11.GL_BLEND);
			//}
		}
	}

	public static boolean isInFakeWater(Entity entity)
	{
		for (int i = 0; i < 8; ++i)
		{
			float f = ((float)((i >> 0) % 2) - 0.5F) * entity.width * 0.8F;
			float f1 = ((float)((i >> 1) % 2) - 0.5F) * 0.1F;
			float f2 = ((float)((i >> 2) % 2) - 0.5F) * entity.width * 0.8F;
			int x = MathHelper.floor_double(entity.posX + f);
			int y = MathHelper.floor_double(entity.posY + entity.getEyeHeight()+f1);
			int z = MathHelper.floor_double(entity.posZ + f2);

			if(FluidUtils.shouldRenderAquaConnectable(entity.worldObj, x,y,z) && FluidUtils.getFakeFillMaterial(entity.worldObj, x, y, z)==Material.water)
				return true;
		}
		return false;
	}

	public static boolean liquid_shouldSideBeRendered(Block block, IBlockAccess world, int x, int y, int z, int side) {
		if (side >= 0 && side < 6)
			if (FluidUtils.canFluidConnectToBlock(world, x, y, z, side, block.blockMaterial))
				return false;
		Material material = world.getBlockMaterial(x, y, z);
		return material != block.blockMaterial && (side == 1 || side == 0 && block.getBlockBoundsMinY() > 0 || (side == 2 && block.getBlockBoundsMinZ() > 0 || (side == 3 && block.getBlockBoundsMaxZ() < 1 || (side == 4 && block.getBlockBoundsMinX() > 0 || (side == 5 && block.getBlockBoundsMaxX() < 1 || !blockIsOpaque(world, x, y, z))))));
	}

	public static void fireMidRenderEvent(WorldRenderer wr, RenderBlocks rb, int pass, int posX, int posY, int posZ) {
		Tessellator.instance.setTranslation((double) (-posX), (double) (-posY), (double) (-posZ));
		if (rb != null && rb.blockAccess instanceof ChunkCache)
			MinecraftForge.EVENT_BUS.post(new RenderWorldEventMid(wr, (ChunkCache) rb.blockAccess, rb, pass));
	}
}