/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lib.CassandraHosts;
import models.StudentModel;

import com.datastax.driver.core.Cluster;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;
import stores.LoggedIn;

/**
 *
 * @author Luke
 */
@WebServlet(name = "Register", urlPatterns = {"/Register"})
public class Register extends HttpServlet {

	private static final long serialVersionUID = 1L;
	Cluster cluster=null;
    public void init(ServletConfig config) throws ServletException {
        cluster = CassandraHosts.getCluster();
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher rd = request.getRequestDispatcher("/Register.jsp");
        rd.forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username=request.getParameter("username");
        String password=request.getParameter("password");
        String name=request.getParameter("name");
        String surname=request.getParameter("surname");
      
        
        if (username.equals(""))
        {
        	error("Please enter a username", response);
        	return;
        }
        else if (password.equals(""))
        {
        	error("Please enter your password", response);
            return;
        }
        else if (name.equals(""))
        {
        	error("Please enter your first name", response);
            return;
        }
        else if (surname.equals(""))
        {
        	error("Please enter your second name", response);
            return;
        }
        
        StudentModel st = new StudentModel();
        st.setCluster(cluster);
        
        boolean studentExists = st.existingStudent(username);
        
       if (studentExists == true)
        {
        	response.sendRedirect("/SaveTheSemester");
        }
        else
        {
            boolean success = st.RegisterStudent(username, password, name, surname);
            
            if (success){
                HttpSession session=request.getSession();
                LoggedIn lg= new LoggedIn();
                lg.setLoggedin();
                lg.setUsername(username);

                session.setAttribute("LoggedIn", lg);
            }
            
            response.sendRedirect("/SaveTheSemester");
        }  
    }
    
    private void error(String fault, HttpServletResponse response) throws ServletException, IOException
    {
    	 PrintWriter out = new PrintWriter(response.getOutputStream());
    	 out.println("<h1>You have made a mistake, please try again</h1>");
    	 out.close();
    }

}