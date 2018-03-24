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

package cn.nekocode.items.processor.generator;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.NoType;

import cn.nekocode.items.processor.Constants;
import cn.nekocode.items.processor.Environment;
import cn.nekocode.items.processor.model.ItemViewIdInfo;
import cn.nekocode.items.processor.model.ItemViewInfo;
import cn.nekocode.items.processor.model.ItemsInfo;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public class RecyclerViewAdapterGenerator {
    private final TypeElement mClassElement;
    private final ItemsInfo mItemsInfo;
    private final Map<TypeElement, List<ItemViewIdInfo>> mSelectorMap;


    public RecyclerViewAdapterGenerator(
            TypeElement classElement,
            ItemsInfo itemsInfo,
            Map<TypeElement, List<ItemViewIdInfo>> selectorMap) {

        this.mClassElement = classElement;
        this.mItemsInfo = itemsInfo;
        this.mSelectorMap = selectorMap;
    }

    public JavaFile generate() {
        final String packageName = Environment.getPackageName(mClassElement);
        final ClassName adapter =
                ClassName.get(packageName, mClassElement.getSimpleName().toString() + "Adapter");
        final ClassName arrayList = ClassName.get("java.util", "ArrayList");
        final ClassName recyclerView = ClassName.get("android.support.v7.widget", "RecyclerView");

        final AnnotationSpec nonNull = AnnotationSpec.builder(
                ClassName.get("android.support.annotation", "NonNull")).build();
        final AnnotationSpec nullable = AnnotationSpec.builder(
                ClassName.get("android.support.annotation", "Nullable")).build();


        final TypeSpec.Builder Adapter = TypeSpec.classBuilder(adapter)
                .superclass(ParameterizedTypeName.get(
                        recyclerView.nestedClass("Adapter"), recyclerView.nestedClass("ViewHolder")))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        /*
          Ready data
         */
        final Map<TypeElement, ItemViewInfo> bindingMap = mItemsInfo.getBindingMap();
        final Map<TypeElement, TypeElement> viewToDataMap = new LinkedHashMap<>();
        final Map<TypeElement, Integer> dataIdMap = new LinkedHashMap<>();
        final Map<TypeElement, Integer> viewIdMap = new LinkedHashMap<>();
        int dataIdCounter = 0;
        int viewIdCounter = 0;
        TypeElement dataElement, viewElement, selectorElement;
        List<ItemViewIdInfo> itemViewIdInfos;
        for (Map.Entry<TypeElement, ItemViewInfo> entry : bindingMap.entrySet()) {
            dataElement = entry.getKey();
            viewElement = entry.getValue().getTypeElement();

            dataIdMap.put(dataElement, ++ dataIdCounter);

            if (!entry.getValue().isSelector()) {
                // View class
                viewIdMap.put(viewElement, ++ viewIdCounter);
                viewToDataMap.put(viewElement, dataElement);

            } else {
                // Selector class
                selectorElement = viewElement;
                itemViewIdInfos = findAllAvailableItemViewIdInfo(mSelectorMap, selectorElement);
                for (ItemViewIdInfo info : itemViewIdInfos) {
                    viewElement = info.getTargetClassElement();
                    viewIdMap.put(viewElement, ++ viewIdCounter);
                    viewToDataMap.put(viewElement, dataElement);
                }
            }
        }


        /*
          Data and views' ids
         */
        final TypeSpec.Builder Data = TypeSpec.classBuilder("Data")
                .addModifiers(Modifier.STATIC, Modifier.FINAL);
        final TypeSpec.Builder View = TypeSpec.classBuilder("View")
                .addModifiers(Modifier.STATIC, Modifier.FINAL);

        for (Map.Entry<TypeElement, Integer> entry : dataIdMap.entrySet()) {
            Data.addField(FieldSpec.builder(TypeName.INT, entry.getKey().getSimpleName().toString())
                    .addModifiers(Modifier.STATIC, Modifier.FINAL)
                    .initializer("$L", entry.getValue())
                    .build()
            );
        }
        for (Map.Entry<TypeElement, Integer> entry : viewIdMap.entrySet()) {
            View.addField(FieldSpec.builder(TypeName.INT, entry.getKey().getSimpleName().toString())
                    .addModifiers(Modifier.STATIC, Modifier.FINAL)
                    .initializer("$L", entry.getValue())
                    .build()
            );
        }

        Adapter.addType(
                TypeSpec.classBuilder("Id")
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .addType(Data.build())
                        .addType(View.build())
                        .build()
        );


        /*
          Event listeners
         */
        final TypeSpec.Builder EventListener = TypeSpec.classBuilder("EventListener")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addMethod(
                        MethodSpec.methodBuilder("onItemClick")
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(TypeName.INT, "position")
                                .addParameter(TypeName.OBJECT, "data")
                                .build()
                )
                .addMethod(
                        MethodSpec.methodBuilder("onItemLongClick")
                                .addModifiers(Modifier.PUBLIC)
                                .returns(TypeName.BOOLEAN)
                                .addParameter(TypeName.INT, "position")
                                .addParameter(TypeName.OBJECT, "data")
                                .addStatement("return false")
                                .build()
                );

        final ClassName itemEvent = ClassName.get(Constants.LIBRARY_PACKAGE + ".view", "ItemEvent");

        for (Map.Entry<TypeElement, TypeElement> entry : viewToDataMap.entrySet()) {
            viewElement = entry.getKey();
            dataElement = entry.getValue();

            EventListener.addMethod(
                    MethodSpec.methodBuilder("on" + viewElement.getSimpleName() + "Event")
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(
                                    ParameterSpec.builder(
                                            ParameterizedTypeName.get(itemEvent, ClassName.get(dataElement)),
                                            "event"
                                    )
                                            .addAnnotation(nonNull)
                                            .build()
                             )
                            .build()
            );
        }
        Adapter.addType(EventListener.build());

        final ClassName itemEventHandler = ClassName.get(Constants.LIBRARY_PACKAGE + ".view", "ItemEventHandler");

        TypeName eventHandler;
        TypeSpec itemEventHandlerImpl;
        for (Map.Entry<TypeElement, TypeElement> entry : viewToDataMap.entrySet()) {
            viewElement = entry.getKey();
            dataElement = entry.getValue();

            eventHandler = ParameterizedTypeName.get(itemEventHandler, ClassName.get(dataElement));
            itemEventHandlerImpl = TypeSpec.anonymousClassBuilder("")
                    .addSuperinterface(eventHandler)
                    .addMethod(
                            MethodSpec.methodBuilder("sendEvent")
                                    .addModifiers(Modifier.PUBLIC)
                                    .addAnnotation(Override.class)
                                    .addParameter(
                                            ParameterSpec.builder(
                                                    ParameterizedTypeName.get(itemEvent, ClassName.get(dataElement)),
                                                    "event"
                                            )
                                                    .addAnnotation(nonNull)
                                                    .build()
                                    )
                                    .beginControlFlow("for (EventListener listener : mEventListeners)")
                                    .addStatement("listener.on" + viewElement.getSimpleName() + "Event(event)")
                                    .endControlFlow()
                                    .build()
                    ).build();

            Adapter.addField(
                    FieldSpec.builder(
                            ParameterizedTypeName.get(itemEventHandler, ClassName.get(dataElement)),
                            "m" + viewElement.getSimpleName() + "EventHandler"
                    )
                            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                            .initializer("$L", itemEventHandlerImpl)
                            .build()
            );
        }

        Adapter.addField(
                FieldSpec.builder(
                        ParameterizedTypeName.get(arrayList, adapter.nestedClass("EventListener")),
                        "mEventListeners"
                )
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                        .initializer("new $T<>()", arrayList)
                        .build()
        );

        Adapter.addMethod(
                MethodSpec.methodBuilder("addEventListener")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(
                                ParameterSpec.builder(
                                        adapter.nestedClass("EventListener"),
                                        "listener"
                                )
                                        .addAnnotation(nullable)
                                        .build()
                        )
                        .beginControlFlow("if (listener != null)")
                        .addStatement("mEventListeners.add(listener);")
                        .endControlFlow()
                        .build()
        );

        Adapter.addMethod(
                MethodSpec.methodBuilder("removeEventListener")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(
                                ParameterSpec.builder(
                                        adapter.nestedClass("EventListener"),
                                        "listener"
                                )
                                        .addAnnotation(nullable)
                                        .build()
                        )
                        .beginControlFlow("if (listener != null)")
                        .addStatement("mEventListeners.remove(listener);")
                        .endControlFlow()
                        .build()
        );


        /*
          Item view selectors
         */
        TypeSpec.Builder selectorImpl;
        for (Map.Entry<TypeElement, ItemViewInfo> entry : bindingMap.entrySet()) {
            viewElement = entry.getValue().getTypeElement();

            if (entry.getValue().isSelector()) {
                selectorElement = viewElement;
                itemViewIdInfos = findAllAvailableItemViewIdInfo(mSelectorMap, selectorElement);

                selectorImpl = TypeSpec.anonymousClassBuilder("")
                        .addSuperinterface(ClassName.get(selectorElement));


                for (ItemViewIdInfo info : itemViewIdInfos) {
                    viewElement = info.getTargetClassElement();

                    selectorImpl.addMethod(
                            MethodSpec.methodBuilder(info.getMethoElement().getSimpleName().toString())
                                    .addModifiers(Modifier.PUBLIC)
                                    .addAnnotation(Override.class)
                                    .returns(TypeName.INT)
                                    .addStatement("return Id.View.$N", viewElement.getSimpleName())
                                    .build()
                    );
                }

                Adapter.addField(
                        FieldSpec.builder(
                                ClassName.get(selectorElement),
                                "m" + selectorElement.getSimpleName()
                        )
                                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                                .initializer("$L", selectorImpl.build())
                                .build()
                );

                Adapter.addMethod(
                        MethodSpec.methodBuilder("get" + selectorElement.getSimpleName())
                                .addModifiers(Modifier.PUBLIC)
                                .returns(ClassName.get(selectorElement))
                                .addStatement("return m" + selectorElement.getSimpleName())
                                .build()
                );
            }
        }


        /*
          Data collection
         */
        final ClassName collection = ClassName.get(mItemsInfo.getCollectionType());
        Adapter.addField(
                FieldSpec.builder(collection, "mDataCollection")
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                        .initializer("new $T()", collection)
                        .build()
        );

        final ClassName itemData = ClassName.get(Constants.LIBRARY_PACKAGE + ".data", "ItemData");
        Adapter.addMethod(
                MethodSpec.methodBuilder("getDataCollection")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ParameterizedTypeName.get(arrayList, itemData).annotated(nonNull))
                        .addStatement("return mDataCollection.getCollection()")
                        .build()
        );


        /*
          Data wrapping methods
         */
        for (Map.Entry<TypeElement, ItemViewInfo> entry : bindingMap.entrySet()) {
            dataElement = entry.getKey();

            Adapter.addMethod(
                    MethodSpec.methodBuilder(dataElement.getSimpleName().toString())
                    .addModifiers(Modifier.PUBLIC)
                    .returns(ParameterizedTypeName.get(itemData, ClassName.get(dataElement))
                            .annotated(nonNull))
                    .addParameter(
                            ParameterSpec.builder(
                                    ClassName.get(dataElement),
                                    "data"
                            )
                                    .addAnnotation(nonNull)
                                    .build()
                    )
                    .addStatement("return new ItemData<>(data, Id.Data.$N)",
                            dataElement.getSimpleName().toString())
                    .build()
            );
        }


        /*
          Overrided method: getItemCount
         */
        Adapter.addMethod(
                MethodSpec.methodBuilder("getItemCount")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .returns(TypeName.INT)
                        .addStatement("return mDataCollection.getSize()")
                        .build()
        );


        /*
          Overrided method: getItemViewType
         */
        MethodSpec.Builder getItemViewType =
                MethodSpec.methodBuilder("getItemViewType")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .returns(TypeName.INT)
                        .addParameter(TypeName.INT, "position")
                        .addStatement("final ItemData data = mDataCollection.getData(position)")
                        .beginControlFlow("switch (data.getDataType())");

        for (Map.Entry<TypeElement, ItemViewInfo> entry : bindingMap.entrySet()) {
            dataElement = entry.getKey();
            viewElement = entry.getValue().getTypeElement();

            if (!entry.getValue().isSelector()) {
                getItemViewType.beginControlFlow("case Id.Data.$N:", dataElement.getSimpleName().toString())
                        .addStatement("return Id.View." + viewElement.getSimpleName())
                        .endControlFlow();

            } else {
                selectorElement = viewElement;
                getItemViewType.beginControlFlow("case Id.Data.$N:", dataElement.getSimpleName().toString())
                        .addStatement("return m$N.select(($T) data.getData())",
                                selectorElement.getSimpleName().toString(), ClassName.get(dataElement))
                        .endControlFlow();
            }
        }

        Adapter.addMethod(
                getItemViewType.endControlFlow()
                        .addStatement("throw new RuntimeException(\"Unregistered data type.\")")
                        .build()
        );


        /*
          Overrided method: onBindViewHolder
         */
        final ClassName recyclerViewItemView = ClassName.get(Constants.LIBRARY_PACKAGE + ".view", "RecyclerViewItemView");
        Adapter.addMethod(
                MethodSpec.methodBuilder("onBindViewHolder")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .addParameter(
                                ParameterSpec.builder(recyclerView.nestedClass("ViewHolder"), "holder")
                                        .addAnnotation(nonNull)
                                        .build()
                        )
                        .addParameter(TypeName.INT, "position")
                        .addStatement("final ItemData data = mDataCollection.getData(position)")
                        .addStatement(
                                "final RecyclerViewItemView view = (($T) holder).outter()",
                                recyclerViewItemView.nestedClass("InnerViewHolder"))
                        .addStatement("view._onBindData(data.getData())")
                        .build()
        );


        /*
          Overrided method: onCreateViewHolder
         */
        final ClassName viewGroup = ClassName.get("android.view", "ViewGroup");
        MethodSpec.Builder onCreateViewHolder =
                MethodSpec.methodBuilder("onCreateViewHolder")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .returns(recyclerView.nestedClass("ViewHolder"))
                        .addParameter(
                                ParameterSpec.builder(viewGroup, "parent")
                                        .addAnnotation(nonNull)
                                        .build()
                        )
                        .addParameter(TypeName.INT, "viewType")
                        .addStatement("final $T holder", recyclerViewItemView.nestedClass("InnerViewHolder"))
                        .beginControlFlow("switch (viewType)");

        for (Map.Entry<TypeElement, TypeElement> entry : viewToDataMap.entrySet()) {
            viewElement = entry.getKey();

            onCreateViewHolder.beginControlFlow("case Id.View.$N:", viewElement.getSimpleName().toString())
                    .addStatement(
                            "holder = new $T().onCreateViewHolder(this, " +
                                    "m" + viewElement.getSimpleName() + "EventHandler, parent)",
                            ClassName.get(viewElement))
                    .addStatement("break")
                    .endControlFlow();
        }

        final ClassName view = ClassName.get("android.view", "View");
        onCreateViewHolder.beginControlFlow("default:")
                .addStatement("throw new RuntimeException(\"Unsupported view type.\")")
                .endControlFlow()
                .endControlFlow()
                .addCode("holder.itemView.setOnClickListener(new $T.OnClickListener() {\n" +
                        "    @Override\n" +
                        "    public void onClick(View v) {\n" +
                        "        for (EventListener listener : mEventListeners) {\n" +
                        "            listener.onItemClick(holder.getAdapterPosition(), holder.outter().getData());\n" +
                        "        }\n" +
                        "    }\n" +
                        "});\n" +
                        "holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {\n" +
                        "    @Override\n" +
                        "    public boolean onLongClick(View v) {\n" +
                        "        boolean consumed = false;\n" +
                        "        for (EventListener listener : mEventListeners) {\n" +
                        "            consumed |= listener.onItemLongClick(holder.getAdapterPosition(), holder.outter().getData());\n" +
                        "        }\n" +
                        "        return consumed;\n" +
                        "    }\n" +
                        "});", view)
                .addStatement("return holder");

        Adapter.addMethod(onCreateViewHolder.build());


        return JavaFile.builder(packageName, Adapter.build())
                .indent("    ")
                .build();
    }

    private List<ItemViewIdInfo> findAllAvailableItemViewIdInfo(
            Map<TypeElement, List<ItemViewIdInfo>> selectorMap, TypeElement selectorElement) {

        final List<ItemViewIdInfo> rlt = new ArrayList<>();

        List<ItemViewIdInfo> infos;
        do {
            infos = selectorMap.get(selectorElement);
            if (infos != null) {
                rlt.addAll(infos);
            }

            if (selectorElement.getSuperclass() instanceof NoType) {
                break;
            }
            selectorElement = Environment.asTypeElement(selectorElement.getSuperclass());
        } while (true);

        return rlt;
    }
}
