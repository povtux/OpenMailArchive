package org.openmailarchive.search;

import org.json.simple.JSONArray;
import org.openmailarchive.Entities.User;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

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
 * Created by pov on 15/12/16.
 */
@WebServlet(name = "SearchServlet", urlPatterns = {"/search"})
public class SearchServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Context initCtx;
        Connection conn;
        try {
            initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            DataSource ds = (DataSource) envCtx.lookup("jdbc/OpenMailArchDB");
            conn = ds.getConnection();

            int pageOffset = 0;
            if (request.getParameter("next") != null)
                pageOffset = Integer.parseInt(request.getParameter("next"));

            JSONArray json;
            if (request.getParameter("query") != null)
                /*json = search.querySearch(
                        conn,
                        (User) request.getSession().getAttribute("user"),
                        pageOffset,
                        request.getParameter("query"));*/
                json = new JSONArray();
            else {
                DbSearch search = new DbSearch();
                json = search.defaultSearch(conn, (User) request.getSession().getAttribute("user"), pageOffset);
            }

            response.setContentType("application/json");
            response.getWriter().write(json.toString());

            conn.close();
        } catch (SQLException | NamingException e) {
            getServletContext().log(e.getMessage(), e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
