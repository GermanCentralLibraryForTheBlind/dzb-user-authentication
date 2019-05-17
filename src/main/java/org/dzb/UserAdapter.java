/*
 * Copyright 2019 DZB Leipzig
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

package org.dzb;

import org.jboss.logging.Logger;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:lars.voigt@dzb.de">Lars Voigt</a>
 * @version $Revision: 1 $
 */
public class UserAdapter extends AbstractUserAdapterFederatedStorage {
    private static final Logger logger = Logger.getLogger(UserAdapter.class);
    protected DzbUserEntity entity;
    protected String keycloakId;

    public UserAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, DzbUserEntity entity) {
        super(session, realm, model);
        this.entity = entity;
        keycloakId = StorageId.keycloakId(model, String.valueOf(entity.getId()));
    }

    public String getPassword() {
        return entity.getPassword();
    }

    @Override
    public String getUsername() {
        return entity.getUsername();
    }

    @Override
    public void setUsername(String username) {
    }

    @Override
    public String getEmail() {
        return entity.getEmail();
    }

    @Override
    public String getId() {
        return keycloakId;
    }


    @Override
    public String getFirstAttribute(String name) {
        if (name.equals(AbstractUserAdapterFederatedStorage.LAST_NAME_ATTRIBUTE)) {
            return entity.getLastName();
        } else if (name.equals(AbstractUserAdapterFederatedStorage.FIRST_NAME_ATTRIBUTE)) {
            return entity.getFirstName();
        } else {
            return super.getFirstAttribute(name);
        }
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        Map<String, List<String>> attrs = super.getAttributes();
        MultivaluedHashMap<String, String> all = new MultivaluedHashMap<>();
        all.putAll(attrs);
        all.add(AbstractUserAdapterFederatedStorage.LAST_NAME_ATTRIBUTE, entity.getLastName());
        all.add(AbstractUserAdapterFederatedStorage.FIRST_NAME_ATTRIBUTE, entity.getFirstName());
        return all;
    }

    @Override
    public List<String> getAttribute(String name) {
        List<String> attr = new LinkedList<>();

        if (name.equals(AbstractUserAdapterFederatedStorage.LAST_NAME_ATTRIBUTE)) {
            attr.add(entity.getLastName());
            return attr;
        } else if (name.equals(AbstractUserAdapterFederatedStorage.FIRST_NAME_ATTRIBUTE)) {
            attr.add(entity.getFirstName());
            return attr;
        } else {
            return super.getAttribute(name);
        }
    }
}
