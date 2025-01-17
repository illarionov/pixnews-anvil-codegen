/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.anvil.ksp.codegen.common.util

import com.squareup.anvil.compiler.internal.reference.AnvilCompilationExceptionClassReference
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.allSuperTypeClassReferences
import org.jetbrains.kotlin.name.FqName
import ru.pixnews.anvil.ksp.codegen.common.InternalPixnewsAnvilCodegenApi

// TODO: remove
@InternalPixnewsAnvilCodegenApi
public fun ClassReference.checkClassExtendsType(type: FqName) {
    if (allSuperTypeClassReferences().none { it.fqName == type }) {
        throw AnvilCompilationExceptionClassReference(
            message = "${this.fqName} doesn't extend $type",
            classReference = this,
        )
    }
}
