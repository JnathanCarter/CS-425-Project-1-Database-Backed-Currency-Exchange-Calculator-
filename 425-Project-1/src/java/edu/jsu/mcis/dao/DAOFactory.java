/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.jsu.mcis.dao;
import java.sql.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 *
 * @author johnc
 */
public class DAOFactory {
     private DataSource ds = null;

    public DAOFactory() {

        try {

            Context envContext = new InitialContext();
            Context initContext = (Context) envContext.lookup("java:/comp/env");
            ds = (DataSource) initContext.lookup("jdbc/db_pool");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    Connection getConnection() {

        Connection c = null;

        try {

            if (ds != null) {
                c = ds.getConnection();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return c;

    }

    public CurrencyDAO getCurrencyDAO(){
        return new CurrencyDAO(this);
    }
    public RateDAO gRateDAO(){
        return new RateDAO(this);
    }
}
