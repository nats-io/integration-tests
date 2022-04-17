// Copyright 2022 The NATS Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package ngs.utils;

import ngs.objects.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LimitsTest {

    // TODO CALCULATE THESE
    static String NSC_HOME = "C:\\Users\\batman\\.config\\nats\\nsc\\";
    static String NSC_CONTEXT_HOME = "C:\\Users\\batman\\.config\\nats\\context\\";

    static NscConfig getNscConfig() throws IOException {
        return JsonReader.readFromFile(NSC_HOME + "nsc.json", NscConfig.class);
    }

    static NscAccount getDefaultAccount(NscConfig nscConfig) throws IOException {
        String accountFile = NSC_CONTEXT_HOME + nscConfig.getDefaultAccountFileName();
        return JsonReader.readFromFile(accountFile, NscAccount.class);
    }

    @Test
    public void testLimits() throws Exception {
        NscConfig nscConfig = getNscConfig();
        NscAccount nscAccount = getDefaultAccount(nscConfig);
    }
}