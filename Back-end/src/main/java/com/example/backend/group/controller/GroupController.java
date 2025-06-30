package com.example.backend.group.controller;

import com.example.backend.group.dto.request.GroupRequest;
import com.example.backend.group.dto.response.GroupResponse;
import com.example.backend.group.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
@Tag(name = "GroupAPI", description = "그룹 관련 API.")
public class GroupController {

    private final GroupService groupService;

    @PostMapping("/create")
    @Operation(summary = "그룹 생성", description = "새로운 그룹을 생성하는 API.")
    public ResponseEntity<Void> groupCreate(@RequestBody GroupRequest.groupCreateRequest request) {
        groupService.create(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    @Operation(summary = "그룹 목록 조회", description = "사용자가 속한 그룹 목록 조회")
    public ResponseEntity<List<GroupResponse.groupUserResponse>> groupList() {
        List<GroupResponse.groupUserResponse> groups = groupService.groupList();
        return ResponseEntity.ok(groups);
    }

    @PutMapping("/{groupId}/member/{userId}")
    @Operation(summary = "그룹 인원 추가", description = "그룹에 새로운 유저를 추가하는 API")
    public ResponseEntity<Void> addMemberToGroup(@PathVariable UUID groupId, @PathVariable UUID userId) {
        groupService.addMember(groupId, userId);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/{groupId}/member/{userId}")
    @Operation(summary = "그룹 인원 삭제", description = "사용자가 속한 그룹에 인원 삭제")
    public ResponseEntity<Void> removeMemberToGroup(@PathVariable UUID groupId, @PathVariable UUID userId) {
        groupService.removeMember(groupId, userId);
        return ResponseEntity.status(204).build();
    }

    @DeleteMapping("/{groupId}")
    @Operation(summary = "그룹 삭제", description = "사용자가 속한 그룹 삭제")
    public ResponseEntity<Void> deleteGroup(@PathVariable UUID groupId) {
        groupService.removeGroup(groupId);
        return ResponseEntity.status(204).build();
    }

    @GetMapping("/count/{groupId}")
    @Operation(summary = "그룹 인원 카운팅", description = "사용자가 속한 그룹 인원 수 체크")
    public ResponseEntity<GroupResponse.groupCountingResponse> countGroup(@PathVariable UUID groupId) {
        GroupResponse.groupCountingResponse response = groupService.countGroup(groupId);
        return ResponseEntity.ok(response);
    }
}
