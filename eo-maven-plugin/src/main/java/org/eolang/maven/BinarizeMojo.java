/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2023 Objectionary.com
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

import com.google.common.io.CharStreams;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.yegor256.tojos.Tojo;
import com.yegor256.xsline.Shift;
import com.yegor256.xsline.TrBulk;
import com.yegor256.xsline.TrClasspath;
import com.yegor256.xsline.Train;
import com.yegor256.xsline.Xsline;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.eolang.maven.rust_project.BuildFailureException;
import org.eolang.maven.rust_project.Project;
import org.eolang.maven.util.Home;
import org.eolang.parser.ParsingTrain;

/**
 * Compile binaries.
 *
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @since 0.1
 */
@Mojo(
    name = "binarize",
    defaultPhase = LifecyclePhase.PROCESS_SOURCES,
    threadSafe = true,
    requiresDependencyResolution = ResolutionScope.COMPILE
)
@SuppressWarnings("PMD.LongVariable")
public final class BinarizeMojo extends SafeMojo {

    /**
     * The directory where to binarize to.
     */
    public static final Path DIR = Paths.get("binarize");

    /**
     * The directory with generated .rs files.
     */
    public static final Path CODES = Paths.get("codes");

    /**
     * Parsing train with XSLs.
     */
    static final Train<Shift> TRAIN = new TrBulk<>(
        new TrClasspath<>(
            new ParsingTrain()
                .empty()
        ),
        Arrays.asList(
            "/org/eolang/maven/add_rust/add_rust.xsl"
        )
    ).back().back();

    /**
     * Target directory.
     * @checkstyle MemberNameCheck (7 lines)
     */
    @Parameter(
        required = true,
        defaultValue = "${project.build.directory}/eo-binaries"
    )
    @SuppressWarnings("PMD.UnusedPrivateField")
    private File generatedDir;

    @Override
    public void exec() throws IOException {
        new Moja<>(BinarizeParseMojo.class).copy(this).execute();

        final Path dest = targetDir.toPath().resolve("Lib");
        final ProcessBuilder builder = new ProcessBuilder("cargo", "build")
            .directory(dest.toFile());
        Logger.info(this, "Building rust project..");
        final Process building = builder.start();
        try {
            building.waitFor();
        } catch (final InterruptedException exception) {
            throw new BuildFailureException(
                String.format(
                    "Interrupted while building %s",
                    dest.toAbsolutePath()
                ),
                exception
            );
        }
        if (building.exitValue() != 0) {
            Logger.error(this, "There was an error in compilation");
            Logger.error(
                this,
                CharStreams.toString(
                    new InputStreamReader(
                        building.getErrorStream(),
                        Charsets.UTF_8
                    )
                )
            );
            throw new BuildFailureException(
                String.format(
                    "Failed to build cargo project with dest = %s",
                    dest.toAbsolutePath()
                )
            );
        }
    }

    /**
     * Creates a "rust" section in xml file and returns the resulting XML.
     * @param input The .xmir file
     * @return The content of rust section
     * @throws IOException If any issues with I/O
     */
    private XML addRust(
        final XML input
    ) {
        final String name = input.xpath("/program/@name").get(0);
        final Place place = new Place(name);
        final Train<Shift> trn = new SpyTrain(
            BinarizeMojo.TRAIN,
            place.make(this.targetDir.toPath().resolve(BinarizeMojo.DIR), "")
        );
        return new Xsline(trn).pass(input);
    }

    /**
     * Makes a text from Hexed text.
     * @param txt Hexed chars separated by backspace.
     * @return Normal text.
     */
    private static String unhex(final String txt) {
        final StringBuilder hex = new StringBuilder(txt.length());
        for (final char chr : txt.toCharArray()) {
            if (chr == ' ') {
                continue;
            }
            hex.append(chr);
        }
        final String result;
        try {
            final byte[] bytes = Hex.decodeHex(String.valueOf(hex).toCharArray());
            result = new String(bytes, "UTF-8");
        } catch (final DecoderException | UnsupportedEncodingException exception) {
            throw new IllegalArgumentException(
                String.format("Invalid String %s, cannot unhex", txt),
                exception
            );
        }
        return result;
    }

    /**
     * Uniquely converts the loc into the name for jni function.
     * @param loc Location attribute of the rust insert.
     * @return Name for function.
     */
    private static String name(final String loc) {
        final StringBuilder out = new StringBuilder(1 + 5 * loc.length());
        out.append('f').append(loc.toLowerCase(Locale.ENGLISH).replaceAll("[^a-z0-9]", "o"));
        for (final char chr: loc.toCharArray()) {
            out.append(
                String.format(
                    "%04x",
                    (int) chr
                )
            );
        }
        return out.toString();
    }

}
