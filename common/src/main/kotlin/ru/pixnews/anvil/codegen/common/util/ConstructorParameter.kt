/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.anvil.codegen.common.util

import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.ParameterReference
import com.squareup.kotlinpoet.TypeName
import ru.pixnews.anvil.codegen.common.classname.AndroidClassName

public class ConstructorParameter(
    public val name: String,
    public val resolvedType: TypeName,
)

public fun ConstructorParameter.isSavedStateHandle(): Boolean = resolvedType == AndroidClassName.savedStateHandle

public fun List<ParameterReference>.parseConstructorParameters(
    implementingClass: ClassReference,
): List<ConstructorParameter> = this.map {
    ConstructorParameter(it.name, it.resolveTypeName(implementingClass))
}
