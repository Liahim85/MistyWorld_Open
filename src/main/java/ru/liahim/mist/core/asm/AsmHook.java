package ru.liahim.mist.core.asm;

import ru.liahim.mist.core.asm.HookInjectorFactory.MethodEnter;
import ru.liahim.mist.core.asm.HookInjectorFactory.MethodExit;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * РљР»Р°СЃСЃ, РѕС‚РІРµС‡Р°СЋС‰РёР№ Р·Р° СѓСЃС‚Р°РЅРѕРІРєСѓ РѕРґРЅРѕРіРѕ С…СѓРєР° РІ РѕРґРёРЅ РјРµС‚РѕРґ.
 * РўРµСЂРјРёРЅРѕР»РѕРіРёСЏ:
 * hook (С…СѓРє) - РІС‹Р·РѕРІ РІР°С€РµРіРѕ СЃС‚Р°С‚РёС‡РµСЃРєРѕРіРѕ РјРµС‚РѕРґР° РёР· СЃС‚РѕСЂРѕРЅРЅРµРіРѕ РєРѕРґР° (РјР°Р№РЅРєСЂР°С„С‚Р°, С„РѕСЂРґР¶Р°, РґСЂСѓРіРёС… РјРѕРґРѕРІ)
 * targetMethod (С†РµР»РµРІРѕР№ РјРµС‚РѕРґ) - РјРµС‚РѕРґ, РєСѓРґР° РІСЃС‚Р°РІР»СЏРµС‚СЃСЏ С…СѓРє
 * targetClass (С†РµР»РµРІРѕР№ РєР»Р°СЃСЃ) - РєР»Р°СЃСЃ, РіРґРµ РЅР°С…РѕРґРёС‚СЃСЏ РјРµС‚РѕРґ, РєСѓРґР° РІСЃС‚Р°РІР»СЏРµС‚СЃСЏ С…СѓРє
 * hookMethod (С…СѓРє-РјРµС‚РѕРґ) - РІР°С€ СЃС‚Р°С‚РёС‡РµСЃРєРёР№ РјРµС‚РѕРґ, РєРѕС‚РѕСЂС‹Р№ РІС‹Р·С‹РІР°РµС‚СЃСЏ РёР· СЃС‚РѕСЂРѕРЅРЅРµРіРѕ РєРѕРґР°
 * hookClass (РєР»Р°СЃСЃ СЃ С…СѓРєРѕРј) - РєР»Р°СЃСЃ, РІ РєРѕС‚РѕСЂРѕРј СЃРѕРґРµСЂР¶РёС‚СЃСЏ С…СѓРє-РјРµС‚РѕРґ
 */
public class AsmHook implements Cloneable, Comparable<AsmHook> {

    private String targetClassName; // С‡РµСЂРµР· С‚РѕС‡РєРё
    private String targetMethodName;
    private List<Type> targetMethodParameters = new ArrayList<Type>(2);
    private Type targetMethodReturnType; //РµСЃР»Рё РЅРµ Р·Р°РґР°РЅРѕ, С‚Рѕ РЅРµ РїСЂРѕРІРµСЂСЏРµС‚СЃСЏ

    private String hooksClassName; // С‡РµСЂРµР· С‚РѕС‡РєРё
    private String hookMethodName;
    // -1 - Р·РЅР°С‡РµРЅРёРµ return
    private List<Integer> transmittableVariableIds = new ArrayList<Integer>(2);
    private List<Type> hookMethodParameters = new ArrayList<Type>(2);
    private Type hookMethodReturnType = Type.VOID_TYPE;
    private boolean hasReturnValueParameter; // РµСЃР»Рё РІ С…СѓРє-РјРµС‚РѕРґ РїРµСЂРµРґР°РµС‚СЃСЏ Р·РЅР°С‡РµРЅРёРµ РёР· return

    private ReturnCondition returnCondition = ReturnCondition.NEVER;
    private ReturnValue returnValue = ReturnValue.VOID;
    private Object primitiveConstant;

    private HookInjectorFactory injectorFactory = ON_ENTER_FACTORY;
    private HookPriority priority = HookPriority.NORMAL;

    public static final HookInjectorFactory ON_ENTER_FACTORY = MethodEnter.INSTANCE;
    public static final HookInjectorFactory ON_EXIT_FACTORY = MethodExit.INSTANCE;

    // РјРѕР¶РµС‚ Р±С‹С‚СЊ Р±РµР· РІРѕР·РІСЂР°С‰Р°РµРјРѕРіРѕ С‚РёРїР°
    private String targetMethodDescription;
    private String hookMethodDescription;
    private String returnMethodName;
    // РјРѕР¶РµС‚ Р±С‹С‚СЊ Р±РµР· РІРѕР·РІСЂР°С‰Р°РµРјРѕРіРѕ С‚РёРїР°
    private String returnMethodDescription;

    private boolean createMethod;
    private boolean isMandatory;

    protected String getTargetClassName() {
        return targetClassName;
    }

    private String getTargetClassInternalName() {
        return targetClassName.replace('.', '/');
    }

    private String getHookClassInternalName() {
        return hooksClassName.replace('.', '/');
    }

    protected boolean isTargetMethod(String name, String desc) {
        return (targetMethodReturnType == null && desc.startsWith(targetMethodDescription) ||
                desc.equals(targetMethodDescription)) && name.equals(targetMethodName);
    }

    protected boolean getCreateMethod() {
        return createMethod;
    }

    protected boolean isMandatory() {
         return isMandatory;
    }

    protected HookInjectorFactory getInjectorFactory() {
        return injectorFactory;
    }

    private boolean hasHookMethod() {
        return hookMethodName != null && hooksClassName != null;
    }

    protected void createMethod(HookInjectorClassVisitor classVisitor) {
        ClassMetadataReader.MethodReference superMethod = classVisitor.transformer.classMetadataReader
                .findVirtualMethod(getTargetClassInternalName(), targetMethodName, targetMethodDescription);
        // СЋР·Р°РµРј РЅР°Р·РІР°РЅРёРµ СЃСѓРїРµСЂРјРµС‚РѕРґР°, РїРѕС‚РѕРјСѓ С‡С‚Рѕ findVirtualMethod РјРѕР¶РµС‚ РІРµСЂРЅСѓС‚СЊ РјРµС‚РѕРґ СЃ РґСЂСѓРіРёРј РЅР°Р·РІР°РЅРёРµРј
        MethodVisitor mv = classVisitor.visitMethod(Opcodes.ACC_PUBLIC,
                superMethod == null ? targetMethodName : superMethod.name, targetMethodDescription, null, null);
        if (mv instanceof HookInjectorMethodVisitor) {
            HookInjectorMethodVisitor inj = (HookInjectorMethodVisitor) mv;
            inj.visitCode();
            inj.visitLabel(new Label());
            if (superMethod == null) {
                injectDefaultValue(inj, targetMethodReturnType);
            } else {
                injectSuperCall(inj, superMethod);
            }
            injectReturn(inj, targetMethodReturnType);
            inj.visitLabel(new Label());
            inj.visitMaxs(0, 0);
            inj.visitEnd();
        } else {
            throw new IllegalArgumentException("Hook injector not created");
        }
    }

