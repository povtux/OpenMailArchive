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
public class Organisation {
    private int id = 0;
    private String name;
    private List<String> domains;

    /**
     * load an Organisation from the database according to the id in parameter
     *
     * @param id
     * @return Organisation or null if not found
     */
    public static Organisation load(int id) {
        Organisation org = new Organisation();
        org.setId(id);

        String queryOrg = "SELECT orgname FROM organisation WHERE orgid=?";

        Context initCtx;
        Connection conn = null;
        try {
            initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            DataSource ds = (DataSource) envCtx.lookup("jdbc/OpenMailArchDB");

            conn = ds.getConnection();

            PreparedStatement stmtMail = conn.prepareStatement(queryOrg);
            stmtMail.setInt(1, id);
            ResultSet rsOrg = stmtMail.executeQuery();

            if (rsOrg.wasNull()) {
                rsOrg.close();
                stmtMail.close();
                return null;
            }

            if (rsOrg.next()) {
                org.setName(rsOrg.getString("orgname"));
                rsOrg.close();
                stmtMail.close();
            } else {
                rsOrg.close();
                stmtMail.close();
                return null;
            }

            String queryDom = "SELECT domain FROM org_domain WHERE orgid=?";
            PreparedStatement stmt = conn.prepareStatement(queryDom);
            stmt.setInt(1, id);
            ResultSet rsDom = stmt.executeQuery();

            List<String> doms = new ArrayList<>();
            while (rsDom.next())
                doms.add(rsDom.getString("domain"));

            rsDom.close();
            stmt.close();

            org.setDomains(doms);

        } catch (NamingException | SQLException e) {
            e.printStackTrace();
        }


        return org;
    }

    public boolean save() {
        Context initCtx;
        Connection conn = null;
        int res = 0;
        if (id == 0) {
            // insert
            String insertQuery = "INSERT INTO organisation(orgname) VALUES(?)";
            try {
                initCtx = new InitialContext();
                Context envCtx = (Context) initCtx.lookup("java:comp/env");
                DataSource ds = (DataSource) envCtx.lookup("jdbc/OpenMailArchDB");

                conn = ds.getConnection();

                PreparedStatement stmt = conn.prepareStatement(insertQuery);
                stmt.setString(1, name);
                res = stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    id = rs.getInt(1);
                }

                rs.close();
                stmt.close();
            } catch (NamingException | SQLException e) {
                e.printStackTrace();
            }
        } else {
            // update
            String insertQuery = "UPDATE organisation SET orgname=? WHERE orgid=?";
            try {
                initCtx = new InitialContext();
                Context envCtx = (Context) initCtx.lookup("java:comp/env");
                DataSource ds = (DataSource) envCtx.lookup("jdbc/OpenMailArchDB");

                conn = ds.getConnection();

                PreparedStatement stmt = conn.prepareStatement(insertQuery);
                stmt.setString(1, name);
                stmt.setInt(2, id);
                res = stmt.executeUpdate();
                stmt.close();
            } catch (NamingException | SQLException e) {
                e.printStackTrace();
            }
        }

        return res != 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addDomain(String member) {
        if (domains == null) {
            domains = new ArrayList<>();
        }

        if (!domains.contains(member)) {
            domains.add(member);

            String query = "INSERT INTO org_domain(domain, orgid) VALUES(?, ?)";
            execUpdate(query, member);
        }
    }

    public void delDomain(String member) {
        if (domains == null) {
            domains = new ArrayList<>();
        }

        if (domains.contains(member)) {
            domains.remove(member);

            String query = "DELETE FROM org_domain WHERE doamin=? AND orgid=?";
            execUpdate(query, member);
        }
    }

    private void execUpdate(String query, String member) {
        Context initCtx;
        Connection conn = null;
        try {
            initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            DataSource ds = (DataSource) envCtx.lookup("jdbc/OpenMailArchDB");

            conn = ds.getConnection();

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, member);
            stmt.setInt(2, this.id);
            stmt.executeUpdate();
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
        }
    }

    public List<String> getDomains() {
        return domains;
    }

    private void setDomains(List<String> domains) {
        this.domains = domains;
    }
}
