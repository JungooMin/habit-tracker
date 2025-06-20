import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class DateSelector extends JPanel {
    private JComboBox<Integer> yearCombo;
    private JComboBox<Integer> monthCombo;
    private JComboBox<Integer> dayCombo;
    
    public DateSelector() {
        this(LocalDate.now());
    }
    
    public DateSelector(LocalDate initialDate) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        initializeComponents(initialDate);
        setupListeners();
    }
    
    private void initializeComponents(LocalDate initialDate) {
        // 년도 콤보박스 (현재년도 기준 -10년 ~ +10년)
        int currentYear = LocalDate.now().getYear();
        Integer[] years = new Integer[21];
        for (int i = 0; i < 21; i++) {
            years[i] = currentYear - 10 + i;
        }
        yearCombo = new JComboBox<>(years);
        yearCombo.setSelectedItem(initialDate.getYear());
        
        // 월 콤보박스 (1~12)
        Integer[] months = new Integer[12];
        for (int i = 0; i < 12; i++) {
            months[i] = i + 1;
        }
        monthCombo = new JComboBox<>(months);
        monthCombo.setSelectedItem(initialDate.getMonthValue());
        
        // 일 콤보박스 (초기값 설정)
        dayCombo = new JComboBox<>();
        updateDayCombo();
        dayCombo.setSelectedItem(initialDate.getDayOfMonth());
        
        // 라벨과 함께 추가
        add(new JLabel("년: "));
        add(yearCombo);
        add(new JLabel("월: "));
        add(monthCombo);
        add(new JLabel("일: "));
        add(dayCombo);
    }
    
    private void setupListeners() {
        // 년도나 월이 변경되면 일 콤보박스 업데이트
        yearCombo.addActionListener(e -> updateDayCombo());
        monthCombo.addActionListener(e -> updateDayCombo());
    }
    
    private void updateDayCombo() {
        if (yearCombo.getSelectedItem() == null || monthCombo.getSelectedItem() == null) {
            return;
        }
        
        int year = (Integer) yearCombo.getSelectedItem();
        int month = (Integer) monthCombo.getSelectedItem();
        
        // 현재 선택된 일 저장
        Integer currentDay = (Integer) dayCombo.getSelectedItem();
        
        // 해당 년월의 마지막 날 계산
        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();
        
        // 일 콤보박스 업데이트
        dayCombo.removeAllItems();
        for (int day = 1; day <= daysInMonth; day++) {
            dayCombo.addItem(day);
        }
        
        // 이전에 선택했던 일이 유효하면 다시 선택
        if (currentDay != null && currentDay <= daysInMonth) {
            dayCombo.setSelectedItem(currentDay);
        }
    }
    
    public LocalDate getSelectedDate() {
        if (yearCombo.getSelectedItem() == null || 
            monthCombo.getSelectedItem() == null || 
            dayCombo.getSelectedItem() == null) {
            return LocalDate.now();
        }
        
        return LocalDate.of(
            (Integer) yearCombo.getSelectedItem(),
            (Integer) monthCombo.getSelectedItem(),
            (Integer) dayCombo.getSelectedItem()
        );
    }
    
    public void setSelectedDate(LocalDate date) {
        yearCombo.setSelectedItem(date.getYear());
        monthCombo.setSelectedItem(date.getMonthValue());
        updateDayCombo();
        dayCombo.setSelectedItem(date.getDayOfMonth());
    }
    
    public boolean isValidSelection() {
        return yearCombo.getSelectedItem() != null && 
               monthCombo.getSelectedItem() != null && 
               dayCombo.getSelectedItem() != null;
    }
}