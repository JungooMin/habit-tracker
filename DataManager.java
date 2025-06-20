import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class DataManager {
    private static final String DATA_FILE = "habits_data.dat";
    
    // 데이터 저장
    public static void saveData(HabitTracker tracker) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            System.out.println("데이터 저장 시작...");
            System.out.println("저장할 습관 수: " + tracker.getHabits().size());
            
            // 습관 리스트를 직렬화 가능한 형태로 변환
            List<SerializableHabit> serializableHabits = new ArrayList<>();
            for (Habit habit : tracker.getHabits()) {
                serializableHabits.add(new SerializableHabit(
                    habit.getName(),
                    habit.getStartDate().toString(),
                    habit.getEndDate().toString()
                ));
                System.out.println("저장 중인 습관: " + habit.getName() + 
                    " (" + habit.getStartDate() + " ~ " + habit.getEndDate() + ")");
            }
            
            // 완료 기록도 직렬화 가능한 형태로 변환
            Map<String, Set<String>> serializableRecords = new HashMap<>();
            for (Habit habit : tracker.getHabits()) {
                Set<String> dateStrings = new HashSet<>();
                for (LocalDate date = habit.getStartDate(); !date.isAfter(habit.getEndDate()); date = date.plusDays(1)) {
                    if (tracker.isCompleted(habit.getName(), date)) {
                        dateStrings.add(date.toString());
                    }
                }
                serializableRecords.put(habit.getName(), dateStrings);
                System.out.println("습관 '" + habit.getName() + "'의 완료 기록 수: " + dateStrings.size());
            }
            
            oos.writeObject(serializableHabits);
            oos.writeObject(serializableRecords);
            
            System.out.println("데이터 저장 완료: " + DATA_FILE);
            
        } catch (IOException e) {
            System.err.println("데이터 저장 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 데이터 로드
    @SuppressWarnings("unchecked")
    public static HabitTracker loadData() {
        HabitTracker tracker = new HabitTracker();
        
        File file = new File(DATA_FILE);
        System.out.println("데이터 파일 경로: " + file.getAbsolutePath());
        System.out.println("데이터 파일 존재 여부: " + file.exists());
        
        if (!file.exists()) {
            System.out.println("데이터 파일이 없습니다. 빈 트래커를 반환합니다.");
            return tracker; // 파일이 없으면 빈 트래커 반환
        }
        
        System.out.println("데이터 파일 크기: " + file.length() + " bytes");
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            System.out.println("데이터 파일 읽기 시작...");
            
            List<SerializableHabit> serializableHabits = (List<SerializableHabit>) ois.readObject();
            Map<String, Set<String>> serializableRecords = (Map<String, Set<String>>) ois.readObject();
            
            System.out.println("파일에서 읽은 습관 수: " + serializableHabits.size());
            System.out.println("파일에서 읽은 기록 수: " + serializableRecords.size());
            
            // 습관 복원
            for (SerializableHabit sh : serializableHabits) {
                try {
                    Habit habit = new Habit(
                        sh.getName(),
                        LocalDate.parse(sh.getStartDate()),
                        LocalDate.parse(sh.getEndDate())
                    );
                    tracker.addHabit(habit);
                    System.out.println("복원된 습관: " + habit.getName() + 
                        " (" + habit.getStartDate() + " ~ " + habit.getEndDate() + ")");
                } catch (Exception e) {
                    System.err.println("습관 복원 중 오류: " + sh.getName() + " - " + e.getMessage());
                }
            }
            
            // 완료 기록 복원
            for (Map.Entry<String, Set<String>> entry : serializableRecords.entrySet()) {
                String habitName = entry.getKey();
                Set<String> dateStrings = entry.getValue();
                System.out.println("복원 중인 기록 - 습관: " + habitName + ", 기록 수: " + dateStrings.size());
                
                for (String dateString : dateStrings) {
                    try {
                        tracker.markCompleted(habitName, LocalDate.parse(dateString));
                    } catch (Exception e) {
                        System.err.println("완료 기록 복원 중 오류: " + habitName + " - " + dateString + " - " + e.getMessage());
                    }
                }
            }
            
            System.out.println("데이터 로드 완료. 최종 습관 수: " + tracker.getHabits().size());
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("데이터 로드 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            
            // 파일이 손상된 경우 백업 생성
            try {
                File backupFile = new File(DATA_FILE + ".backup." + System.currentTimeMillis());
                if (file.renameTo(backupFile)) {
                    System.out.println("손상된 파일을 백업으로 이동: " + backupFile.getName());
                }
            } catch (Exception backupError) {
                System.err.println("백업 생성 중 오류: " + backupError.getMessage());
            }
            
            // 빈 트래커 반환
            tracker = new HabitTracker();
        }
        
        return tracker;
    }
    
    // 만료된 습관 정리 및 알림 필요 여부 반환
    public static boolean cleanExpiredHabits(HabitTracker tracker) {
        LocalDate today = LocalDate.now();
        List<Habit> habitsToRemove = new ArrayList<>();
        
        System.out.println("만료된 습관 정리 시작...");
        
        for (Habit habit : tracker.getHabits()) {
            if (habit.getEndDate().isBefore(today)) {
                habitsToRemove.add(habit);
                System.out.println("만료된 습관 발견: " + habit.getName() + " (종료일: " + habit.getEndDate() + ")");
            }
        }
        
        for (Habit habit : habitsToRemove) {
            tracker.removeHabit(habit.getName());
            System.out.println("만료된 습관 제거됨: " + habit.getName());
        }
        
        System.out.println("만료된 습관 정리 완료. 제거된 습관 수: " + habitsToRemove.size());
        
        return !habitsToRemove.isEmpty(); // 삭제된 습관이 있으면 true 반환
    }
    
    // 직렬화 가능한 습관 클래스
    private static class SerializableHabit implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private String startDate;
        private String endDate;
        
        public SerializableHabit(String name, String startDate, String endDate) {
            this.name = name;
            this.startDate = startDate;
            this.endDate = endDate;
        }
        
        public String getName() { return name; }
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
    }
}