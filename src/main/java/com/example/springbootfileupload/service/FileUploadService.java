package com.example.springbootfileupload.service;

import com.example.springbootfileupload.entity.TempTableEntry;
import com.example.springbootfileupload.entity.UploadedFile;
import com.example.springbootfileupload.entity.Users;
import com.example.springbootfileupload.repository.TempTableRepository;
import com.example.springbootfileupload.repository.UploadedFileRepository;
import com.example.springbootfileupload.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FileUploadService {

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @Autowired
    private TempTableRepository tempTableRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String UPLOAD_DIR = "./uploads/";

    @Transactional(rollbackFor = Exception.class)
    public UploadedFile uploadFile(MultipartFile multipartFile, String originalFileName) throws IOException {
        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setOriginalFileName(originalFileName);

        List<TempTableEntry> tempTableEntries;
        try {
            tempTableEntries = convertExcelToTempTableEntries(multipartFile);
        } catch (IOException e) {
            uploadedFile.setTimestampFileName("");
            uploadedFile.setStatus("Failed: Error processing Excel file.");
            uploadedFileRepository.save(uploadedFile);
            return uploadedFile;
        }

        String timestampFileName = generateTimestampFileName();
        uploadedFile.setTimestampFileName(timestampFileName);
        uploadedFile.setStatus("File uploaded and converted to CSV successfully");

        uploadedFile = uploadedFileRepository.save(uploadedFile);

        saveTempTableEntries(tempTableEntries, uploadedFile);

        saveEntriesToCsv(tempTableEntries, timestampFileName);

        return uploadedFile;
    }

    @Transactional
    public void assignEntryToUser(Long entryId, String username) {
        Optional<TempTableEntry> optionalEntry = tempTableRepository.findById(entryId);
        if (optionalEntry.isPresent()) {
            TempTableEntry entry = optionalEntry.get();
            entry.setAssignedUser(username);
            tempTableRepository.save(entry);
        } else {
            throw new IllegalArgumentException("Entry not found with ID: " + entryId);
        }
    }

    private String generateTimestampFileName() {
        return LocalDateTime.now().toString().replace(":", "-") + ".csv";
    }

    private List<TempTableEntry> convertExcelToTempTableEntries(MultipartFile multipartFile) throws IOException {
        Workbook workbook = WorkbookFactory.create(multipartFile.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        List<TempTableEntry> entries = new ArrayList<>();
        Row headerRow = sheet.getRow(0);

        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row != null) {
                TempTableEntry entry = new TempTableEntry();
                entry.setPoleNumber(getStringValueFromCell(row.getCell(0)));
                entry.setArea(getStringValueFromCell(row.getCell(1)));
                entry.setDistrict(getStringValueFromCell(row.getCell(2)));
                entry.setCity(getStringValueFromCell(row.getCell(3)));
                entry.setState(getStringValueFromCell(row.getCell(4)));
                entry.setLatitude(getNumericValueFromCell(row.getCell(5)));
                entry.setLongitude(getNumericValueFromCell(row.getCell(6)));

                entries.add(entry);
            }
        }

        workbook.close();

        return entries;
    }

    private String getStringValueFromCell(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue()).trim();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue()).trim();
            default:
                return "";
        }
    }

    private double getNumericValueFromCell(Cell cell) {
        if (cell == null) {
            return 0.0;
        }
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            default:
                return 0.0;
        }
    }

    private void saveTempTableEntries(List<TempTableEntry> tempTableEntries, UploadedFile uploadedFile) {
        for (TempTableEntry entry : tempTableEntries) {
            entry.setUploadedFile(uploadedFile);
            tempTableRepository.save(entry);
        }
    }

    private void saveEntriesToCsv(List<TempTableEntry> tempTableEntries, String fileName) throws IOException {
        File csvFile = new File(UPLOAD_DIR + fileName);
        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFile))) {
            writer.println("Pole Number, Area, District, City, State, Latitude, Longitude"); // Header

            for (TempTableEntry entry : tempTableEntries) {
                writer.println(String.format("%s, %s, %s, %s, %s, %.6f, %.6f",
                        entry.getPoleNumber(), entry.getArea(), entry.getDistrict(), entry.getCity(),
                        entry.getState(), entry.getLatitude(), entry.getLongitude()));
            }
        }
    }

    @Transactional
    public List<UploadedFile> getAllFiles() {
        return uploadedFileRepository.findAll();
    }

    @Transactional
    public List<UploadedFile> getAllFilesForCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        if (isAdmin(currentPrincipalName)) {
            return uploadedFileRepository.findAll();
        } else {
            return fetchAssignedFilesForUser(currentPrincipalName);
        }
    }

    private List<UploadedFile> fetchAssignedFilesForUser(String username) {
        List<TempTableEntry> assignedEntries = tempTableRepository.findByAssignedUser(username);
        List<Long> fileIds = assignedEntries.stream()
                .map(entry -> entry.getUploadedFile().getId())
                .distinct()
                .collect(Collectors.toList());

        return uploadedFileRepository.findAllById(fileIds);
    }

    private boolean isAdmin(String username) {
        return username.equals("admin"); // Example: check if username is "admin"
    }

    @Transactional
    public List<TempTableEntry> getTempTableEntriesByFileId(Long fileId) {
        return tempTableRepository.findByUploadedFileId(fileId);
    }

    @Transactional
    public UploadedFile getFileById(Long id) {
        Optional<UploadedFile> file = uploadedFileRepository.findById(id);
        return file.orElseThrow(() -> new IllegalArgumentException("Invalid file ID"));
    }

    @Transactional
    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public List<TempTableEntry> getAssignedEntriesByUsername(String username) {
        return tempTableRepository.findByAssignedUser(username);
    }
    


}
