package com.xeroKarao.demo.repository;

import com.xeroKarao.demo.model.FileData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepo extends JpaRepository<FileData, Integer> {
}
