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

import ngs.objects.NscAccount;
import ngs.objects.NscConfig;

import java.io.IOException;

public class NscUtils {

    // TODO CALCULATE THESE
    public static final String NATS_HOME;
    public static final String NSC_HOME;
    public static final String NSC_CONTEXT_HOME;

    public static NscConfig getNscConfig() throws IOException {
        return JsonReader.readFromFile(NSC_HOME + "nsc.json", NscConfig.class);
    }

    public static NscAccount getDefaultAccount(NscConfig nscConfig) throws IOException {
        String accountFile = NSC_CONTEXT_HOME + nscConfig.getDefaultAccountFileName();
        return JsonReader.readFromFile(accountFile, NscAccount.class);
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    public static void main(String[] args) {
        String s = System.getenv("NSC_HOME");
        System.out.println(s);
    }

    static {
        String natsHomeEnv = System.getenv("NATS_HOME");
        String nscHomeEnv = System.getenv("NSC_HOME");
        if (isWindows()) {
            if (natsHomeEnv == null) {
                NATS_HOME = System.getenv("HOMEDRIVE")
                    + System.getenv("HOMEPATH")
                    + "\\.config\\nats\\";
            }
            else {
                NATS_HOME = endWithBackSlash(natsHomeEnv);
            }

            if (nscHomeEnv == null) {
                NSC_HOME = NATS_HOME + "nsc\\";
            }
            else {
                NSC_HOME = endWithBackSlash(nscHomeEnv);
            }

            NSC_CONTEXT_HOME = NATS_HOME + "context\\";
        }
        else {
            if (natsHomeEnv == null) {
                NATS_HOME = System.getenv("HOME") + "/.config/nats/";
            }
            else {
                NATS_HOME = endWithSlash(natsHomeEnv);
            }

            if (nscHomeEnv == null) {
                NSC_HOME = NATS_HOME + "nsc/";
            }
            else {
                NSC_HOME = endWithSlash(nscHomeEnv);
            }

            NSC_CONTEXT_HOME = NATS_HOME + "context/";
        }
    }

    private static String endWithBackSlash(String s) {
        return s.endsWith("\\") ? s : s + "\\";
    }

    private static String endWithSlash(String s) {
        return s.endsWith("/") ? s : s + "/";
    }
}
