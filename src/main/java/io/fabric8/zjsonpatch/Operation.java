/**
 * Copyright 2016 flipkart.com zjsonpatch.
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
package io.fabric8.zjsonpatch;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.fabric8.zjsonpatch.internal.guava.Preconditions.checkNotNull;

/**
 * User: gopi.vishwakarma
 * Date: 30/07/14
 */
enum Operation {
    ADD("add"),
    REMOVE("remove"),
    REPLACE("replace"),
    MOVE("move"),
    COPY("copy"),
    TEST("test");

    private final static Map<String, Operation> OPS = immutableMap(
            ADD.rfcName, ADD,
            REMOVE.rfcName, REMOVE,
            REPLACE.rfcName, REPLACE,
            MOVE.rfcName, MOVE,
            COPY.rfcName, COPY,
            TEST.rfcName, TEST
            );

    private String rfcName;

    Operation(String rfcName) {
        this.rfcName = rfcName;
    }

    public static Operation fromRfcName(String rfcName) {
        if (rfcName == null) throw new InvalidJsonPatchException("rfcName cannot be null");
        Operation op = OPS.get(rfcName.toLowerCase());
        if (op == null) throw new InvalidJsonPatchException("unknown / unsupported operation " + rfcName);
        return op;
    }

    public String rfcName() {
        return this.rfcName;
    }

    private static <K,V> Map<K,V> immutableMap(Object... keyValPair){
        Map<K,V> map = new HashMap<K,V>();

        if(keyValPair.length % 2 != 0){
            throw new IllegalArgumentException("Keys and values must be pairs.");
        }

        for(int i = 0; i < keyValPair.length; i += 2){
            map.put((K) keyValPair[i], (V) keyValPair[i+1]);
        }

        return Collections.unmodifiableMap(map);
    }


}
