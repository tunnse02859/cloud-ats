package org.ats.services.schedule;

import com.google.inject.assistedinject.Assisted;

public interface ScheduleFactory {
  public Schedule create( @Assisted("name") String name, @Assisted("project_id") String project_id);
}
