/*
 * Copyright 2018. nekocode (nekocode.cn@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.nekocode.items;

import java.util.HashMap;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public class Items {
    private static final HashMap<Class<? extends ItemAdapter>, Class<? extends ItemAdapter>>
            IMPL = new HashMap<>();

    public static <T extends ItemAdapter> T create(Class<T> adapterClass) {
        try {
            Class<? extends T> implClass = (Class<? extends T>) IMPL.get(adapterClass);
            if (implClass == null) {
                implClass = (Class<? extends T>) Class.forName(adapterClass.getName() + "_Impl");
                IMPL.put(adapterClass, implClass);
            }
            return implClass.getConstructor().newInstance();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}