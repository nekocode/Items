/*
 * Copyright 2018. nekocode (nekocode.cn@gmail.com)
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

package cn.nekocode.items.processor

import cn.nekocode.items.processor.model.Item
import cn.nekocode.items.processor.model.Selector
import cn.nekocode.items.processor.util.Either
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.NoType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic

/**
 * Debug: ./gradlew --no-daemon -Dorg.gradle.debug=true :exampleApp:clean :exampleApp:compileDebugJavaWithJavac
 * @author nekocode (nekocode.cn@gmail.com)
 */
class ItemsProcessor : AbstractProcessor() {

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Names.ADAPTER_ANNOTATION)
    }

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        val annotationElement = elements().getTypeElement(Names.ADAPTER_ANNOTATION)
        processing@ for (annotatedElement in roundEnv.getElementsAnnotatedWith(annotationElement)) {
            val adapterElement = annotatedElement as TypeElement

            // Check if this element is not an interface
            if (adapterElement.isInterface()) {
                printError("The @${Names.ADAPTER_ANNOTATION} " +
                        "should not annotates to interface: ${adapterElement.qualifiedName}")
                continue
            }

            // Check if this element is abstract
            if (!adapterElement.isAbstract()) {
                printError("The adapter should be abstract: ${adapterElement.qualifiedName}")
                continue
            }

            val itemAdapterElement = elements().getTypeElement(Names.ITEM_ADAPTER)

            // Check if this element is extending ItemAdapter
            if (adapterElement.findExtendsType(itemAdapterElement) == null) {
                printError("The adapter class should extends class ${itemAdapterElement.qualifiedName}: " +
                        "${adapterElement.qualifiedName}")
                continue
            }

            // Check constructor
            for (element in adapterElement.enclosedElements) {
                if (element is ExecutableElement &&
                    element.simpleName.contentEquals("<init>") &&
                    element.parameters.size > 0) {
                    printError("The adapter should not have constructor having parameters: " +
                            "${adapterElement.qualifiedName}")
                    continue@processing
                }
            }

            // Get the method element of getData()
            var foundElement: ExecutableElement? = null
            for (element in itemAdapterElement.enclosedElements) {
                if (element is ExecutableElement &&
                    element.simpleName.contentEquals(Names.GET_DATA)) {
                    foundElement = element
                }
            }
            val getDataElement = foundElement!!

            // Get the getItemCount() method element
            foundElement = null
            for (element in elements().getTypeElement(Names.RECYCLER_VIEW_ADAPTER).enclosedElements) {
                if (element is ExecutableElement &&
                    element.simpleName.contentEquals(Names.GET_ITEM_COUNT)) {
                    foundElement = element
                }
            }
            val getItemCountElement = foundElement!!

            // Check if this element override specified methods
            if (!adapterElement.hasOverrideMethod(getDataElement)) {
                printError("The adapter class should override method ${Names.GET_DATA}(): " +
                        "${adapterElement.qualifiedName}")
                continue
            }
            if (!adapterElement.hasOverrideMethod(getItemCountElement)) {
                printError("The adapter class should override method ${Names.GET_ITEM_COUNT}(): " +
                        "${adapterElement.qualifiedName}")
                continue
            }

            // To unwrap either
            fun <T> getOrPrintError(either: Either<T>): T? {
                return when (either) {
                    is Either.Success -> either.value
                    is Either.Error -> {
                        val errorMsg = either.msg!!
                        printError(errorMsg)
                        null
                    }
                }
            }

            // Find all delegate methods
            val itemMethodElements = getOrPrintError(
                findItemMethod(adapterElement)
            ) ?: continue@processing

            // Find all items
            val items = getOrPrintError(
                findItems(adapterElement, itemMethodElements)
            ) ?: continue@processing

            // Find all selector methods
            val selectorMethodElements = getOrPrintError(
                findSelectorMethods(adapterElement)
            ) ?: continue@processing

            // Create item-id map
            var id = 0
            val itemToIds = HashMap<Item, Int>()
            val itemSet = HashSet<TypeElement>()
            for (item in items) {
                val itemElement = item.item
                if (itemElement !in itemSet) {
                    itemToIds[item] = id ++
                    itemSet.add(itemElement)
                } else {
                    printError("There is a duplicate item ${itemElement.qualifiedName} in adapter: " +
                            "${adapterElement.qualifiedName}")
                    continue@processing
                }
            }

            // Create data-item map
            val dataToItems = HashMap<TypeElement, Item?>()
            val duplicateData = HashSet<TypeElement>()
            for (item in items) {
                val dataElement = item.data
                if (dataElement !in dataToItems) {
                    dataToItems[dataElement] = item
                } else {
                    // If there is a duplicate data type, record it
                    dataToItems[dataElement] = null
                    duplicateData.add(dataElement)
                }
            }

            // Create data-selector map
            val dataToSelectors = HashMap<TypeElement, Selector>()
            for (selector in selectorMethodElements) {
                val dataElement = selector.data
                if (dataElement !in dataToSelectors) {
                    dataToSelectors[dataElement] = selector
                } else {
                    printError("More than one selector method has one same data type in adapter: " +
                            "${adapterElement.qualifiedName}#${selector.method.simpleName}")
                    continue@processing
                }
            }

            // Check if all item views having duplicate data type have corresponding selectors
            for (data in duplicateData) {
                if (!dataToSelectors.containsKey(data)) {
                    printError("Missing view selector for duplicate data type ${data.qualifiedName} in adapter:" +
                            "${adapterElement.qualifiedName}")
                    continue@processing
                }
            }

            // Generate code
            AdapterGenerator(
                processingEnv,
                adapterElement,
                items,
                itemToIds,
                dataToItems,
                dataToSelectors
            ).generate()
        }

        return true
    }

    /**
     * Find all methods which are annotated with @ItemMethod
     */
    private fun findItemMethod(
        adapterElement: TypeElement
    ): Either<List<ExecutableElement>> {
        val viewDelegateElement = elements().getTypeElement(Names.ITEM_ANNOTATION)
        val methodElements = ArrayList<ExecutableElement>()

        // Find delegate methods
        for (element in adapterElement.enclosedElements) {
            if (element !is ExecutableElement) {
                continue
            }

            // Check if this element is annotated with @ViewDelegate
            if (element.findAnnotation(viewDelegateElement) != null) {
                methodElements.add(element)
            }
        }

        // Validate these methods
        for (element in methodElements) {
            if (!element.isAbstract()) {
                return Either.Error("The delegate method should be abstract: " +
                        "${adapterElement.qualifiedName}#${element.simpleName}")
            }

            if (element.parameters.size > 0) {
                return Either.Error("The delegate method should not have parameters: " +
                        "${adapterElement.qualifiedName}#${element.simpleName}")
            }
        }
        return Either.Success(methodElements)
    }

    /**
     * Find all items
     */
    private fun findItems(
        adapterElement: TypeElement,
        methodElements: List<ExecutableElement>
    ): Either<List<Item>> {
        val baseItemElement = elements().getTypeElement(Names.BASE_ITEM)
        val items = ArrayList<Item>()

        for (element in methodElements) {
            val returnElement = element.returnType.asElement() as TypeElement

            // Check if the item class is not abstract
            if (returnElement.isAbstract()) {
                return Either.Error("The item class should not be abstract: " +
                        "${adapterElement.qualifiedName}#${element.simpleName}")
            }

            // Check if the item class is extending BaseItem
            returnElement.findExtendsType(baseItemElement)
                ?: return Either.Error(
                    "The item class should extends " +
                            "${baseItemElement.qualifiedName}: " +
                            "${returnElement.qualifiedName}"
                )

            // Extract types
            val itemViewGenericTypes = (returnElement.superclass as DeclaredType).typeArguments
            val dataElement = itemViewGenericTypes[0].asElement() as TypeElement
            val holderElement = itemViewGenericTypes[1].asElement() as TypeElement
            val callbackElement = itemViewGenericTypes[2].asElement() as TypeElement

            items.add(
                Item(element, returnElement, dataElement, holderElement, callbackElement)
            )
        }

        return Either.Success(items)
    }

    /**
     * Find all methods which are annotated with @SelectorMethod
     * @return list of selector methods, list of data types
     */
    private fun findSelectorMethods(
        adapterElement: TypeElement
    ): Either<List<Selector>> {
        val viewSelectorElement = elements().getTypeElement(Names.SELECTOR_ANNOTATION)
        val methodElements = ArrayList<ExecutableElement>()
        val selectors = ArrayList<Selector>()

        // Find selector methods
        for (element in adapterElement.enclosedElements) {
            if (element !is ExecutableElement) {
                continue
            }

            // Check if this element is annotated with @ViewDelegate
            if (element.findAnnotation(viewSelectorElement) != null) {
                methodElements.add(element)
            }
        }

        // Validate these methods
        for (element in methodElements) {
            // Check if this method is implemented
            if (element.isAbstract()) {
                return Either.Error("The selector method should be implemented: " +
                        "${adapterElement.qualifiedName}#${element.simpleName}")
            }

            // Check parameters of this method
            val parameters = element.parameters
            if (parameters.size != 2 ||
                parameters[0].asType().kind != TypeKind.INT) {
                return Either.Error("Parameters of the selector method should be (int index, YourDataType data): " +
                        "${adapterElement.qualifiedName}#${element.simpleName}")
            }

            val dataElement = parameters[1].asType().asElement() as TypeElement
            selectors.add(Selector(element, dataElement))
        }

        return Either.Success(selectors)
    }

    private fun elements() = processingEnv.elementUtils

    private fun types() = processingEnv.typeUtils

    private fun TypeMirror.asElement() = types().asElement(this)

    private fun AnnotationMirror.asElement() =
        types().asElement(this.annotationType)

    private fun Element.isAbstract() =
        this.modifiers.contains(Modifier.ABSTRACT)

    private fun TypeElement.isInterface() =
        this.kind == ElementKind.INTERFACE

    private fun TypeElement.superClassElement(): TypeElement? {
        if (this.superclass is NoType) {
            return null
        }
        return this.superclass.asElement() as TypeElement
    }

    private fun TypeElement.findExtendsType(targetTypeElement: TypeElement): TypeMirror? {
        if (!targetTypeElement.isInterface()) {
            if (this.isInterface()) {
                // If source type is interface, return null
                return null
            }

            if (this.superClassElement() == targetTypeElement) {
                return this.superclass
            } else {
                // Find deeper level
                this.superClassElement()?.findExtendsType(targetTypeElement)?.run {
                    // If found, return it
                    return this
                }
            }

            return null

        } else {
            // Find first level extends
            for (_interface in this.interfaces) {
                if (_interface.asElement() == targetTypeElement) {
                    return _interface
                }
            }

            // Find deeper level
            this.superClassElement()?.findExtendsType(targetTypeElement)?.run {
                // If found, return it
                return this
            }
            for (_interface in this.interfaces) {
                (_interface.asElement() as TypeElement).findExtendsType(targetTypeElement)?.run {
                    // If found, return it
                    return this
                }
            }

            return null
        }
    }

    private fun Element.findAnnotation(annotationElement: TypeElement): AnnotationMirror? {
        for (annotation in this.annotationMirrors) {
            if (annotation.asElement() == annotationElement) {
                return annotation
            }
        }
        return null
    }

    private fun TypeElement.hasOverrideMethod(targetMethodElement: ExecutableElement): Boolean {
        for (element in this.enclosedElements) {
            if (element !is ExecutableElement) {
                continue
            }

            if (element.simpleName.contentEquals(targetMethodElement.simpleName)) {
                if (elements().overrides(element, targetMethodElement, this)) {
                    return true
                }
            }
        }
        this.superClassElement()?.hasOverrideMethod(targetMethodElement)?.run {
            // If found, return it
            return this
        }
        return false
    }

    private fun printError(msg: String) {
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, msg)
    }
}