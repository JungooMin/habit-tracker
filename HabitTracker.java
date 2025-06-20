import java.time.LocalDate;
import java.util.*;

public class HabitTracker {
    private List<Habit> habits = new ArrayList<>();
    private Map<String, Set<LocalDate>> records = new HashMap<>();

    public void addHabit(Habit habit) {
        habits.add(habit);
        records.put(habit.getName(), new HashSet<>());
    }

    public boolean removeHabit(String name) {
        boolean removed = habits.removeIf(h -> h.getName().equals(name));
        records.remove(name);
        return removed;
    }

    public void markCompleted(String habitName, LocalDate date) {
        if (records.containsKey(habitName)) {
            records.get(habitName).add(date);
        }
    }
    
    // 완료 기록 제거 (체크박스 해제용)
    public void removeCompleted(String habitName, LocalDate date) {
        if (records.containsKey(habitName)) {
            records.get(habitName).remove(date);
        }
    }
    
    // 특정 날짜에 완료했는지 확인
    public boolean isCompleted(String habitName, LocalDate date) {
        if (!records.containsKey(habitName)) return false;
        return records.get(habitName).contains(date);
    }
    
    // 완료된 일수 반환
    public long getCompletedDaysCount(String habitName) {
        if (!records.containsKey(habitName)) return 0;
        return records.get(habitName).size();
    }

    public double getCompletionRate(String habitName) {
        Habit habit = habits.stream()
                .filter(h -> h.getName().equals(habitName))
                .findFirst()
                .orElse(null);
        if (habit == null) return 0.0;

        long totalDays = habit.getStartDate().datesUntil(habit.getEndDate().plusDays(1)).count();
        long completedDays = records.get(habitName).size();

        return (double) completedDays / totalDays * 100;
    }
    
    // 습관 날짜 수정
    public boolean updateHabitDates(String habitName, LocalDate newStartDate, LocalDate newEndDate) {
        Habit habit = habits.stream()
                .filter(h -> h.getName().equals(habitName))
                .findFirst()
                .orElse(null);
        
        if (habit == null) return false;
        
        // 기존 습관 제거하고 새로운 날짜로 습관 생성
        habits.removeIf(h -> h.getName().equals(habitName));
        habits.add(new Habit(habitName, newStartDate, newEndDate));
        
        // 기존 완료 기록은 유지 (새로운 기간에 맞지 않는 기록은 자동으로 무시됨)
        return true;
    }

    public void printHabitStatus() {
        for (Habit habit : habits) {
            String name = habit.getName();
            double rate = getCompletionRate(name);
            System.out.printf("습관: %s | 달성률: %.2f%% ", name, rate);
            if (rate >= 80) {
                System.out.println("훌륭해요! 계속 이어가요!");
            } else if (rate >= 50) {
                System.out.println("좋아요! 조금만 더 힘내요!");
            } else {
                System.out.println("지금이 시작해보는거 어떨까요요!");
            }
        }
    }
    
    public boolean isDateInHabitPeriod(String habitName, LocalDate date) {
        Habit habit = habits.stream()
                .filter(h -> h.getName().equals(habitName))
                .findFirst()
                .orElse(null);
        if (habit == null) return false;
        return habit.isInPeriod(date); 
    }
    
    public List<Habit> getHabits() {
        return habits;
    }
}
