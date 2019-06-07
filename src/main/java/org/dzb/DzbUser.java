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

import javax.persistence.*;
import java.util.ArrayList;


/**
 * @author <a href="mailto:lars.voigt@dzb.de">Lars Voigt</a>
 * @version $Revision: 1 $
 */
@NamedQueries({
        @NamedQuery(
                name = "getUserByUsername",
                query = "select u from DzbUser u where u.username = :username"
        ),
        @NamedQuery(
                name = "getUserByEmail",
                query = "select u from DzbUser u where u.username = :username"
        ),
        @NamedQuery(
                name = "getUserCount",
                query = "select count(u) from DzbUser u"
        ),
        @NamedQuery(
                name = "getAllUsers",
                query = "select u from DzbUser u"
        ),
        @NamedQuery(
                name = "searchForUser",
                query = "select u from DzbUser u where " +
                        "( lower(u.username) like :search or u.lastName like :search )" +
                        " order by u.username"
        ),
})
@Entity
@Table(name = "tblPerson")
public class DzbUser {


    public static String GENDER_ATTRIBUTE = "gender";
    public static String BLIND_ATTRIBUTE = "blind";
    public static String PARTIALLYSIGHTED_ATTRIBUTE = "partiallysighted";
    public static String DYSLEXIA_ATTRIBUTE = "dyslexia";
    public static String HANDICAP_ATTRIBUTE = "handicap";
    public static String BIRTHDATE_ATTRIBUTE = "birthdate";

    @Id
    @Column(name = "Pers_Nr")
    private int id;

    @Column(name = "Pers_Ident_EMail")
    private String username;

    @Column(name = "Pers_Ident_PW")
    private String password;

    @Column(name = "Pers_Nachname")
    private String lastName;

    @Column(name = "Pers_Vorname")
    private String firstName;

    //“wrong column type encountered” schema-validation errors with JPA and Hibernate -> columnDefinition = "nchar"
    @Column(name = "Pers_Geschlecht", columnDefinition = "nchar")
    private String gender;

    @Column(name = "Pers_Blind")
    private boolean blind;

    @Column(name = "Pers_Sehbehindert")
    private boolean partiallysighted;

    @Column(name = "Pers_Legastheniker")
    private boolean dyslexia;


    @Column(name = "Pers_GebDat" , columnDefinition = "smalldatetime")
    private  String birthdate;

    public DzbUser() {
    }

    //    @Column(name="Pers_Ident_EMail")
//    private String email;

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getGender() {
        return gender;
    }

    public String getHandicap() {

        ArrayList<String> handicaps = new ArrayList<>();
        if (blind)
            handicaps.add(BLIND_ATTRIBUTE);
        else if (partiallysighted)
            handicaps.add(PARTIALLYSIGHTED_ATTRIBUTE);
        else if (dyslexia)
            handicaps.add(DYSLEXIA_ATTRIBUTE);


        return String.join(",", handicaps);
    }

    public boolean getBlind() {
        return blind;
    }

    public boolean getPartiallysighted() {
        return partiallysighted;
    }

    public boolean getDyslexia() {
        return dyslexia;
    }

    public String getBirthdate() {
        return birthdate;
    }
}
