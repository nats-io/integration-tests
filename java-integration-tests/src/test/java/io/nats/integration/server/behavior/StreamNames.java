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

package io.nats.integration.server.behavior;

import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.NUID;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;
import io.nats.integration.utils.JetStreamTestBase;
import io.nats.integration.utils.JetStreamTestHelper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

class StreamNames extends JetStreamTestBase {

    @Test
    public void testStreamNames() throws Exception {
        runInJsServer(nc -> {
            JetStreamTestHelper h = new JetStreamTestHelper(nc);
            createTestMemoryStream(h);

            _testStreamName(h.jsm, "safe");
            assertValid(h.jsm, "ampersand&");
            assertValid(h.jsm, "pound#");
            assertValid(h.jsm, "percent%");
            assertValid(h.jsm, "leftcurly{");
            assertValid(h.jsm, "rightcurly}");
            assertValid(h.jsm, "dollar$");
            assertValid(h.jsm, "exclamation!");
            assertValid(h.jsm, "singlequote'");
            assertValid(h.jsm, "at@");
            assertValid(h.jsm, "plus+");
            assertValid(h.jsm, "backtick`");
            assertValid(h.jsm, "equal=");
            assertValid(h.jsm, "carrot^");
            assertValid(h.jsm, "leftp(");
            assertValid(h.jsm, "rightp)");
            assertValid(h.jsm, "leftb[");
            assertValid(h.jsm, "rightb]");
            assertValid(h.jsm, "semi;");
            assertValid(h.jsm, "comma,");

            assertIllegalArgumentException(h.jsm, "dot.");
            assertIllegalArgumentException(h.jsm, "star*");
            assertIllegalArgumentException(h.jsm, "greaterthan>");
            assertIllegalArgumentException(h.jsm, "fwdslash/");
            assertIllegalArgumentException(h.jsm, "backslash\\");

            assertJetStreamApiException(h.jsm, "colon:");
            assertJetStreamApiException(h.jsm, "doublequote\"");
            assertJetStreamApiException(h.jsm, "pipe|");
            assertJetStreamApiException(h.jsm, "question?");
            assertJetStreamApiException(h.jsm, "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");

            if (System.getProperty("os.name").startsWith("Windows")) {
                assertJetStreamApiException(h.jsm, "lessthan<");
            }
            else {
                assertValid(h.jsm, "lessthan<");
            }
        });
    }

    private void assertIllegalArgumentException(JetStreamManagement jsm, String stream) {
        assertThrows(IllegalArgumentException.class, () -> _testStreamName(jsm, stream));
    }

    private void assertJetStreamApiException(JetStreamManagement jsm, String stream) {
        assertThrows(JetStreamApiException.class, () -> _testStreamName(jsm, stream));
    }

    private void assertValid(JetStreamManagement jsm, String stream) {
        try {
            _testStreamName(jsm, stream);
            jsm.deleteStream(stream);
        } catch (Exception e) {
            fail(e);
        }
    }

    private void _testStreamName(JetStreamManagement jsm, String stream) throws IOException, JetStreamApiException {
        System.out.println(stream);
        try {
            jsm.deleteStream(stream);
        } catch (Exception e) {
            // ignore
        }

        StreamConfiguration sc = StreamConfiguration.builder()
            .name(stream)
            .storageType(StorageType.File)
            .subjects(NUID.nextGlobal())
            .build();
        jsm.addStream(sc);
    }
}