    protected void inject(HookInjectorMethodVisitor inj) {
        Type targetMethodReturnType = inj.methodType.getReturnType();

        // СЃРѕС…СЂР°РЅСЏРµРј Р·РЅР°С‡РµРЅРёРµ, РєРѕС‚РѕСЂРѕРµ Р±С‹Р»Рѕ РїРµСЂРµРґР°РЅРѕ return РІ Р»РѕРєР°Р»СЊРЅСѓСЋ РїРµСЂРµРјРµРЅРЅСѓСЋ
        int returnLocalId = -1;
        if (hasReturnValueParameter) {
            returnLocalId = inj.newLocal(targetMethodReturnType);
            inj.visitVarInsn(targetMethodReturnType.getOpcode(54), returnLocalId); //storeLocal
        }

        // РІС‹Р·С‹РІР°РµРј С…СѓРє-РјРµС‚РѕРґ
        int hookResultLocalId = -1;
        if (hasHookMethod()) {
            injectInvokeStatic(inj, returnLocalId, hookMethodName, hookMethodDescription);

            if (returnValue == ReturnValue.HOOK_RETURN_VALUE || returnCondition.requiresCondition) {
                hookResultLocalId = inj.newLocal(hookMethodReturnType);
                inj.visitVarInsn(hookMethodReturnType.getOpcode(54), hookResultLocalId); //storeLocal
            }
        }

        // РІС‹Р·С‹РІР°РµРј return
        if (returnCondition != ReturnCondition.NEVER) {
            Label label = inj.newLabel();

            // РІСЃС‚Р°РІР»СЏРµРј GOTO-РїРµСЂРµС…РѕРґ Рє label'Сѓ РїРѕСЃР»Рµ РІС‹Р·РѕРІР° return
            if (returnCondition != ReturnCondition.ALWAYS) {
                inj.visitVarInsn(hookMethodReturnType.getOpcode(21), hookResultLocalId); //loadLocal
                if (returnCondition == ReturnCondition.ON_TRUE) {
                    inj.visitJumpInsn(IFEQ, label);
                } else if (returnCondition == ReturnCondition.ON_NULL) {
                    inj.visitJumpInsn(IFNONNULL, label);
                } else if (returnCondition == ReturnCondition.ON_NOT_NULL) {
                    inj.visitJumpInsn(IFNULL, label);
                }
            }

            // РІСЃС‚Р°РІР»СЏРµРј РІ СЃС‚Р°Рє Р·РЅР°С‡РµРЅРёРµ, РєРѕС‚РѕСЂРѕРµ РЅРµРѕР±С…РѕРґРёРјРѕ РІРµСЂРЅСѓС‚СЊ
            if (returnValue == ReturnValue.NULL) {
                inj.visitInsn(Opcodes.ACONST_NULL);
            } else if (returnValue == ReturnValue.PRIMITIVE_CONSTANT) {
                inj.visitLdcInsn(primitiveConstant);
            } else if (returnValue == ReturnValue.HOOK_RETURN_VALUE) {
                inj.visitVarInsn(hookMethodReturnType.getOpcode(21), hookResultLocalId); //loadLocal
            } else if (returnValue == ReturnValue.ANOTHER_METHOD_RETURN_VALUE) {
                String returnMethodDescription = this.returnMethodDescription;
                // РµСЃР»Рё РЅРµ Р±С‹Р» РѕРїСЂРµРґРµР»С‘РЅ Р·Р°СЂР°РЅРµРµ РЅСѓР¶РЅС‹Р№ РІРѕР·РІСЂР°С‰Р°РµРјС‹Р№ С‚РёРї, С‚Рѕ РґРѕР±Р°РІР»СЏРµРј РµРіРѕ Рє РѕРїРёСЃР°РЅРёСЋ
                if (returnMethodDescription.endsWith(")")) {
                    returnMethodDescription += targetMethodReturnType.getDescriptor();
                }
                injectInvokeStatic(inj, returnLocalId, returnMethodName, returnMethodDescription);
            }

            // РІС‹Р·С‹РІР°РµРј return
            injectReturn(inj, targetMethodReturnType);

            // РІСЃС‚Р°РІР»СЏРµРј label, Рє РєРѕС‚РѕСЂРѕРјСѓ РёРґРµС‚ GOTO-РїРµСЂРµС…РѕРґ
            inj.visitLabel(label);
        }

        //РєР»Р°РґРµРј РІ СЃС‚РµРє Р·РЅР°С‡РµРЅРёРµ, РєРѕС‚РѕСЂРѕРµ С€Р»Рѕ РІ return
        if (hasReturnValueParameter) {
            injectLoad(inj, targetMethodReturnType, returnLocalId);
        }
    }

    private void injectLoad(HookInjectorMethodVisitor inj, Type parameterType, int variableId) {
        int opcode;
        if (parameterType == INT_TYPE || parameterType == BYTE_TYPE || parameterType == CHAR_TYPE ||
                parameterType == BOOLEAN_TYPE || parameterType == SHORT_TYPE) {
            opcode = ILOAD;
        } else if (parameterType == LONG_TYPE) {
            opcode = LLOAD;
        } else if (parameterType == FLOAT_TYPE) {
            opcode = FLOAD;
        } else if (parameterType == DOUBLE_TYPE) {
            opcode = DLOAD;
        } else {
            opcode = ALOAD;
        }
        inj.visitVarInsn(opcode, variableId);
    }

    private void injectSuperCall(HookInjectorMethodVisitor inj, ClassMetadataReader.MethodReference method) {
        int variableId = 0;
        for (int i = 0; i <= targetMethodParameters.size(); i++) {
            Type parameterType = i == 0 ? TypeHelper.getType(targetClassName) : targetMethodParameters.get(i - 1);
            injectLoad(inj, parameterType, variableId);
            if (parameterType.getSort() == Type.DOUBLE || parameterType.getSort() == Type.LONG) {
                variableId += 2;
            } else {
                variableId++;
            }
        }
        inj.visitMethodInsn(INVOKESPECIAL, method.owner, method.name, method.desc, false);
    }

    private void injectDefaultValue(HookInjectorMethodVisitor inj, Type targetMethodReturnType) {
        switch (targetMethodReturnType.getSort()) {
            case Type.VOID:
                break;
            case Type.BOOLEAN:
            case Type.CHAR:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
                inj.visitInsn(Opcodes.ICONST_0);
                break;
            case Type.FLOAT:
                inj.visitInsn(Opcodes.FCONST_0);
                break;
            case Type.LONG:
                inj.visitInsn(Opcodes.LCONST_0);
                break;
            case Type.DOUBLE:
                inj.visitInsn(Opcodes.DCONST_0);
                break;
            default:
                inj.visitInsn(Opcodes.ACONST_NULL);
                break;
        }
    }

    private void injectReturn(HookInjectorMethodVisitor inj, Type targetMethodReturnType) {
        if (targetMethodReturnType == INT_TYPE || targetMethodReturnType == SHORT_TYPE ||
                targetMethodReturnType == BOOLEAN_TYPE || targetMethodReturnType == BYTE_TYPE
                || targetMethodReturnType == CHAR_TYPE) {
            inj.visitInsn(IRETURN);
        } else if (targetMethodReturnType == LONG_TYPE) {
            inj.visitInsn(LRETURN);
        } else if (targetMethodReturnType == FLOAT_TYPE) {
            inj.visitInsn(FRETURN);
        } else if (targetMethodReturnType == DOUBLE_TYPE) {
            inj.visitInsn(DRETURN);
        } else if (targetMethodReturnType == VOID_TYPE) {
            inj.visitInsn(RETURN);
        } else {
            inj.visitInsn(ARETURN);
        }
    }

    private void injectInvokeStatic(HookInjectorMethodVisitor inj, int returnLocalId, String name, String desc) {
        for (int i = 0; i < hookMethodParameters.size(); i++) {
            Type parameterType = hookMethodParameters.get(i);
            int variableId = transmittableVariableIds.get(i);
            if (inj.isStatic) {
                // РµСЃР»Рё РїРѕРїС‹С‚РєР° РїРµСЂРµРґР°С‡Рё this РёР· СЃС‚Р°С‚РёС‡РµСЃРєРѕРіРѕ РјРµС‚РѕРґР°, С‚Рѕ РїРµСЂРµРґР°РµРј null
                if (variableId == 0) {
                    inj.visitInsn(Opcodes.ACONST_NULL);
                    continue;
                }
                // РёРЅР°С‡Рµ СЃРґРІРёРіР°РµРј РЅРѕРјРµСЂ Р»РѕРєР°Р»СЊРЅРѕР№ РїРµСЂРµРјРµРЅРЅРѕР№
                if (variableId > 0) variableId--;
            }
            if (variableId == -1) variableId = returnLocalId;
            injectLoad(inj, parameterType, variableId);
        }

        inj.visitMethodInsn(INVOKESTATIC, getHookClassInternalName(), name, desc, false);
    }

