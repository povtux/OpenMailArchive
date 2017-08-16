package org.openmailarchive.Entities;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
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
public class Group {
    private int grpid;
    private String name;
    private Organisation org;
    private List<String> members;

    /**
     * loads a group from it's ID in database
     *
     * @param id
     * @return Group or null if not found
     */
    public static Group load(int id, boolean organisationFromDb, ServletContext context) {
        Group grp = new Group();
        grp.setGrpid(id);

        String query;
        if (organisationFromDb) {
            query = "SELECT name, orgid FROM grp WHERE grpid=?";
        } else {
            query = "SELECT name FROM grp WHERE grpid=?";
        }

        Context initCtx;
        Connection conn;
        try {
            initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            DataSource ds = (DataSource) envCtx.lookup("jdbc/OpenMailArchDB");

            conn = ds.getConnection();

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, id);
            ResultSet rsMail = stmt.executeQuery();

            if (rsMail.wasNull()) {
                rsMail.close();
                stmt.close();
                return null;
            }

            if (rsMail.next()) {
                grp.setName(rsMail.getString("name"));
                context.log("GROUP LOAD: found in DB: " + grp.getName());
                if (organisationFromDb)
                    grp.setOrg(Organisation.load(rsMail.getInt("orgid"), context));

                rsMail.close();
                stmt.close();
            } else {
                rsMail.close();
                stmt.close();
                return null;
            }

            String membersQuery = "SELECT username FROM grp_members WHERE grpid=?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, id);
            ResultSet rsMembers = stmt.executeQuery();

            List<String> members = new ArrayList<>();
            while (rsMembers.next()) {
                members.add(rsMembers.getString(1));
            }
            rsMembers.close();
            stmt.close();

            grp.setMembers(members);

            conn.close();
        } catch (NamingException | SQLException e) {
            context.log(e.getMessage(), e);
        }

        return grp;
    }

    public int getGrpid() {
        return grpid;
    }

    private void setGrpid(int grpid) {
        this.grpid = grpid;
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public Organisation getOrg() {
        return org;
    }

    public void setOrg(Organisation org) {
        this.org = org;
    }

    public void addMember(String member) {
        if (members == null) {
            members = new ArrayList<>();
        }

        if (!members.contains(member)) {
            members.add(member);

            String query = "INSERT INTO grp_members(username, grpid) VALUES(?, ?)";
            execUpdate(query, member);
        }
    }

    public void delMember(String member) {
        if (members == null) {
            members = new ArrayList<>();
        }

        if (members.contains(member)) {
            members.remove(member);

            String query = "DELETE FROM grp_members WHERE username=? AND grpid=?";
            execUpdate(query, member);
        }
    }

    private void execUpdate(String query, String member) {
        Context initCtx;
        Connection conn;
        try {
            initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            DataSource ds = (DataSource) envCtx.lookup("jdbc/OpenMailArchDB");

            conn = ds.getConnection();

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, member);
            stmt.setInt(2, this.grpid);
            stmt.executeUpdate();
            stmt.close();

            conn.close();
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
        }
    }

    private void setMembers(List<String> members) {
        this.members = members;
    }

    public List<String> getMembers() {
        return members;
    }
}
