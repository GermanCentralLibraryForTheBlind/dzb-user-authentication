package org.dzb.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.jboss.logging.Logger;
import org.keycloak.models.UserCredentialModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class TestSetup {

    private static final Logger logger = Logger.getLogger(TestSetup.class);

    protected static MSSQLDatabaseAdapter mssqlDatabaseAdapter;
    private static String SERVER_NAME = "dzbvm-sql2005";
    private static String DB = "DZB";
    private static String USER = "OpenID_Test";
    private static String PASSWORD = "oidtest";


    public static void init() {

        Properties properties = new Properties();
        properties.setProperty("serverName", SERVER_NAME);
        properties.setProperty("db", DB);
        properties.setProperty("user", USER);
        properties.setProperty("password", PASSWORD);

        try {
            mssqlDatabaseAdapter = new MSSQLDatabaseAdapter(properties);
            mssqlDatabaseAdapter.getConnection();
        } catch (Exception ex) {
            logger.error("Failed to connect database", ex);
        }
    }

    //INSERT INTO "tblPerson" ("Pers_Nr", "Pers_Inst_Nr", "Pers_AP_von", "Pers_Anrede", "Pers_Titel", "Pers_Vorname", "Pers_Nachname", "Pers_Geschlecht", "Pers_GebDat", "Pers_Beruf", "Pers_Besteuer", "Pers_EULand", "Pers_EUUStID", "Pers_Notiz", "Pers_LoeschDat", "Pers_LoeschKennung", "Pers_AddUser", "Pers_AddTime", "Pers_NachnameBSV_aktuell",
    // "Pers_NachnameBSK_aktuell", "Pers_NachnameBSV", "Pers_NachnameBSK", "Pers_BSK_Einlesedatum", "Pers_BSV_Einlesedatum", "Pers_XMLerzeugen", "Pers_Kundengruppe", "Pers_Kundentyp", "Pers_Beguenstigt", "Pers_Nachweis", "Pers_Blind", "Pers_Sehbehindert", "Pers_Legastheniker", "Pers_Koerperbehindert", "Pers_Weiteres", "Pers_Ohne_Beeintraechtigung", "Pers_Ident_EMail", "Pers_Ident_PW", "Pers_Ident_Datum_aktiviert", "Pers_Ident_gesperrt", "Pers_Ident_letzte_Anmeldung") VALUES (282, NULL, NULL, 'Frau', NULL, '', '', 'W', '', NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'False', 'False', 'False', 'False', 'False', 'False', 'False', 'False', NULL, NULL, NULL, 'False', NULL);

    public static void main(String[] args) {

        init();

        List<Properties> ps = new ArrayList<>();

        Properties p1 = new Properties();
        p1.setProperty("table", "tblPerson");
        p1.setProperty("Pers_Ident_EMail", "blind@dzb.de"); // username
        p1.setProperty("Pers_Ident_PW", hashPassword("blind")); // password
        p1.setProperty("Pers_Nachname", "Charles"); // last name
        p1.setProperty("Pers_Vorname", "Ray"); // firstname
        p1.setProperty("Pers_Blind", "true"); // handicap blind


        Properties p2 = new Properties();
        p2.setProperty("table", "tblPerson");
        p2.setProperty("Pers_Ident_EMail", "partially-sighted@dzb.de"); // username
        p2.setProperty("Pers_Ident_PW", hashPassword("partially-sighted")); // password
        p2.setProperty("Pers_Nachname", "Mueller"); // last name
        p2.setProperty("Pers_Vorname", "Julian"); // firstname
        p2.setProperty("Pers_Sehbehindert", "true"); // handicap partially-sighted

        Properties p3 = new Properties();
        p3.setProperty("table", "tblPerson");
        p3.setProperty("Pers_Ident_EMail", "dyslexia@dzb.de"); // username
        p3.setProperty("Pers_Ident_PW", hashPassword("dyslexia")); // password
        p3.setProperty("Pers_Nachname", "Schulze"); // last name
        p3.setProperty("Pers_Vorname", "Kurt"); // firstname
        p3.setProperty("Pers_Legastheniker", "true"); // handicap dyslexia

        ps.add(p1);
        ps.add(p2);
        ps.add(p3);

        try {
            mssqlDatabaseAdapter.addUsers(ps);
        } catch (Exception ex) {
            logger.error("Failed to add user: ", ex);
        }
    }


    // refactoring same function in DzbUserStorageProvider
    private static String hashPassword(String password) {
        return DigestUtils.sha256Hex(password);
    }
}