    public String getPatchedMethodName() {
        return targetClassName + '#' + targetMethodName + targetMethodDescription;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AsmHook: ");

        sb.append(targetClassName).append('#').append(targetMethodName);
        sb.append(targetMethodDescription);
        sb.append(" -> ");
        sb.append(hooksClassName).append('#').append(hookMethodName);
        sb.append(hookMethodDescription);

        sb.append(", ReturnCondition=" + returnCondition);
        sb.append(", ReturnValue=" + returnValue);
        if (returnValue == ReturnValue.PRIMITIVE_CONSTANT) sb.append(", Constant=" + primitiveConstant);
        sb.append(", InjectorFactory: " + injectorFactory.getClass().getName());
        sb.append(", CreateMethod = " + createMethod);

        return sb.toString();
    }

    @Override
    public int compareTo(AsmHook o) {
        if (injectorFactory.isPriorityInverted && o.injectorFactory.isPriorityInverted) {
            return priority.ordinal() > o.priority.ordinal() ? -1 : 1;
        } else if (!injectorFactory.isPriorityInverted && !o.injectorFactory.isPriorityInverted) {
            return priority.ordinal() > o.priority.ordinal() ? 1 : -1;
        } else {
            return injectorFactory.isPriorityInverted ? 1 : -1;
        }
    }

    public static Builder newBuilder() {
        return new AsmHook().new Builder();
    }

    public class Builder extends AsmHook {

        private Builder() {

        }

        /**
         * --- РћР‘РЇР—РђРўР•Р›Р¬РќРћ Р’Р«Р—Р’РђРўР¬ ---
         * РћРїСЂРµРґРµР»СЏРµС‚ РЅР°Р·РІР°РЅРёРµ РєР»Р°СЃСЃР°, РІ РєРѕС‚РѕСЂС‹Р№ РЅРµРѕР±С…РѕРґРёРјРѕ СѓСЃС‚Р°РЅРѕРІРёС‚СЊ С…СѓРє.
         *
         * @param className РќР°Р·РІР°РЅРёРµ РєР»Р°СЃСЃР° СЃ СѓРєР°Р·Р°РЅРёРµРј РїР°РєРµС‚Р°, СЂР°Р·РґРµР»РµРЅРЅРѕРµ С‚РѕС‡РєР°РјРё.
         *                  РќР°РїСЂРёРјРµСЂ: net.minecraft.world.World
         */
        public Builder setTargetClass(String className) {
            AsmHook.this.targetClassName = className;
            return this;
        }

        /**
         * --- РћР‘РЇР—РђРўР•Р›Р¬РќРћ Р’Р«Р—Р’РђРўР¬ ---
         * РћРїСЂРµРґРµР»СЏРµС‚ РЅР°Р·РІР°РЅРёРµ РјРµС‚РѕРґР°, РІ РєРѕС‚РѕСЂС‹Р№ РЅРµРѕР±С…РѕРґРёРјРѕ РІСЃС‚Р°РІРёС‚СЊ С…СѓРє.
         * Р•СЃР»Рё РЅСѓР¶РЅРѕ РїСЂРѕРїР°С‚С‡РёС‚СЊ РєРѕРЅСЃС‚СЂСѓРєС‚РѕСЂ, С‚Рѕ РІ РЅР°Р·РІР°РЅРёРё РјРµС‚РѕРґР° РЅСѓР¶РЅРѕ СѓРєР°Р·Р°С‚СЊ <init>.
         *
         * @param methodName РќР°Р·РІР°РЅРёРµ РјРµС‚РѕРґР°.
         *                   РќР°РїСЂРёРјРµСЂ: getBlockId
         */
        public Builder setTargetMethod(String methodName) {
            AsmHook.this.targetMethodName = methodName;
            return this;
        }

        /**
         * --- РћР‘РЇР—РђРўР•Р›Р¬РќРћ Р’Р«Р—Р’РђРўР¬, Р•РЎР›Р� РЈ Р¦Р•Р›Р•Р’РћР“Рћ РњР•РўРћР”Рђ Р•РЎРўР¬ РџРђР РђРњР•РўР Р« ---
         * Р”РѕР±Р°РІР»СЏРµС‚ РѕРґРёРЅ РёР»Рё РЅРµСЃРєРѕР»СЊРєРѕ РїР°СЂР°РјРµС‚СЂРѕРІ Рє СЃРїРёСЃРєСѓ РїР°СЂР°РјРµС‚СЂРѕРІ С†РµР»РµРІРѕРіРѕ РјРµС‚РѕРґР°.
         * <p/>
         * Р­С‚Рё РїР°СЂР°РјРµС‚СЂС‹ РёСЃРїРѕР»СЊР·СѓСЋС‚СЃСЏ, С‡С‚РѕР±С‹ СЃРѕСЃС‚Р°РІРёС‚СЊ РѕРїРёСЃР°РЅРёРµ С†РµР»РµРІРѕРіРѕ РјРµС‚РѕРґР°.
         * Р§С‚РѕР±С‹ РѕРґРЅРѕР·РЅР°С‡РЅРѕ РѕРїСЂРµРґРµР»РёС‚СЊ С†РµР»РµРІРѕР№ РјРµС‚РѕРґ, РЅРµРґРѕСЃС‚Р°С‚РѕС‡РЅРѕ С‚РѕР»СЊРєРѕ РµРіРѕ РЅР°Р·РІР°РЅРёСЏ - РЅСѓР¶РЅРѕ РµС‰С‘ Рё РѕРїРёСЃР°РЅРёРµ.
         * <p/>
         * РџСЂРёРјРµСЂС‹ РёСЃРїРѕР»СЊР·РѕРІР°РЅРёСЏ:
         * import static gloomyfolken.hooklib.asm.TypeHelper.*
         * //...
         * addTargetMethodParameters(Type.INT_TYPE)
         * Type worldType = getType("net.minecraft.world.World")
         * Type playerType = getType("net.minecraft.entity.player.EntityPlayer")
         * addTargetMethodParameters(worldType, playerType, playerType)
         *
         * @param parameterTypes РўРёРїС‹ РїР°СЂР°РјРµС‚СЂРѕРІ С†РµР»РµРІРѕРіРѕ РјРµС‚РѕРґР°
         * @see TypeHelper
         */
        public Builder addTargetMethodParameters(Type... parameterTypes) {
            for (Type type : parameterTypes) {
                AsmHook.this.targetMethodParameters.add(type);
            }
            return this;
        }

        /**
         * Р”РѕР±Р°РІР»СЏРµС‚ РѕРґРёРЅ РёР»Рё РЅРµСЃРєРѕР»СЊРєРѕ РїР°СЂР°РјРµС‚СЂРѕРІ Рє СЃРїРёСЃРєСѓ РїР°СЂР°РјРµС‚СЂРѕРІ С†РµР»РµРІРѕРіРѕ РјРµС‚РѕРґР°.
         * РћР±С‘СЂС‚РєР° РЅР°Рґ addTargetMethodParameters(Type... parameterTypes), РєРѕС‚РѕСЂР°СЏ СЃР°РјР° СЃС‚СЂРѕРёС‚ С‚РёРїС‹ РёР· РЅР°Р·РІР°РЅРёСЏ.
         *
         * @param parameterTypeNames РќР°Р·РІР°РЅРёСЏ РєР»Р°СЃСЃРѕРІ РїР°СЂР°РјРµС‚СЂРѕРІ С†РµР»РµРІРѕРіРѕ РјРµС‚РѕРґР°.
         *                           РќР°РїСЂРёРјРµСЂ: net.minecraft.world.World
         */

        public Builder addTargetMethodParameters(String... parameterTypeNames) {
            Type[] types = new Type[parameterTypeNames.length];
            for (int i = 0; i < parameterTypeNames.length; i++) {
                types[i] = TypeHelper.getType(parameterTypeNames[i]);
            }
            return addTargetMethodParameters(types);
        }

