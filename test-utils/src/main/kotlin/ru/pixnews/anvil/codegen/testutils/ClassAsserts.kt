/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.anvil.codegen.testutils

import assertk.Assert
import assertk.assertions.isNotNull

public inline fun <reified T : Annotation> Assert<Class<*>>.haveAnnotation(
    annotationClass: Class<T>,
) {
    transform { clazz -> clazz.getAnnotation(annotationClass) }.isNotNull()
}
