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

public class Import {
    private String name;
    private String subject;
    private String account;
    private String token;
    private String to;
    private String localSubject;
    private String type;
    private boolean share;

    public String getName() {
        return name;
    }

    public String getSubject() {
        return subject;
    }

    public String getAccount() {
        return account;
    }

    public String getToken() {
        return token;
    }

    public String getTo() {
        return to;
    }

    public String getLocalSubject() {
        return localSubject;
    }

    public String getType() {
        return type;
    }

    public boolean isShare() {
        return share;
    }
}
