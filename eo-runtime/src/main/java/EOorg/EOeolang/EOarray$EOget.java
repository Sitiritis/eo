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

package EOorg.EOeolang;

import org.eolang.phi.AtBound;
import org.eolang.phi.AtFree;
import org.eolang.phi.AtLambda;
import org.eolang.phi.Dataized;
import org.eolang.phi.PhDefault;
import org.eolang.phi.Phi;

/**
 * GET.
 *
 * @since 1.0
 */
public class EOarray$EOget extends PhDefault {

    public EOarray$EOget(final Phi parent) {
        super(parent);
        this.add("i", new AtFree());
        this.add("φ", new AtBound(new AtLambda(this, self -> {
            final Phi[] array = new Dataized(
                self.attr("ρ").get()
            ).take(Phi[].class);
            final int idx = (int) (long) new Dataized(
                self.attr("i").get()
            ).take(Long.class);
            if (array.length <= idx) {
                throw new IllegalArgumentException(
                    String.format(
                        "Can't get() the %dth element of the array, there are just %d of them",
                        idx, array.length
                    )
                );
            }
            return array[idx];
        })));
    }

}
