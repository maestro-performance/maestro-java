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
 *
 */

package org.maestro.client.exchange.support;

import java.util.Objects;

public class DefaultGroupInfo implements GroupInfo {
    private final String memberName;
    private final String groupName;

    public DefaultGroupInfo(final String memberName, final String groupName) {
        this.memberName = memberName;
        this.groupName = groupName;
    }

    public String memberName() {
        return memberName;
    }

    public String groupName() {
        return groupName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultGroupInfo that = (DefaultGroupInfo) o;
        return Objects.equals(memberName, that.memberName) &&
                Objects.equals(groupName, that.groupName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberName, groupName);
    }

    @Override
    public String toString() {
        return "DefaultGroupInfo{" +
                "memberName='" + memberName + '\'' +
                ", groupName='" + groupName + '\'' +
                '}';
    }
}
