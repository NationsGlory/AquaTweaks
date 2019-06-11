package blusunrize.aquatweaks.core;

import blusunrize.aquatweaks.ATLog;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class AquaTweaksCoreTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String className, String newClassName, byte[] origCode) {
        //patch shouldSideBeRendered in BlockLiquid
        if (className.equals("net.minecraft.block.BlockFluid") || className.equals("apc")) {
            ATLog.info("Patching 'shouldSideBeRendered'");
            ClassReader rd = new ClassReader(origCode);
            ClassWriter wr = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            ClassVisitor patcher = new Visitor_ShouldSide(wr);
            rd.accept(patcher, ClassReader.EXPAND_FRAMES);
            return wr.toByteArray();
        }

        //add custom render hook
        if (className.equals("net.minecraft.client.renderer.WorldRenderer") || className.equals("bfa")) {
            ClassReader classReader = new ClassReader(origCode);
            ClassNode classNode = new ClassNode();
            classReader.accept(classNode, 0);

            final String methodToPatch = "updateRenderer";
            final String methodToPatch_srg = "func_78907_a";
            final String methodToPatch_obf = "a";
            final String qdesc = "()V";

            for (MethodNode methodNode : classNode.methods) {
                String name = methodNode.name;
                if ((name.equals(methodToPatch) || name.equals(methodToPatch_srg) || name.equals(methodToPatch_obf) && methodNode.desc.equals(qdesc)))
                {
                    InsnList insnList = methodNode.instructions;
                    for(AbstractInsnNode insnNode : insnList.toArray())
                    {
                        if(insnNode instanceof MethodInsnNode)
                        {
                            MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
                            System.out.println(methodInsnNode.owner+"/"+methodInsnNode.name+methodInsnNode.desc);
                            if (((methodInsnNode.owner.equals("net/minecraft/client/renderer/Tessellator") && methodInsnNode.name.equals("setTranslation")) || (methodInsnNode.owner.equals("net/minecraft/client/renderer/Tessellator") && methodInsnNode.name.equals("func_78373_b"))) && methodInsnNode.desc.equals("(DDD)V")) {
                                boolean dev = !methodInsnNode.name.equals("func_78373_b");

                                AbstractInsnNode iD = new MethodInsnNode(Opcodes.INVOKESTATIC,
                                        "blusunrize/aquatweaks/AquaEventHandler",
                                        "fireMidRenderEvent",
                                       "(Lnet/minecraft/client/renderer/WorldRenderer;Lnet/minecraft/client/renderer/RenderBlocks;IIII)V");

                                insnList.insert(insnNode, iD);

                                insnList.insertBefore(iD, new VarInsnNode(Opcodes.ALOAD, 0));
                                insnList.insertBefore(iD, new VarInsnNode(Opcodes.ALOAD, 10));
                                if (AquaTweaksCoreLoader.optifine)
                                    insnList.insertBefore(iD, new VarInsnNode(Opcodes.ILOAD, 13));
                                else
                                    insnList.insertBefore(iD, new VarInsnNode(Opcodes.ILOAD, 11));
                                insnList.insertBefore(iD, new VarInsnNode(Opcodes.ALOAD, 0));
                                insnList.insertBefore(iD, new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/renderer/WorldRenderer", dev ? "posX" : "field_78923_c", "I"));
                                insnList.insertBefore(iD, new VarInsnNode(Opcodes.ALOAD, 0));
                                insnList.insertBefore(iD, new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/renderer/WorldRenderer", dev ? "posY" : "field_78920_d", "I"));
                                insnList.insertBefore(iD, new VarInsnNode(Opcodes.ALOAD, 0));
                                insnList.insertBefore(iD, new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/renderer/WorldRenderer", dev ? "posZ" : "field_78921_e", "I"));
                            }
                        }
                    }
                }

            }

            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
            classNode.accept(classWriter);
            return classWriter.toByteArray();
        }

        if (className.equals("net.minecraft.entity.Entity") || className.equals("nn")) {
            ClassReader classReader = new ClassReader(origCode);
            ClassNode classNode = new ClassNode();
            classReader.accept(classNode, 0);

            for (MethodNode methodNode : classNode.methods) {
                if (methodNode.name.equals("isInsideOfMaterial") || (methodNode.name.equals("a") && methodNode.desc.equals("(Lakc;)Z"))) {
                    boolean dev = methodNode.name.equals("isInsideOfMaterial");
                    InsnList insnList = methodNode.instructions;
                    insnList.clear();
                    insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    insnList.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "blusunrize/aquatweaks/CommonEvent", "isInsideOfMaterial", dev ? "(Lnet/minecraft/entity/Entity;Lnet/minecraft/block/material/Material;)Z" : "(Lnn;Lakc;)Z"));
                    insnList.add(new InsnNode(Opcodes.IRETURN));
                }
            }

            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
            classNode.accept(classWriter);
            return classWriter.toByteArray();
        }
        return origCode;
    }

    private static class InsertInitCodeBeforeReturnMethodVisitor extends MethodVisitor {
        public InsertInitCodeBeforeReturnMethodVisitor(MethodVisitor mv) {
            super(Opcodes.ASM4, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc) {

            if (((owner.equals("net/minecraft/client/renderer/Tessellator") && name.equals("setTranslation")) || (owner.equals("bfq") && name.equals("b"))) && desc.equals("(DDD)V")) {
                boolean dev = !owner.equals("bfq");
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitVarInsn(Opcodes.ALOAD, 10);
                if (AquaTweaksCoreLoader.optifine)
                    mv.visitVarInsn(Opcodes.ILOAD, 22);
                else
                    mv.visitVarInsn(Opcodes.ILOAD, 11);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/renderer/WorldRenderer", dev ? "posX" : "field_78923_c", "I");
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/renderer/WorldRenderer", dev ? "posY" : "field_78920_d", "I");
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/renderer/WorldRenderer", dev ? "posZ" : "field_78921_e", "I");
                mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                        "blusunrize/aquatweaks/AquaEventHandler",
                        "fireMidRenderEvent",
                        dev ? "(Lnet/minecraft/client/renderer/WorldRenderer;Lnet/minecraft/client/renderer/RenderBlocks;IIII)V" : "(Lbfa;Lbfr;IIII)V");
            }
            super.visitMethodInsn(opcode, owner, name, desc);
        }
    }

    public static class Visitor_RenderEvent extends ClassVisitor {
        public Visitor_RenderEvent(ClassWriter writer) {
            super(Opcodes.ASM4, writer);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            final String methodToPatch = "updateRenderer";
            final String methodToPatch_srg = "func_78907_a";
            final String methodToPatch_obf = "a";
            final String qdesc = "()V";
            if ((name.equals(methodToPatch) || name.equals(methodToPatch_srg) || name.equals(methodToPatch_obf))
                    && (desc.equals(qdesc))) {
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                return new InsertInitCodeBeforeReturnMethodVisitor(mv);

            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
    }


    public static class Visitor_ShouldSide extends ClassVisitor {
        public Visitor_ShouldSide(ClassWriter writer) {
            super(Opcodes.ASM4, writer);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            final String methodToPatch = "shouldSideBeRendered";
            final String methodToPatch_srg = "func_71877_c";
            final String methodToPatch_obf = "a";
            final String qdesc = "(Lnet/minecraft/world/IBlockAccess;IIII)Z";
            final String qdesc_obf = "(Lacf;IIII)Z";
            final String qdescInv = "(Lnet/minecraft/block/Block;Lnet/minecraft/world/IBlockAccess;IIII)Z";
            final String qdescInv_obf = "(Laqz;Lacf;IIII)Z";

            if ((name.equals(methodToPatch) || name.equals(methodToPatch_srg) || name.equals(methodToPatch_obf))
                    && (desc.equals(qdesc) || desc.equals(qdesc_obf))) {
                final String invokeDesc = desc.equals(qdesc) ? qdescInv : qdescInv_obf;

                return new MethodVisitor(Opcodes.ASM4, super.visitMethod(access, name, desc, signature, exceptions)) {
                    @Override
                    public void visitCode() {
                        mv.visitCode();
                        mv.visitVarInsn(Opcodes.ALOAD, 0);

                        mv.visitVarInsn(Opcodes.ALOAD, 1);
                        mv.visitVarInsn(Opcodes.ILOAD, 2);
                        mv.visitVarInsn(Opcodes.ILOAD, 3);
                        mv.visitVarInsn(Opcodes.ILOAD, 4);
                        mv.visitVarInsn(Opcodes.ILOAD, 5);
                        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "blusunrize/aquatweaks/AquaEventHandler", "liquid_shouldSideBeRendered",
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


}