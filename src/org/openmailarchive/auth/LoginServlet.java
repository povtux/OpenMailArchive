package org.openmailarchive.auth;

import org.openmailarchive.Entities.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
@WebServlet(urlPatterns = {"/login"}, name = "login")
public class LoginServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user;

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        Map<String, String> messages = new HashMap<>();

        getServletContext().log("LOGIN: try to authenticate user: " + username);

        if (username == null || username.isEmpty()) {
            messages.put("username", "Please enter username");
        }

        if (password == null || password.isEmpty()) {
            messages.put("password", "Please enter password");
        }

        if (messages.isEmpty()) {
            if (User.authenticate(username, password)) {
                getServletContext().log("LOGIN: authenticated user: " + username);
                user = User.load(username, getServletContext());
                getServletContext().log("LOGIN: loaded user: " + username);
                request.getSession().setAttribute("user", user);
                getServletContext().log("LOGIN: session updated user: " + username);
                response.sendRedirect(request.getContextPath() + "/index.jsp");
                getServletContext().log("LOGIN: redirect user: " + username);
                return;
            } else {
                messages.put("login", "Unknown login, please try again");
                getServletContext().log("LOGIN: bad auth for user: " + username);
            }
        }

        request.setAttribute("messages", messages);
        request.getRequestDispatcher("/login.jsp").forward(request, response);

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }
}
