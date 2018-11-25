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

import cn.nekocode.items.processor.model.Delegate
import cn.nekocode.items.processor.model.Selector
import cn.nekocode.items.processor.util.Either
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
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
        return mutableSetOf(Names.ADAPTER)
    }

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        val annotationElement = elements().getTypeElement(Names.ADAPTER)
        processing@ for (annotatedElement in roundEnv.getElementsAnnotatedWith(annotationElement)) {
            val adapterElement = annotatedElement as TypeElement

            // Check if this element is not an interface
            if (adapterElement.isInterface()) {
                printError("The @${Names.ADAPTER} " +
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
            var overrideGetData = false
            var overrideGetItemCount = false
            for (element in adapterElement.enclosedElements) {
                if (element !is ExecutableElement) {
                    continue
                }

                if (!overrideGetData && element.simpleName.contentEquals(Names.GET_DATA)) {
                    overrideGetData = elements().overrides(
                        element, getDataElement, adapterElement)
                }
                if (!overrideGetItemCount && element.simpleName.contentEquals(Names.GET_ITEM_COUNT)) {
                    overrideGetItemCount = elements().overrides(
                        element, getItemCountElement, adapterElement)
                }
            }
            if (!overrideGetData) {
                printError("The adapter class should override method ${Names.GET_DATA}(): " +
                        "${adapterElement.qualifiedName}")
                continue
            }
            if (!overrideGetItemCount) {
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
            val delegateMethodElements = getOrPrintError(
                findDelegateMethods(adapterElement)
            ) ?: continue@processing

            // Find all delegate interfaces
            val delegates = getOrPrintError(
                findDelegates(adapterElement, delegateMethodElements)
            ) ?: continue@processing

            // Find all selector methods
            val selectors = getOrPrintError(
                findSelectors(adapterElement)
            ) ?: continue@processing

            // Create delegate-id map
            var id = 0
            val delegateIds = HashMap<Delegate, Int>()
            val views = HashSet<TypeElement>()
            for (delegate in delegates) {
                val view = delegate.view
                if (view !in views) {
                    delegateIds[delegate] = id ++
                    views.add(view)
                } else {
                    printError("There is a duplicate item view ${view.qualifiedName} in adapter: " +
                            "${adapterElement.qualifiedName}")
                    continue@processing
                }
            }

            // Create data-delegate map
            val dataDelegates = HashMap<TypeElement, Delegate?>()
            val duplicateData = HashSet<TypeElement>()
            for (delegate in delegates) {
                val dataElement = delegate.data
                if (dataElement !in dataDelegates) {
                    dataDelegates[dataElement] = delegate
                } else {
                    // If there is a duplicate data type, record it
                    dataDelegates[dataElement] = null
                    duplicateData.add(dataElement)
                }
            }

            // Create data-selector map
            val dataSelectors = HashMap<TypeElement, Selector>()
            for (selector in selectors) {
                val dataElement = selector.data
                if (dataElement !in dataSelectors) {
                    dataSelectors[dataElement] = selector
                } else {
                    printError("There is a selector method having duplicate data type in adapter: " +
                            "${adapterElement.qualifiedName}#${selector.method.simpleName}")
                    continue@processing
                }
            }

            // Check if all item views having duplicate data type have corresponding selectors
            for (data in duplicateData) {
                if (!dataSelectors.containsKey(data)) {
                    printError("Missing view selector for duplicate data type ${data.qualifiedName} in adapter:" +
                            "${adapterElement.qualifiedName}")
                    continue@processing
                }
            }
        }

        return true
    }

    /**
     * Find all methods which are annotated with @ViewDelegate
     */
    private fun findDelegateMethods(
        adapterElement: TypeElement
    ): Either<List<ExecutableElement>> {
        val viewDelegateElement = elements().getTypeElement(Names.VIEW_DELEGATE)
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
     * Find all delegate interfaces
     * @return list of delegate interfaces, list of item views, list of data, list of callbacks
     */
    private fun findDelegates(
        adapterElement: TypeElement,
        methodElements: List<ExecutableElement>
    ): Either<List<Delegate>> {
        val viewDelegateOfElement = elements().getTypeElement(Names.VIEW_DELEGATE_OF)
        val itemViewDelegateElement = elements().getTypeElement(Names.ITEM_VIEW_DELEGATE)
        val delegates = ArrayList<Delegate>()

        for (element in methodElements) {
            val delegateElement = element.returnType.asElement() as TypeElement

            // Check if the return type is interface
            if (!delegateElement.isInterface()) {
                return Either.Error("The delegate method should return an interface: " +
                        "${adapterElement.qualifiedName}#${element.simpleName}")
            }

            // Check if the delegate interface is annotated with @ViewDelegateOf
            val viewDelegateOfAnnotation = delegateElement.findAnnotation(viewDelegateOfElement)
                ?: return Either.Error(
                    "The delegate interface should be annotated with " +
                            "@${viewDelegateOfElement.qualifiedName}: " +
                            "${delegateElement.qualifiedName}"
                )

            // Check if the delegate interface is extending ItemViewDelegate
            val delegateSuperType = delegateElement.findExtendsType(itemViewDelegateElement)
                ?: return Either.Error(
                    "The delegate interface should extends interface " +
                            "${itemViewDelegateElement.qualifiedName}: " +
                            "${delegateElement.qualifiedName}"
                )

            // Extract types
            val viewElement = (viewDelegateOfAnnotation.getValue("value") as TypeMirror)
                .asElement() as TypeElement
            val itemViewGenericTypes = (viewElement.superclass as DeclaredType).typeArguments
            val dataElement = itemViewGenericTypes[0].asElement() as TypeElement
            val callbackElement = itemViewGenericTypes[1].asElement() as TypeElement

            // Check if callback types matches
            if (callbackElement != (delegateSuperType as DeclaredType).typeArguments[0].asElement()) {
                return Either.Error("Callback types of delegate interface and item view unmatched: " +
                        "${delegateElement.qualifiedName}")
            }

            delegates.add(
                Delegate(
                    element, delegateElement,
                    viewElement, dataElement, callbackElement
                )
            )
        }

        return Either.Success(delegates)
    }

    /**
     * Find all methods which are annotated with @ViewSelector
     * @return list of selector methods, list of data types
     */
    private fun findSelectors(
        adapterElement: TypeElement
    ): Either<List<Selector>> {
        val viewSelectorElement = elements().getTypeElement(Names.VIEW_SELECTOR)
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

    private fun TypeElement.findExtendsType(typeElement: TypeElement): TypeMirror? {
        if (!typeElement.isInterface()) {
            return if (this.superclass.asElement() == typeElement) {
                this.superclass
            } else {
                null
            }
        }

        for (`interface` in this.interfaces) {
            if (`interface`.asElement() == typeElement) {
                return `interface`
            }
        }
        return null
    }

    private fun Element.findAnnotation(annotationElement: TypeElement): AnnotationMirror? {
        for (annotation in this.annotationMirrors) {
            if (annotation.asElement() == annotationElement) {
                return annotation
            }
        }
        return null
    }

    private fun AnnotationMirror.getValue(name: String): Any? {
        for ((key, value) in this.elementValues) {
            if (key.simpleName.contentEquals(name)) {
                return value.value
            }
        }
        return null
    }

    private fun printError(msg: String) {
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, msg)
    }
}