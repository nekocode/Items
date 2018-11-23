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
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
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
            // Check if this element is not an interface
            if (annotatedElement.kind == ElementKind.INTERFACE) {
                printError("The @${Names.ADAPTER} " +
                        "should not annotates to interface: ${annotatedElement.simpleName}")
                return true
            }

            val adapterElement = annotatedElement as TypeElement

            // Check if this element is abstract
            if (!adapterElement.modifiers.contains(Modifier.ABSTRACT)) {
                printError("The @${Names.ADAPTER} " +
                        "should be abstract: ${annotatedElement.simpleName}")
                return true
            }

            val superElement = processingEnv.typeUtils
                .asElement(adapterElement.superclass) as TypeElement
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
        }

        return true
    }

    private fun printError(msg: String) {
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, msg);
    }

    private fun elements() = processingEnv.elementUtils
}