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
        int externalId = Integer.valueOf(storageId.getExternalId());

        logger.info("getUserById: " + id + ", External Id: " + externalId);

        DzbUser user = em.find(DzbUser.class, externalId);

        if (user == null) {
            logger.info("could not find user by id: " + id);
            return null;
        }
        logger.info("User " + user.getUsername() +  " found by id " + externalId);
        return new UserAdapter(this.session, realm, this.model, user);
    }

    @Override
    public UserModel getUserByUsername(String username, RealmModel realm) {
        logger.info("getUserByUsername: " + username);
        TypedQuery<DzbUser> query = em.createNamedQuery("getUserByUsername", DzbUser.class);
        query.setParameter("username", username);
        List<DzbUser> result = query.getResultList();
        if (result.isEmpty()) {
            logger.info("could not find UserByUsername: " + username);
            return null;
        }

        return new UserAdapter(session, realm, model, result.get(0));
    }

    @Override
    public UserModel getUserByEmail(String email, RealmModel realm) {
        logger.info("getUserByEmail: " + email);
        TypedQuery<DzbUser> query = em.createNamedQuery("getUserByEmail", DzbUser.class);
        query.setParameter("username", email);
        List<DzbUser> result = query.getResultList();

        logger.info("result: " + result);
        if (result.isEmpty()) {
            logger.info("could not find UserByEmail: " + email);
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

        String password = encryptUserCredential(input);
        final boolean valid = password != null && password.equals(getPassword(user));
        logger.info("User " + user.getUsername() + " is " + ((valid) ? "valid" : "invalid"));
        return valid;
    }


    /* user password from store */
    public String getPassword(UserModel user) {
        return getUserAdapter(user).getPassword();
    }


    /* user input credentails */
    public String encryptUserCredential(CredentialInput cred) {

        return DigestUtils.sha256Hex(((UserCredentialModel) cred).getValue());
    }

    @Override
    public int getUsersCount(RealmModel realm) {
        Object count = em.createNamedQuery("getUserCount")
                .getSingleResult();

        final int usersCount =  ((Number) count).intValue();
        logger.info("Users Count: " + usersCount);
        return usersCount;
    }

    @Override
    public List<UserModel> getUsers(RealmModel realm) {
        return getUsers(realm, -1, -1);
    }

    @Override
    public List<UserModel> getUsers(RealmModel realm, int firstResult, int maxResults) {
        logger.info("getUsers: " + firstResult + " " + maxResults);
        TypedQuery<DzbUser> query = em.createNamedQuery("getAllUsers", DzbUser.class);
        if (firstResult != -1) {
            query.setFirstResult(firstResult);
        }
        if (maxResults != -1) {
            query.setMaxResults(maxResults);
        }
        List<DzbUser> results = query.getResultList();
        List<UserModel> users = new LinkedList<>();
        for (DzbUser entity : results) users.add(new UserAdapter(session, realm, model, entity));

        logger.info("Returned " + users.size() + " users");

        return users;
    }

    @Override
    public List<UserModel> searchForUser(String search, RealmModel realm) {
        return searchForUser(search, realm, -1, -1);
    }


    @Override
    public List<UserModel> searchForUser(String search, RealmModel realm, int firstResult, int maxResults) {

        logger.info("searchForUser: " + search);

        TypedQuery<DzbUser> query = em.createNamedQuery("searchForUser", DzbUser.class);


        query.setParameter("search", "%" + search.toLowerCase() + "%");
        if (firstResult != -1) {
            query.setFirstResult(firstResult);
        }
        if (maxResults != -1) {
            query.setMaxResults(maxResults);
        }
        List<DzbUser> results = query.getResultList();
        List<UserModel> users = new LinkedList<>();
        for (DzbUser entity : results) users.add(new UserAdapter(session, realm, model, entity));

        logger.info("Returned " + users.size() + " users");
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
