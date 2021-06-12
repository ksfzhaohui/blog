//package com.zh.asm.reflect;
//
//import org.objectweb.asm.*;
//
//import java.lang.reflect.Modifier;
//
//import static org.objectweb.asm.Opcodes.*;
//import static org.objectweb.asm.Opcodes.ATHROW;
//
//public class test {
//
//    public static void main(String[] args) {
//        String accessClassNameInternal = "com.zh.asm.reflect.TestBeanMethodAccess";
//        String classNameInternal ="com/zh/asm/reflect/TestBean";
//        boolean isInterface = false;
//
//        String methodNames[]={"getName","getId","getAge"};
//        String parameterTypes[]={};
//        String returnTypes[]={};
//
//        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
//        MethodVisitor mv;
//        cw.visit(V1_1, ACC_PUBLIC + ACC_SUPER, accessClassNameInternal, null, "com/esotericsoftware/reflectasm/MethodAccess",
//                null);
//        {
//            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
//            mv.visitCode();
//            mv.visitVarInsn(ALOAD, 0);
//            mv.visitMethodInsn(INVOKESPECIAL, "com/esotericsoftware/reflectasm/MethodAccess", "<init>", "()V");
//            mv.visitInsn(RETURN);
//            mv.visitMaxs(0, 0);
//            mv.visitEnd();
//        }
//        {
//            mv = cw.visitMethod(ACC_PUBLIC + ACC_VARARGS, "invoke",
//                    "(Ljava/lang/Object;I[Ljava/lang/Object;)Ljava/lang/Object;", null, null);
//            mv.visitCode();
//
//            if (!methods.isEmpty()) {
//                mv.visitVarInsn(ALOAD, 1);
//                mv.visitTypeInsn(CHECKCAST, classNameInternal);
//                mv.visitVarInsn(ASTORE, 4);
//
//                mv.visitVarInsn(ILOAD, 2);
//                Label[] labels = new Label[n];
//                for (int i = 0; i < n; i++)
//                    labels[i] = new Label();
//                Label defaultLabel = new Label();
//                mv.visitTableSwitchInsn(0, labels.length - 1, defaultLabel, labels);
//
//                StringBuilder buffer = new StringBuilder(128);
//                for (int i = 0; i < n; i++) {
//                    mv.visitLabel(labels[i]);
//                    if (i == 0)
//                        mv.visitFrame(Opcodes.F_APPEND, 1, new Object[] {classNameInternal}, 0, null);
//                    else
//                        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
//                    mv.visitVarInsn(ALOAD, 4);
//
//                    buffer.setLength(0);
//                    buffer.append('(');
//
//                    Class[] paramTypes = parameterTypes[i];
//                    Class returnType = returnTypes[i];
//                    for (int paramIndex = 0; paramIndex < paramTypes.length; paramIndex++) {
//                        mv.visitVarInsn(ALOAD, 3);
//                        mv.visitIntInsn(BIPUSH, paramIndex);
//                        mv.visitInsn(AALOAD);
//                        Type paramType = Type.getType(paramTypes[paramIndex]);
//                        switch (paramType.getSort()) {
//                            case Type.BOOLEAN:
//                                mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
//                                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
//                                break;
//                            case Type.BYTE:
//                                mv.visitTypeInsn(CHECKCAST, "java/lang/Byte");
//                                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B");
//                                break;
//                            case Type.CHAR:
//                                mv.visitTypeInsn(CHECKCAST, "java/lang/Character");
//                                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C");
//                                break;
//                            case Type.SHORT:
//                                mv.visitTypeInsn(CHECKCAST, "java/lang/Short");
//                                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S");
//                                break;
//                            case Type.INT:
//                                mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
//                                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
//                                break;
//                            case Type.FLOAT:
//                                mv.visitTypeInsn(CHECKCAST, "java/lang/Float");
//                                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F");
//                                break;
//                            case Type.LONG:
//                                mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
//                                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J");
//                                break;
//                            case Type.DOUBLE:
//                                mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
//                                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D");
//                                break;
//                            case Type.ARRAY:
//                                mv.visitTypeInsn(CHECKCAST, paramType.getDescriptor());
//                                break;
//                            case Type.OBJECT:
//                                mv.visitTypeInsn(CHECKCAST, paramType.getInternalName());
//                                break;
//                        }
//                        buffer.append(paramType.getDescriptor());
//                    }
//
//                    buffer.append(')');
//                    buffer.append(Type.getDescriptor(returnType));
//                    int invoke;
//                    if (isInterface)
//                        invoke = INVOKEINTERFACE;
//                    else if (Modifier.isStatic(methods.get(i).getModifiers()))
//                        invoke = INVOKESTATIC;
//                    else
//                        invoke = INVOKEVIRTUAL;
//                    mv.visitMethodInsn(invoke, classNameInternal, methodNames[i], buffer.toString());
//
//                    switch (Type.getType(returnType).getSort()) {
//                        case Type.VOID:
//                            mv.visitInsn(ACONST_NULL);
//                            break;
//                        case Type.BOOLEAN:
//                            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
//                            break;
//                        case Type.BYTE:
//                            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
//                            break;
//                        case Type.CHAR:
//                            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
//                            break;
//                        case Type.SHORT:
//                            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
//                            break;
//                        case Type.INT:
//                            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
//                            break;
//                        case Type.FLOAT:
//                            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
//                            break;
//                        case Type.LONG:
//                            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
//                            break;
//                        case Type.DOUBLE:
//                            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
//                            break;
//                    }
//
//                    mv.visitInsn(ARETURN);
//                }
//
//                mv.visitLabel(defaultLabel);
//                mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
//            }
//            mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
//            mv.visitInsn(DUP);
//            mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
//            mv.visitInsn(DUP);
//            mv.visitLdcInsn("Method not found: ");
//            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
//            mv.visitVarInsn(ILOAD, 2);
//            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;");
//            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
//            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V");
//            mv.visitInsn(ATHROW);
//            mv.visitMaxs(0, 0);
//            mv.visitEnd();
//        }
//        cw.visitEnd();
//        byte[] data = cw.toByteArray();
//    }
//}
