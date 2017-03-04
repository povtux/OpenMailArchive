package org.openmailarchive.Entities;

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
 * Created by pov on 19/02/17.
 */
public class Recipient {
    public static final String RECIPIENT_TYPE_TO = "TO";
    public static final String RECIPIENT_TYPE_CC = "CC";
    public static final String RECIPIENT_TYPE_BCC = "BCC";

    private final String type;
    private final String address;

    public Recipient(String type, String address) {
        this.type = type;
        this.address = address;
    }

    String getType() {
        return type;
    }

    String getAddress() {
        return address;
    }
}
