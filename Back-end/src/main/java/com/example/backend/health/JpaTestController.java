package com.example.backend.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/health/jpa")
@Tag(name = "JpaTestAPI", description = "JPA 설정 테스트 API")
public class JpaTestController {

    private final EntityManager entityManager;

    @GetMapping("/connection-test")
    @Operation(summary = "데이터베이스 연결 테스트", description = "데이터베이스 연결 상태를 확인합니다")
    public ResponseEntity<Map<String, Object>> testDatabaseConnection() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 간단한 쿼리로 연결 테스트
            Query query = entityManager.createNativeQuery("SELECT 1");
            Object result = query.getSingleResult();

            response.put("connected", true);
            response.put("message", "데이터베이스 연결 성공");
            response.put("testQuery", "SELECT 1");
            response.put("result", result);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("connected", false);
            response.put("message", "데이터베이스 연결 실패: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/database-info")
    @Operation(summary = "데이터베이스 정보 조회", description = "현재 연결된 데이터베이스의 정보를 조회합니다")
    public ResponseEntity<Map<String, Object>> getDatabaseInfo() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 데이터베이스 버전 정보
            Query versionQuery = entityManager.createNativeQuery("SELECT VERSION()");
            String version = (String) versionQuery.getSingleResult();

            // 현재 데이터베이스명
            Query dbNameQuery = entityManager.createNativeQuery("SELECT DATABASE()");
            String dbName = (String) dbNameQuery.getSingleResult();

            // 현재 시간
            Query timeQuery = entityManager.createNativeQuery("SELECT NOW()");
            Object currentTime = timeQuery.getSingleResult();

            response.put("databaseVersion", version);
            response.put("databaseName", dbName);
            response.put("currentTime", currentTime);
            response.put("message", "데이터베이스 정보 조회 성공");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "데이터베이스 정보 조회 실패: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/tables")
    @Operation(summary = "테이블 목록 조회", description = "현재 데이터베이스의 테이블 목록을 조회합니다")
    public ResponseEntity<Map<String, Object>> getTableList() {
        Map<String, Object> response = new HashMap<>();

        try {
            Query tablesQuery = entityManager.createNativeQuery("SHOW TABLES");
            List<?> tables = tablesQuery.getResultList();

            response.put("tables", tables);
            response.put("tableCount", tables.size());
            response.put("message", "테이블 목록 조회 성공");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "테이블 목록 조회 실패: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/user-table-check")
    @Operation(summary = "User 테이블 존재 확인", description = "User 엔티티의 테이블이 생성되었는지 확인합니다")
    public ResponseEntity<Map<String, Object>> checkUserTable() {
        Map<String, Object> response = new HashMap<>();

        try {
            // User 테이블 구조 확인
            Query describeQuery = entityManager.createNativeQuery("DESCRIBE user");
            List<?> tableStructure = describeQuery.getResultList();

            // User 테이블 레코드 수 확인
            Query countQuery = entityManager.createNativeQuery("SELECT COUNT(*) FROM user");
            Object recordCount = countQuery.getSingleResult();

            response.put("tableExists", true);
            response.put("tableStructure", tableStructure);
            response.put("recordCount", recordCount);
            response.put("message", "User 테이블 확인 성공");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("tableExists", false);
            response.put("message", "User 테이블 확인 실패: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/sql-test")
    @Operation(summary = "SQL 실행 테스트", description = "간단한 SQL 쿼리를 실행하여 JPA 동작을 테스트합니다")
    public ResponseEntity<Map<String, Object>> testSqlExecution() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 현재 시간 조회 (별칭 수정)
            Query timeQuery = entityManager.createNativeQuery("SELECT NOW() as current_datetime, 'JPA SQL Test' as test_message");
            Object[] result = (Object[]) timeQuery.getSingleResult();

            // 간단한 계산
            Query mathQuery = entityManager.createNativeQuery("SELECT 1+1 as calculation");
            Object mathResult = mathQuery.getSingleResult();

            response.put("currentTime", result[0]);
            response.put("testMessage", result[1]);
            response.put("calculation", mathResult);
            response.put("message", "SQL 실행 테스트 성공");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "SQL 실행 테스트 실패: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/test-transaction")
    @Operation(summary = "트랜잭션 테스트", description = "JPA 트랜잭션이 올바르게 작동하는지 테스트합니다")
    public ResponseEntity<Map<String, Object>> testTransaction() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 트랜잭션 상태 확인
            Query transactionQuery = entityManager.createNativeQuery("SELECT @@autocommit");
            Object autoCommit = transactionQuery.getSingleResult();

            response.put("autoCommit", autoCommit);
            response.put("entityManagerOpen", entityManager.isOpen());
            response.put("message", "트랜잭션 테스트 성공");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "트랜잭션 테스트 실패: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/hibernate-info")
    @Operation(summary = "Hibernate 정보 조회", description = "Hibernate 설정 정보를 조회합니다")
    public ResponseEntity<Map<String, Object>> getHibernateInfo() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Hibernate 버전 정보
            response.put("hibernateVersion", org.hibernate.Version.getVersionString());
            response.put("entityManagerFactory", entityManager.getEntityManagerFactory().getClass().getSimpleName());
            response.put("message", "Hibernate 정보 조회 성공");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Hibernate 정보 조회 실패: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/entity-metadata")
    @Operation(summary = "엔티티 메타데이터 조회", description = "등록된 JPA 엔티티들의 메타데이터를 조회합니다")
    public ResponseEntity<Map<String, Object>> getEntityMetadata() {
        Map<String, Object> response = new HashMap<>();

        try {
            var metamodel = entityManager.getMetamodel();
            var entities = metamodel.getEntities();

            Map<String, Object> entityInfo = new HashMap<>();
            entities.forEach(entityType -> {
                String entityName = entityType.getName();
                String javaType = entityType.getJavaType().getSimpleName();
                entityInfo.put(entityName, javaType);
            });

            response.put("entityCount", entities.size());
            response.put("entities", entityInfo);
            response.put("message", "엔티티 메타데이터 조회 성공");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "엔티티 메타데이터 조회 실패: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }
}