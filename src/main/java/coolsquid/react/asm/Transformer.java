package coolsquid.react.asm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

public class Transformer implements IClassTransformer, IFMLLoadingPlugin {

	private static boolean transformEntity;

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (transformEntity && transformedName.equals("net.minecraft.entity.Entity")) {
			ClassNode c = createClassNode(basicClass);
			MethodNode m = this.getMethod(c, "move", "(Lnet/minecraft/entity/MoverType;DDD)V", "a", "(Lamu;)I");
			InsnList toInject = new InsnList();
			toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
			toInject.add(new VarInsnNode(Opcodes.ALOAD, 1));
			toInject.add(new VarInsnNode(Opcodes.DLOAD, 2));
			toInject.add(new VarInsnNode(Opcodes.DLOAD, 4));
			toInject.add(new VarInsnNode(Opcodes.DLOAD, 6));
			toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(Hooks.class), "fireMoveEvent",
					"(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/MoverType;DDD)Z", false));
			LabelNode l1 = new LabelNode();
			toInject.add(new JumpInsnNode(Opcodes.IFEQ, l1));
			LabelNode l2 = new LabelNode();
			toInject.add(l2);
			toInject.add(new InsnNode(Opcodes.RETURN));
			toInject.add(l1);
			m.instructions.insertBefore(m.instructions.getFirst(), toInject);
			return toBytes(c);
		}
		return basicClass;
	}

	private MethodNode getMethod(ClassNode c, String name, String desc, String obfName, String obfDesc) {
		for (MethodNode m : c.methods) {
			if ((m.name.equals(name) || m.name.equals(obfName)) && (m.desc.equals(desc) || m.desc.equals(obfDesc))) {
				return m;
			}
		}
		return null;
	}

	private static ClassNode createClassNode(byte[] bytes) {
		ClassNode c = new ClassNode();
		ClassReader r = new ClassReader(bytes);
		r.accept(c, ClassReader.EXPAND_FRAMES);
		return c;
	}

	private static byte[] toBytes(ClassNode c) {
		ClassWriter w = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		c.accept(w);
		return w.toByteArray();
	}

	@Override
	public String[] getASMTransformerClass() {
		Properties properties = new Properties();
		try {
			File propFile = new File("config/react/asm.properties");
			if (!propFile.exists()) {
				if (!propFile.getParentFile().exists()) {
					propFile.getParentFile().mkdirs();
				}
				propFile.createNewFile();
			}
			properties.load(new FileInputStream(propFile));
			if (properties.containsKey("transformEntity")) {
				transformEntity = Boolean.parseBoolean(properties.getProperty("transformEntity", "false"));
			} else {
				properties.setProperty("transformEntity", "false");
			}
			properties.store(new FileOutputStream(propFile), "");
			if (transformEntity) {
				return new String[] { this.getClass().getName() };
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}
}