package org.openmailarchive.search;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openmailarchive.Entities.Mail;
import org.openmailarchive.Entities.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Logger;

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
 * Created by pov on 19/03/17.
 */
public class DbSearch {
    // TODO: make this parameter readable from config properties
    private static int mailsParPage = 20;

    public JSONArray defaultSearch(Connection conn, User currentUser, int pageOffset) {
        return getJsonArrayFromSqlQuery(conn, currentUser, pageOffset, "");
    }

    public JSONArray querySearch(Connection conn, User currentUser, int pageOffset, String query) {
        String whereClause = " AND (subject LIKE '%" + query + "%' OR body LIKE '%" + query + "%') ";
        return getJsonArrayFromSqlQuery(conn, currentUser, pageOffset, whereClause);
    }

    private JSONArray getJsonArrayFromSqlQuery(Connection conn, User currentUser, int pageOffset, String whereClause) {
        JSONArray json = new JSONArray();

        // calculate records offset depending on page offset
        String offset = "";
        if (pageOffset > 0)
            offset = " OFFSET " + pageOffset * mailsParPage;

        // TODO: enable users not to use spamFilter from GUI.
        String spamFilter = "spamScore<5.0 AND ";
        String query = "SELECT mailid,mailfrom,dt,subject,body,bodyType FROM mail WHERE " + spamFilter +
                getDefaultWhereClauseFromUser(conn, currentUser) +
                whereClause +
                " ORDER BY dt DESC LIMIT " + mailsParPage + offset;

        Logger.getLogger("SEARCH").info("QUERY: " + query);

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                JSONObject obj = new JSONObject();
                obj.put("mailid", rs.getString("mailid"));
                obj.put("mailfrom", rs.getString("mailfrom")
                        .replace("<", "&lt;").replace(">", "&gt;"));
                obj.put("dt", rs.getTimestamp("dt").toString());
                obj.put("subject", rs.getString("subject"));
                obj.put("body", rs.getString("body"));
                obj.put("bodyType", rs.getString("bodyType"));
                json.add(obj);
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return json;
    }

    public JSONArray getBodyOfMailById(Connection conn, User currentUser, String mailId) {
        JSONArray json = new JSONArray();

        String query = "SELECT count(*) FROM mail WHERE (" +
                getDefaultWhereClauseFromUser(conn, currentUser) +
                ") AND mailid=?";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, mailId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                if (rs.getInt(1) >= 1) {
                    json.add(Mail.load(conn, mailId).getJSON());
                }
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return json;
    }

    private String getDefaultWhereClauseFromUser(Connection conn, User user) {
        String whereClause = "";

        if (user.getIs_org_admin() == User.ACTIVE) {
            String query = "SELECT domain FROM org_domain WHERE orgid IN(SELECT orgid FROM user WHERE username=?)";
            try {
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, user.getUsername());
                ResultSet rs = stmt.executeQuery();

                String like1 = "";
                String like2 = "";
                while (rs.next()) {
                    if (!Objects.equals(like1, "")) like1 += " OR ";
                    like1 += "recipient LIKE \"%@" + rs.getString(1) + "%\"";
                    if (!Objects.equals(like2, "")) like2 += " OR ";
                    like2 += "mailfrom LIKE \"%@" + rs.getString(1) + "%\"";
                }
                rs.close();
                stmt.close();
                whereClause = "(mailid IN(SELECT mailid FROM recipient WHERE " + like1 + " ) OR " + like2 + ")";
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            whereClause = "(mailid IN(SELECT mailid FROM recipient WHERE recipient LIKE \"%" +
                    user.getUsername() + "%\" ) OR mailfrom LIKE \"%" + user.getUsername() + "%\")";
        }

        return whereClause;
    }
}
