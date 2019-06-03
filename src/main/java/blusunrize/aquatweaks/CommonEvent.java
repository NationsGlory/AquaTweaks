package blusunrize.aquatweaks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class CommonEvent {

    public static boolean isInsideOfMaterial(Entity entity, Material par1Material) {
        double d0 = entity.posY + (double) entity.getEyeHeight();
        int i = MathHelper.floor_double(entity.posX);
        int j = MathHelper.floor_float((float) MathHelper.floor_double(d0));
        int k = MathHelper.floor_double(entity.posZ);
        int l = entity.worldObj.getBlockId(i, j, k);

        if (par1Material == Material.water && isInFakeWater(entity)) {
            return true;
        }

        Block block = Block.blocksList[l];
        if (block != null && block.blockMaterial == par1Material) {
            double filled = block.getFilledPercentage(entity.worldObj, i, j, k);
            if (filled < 0) {
                filled *= -1;
                //filled -= 0.11111111F; //Why this is needed.. not sure...
                return d0 > (j + (1 - filled));
            } else {
                return d0 < (j + filled);
            }
        } else {
            return false;
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

}