        /**
         * Р�Р·РјРµРЅСЏРµС‚ С‚РёРї, РІРѕР·РІСЂР°С‰Р°РµРјС‹Р№ С†РµР»РµРІС‹Рј РјРµС‚РѕРґРѕРј.
         * Р’РѕРІСЂР°С‰Р°РµРјС‹Р№ С‚РёРї РёСЃРїРѕР»СЊР·СѓРµС‚СЃСЏ, С‡С‚РѕР±С‹ СЃРѕСЃС‚Р°РІРёС‚СЊ РѕРїРёСЃР°РЅРёРµ С†РµР»РµРІРѕРіРѕ РјРµС‚РѕРґР°.
         * Р§С‚РѕР±С‹ РѕРґРЅРѕР·РЅР°С‡РЅРѕ РѕРїСЂРµРґРµР»РёС‚СЊ С†РµР»РµРІРѕР№ РјРµС‚РѕРґ, РЅРµРґРѕСЃС‚Р°С‚РѕС‡РЅРѕ С‚РѕР»СЊРєРѕ РµРіРѕ РЅР°Р·РІР°РЅРёСЏ - РЅСѓР¶РЅРѕ РµС‰С‘ Рё РѕРїРёСЃР°РЅРёРµ.
         * РџРѕ СѓРјРѕР»С‡Р°РЅРёСЋ С…СѓРє РїСЂРёРјРµРЅСЏРµС‚СЃСЏ РєРѕ РІСЃРµРј РјРµС‚РѕРґР°Рј, РїРѕРґС…РѕРґСЏС‰РёРј РїРѕ РЅР°Р·РІР°РЅРёСЋ Рё СЃРїРёСЃРєСѓ РїР°СЂР°РјРµС‚СЂРѕРІ.
         *
         * @param returnType РўРёРї, РІРѕР·РІСЂР°С‰Р°РµРјС‹Р№ С†РµР»РµРІС‹Рј РјРµС‚РѕРґРѕРј
         * @see TypeHelper
         */
        public Builder setTargetMethodReturnType(Type returnType) {
            AsmHook.this.targetMethodReturnType = returnType;
            return this;
        }

        /**
         * Р�Р·РјРµРЅСЏРµС‚ С‚РёРї, РІРѕР·РІСЂР°С‰Р°РµРјС‹Р№ С†РµР»РµРІС‹Рј РјРµС‚РѕРґРѕРј.
         * РћР±С‘СЂС‚РєР° РЅР°Рґ setTargetMethodReturnType(Type returnType)
         *
         * @param returnType РќР°Р·РІР°РЅРёРµ РєР»Р°СЃСЃР°, СЌРєР·РµРјРїР»СЏСЂ РєРѕС‚РѕСЂРѕРіРѕ РІРѕР·РІСЂР°С‰Р°РµС‚ С†РµР»РµРІРѕР№ РјРµС‚РѕРґ
         */
        public Builder setTargetMethodReturnType(String returnType) {
            return setTargetMethodReturnType(TypeHelper.getType(returnType));
        }

        /**
         * --- РћР‘РЇР—РђРўР•Р›Р¬РќРћ Р’Р«Р—Р’РђРўР¬, Р•РЎР›Р� РќРЈР–Р•Рќ РҐРЈРљ-РњР•РўРћР”, Рђ РќР• РџР РћРЎРўРћ return SOME_CONSTANT ---
         * РћРїСЂРµРґРµР»СЏРµС‚ РЅР°Р·РІР°РЅРёРµ РєР»Р°СЃСЃР°, РІ РєРѕС‚РѕСЂРѕРј РЅР°С…РѕРґРёС‚СЃСЏ С…СѓРє-РјРµС‚РѕРґ.
         *
         * @param className РќР°Р·РІР°РЅРёРµ РєР»Р°СЃСЃР° СЃ СѓРєР°Р·Р°РЅРёРµРј РїР°РєРµС‚Р°, СЂР°Р·РґРµР»РµРЅРЅРѕРµ С‚РѕС‡РєР°РјРё.
         *                  РќР°РїСЂРёРјРµСЂ: net.myname.mymod.asm.MyHooks
         */
        public Builder setHookClass(String className) {
            AsmHook.this.hooksClassName = className;
            return this;
        }

        /**
         * --- РћР‘РЇР—РђРўР•Р›Р¬РќРћ Р’Р«Р—Р’РђРўР¬, Р•РЎР›Р� РќРЈР–Р•Рќ РҐРЈРљ-РњР•РўРћР”, Рђ РќР• РџР РћРЎРўРћ return SOME_CONSTANT ---
         * РћРїСЂРµРґРµР»СЏРµС‚ РЅР°Р·РІР°РЅРёРµ С…СѓРє-РјРµС‚РѕРґР°.
         * РҐРЈРљ-РњР•РўРћР” Р”РћР›Р–Р•Рќ Р‘Р«РўР¬ РЎРўРђРўР�Р§Р•РЎРљР�Рњ, Рђ РџР РћР’Р•Р РљР� РќРђ Р­РўРћ РќР•Рў. Р‘СѓРґСЊС‚Рµ РІРЅРёРјР°С‚РµР»СЊРЅС‹.
         *
         * @param methodName РќР°Р·РІР°РЅРёРµ С…СѓРє-РјРµС‚РѕРґР°.
         *                   РќР°РїСЂРёРјРµСЂ: myFirstHook
         */
        public Builder setHookMethod(String methodName) {
            AsmHook.this.hookMethodName = methodName;
            return this;
        }

        /**
         * --- РћР‘РЇР—РђРўР•Р›Р¬РќРћ Р’Р«Р—Р’РђРўР¬, Р•РЎР›Р� РЈ РҐРЈРљ-РњР•РўРћР”Рђ Р•РЎРўР¬ РџРђР РђРњР•РўР Р« ---
         * Р”РѕР±Р°РІР»СЏРµС‚ РїР°СЂР°РјРµС‚СЂ РІ СЃРїРёСЃРѕРє РїР°СЂР°РјРµС‚СЂРѕРІ С…СѓРє-РјРµС‚РѕРґР°.
         * Р’ Р±Р°Р№С‚РєРѕРґРµ РЅРµ СЃРѕС…СЂР°РЅСЏСЋС‚СЃСЏ РЅР°Р·РІР°РЅРёСЏ РїР°СЂР°РјРµС‚СЂРѕРІ. Р’РјРµСЃС‚Рѕ СЌС‚РѕРіРѕ РїСЂРёС…РѕРґРёС‚СЃСЏ РёСЃРїРѕР»СЊР·РѕРІР°С‚СЊ РёС… РЅРѕРјРµСЂР°.
         * РќР°РїСЂРёРјРµСЂ, РІ РєР»Р°СЃСЃРµ EntityLivingBase РµСЃС‚СЊ РјРµС‚РѕРґ attackEntityFrom(DamageSource damageSource, float damage).
         * Р’ РЅС‘Рј Р±СѓРґСѓС‚ РёСЃРїРѕР»СЊР·РѕРІР°С‚СЊСЃСЏ С‚Р°РєРёРµ РЅРѕРјРµСЂР° РїР°СЂР°РјРµС‚СЂРѕРІ:
         * 1 - damageSource
         * 2 - damage
         * Р’РђР–РќР«Р™ РњРћРњР•РќРў: LONG Р� DOUBLE "Р—РђРќР�РњРђР®Рў" Р”Р’Рђ РќРћРњР•Р Рђ.
         * РўРµРѕСЂРµС‚РёС‡РµСЃРєРё, РєСЂРѕРјРµ РїР°СЂР°РјРµС‚СЂРѕРІ РІ С…СѓРє-РјРµС‚РѕРґ РјРѕР¶РЅРѕ РїРµСЂРµРґР°С‚СЊ Рё Р»РѕРєР°Р»СЊРЅС‹Рµ РїРµСЂРµРјРµРЅРЅС‹Рµ, РЅРѕ РёС…
         * РЅРѕРјРµСЂР° СЃР»РѕР¶РЅРµРµ РїРѕСЃС‡РёС‚Р°С‚СЊ.
         * РќР°РїСЂРёРјРµСЂ, РІ РєР»Р°СЃСЃРµ Entity РµСЃС‚СЊ РјРµС‚РѕРґ setPosition(double x, double y, double z).
         * Р’ РЅС‘Рј Р±СѓРґСѓС‚ С‚Р°РєРёРµ РЅРѕРјРµСЂР° РїР°СЂР°РјРµС‚СЂРѕРІ:
         * 1 - x
         * 2 - РїСЂРѕРїСѓС‰РµРЅРѕ
         * 3 - y
         * 4 - РїСЂРѕРїСѓС‰РµРЅРѕ
         * 5 - z
         * 6 - РїСЂРѕРїСѓС‰РµРЅРѕ
         * <p/>
         * РљРѕРґ СЌС‚РѕРіРѕ РјРµС‚РѕРґР° С‚Р°РєРѕРІ:
         * //...
         * float f = ...;
         * float f1 = ...;
         * //...
         * Р’ С‚Р°РєРѕРј СЃР»СѓС‡Р°Рµ Сѓ f Р±СѓРґРµС‚ РЅРѕРјРµСЂ 7, Р° Сѓ f1 - 8.
         * <p/>
         * Р•СЃР»Рё С†РµР»РµРІРѕР№ РјРµС‚РѕРґ static, С‚Рѕ РЅРµ РЅСѓР¶РЅРѕ РЅР°С‡РёРЅР°С‚СЊ РѕС‚СЃС‡РµС‚ Р»РѕРєР°Р»СЊРЅС‹С… РїРµСЂРµРјРµРЅРЅС‹С… СЃ РЅСѓР»СЏ, РЅРѕРјРµСЂР°
         * Р±СѓРґСѓС‚ СЃРјРµС‰РµРЅС‹ Р°РІС‚РѕРјР°С‚РёС‡РµСЃРєРё.
         *
         * @param parameterType РўРёРї РїР°СЂР°РјРµС‚СЂР° С…СѓРє-РјРµС‚РѕРґР°
         * @param variableId    ID Р·РЅР°С‡РµРЅРёСЏ, РїРµСЂРµРґР°РІР°РµРјРѕРіРѕ РІ С…СѓРє-РјРµС‚РѕРґ
         * @throws IllegalStateException РµСЃР»Рё РЅРµ Р·Р°РґР°РЅРѕ РЅР°Р·РІР°РЅРёРµ С…СѓРє-РјРµС‚РѕРґР° РёР»Рё РєР»Р°СЃСЃР°, РєРѕС‚РѕСЂС‹Р№ РµРіРѕ СЃРѕРґРµСЂР¶РёС‚
         */
        public Builder addHookMethodParameter(Type parameterType, int variableId) {
            if (!AsmHook.this.hasHookMethod()) {
                throw new IllegalStateException("Hook method is not specified, so can not append " +
                        "parameter to its parameters list.");
            }
            AsmHook.this.hookMethodParameters.add(parameterType);
            AsmHook.this.transmittableVariableIds.add(variableId);
            return this;
        }

