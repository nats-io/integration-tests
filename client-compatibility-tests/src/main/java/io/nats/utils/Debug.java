// Copyright 2020 The NATS Authors
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

package io.nats.utils;

@SuppressWarnings("SameParameterValue")
public abstract class Debug {
    private static final String INDENT = "    ";
    private static final String NEWLINE_INDENT = "\n" + INDENT;
    private static final boolean PRINT_THREAD_ID = true;

    private Debug() {}  /* ensures cannot be constructed */

    public static String toString(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof byte[]) {
            return new String((byte[])o);
        }
        return o.toString();
    }

    public static void dbg(String label) {
        debug(label, null, false);
    }

    public static void err(String label) {
        debug(label, null, true);
    }

    public static void dbg(String label, Object... extras) {
        debug(label, false, extras);
    }

    public static void err(String label, Object... extras) {
        debug(label, true, extras);
    }

    public static void err(String label, Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.getMessage());
        StackTraceElement[] extras = e.getStackTrace();
        for (int x = 0; x < extras.length; x++) {
            if (x > 0) {
                sb.append("\n");
            }
            sb.append(INDENT).append(toString(extras[x]));
        }
        debug(label, sb.toString(), true);
    }

    private static void debug(String label, boolean error, Object... extras) {
        if (extras.length == 1) {
            debug(label, toString(extras[0]), error);
        }
        else {
            StringBuilder sb = new StringBuilder();
            for (int x = 0; x < extras.length; x++) {
                if (x > 0) {
                    sb.append("\n");
                }
                sb.append(toString(extras[x]));
            }
            debug(label, sb.toString(), error);
        }
    }

    private static void debug(String label, String extraStr, boolean error) {
        String start;
        if (PRINT_THREAD_ID) {
            String tn = Thread.currentThread().getName().replace("pool-", "p").replace("-thread-", "t");
            start = "[" + tn + "@" + time() + "] " + label;
        }
        else {
            start = "[" + time() + "] " + label;
        }

        if (extraStr == null || extraStr.isEmpty()) {
            if (error) {
                System.err.println(start);
            }
            else {
                System.out.println(start);
            }
            return;
        }

        extraStr = NEWLINE_INDENT + extraStr.replaceAll("\n", NEWLINE_INDENT);
        if (error) {
            System.err.println(start + extraStr);
        }
        else {
            System.out.println(start + extraStr);
        }
    }

    private static String time() {
        String t = "" + System.currentTimeMillis();
        return t.substring(t.length() - 9);
    }
}
