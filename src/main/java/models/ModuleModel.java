/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.UserType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import stores.*;
import java.util.Map;
import lib.Convertors;

/**
 *
 * @author peterbennington
 */

public class ModuleModel {
 
    Cluster cluster;

    public ModuleModel() {

    }
    
    public Set<Module> getStudentModules(String user){
        Set<Module> studentModules = new HashSet();
        
        Session session = cluster.connect("savethesemester");
        PreparedStatement psModules = session.prepare("select modulecode from modules where username = ?");
        BoundStatement bsModules = new BoundStatement(psModules);
        ResultSet rs = session.execute(bsModules.bind(user));
        
        if (rs.isExhausted()) {
            System.out.println("No modules found for student: " + user);
            return null;
        }
        else {
            for (Row row : rs){
                Module module = getModule(user, row.getString("modulecode"));
                studentModules.add(module);
            }
        }
        
        return studentModules;
    }
    
    public Module getModule(String user, String moduleCode){
        Session session = cluster.connect("savethesemester");
        PreparedStatement psModules = session.prepare("select modulename, startdate, examdate from modules where username = ? AND modulecode = ?");
        BoundStatement bsModules = new BoundStatement(psModules);
        ResultSet rs = session.execute(bsModules.bind(user, moduleCode));
        
        Module module = null;
        
        if (rs.isExhausted()) {
            System.out.println("No module found for " + user + " - " + moduleCode);
            return null;
        }
        else {
            for (Row row : rs){
                module = new Module();
                
                module.setUsername(user);
                module.setModuleCode(moduleCode);
                module.setModuleName(row.getString("modulename"));
                module.setStartDate(row.getDate("startdate"));
                module.setExamDate(row.getDate("examdate"));
                
                Set<ModuleFile> moduleFiles = getModuleFiles(user, moduleCode);
                
                module.setFiles(moduleFiles);
            }
        }
        
        return module;
    }
    
        public Set<Deliverable> getDeliverables(String user, String ModuleCode){
        Set<Deliverable> deliverables = new HashSet();
        
        Session session = cluster.connect("savethesemester");
        PreparedStatement psDeliverables = session.prepare("select deliverableid from deliverables where username = ? AND modulecode = ?");
        BoundStatement bsDeliverables = new BoundStatement(psDeliverables);
        ResultSet rs = session.execute(bsDeliverables.bind(user, ModuleCode));
        
        if (rs.isExhausted()) {
            System.out.println("No modules found for student: " + user);
            return null;
        }
        else {
            for (Row row : rs){
                Deliverable deliverable = getDeliverable(user, ModuleCode, row.getUUID("deliverableid"));
                deliverables.add(deliverable);
            }
        }
        
        return deliverables;
    }
    
    public Deliverable getDeliverable(String username, String moduleCode, UUID deliverableID) {
        Session session = cluster.connect("savethesemester");
        PreparedStatement psDeliverables = session.prepare("select deliverablename,duedate,percentageworth,percentageachieved from deliverables where username = ? AND modulecode = ? AND deliverableid =?");
        BoundStatement bsDeliverables = new BoundStatement(psDeliverables);
        ResultSet rs = session.execute(bsDeliverables.bind(username, moduleCode,deliverableID));
        
        Deliverable deliverable = null;
        
        if (rs.isExhausted()) {
            System.out.println("No deliverable found for " + username + " - " + moduleCode + " - " + deliverableID);
            return null;
        }
        else {
            for (Row row : rs){
                deliverable = new Deliverable();
                
                deliverable.setUsername(username);
                deliverable.setModuleCode(moduleCode);
                deliverable.setDeliverableName(row.getString("deliverablename"));
                deliverable.setDueDate(row.getDate("duedate"));
                deliverable.setPercentageWorth(row.getDouble("percentageWorth"));
                deliverable.setPercentageAchieved(row.getDouble("percentageAchieved"));
            }
        }
        
        return deliverable;
    }
    
