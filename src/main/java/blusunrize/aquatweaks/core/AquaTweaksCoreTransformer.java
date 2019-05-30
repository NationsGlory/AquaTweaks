package blusunrize.aquatweaks.core;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.MinecraftForge;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import blusunrize.aquatweaks.ATLog;
import blusunrize.aquatweaks.FluidUtils;
import blusunrize.aquatweaks.RenderWorldEventMid;
import cpw.mods.fml.common.ObfuscationReflectionHelper;

import static blusunrize.aquatweaks.FluidUtils.blockIsOpaque;

public class AquaTweaksCoreTransformer implements IClassTransformer
{
	@Override
	public byte[] transform(String className, String newClassName, byte[] origCode)
	{
		//patch shouldSideBeRendered in BlockLiquid
		if(className.equals("net.minecraft.block.BlockFluid")||className.equals("apc"))
		{
			ATLog.info("Patching 'shouldSideBeRendered'");
			ClassReader rd = new ClassReader(origCode);
			ClassWriter wr = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			ClassVisitor patcher = new Visitor_ShouldSide(wr);
			rd.accept(patcher, ClassReader.EXPAND_FRAMES);
			return wr.toByteArray();
		}

		//add custom render hook
		if(className.equals("net.minecraft.client.renderer.WorldRenderer")||className.equals("bfa"))
		{
			ATLog.info("Adding custom world render hook");
			ClassReader rd = new ClassReader(origCode);
			ClassWriter wr = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			ClassVisitor patcher = new Visitor_RenderEvent(wr);
			rd.accept(patcher, ClassReader.EXPAND_FRAMES);
			return wr.toByteArray();
		}
		return origCode;
	}

	private static class InsertInitCodeBeforeReturnMethodVisitor extends MethodVisitor
	{
		public InsertInitCodeBeforeReturnMethodVisitor(MethodVisitor mv)
		{
			super(Opcodes.ASM4, mv);
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc) {

			if(owner.equals("net/minecraft/client/renderer/Tessellator") && name.equals("setTranslation") && desc.equals("(DDD)V"))
			{
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitVarInsn(Opcodes.ALOAD, 10);
				mv.visitVarInsn(Opcodes.ILOAD, 11);
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/renderer/WorldRenderer", "posX", "I");
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/renderer/WorldRenderer", "posY", "I");
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/renderer/WorldRenderer", "posZ", "I");
				mv.visitMethodInsn(Opcodes.INVOKESTATIC,
						"blusunrize/aquatweaks/core/AquaTweaksCoreTransformer",
						"fireMidRenderEvent",
						"(Lnet/minecraft/client/renderer/WorldRenderer;Lnet/minecraft/client/renderer/RenderBlocks;IIII)V");
			}
			super.visitMethodInsn(opcode, owner, name, desc);
		}
	}

	public static class Visitor_RenderEvent extends ClassVisitor
	{
		public Visitor_RenderEvent(ClassWriter writer)
		{
			super(Opcodes.ASM4, writer);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
		{
			final String methodToPatch = "updateRenderer";
			final String methodToPatch_srg = "func_78907_a";
			final String methodToPatch_obf = "a";
			final String qdesc = "()V";
			if((name.equals(methodToPatch)||name.equals(methodToPatch_srg)||name.equals(methodToPatch_obf))
					&&(desc.equals(qdesc)))
			{
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				return new InsertInitCodeBeforeReturnMethodVisitor(mv);

			}
			return super.visitMethod(access, name, desc, signature, exceptions);
		}
	}
	public static void fireMidRenderEvent(WorldRenderer wr, RenderBlocks rb, int pass, int posX, int posY, int posZ)
	{
		Tessellator.instance.setTranslation((double)(-posX), (double)(-posY), (double)(-posZ));
		if(rb!=null && rb.blockAccess instanceof ChunkCache)
			MinecraftForge.EVENT_BUS.post(new RenderWorldEventMid(wr, (ChunkCache)rb.blockAccess, rb, pass));
	}


	public static class Visitor_ShouldSide extends ClassVisitor
	{
		public Visitor_ShouldSide(ClassWriter writer)
		{
			super(Opcodes.ASM4, writer);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
		{
			final String methodToPatch = "shouldSideBeRendered";
			final String methodToPatch_srg = "func_71877_c";
			final String methodToPatch_obf = "a";
			final String qdesc = "(Lnet/minecraft/world/IBlockAccess;IIII)Z";
			final String qdesc_obf = "(Lacf;IIII)Z";
			final String qdescInv = "(Lnet/minecraft/block/Block;Lnet/minecraft/world/IBlockAccess;IIII)Z";
			final String qdescInv_obf = "(Laqz;Lacf;IIII)Z";

			if((name.equals(methodToPatch)||name.equals(methodToPatch_srg)||name.equals(methodToPatch_obf))
					&&(desc.equals(qdesc)||desc.equals(qdesc_obf)))
			{
				final String invokeDesc = desc.equals(qdesc)?qdescInv:qdescInv_obf;

				return new MethodVisitor(Opcodes.ASM4, super.visitMethod(access, name, desc, signature, exceptions))
				{
					@Override
					public void visitCode()
					{
						mv.visitCode();
						mv.visitVarInsn(Opcodes.ALOAD, 0);

						mv.visitVarInsn(Opcodes.ALOAD, 1);
						mv.visitVarInsn(Opcodes.ILOAD, 2);
						mv.visitVarInsn(Opcodes.ILOAD, 3);
						mv.visitVarInsn(Opcodes.ILOAD, 4);
						mv.visitVarInsn(Opcodes.ILOAD, 5);
						mv.visitMethodInsn(Opcodes.INVOKESTATIC, "blusunrize/aquatweaks/core/AquaTweaksCoreTransformer", "liquid_shouldSideBeRendered",
								invokeDesc);
						mv.visitInsn(Opcodes.IRETURN);
						mv.visitMaxs(5, 1);
						mv.visitEnd();
					}
				};
			}
			return super.visitMethod(access, name, desc, signature, exceptions);
		}
	}

	public static boolean liquid_shouldSideBeRendered(Block block, IBlockAccess world, int x, int y, int z, int side)
	{
		if(side>=0 && side<6)
			if(FluidUtils.canFluidConnectToBlock(world, x, y, z, side, block.blockMaterial))
//			if(world.getBlock(x, y, z) instanceof IAquaConnectable && ((IAquaConnectable)world.getBlock(x, y, z)).canConnectTo(world, x, y, z, ForgeDirection.OPPOSITES[side]) && FluidUtils.isBlockSubmerged(world, x, y, z, Material.water))
				return false;
		Material material = world.getBlockMaterial(x, y, z);
		return material != block.blockMaterial && (side == 1 || side == 0 && block.getBlockBoundsMinY() > 0 || (side == 2 && block.getBlockBoundsMinZ() > 0 || (side == 3 && block.getBlockBoundsMaxZ() < 1 || (side == 4 && block.getBlockBoundsMinX() > 0 || (side == 5 && block.getBlockBoundsMaxX() < 1 || !blockIsOpaque(world, x,y,z))))));
	}
}