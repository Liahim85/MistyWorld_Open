package ru.liahim.mist.core.asm;

import org.objectweb.asm.MethodVisitor;

/**
 * Р В¤Р В°Р В±РЎР‚Р С‘Р С”Р В°, Р В·Р В°Р Т‘Р В°РЎР‹РЎвЂ°Р В°РЎРЏ РЎвЂљР С‘Р С— Р С‘Р Р…Р В¶Р ВµР С”РЎвЂљР С•РЎР‚Р В° РЎвЂ¦РЎС“Р С”Р С•Р Р†. Р В¤Р В°Р С”РЎвЂљР С‘РЎвЂЎР ВµРЎРѓР С”Р С‘, Р С•РЎвЂљ Р Р†РЎвЂ№Р В±Р С•РЎР‚Р В° РЎвЂћР В°Р В±РЎР‚Р С‘Р С”Р С‘ Р В·Р В°Р Р†Р С‘РЎРѓР С‘РЎвЂљ РЎвЂљР С•, Р Р† Р С”Р В°Р С”Р С‘Р Вµ РЎС“РЎвЂЎР В°РЎРѓРЎвЂљР С”Р С‘ Р С”Р С•Р Т‘Р В° Р С—Р С•Р С—Р В°Р Т‘РЎвЂ�РЎвЂљ РЎвЂ¦РЎС“Р С”.
 * "Р пїЅР В· Р С”Р С•РЎР‚Р С•Р В±Р С”Р С‘" Р Т‘Р С•РЎРѓРЎвЂљРЎС“Р С—Р Р…Р С• Р Т‘Р Р†Р В° РЎвЂљР С‘Р С—Р В° Р С‘Р Р…Р В¶Р ВµР С”РЎвЂљР С•РЎР‚Р С•Р Р†: MethodEnter, Р С”Р С•РЎвЂљР С•РЎР‚РЎвЂ№Р в„– Р Р†РЎРѓРЎвЂљР В°Р Р†Р В»РЎРЏР ВµРЎвЂљ РЎвЂ¦РЎС“Р С” Р Р…Р В° Р Р†РЎвЂ¦Р С•Р Т‘Р Вµ Р Р† Р С�Р ВµРЎвЂљР С•Р Т‘,
 * Р С‘ MethodExit, Р С”Р С•РЎвЂљР С•РЎР‚РЎвЂ№Р в„– Р Р†РЎРѓРЎвЂљР В°Р Р†Р В»РЎРЏР ВµРЎвЂљ РЎвЂ¦РЎС“Р С” Р Р…Р В° Р С”Р В°Р В¶Р Т‘Р С•Р С� Р Р†РЎвЂ№РЎвЂ¦Р С•Р Т‘Р Вµ.
 */
public abstract class HookInjectorFactory {

    /**
     * Р СљР ВµРЎвЂљР С•Р Т‘ AdviceAdapter#visitInsn() - РЎв‚¬РЎвЂљРЎС“Р С”Р В° РЎРѓРЎвЂљРЎР‚Р В°Р Р…Р Р…Р В°РЎРЏ. Р СћР В°Р С� Р С—Р С•РЎвЂЎР ВµР С�РЎС“-РЎвЂљР С• Р Р†РЎвЂ№Р В·Р С•Р Р† РЎРѓР В»Р ВµР Т‘РЎС“РЎР‹РЎвЂ°Р ВµР С–Р С• MethodVisitor'a
     * Р С—РЎР‚Р С•Р С‘Р В·Р Р†Р С•Р Т‘Р С‘РЎвЂљРЎРѓРЎРЏ Р С—Р С•РЎРѓР В»Р Вµ Р В»Р С•Р С–Р С‘Р С”Р С‘, Р В° Р Р…Р Вµ Р Т‘Р С•, Р С”Р В°Р С” Р Р†Р С• Р Р†РЎРѓР ВµРЎвЂ¦ Р С•РЎРѓРЎвЂљР В°Р В»РЎРЉР Р…РЎвЂ№РЎвЂ¦ РЎРѓР В»РЎС“РЎвЂЎР В°РЎРЏРЎвЂ¦. Р СџР С•РЎРЊРЎвЂљР С•Р С�РЎС“ Р Т‘Р В»РЎРЏ MethodExit Р С—РЎР‚Р С‘Р С•РЎР‚Р С‘РЎвЂљР ВµРЎвЂљ
     * РЎвЂ¦РЎС“Р С”Р С•Р Р† Р С‘Р Р…Р Р†Р ВµРЎР‚РЎвЂљР С‘РЎР‚РЎС“Р ВµРЎвЂљРЎРѓРЎРЏ.
     */
    protected boolean isPriorityInverted = false;

    abstract HookInjectorMethodVisitor createHookInjector(MethodVisitor mv, int access, String name, String desc,
                                                          AsmHook hook, HookInjectorClassVisitor cv);


    static class MethodEnter extends HookInjectorFactory {

        public static final MethodEnter INSTANCE = new MethodEnter();

        private MethodEnter() {}

        @Override
        public HookInjectorMethodVisitor createHookInjector(MethodVisitor mv, int access, String name, String desc,
                                                            AsmHook hook, HookInjectorClassVisitor cv) {
            return new HookInjectorMethodVisitor.MethodEnter(mv, access, name, desc, hook, cv);
        }

    }

    static class MethodExit extends HookInjectorFactory {

        public static final MethodExit INSTANCE = new MethodExit();

        private MethodExit() {
            isPriorityInverted = true;
        }

        @Override
        public HookInjectorMethodVisitor createHookInjector(MethodVisitor mv, int access, String name, String desc,
                                                            AsmHook hook, HookInjectorClassVisitor cv) {
            return new HookInjectorMethodVisitor.MethodExit(mv, access, name, desc, hook, cv);
        }
    }

    static class LineNumber extends HookInjectorFactory {

        private int lineNumber;

        public LineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
        }

        @Override
        public HookInjectorMethodVisitor createHookInjector(MethodVisitor mv, int access, String name, String desc,
                                                            AsmHook hook, HookInjectorClassVisitor cv) {
            return new HookInjectorMethodVisitor.LineNumber(mv, access, name, desc, hook, cv, lineNumber);
        }
    }

}
