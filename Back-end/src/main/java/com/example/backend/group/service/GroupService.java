package com.example.backend.group.service;

import com.example.backend.group.dto.request.GroupRequest;
import com.example.backend.group.dto.response.GroupResponse;
import com.example.backend.group.entity.Group;
import com.example.backend.group.repository.GroupRepository;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.common.auth.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    // 1️⃣ 그룹 생성
    @Transactional
    public void create(GroupRequest.groupCreateRequest request) {
        UUID currentUserId = AuthUtil.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Group group = Group.builder()
                .groupName(request.getGroupName())
                .build();

        group.getUsers().add(currentUser);

        groupRepository.save(group);
    }

    // 2️⃣ 그룹 목록 조회
    @Transactional
    public List<GroupResponse.groupUserResponse> groupList() {
        UUID currentUserId = AuthUtil.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<Group> groups = groupRepository.findByUsersContaining(currentUser);

        return groups.stream()
                .map(group -> GroupResponse.groupUserResponse.builder()
                        .groupId(group.getGroupId())
                        .groupName(group.getGroupName())
                        .createdAt(group.getCreatedAt())
                        .users(
                                group.getUsers().stream()
                                        .map(user -> GroupResponse.groupUser.builder()
                                                .userId(user.getUserId())
                                                .userName(user.getUserName())
                                                .build())
                                        .collect(Collectors.toSet())
                        )
                        .build())
                .collect(Collectors.toList());
    }

    // 3️⃣ 그룹 인원 추가
    @Transactional
    public void addMember(UUID groupId, UUID userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if(group.getUsers().contains(user)) {
            throw new IllegalStateException("사용자가 이미 그룹에 속해 있습니다.");
        }
        group.getUsers().add(user);
    }

    // 4️⃣ 그룹 인원 삭제
    @Transactional
    public void removeMember(UUID groupId, UUID userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!group.getUsers().contains(user)) {
            throw new IllegalStateException("사용자가 그룹에 속해 있지 않습니다.");
        }
        group.getUsers().remove(user);
    }

    // 5️⃣ 그룹 삭제
    @Transactional
    public void removeGroup(UUID groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));
        groupRepository.delete(group);
    }

    // 6️⃣ 그룹 인원 카운팅
    @Transactional
    public GroupResponse.groupCountingResponse countGroup(UUID groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        return GroupResponse.groupCountingResponse.builder()
                .groupId(group.getGroupId())
                .memberCount(group.getUsers().size())
                .build();
    }
}
