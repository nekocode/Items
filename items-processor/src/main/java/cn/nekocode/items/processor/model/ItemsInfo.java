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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import cn.nekocode.items.annotation.Items;
import cn.nekocode.items.processor.Constants;
import cn.nekocode.items.processor.ElementException;
import cn.nekocode.items.processor.Environment;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public class ItemsInfo {
    private final TypeElement mCollectionType;
    private final TypeElement mAdapterType;
    private final LinkedHashMap<TypeElement, ItemViewInfo> mBindingMap;


    public static ItemsInfo parse(TypeElement element) {
        final AnnotationMirror annotationMirror = Environment.getAnnotationMirror(element, Items.class);

        /*
          ItemDataCollection
         */
        final TypeElement collectionType;
        final AnnotationValue collectionTypeValue = Environment.getAnnotationValue(annotationMirror, "collectionType");
        if (collectionTypeValue != null) {
            collectionType = Environment.asTypeElement((TypeMirror) collectionTypeValue.getValue());

        } else {
            collectionType = Environment.getTypeElement(Constants.CLASSPATH_ITEM_DATA_ARRAY_LIST);
        }

        /*
          AdapterType
         */
        final TypeElement adapterType;
        final AnnotationValue adapter = Environment.getAnnotationValue(annotationMirror, "adapterType");
        if (adapter != null) {
            adapterType = Environment.asTypeElement((TypeMirror) adapter.getValue());

            switch (adapterType.getQualifiedName().toString()) {
                // ...
                case Constants.CLASSPATH_RECYCLER_VIEW_ADAPTER:
                    break;

                default:
                    throw new ElementException(element,
                            "Unsupported adapter type [" + adapterType.getSimpleName() +  "].");
            }

        } else {
            adapterType = Environment.getTypeElement(Constants.CLASSPATH_RECYCLER_VIEW_ADAPTER);
        }

        /*
          ItemBinding[]
         */
        final LinkedHashMap<TypeElement, ItemViewInfo> bindings = new LinkedHashMap<>();
        final AnnotationValue value = Environment.getAnnotationValue(annotationMirror, "value");
        final TypeElement selectorAncestorElement =
                Environment.getTypeElement(Constants.CLASSPATH_ITEM_VIEW_SELECTOR);
        final TypeElement viewAncestorElement =
                Environment.getTypeElement(Constants.CLASSPATH_ITEM_VIEW);

        AnnotationMirror data, view;
        AnnotationValue dataValue, viewSelector, viewValue;
        TypeElement dataElement, viewElement, selectorElement;
        ItemViewInfo itemViewInfo;
        for (AnnotationMirror binding : (List<AnnotationMirror>) value.getValue()) {
            /*
              data
             */
            data = (AnnotationMirror) Environment.getAnnotationValue(binding, "data").getValue();
            dataValue = Environment.getAnnotationValue(data, "value");
            dataElement = Environment.asTypeElement((TypeMirror) dataValue.getValue());

            /*
              view
             */
            view = (AnnotationMirror) Environment.getAnnotationValue(binding, "view").getValue();
            viewSelector = Environment.getAnnotationValue(view, "selector");
            viewValue = Environment.getAnnotationValue(view, "value");

            if (viewSelector != null) {
                // Selector
                selectorElement = Environment.asTypeElement((TypeMirror) viewSelector.getValue());

                if (isDataTypeMatched(selectorElement, selectorAncestorElement, dataElement.asType())) {
                    itemViewInfo = new ItemViewInfo(true, selectorElement);

                } else {
                    throw new ElementException(element,
                            "Selector [" + selectorElement.getSimpleName() + "] does not match data [" +
                                    dataElement.getSimpleName() + "].");
                }

            } else {
                // TODO Ensure the view type match the adapter type
                // ItemView
                if (viewValue != null) {
                    viewElement = Environment.asTypeElement((TypeMirror) viewValue.getValue());
                    if (isDataTypeMatched(viewElement, viewAncestorElement, dataElement.asType())) {
                        itemViewInfo = new ItemViewInfo(false, viewElement);

                    } else {
                        throw new ElementException(element,
                                "View [" + viewElement.getSimpleName() + "] does not match data [" +
                                        dataElement.getSimpleName() + "].");
                    }

                } else {
                    throw new ElementException(element,
                            "Element: " + element.getSimpleName() + "Does not declare view for data [" +
                                    dataElement.getSimpleName() + "].");
                }
            }

            bindings.put(dataElement, itemViewInfo);
        }

        return new ItemsInfo(collectionType, adapterType, bindings);
    }

    private static boolean isDataTypeMatched(
            TypeElement element, TypeElement ancestorElement, TypeMirror dataType) {

        final TypeMirror[] genericTypes =
                Environment.findGenericTypesPassToAncestor(
                        element, ancestorElement);

        return !(genericTypes == null || genericTypes.length != 1) &&
                dataType.equals(genericTypes[0]);
    }

    private ItemsInfo(
            TypeElement collection,
            TypeElement adapterType,
            LinkedHashMap<TypeElement, ItemViewInfo> bindingMap) {

        this.mCollectionType = collection;
        this.mAdapterType = adapterType;
        this.mBindingMap = bindingMap;
    }

    public TypeElement getCollectionType() {
        return mCollectionType;
    }

    public TypeElement getAdapterType() {
        return mAdapterType;
    }

    public Map<TypeElement, ItemViewInfo> getBindingMap() {
        return mBindingMap;
    }
}
