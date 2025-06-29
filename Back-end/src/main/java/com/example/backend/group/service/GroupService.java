package com.example.backend.group.service;

import com.example.backend.group.dto.request.GroupRequest;
import com.example.backend.group.entity.Group;
import com.example.backend.group.repository.GroupRepository;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.common.auth.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

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
}
