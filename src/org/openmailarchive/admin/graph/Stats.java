package org.openmailarchive.admin.graph;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;

public class Stats {
    public static String getNbMailPerHourSvgGraph() {
        SvgBarGraph sbg = new SvgBarGraph();
        sbg.setBarWidth(3);
        sbg.setInterBarWidth(1);
        sbg.setHeight(300);
        sbg.setWidth(800);
        sbg.setIndicationInterval(12);

        // Main title
        SimpleDateFormat oDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sbg.setTitle("Mails par heure (" + oDateFormat.format(Date.from(Instant.now())) + ")");

        String queryMail = "SELECT MYDATE, COUNT(*) FROM" +
                " (SELECT @row := @row + 1 as row, DATE_FORMAT(DATE_SUB(SYSDATE(), INTERVAL @row HOUR), \"%Y-%m-%d %H\") AS MYDATE " +
                "FROM mail records_table, (SELECT @row := 0) r LIMIT 168) dts LEFT JOIN mail ON MYDATE = DATE_FORMAT(dt, \"%Y-%m-%d %H\") " +
                "GROUP BY MYDATE";

        sbg.setValues(getHashFromQuery(queryMail));

        return sbg.getSvgGraph();
    }

    public static String getNbMailPerDaySvgGraph() {
        SvgBarGraph sbg = new SvgBarGraph();
        sbg.setBarWidth(10);
        sbg.setInterBarWidth(2);
        sbg.setHeight(300);
        sbg.setWidth(500);
        sbg.setIndicationInterval(5);

        // Main title
        SimpleDateFormat oDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sbg.setTitle("Mails par jour (" + oDateFormat.format(Date.from(Instant.now())) + ")");

        String queryMail = "SELECT MYDATE, COUNT(*) " +
                "FROM (SELECT @row := @row + 1 as row, DATE_FORMAT(DATE_SUB(SYSDATE(), INTERVAL @row DAY), \"%Y-%m-%d\") AS MYDATE " +
                "FROM mail records_table, (SELECT @row := 0) r LIMIT 30) dts LEFT JOIN mail ON MYDATE = DATE_FORMAT(dt, \"%Y-%m-%d\") " +
                "GROUP BY MYDATE";

        sbg.setValues(getHashFromQuery(queryMail));

        return sbg.getSvgGraph();
    }

    private static HashMap<String, Integer> getHashFromQuery(String query) {
        int res;
        String dt;
        HashMap<String, Integer> valeurs = new HashMap<>();
        try {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            DataSource ds = (DataSource) envCtx.lookup("jdbc/OpenMailArchDB");

            Connection conn = ds.getConnection();

            PreparedStatement stmtMail = conn.prepareStatement(query);
            ResultSet rsMail = stmtMail.executeQuery();

            if (rsMail.wasNull()) {
                rsMail.close();
                stmtMail.close();
                return null;
            }

            while (rsMail.next()) {
                dt = rsMail.getString(1);
                res = rsMail.getInt(2);
                valeurs.put(dt, res);
            }

            conn.close();
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
        }

        return valeurs;
    }
}
