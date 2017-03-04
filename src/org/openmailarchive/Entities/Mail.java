package org.openmailarchive.Entities;

import org.apache.commons.lang.NullArgumentException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This file is part of OpenMailArchive.
 *
 * OpenMailArchive is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenMailArchive is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenMailArchive.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Created by pov on 12/02/17.
 */

public class Mail {
    public static final int PLAIN = 0;
    public static final int HTML = 1;

    private String mailid;
    private String filepath;
    private String mailfrom;
    private Timestamp dt;
    private String subject;
    private String body;
    private int bodyType;
    private final List<Recipient> recipients;
    private final List<Attachment> attachments;

    public Mail() {
        recipients = new ArrayList<>();
        attachments = new ArrayList<>();
    }

    public String getMailid() {
        return mailid;
    }

    public void setMailid(String mailid) throws NullArgumentException {
        if(mailid == null) throw new NullArgumentException("mailid cannot be null");
        this.mailid = mailid;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) throws NullArgumentException {
        if(filepath == null) throw new NullArgumentException("filepath cannot be null");
        this.filepath = filepath;
    }

    public String getMailfrom() {
        return mailfrom;
    }

    public void setMailfrom(String mailfrom) throws NullArgumentException {
        if(mailfrom == null) throw new NullArgumentException("mailfrom cannot be null");
        this.mailfrom = mailfrom;
    }

    public Timestamp getDt() {
        return dt;
    }

    public void setDt(Timestamp dt) throws NullArgumentException {
        if(dt == null) throw new NullArgumentException("dt cannot be null");
        this.dt = dt;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) throws NullArgumentException {
        if(subject == null) throw new NullArgumentException("subject cannot be null");
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) throws NullArgumentException {
        if(body == null) throw new NullArgumentException("body cannot be null");
        this.body = body;
    }

    public int getBodyType() {
        return bodyType;
    }

    public void setBodyType(int bodyType) throws IllegalArgumentException {
        if(bodyType < 0 || bodyType > 1) throw new IllegalArgumentException("bodyType must be a known value");
        this.bodyType = bodyType;
    }

    public List<Recipient> getRecipients() {
        return recipients;
    }

    public void addRecipient(Recipient recipient) {
        this.recipients.add(recipient);
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void addAttachment(Attachment attachment) {
        this.attachments.add(attachment);
    }

    public boolean insert(Connection conn) throws SQLException {

        conn.setAutoCommit(false);

        String insertMail = "INSERT INTO `mail`(`mailid`, `filepath`, `mailfrom`, `dt`, `subject`, `body`, `bodyType`) VALUES(?, ?, ?, ?, ?, ?, ?)";
        String insertRecipient = "INSERT INTO `recipient`(`mailid`, `recipientType`, `recipient`) VALUES(?, ?, ?)";
        String insertAttachment = "INSERT INTO `attachment`(`mailid`, `mimeType`, `filename`) VALUES(?, ?, ?)";

        try {
            PreparedStatement stmtMail = conn.prepareStatement(insertMail);
            stmtMail.setString(1, mailid);
            stmtMail.setString(2, filepath);
            stmtMail.setString(3, mailfrom);
            stmtMail.setTimestamp(4, dt);
            stmtMail.setString(5, subject);
            stmtMail.setString(6, body);
            stmtMail.setInt(7, bodyType);
            stmtMail.executeUpdate();

            PreparedStatement stmtRecip = conn.prepareStatement(insertRecipient);
            for (Recipient r: recipients) {
                stmtRecip.setString(1, mailid);
                stmtRecip.setString(2, r.getType());
                stmtRecip.setString(3, r.getAddress());
                stmtRecip.executeUpdate();
            }

            PreparedStatement stmtAttach = conn.prepareStatement(insertAttachment);
            for (Attachment a: attachments) {
                stmtAttach.setString(1, mailid);
                stmtAttach.setString(2, a.getType());
                stmtAttach.setString(3, a.getFilename());
                stmtAttach.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            conn.rollback();
            return false;
        }
        return true;
    }
}
