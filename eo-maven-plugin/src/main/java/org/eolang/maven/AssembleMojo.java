/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2021 Yegor Bugayenko
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

import com.jcabi.log.Logger;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.cactoos.Func;
import org.cactoos.Input;

/**
 * Pull all necessary EO XML files from Objectionary and parse them all.
 *
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@Mojo(
    name = "assemble",
    defaultPhase = LifecyclePhase.PROCESS_SOURCES,
    threadSafe = true
)
public final class AssembleMojo extends SafeMojo {

    /**
     * Tojo ATTR.
     */
    public static final String ATTR_EO = "eo";

    /**
     * Tojo ATTR.
     */
    public static final String ATTR_VERSION = "version";

    /**
     * Tojo ATTR.
     */
    public static final String ATTR_XMIR = "xmir";

    /**
     * Tojo ATTR.
     */
    public static final String ATTR_XMIR2 = "xmir2";

    /**
     * Tojo ATTR.
     */
    public static final String ATTR_JAR = "jar";

    /**
     * Tojo ATTR.
     */
    public static final String ATTR_DISCOVERED = "discovered";

    /**
     * Tojo ATTR.
     */
    public static final String ATTR_SCOPE = "scope";

    /**
     * Output.
     * @checkstyle MemberNameCheck (7 lines)
     */
    @Parameter(
        required = true,
        defaultValue = "${project.build.outputDirectory}"
    )
    private File outputDir;

    /**
     * The objectionary.
     */
    @SuppressWarnings("PMD.ImmutableField")
    private Func<String, Input> objectionary = new Objectionary();

    /**
     * The path to a text file where paths of all added
     * .class (and maybe others) files are placed.
     * @checkstyle MemberNameCheck (7 lines)
     * @since 0.11.0
     */
    @Parameter(
        required = true,
        defaultValue = "${project.build.directory}/eo-resolved.csv"
    )
    private File resolvedList;

    /**
     * Skip artifact with the version 0.0.0.
     * @checkstyle MemberNameCheck (7 lines)
     * @since 0.9.0
     */
    @Parameter(required = true, defaultValue = "true")
    private boolean skipZeroVersions;

    /**
     * Shall we discover JAR artifacts for .EO sources?
     * @checkstyle MemberNameCheck (7 lines)
     * @since 0.12.0
     */
    @Parameter(required = true, defaultValue = "false")
    private boolean discoverSelf;

    /**
     * Overwrite existing .class files?
     * @checkstyle MemberNameCheck (7 lines)
     * @since 0.10.0
     */
    @Parameter(required = true, defaultValue = "true")
    private Boolean overWrite;

    @Override
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    public void exec() throws IOException {
        String before = this.status();
        int cycle = 0;
        final Moja<?>[] mojas = {
            new Moja<>(ParseMojo.class)
                .with("targetDir", this.targetDir)
                .with("foreign", this.foreign),
            new Moja<>(OptimizeMojo.class)
                .with("foreign", this.foreign)
                .with("targetDir", this.targetDir),
            new Moja<>(DiscoverMojo.class)
                .with("foreign", this.foreign),
            new Moja<>(PullMojo.class)
                .with("targetDir", this.targetDir)
                .with("objectionary", this.objectionary)
                .with("foreign", this.foreign),
            new Moja<>(ResolveMojo.class)
                .with("outputDir", this.outputDir)
                .with("foreign", this.foreign)
                .with("project", this.project)
                .with("session", this.session)
                .with("manager", this.manager)
                .with("resolvedList", this.resolvedList)
                .with("skipZeroVersions", this.skipZeroVersions)
                .with("discoverSelf", this.discoverSelf)
                .with("overWrite", this.overWrite),
        };
        while (true) {
            for (final Moja<?> moja : mojas) {
                moja.execute();
            }
            final String after = this.status();
            ++cycle;
            Logger.info(
                this, "Assemble cycle #%d (%s -> %s)",
                cycle, before, after
            );
            if (after.equals(before)) {
                break;
            }
            before = after;
        }
        Logger.info(
            this, "%d assemble cycle(s) produced some new object(s): %s",
            cycle, before
        );
    }

    /**
     * Status of tojos.
     * @return Status in text
     * @throws IOException If fails
     */
    private String status() throws IOException {
        final String[] attrs = {
            AssembleMojo.ATTR_EO,
            AssembleMojo.ATTR_XMIR,
            AssembleMojo.ATTR_XMIR2,
            AssembleMojo.ATTR_DISCOVERED,
        };
        final Collection<String> parts = new LinkedList<>();
        for (final String attr : attrs) {
            parts.add(
                String.format(
                    "%s:%d",
                    attr,
                    this.tojos().select(tojo -> tojo.exists(attr)).size()
                )
            );
        }
        return String.join("/", parts);
    }

}
