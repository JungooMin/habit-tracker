import java.time.LocalDate;

public class Habit {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;

    public Habit(String name, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getName() {
        return name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public boolean isInPeriod(LocalDate date) {
        return !(date.isBefore(startDate) || date.isAfter(endDate));
    }
}


