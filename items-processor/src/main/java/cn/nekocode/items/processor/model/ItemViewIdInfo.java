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

package cn.nekocode.items.processor.model;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import cn.nekocode.items.annotation.ItemViewId;
import cn.nekocode.items.processor.Constants;
import cn.nekocode.items.processor.ElementException;
import cn.nekocode.items.processor.Environment;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public class ItemViewIdInfo {
    private ExecutableElement mMethoElement;
    private TypeElement mTargetClassElement;


    public static ItemViewIdInfo parse(ExecutableElement element, TypeElement selectorElement) {
        final AnnotationMirror annotationMirror =
                Environment.getAnnotationMirror(element, ItemViewId.class);
        final TypeMirror value =
                (TypeMirror) Environment.getAnnotationValue(annotationMirror, "value").getValue();
        final TypeElement targetClassElement = Environment.asTypeElement(value);

        if (!isDataTypeMatched(targetClassElement, selectorElement)) {
            throw new ElementException(selectorElement,
                    "The data type of selector does not match the inner itemview [" +
                            targetClassElement.getSimpleName() + "].\n");
        }

        return new ItemViewIdInfo(element, targetClassElement);
    }

    private static boolean isDataTypeMatched(
            TypeElement itemViewElement, TypeElement selectorElement) {

        final TypeElement itemViewAncestorElement =
                Environment.getTypeElement(Constants.CLASSPATH_ITEM_VIEW);
        final TypeElement selectorAncestorElement =
                Environment.getTypeElement(Constants.CLASSPATH_ITEM_VIEW_SELECTOR);

        final TypeMirror[] itemViewDataTypes =
                Environment.findGenericTypesPassToAncestor(
                        itemViewElement, itemViewAncestorElement);
        final TypeMirror[] selectorDataTypes =
                Environment.findGenericTypesPassToAncestor(
                        selectorElement, selectorAncestorElement);

        return (itemViewDataTypes != null && itemViewDataTypes.length == 1) &&
                (selectorDataTypes != null && selectorDataTypes.length == 1) &&
                itemViewDataTypes[0] != null &&
                (itemViewDataTypes[0].equals(selectorDataTypes[0]));
    }

    private ItemViewIdInfo(ExecutableElement methoElement, TypeElement targetClassElement) {
        this.mMethoElement = methoElement;
        this.mTargetClassElement = targetClassElement;
    }

    public ExecutableElement getMethoElement() {
        return mMethoElement;
    }

    public TypeElement getTargetClassElement() {
        return mTargetClassElement;
    }
}
