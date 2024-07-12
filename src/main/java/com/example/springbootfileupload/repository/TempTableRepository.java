package com.example.springbootfileupload.repository;

import com.example.springbootfileupload.entity.TempTableEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import javax.transaction.Transactional;

@Repository
public interface TempTableRepository extends JpaRepository<TempTableEntry, Long> {
    List<TempTableEntry> findByUploadedFileId(Long fileId);

    List<TempTableEntry> findByAssignedUser(String username);
    
    @Transactional
    @Modifying
    @Query("UPDATE TempTableEntry t SET t.assignedUser = :username WHERE t.id = :id")
    void assignUserToEntry(@Param("id") Long id, @Param("username") String username);

}