        /**
         * Р”РѕР±Р°РІР»СЏРµС‚ РїР°СЂР°РјРµС‚СЂ РІ СЃРїРёСЃРѕРє РїР°СЂР°РјРµС‚СЂРѕРІ С†РµР»РµРІРѕРіРѕ РјРµС‚РѕРґР°.
         * РћР±С‘СЂС‚РєР° РЅР°Рґ addHookMethodParameter(Type parameterType, int variableId)
         *
         * @param parameterTypeName РќР°Р·РІР°РЅРёРµ С‚РёРїР° РїР°СЂР°РјРµС‚СЂР° С…СѓРє-РјРµС‚РѕРґР°.
         *                          РќР°РїСЂРёРјРµСЂ: net.minecraft.world.World
         * @param variableId        ID Р·РЅР°С‡РµРЅРёСЏ, РїРµСЂРµРґР°РІР°РµРјРѕРіРѕ РІ С…СѓРє-РјРµС‚РѕРґ
         */
        public Builder addHookMethodParameter(String parameterTypeName, int variableId) {
            return addHookMethodParameter(TypeHelper.getType(parameterTypeName), variableId);
        }

        /**
         * Р”РѕР±Р°РІР»СЏРµС‚ РІ СЃРїРёСЃРѕРє РїР°СЂР°РјРµС‚СЂРѕРІ С…СѓРє-РјРµС‚РѕРґР° С†РµР»РµРІРѕР№ РєР»Р°СЃСЃ Рё РїРµСЂРµРґР°РµС‚ С…СѓРє-РјРµС‚РѕРґСѓ this.
         * Р•СЃР»Рё С†РµР»РµРІРѕР№ РјРµС‚РѕРґ static, С‚Рѕ Р±СѓРґРµС‚ РїРµСЂРµРґР°РЅРѕ null.
         *
         * @throws IllegalStateException РµСЃР»Рё РЅРµ Р·Р°РґР°РЅ С…СѓРє-РјРµС‚РѕРґ
         */
        public Builder addThisToHookMethodParameters() {
            if (!AsmHook.this.hasHookMethod()) {
                throw new IllegalStateException("Hook method is not specified, so can not append " +
                        "parameter to its parameters list.");
            }
            AsmHook.this.hookMethodParameters.add(TypeHelper.getType(targetClassName));
            AsmHook.this.transmittableVariableIds.add(0);
            return this;
        }

        /**
         * Р”РѕР±Р°РІР»СЏРµС‚ РІ СЃРїРёСЃРѕРє РїР°СЂР°РјРµС‚СЂРѕРІ С…СѓРє-РјРµС‚РѕРґР° С‚РёРї, РІРѕР·РІСЂР°С‰Р°РµРјС‹Р№ С†РµР»РµРІС‹Рј РјРµС‚РѕРґРѕРј Рё
         * РїРµСЂРµРґР°РµС‚ С…СѓРє-РјРµС‚РѕРґСѓ Р·РЅР°С‡РµРЅРёРµ, РєРѕС‚РѕСЂРѕРµ РІРµСЂРЅС‘С‚ return.
         * Р‘РѕР»РµРµ С„РѕСЂРјР°Р»СЊРЅРѕ, РїСЂРё РІС‹Р·РѕРІРµ С…СѓРє-РјРµС‚РѕРґР° СѓРєР°Р·С‹РІР°РµС‚ РІ РєР°С‡РµСЃС‚РІРµ СЌС‚РѕРіРѕ РїР°СЂР°РјРµС‚СЂР° РІРµСЂС…РЅРµРµ Р·РЅР°С‡РµРЅРёРµ РІ СЃС‚РµРєРµ.
         * РќР° РїСЂР°РєС‚РёРєРµ РѕСЃРЅРѕРІРЅРѕРµ РїСЂРёРјРµРЅРµРЅРёРµ -
         * РќР°РїСЂРёРјРµСЂ, РµСЃС‚СЊ С‚Р°РєРѕР№ РєРѕРґ РјРµС‚РѕРґР°:
         * int foo = bar();
         * return foo;
         * Р�Р»Рё С‚Р°РєРѕР№:
         * return bar()
         * <p/>
         * Р’ РѕР±РѕРёС… СЃР»СѓС‡Р°СЏС… С…СѓРє-РјРµС‚РѕРґСѓ РјРѕР¶РЅРѕ РїРµСЂРµРґР°С‚СЊ РІРѕР·РІСЂР°С‰Р°РµРјРѕРµ Р·РЅР°С‡РµРЅРёРµ РїРµСЂРµРґ РІС‹Р·РѕРІРѕРј return.
         *
         * @throws IllegalStateException РµСЃР»Рё С†РµР»РµРІРѕР№ РјРµС‚РѕРґ РІРѕР·РІСЂР°С‰Р°РµС‚ void
         * @throws IllegalStateException РµСЃР»Рё РЅРµ Р·Р°РґР°РЅ С…СѓРє-РјРµС‚РѕРґ
         */
        public Builder addReturnValueToHookMethodParameters() {
            if (!AsmHook.this.hasHookMethod()) {
                throw new IllegalStateException("Hook method is not specified, so can not append " +
                        "parameter to its parameters list.");
            }
            if (AsmHook.this.targetMethodReturnType == Type.VOID_TYPE) {
                throw new IllegalStateException("Target method's return type is void, it does not make sense to " +
                        "transmit its return value to hook method.");
            }
            AsmHook.this.hookMethodParameters.add(AsmHook.this.targetMethodReturnType);
            AsmHook.this.transmittableVariableIds.add(-1);
            AsmHook.this.hasReturnValueParameter = true;
            return this;
        }

