package Model;

/**
 * This class is no longer being used
 * <p>
 * Represents a CronTab object
 * This object is separate from the object hierarchy,
 * and is only used as a property of MenuSchedule
 */
@Deprecated
public class CronTab {

    //TODO: private state

    /**
     * Represents a CronTab object
     *
     * @param cronTab String to parse as a CronTab
     */
    public CronTab(String cronTab) {
        parseCrontab(cronTab);
    }

    private void parseCrontab(String cronTab) {
        //TODO
    }

    /**
     * Checks whether the current time is within the time range of this CronTab
     *
     * @return whether the CronTab is active
     */
    public boolean isActive() {
        //TODO
        return false;
    }

    @Override
    public String toString() {
        //TODO
        return "CronTab(TODO)";
    }
}
