/*
 *  Copyright 2017 Otavio R. Piske <angusyoung@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.maestro.common.duration;

import org.maestro.common.exceptions.DurationParseException;

import java.time.Duration;

public class DurationUtils {
    static final DurationCount DEFAULT_WARM_UP_DURATION = new DurationCount(DurationCount.WARM_UP_COUNT);

    private DurationUtils() {}

    public static long parse(CharSequence sequence) throws DurationParseException {
        Duration d = Duration.ZERO;
        int last = 0;

        for (int i = 0; i < sequence.length(); i++) {
            switch (sequence.charAt(i)) {
                case 's': {
                    CharSequence tmp = sequence.subSequence(last, i);
                    if (tmp == null) {
                        throw new DurationParseException("Unable to parse the sequence " + sequence);
                    }


                    int number = Integer.parseInt(tmp.toString());
                    Duration tmpSeconds = Duration.ofSeconds(number);

                    d = d.plus(tmpSeconds);
                    last = i + 1;

                    break;
                }
                case 'm': {
                    CharSequence tmp = sequence.subSequence(last, i);
                    if (tmp == null) {
                        throw new DurationParseException("Unable to parse the sequence " + sequence);
                    }


                    int number = Integer.parseInt(tmp.toString());
                    d = d.plusMinutes(number);
                    last = i + 1;

                    break;
                }
                case 'h': {
                    CharSequence tmp = sequence.subSequence(last, i);
                    if (tmp == null) {
                        throw new DurationParseException("Unable to parse the sequence " + sequence);
                    }


                    int number = Integer.parseInt(tmp.toString());
                    d = d.plusHours(number);
                    last = i + 1;

                    break;
                }
                case 'd': {
                    CharSequence tmp = sequence.subSequence(last, i);
                    if (tmp == null) {
                        throw new DurationParseException("Unable to parse the sequence " + sequence);
                    }


                    int number = Integer.parseInt(tmp.toString());
                    d = d.plusDays(number);
                    last = i + 1;

                    break;
                }
                default: {
                    if (last == 0 && i == (sequence.length() - 1)) {
                        int number = Integer.parseInt(sequence.toString());

                        d = d.plusSeconds(number);
                        last = i + 1;
                    }
                }
            }
        }

        return d.getSeconds();
    }
}
