package com.powerreliability.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.system.entity.SysDept;
import com.powerreliability.system.mapper.SysDeptMapper;
import com.powerreliability.system.service.ISysDeptService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {

    @Override
    public List<SysDept> buildTree(List<SysDept> depts) {
        if (depts == null || depts.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, List<SysDept>> parentMap = depts.stream()
                .collect(Collectors.groupingBy(d -> d.getParentId() != null ? d.getParentId() : 0L));

        for (SysDept dept : depts) {
            Long id = dept.getId();
            if (parentMap.containsKey(id)) {
                List<SysDept> children = parentMap.get(id);
                children.sort(Comparator.comparingInt(SysDept::getSortOrder));
                dept.setChildren(children);
            } else {
                dept.setChildren(new ArrayList<>());
            }
        }

        return depts.stream()
                .filter(d -> d.getParentId() == null || d.getParentId() == 0L)
                .sorted(Comparator.comparingInt(SysDept::getSortOrder))
                .collect(Collectors.toList());
    }
}
