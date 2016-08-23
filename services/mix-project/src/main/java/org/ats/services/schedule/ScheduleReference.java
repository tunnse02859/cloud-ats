package org.ats.services.schedule;

import org.ats.services.data.common.Reference;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class ScheduleReference extends Reference<Schedule>{
  
  private ScheduleService service;
  
  @Inject
  ScheduleReference(ScheduleService service, @Assisted("id") String id) {
    super(id);
    this.service = service;
  }

  @Override
  public Schedule get() {
    return service.get(id);
  }

  @Override
  public Schedule get(String... mixins) {
    return service.get(id, mixins);
  }
}
