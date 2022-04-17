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

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ngs.objects.NatsClaims;
import ngs.objects.Payload;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class JwtReader {
    public static Payload getNatsClaims(String jwtString) throws IOException {
        DecodedJWT jwt = JWT.decode(jwtString);
        Map<String, com.auth0.jwt.interfaces.Claim> claims = jwt.getClaims();
        Payload nc = new Payload();
        nc.setIat(claims.get("iat").asLong());
        nc.setIss(claims.get("iss").asString());
        nc.setJti(claims.get("jti").asString());
        nc.setName(claims.get("name").asString());
        nc.setSub(claims.get("sub").asString());
        Claim natsClaim = claims.get("nats");
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        nc.setNats(mapper.readValue(natsClaim.toString(), NatsClaims.class));
        return nc;
    }

    public static Payload getNatsClaims(byte[] jwtBytes) throws IOException {
        return getNatsClaims(new String(jwtBytes, StandardCharsets.UTF_8));
    }

    public static Payload getNatsClaims(Path path) throws IOException {
        return getNatsClaims(Files.readAllBytes(path));
    }

    public static Payload getNatsClaims(File file) throws IOException {
        return getNatsClaims(file.toPath());
    }

    public static Payload getNatsClaimsFromFile(String filename) throws IOException {
        return getNatsClaims(new File(filename));
    }
}
