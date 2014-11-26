<%-- 
    Document   : ExamPlanner
    Created on : 25-Nov-2014, 15:28:52
    Author     : Tom
--%>

<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.GregorianCalendar"%>
<%@page import="java.util.Calendar"%>
<%@page import="stores.Module"%>
<%@page import="java.util.Iterator"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Exam Planner</title>
    </head>
    <body>
        <h1>Exam Planner</h1>
        <% Set<Module> modules = (Set<Module>) request.getAttribute("modules"); %>
        
        <%
            if (modules != null || !modules.isEmpty()){
        %>
            <table border="1">
            <tr>
                <th>Module Code</th>
                <th>Module Name</th>
                <th>Exam Date</th>
                <th>Days until exam</th>
                <th>Num Files</th>
                <th>Est Files/Day</th>
                <th>Num File Pages</th>
                <th>Est Pages/Day</th>
            </tr>
            
        <%
            Iterator<Module> iterator = modules.iterator();

            while (iterator.hasNext()){
                Module module = iterator.next();
                
                Calendar today = new GregorianCalendar();
                Calendar exam = new GregorianCalendar();
                exam.setTime(module.getExamDate());
                
                SimpleDateFormat formatter = new SimpleDateFormat("EEE dd-MMM-yyyy");
                String examDate = formatter.format(exam.getTime());
                
                final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
                int diffInDays = (int) ((exam.getTimeInMillis() - today.getTimeInMillis()) / DAY_IN_MILLIS);
                
                int numOfFiles = module.getNumOfFiles();
                float numFilesPerDay = numOfFiles / diffInDays;
                
                int numOfFilePages = module.getNumFilePages();
                float numFilePagesPerDay = numOfFilePages / diffInDays;
        %>
                <tr>
                    <td><%=module.getModuleCode()%></td>
                    <td><%=module.getModuleName()%></td>
                    <td><%=examDate%></td>
                    <td><%=diffInDays%></td>
                    <td><%=numOfFiles%></td>
                    <td><%=numFilesPerDay%></td>
                    <td><%=numOfFilePages%></td>
                    <td><%=numFilePagesPerDay%></td>
                </tr>
        <%
            }
        %>
            </table>
        <% }
            else {
        %>      <p>No modules found</p>
        <%
            }
        %>
    </body>
</html>
