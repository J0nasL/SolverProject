package Model;

/**
 * This class is no longer in use
 */
@Deprecated
public class MenuSchedule extends ModelObject {
    private CronTab schedule;

    public MenuSchedule(String id, CronTab schedule) {
        super(id, schedule.toString());
        this.schedule = schedule;
    }

    /**
     * Whether the current schedule is in use
     *
     * @return true when the schedule is the one being used
     */
    public boolean isActive() {
        return schedule.isActive();
    }

    @Override
    public String toString() {
        return "MenuSchedule(schedule:" + schedule.toString() + ", " + super.toString() + ")";
    }


}
