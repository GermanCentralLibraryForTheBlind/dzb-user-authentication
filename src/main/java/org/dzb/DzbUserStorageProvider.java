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

import org.apache.commons.codec.digest.DigestUtils;
import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.*;
import org.keycloak.models.cache.CachedUserModel;

import org.keycloak.storage.StorageId;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.keycloak.storage.UserStorageProvider;

import javax.ejb.Local;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.*;

/**
 * @author <a href="mailto:lars.voigt@dzb.de">Lars Voigt</a>
 * @version $Revision: 1 $
 */
@Stateful
@Local(DzbUserStorageProvider.class)
public class DzbUserStorageProvider implements
        UserStorageProvider,
        UserLookupProvider,
        CredentialInputValidator,
        CredentialInputUpdater,
        UserQueryProvider {

    protected KeycloakSession session;
    protected ComponentModel model;

    @PersistenceContext(unitName = "dzb-userstore-keycloak-spi-adapter")
    protected EntityManager em;

    private static final Logger logger = Logger.getLogger(UserStorageProvider.class);
    // map of loaded users in this transaction
    //  protected Map<String, UserModel> loadedUsers = new HashMap<>();

    public void setModel(ComponentModel model) {
        this.model = model;
    }

    public void setSession(KeycloakSession session) {
        this.session = session;
    }

    // UserLookupProvider methods
    @Override
    public UserModel getUserById(String id, RealmModel realm) {

        logger.info("getUserById: " + id);
        StorageId storageId = new StorageId(id);
        int persistenceId = Integer.valueOf(storageId.getExternalId());
        DzbUserEntity entity = em.find(DzbUserEntity.class, persistenceId);

        logger.info("entity: " + entity.getUsername());
        if (entity == null) {
            logger.info("could not find user by id: " + id);
            return null;
        }

//        return new UserAdapter(this.session, realm, this.model, entity);
        return getUserByUsername( entity.getUsername(), realm);

//        String persistenceId = StorageId.externalId(id);
//        DzbUserEntity entity = em.find(DzbUserEntity.class, persistenceId);
//        if (entity == null) {
//            logger.info("could not find user by id: " + id);
//            return null;
//        }
//        return null;
    }

    @Override
    public UserModel getUserByUsername(String username, RealmModel realm) {
        logger.info("getUserByUsername: " + username);
        TypedQuery<DzbUserEntity> query = em.createNamedQuery("getUserByUsername", DzbUserEntity.class);
        query.setParameter("username", username);
        List<DzbUserEntity> result = query.getResultList();
        if (result.isEmpty()) {
            logger.info("could not find username: " + username);
            return null;
        }

        return new UserAdapter(session, realm, model, result.get(0));
    }

    @Override
    public UserModel getUserByEmail(String email, RealmModel realm) {
        logger.info("getUserByEmail: " + email);
        TypedQuery<DzbUserEntity> query = em.createNamedQuery("getUserByEmail", DzbUserEntity.class);
        query.setParameter("username", email);
        List<DzbUserEntity> result = query.getResultList();

        logger.info("result: " + result);
        if (result.isEmpty()) {
            logger.info("could not find username: " + email);
            return null;
        }
        return new UserAdapter(session, realm, model, result.get(0));
    }

//    @Override
//    public void onCache(RealmModel realm, CachedUserModel user, UserModel delegate) {
//        String password = ((UserAdapter) delegate).getPassword();
//        if (password != null) {
//            user.getCachedWith().put(PASSWORD_CACHE_KEY, password);
//        }
//    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return CredentialModel.PASSWORD.equals(credentialType);
    }

    @Override
    public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {
        return false;
    }

    @Override
    public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {

    }


    public UserAdapter getUserAdapter(UserModel user) {
        UserAdapter adapter = null;
//        if (user instanceof CachedUserModel) {
//            adapter = (UserAdapter) ((CachedUserModel) user).getDelegateForUpdate();
//        } else {
        adapter = (UserAdapter) user;
//        }
        return adapter;
    }

    @Override
    public Set<String> getDisableableCredentialTypes(RealmModel realm, UserModel user) {
        if (getUserAdapter(user).getPassword() != null) {
            Set<String> set = new HashSet<>();
            set.add(CredentialModel.PASSWORD);
            return set;
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType) && getPassword(user) != null;
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {

        if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel))
            return false;

        String password = getCredentialHashedPassword(input);
        return password != null && password.equals(getPassword(user));
    }


    /* user password from store */
    public String getPassword(UserModel user) {
        return getUserAdapter(user).getPassword();
    }


    /* user input credentails */
    public String getCredentialHashedPassword(CredentialInput cred) {

        return DigestUtils.sha256Hex(((UserCredentialModel) cred).getValue());
    }

    @Override
    public int getUsersCount(RealmModel realm) {
        Object count = em.createNamedQuery("getUserCount")
                .getSingleResult();
        return ((Number) count).intValue();
    }

    @Override
    public List<UserModel> getUsers(RealmModel realm) {
        return getUsers(realm, -1, -1);
    }

    @Override
    public List<UserModel> getUsers(RealmModel realm, int firstResult, int maxResults) {
        logger.info("getUsers: " + firstResult + " " + maxResults);
        TypedQuery<DzbUserEntity> query = em.createNamedQuery("getAllUsers", DzbUserEntity.class);
        if (firstResult != -1) {
            query.setFirstResult(firstResult);
        }
        if (maxResults != -1) {
            query.setMaxResults(maxResults);
        }
        List<DzbUserEntity> results = query.getResultList();
        List<UserModel> users = new LinkedList<>();
        for (DzbUserEntity entity : results) users.add(new UserAdapter(session, realm, model, entity));
        return users;
    }

    @Override
    public List<UserModel> searchForUser(String search, RealmModel realm) {
        return searchForUser(search, realm, -1, -1);
    }

    @Override
    public List<UserModel> searchForUser(String search, RealmModel realm, int firstResult, int maxResults) {

        logger.info("searchForUser: " + search);

        TypedQuery<DzbUserEntity> query = em.createNamedQuery("searchForUser", DzbUserEntity.class);


        query.setParameter("search", "%" + search.toLowerCase() + "%");
        if (firstResult != -1) {
            query.setFirstResult(firstResult);
        }
        if (maxResults != -1) {
            query.setMaxResults(maxResults);
        }
        List<DzbUserEntity> results = query.getResultList();
        List<UserModel> users = new LinkedList<>();
        for (DzbUserEntity entity : results) users.add(new UserAdapter(session, realm, model, entity));

        return users;
    }

    @Override
    public List<UserModel> searchForUser(Map<String, String> params, RealmModel realm) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<UserModel> searchForUser(Map<String, String> params, RealmModel realm, int firstResult, int maxResults) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<UserModel> getGroupMembers(RealmModel realm, GroupModel group, int firstResult, int maxResults) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<UserModel> getGroupMembers(RealmModel realm, GroupModel group) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<UserModel> searchForUserByUserAttribute(String attrName, String attrValue, RealmModel realm) {
        return Collections.EMPTY_LIST;
    }

    @Remove
    @Override
    public void close() {
    }
}
