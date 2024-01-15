/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.anvil.codegen.common.util

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import ru.pixnews.anvil.codegen.common.InternalPixnewsAnvilCodegenApi
import ru.pixnews.anvil.codegen.common.classname.AnvilClassName

/**
 * `@ContributesTo(className::class, replaces = [..])`
 */
@InternalPixnewsAnvilCodegenApi
public fun contributesToAnnotation(
    className: ClassName,
    replaces: List<TypeName> = emptyList(),
): AnnotationSpec {
    return with(AnnotationSpec.builder(AnvilClassName.contributesTo)) {
        addMember("%T::class", className)
        if (replaces.isNotEmpty()) {
            @Suppress("SpreadOperator")
            addMember(
                "replaces = [${replaces.joinToString(",") { "%T::class" }}]",
                *replaces.toTypedArray(),
            )
        }
        build()
    }
}

/**
 * `@ContributesTo(className::class)`
 */
@InternalPixnewsAnvilCodegenApi
public fun contributesMultibindingAnnotation(scope: ClassName): AnnotationSpec {
    return AnnotationSpec.builder(AnvilClassName.contributesMultibinding)
        .addMember("scope = %T::class", scope)
        .build()
}
