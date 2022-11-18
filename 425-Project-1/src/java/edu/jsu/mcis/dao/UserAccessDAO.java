/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jsu.mcis.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author johnc
 */
public class UserAccessDAO {
        private final DAOFactory daoFactory;
        private final String QUERY_SELECT = "SELECT * FROM user_access WHERE userid = ? AND access_date = ?";

        UserAccessDAO(DAOFactory daoFactory) {
                this.daoFactory = daoFactory;
        }

        public Boolean checkIfAccessLimitReached(int id) {
                int accessLimit = 10;
                Connection conn = daoFactory.getConnection();
                PreparedStatement ps = null;
                ResultSet rs = null;

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDateTime now = LocalDateTime.now();
                LocalDate currentDate = now.toLocalDate();

                try {

                        ps = conn.prepareStatement(QUERY_SELECT);
                        ps.setInt(1, id);
                        ps.setString(2, currentDate.toString());

                        boolean hasresults = ps.execute();

                        if (hasresults) {

                                rs = ps.getResultSet();

                                if (rs.next()) {
                                        if (rs.getInt("access_count") <= accessLimit) {
                                                return false;
                                        }
                                }

                        }

                } catch (Exception e) {
                        e.printStackTrace();
                } finally {

                        if (rs != null) {
                                try {
                                        rs.close();
                                        rs = null;
                                } catch (Exception e) {
                                        e.printStackTrace();
                                }
                        }
                        if (ps != null) {
                                try {
                                        ps.close();
                                        ps = null;
                                } catch (Exception e) {
                                        e.printStackTrace();
                                }
                        }
                        if (conn != null) {
                                try {
                                        conn.close();
                                        conn = null;
                                } catch (Exception e) {
                                        e.printStackTrace();
                                }
                        }

                }
                return true;
        }
        /*
         * 
         * //TODO
         * public void incrementAccessCounter(int id) {
         * 
         * 
         * }
         * 
         */
}
