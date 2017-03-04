package org.openmailarchive.smtpd.MimeHandling;

import java.util.ArrayList;
import java.util.Hashtable;
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
public class MimeMessage extends MimePart{
    private final Hashtable<String, String> bodies = new Hashtable<>();
    private final List<MimePart> parts = new ArrayList<>();
    private boolean isMultiPart = false;

    public String getPlainBody() {
        if(bodies.containsKey("plain"))
            return bodies.get("plain");
        return "";
    }

    public String getHTMLBody() {
        if(bodies.containsKey("html"))
            return bodies.get("html");
        return "";
    }

    public void addBody(String type, String body) {
        this.bodies.put(type, body);
        //System.out.println("addBody: " + type);
    }

    public List<MimePart> getParts() {
        return parts;
    }

    public void addPart(MimePart part) {
        this.parts.add(part);
    }

    public boolean isMultiPart() {
        return isMultiPart;
    }

    public void setMultiPart(boolean multiPart) {
        isMultiPart = multiPart;
    }
}
