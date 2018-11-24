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

import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
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
        for (annotatedElement in roundEnv.getElementsAnnotatedWith(annotationElement)) {
            val adapterElement = annotatedElement as TypeElement

            // Check if this element is not an interface
            if (adapterElement.kind == ElementKind.INTERFACE) {
                printError("The @${Names.ADAPTER} " +
                        "should not annotates to interface: ${adapterElement.qualifiedName}")
                return true
            }

            // Check if this element is abstract
            if (!adapterElement.isAbstract()) {
                printError("The adapter should be abstract: ${adapterElement.qualifiedName}")
                return true
            }

            val superElement = adapterElement.superclass.asElement() as TypeElement
            val itemAdapterElement = elements().getTypeElement(Names.ITEM_ADAPTER)

            // Check if the super class of this element is ItemAdapter
            if (superElement != itemAdapterElement) {
                printError("The adapter class should extends class ${itemAdapterElement.qualifiedName}: " +
                        "${adapterElement.qualifiedName}")
                return true
            }

            // Get the method element of getData()
            var foundElement: ExecutableElement? = null
            for (element in itemAdapterElement.enclosedElements) {
                if (element is ExecutableElement &&
                    element.simpleName.toString() == Names.GET_DATA) {
                    foundElement = element
                }
            }
            val getDataElement = foundElement!!

            // Get the getItemCount() method element
            foundElement = null
            for (element in elements().getTypeElement(Names.RECYCLER_VIEW_ADAPTER).enclosedElements) {
                if (element is ExecutableElement &&
                    element.simpleName.toString() == Names.GET_ITEM_COUNT) {
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

                if (!overrideGetData && element.simpleName.toString() == Names.GET_DATA) {
                    overrideGetData = elements().overrides(
                        element, getDataElement, adapterElement)
                }
                if (!overrideGetItemCount && element.simpleName.toString() == Names.GET_ITEM_COUNT) {
                    overrideGetItemCount = elements().overrides(
                        element, getItemCountElement, adapterElement)
                }
            }
            if (!overrideGetData) {
                printError("The adapter class should override method ${Names.GET_DATA}(): " +
                        "${adapterElement.qualifiedName}")
                return true
            }
            if (!overrideGetItemCount) {
                printError("The adapter class should override method ${Names.GET_ITEM_COUNT}(): " +
                        "${adapterElement.qualifiedName}")
                return true
            }

            // Find all delegate methods
            val delegateMethodElements = when (val either = findDelegateMethods(adapterElement)) {
                is Either.Success -> either.value
                is Either.Error -> {
                    val errorMsg = either.msg!!
                    printError(errorMsg)
                    return true
                }
            }

            findDelegates(delegateMethodElements)
        }

        return true
    }

    /**
     * Find all methods which are annotated with @ViewDelegate
     */
    private fun findDelegateMethods(adapterElement: TypeElement): Either<List<ExecutableElement>> {
        val viewDelegateElement = elements().getTypeElement(Names.VIEW_DELEGATE)
        val methodElements = ArrayList<ExecutableElement>()

        // Find delegate methods
        for (element in adapterElement.enclosedElements) {
            if (element !is ExecutableElement) {
                continue
            }

            for (annotation in element.annotationMirrors) {
                // Check if this element is annotated with @ViewDelegate
                if (annotation.asElement() == viewDelegateElement) {
                    methodElements.add(element)
                }
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
     */
    private fun findDelegates(methodElements: Iterable<ExecutableElement>): List<TypeElement> {
        val viewDelegateOfElement = elements().getTypeElement(Names.VIEW_DELEGATE_OF)
        val interfaceElements = ArrayList<TypeElement>()

        for (element in methodElements) {
            // todo
        }
        return interfaceElements
    }

    private fun elements() = processingEnv.elementUtils

    private fun TypeMirror.asElement() =
        processingEnv.typeUtils.asElement(this)

    private fun AnnotationMirror.asElement() =
        processingEnv.typeUtils.asElement(this.annotationType)

    private fun Element.isAbstract() =
        this.modifiers.contains(Modifier.ABSTRACT)

    private fun printError(msg: String) {
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, msg);
    }
}