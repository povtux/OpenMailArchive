package org.openmailarchive.smtpd; /**
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

import org.openmailarchive.index.LuceneMailIndexer;
import org.subethamail.smtp.server.SMTPServer;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


@javax.servlet.annotation.WebListener()
public class SmtpdLauncherListener implements ServletContextListener {
    private SMTPServer smtpServer = null;
    private LuceneMailIndexer lmi;

    // Public constructor is required by servlet spec
    public SmtpdLauncherListener() {
    }

    // -------------------------------------------------------
    // ServletContextListener implementation
    // -------------------------------------------------------
    public void contextInitialized(ServletContextEvent sce) {
      /* This method is called when the servlet context is
         initialized(when the Web application is deployed). 
         You can initialize servlet context related data here.
      */
        ServletContext c = sce.getServletContext();

        lmi = new LuceneMailIndexer(c.getInitParameter("luceneStoreBasePath"));
        lmi.start();

        MyMessageHandlerFactory myFactory = new MyMessageHandlerFactory(c, lmi);
        smtpServer = new SMTPServer(myFactory);
        smtpServer.setPort(Integer.parseInt(c.getInitParameter("SMTPPort")));
        smtpServer.start();
    }

    public void contextDestroyed(ServletContextEvent sce) {
      /* This method is invoked when the Servlet Context 
         (the Web application) is undeployed or 
         Application Server shuts down.
      */
      smtpServer.stop();
        lmi.setMustExist();
    }
}
