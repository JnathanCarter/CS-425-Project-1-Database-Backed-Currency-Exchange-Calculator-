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
            // contains date
            if (request.getParameterMap().containsKey("date")
                    && (!(request.getParameterMap().containsKey("currency")))) {
                String date = request.getParameter("date");
                RateDAO dao = daoFactory.gRateDAO();
                out.println(dao.find(date));

            }

            // contains date and currency
            else if (request.getParameterMap().containsKey("currency")) {

                String date = request.getParameter("date");
                String curency = request.getParameter("currency");

                RateDAO dao = daoFactory.gRateDAO();
                out.println(dao.findByDateCurrency(date, curency));
            }
            // does not contain date or currency
            else {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDateTime now = LocalDateTime.now();
                LocalDate currentDate = now.toLocalDate();

                System.out.println("Todays date --------------> " + currentDate.toString());

                RateDAO dao = daoFactory.gRateDAO();
                out.println(dao.find(currentDate.toString()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
