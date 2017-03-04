package org.openmailarchive.smtpd;/**
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
 * Created by pov on 14/12/16.
 */

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.message.DefaultBodyDescriptorBuilder;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.stream.BodyDescriptorBuilder;
import org.apache.james.mime4j.stream.MimeConfig;
import org.apache.james.mime4j.stream.RawField;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.openmailarchive.Entities.Attachment;
import org.openmailarchive.Entities.Mail;
import org.openmailarchive.Entities.Recipient;
import org.openmailarchive.index.LuceneMailIndexer;
import org.openmailarchive.smtpd.MimeHandling.MimeContentHandler;
import org.openmailarchive.smtpd.MimeHandling.MimePart;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.RejectException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Base64.Decoder;

class MyMessageHandlerFactory implements MessageHandlerFactory {

    private final ServletContext context;

    MyMessageHandlerFactory(ServletContext c) {
        this.context = c;
    }

    public MessageHandler create(MessageContext ctx) {
        return new Handler(ctx);
    }

    class Handler implements MessageHandler {
        final MessageContext ctx;
        String completeMail;

        Handler(MessageContext ctx) {
            this.ctx = ctx;
        }

        public void from(String from) throws RejectException {}

        public void recipient(String recipient) throws RejectException {}

        public void data(InputStream data) throws IOException {
            StringWriter writer = new StringWriter();
            IOUtils.copy(data, writer, "UTF-8");
            completeMail = writer.toString();
        }