    public Set<ModuleFile> getModuleFiles(String user, String moduleCode){
        Session session = cluster.connect("savethesemester");
        PreparedStatement psModuleFiles = session.prepare("select files from modules where username = ? AND modulecode = ?");
        BoundStatement bsModuleFiles = new BoundStatement(psModuleFiles);
        ResultSet rs = session.execute(bsModuleFiles.bind(user, moduleCode));
        
        Set<ModuleFile> moduleFiles = null;

        if (rs.isExhausted()) {
            System.out.println("No files found for " + user + " - " + moduleCode);
            return null;
        }
        else {            
            for (Row row : rs){
                Map<UUID, UDTValue> files = row.getMap("files", UUID.class, UDTValue.class);
                Iterator iterator = files.entrySet().iterator();
                
                moduleFiles = new HashSet<>();

                while (iterator.hasNext()){
                    Map.Entry file = (Map.Entry) iterator.next();
                    UUID fileID = (UUID) file.getKey();
                    
                    ModuleFile moduleFile = getModuleFile(user, moduleCode, fileID);                    
                    moduleFiles.add(moduleFile);
                }
            }
        }
        
        return moduleFiles;
    }
    
    public ModuleFile getModuleFile(String user, String moduleCode, UUID fileID){
        Session session = cluster.connect("savethesemester");
        PreparedStatement psModuleFile = session.prepare("select files from modules where username = ? AND modulecode = ?");
        BoundStatement bsModuleFile = new BoundStatement(psModuleFile);
        ResultSet rs = session.execute(bsModuleFile.bind(user, moduleCode));
        
        ModuleFile moduleFile = null;
        
        if (rs.isExhausted()) {
            System.out.println("No files found for " + user + " - " + moduleCode);
            return null;
        }
        else {
            Map<UUID, UDTValue> files = rs.one().getMap("files", UUID.class, UDTValue.class);
            
            if (files.containsKey(fileID)){
                moduleFile = new ModuleFile();
                
                UDTValue fileInfo = (UDTValue) files.get(fileID);
                String fileName = fileInfo.getString("filename");
                String fileType = fileInfo.getString("filetype");
                int numPages = fileInfo.getInt("numpages");
                boolean completed = fileInfo.getBool("completed");
                Date dateCompleted = fileInfo.getDate("datecompleted");
                
                moduleFile.setFileID(fileID);
                moduleFile.setFileName(fileName);
                moduleFile.setFileType(fileType);
                moduleFile.setNumPages(numPages);
                moduleFile.setCompleted(completed);
                moduleFile.setDateCompleted(dateCompleted);
            }
        }
        
        return moduleFile;
    }
    
    public void setFileComplete(UUID fileID, boolean completed, String username, String moduleCode){
        ModuleFile moduleFile = getModuleFile(username, moduleCode, fileID);
        
        UserType fileUDT = cluster.getMetadata().getKeyspace("savethesemester").getUserType("file");
        
        UDTValue file;
        if (completed){
            Date date = new Date();
            file = fileUDT.newValue()
                    .setString("filename", moduleFile.getFileName())
                    .setString("filetype", moduleFile.getFileType())
                    .setInt("numpages", moduleFile.getNumPages())
                    .setBool("completed", completed)
                    .setDate("datecompleted", date);
        }
        else {
            file = fileUDT.newValue()
                    .setString("filename", moduleFile.getFileName())
                    .setString("filetype", moduleFile.getFileType())
                    .setInt("numpages", moduleFile.getNumPages())
                    .setBool("completed", completed);
        }
        
        Session session = cluster.connect("savethesemester");
        PreparedStatement ps = session.prepare("update modules set files[?] = ? where username = ? and modulecode = ?");
        BoundStatement bs = new BoundStatement(ps);
        session.execute(bs.bind(fileID, file, username, moduleCode));
    }
    
