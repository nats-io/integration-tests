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

package ngs.objects;

import com.fasterxml.jackson.annotation.JsonAlias;

public class NscAccount {
    private String  description;
    private String  url;
    private String  token;
    private String  user;
    private String  password;
    private String  creds;
    private String  nkey;
    private String  cert;
    private String  key;
    private String  ca;
    private String  nsc;

    @JsonAlias({ "jetstream_domain" })
    private String  jetstreamDomain;

    @JsonAlias({ "jetstream_api_prefix" })
    private String  jetstreamApiPrefix;

    @JsonAlias({ "jetstream_event_prefix" })
    private String  jetstreamEventPrefix;

    @JsonAlias({ "inbox_prefix" })
    private String  inboxPrefix;

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public String getToken() {
        return token;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getCreds() {
        return creds;
    }

    public String getNkey() {
        return nkey;
    }

    public String getCert() {
        return cert;
    }

    public String getKey() {
        return key;
    }

    public String getCa() {
        return ca;
    }

    public String getNsc() {
        return nsc;
    }

    public String getJetstreamDomain() {
        return jetstreamDomain;
    }

    public String getJetstreamApiPrefix() {
        return jetstreamApiPrefix;
    }

    public String getJetstreamEventPrefix() {
        return jetstreamEventPrefix;
    }

    public String getInboxPrefix() {
        return inboxPrefix;
    }
}
