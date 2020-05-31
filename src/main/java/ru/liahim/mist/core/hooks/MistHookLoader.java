package ru.liahim.mist.core.hooks;

import ru.liahim.mist.core.minecraft.HookLoader;
import ru.liahim.mist.core.minecraft.PrimaryClassTransformer;

public class MistHookLoader extends HookLoader {

	@Override
	public String[] getASMTransformerClass() {
		return new String[] { PrimaryClassTransformer.class.getName() };
	}

	@Override
	public void registerHooks() {
		registerHookContainer("ru.liahim.mist.core.hooks.AnnotationHooks");
	}
}