        public void done() {
            MimeContentHandler contentHandler = new MimeContentHandler();
            try {
                // configure parsing
                MimeConfig mime4jParserConfig = new MimeConfig();
                BodyDescriptorBuilder bodyDescriptorBuilder = new DefaultBodyDescriptorBuilder();
                MimeStreamParser mime4jParser = new MimeStreamParser(mime4jParserConfig, DecodeMonitor.SILENT,bodyDescriptorBuilder);
                mime4jParser.setContentDecoding(true);
                mime4jParser.setContentHandler(contentHandler);

                // parse
                mime4jParser.parse(new ByteArrayInputStream(completeMail.getBytes(StandardCharsets.UTF_8)));
            } catch (MimeException | IOException e) {
                context.log("", e);
            }

            org.openmailarchive.smtpd.MimeHandling.MimeMessage msg = contentHandler.getMessage();

            // check if mail ID is present and not empty. if so, build one based on date, from, to, subject and md5 or size
            if(msg.searchHeader("Message-ID") == null || msg.searchHeader("Message-ID").equals("")) {
                String id;
                try {
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    id = msg.searchHeader("date") +
                            msg.searchHeader("from") +
                            msg.searchHeader("to") +
                            msg.searchHeader("subject") +
                            Arrays.toString(md.digest(completeMail.getBytes()));
                } catch (NoSuchAlgorithmException e) {
                    context.log(String.format("'%s'", msg.searchHeader("Message-ID")), e);
                    // if enable to calculate MD5, use size. Less sure but should not append
                    id = msg.searchHeader("date") +
                            msg.searchHeader("from") +
                            msg.searchHeader("to") +
                            msg.searchHeader("subject") +
                            completeMail.length();
                }
                msg.addHeader(new RawField("Message-ID", id));
            }
            // Check if mail ID is not already present. It might come multiple times if multiple recipients or misconfiguration of front mail server
            Context initCtx;
            Connection conn = null;
            try {
                initCtx = new InitialContext();
                Context envCtx = (Context) initCtx.lookup("java:comp/env");
                DataSource ds = (DataSource)envCtx.lookup("jdbc/OpenMailArchDB");

                conn = ds.getConnection();

                String query = String.format("SELECT COUNT(*) FROM `mail` WHERE `mailid`='%s'",
                        msg.searchHeader("Message-ID"));
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                while(rs.next()) {
                    int nb  = rs.getInt(1);
                    if(nb>0) {
                        // log the fact that we do not treat a mail already treated
                        context.log(String.format("Mailid '%s' already received and indexed",
                                msg.searchHeader("Message-ID")));
                        return;
                    }
                }
            } catch (NamingException | SQLException e) {
                context.log(String.format("'%s'", msg.searchHeader("Message-ID")), e);
            }

            // write mail to disk
            // TODO: clean message-id for filename <>...
            String filepath = context.getInitParameter("mailStoreBasePath") +
                    new SimpleDateFormat("yyyy/MM/dd/").format(new Date()) +
                    msg.searchHeader("Message-ID") +
                    ".eml";

            try {
                FileUtils.writeStringToFile(new File(filepath), completeMail, "UTF-8");
            } catch (IOException e) {
                // log write to disk failed
                context.log(String.format("Writing file for mail id '%s' failed",
                        msg.searchHeader("Message-ID")), e);
            }
            // create database record for mail & index data
            Tika tika = new Tika();

            Mail m = new Mail();
            // Message ID
            m.setMailid(msg.searchHeader("Message-ID"));
            // From
            m.setMailfrom(msg.searchHeader("from"));
            // Subject
            m.setSubject(msg.searchHeader("subject"));
            // path to .eml file
            m.setFilepath(filepath);

            // Date (parse and convert)
            SimpleDateFormat formatter = new SimpleDateFormat("d MMM yyyy H:m:s Z", Locale.ENGLISH);
            String theDate = msg.searchHeader("date");
            if(theDate.contains("(")) {
                theDate = theDate.substring(0, theDate.lastIndexOf('(')-1).trim();
            }
            if(theDate.contains(",")) {
                theDate = theDate.substring(4).trim();
            }
            try {
                m.setDt(new java.sql.Timestamp(formatter.parse(theDate).getTime()));
            } catch (ParseException e) {
                context.log(String.format("'%s'", m.getMailid()), e);
            }

            // To (split and add)
            for(String to: msg.searchHeader("to").split(",")) {
                m.addRecipient(new Recipient(Recipient.RECIPIENT_TYPE_TO, to.trim()));
            }

            // Cc (split and add)
            if(msg.searchHeader("cc") != null)
                for(String to: msg.searchHeader("cc").split(",")) {
                    m.addRecipient(new Recipient(Recipient.RECIPIENT_TYPE_CC, to.trim()));
                }

            // Bcc (split and add)
            if(msg.searchHeader("bcc") != null)
                for(String to: msg.searchHeader("bcc").split(",")) {
                    m.addRecipient(new Recipient(Recipient.RECIPIENT_TYPE_BCC, to.trim()));
                }

            // Body (if HTML is present, use this one, else, the plain body)
            String bodyString = msg.getPlainBody();
            if(msg.getHTMLBody() != null && !Objects.equals(msg.getHTMLBody(), "")) {
                m.setBody(msg.getHTMLBody());
                m.setBodyType(Mail.HTML);

                try {
                    bodyString = tika.parseToString(new ByteArrayInputStream(msg.getHTMLBody().getBytes(StandardCharsets.UTF_8)));
                } catch (TikaException | IOException e) {
                    context.log(String.format("'%s'", m.getMailid()), e);
                }
            }
            else {
                m.setBody(msg.getPlainBody());
                m.setBodyType(Mail.PLAIN);
            }

            // Attachments
            Map<String, String> attach = new HashMap<>();
            String attachBody;
            Decoder decoder = Base64.getDecoder();
            for(MimePart mp: msg.getParts()) {
                Attachment att = new Attachment(mp.getMimeType(),
                        mp.searchHeader("Content-Disposition").split("=")[1].trim()
                );
                m.addAttachment(att);

                try {
                    attachBody = tika.parseToString(
                            decoder.wrap(
                                    new ByteArrayInputStream(
                                            mp.getBody().getBytes(StandardCharsets.UTF_8)
                                    )
                            )
                    );
                    // Add attachment to the Map for future creation of Lucene index
                    attach.put(att.getFilename(), attachBody);
                } catch (TikaException | IOException e) {
                    context.log(String.format("'%s'", m.getMailid()), e);
                }
            }

            // create Lucene index
            LuceneMailIndexer lmi = new LuceneMailIndexer(context.getInitParameter("luceneStoreBasePath") );
            try {
                lmi.indexMail(m.getMailid(), bodyString, attach);
                lmi.end();
            } catch (IOException e) {
                context.log(String.format("'%s'", m.getMailid()), e);
            }

            // try to insert data in MySQL
            try {
                m.insert(conn);
            } catch (SQLException e) {
                context.log(String.format("'%s'", m.getMailid()), e);
            }

            // at the end, release DB connexion
            try {
                if(conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                context.log(String.format("'%s'", m.getMailid()), e);
            }
        }

    }
}