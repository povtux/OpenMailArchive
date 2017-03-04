package org.openmailarchive.smtpd.MimeHandling;

import org.apache.james.mime4j.stream.Field;

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
 * Created by pov on 18/12/16.
 */
public class MimePart {
    private final List<Field> headers = new ArrayList<>();
    private String body = "";
    private String mimeType = "";

    public List<Field> getHeaders() {
        return headers;
    }

    public String searchHeader(String headerName) {
        for(Field f:headers) {
            if(f.getName().toLowerCase().equals(headerName.toLowerCase()))
                return f.getBody();
        }
        return null;
    }

    public void addHeader(Field header) {
        this.headers.add(header);
        // System.out.println(header.getName());
    }

    public String getBody() {
        return body;
    }

    void setBody(String body) {
        this.body = body;
    }

    public String getMimeType() {
        return mimeType;
    }

    void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
