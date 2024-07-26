package com.example.springbootfileupload.controller;

import com.example.springbootfileupload.entity.TempTableEntry;
import com.example.springbootfileupload.repository.TempTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MapController {

    @Autowired
    private TempTableRepository tempTableRepository;

    @GetMapping(value = "/entries", produces = "text/csv")
    public ResponseEntity<byte[]> getAllEntriesAsCsv() {
        try {
            List<TempTableEntry> entries = tempTableRepository.findAll();

            // Convert entries to CSV format
            StringBuilder csvBuilder = new StringBuilder();
            csvBuilder.append("poleNumber,area,district,city,state,latitude,longitude,assignedUser\n");
            for (TempTableEntry entry : entries) {
                csvBuilder.append(String.format("%s,%s,%s,%s,%s,%.6f,%.6f,%s\n",
                        entry.getPoleNumber(),
                        entry.getArea(),
                        entry.getDistrict(),
                        entry.getCity(),
                        entry.getState(),
                        entry.getLatitude(),
                        entry.getLongitude(),
                        entry.getAssignedUser()));
            }

            // Convert CSV string to byte array
            byte[] csvBytes = csvBuilder.toString().getBytes();

            // Set HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentLength(csvBytes.length);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=entries.csv");

            return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}