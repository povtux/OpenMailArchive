package org.openmailarchive.Entities;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This file is part of OpenMailArchive.
 * <p>
 * OpenMailArchive is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenMailArchive is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenMailArchive.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Created by pov on 18/03/17.
 */
public class User {
    public static final int ACTIVE = 1;
    public static final int INACTIVE = 0;

    private String username;
    private String passwd;
    private Organisation org;
    private String firstname;
    private String lastname;
    private int is_active;
    private List<Group> groups;


    /**
     * Load a user from the database by it's username
     *
     * @param username
     * @return new user object populated with database information or null if not found
     */
    public static User load(String username) {
        User usr = new User();
        usr.setUsername(username);

        String queryMail = "SELECT orgid, firstname, lastname, active FROM user WHERE username=?";

        Context initCtx;
        Connection conn = null;
        try {
            initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            DataSource ds = (DataSource) envCtx.lookup("jdbc/OpenMailArchDB");

            conn = ds.getConnection();

            PreparedStatement stmtMail = conn.prepareStatement(queryMail);
            stmtMail.setString(1, username);
            ResultSet rsMail = stmtMail.executeQuery();

            if (rsMail.wasNull()) {
                rsMail.close();
                stmtMail.close();
                return null;
            }

            if (rsMail.next()) {
                usr.setFirstname(rsMail.getString("firstname"));
                usr.setLastname(rsMail.getString("lastname"));
                usr.setIs_active(rsMail.getInt("active"));

                usr.setOrg(Organisation.load(rsMail.getInt("orgid")));

                rsMail.close();
                stmtMail.close();
            } else {
                rsMail.close();
                stmtMail.close();
                return null;
            }

            List<Group> groups = new ArrayList<>();
            String query = "SELECT grpid FROM user_grp WHERE username=?";
            PreparedStatement stmtGrps = conn.prepareStatement(queryMail);
            stmtGrps.setString(1, username);
            ResultSet rsGrps = stmtGrps.executeQuery();

            while (rsGrps.next()) {
                Group g = Group.load(rsGrps.getInt("grpid"), false);
                assert g != null;
                g.setOrg(usr.getOrg());
                groups.add(g);
            }
            rsGrps.close();
            stmtGrps.close();

            usr.setGroups(groups);
        } catch (NamingException | SQLException e) {
            e.printStackTrace();
        }

        return usr;
    }

    public static boolean authenticate(String username, String passwd) {
        String queryMail = "SELECT COUNT(*) FROM user WHERE username=? AND passwd=PASSWORD(?)";

        Context initCtx;
        Connection conn = null;
        int res = 0;
        try {
            initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            DataSource ds = (DataSource) envCtx.lookup("jdbc/OpenMailArchDB");

            conn = ds.getConnection();

            PreparedStatement stmtMail = conn.prepareStatement(queryMail);
            stmtMail.setString(1, username);
            stmtMail.setString(2, passwd);
            ResultSet rsMail = stmtMail.executeQuery();

            if (rsMail.wasNull()) {
                rsMail.close();
                stmtMail.close();
                return false;
            }

            if (rsMail.next()) {
                res = rsMail.getInt(1);
            }
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
        }

        return res == 1;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public Organisation getOrg() {
        return org;
    }

    public void setOrg(Organisation org) {
        this.org = org;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public int getIs_active() {
        return is_active;
    }

    public void setIs_active(int is_active) {
        this.is_active = is_active;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }
}