        /**
         * Р—Р°РґР°РµС‚ СѓСЃР»РѕРІРёРµ, РїСЂРё РєРѕС‚РѕСЂРѕРј РїРѕСЃР»Рµ РІС‹Р·РѕРІР° С…СѓРє-РјРµС‚РѕРґР° РІС‹Р·С‹РІР°РµС‚СЃСЏ return.
         * РџРѕ СѓРјРѕР»С‡Р°РЅРёСЋ return РЅРµ РІС‹Р·С‹РІР°РµС‚СЃСЏ РІРѕРѕР±С‰Рµ.
         * РљСЂРѕРјРµ С‚РѕРіРѕ, СЌС‚РѕС‚ РјРµС‚РѕРґ РёР·РјРµРЅСЏРµС‚ С‚РёРї РІРѕР·РІСЂР°С‰Р°РµРјРѕРіРѕ Р·РЅР°С‡РµРЅРёСЏ С…СѓРє-РјРµС‚РѕРґР°:
         * NEVER -> void
         * ALWAYS -> void
         * ON_TRUE -> boolean
         * ON_NULL -> Object
         * ON_NOT_NULL -> Object
         *
         * @param condition РЈСЃР»РѕРІРёРµ РІС‹С…РѕРґР° РїРѕСЃР»Рµ РІС‹Р·РѕРІР° С…СѓРє-РјРµС‚РѕРґР°
         * @throws IllegalArgumentException РµСЃР»Рё condition == ON_TRUE, ON_NULL РёР»Рё ON_NOT_NULL, РЅРѕ РЅРµ Р·Р°РґР°РЅ С…СѓРє-РјРµС‚РѕРґ.
         * @see ReturnCondition
         */
        public Builder setReturnCondition(ReturnCondition condition) {
            if (condition.requiresCondition && AsmHook.this.hookMethodName == null) {
                throw new IllegalArgumentException("Hook method is not specified, so can not use return " +
                        "condition that depends on hook method.");
            }

            AsmHook.this.returnCondition = condition;
            Type returnType;
            switch (condition) {
                case NEVER:
                case ALWAYS:
                    returnType = VOID_TYPE;
                    break;
                case ON_TRUE:
                    returnType = BOOLEAN_TYPE;
                    break;
                default:
                    returnType = getType(Object.class);
                    break;
            }
            AsmHook.this.hookMethodReturnType = returnType;
            return this;
        }

        /**
         * --- РћР‘РЇР—РђРўР•Р›Р¬РќРћ Р’Р«Р—Р’РђРўР¬, Р•РЎР›Р� Р¦Р•Р›Р•Р’РћР™ РњР•РўРћР” Р’РћР—Р’Р РђР©РђР•Рў РќР• void, Р� Р’Р«Р—Р’РђРќ setReturnCondition ---
         * Р—Р°РґР°РµС‚ Р·РЅР°С‡РµРЅРёРµ, РєРѕС‚РѕСЂРѕРµ РІРѕР·РІСЂР°С‰Р°РµС‚СЃСЏ РїСЂРё РІС‹Р·РѕРІРµ return РїРѕСЃР»Рµ РІС‹Р·РѕРІР° С…СѓРє-РјРµС‚РѕРґР°.
         * РЎР»РµРґСѓРµС‚ РІС‹Р·С‹РІР°С‚СЊ РїРѕСЃР»Рµ setReturnCondition.
         * РџРѕ СѓРјРѕР»С‡Р°РЅРёСЋ РІРѕР·РІСЂР°С‰Р°РµС‚СЃСЏ void.
         * РљСЂРѕРјРµ С‚РѕРіРѕ, РµСЃР»Рё value == ReturnValue.HOOK_RETURN_VALUE, С‚Рѕ СЌС‚РѕС‚ РјРµС‚РѕРґ РёР·РјРµРЅСЏРµС‚ С‚РёРї РІРѕР·РІСЂР°С‰Р°РµРјРѕРіРѕ
         * Р·РЅР°С‡РµРЅРёСЏ С…СѓРє-РјРµС‚РѕРґР° РЅР° С‚РёРї, СѓРєР°Р·Р°РЅРЅС‹Р№ РІ setTargetMethodReturnType()
         *
         * @param value РІРѕР·РІСЂР°С‰Р°РµРјРѕРµ Р·РЅР°С‡РµРЅРёРµ
         * @throws IllegalStateException    РµСЃР»Рё returnCondition == NEVER (С‚. Рµ. РµСЃР»Рё setReturnCondition() РЅРµ РІС‹Р·С‹РІР°Р»СЃСЏ).
         *                                  РќРµС‚ СЃРјС‹СЃР»Р° СѓРєР°Р·С‹РІР°С‚СЊ РІРѕР·РІСЂР°С‰Р°РµРјРѕРµ Р·РЅР°С‡РµРЅРёРµ, РµСЃР»Рё return РЅРµ РІС‹Р·С‹РІР°РµС‚СЃСЏ.
         * @throws IllegalArgumentException РµСЃР»Рё value == ReturnValue.HOOK_RETURN_VALUE, Р° С‚РёРї РІРѕР·РІСЂР°С‰Р°РµРјРѕРіРѕ Р·РЅР°С‡РµРЅРёСЏ
         *                                  С†РµР»РµРІРѕРіРѕ РјРµС‚РѕРґР° СѓРєР°Р·Р°РЅ РєР°Рє void (РёР»Рё setTargetMethodReturnType РµС‰С‘ РЅРµ РІС‹Р·С‹РІР°Р»СЃСЏ).
         *                                  РќРµС‚ СЃРјС‹СЃР»Р° РёСЃРїРѕР»СЊР·РѕРІР°С‚СЊ Р·РЅР°С‡РµРЅРёРµ, РєРѕС‚РѕСЂРѕРµ РІРµСЂРЅСѓР» С…СѓРє-РјРµС‚РѕРґ, РµСЃР»Рё РјРµС‚РѕРґ РІРѕР·РІСЂР°С‰Р°РµС‚ void.
         */
        public Builder setReturnValue(ReturnValue value) {
            if (AsmHook.this.returnCondition == ReturnCondition.NEVER) {
                throw new IllegalStateException("Current return condition is ReturnCondition.NEVER, so it does not " +
                        "make sense to specify the return value.");
            }
            Type returnType = AsmHook.this.targetMethodReturnType;
            if (value != ReturnValue.VOID && returnType == VOID_TYPE) {
                throw new IllegalArgumentException("Target method return value is void, so it does not make sense to " +
                        "return anything else.");
            }
            if (value == ReturnValue.VOID && returnType != VOID_TYPE) {
                throw new IllegalArgumentException("Target method return value is not void, so it is impossible " +
                        "to return VOID.");
            }
            if (value == ReturnValue.PRIMITIVE_CONSTANT && returnType != null && !isPrimitive(returnType)) {
                throw new IllegalArgumentException("Target method return value is not a primitive, so it is " +
                        "impossible to return PRIVITIVE_CONSTANT.");
            }
            if (value == ReturnValue.NULL && returnType != null && isPrimitive(returnType)) {
                throw new IllegalArgumentException("Target method return value is a primitive, so it is impossible " +
                        "to return NULL.");
            }
            if (value == ReturnValue.HOOK_RETURN_VALUE && !hasHookMethod()) {
                throw new IllegalArgumentException("Hook method is not specified, so can not use return " +
                        "value that depends on hook method.");
            }

            AsmHook.this.returnValue = value;
            if (value == ReturnValue.HOOK_RETURN_VALUE) {
                AsmHook.this.hookMethodReturnType = AsmHook.this.targetMethodReturnType;
            }
            return this;
        }

        /**
         * Р’РѕР·РІСЂР°С‰Р°РµС‚ С‚РёРї РІРѕР·РІСЂР°С‰Р°РµРјРѕРіРѕ Р·РЅР°С‡РµРЅРёСЏ С…СѓРє-РјРµС‚РѕРґР°, РµСЃР»Рё РєРѕРјСѓ-С‚Рѕ СЃР»РѕР¶РЅРѕ "РІС‹С‡РёСЃР»РёС‚СЊ" РµРіРѕ СЃР°РјРѕСЃС‚РѕСЏС‚РµР»СЊРЅРѕ.
         *
         * @return С‚РёРї РІРѕР·РІСЂР°С‰Р°РµРјРѕРіРѕ Р·РЅР°С‡РµРЅРёСЏ С…СѓРє-РјРµС‚РѕРґР°
         */
        public Type getHookMethodReturnType() {
            return hookMethodReturnType;
        }

