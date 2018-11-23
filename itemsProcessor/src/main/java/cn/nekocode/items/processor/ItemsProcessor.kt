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
            if (annotatedElement.kind == ElementKind.INTERFACE) {
                printError("The @${Names.ADAPTER} " +
                        "should not annotates to interface: ${annotatedElement.simpleName}")
                return true
            }
            val adapterElement = annotatedElement as TypeElement
            val superElement = processingEnv.typeUtils
                .asElement(adapterElement.superclass) as TypeElement
            val itemAdapterElement = elements().getTypeElement(Names.ITEM_ADAPTER)

            if (superElement != itemAdapterElement) {
                printError("The adapter class should extends class ${itemAdapterElement.qualifiedName}: " +
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