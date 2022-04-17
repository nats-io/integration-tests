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

package io.nats.integration.client;

import io.nats.client.Nats;
import io.nats.integration.utils.JetStreamTestBase;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientVersionTest extends JetStreamTestBase {

    @Test
    public void testGetServerInfo() throws Exception {
        // THIS ASSUMES KNOWLEDGE OF THE build.gradle FILE FOR THIS PROJECT!
        String expected = new String(Files.readAllBytes(new File("build.gradle").toPath()), StandardCharsets.UTF_8);
        int at = expected.indexOf("testImplementation 'io.nats:jnats:");
        expected = expected.substring(at + 34);
        at = expected.indexOf("'");
        expected = expected.substring(0, at);
        at = expected.indexOf("-SNAPSHOT");
        if (at != -1) {
            expected = expected.substring(0, at);
        }
        assertEquals(expected, Nats.CLIENT_VERSION);
    }
}