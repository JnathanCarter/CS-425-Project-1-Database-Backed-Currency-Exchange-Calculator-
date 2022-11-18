package edu.jsu.mcis.Project1;

import edu.jsu.mcis.dao.DAOFactory;
import edu.jsu.mcis.dao.RateDAO;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.html.parser.DTD;

public class RateServlet extends HttpServlet {
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        DAOFactory daoFactory = null;

        ServletContext context = request.getServletContext();

        if (context.getAttribute("daoFactory") == null) {
            System.err.println("*** Creating new DAOFactory ...");
            daoFactory = new DAOFactory();
            context.setAttribute("daoFactory", daoFactory);
        } else {
            daoFactory = (DAOFactory) context.getAttribute("daoFactory");
        }
        response.setContentType("application/json; charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            // Checks for key parameter
            if (request.getParameterMap().containsKey("key")) {

                String uri = request.getPathInfo();
                String[] path;
                String key = request.getParameter("key");

                if (uri != null) {
                    path = uri.split("/");

                    String date;
                    String currency;

                    RateDAO dao = daoFactory.gRateDAO();

                    int lengthOfPath = path.length;

                    switch (lengthOfPath) {
                        case 2:
                            date = path[1];
                            out.println(dao.find(key, date));
                            break;
                        case 3:
                            date = path[1];
                            currency = path[2];

                            out.println(dao.findByDateCurrency(key, date, currency));
                            break;
                    }
                } else {
                    RateDAO dao = daoFactory.gRateDAO();
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDateTime now = LocalDateTime.now();
                    LocalDate currentDate = now.toLocalDate();

                    System.out.println("Todays date --------------> " + currentDate.toString());

                    out.println(dao.find(key, currentDate.toString()));
                }

            } else {
                out.println("NO ACCESS KEY PROVIDED");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
