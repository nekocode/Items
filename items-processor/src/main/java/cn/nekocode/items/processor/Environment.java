/*
 * Copyright 2018 nekocode (nekocode.cn@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.nekocode.items.processor;

import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public final class Environment {
    private static final ThreadLocal<ProcessingEnvironment> envs = new ThreadLocal<>();


    public static void set(ProcessingEnvironment env) {
        envs.set(env);
    }

    public static ProcessingEnvironment get() {
        return envs.get();
    }

    public static Messager getMessager() {
        return envs.get().getMessager();
    }

    public static Elements getElements() {
        return envs.get().getElementUtils();
    }

    public static Types getTypes() {
        return envs.get().getTypeUtils();
    }

    public static Filer getFiler() {
        return envs.get().getFiler();
    }

    public static void logError(String message) {
        envs.get().getMessager().printMessage(Diagnostic.Kind.ERROR, message);
    }

    public static void logWarning(String message) {
        envs.get().getMessager().printMessage(Diagnostic.Kind.WARNING, message);
    }

    public static String getPackageName(TypeElement classElement) {
        return getPackageOf(classElement).getQualifiedName().toString();
    }

    public static TypeElement getTypeElement(CharSequence charSequence) {
        return getElements().getTypeElement(charSequence);
    }

    public static PackageElement getPackageOf(Element element) {
        return getElements().getPackageOf(element);
    }

    public static boolean overrides(
            ExecutableElement overrider, ExecutableElement overridden, TypeElement type) {

        return getElements().overrides(overrider, overridden, type);
    }

    public static Element asElement(TypeMirror typeMirror) {
        return getTypes().asElement(typeMirror);
    }

    public static TypeElement asTypeElement(TypeMirror typeMirror) {
        return (TypeElement) asElement(typeMirror);
    }

    public static TypeMirror erasure(TypeMirror typeMirror) {
        return getTypes().erasure(typeMirror);
    }

    public static boolean isSameType(TypeMirror childType, TypeMirror parentType) {
        return getTypes().isSameType(childType, parentType);
    }

    public static boolean isSameRawType(TypeMirror childType, TypeMirror parentType) {
        return getTypes().isSameType(erasure(childType), erasure(parentType));
    }

    public static boolean isAssignable(TypeMirror childType, TypeMirror parentType) {
        return getTypes().isAssignable(childType, parentType);
    }

    public static boolean isRawTypeAssignable(TypeMirror childType, TypeMirror parentType) {
        return isAssignable(erasure(childType), erasure(parentType));
    }

    public static AnnotationMirror getAnnotationMirror(Element element, Class annotationClass) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (annotationClass.getCanonicalName()
                    .equals(mirror.getAnnotationType().toString())) {

                return mirror;
            }
        }
        return null;
    }

    public static AnnotationValue getAnnotationValue(AnnotationMirror mirror, String key) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                mirror.getElementValues().entrySet()) {

            if (entry.getKey().getSimpleName().contentEquals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static TypeMirror[] findGenericTypesPassToAncestor(
            TypeElement childElement, TypeElement ancestorElement) {

        final List<? extends TypeParameterElement> childParas = childElement.getTypeParameters();
        final TypeMirror[] childInputGenericTypes = new TypeMirror[childParas.size()];

        int i = 0;
        for (TypeParameterElement childPara : childParas) {
            childInputGenericTypes[i ++] = childPara.asType();
        }

        return findGenericTypesPassToAncestor(
                childElement, childInputGenericTypes, ancestorElement);
    }

    public static TypeMirror[] findGenericTypesPassToAncestor(
            TypeElement childElement, TypeMirror[] childInputGenericTypes,
            TypeElement ancestorElement) {

        final TypeMirror rawChildType = erasure(childElement.asType());
        final TypeMirror rawAncestorType = erasure(ancestorElement.asType());

        if (isSameType(rawChildType, rawAncestorType)) {
            return getParentGenericTypes(
                    childElement.asType(), childInputGenericTypes, ancestorElement.asType());

        } else if (!isAssignable(rawChildType, rawAncestorType)) {
            return null;
        }

        TypeMirror[] inputGenericTypes, rlt;
        if (ancestorElement.getKind().isInterface()) {
            for (TypeMirror parentInterfaceType : childElement.getInterfaces()) {
                if (!isAssignable(erasure(parentInterfaceType), rawAncestorType))
                    continue;

                inputGenericTypes = getParentGenericTypes(
                        childElement.asType(), childInputGenericTypes,
                        parentInterfaceType);

                rlt = findGenericTypesPassToAncestor(
                        asTypeElement(parentInterfaceType), inputGenericTypes,
                        ancestorElement);

                if (rlt != null) {
                    return rlt;
                }
            }
        }

        final TypeMirror superClassType = childElement.getSuperclass();
        inputGenericTypes = getParentGenericTypes(
                childElement.asType(), childInputGenericTypes,
                superClassType);

        return findGenericTypesPassToAncestor(
                asTypeElement(superClassType), inputGenericTypes,
                ancestorElement);
    }

    private static TypeMirror[] getParentGenericTypes(
            TypeMirror childType, TypeMirror[] childInputGenericTypes,
            TypeMirror parentType) {

        final List<? extends TypeMirror> childTypeArgs = ((DeclaredType) childType).getTypeArguments();
        final List<? extends TypeMirror> parentTypeArgs = ((DeclaredType) parentType).getTypeArguments();

        final TypeMirror[] rlt = new TypeMirror[parentTypeArgs.size()];
        int i = 0, j;
        for (TypeMirror parentTypeArg : parentTypeArgs) {
            if (parentTypeArg.getKind().equals(TypeKind.DECLARED)) {
                rlt[i] = parentTypeArg;

            } else {
                j = 0;
                for (TypeMirror childTypeArg: childTypeArgs) {
                    if (childTypeArg.equals(parentTypeArg)) {
                        rlt[i] = childInputGenericTypes[j];
                        break;
                    }
                    j ++;
                }
            }
            i ++;
        }

        return rlt;
    }
}
