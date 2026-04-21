package com.github.deniskoriavets.sportvision.service;


import com.github.deniskoriavets.sportvision.dto.request.GroupRequest;
import com.github.deniskoriavets.sportvision.dto.response.GroupResponse;
import com.github.deniskoriavets.sportvision.dto.criteria.GroupSearchCriteria;
import com.github.deniskoriavets.sportvision.exception.ResourceNotFoundException;
import com.github.deniskoriavets.sportvision.mapper.GroupMapper;
import com.github.deniskoriavets.sportvision.repository.ChildRepository;
import com.github.deniskoriavets.sportvision.repository.GroupRepository;
import com.github.deniskoriavets.sportvision.repository.ParentRepository;
import com.github.deniskoriavets.sportvision.repository.SectionRepository;
import com.github.deniskoriavets.sportvision.repository.specification.GroupSpecifications;
import com.github.deniskoriavets.sportvision.service.interfaces.GroupService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;

    private final GroupMapper groupMapper;

    private final SectionRepository sectionRepository;

    private final ParentRepository parentRepository;

    private final ChildRepository childRepository;

    @Override
    @Transactional
    public GroupResponse createGroup(GroupRequest groupRequest) {
        var section = sectionRepository.findById(groupRequest.sectionId())
            .orElseThrow(() -> new ResourceNotFoundException("Section not found"));

        var entity = groupMapper.toEntity(groupRequest);
        entity.setSection(section);

        if (groupRequest.coachId() != null) {
            var coach = parentRepository.findById(groupRequest.coachId())
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found"));
            entity.setCoach(coach);
        }

        return groupMapper.toResponseWithOccupancy(groupRepository.save(entity), 0);
    }

    @Override
    public Page<GroupResponse> getGroups(GroupSearchCriteria criteria, Pageable pageable) {
        var spec = GroupSpecifications.build(criteria);
        return groupRepository.findAll(spec, pageable)
            .map(groupMapper::toResponse);
    }

    @Override
    public GroupResponse getGroupById(UUID id) {
        var group = groupRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + id));
        return groupMapper.toResponse(group);
    }

    @Override
    @Transactional
    public GroupResponse updateGroup(UUID id, GroupRequest groupRequest) {
        var entity = groupRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + id));

        entity.setName(groupRequest.name());
        entity.setMaxCapacity(groupRequest.maxCapacity());
        entity.setAgeMin(groupRequest.ageMin());
        entity.setAgeMax(groupRequest.ageMax());

        if (!groupRequest.sectionId().equals(entity.getSection().getId())) {
            var newSection = sectionRepository.findById(groupRequest.sectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));
            entity.setSection(newSection);
        }

        if (groupRequest.coachId() != null) {
            if (entity.getCoach() == null ||
                !groupRequest.coachId().equals(entity.getCoach().getId())) {
                var newCoach = parentRepository.findById(groupRequest.coachId())
                    .orElseThrow(() -> new ResourceNotFoundException("Coach (Parent) not found"));
                entity.setCoach(newCoach);
            }
        } else {
            entity.setCoach(null);
        }

        var savedGroup = groupRepository.save(entity);
        return groupMapper.toResponse(savedGroup);
    }

    @Override
    @Transactional
    public void deleteGroup(UUID id) {
        var group = groupRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + id));
        groupRepository.delete(group);
    }
}
