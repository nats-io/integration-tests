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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReadersTest {

    @Test
    public void testReadJsonNatsClaim() throws Exception {
        Payload claim = JsonReader.readFromFile("src\\test\\resources\\NatsClaim.json", Payload.class);
        assertNatsClaims(claim, true);
    }

    @Test
    public void testReadJwtNatsClaim() throws Exception {
        Payload claim = JwtReader.getNatsClaimsFromFile("src\\test\\resources\\NatsClaim.jwt");
        assertNatsClaims(claim, false);
    }

    private void assertNatsClaims(Payload claim, boolean extended) {
        assertEquals(1650041223, claim.getIat());
        assertEquals("ODSKBNDIT3LTZWFSRAWOBXSBZ7VZCDQVU6TBJX3TQGYXUWRU46ANJJS4", claim.getIss());
        assertEquals("IUJYSKQHYIHW5MLLDKZBVLGA3O27LHYMN5BMC72XEPADVH2V5BHA", claim.getJti());
        assertEquals("A", claim.getName());
        assertEquals("ACUCEFPEVLYC2B3KD6FBDMY2COVHAYLBU27YGQ7YH5PFKAONDN5SIFVN", claim.getSub());
        NatsClaims natsClaims = claim.getNats();
        List<Import> imports = natsClaims.getImports();
        assertEquals(3, imports.size());
        OperatorLimitsV2 limits = natsClaims.getLimits();
        assertEquals(10, limits.getConn());
        assertEquals(1000000000, limits.getData());
        assertEquals(-1, limits.getExports());
        assertEquals(-1, limits.getImports());
        assertEquals(1000, limits.getPayload());
        assertEquals(10, limits.getSubs());

        if (extended) {
            assertTrue(limits.areWildcardsAllowed());
            assertEquals(42, limits.getLeafNodeConn());
            assertEquals(101, limits.getMemoryStorage());
            assertEquals(102, limits.getDiskStorage());
            assertEquals(103, limits.getStreams());
            assertEquals(104, limits.getConsumer());
            assertEquals(105, limits.getMaxAckPending());
            assertEquals(106, limits.getMemoryMaxStreamBytes());
            assertEquals(107, limits.getDiskMaxStreamBytes());
            assertTrue(limits.isMaxBytesRequired());
        }
    }

    @Test
    public void testNscConfig() throws Exception {
        NscConfig nscConfig = JsonReader.readFromFile("src\\test\\resources\\NscConfig.json", NscConfig.class);
        assertEquals("C:\\Users\\test\\.local\\share\\nats\\nsc\\stores", nscConfig.getStoreRoot());
        assertEquals("TEST", nscConfig.getOperator());
        assertEquals("test_account", nscConfig.getAccount());
    }

    @Test
    public void testNscAccount() throws Exception {
        NscAccount nscAccount = JsonReader.readFromFile("src\\test\\resources\\NscAccount.json", NscAccount.class);
        assertEquals("desc", nscAccount.getDescription());
        assertEquals("earl", nscAccount.getUrl());
        assertEquals("token", nscAccount.getToken());
        assertEquals("user", nscAccount.getUser());
        assertEquals("pass", nscAccount.getPassword());
        assertEquals("creds", nscAccount.getCreds());
        assertEquals("nkey", nscAccount.getNkey());
        assertEquals("cert", nscAccount.getCert());
        assertEquals("key", nscAccount.getKey());
        assertEquals("ca", nscAccount.getCa());
        assertEquals("nsc", nscAccount.getNsc());
        assertEquals("jsdomain", nscAccount.getJetstreamDomain());
        assertEquals("jsapiprefix", nscAccount.getJetstreamApiPrefix());
        assertEquals("jseventprefix", nscAccount.getJetstreamEventPrefix());
        assertEquals("inboxprefix", nscAccount.getInboxPrefix());
    }
}