        /**
         * РќР°РїСЂСЏРјСѓСЋ СѓРєР°Р·С‹РІР°РµС‚ С‚РёРї, РІРѕР·РІСЂР°С‰Р°РµРјС‹Р№ С…СѓРє-РјРµС‚РѕРґРѕРј.
         *
         * @param type
         */
        protected void setHookMethodReturnType(Type type) {
            AsmHook.this.hookMethodReturnType = type;
        }

        private boolean isPrimitive(Type type) {
            return type.getSort() > 0 && type.getSort() < 9;
        }

        /**
         * --- РћР‘РЇР—РђРўР•Р›Р¬РќРћ Р’Р«Р—Р’РђРўР¬, Р•РЎР›Р� Р’РћР—Р’Р РђР©РђР•РњРћР• Р—РќРђР§Р•РќР�Р• РЈРЎРўРђРќРћР’Р›Р•РќРћ РќРђ PRIMITIVE_CONSTANT ---
         * РЎР»РµРґСѓРµС‚ РІС‹Р·С‹РІР°С‚СЊ РїРѕСЃР»Рµ setReturnValue(ReturnValue.PRIMITIVE_CONSTANT)
         * Р—Р°РґР°РµС‚ РєРѕРЅСЃС‚Р°РЅС‚Сѓ, РєРѕС‚РѕСЂР°СЏ Р±СѓРґРµС‚ РІРѕР·РІСЂР°С‰РµРЅР° РїСЂРё РІС‹Р·РѕРІРµ return.
         * РљР»Р°СЃСЃ Р·Р°РґР°РЅРЅРѕРіРѕ РѕР±СЉРµРєС‚Р° РґРѕР»Р¶РµРЅ СЃРѕРѕС‚РІРµС‚СЃС‚РІРѕРІР°С‚СЊ РїСЂРёРјРёС‚РёРІРЅРѕРјСѓ С‚РёРїСѓ.
         * РќР°РїСЂРёРјРµСЂ, РµСЃР»Рё С†РµР»РµРІРѕР№ РјРµС‚РѕРґ РІРѕР·РІСЂР°С‰Р°РµС‚ int, С‚Рѕ РІ СЌС‚РѕС‚ РјРµС‚РѕРґ РґРѕР»Р¶РµРЅ Р±С‹С‚СЊ РїРµСЂРµРґР°РЅ РѕР±СЉРµРєС‚ РєР»Р°СЃСЃР° Integer.
         *
         * @param constant РћР±СЉРµРєС‚, РєР»Р°СЃСЃ РєРѕС‚РѕСЂРѕРіРѕ СЃРѕРѕС‚РІРµС‚СЃС‚РІСѓРµС‚ РїСЂРёРјРёС‚РёРІСѓ, РєРѕС‚РѕСЂС‹Р№ СЃР»РµРґСѓРµС‚ РІРѕР·РІСЂР°С‰Р°С‚СЊ.
         * @throws IllegalStateException    РµСЃР»Рё РІРѕР·РІСЂР°С‰Р°РµРјРѕРµ Р·РЅР°С‡РµРЅРёРµ РЅРµ СѓСЃС‚Р°РЅРѕРІР»РµРЅРѕ РЅР° PRIMITIVE_CONSTANT
         * @throws IllegalArgumentException РµСЃР»Рё РєР»Р°СЃСЃ РѕР±СЉРµРєС‚Р° constant РЅРµ СЏРІР»СЏРµС‚СЃСЏ РѕР±С‘СЂС‚РєРѕР№
         *                                  РґР»СЏ РїСЂРёРјРёС‚РёРІРЅРѕРіРѕ С‚РёРїР°, РєРѕС‚РѕСЂС‹Р№ РІРѕР·РІСЂР°С‰Р°РµС‚ С†РµР»РµРІРѕР№ РјРµС‚РѕРґ.
         */
        public Builder setPrimitiveConstant(Object constant) {
            if (AsmHook.this.returnValue != ReturnValue.PRIMITIVE_CONSTANT) {
                throw new IllegalStateException("Return value is not PRIMITIVE_CONSTANT, so it does not make sence" +
                        "to specify that constant.");
            }
            Type returnType = AsmHook.this.targetMethodReturnType;
            if (returnType == BOOLEAN_TYPE && !(constant instanceof Boolean) ||
                    returnType == CHAR_TYPE && !(constant instanceof Character) ||
                    returnType == BYTE_TYPE && !(constant instanceof Byte) ||
                    returnType == SHORT_TYPE && !(constant instanceof Short) ||
                    returnType == INT_TYPE && !(constant instanceof Integer) ||
                    returnType == LONG_TYPE && !(constant instanceof Long) ||
                    returnType == FLOAT_TYPE && !(constant instanceof Float) ||
                    returnType == DOUBLE_TYPE && !(constant instanceof Double)) {
                throw new IllegalArgumentException("Given object class does not math target method return type.");
            }

            AsmHook.this.primitiveConstant = constant;
            return this;
        }

        /**
         * --- РћР‘РЇР—РђРўР•Р›Р¬РќРћ Р’Р«Р—Р’РђРўР¬, Р•РЎР›Р� Р’РћР—Р’Р РђР©РђР•РњРћР• Р—РќРђР§Р•РќР�Р• РЈРЎРўРђРќРћР’Р›Р•РќРћ РќРђ ANOTHER_METHOD_RETURN_VALUE ---
         * РЎР»РµРґСѓРµС‚ РІС‹Р·С‹РІР°С‚СЊ РїРѕСЃР»Рµ setReturnValue(ReturnValue.ANOTHER_METHOD_RETURN_VALUE)
         * Р—Р°РґР°РµС‚ РјРµС‚РѕРґ, СЂРµР·СѓР»СЊС‚Р°С‚ РІС‹Р·РѕРІР° РєРѕС‚РѕСЂРѕРіРѕ Р±СѓРґРµС‚ РІРѕР·РІСЂР°С‰С‘РЅ РїСЂРё РІС‹Р·РѕРІРµ return.
         *
         * @param methodName РЅР°Р·РІР°РЅРёРµ РјРµС‚РѕРґР°, СЂРµР·СѓР»СЊС‚Р°С‚ РІС‹Р·РѕРІР° РєРѕС‚РѕСЂРѕРіРѕ СЃР»РµРґСѓРµС‚ РІРѕР·РІСЂР°С‰Р°С‚СЊ
         * @throws IllegalStateException РµСЃР»Рё РІРѕР·РІСЂР°С‰Р°РµРјРѕРµ Р·РЅР°С‡РµРЅРёРµ РЅРµ СѓСЃС‚Р°РЅРѕРІР»РµРЅРѕ РЅР° ANOTHER_METHOD_RETURN_VALUE
         */
        public Builder setReturnMethod(String methodName) {
            if (AsmHook.this.returnValue != ReturnValue.ANOTHER_METHOD_RETURN_VALUE) {
                throw new IllegalStateException("Return value is not ANOTHER_METHOD_RETURN_VALUE, " +
                        "so it does not make sence to specify that method.");
            }

            AsmHook.this.returnMethodName = methodName;
            return this;
        }

