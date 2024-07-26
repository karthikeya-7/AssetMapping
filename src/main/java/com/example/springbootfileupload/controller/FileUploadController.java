package com.example.springbootfileupload.controller;

import com.example.springbootfileupload.entity.TempTableEntry;
import com.example.springbootfileupload.entity.UploadedFile;
import com.example.springbootfileupload.entity.Users;
import com.example.springbootfileupload.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @GetMapping
    public String index(Model model) {
        List<UploadedFile> files = fileUploadService.getAllFilesForCurrentUser();
        model.addAttribute("files", files);
        return "index";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) {
        try {
            UploadedFile uploadedFile = fileUploadService.uploadFile(file, file.getOriginalFilename());
            if (uploadedFile.getStatus().startsWith("Failed")) {
                model.addAttribute("error", uploadedFile.getStatus());
            } else {
                model.addAttribute("uploadFile", uploadedFile);
                model.addAttribute("files", fileUploadService.getAllFilesForCurrentUser());
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error uploading file: " + e.getMessage());
        }
        return "index";
    }

    @GetMapping("/view/{id}")
    public String viewFileContent(@PathVariable Long id, Model model) {
        try {
            List<TempTableEntry> tempTableEntries = fileUploadService.getTempTableEntriesByFileId(id);
            List<Users> users = fileUploadService.getAllUsers();

            if (tempTableEntries.isEmpty()) {
                model.addAttribute("error", "No data found for this file.");
                return "index"; // or handle differently
            }

            model.addAttribute("tempTableEntries", tempTableEntries);
            model.addAttribute("files", fileUploadService.getAllFilesForCurrentUser());
            model.addAttribute("users", users);

            return "view";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error viewing file: " + e.getMessage());
            return "index"; // or handle error appropriately
        }
    }

    @GetMapping("/view-users")
    public String viewUsers(Model model) {
        List<Users> users = fileUploadService.getAllUsers();
        model.addAttribute("users", users);
        return "view-users";
    }

    @PostMapping("/assign/{id}")
    public String assignEntryToUser(@PathVariable Long id, @RequestParam("username") String username, Model model) {
        try {
            fileUploadService.assignEntryToUser(id, username);
            return "redirect:/view/" + id;
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error assigning entry: " + e.getMessage());
            return "index"; // or handle error appropriately
        }
    }
    
    @GetMapping("/assigned-tasks")
    public String getAssignedTasks(Model model, @AuthenticationPrincipal Users currentUser) {
        try {
            String username = currentUser.getUsername();
            List<TempTableEntry> assignedEntries = fileUploadService.getAssignedEntriesByUsername(username);
            model.addAttribute("assignedEntries", assignedEntries);
            return "assigned-tasks";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error fetching assigned tasks: " + e.getMessage());
            return "error"; // Handle error appropriately, e.g., show an error page
        }
    }

    @GetMapping("/view-map")
    public String viewMap() {
        return "view-map";
    }

}
