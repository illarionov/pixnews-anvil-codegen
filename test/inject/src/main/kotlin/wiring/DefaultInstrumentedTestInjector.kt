/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.anvil.ksp.codegen.test.inject.wiring

public class DefaultInstrumentedTestInjector(
    private val providers: Map<Class<out Any>, SingleInstrumentedTestInjector>,
) : InstrumentedTestInjector {
    override fun inject(test: Any) {
        providers[test::class.java]?.also {
            it.injectMembers(test)
        } ?: error("No member injector for $test")
    }
}