        /**
         * Р—Р°РґР°РµС‚ С„Р°Р±СЂРёРєСѓ, РєРѕС‚РѕСЂР°СЏ СЃРѕР·РґР°СЃС‚ РёРЅР¶РµРєС‚РѕСЂ РґР»СЏ СЌС‚РѕРіРѕ С…СѓРєР°.
         * Р•СЃР»Рё РіРѕРІРѕСЂРёС‚СЊ Р±РѕР»РµРµ С‡РµР»РѕРІРµС‡РµСЃРєРёРј СЏР·С‹РєРѕРј, С‚Рѕ СЌС‚РѕС‚ РјРµС‚РѕРґ РѕРїСЂРµРґРµР»СЏРµС‚, РіРґРµ Р±СѓРґРµС‚ РІСЃС‚Р°РІР»РµРЅ С…СѓРє:
         * РІ РЅР°С‡Р°Р»Рµ РјРµС‚РѕРґР°, РІ РєРѕРЅС†Рµ РёР»Рё РіРґРµ-С‚Рѕ РµС‰С‘.
         * Р•СЃР»Рё РЅРµ СЃРѕР·РґР°РІР°С‚СЊ СЃРІРѕРёС… РёРЅР¶РµРєС‚РѕСЂРѕРІ, С‚Рѕ РјРѕР¶РЅРѕ РёСЃРїРѕР»СЊР·РѕРІР°С‚СЊ РґРІРµ С„Р°Р±СЂРёРєРё:
         * AsmHook.ON_ENTER_FACTORY (РІСЃС‚Р°РІР»СЏРµС‚ С…СѓРє РЅР° РІС…РѕРґРµ РІ РјРµС‚РѕРґ, РёСЃРїРѕР»СЊР·СѓРµС‚СЃСЏ РїРѕ СѓРјРѕР»С‡Р°РЅРёСЋ)
         * AsmHook.ON_EXIT_FACTORY (РІСЃС‚Р°РІР»СЏРµС‚ С…СѓРє РЅР° РІС‹С…РѕРґРµ РёР· РјРµС‚РѕРґР°)
         *
         * @param factory Р¤Р°Р±СЂРёРєР°, СЃРѕР·РґР°СЋС‰Р°СЏ РёРЅР¶РµРєС‚РѕСЂ РґР»СЏ СЌС‚РѕРіРѕ С…СѓРєР°
         */
        public Builder setInjectorFactory(HookInjectorFactory factory) {
            AsmHook.this.injectorFactory = factory;
            return this;
        }

        /**
         * Р—Р°РґР°РµС‚ РїСЂРёРѕСЂРёС‚РµС‚ С…СѓРєР°.
         * РҐСѓРєРё СЃ Р±РѕР»СЊС€РёРј РїСЂРёРѕСЂРёС‚РµС‚РѕРј РІС‹Р·Р°РІР°СЋС‚СЃСЏ СЂР°РЅСЊС€Рµ.
         */
        public Builder setPriority(HookPriority priority) {
            AsmHook.this.priority = priority;
            return this;
        }

        /**
         * РџРѕР·РІРѕР»СЏРµС‚ РЅРµ С‚РѕР»СЊРєРѕ РІСЃС‚Р°РІР»СЏС‚СЊ С…СѓРєРё РІ СЃСѓС‰РµСЃС‚РІСѓСЋС‰РёРµ РјРµС‚РѕРґС‹, РЅРѕ Рё РґРѕР±Р°РІР»СЏС‚СЊ РЅРѕРІС‹Рµ. Р­С‚Рѕ РјРѕР¶РµС‚ РїРѕРЅР°РґРѕР±РёС‚СЊСЃСЏ,
         * РєРѕРіРґР° РЅСѓР¶РЅРѕ РїРµСЂРµРѕРїСЂРµРґРµР»РёС‚СЊ РјРµС‚РѕРґ СЃСѓРїРµСЂРєР»Р°СЃСЃР°. Р•СЃР»Рё СЃСѓРїРµСЂ-РјРµС‚РѕРґ РЅР°Р№РґРµРЅ, С‚Рѕ С‚РµР»Рѕ РіРµРЅРµСЂРёСЂСѓРµРјРѕРіРѕ РјРµС‚РѕРґР°
         * РїСЂРµРґСЃС‚Р°РІР»СЏРµС‚ СЃРѕР±РѕР№ РІС‹Р·РѕРІ СЃСѓРїРµСЂ-РјРµС‚РѕРґР°. Р�РЅР°С‡Рµ СЌС‚Рѕ РїСЂРѕСЃС‚Рѕ РїСѓСЃС‚РѕР№ РјРµС‚РѕРґ РёР»Рё return false/0/null РІ Р·Р°РІРёСЃРёРјРѕСЃС‚Рё
         * РѕС‚ РІРѕР·РІСЂР°С‰Р°РµРјРѕРіРѕ С‚РёРїР°.
         */
        public Builder setCreateMethod(boolean createMethod) {
            AsmHook.this.createMethod = createMethod;
            return this;
        }

        /**
         * РџРѕР·РІРѕР»СЏРµС‚ РѕР±СЉСЏРІРёС‚СЊ С…СѓРє "РѕР±СЏР·Р°С‚РµР»СЊРЅС‹Рј" РґР»СЏ Р·Р°РїСѓСЃРєР° РёРіСЂС‹. Р’ СЃР»СѓС‡Р°Рµ РЅРµСѓРґР°С‡Рё РІРѕ РІСЂРµРјСЏ РІСЃС‚Р°РІРєРё С‚Р°РєРѕРіРѕ С…СѓРєР°
         * Р±СѓРґРµС‚ РЅРµ РїСЂРѕСЃС‚Рѕ РІС‹РІРµРґРµРЅРѕ СЃРѕРѕР±С‰РµРЅРёРµ РІ Р»РѕРі, Р° РєСЂР°С€РЅРµС‚СЃСЏ РёРіСЂР°.
         */
        public Builder setMandatory(boolean isMandatory) {
            AsmHook.this.isMandatory = isMandatory;
            return this;
        }

        private String getMethodDesc(Type returnType, List<Type> paramTypes) {
            Type[] paramTypesArray = paramTypes.toArray(new Type[0]);
            if (returnType == null) {
                String voidDesc = Type.getMethodDescriptor(Type.VOID_TYPE, paramTypesArray);
                return voidDesc.substring(0, voidDesc.length() - 1);
            } else {
                return Type.getMethodDescriptor(returnType, paramTypesArray);
            }
        }

        /**
         * РЎРѕР·РґР°РµС‚ С…СѓРє РїРѕ Р·Р°РґР°РЅРЅС‹Рј РїР°СЂР°РјРµС‚СЂР°Рј.
         *
         * @return РїРѕР»СѓС‡РµРЅРЅС‹Р№ С…СѓРє
         * @throws IllegalStateException РµСЃР»Рё РЅРµ Р±С‹Р» РІС‹Р·РІР°РЅ РєР°РєРѕР№-Р»РёР±Рѕ РёР· РѕР±СЏР·Р°С‚РµР»СЊРЅС‹С… РјРµС‚РѕРґРѕРІ
         */
        public AsmHook build() {
            AsmHook hook = AsmHook.this;

            if (hook.createMethod && hook.targetMethodReturnType == null) {
                hook.targetMethodReturnType = hook.hookMethodReturnType;
            }
            hook.targetMethodDescription = getMethodDesc(hook.targetMethodReturnType, hook.targetMethodParameters);

            if (hook.hasHookMethod()) {
                hook.hookMethodDescription = Type.getMethodDescriptor(hook.hookMethodReturnType,
                        hook.hookMethodParameters.toArray(new Type[0]));
            }
            if (hook.returnValue == ReturnValue.ANOTHER_METHOD_RETURN_VALUE) {
                hook.returnMethodDescription = getMethodDesc(hook.targetMethodReturnType, hook.hookMethodParameters);
            }

            try {
                hook = (AsmHook) AsmHook.this.clone();
            } catch (CloneNotSupportedException impossible) {
            }

            if (hook.targetClassName == null) {
                throw new IllegalStateException("Target class name is not specified. " +
                        "Call setTargetClassName() before build().");
            }

            if (hook.targetMethodName == null) {
                throw new IllegalStateException("Target method name is not specified. " +
                        "Call setTargetMethodName() before build().");
            }

            if (hook.returnValue == ReturnValue.PRIMITIVE_CONSTANT && hook.primitiveConstant == null) {
                throw new IllegalStateException("Return value is PRIMITIVE_CONSTANT, but the constant is not " +
                        "specified. Call setReturnValue() before build().");
            }

            if (hook.returnValue == ReturnValue.ANOTHER_METHOD_RETURN_VALUE && hook.returnMethodName == null) {
                throw new IllegalStateException("Return value is ANOTHER_METHOD_RETURN_VALUE, but the method is not " +
                        "specified. Call setReturnMethod() before build().");
            }

            if (!(hook.injectorFactory instanceof MethodExit) && hook.hasReturnValueParameter) {
                throw new IllegalStateException("Can not pass return value to hook method " +
                        "because hook location is not return insn.");
            }

            return hook;
        }

    }

}
