/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2022 Objectionary.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.eolang.maven;

/**
 * Hash of tag.
 *
 * @todo #1174:90m It's much better to move CommitHash class and all
 * his implementations (including all connected classes) to a separate package.
 * The example of the package name: `org.eolang.maven.hash`
 * @since 0.28.11
 */
interface CommitHash {

    /**
     * SHA Hash.
     *
     * @return SHA of commit
     */
    String value();

    /**
     * Hardcoded commit hash.
     *
     * @since 0.28.11
     */
    final class ChConstant implements CommitHash {

        /**
         * Hardcoded value.
         */
        private final String hash;

        /**
         * The main constructor.
         *
         * @param hash Hardcoded value.
         */
        ChConstant(final String hash) {
            this.hash = hash;
        }

        @Override
        public String value() {
            return this.hash;
        }
    }
}