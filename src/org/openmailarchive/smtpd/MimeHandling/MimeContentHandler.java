package org.openmailarchive.smtpd.MimeHandling;

import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.stream.BodyDescriptor;
import org.apache.james.mime4j.stream.Field;

import java.io.IOException;
import java.io.InputStream;

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
 * Created by pov on 18/12/16.
 */
public class MimeContentHandler implements ContentHandler{
    private MimeMessage message;
    private MimePart part;
    private boolean inPart;

    @Override
    public void startMessage() throws MimeException {
        message = new MimeMessage();
        inPart = false;
    }

    @Override
    public void endMessage() throws MimeException {

    }

    @Override
    public void startBodyPart() throws MimeException {
        part = new MimePart();
        inPart = true;
    }

    @Override
    public void endBodyPart() throws MimeException {
        if(part != null)
            message.addPart(part);
        part = null;
        inPart = false;
    }

    @Override
    public void startHeader() throws MimeException {

    }

    @Override
    public void field(Field rawField) throws MimeException {
        if(inPart) part.addHeader(rawField);
        else message.addHeader(rawField);
    }

    @Override
    public void endHeader() throws MimeException {

    }

    @Override
    public void preamble(InputStream is) throws MimeException, IOException {

    }

    @Override
    public void epilogue(InputStream is) throws MimeException, IOException {

    }

    @Override
    public void startMultipart(BodyDescriptor bd) throws MimeException {
        message.setMultiPart(true);
    }

    @Override
    public void endMultipart() throws MimeException {

    }

    @Override
    public void body(BodyDescriptor bd, InputStream is) throws MimeException, IOException {
        String charset = bd.getCharset();
        if (charset == null) charset = "UTF8";
        else if (charset.toLowerCase().equals("cp-850")) charset = "latin1";
        String body = IOUtils.toString(is, charset);
        //System.out.println(bd.getMimeType());
        if(!inPart) {
            // assume a simple mail with only a body

            if(bd.getMimeType().equals("text/plain"))
                message.addBody("plain", body);
            else if(bd.getMimeType().equals("text/html"))
                message.addBody("html", body);
            else {
                message.setBody(body);
                message.setMimeType(bd.getMimeType());
            }
        }

        else {
            //System.out.println(bd.getMimeType() + " " + part.searchHeader("Content-Disposition"));
            // could be the body of the message itself in plain or HTML but could also be the body of an attachment
            if(bd.getMimeType().equals("text/plain") && part.searchHeader("Content-Disposition") == null) {
                // assume a plain text with no content disposition is the plain text representation of the message
                message.addBody("plain", body);
                part = null;
            }
            else if(bd.getMimeType().equals("text/html") && part.searchHeader("Content-Disposition") == null) {
                // assume a HTML with no content disposition is the HTML representation of the message
                message.addBody("html", body);
                part = null;
            }
            else {
                // assume this is the body of an attachment
                part.setBody(body);
                part.setMimeType(bd.getMimeType());
            }
        }
    }

    @Override
    public void raw(InputStream is) throws MimeException, IOException {

    }

    public MimeMessage getMessage() {
        return message;
    }
}
