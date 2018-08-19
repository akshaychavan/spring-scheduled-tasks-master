package com.lankydan.event.repository;

import org.springframework.data.repository.CrudRepository;

import com.lankydan.event.model.SchedularJob;

public interface SchedularJobRepository extends CrudRepository<SchedularJob, Long> {
}
