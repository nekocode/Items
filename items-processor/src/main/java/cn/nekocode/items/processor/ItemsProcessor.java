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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

import cn.nekocode.items.annotation.ItemViewId;
import cn.nekocode.items.annotation.Items;
import cn.nekocode.items.processor.generator.RecyclerViewAdapterGenerator;
import cn.nekocode.items.processor.model.ItemViewIdInfo;
import cn.nekocode.items.processor.model.ItemsInfo;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public class ItemsProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
            types.add(annotation.getCanonicalName());
        }
        return types;
    }

    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        final Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();
        annotations.add(Items.class);
        annotations.add(ItemViewId.class);
        return annotations;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        Environment.set(processingEnvironment);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment env) {
        try {
            /*
              Find and parse all annotated element
             */
            final Map<TypeElement, ItemsInfo> itemsMap = findAndParseItems(env);
            final Map<TypeElement, List<ItemViewIdInfo>> selectorMap = findAndParseSelectors(env);

            /*
              Generate class files
             */
            ItemsInfo itemsInfo;
            for (Map.Entry<TypeElement, ItemsInfo> entry : itemsMap.entrySet()) {
                itemsInfo = entry.getValue();

                switch (itemsInfo.getAdapterType().getQualifiedName().toString()) {
                    case Constants.CLASSPATH_RECYCLER_VIEW_ADAPTER:
                        new RecyclerViewAdapterGenerator(
                                entry.getKey(), entry.getValue(), selectorMap)
                                .generate()
                                .writeTo(Environment.getFiler());
                        break;
                }
            }

        } catch (Exception e) {
            Environment.logError(e.getMessage());
        }

        return true;
    }

    private Map<TypeElement, ItemsInfo> findAndParseItems(RoundEnvironment env) {
        final Map<TypeElement, ItemsInfo> map = new LinkedHashMap<>();

        ItemsInfo itemsInfo;
        for (Element element : env.getElementsAnnotatedWith(Items.class)) {
            if (element.getKind() != ElementKind.INTERFACE) {
                throw new ElementException(element,
                        "@Items can only be applied to interface.");
            }

            itemsInfo = ItemsInfo.parse((TypeElement) element);
            map.put((TypeElement) element, itemsInfo);
        }

        return map;
    }

    private Map<TypeElement, List<ItemViewIdInfo>> findAndParseSelectors(RoundEnvironment env) {
        final TypeElement selectorAncestorElement =
                Environment.getTypeElement(Constants.CLASSPATH_ITEM_VIEW_SELECTOR);
        final ExecutableElement selectMethodElement =
                (ExecutableElement) selectorAncestorElement.getEnclosedElements().get(0);
        final Map<TypeElement, List<ItemViewIdInfo>> map = new LinkedHashMap<>();

        TypeElement ownerClassElement;
        ArrayList<ItemViewIdInfo> methodElements;
        for (Element element : env.getElementsAnnotatedWith(ItemViewId.class)) {
            ownerClassElement = (TypeElement) element.getEnclosingElement();

            if (!element.getModifiers().contains(Modifier.ABSTRACT)) {
                throw new ElementException(ownerClassElement,
                        "The method [" + element.getSimpleName() +  "] must be abstract.");
            }
            if (!((ExecutableElement) element).getReturnType().getKind().equals(TypeKind.INT)) {
                throw new ElementException(ownerClassElement,
                        "The method [" + element.getSimpleName() +  "] must return int value.");
            }
            if (((ExecutableElement) element).getParameters().size() != 0) {
                throw new ElementException(ownerClassElement,
                        "The method [" + element.getSimpleName() +  "] must has no parameters.");
            }
            if (!Environment.isRawTypeAssignable(
                    ownerClassElement.asType(), selectorAncestorElement.asType())) {

                throw new ElementException(ownerClassElement,
                        "Must implement interface [" + selectorAncestorElement.getSimpleName() +  "].");
            }

            boolean hasOverride = false;
            ExecutableElement methodElement;
            for (Element innerElement : ownerClassElement.getEnclosedElements()) {
                if (innerElement.getKind().equals(ElementKind.METHOD)) {
                    methodElement = (ExecutableElement) innerElement;

                    if (Environment.overrides(methodElement, selectMethodElement, ownerClassElement)) {
                        hasOverride = true;
                        break;
                    }
                }
            }
            if (!hasOverride) {
                throw new ElementException(ownerClassElement,
                        "Must override method [" + selectMethodElement.getSimpleName() +  "].");
            }

            methodElements = (ArrayList<ItemViewIdInfo>) map.get(ownerClassElement);
            if (methodElements == null) {
                methodElements = new ArrayList<>();
                map.put(ownerClassElement, methodElements);
            }
            methodElements.add(ItemViewIdInfo.parse((ExecutableElement) element, ownerClassElement));
        }

        return map;
    }
}
