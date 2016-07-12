/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:JvmName("KClassifiers")
package kotlin.reflect

import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.types.KotlinTypeFactory
import org.jetbrains.kotlin.types.StarProjectionImpl
import org.jetbrains.kotlin.types.TypeProjectionImpl
import org.jetbrains.kotlin.types.Variance
import kotlin.reflect.jvm.internal.KClassImpl
import kotlin.reflect.jvm.internal.KTypeImpl
import kotlin.reflect.jvm.internal.KTypeParameterImpl

/**
 * TODO
 */
fun KClassifier.getType(arguments: List<KTypeProjection>, nullable: Boolean): KType {
    val descriptor = when (this) {
        is KClassImpl<*> -> descriptor
        is KTypeParameterImpl -> descriptor
        else -> throw KotlinReflectionInternalError("Cannot create type for an unsupported classifier: $this (${this.javaClass})")
    }

    val typeConstructor = descriptor.typeConstructor
    val kotlinType = KotlinTypeFactory.simpleType(Annotations.EMPTY, typeConstructor, arguments.mapIndexed { index, typeProjection ->
        if (typeProjection is KTypeProjection.Star) {
            StarProjectionImpl(typeConstructor.parameters[index])
        }
        else {
            val type = (typeProjection.type as KTypeImpl).type
            when (typeProjection) {
                is KTypeProjection.Invariant -> TypeProjectionImpl(Variance.INVARIANT, type)
                is KTypeProjection.In -> TypeProjectionImpl(Variance.IN_VARIANCE, type)
                is KTypeProjection.Out -> TypeProjectionImpl(Variance.OUT_VARIANCE, type)
                is KTypeProjection.Star -> error("Unreachable")
            }
        }
    }, nullable)

    return KTypeImpl(kotlinType) {
        when (this) {
            is KClassImpl<*> -> jClass
            is KTypeParameterImpl -> TODO("Java type is not yet supported for type variables: $this")
            else -> TODO("Java type is not yet supported for non-class classifiers: $this")
        }
    }
}
