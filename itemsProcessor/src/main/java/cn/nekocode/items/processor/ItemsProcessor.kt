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

import cn.nekocode.items.ItemAdapter
import cn.nekocode.items.annotation.Adapter
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
        return mutableSetOf(
            Adapter::class.java.canonicalName
        )
    }

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        for (element in roundEnv.getElementsAnnotatedWith(Adapter::class.java)) {
            if (element.kind == ElementKind.INTERFACE) {
                printError("The @${Adapter::class.java.simpleName} " +
                        "should not annotates to interface class: ${element.simpleName}")
                return true
            }
            val typeElement = element as TypeElement
            val superElement = processingEnv.typeUtils
                .asElement(typeElement.superclass) as TypeElement

            if (superElement.qualifiedName.toString() != ItemAdapter::class.java.canonicalName) {
                printError("The adapter class should extends ${ItemAdapter::class.java.simpleName}: " +
                        "${typeElement.qualifiedName}")
                return true
            }
        }

        return true
    }

    private fun printError(msg: String) {
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, msg);
    }
}