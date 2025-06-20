import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Main extends JFrame {
    private HabitTracker tracker = new HabitTracker();
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);
    private String currentHabitName = "";
    private YearMonth currentCalendarMonth = YearMonth.now();
    
    // 화면 상수
    private static final String MAIN_SCREEN = "MAIN";
    private static final String ADD_SCREEN = "ADD";
    private static final String DETAIL_SCREEN = "DETAIL";
    private static final String EDIT_SCREEN = "EDIT";
    private static final String CALENDAR_SCREEN = "CALENDAR";
    private static final String STATS_SCREEN = "STATS";
    
    // 색상 상수
    private static final Color COMPLETED_COLOR = new Color(76, 175, 80);  // 녹색
    private static final Color INCOMPLETE_COLOR = new Color(244, 67, 54); // 빨간색
    private static final Color TODAY_COLOR = new Color(33, 150, 243);     // 파란색
    private static final Color DISABLED_COLOR = new Color(158, 158, 158); // 회색
    
    public Main() {
        setTitle("이번엔 진짜!");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // 데이터 로드
        loadDataOnStartup();
        
        // 각 화면 생성
        mainPanel.add(createMainScreen(), MAIN_SCREEN);
        mainPanel.add(createAddScreen(), ADD_SCREEN);
        mainPanel.add(createDetailScreen(), DETAIL_SCREEN);
        mainPanel.add(createEditScreen(), EDIT_SCREEN);
        mainPanel.add(createCalendarScreen(), CALENDAR_SCREEN);
        mainPanel.add(createStatsScreen(), STATS_SCREEN);
        
        add(mainPanel);
        
        // 데이터 로드 후 메인 화면 새로고침
        SwingUtilities.invokeLater(() -> {
            refreshMainScreen();
            cardLayout.show(mainPanel, MAIN_SCREEN);
        });
        
        // 프로그램 종료 시 데이터 저장
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                saveDataOnExit();
                System.exit(0);
            }
        });
        
        setVisible(true);
    }
    
    // 데이터 로드 메서드
    private void loadDataOnStartup() {
        try {
            tracker = DataManager.loadData();
            System.out.println("데이터 로드 시작...");
            System.out.println("로드된 습관 수: " + tracker.getHabits().size());
            
            // 로드된 습관들 출력 (디버깅용)
            for (Habit habit : tracker.getHabits()) {
                System.out.println("로드된 습관: " + habit.getName() + 
                    " (" + habit.getStartDate() + " ~ " + habit.getEndDate() + ")");
            }
            
            // 만료된 습관 정리
            boolean hasExpiredHabits = DataManager.cleanExpiredHabits(tracker);
            if (hasExpiredHabits) {
                System.out.println("만료된 습관들이 정리되었습니다.");
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, 
                        "일부 만료된 습관이 자동으로 정리되었습니다.", 
                        "알림", 
                        JOptionPane.INFORMATION_MESSAGE);
                });
            }
            System.out.println("데이터가 성공적으로 로드되었습니다.");
        } catch (Exception e) {
            System.err.println("데이터 로드 중 오류: " + e.getMessage());
            e.printStackTrace();
            tracker = new HabitTracker(); // 빈 트래커로 초기화
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, 
                    "데이터 로드 중 오류가 발생했습니다. 새로운 데이터로 시작합니다.\n오류: " + e.getMessage(), 
                    "오류", 
                    JOptionPane.WARNING_MESSAGE);
            });
        }
    }
    
    // 데이터 저장 메서드
    private void saveDataOnExit() {
        try {
            DataManager.saveData(tracker);
            System.out.println("데이터가 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            System.err.println("데이터 저장 중 오류: " + e.getMessage());
        }
    }
    
    // 날짜 선택용 콤보박스 패널 생성 메서드
    private JPanel createDateSelectionPanel() {
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        
        LocalDate today = LocalDate.now();
        
        JComboBox<Integer> yearCombo = new JComboBox<>();
        for (int year = today.getYear() - 5; year <= today.getYear() + 5; year++) {
            yearCombo.addItem(year);
        }
        yearCombo.setSelectedItem(today.getYear());
        
        JComboBox<Integer> monthCombo = new JComboBox<>();
        for (int month = 1; month <= 12; month++) {
            monthCombo.addItem(month);
        }
        monthCombo.setSelectedItem(today.getMonthValue());
        
        JComboBox<Integer> dayCombo = new JComboBox<>();
        updateDayCombo(dayCombo, today.getYear(), today.getMonthValue());
        dayCombo.setSelectedItem(today.getDayOfMonth());
        
        yearCombo.addActionListener(e -> {
            Integer selectedYear = (Integer) yearCombo.getSelectedItem();
            Integer selectedMonth = (Integer) monthCombo.getSelectedItem();
            if (selectedYear != null && selectedMonth != null) {
                updateDayCombo(dayCombo, selectedYear, selectedMonth);
            }
        });
        
        monthCombo.addActionListener(e -> {
            Integer selectedYear = (Integer) yearCombo.getSelectedItem();
            Integer selectedMonth = (Integer) monthCombo.getSelectedItem();
            if (selectedYear != null && selectedMonth != null) {
                updateDayCombo(dayCombo, selectedYear, selectedMonth);
            }
        });
        
        datePanel.add(yearCombo);
        datePanel.add(new JLabel("년"));
        datePanel.add(monthCombo);
        datePanel.add(new JLabel("월"));
        datePanel.add(dayCombo);
        datePanel.add(new JLabel("일"));
        
        return datePanel;
    }
    
    private JPanel createDateSelectionPanelWithDate(LocalDate date) {
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        
        JComboBox<Integer> yearCombo = new JComboBox<>();
        for (int year = LocalDate.now().getYear() - 5; year <= LocalDate.now().getYear() + 5; year++) {
            yearCombo.addItem(year);
        }
        yearCombo.setSelectedItem(date.getYear());
        
        JComboBox<Integer> monthCombo = new JComboBox<>();
        for (int month = 1; month <= 12; month++) {
            monthCombo.addItem(month);
        }
        monthCombo.setSelectedItem(date.getMonthValue());
        
        JComboBox<Integer> dayCombo = new JComboBox<>();
        updateDayCombo(dayCombo, date.getYear(), date.getMonthValue());
        dayCombo.setSelectedItem(date.getDayOfMonth());
        
        yearCombo.addActionListener(e -> {
            Integer selectedYear = (Integer) yearCombo.getSelectedItem();
            Integer selectedMonth = (Integer) monthCombo.getSelectedItem();
            if (selectedYear != null && selectedMonth != null) {
                updateDayCombo(dayCombo, selectedYear, selectedMonth);
            }
        });
        
        monthCombo.addActionListener(e -> {
            Integer selectedYear = (Integer) yearCombo.getSelectedItem();
            Integer selectedMonth = (Integer) monthCombo.getSelectedItem();
            if (selectedYear != null && selectedMonth != null) {
                updateDayCombo(dayCombo, selectedYear, selectedMonth);
            }
        });
        
        datePanel.add(yearCombo);
        datePanel.add(new JLabel("년"));
        datePanel.add(monthCombo);
        datePanel.add(new JLabel("월"));
        datePanel.add(dayCombo);
        datePanel.add(new JLabel("일"));
        
        return datePanel;
    }
    
    private void updateDayCombo(JComboBox<Integer> dayCombo, int year, int month) {
        Integer selectedDay = (Integer) dayCombo.getSelectedItem();
        dayCombo.removeAllItems();
        
        LocalDate date = LocalDate.of(year, month, 1);
        int lastDay = date.lengthOfMonth();
        
        for (int day = 1; day <= lastDay; day++) {
            dayCombo.addItem(day);
        }
        
        if (selectedDay != null && selectedDay <= lastDay) {
            dayCombo.setSelectedItem(selectedDay);
        } else {
            dayCombo.setSelectedItem(lastDay);
        }
    }
    
    private LocalDate getDateFromComboBoxes(JPanel datePanel) {
        Component[] components = datePanel.getComponents();
        JComboBox<Integer> yearCombo = (JComboBox<Integer>) components[0];
        JComboBox<Integer> monthCombo = (JComboBox<Integer>) components[2];
        JComboBox<Integer> dayCombo = (JComboBox<Integer>) components[4];
        
        Integer year = (Integer) yearCombo.getSelectedItem();
        Integer month = (Integer) monthCombo.getSelectedItem();
        Integer day = (Integer) dayCombo.getSelectedItem();
        
        if (year != null && month != null && day != null) {
            return LocalDate.of(year, month, day);
        }
        return LocalDate.now();
    }
    
    // 1. 메인 화면
    private JPanel createMainScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // 상단 제목과 네비게이션
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("이번엔 진짜!", JLabel.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        titleLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        // 네비게이션 버튼들
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton calendarBtn = new JButton("📅 달력");
        JButton statsBtn = new JButton("📊 통계");
        
        calendarBtn.addActionListener(e -> {
            refreshCalendarScreen();
            cardLayout.show(mainPanel, CALENDAR_SCREEN);
        });
        
        statsBtn.addActionListener(e -> {
            refreshStatsScreen();
            cardLayout.show(mainPanel, STATS_SCREEN);
        });
        
        navPanel.add(calendarBtn);
        navPanel.add(statsBtn);
        
        topPanel.add(titleLabel, BorderLayout.CENTER);
        topPanel.add(navPanel, BorderLayout.EAST);
        panel.add(topPanel, BorderLayout.NORTH);
        
        // 습관 목록 영역
        JPanel habitListPanel = new JPanel();
        habitListPanel.setLayout(new BoxLayout(habitListPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(habitListPanel);
        scrollPane.setPreferredSize(new Dimension(750, 400));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // 하단 버튼
        JButton addHabitButton = new JButton("+ 새로운 습관 등록");
        addHabitButton.setPreferredSize(new Dimension(180, 40));
        addHabitButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        addHabitButton.addActionListener(e -> {
            refreshAddScreen(); // 폼 초기화
            cardLayout.show(mainPanel, ADD_SCREEN);
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(addHabitButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void refreshMainScreen() {
        JPanel mainScreen = (JPanel) mainPanel.getComponent(0);
        JPanel topPanel = (JPanel) mainScreen.getComponent(0);
        JScrollPane scrollPane = (JScrollPane) mainScreen.getComponent(1);
        JPanel habitListPanel = (JPanel) scrollPane.getViewport().getView();
        
        habitListPanel.removeAll();
        
        List<Habit> habits = tracker.getHabits();
        if (habits.isEmpty()) {
            JLabel emptyLabel = new JLabel("등록된 습관이 없습니다.", JLabel.CENTER);
            emptyLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
            habitListPanel.add(emptyLabel);
        } else {
            for (Habit habit : habits) {
                JPanel habitPanel = createHabitPanel(habit);
                habitListPanel.add(habitPanel);
                habitListPanel.add(Box.createVerticalStrut(5));
            }
        }
        
        habitListPanel.revalidate();
        habitListPanel.repaint();
        
        // 변경사항 저장
        saveDataOnExit();
    }
    
    private JPanel createHabitPanel(Habit habit) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            new EmptyBorder(15, 20, 15, 20)
        ));
        panel.setPreferredSize(new Dimension(720, 70));
        panel.setMaximumSize(new Dimension(720, 70));
        
        // 왼쪽: 습관 정보
        String habitInfo = String.format("%s (%s ~ %s)", 
            habit.getName(), 
            habit.getStartDate().toString(),
            habit.getEndDate().toString());
        JLabel infoLabel = new JLabel(habitInfo);
        infoLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        
        // 오른쪽 패널: 달성률 + 체크박스
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        
        // 달성률
        double completionRate = tracker.getCompletionRate(habit.getName());
        long totalDays = habit.getStartDate().datesUntil(habit.getEndDate().plusDays(1)).count();
        long completedDays = tracker.getCompletedDaysCount(habit.getName());
        String rateInfo = String.format("[%d/%d] %.1f%%", completedDays, totalDays, completionRate);
        JLabel rateLabel = new JLabel(rateInfo);
        rateLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        
        // 달성률에 따른 색상 설정
        if (completionRate >= 80) {
            rateLabel.setForeground(COMPLETED_COLOR);
        } else if (completionRate >= 50) {
            rateLabel.setForeground(Color.ORANGE);
        } else {
            rateLabel.setForeground(INCOMPLETE_COLOR);
        }
        
        // 오늘 완료 체크박스
        JCheckBox todayCheckBox = new JCheckBox("오늘");
        LocalDate today = LocalDate.now();
        boolean isTodayCompleted = tracker.isCompleted(habit.getName(), today);
        boolean isTodayInPeriod = habit.isInPeriod(today);
        
        todayCheckBox.setSelected(isTodayCompleted);
        todayCheckBox.setEnabled(isTodayInPeriod);
        todayCheckBox.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        
        if (isTodayInPeriod) {
            todayCheckBox.addActionListener(e -> {
                if (todayCheckBox.isSelected()) {
                    tracker.markCompleted(habit.getName(), today);
                } else {
                    tracker.removeCompleted(habit.getName(), today);
                }
                refreshMainScreen();
            });
        }
        
        rightPanel.add(rateLabel);
        rightPanel.add(todayCheckBox);
        
        // 클릭 이벤트 (상세보기로 이동)
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                currentHabitName = habit.getName();
                refreshDetailScreen();
                cardLayout.show(mainPanel, DETAIL_SCREEN);
            }
        });
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        panel.add(infoLabel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    // 5. 달력 화면
    private JPanel createCalendarScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 상단: 제목과 네비게이션
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // 월 네비게이션
        JPanel monthNavPanel = new JPanel(new FlowLayout());
        JButton prevMonthBtn = new JButton("◀");
        JButton nextMonthBtn = new JButton("▶");
        JLabel monthLabel = new JLabel();
        monthLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        
        prevMonthBtn.addActionListener(e -> {
            currentCalendarMonth = currentCalendarMonth.minusMonths(1);
            refreshCalendarScreen();
        });
        
        nextMonthBtn.addActionListener(e -> {
            currentCalendarMonth = currentCalendarMonth.plusMonths(1);
            refreshCalendarScreen();
        });
        
        monthNavPanel.add(prevMonthBtn);
        monthNavPanel.add(monthLabel);
        monthNavPanel.add(nextMonthBtn);
        
        // 습관 선택 콤보박스
        JComboBox<String> habitCombo = new JComboBox<>();
        habitCombo.addItem("모든 습관");
        for (Habit habit : tracker.getHabits()) {
            habitCombo.addItem(habit.getName());
        }
        
        topPanel.add(monthNavPanel, BorderLayout.CENTER);
        topPanel.add(habitCombo, BorderLayout.EAST);
        panel.add(topPanel, BorderLayout.NORTH);
        
        // 달력 그리드
        JPanel calendarGrid = new JPanel(new GridLayout(7, 7, 2, 2));
        panel.add(calendarGrid, BorderLayout.CENTER);
        
        // 하단 버튼
        JButton homeBtn = new JButton("HOME");
        homeBtn.addActionListener(e -> cardLayout.show(mainPanel, MAIN_SCREEN));
        
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(homeBtn);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void refreshCalendarScreen() {
        JPanel calendarScreen = (JPanel) mainPanel.getComponent(4);
        JPanel topPanel = (JPanel) calendarScreen.getComponent(0);
        JPanel monthNavPanel = (JPanel) topPanel.getComponent(0);
        JLabel monthLabel = (JLabel) monthNavPanel.getComponent(1);
        JComboBox<String> habitCombo = (JComboBox<String>) topPanel.getComponent(1);
        JPanel calendarGrid = (JPanel) calendarScreen.getComponent(1);
        
        // 습관 콤보박스 업데이트 (새로운 습관이 추가되었을 수 있으므로)
        String selectedHabit = (String) habitCombo.getSelectedItem();
        habitCombo.removeAllItems();
        habitCombo.addItem("모든 습관");
        for (Habit habit : tracker.getHabits()) {
            habitCombo.addItem(habit.getName());
        }
        // 이전 선택 유지
        if (selectedHabit != null) {
            habitCombo.setSelectedItem(selectedHabit);
        }
        
        // 콤보박스 변경 이벤트 리스너 추가
        habitCombo.addActionListener(e -> refreshCalendarGrid());
        
        // 월 라벨 업데이트
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월");
        monthLabel.setText(currentCalendarMonth.format(formatter));
        
        refreshCalendarGrid();
    }
    
    private void refreshCalendarGrid() {
        JPanel calendarScreen = (JPanel) mainPanel.getComponent(4);
        JPanel topPanel = (JPanel) calendarScreen.getComponent(0);
        JComboBox<String> habitCombo = (JComboBox<String>) topPanel.getComponent(1);
        JPanel calendarGrid = (JPanel) calendarScreen.getComponent(1);
        
        // 달력 그리드 업데이트
        calendarGrid.removeAll();
        
        // 요일 헤더
        String[] dayNames = {"일", "월", "화", "수", "목", "금", "토"};
        for (String dayName : dayNames) {
            JLabel dayLabel = new JLabel(dayName, JLabel.CENTER);
            dayLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            dayLabel.setOpaque(true);
            dayLabel.setBackground(Color.LIGHT_GRAY);
            calendarGrid.add(dayLabel);
        }
        
        // 달력 날짜들
        LocalDate firstDay = currentCalendarMonth.atDay(1);
        int startDayOfWeek = firstDay.getDayOfWeek().getValue() % 7; // 일요일=0
        
        // 이전 달 빈 칸들
        for (int i = 0; i < startDayOfWeek; i++) {
            calendarGrid.add(new JLabel());
        }
        
        // 현재 달 날짜들
        for (int day = 1; day <= currentCalendarMonth.lengthOfMonth(); day++) {
            LocalDate date = currentCalendarMonth.atDay(day);
            JPanel dayPanel = createDayPanel(date, (String) habitCombo.getSelectedItem());
            calendarGrid.add(dayPanel);
        }
        
        calendarGrid.revalidate();
        calendarGrid.repaint();
    }
    
    private JPanel createDayPanel(LocalDate date, String selectedHabit) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        panel.setPreferredSize(new Dimension(80, 60));
        
        // 날짜 라벨
        JLabel dayLabel = new JLabel(String.valueOf(date.getDayOfMonth()), JLabel.CENTER);
        dayLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        
        // 오늘 날짜 표시
        LocalDate today = LocalDate.now();
        boolean isToday = date.equals(today);
        boolean isFutureDate = date.isAfter(today); // 미래 날짜 확인
        
        if (isToday) {
            dayLabel.setForeground(Color.WHITE);
            panel.setBackground(TODAY_COLOR);
        } else if (isFutureDate) {
            // 미래 날짜는 회색으로 표시
            panel.setBackground(new Color(245, 245, 245));
            dayLabel.setForeground(DISABLED_COLOR);
        } else {
            panel.setBackground(Color.WHITE);
        }
        
        panel.add(dayLabel, BorderLayout.NORTH);
        
        // 습관 완료 상태 표시
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
        
        if ("모든 습관".equals(selectedHabit)) {
            // 모든 습관의 완료 상태를 작은 점으로 표시
            int habitCount = 0;
            int completedCount = 0;
            
            for (Habit habit : tracker.getHabits()) {
                if (habit.isInPeriod(date)) {
                    habitCount++;
                    if (tracker.isCompleted(habit.getName(), date)) {
                        completedCount++;
                    }
                }
            }
            
            if (habitCount > 0) {
                // 전체 습관 중 완료된 비율에 따라 색상 결정
                String statusText = String.format("%d/%d", completedCount, habitCount);
                JLabel statusLabel = new JLabel(statusText);
                statusLabel.setFont(new Font("맑은 고딕", Font.BOLD, 10));
                
                if (completedCount == habitCount) {
                    statusLabel.setForeground(COMPLETED_COLOR); // 모두 완료
                } else if (completedCount > 0) {
                    statusLabel.setForeground(Color.ORANGE); // 일부 완료
                } else {
                    statusLabel.setForeground(INCOMPLETE_COLOR); // 아무것도 완료 안함
                }
                
                statusPanel.add(statusLabel);
            }
            
        } else {
            // 선택된 습관의 완료 상태 표시
            Habit selectedHabitObj = tracker.getHabits().stream()
                .filter(h -> h.getName().equals(selectedHabit))
                .findFirst().orElse(null);
            
            if (selectedHabitObj != null && selectedHabitObj.isInPeriod(date)) {
                JLabel statusLabel = new JLabel("●");
                statusLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 20));
                
                boolean isCompleted = tracker.isCompleted(selectedHabit, date);
                if (isCompleted) {
                    statusLabel.setForeground(COMPLETED_COLOR);
                    statusLabel.setText("✓");
                    statusLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
                } else {
                    statusLabel.setForeground(INCOMPLETE_COLOR);
                    statusLabel.setText("✗");
                    statusLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
                }
                statusPanel.add(statusLabel);
                
                // 달력 날짜 제한: 오늘 이전 날짜까지만 클릭 가능
                if (!isFutureDate) { // 미래 날짜가 아닌 경우에만 클릭 이벤트 추가
                    panel.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseClicked(java.awt.event.MouseEvent e) {
                            if (tracker.isCompleted(selectedHabit, date)) {
                                tracker.removeCompleted(selectedHabit, date);
                            } else {
                                tracker.markCompleted(selectedHabit, date);
                            }
                            refreshCalendarGrid();
                            refreshMainScreen(); // 메인 화면도 업데이트
                        }
                        
                        @Override
                        public void mouseEntered(java.awt.event.MouseEvent e) {
                            panel.setBackground(new Color(230, 230, 230));
                        }
                        
                        @Override
                        public void mouseExited(java.awt.event.MouseEvent e) {
                            if (isToday) {
                                panel.setBackground(TODAY_COLOR);
                            } else {
                                panel.setBackground(Color.WHITE);
                            }
                        }
                    });
                    panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
                
            } else if (selectedHabitObj != null) {
                // 습관 기간 밖의 날짜는 회색으로 표시
                JLabel statusLabel = new JLabel("—");
                statusLabel.setForeground(DISABLED_COLOR);
                statusLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
                statusPanel.add(statusLabel);
            }
        }
        
        panel.add(statusPanel, BorderLayout.CENTER);
        return panel;
    }
    
    // 6. 통계 화면
    private JPanel createStatsScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 제목
        JLabel titleLabel = new JLabel("📊 습관 통계", JLabel.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // 통계 내용
        JPanel statsContent = new JPanel();
        statsContent.setLayout(new BoxLayout(statsContent, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(statsContent);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // 하단 버튼
        JButton homeBtn = new JButton("HOME");
        homeBtn.addActionListener(e -> cardLayout.show(mainPanel, MAIN_SCREEN));
        
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(homeBtn);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void refreshStatsScreen() {
        JPanel statsScreen = (JPanel) mainPanel.getComponent(5);
        JScrollPane scrollPane = (JScrollPane) statsScreen.getComponent(1);
        JPanel statsContent = (JPanel) scrollPane.getViewport().getView();
        
        statsContent.removeAll();
        
        List<Habit> habits = tracker.getHabits();
        if (habits.isEmpty()) {
            JLabel emptyLabel = new JLabel("통계를 표시할 습관이 없습니다.", JLabel.CENTER);
            emptyLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
            statsContent.add(emptyLabel);
        } else {
            // 전체 요약
            JPanel summaryPanel = createSummaryPanel();
            statsContent.add(summaryPanel);
            statsContent.add(Box.createVerticalStrut(20));
            
            // 각 습관별 상세 통계
            for (Habit habit : habits) {
                JPanel habitStatsPanel = createHabitStatsPanel(habit);
                statsContent.add(habitStatsPanel);
                statsContent.add(Box.createVerticalStrut(15));
            }
        }
        
        statsContent.revalidate();
        statsContent.repaint();
    }
    
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("전체 요약"));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        
        StringBuilder summary = new StringBuilder();
        summary.append("총 등록된 습관: ").append(tracker.getHabits().size()).append("개\n");
        
        double totalRate = 0;
        int activeHabits = 0;
        
        for (Habit habit : tracker.getHabits()) {
            if (habit.isInPeriod(LocalDate.now())) {
                activeHabits++;
            }
            totalRate += tracker.getCompletionRate(habit.getName());
        }
        
        summary.append("현재 진행 중인 습관: ").append(activeHabits).append("개\n");
        if (!tracker.getHabits().isEmpty()) {
            summary.append("전체 평균 달성률: ").append(String.format("%.1f%%", totalRate / tracker.getHabits().size()));
        }
        
        JTextArea summaryArea = new JTextArea(summary.toString());
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        summaryArea.setBackground(panel.getBackground());
        
        panel.add(summaryArea, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createHabitStatsPanel(Habit habit) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(habit.getName()));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        
        // 기본 정보
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        
        double completionRate = tracker.getCompletionRate(habit.getName());
        long totalDays = habit.getStartDate().datesUntil(habit.getEndDate().plusDays(1)).count();
        long completedDays = tracker.getCompletedDaysCount(habit.getName());
        
        // 연속 달성 일수 계산
        int streakDays = calculateStreakDays(habit.getName());
        
        infoPanel.add(new JLabel("기간:"));
        infoPanel.add(new JLabel(habit.getStartDate() + " ~ " + habit.getEndDate()));
        
        infoPanel.add(new JLabel("달성률:"));
        JLabel rateLabel = new JLabel(String.format("%.1f%% (%d/%d일)", completionRate, completedDays, totalDays));
        if (completionRate >= 80) {
            rateLabel.setForeground(COMPLETED_COLOR);
        } else if (completionRate >= 50) {
            rateLabel.setForeground(Color.ORANGE);
        } else {
            rateLabel.setForeground(INCOMPLETE_COLOR);
        }
        infoPanel.add(rateLabel);
        
        infoPanel.add(new JLabel("연속 달성:"));
        infoPanel.add(new JLabel(streakDays + "일"));
        
        infoPanel.add(new JLabel("상태:"));
        String status = habit.isInPeriod(LocalDate.now()) ? "진행 중" : "완료됨";
        infoPanel.add(new JLabel(status));
        
        // 시각적 진행바
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue((int) completionRate);
        progressBar.setStringPainted(true);
        progressBar.setString(String.format("%.1f%%", completionRate));
        
        if (completionRate >= 80) {
            progressBar.setForeground(COMPLETED_COLOR);
        } else if (completionRate >= 50) {
            progressBar.setForeground(Color.ORANGE);
        } else {
            progressBar.setForeground(INCOMPLETE_COLOR);
        }
        
        progressPanel.add(new JLabel("진행률:"), BorderLayout.WEST);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(progressPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // 연속 달성 일수 계산
    private int calculateStreakDays(String habitName) {
        Habit habit = tracker.getHabits().stream()
            .filter(h -> h.getName().equals(habitName))
            .findFirst().orElse(null);
        
        if (habit == null) return 0;
        
        int streak = 0;
        LocalDate checkDate = LocalDate.now();
        
        // 오늘부터 거꾸로 확인하면서 연속 달성 일수 계산
        while (habit.isInPeriod(checkDate)) {
            if (tracker.isCompleted(habitName, checkDate)) {
                streak++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }
        }
        
        return streak;
    }
    
    // 2. 새로운 습관 등록 화면
    private JPanel createAddScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 제목
        JLabel titleLabel = new JLabel("새로운 습관 등록", JLabel.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // 입력 폼
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JTextField nameField = new JTextField(20);
        JPanel startDatePanel = createDateSelectionPanel();
        JPanel endDatePanel = createDateSelectionPanel();
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("습관이름 :"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("시작날짜 :"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(startDatePanel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("종료날짜 :"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(endDatePanel, gbc);
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        // 하단 버튼들과 메시지
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        JLabel messageLabel = new JLabel(" ", JLabel.CENTER);
        messageLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        bottomPanel.add(messageLabel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton registerButton = new JButton("등록하기");
        JButton homeButton = new JButton("HOME");
        
        registerButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                messageLabel.setText("습관 이름을 입력해주세요.");
                messageLabel.setForeground(Color.RED);
                return;
            }
            
            // 중복 습관명 검사
            boolean isDuplicate = tracker.getHabits().stream()
                .anyMatch(h -> h.getName().equals(name));
            if (isDuplicate) {
                messageLabel.setText("이미 존재하는 습관 이름입니다.");
                messageLabel.setForeground(Color.RED);
                return;
            }
            
            LocalDate start = getDateFromComboBoxes(startDatePanel);
            LocalDate end = getDateFromComboBoxes(endDatePanel);
            LocalDate today = LocalDate.now();
            
            // 시작날짜 검증
            if (start.isBefore(today)) {
                messageLabel.setText("시작날짜는 오늘 이후여야 합니다.");
                messageLabel.setForeground(Color.RED);
                return;
            }
            
            // 종료날짜 검증 (오늘 날짜는 불가)
            if (end.isBefore(start)) {
                messageLabel.setText("종료날짜는 시작날짜보다 늦어야 합니다.");
                messageLabel.setForeground(Color.RED);
                return;
            }
            
            if (end.equals(today)) {
                messageLabel.setText("종료날짜는 오늘 이후여야 합니다.");
                messageLabel.setForeground(Color.RED);
                return;
            }
            
            if (end.isBefore(today)) {
                messageLabel.setText("종료날짜는 오늘 이후여야 합니다.");
                messageLabel.setForeground(Color.RED);
                return;
            }
            
            tracker.addHabit(new Habit(name, start, end));
            
            // 등록 완료 후 홈 화면으로 이동
            refreshMainScreen();
            cardLayout.show(mainPanel, MAIN_SCREEN);
            
            // 성공 메시지를 잠시 보여주기
            JOptionPane.showMessageDialog(Main.this, 
                "습관이 성공적으로 등록되었습니다!", 
                "등록 완료", 
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        homeButton.addActionListener(e -> cardLayout.show(mainPanel, MAIN_SCREEN));
        
        buttonPanel.add(registerButton);
        buttonPanel.add(homeButton);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // 폼 초기화 메서드
    private void refreshAddScreen() {
        // 기존 화면 제거 후 새로 생성하여 폼 초기화
        mainPanel.remove(1);
        mainPanel.add(createAddScreen(), ADD_SCREEN, 1);
        mainPanel.revalidate();
        mainPanel.repaint();
    }
    
    // 3. 습관 상세 보기 화면
    private JPanel createDetailScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 제목
        JLabel titleLabel = new JLabel("습관 상세보기", JLabel.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // 상세 정보 영역
        JTextArea detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        detailArea.setBackground(panel.getBackground());
        JScrollPane scrollPane = new JScrollPane(detailArea);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // 하단 버튼
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton editButton = new JButton("날짜 변경");
        JButton deleteButton = new JButton("습관 삭제");
        JButton calendarButton = new JButton("📅 달력보기");
        JButton homeButton = new JButton("HOME");
        
        editButton.addActionListener(e -> {
            refreshEditScreen();
            cardLayout.show(mainPanel, EDIT_SCREEN);
        });
        
        deleteButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                this, 
                "정말로 이 습관을 삭제하시겠습니까?", 
                "습관 삭제 확인", 
                JOptionPane.YES_NO_OPTION
            );
            
            if (result == JOptionPane.YES_OPTION) {
                tracker.removeHabit(currentHabitName);
                refreshMainScreen();
                cardLayout.show(mainPanel, MAIN_SCREEN);
            }
        });
        
        calendarButton.addActionListener(e -> {
            refreshCalendarScreen();
            cardLayout.show(mainPanel, CALENDAR_SCREEN);
        });
        
        homeButton.addActionListener(e -> cardLayout.show(mainPanel, MAIN_SCREEN));
        
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(calendarButton);
        buttonPanel.add(homeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void refreshDetailScreen() {
        JPanel detailScreen = (JPanel) mainPanel.getComponent(2);
        JScrollPane scrollPane = (JScrollPane) detailScreen.getComponent(1);
        JTextArea detailArea = (JTextArea) scrollPane.getViewport().getView();
        
        Habit habit = tracker.getHabits().stream()
            .filter(h -> h.getName().equals(currentHabitName))
            .findFirst()
            .orElse(null);
        
        if (habit != null) {
            double rate = tracker.getCompletionRate(currentHabitName);
            int streakDays = calculateStreakDays(currentHabitName);
            long totalDays = habit.getStartDate().datesUntil(habit.getEndDate().plusDays(1)).count();
            long completedDays = tracker.getCompletedDaysCount(currentHabitName);
            
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("습관: %s\n", habit.getName()));
            sb.append(String.format("기간: %s ~ %s\n", habit.getStartDate(), habit.getEndDate()));
            sb.append(String.format("전체 일수: %d일\n", totalDays));
            sb.append(String.format("완료한 일수: %d일\n", completedDays));
            sb.append(String.format("달성률: %.1f%%\n", rate));
            sb.append(String.format("연속 달성: %d일\n\n", streakDays));
            
            if (rate >= 80) {
                sb.append("훌륭해요! 계속 이어가세요!");
            } else if (rate >= 50) {
                sb.append("좋아요! 조금만 더 힘내세요!");
            } else {
                sb.append("지금이 시작할 때예요!");
            }
            
            detailArea.setText(sb.toString());
        }
    }
    
    // 4. 날짜 변경 화면
    private JPanel createEditScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 제목
        JLabel titleLabel = new JLabel("날짜 변경", JLabel.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // 입력 폼
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JPanel startDatePanel = createDateSelectionPanel();
        JPanel endDatePanel = createDateSelectionPanel();
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("시작날짜 :"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(startDatePanel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("종료날짜 :"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(endDatePanel, gbc);
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        // 하단 버튼들과 메시지
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        JLabel messageLabel = new JLabel(" ", JLabel.CENTER);
        messageLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        bottomPanel.add(messageLabel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton changeButton = new JButton("변경하기");
        JButton homeButton = new JButton("HOME");
        
        changeButton.addActionListener(e -> {
            LocalDate newStart = getDateFromComboBoxes(startDatePanel);
            LocalDate newEnd = getDateFromComboBoxes(endDatePanel);
            LocalDate today = LocalDate.now();
            
            // 시작날짜 검증
            if (newStart.isBefore(today)) {
                messageLabel.setText("시작날짜는 오늘 이후여야 합니다.");
                messageLabel.setForeground(Color.RED);
                return;
            }
            
            // 종료날짜 검증
            if (newEnd.isBefore(newStart)) {
                messageLabel.setText("종료날짜는 시작날짜보다 늦어야 합니다.");
                messageLabel.setForeground(Color.RED);
                return;
            }
            
            if (newEnd.equals(today)) {
                messageLabel.setText("종료날짜는 오늘 이후여야 합니다.");
                messageLabel.setForeground(Color.RED);
                return;
            }
            
            if (newEnd.isBefore(today)) {
                messageLabel.setText("종료날짜는 오늘 이후여야 합니다.");
                messageLabel.setForeground(Color.RED);
                return;
            }
            
            tracker.updateHabitDates(currentHabitName, newStart, newEnd);
            messageLabel.setText("변경되었습니다!");
            messageLabel.setForeground(Color.BLUE);
            
            refreshMainScreen();
        });
        
        homeButton.addActionListener(e -> cardLayout.show(mainPanel, MAIN_SCREEN));
        
        buttonPanel.add(changeButton);
        buttonPanel.add(homeButton);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void refreshEditScreen() {
        Habit habit = tracker.getHabits().stream()
            .filter(h -> h.getName().equals(currentHabitName))
            .findFirst()
            .orElse(null);
        
        if (habit != null) {
            mainPanel.remove(3);
            mainPanel.add(createEditScreenWithData(habit), EDIT_SCREEN, 3);
        }
    }
    
    private JPanel createEditScreenWithData(Habit habit) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 제목
        JLabel titleLabel = new JLabel("날짜 변경", JLabel.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // 입력 폼
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JPanel startDatePanel = createDateSelectionPanelWithDate(habit.getStartDate());
        JPanel endDatePanel = createDateSelectionPanelWithDate(habit.getEndDate());
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("시작날짜 :"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(startDatePanel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("종료날짜 :"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(endDatePanel, gbc);
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        // 하단 버튼들과 메시지
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        JLabel messageLabel = new JLabel(" ", JLabel.CENTER);
        messageLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        bottomPanel.add(messageLabel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton changeButton = new JButton("변경하기");
        JButton homeButton = new JButton("HOME");
        
        changeButton.addActionListener(e -> {
            LocalDate newStart = getDateFromComboBoxes(startDatePanel);
            LocalDate newEnd = getDateFromComboBoxes(endDatePanel);
            LocalDate today = LocalDate.now();
            
            // 시작날짜 검증
            if (newStart.isBefore(today)) {
                messageLabel.setText("시작날짜는 오늘 이후여야 합니다.");
                messageLabel.setForeground(Color.RED);
                return;
            }
            
            // 종료날짜 검증
            if (newEnd.isBefore(newStart)) {
                messageLabel.setText("종료날짜는 시작날짜보다 늦어야 합니다.");
                messageLabel.setForeground(Color.RED);
                return;
            }
            
            if (newEnd.equals(today)) {
                messageLabel.setText("종료날짜는 오늘 이후여야 합니다.");
                messageLabel.setForeground(Color.RED);
                return;
            }
            
            if (newEnd.isBefore(today)) {
                messageLabel.setText("종료날짜는 오늘 이후여야 합니다.");
                messageLabel.setForeground(Color.RED);
                return;
            }
            
            tracker.updateHabitDates(currentHabitName, newStart, newEnd);
            messageLabel.setText("변경되었습니다!");
            messageLabel.setForeground(Color.BLUE);
            
            refreshMainScreen();
        });
        
        homeButton.addActionListener(e -> cardLayout.show(mainPanel, MAIN_SCREEN));
        
        buttonPanel.add(changeButton);
        buttonPanel.add(homeButton);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main());
    }
}
