/*
 * Copyright 2018 Otavio R. Piske <angusyoung@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.maestro.common.test;

import org.maestro.common.content.MessageSize;

import java.util.Properties;

abstract class CommonProperties implements MaestroTestProperties {
    private int parallelCount;
    private long messageSize;
    private boolean variableSize;
    private int rate;

    public final void setMessageSize(long messageSize) {
        this.messageSize = messageSize;
    }

    public final void setMessageSize(final String messageSize) {
        if (MessageSize.isVariable(messageSize)) {
            setVariableSize(true);
        }

        this.messageSize = MessageSize.toSizeFromSpec(messageSize);
    }

    @Override
    public final long getMessageSize() {
        return messageSize;
    }

    public final void setParallelCount(int parallelCount) {
        this.parallelCount = parallelCount;
    }

    public final void setParallelCount(String parallelCount) {
        setParallelCount(Integer.parseInt(parallelCount));
    }

    @Override
    public final int getParallelCount() {
        return parallelCount;
    }

    public final void setVariableSize(boolean variableSize) {
        this.variableSize = variableSize;
    }

    @Override
    public final boolean isVariableSize() {
        return variableSize;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public void setRate(final String rate) {
        this.rate = Integer.parseInt(rate);
    }

    @Override
    public final int getRate() {
        return rate;
    }

    protected void write(final Properties prop) {
        prop.setProperty("parallelCount", Integer.toString(getParallelCount()));
        prop.setProperty("messageSize", Long.toString(getMessageSize()));
        prop.setProperty("variableSize", isVariableSize() ? "1" : "0");
        prop.setProperty("rate", Integer.toString(getRate()));
    }

    protected void load(final Properties prop) {
        setParallelCount(prop.getProperty("parallelCount"));
        setMessageSize(prop.getProperty("messageSize"));

        // Optional
        String varSizeStr = prop.getProperty("variableSize");

        if (varSizeStr != null && varSizeStr.equals("1")) {
            setVariableSize(true);
        }

        setRate(prop.getProperty("rate"));
    }

    @Override
    public String toString() {
        return "CommonProperties{" +
                "parallelCount=" + parallelCount +
                ", messageSize=" + messageSize +
                ", variableSize=" + variableSize +
                ", rate=" + rate +
                '}';
    }
}