    public boolean addModule(String moduleCode, String moduleName, String startDate, String examDate, String username) throws ParseException {         
        Session session = cluster.connect("savethesemester");
        // if statement checks if the modulecode has been taken, in which case display error message
        if (moduleExists(username, moduleCode)) {
            return false;
        }
        // here the 2 date strings are parsed into the date format
        Date sDate = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
        Date eDate = new SimpleDateFormat("yyyy-MM-dd").parse(examDate);
        PreparedStatement ps = session.prepare("insert into modules (moduleCode, moduleName, startDate, examDate, dateAdded, username) Values(?,?,?,?,?,?)");
        BoundStatement boundStatement = new BoundStatement(ps);
        Date dateAdded = new Date();
        session.execute(boundStatement.bind(moduleCode, moduleName, sDate, eDate, dateAdded, username));
        return true;
    }
    
    //this boolean method will check the if the module trying to be added already exists
    private boolean moduleExists(String username, String modulecode) {
    
        Session session = cluster.connect("savethesemester");
        PreparedStatement ps = session.prepare("select modulecode from modules where username =? and modulecode = ? ALLOW FILTERING");
        BoundStatement boundState = new BoundStatement(ps);
        ResultSet rs = null;
        rs = session.execute(boundState.bind(username, modulecode));
        if (rs.isExhausted()==true) {
            System.out.println("This module doesn't exist.");
            return false;
        } else {
                    return true;
                }
        }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public boolean addDeliverable(String moduleCode, String deliverableName, String dueDate, double perWorth, double perAchieved, String username) throws ParseException {
        Session session = cluster.connect("savethesemester");    

        Convertors convertor = new Convertors();
        java.util.UUID deliverableID = convertor.getTimeUUID();
 
        // here the 2 date strings are parsed into the date format
        Date dDate = new SimpleDateFormat("yyyy-MM-dd").parse(dueDate);
        PreparedStatement ps = session.prepare("insert into deliverables (deliverableID, moduleCode, deliverableName, dueDate, percentageWorth, percentageAchieved, dateAdded, username) Values(?,?,?,?,?,?,?,?)");
        BoundStatement boundStatement = new BoundStatement(ps);
        Date dateAdded = new Date();
        session.execute(boundStatement.bind(deliverableID, moduleCode, deliverableName, dDate, perWorth, perAchieved, dateAdded, username));
        return true;
    }  
       
        

    public boolean addFile(String fileName, String fileType, String numPages, String username, String modulecode ){
        Session session = cluster.connect("savethesemester");
        Convertors convertor = new Convertors();
        java.util.UUID fileID = convertor.getTimeUUID();
        
        
        if (moduleNotAdd(modulecode, username)){
        System.out.println("please add module");
        return false;
        }
        else 
        {
        int noPages = Integer.valueOf(numPages);
         UserType fileUDT = cluster.getMetadata().getKeyspace("savethesemester").getUserType("file");
         UDTValue newfile = fileUDT.newValue()

                .setString("filename", fileName)
                 .setString("filetype", fileType)

                .setInt("numpages", noPages)
                .setBool("completed", false);
         

        Map<UUID, UDTValue> file = new HashMap();
        file.put(fileID, newfile);
        PreparedStatement pst = session.prepare("UPDATE modules SET files = files + ? where username = ? AND modulecode = ?");
        System.out.println("File has been added!");
        BoundStatement boundStatement = new BoundStatement(pst);
        session.execute(boundStatement.bind(file, username, modulecode));
         return true;
        }
     }  
    
     public boolean moduleNotAdd(String modulecode, String username)
     {
         Session session = cluster.connect("savethesemester"); 
         PreparedStatement ps = session.prepare("select modulecode, username from modules where modulecode =? AND username = ? ALLOW FILTERING");
         BoundStatement boundState = new BoundStatement(ps);
         ResultSet rs = session.execute(boundState.bind(modulecode, username));
         if(rs.isExhausted() == true)
         {
             System.out.println("result set is empty");
             return true; 
         }
         else
         {
             System.out.println(rs.toString());
             return false;
         }
        
     }   
 }

