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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:lars.voigt@dzb.de">Lars Voigt</a>
 * @version $Revision: 1 $
 */
public class UserAdapter extends AbstractUserAdapterFederatedStorage {
    private static final Logger logger = Logger.getLogger(UserAdapter.class);
    protected DzbUser user;
    protected String keycloakId;

    public UserAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, DzbUser user) {
        super(session, realm, model);

//        logger.info("session: " + realm.getDisplayName());
        this.user = user;
        keycloakId = StorageId.keycloakId(model, String.valueOf(user.getId()));
    }

    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public void setUsername(String username) {
    }

    @Override
    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public String getId() {
        return keycloakId;
    }


    @Override
    public String getFirstAttribute(String name) {

        logger.info("getFirstAttribute: " + name);

        if (name.equals(AbstractUserAdapterFederatedStorage.LAST_NAME_ATTRIBUTE)) {
            return user.getLastName();
        } else if (name.equals(AbstractUserAdapterFederatedStorage.FIRST_NAME_ATTRIBUTE)) {
            return user.getFirstName();
        } else {
            return super.getFirstAttribute(name);
        }
    }

    @Override
    public Map<String, List<String>> getAttributes() {

        logger.info("getAttributes");

        Map<String, List<String>> attrs = super.getAttributes();
        MultivaluedHashMap<String, String> all = new MultivaluedHashMap<>();
        all.putAll(attrs);
        all.add(DzbUser.GENDER_ATTRIBUTE, user.getGender());
        all.add(DzbUser.HANDICAP_ATTRIBUTE, user.getHandicap());
        all.add(DzbUser.BLIND_ATTRIBUTE, String.valueOf(user.getBlind()));
        all.add(DzbUser.PARTIALLYSIGHTED_ATTRIBUTE, String.valueOf(user.getPartiallysighted()));
        all.add(DzbUser.DYSLEXIA_ATTRIBUTE, String.valueOf(user.getDyslexia()));

        logger.info("Returned " + all.size() + " attributes");
        return all;
    }

    @Override
    public List<String> getAttribute(String name) {

        logger.info("getAttribute: " + name);
        List<String> attr = new LinkedList<>();

        if (name.equals(DzbUser.HANDICAP_ATTRIBUTE)) {
            attr.add(user.getHandicap());
        } else if (name.equals(DzbUser.GENDER_ATTRIBUTE)) {
            attr.add(user.getGender());
        } else if (name.equals(DzbUser.BIRTHDATE_ATTRIBUTE)) {
            attr.add(user.getBirthdate());
        } else {
            List<String> superAttr = super.getAttribute(name);
            attr.addAll((superAttr == null) ? new ArrayList<>() : superAttr);
        }
        return attr;
    }
}