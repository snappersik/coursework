package com.almetpt.coursework.bookclub.service;
    
import com.almetpt.coursework.bookclub.dto.ChartDataDTO;
import com.almetpt.coursework.bookclub.dto.EventStatisticsDTO;
import com.almetpt.coursework.bookclub.model.Event;
import com.almetpt.coursework.bookclub.repository.EventRepository;
// import com.almetpt.coursework.bookclub.repository.OrderRepository; // Not directly used in user's snippet for this service
// import com.almetpt.coursework.bookclub.repository.UserRepository; // Not directly used in user's snippet for this service
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
    
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
    
@Service
public class StatisticsService {
    
    private final JdbcTemplate jdbcTemplate;
    private final EventRepository eventRepository;
        
    public StatisticsService(JdbcTemplate jdbcTemplate, EventRepository eventRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.eventRepository = eventRepository;
    }
        
    public ChartDataDTO getUsersRegistrationChart() {
        String sql = "SELECT TO_CHAR(created_when, 'YYYY-MM') as month, COUNT(*) as count " +
                "FROM users " +
                "WHERE is_deleted = false " +
                "GROUP BY TO_CHAR(created_when, 'YYYY-MM') " +
                "ORDER BY month ASC " +
                "LIMIT 12";
            
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
            
        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();
            
        for (Map<String, Object> row : rows) {
            labels.add((String) row.get("month"));
            data.add(((Number) row.get("count")).longValue());
        }
            
        return new ChartDataDTO(labels, data);
    }
        
    public ChartDataDTO getOrdersChart() {
        String sql = "SELECT TO_CHAR(created_when, 'YYYY-MM') as month, COUNT(*) as count " +
                "FROM orders " +
                "WHERE is_deleted = false " +
                "GROUP BY TO_CHAR(created_when, 'YYYY-MM') " +
                "ORDER BY month ASC " +
                "LIMIT 12";
            
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
            
        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();
            
        for (Map<String, Object> row : rows) {
            labels.add((String) row.get("month"));
            data.add(((Number) row.get("count")).longValue());
        }
            
        return new ChartDataDTO(labels, data);
    }
        
    public List<EventStatisticsDTO> getEventsStatistics() {
        List<Event> events = eventRepository.findAllByIsDeletedFalse();
        List<EventStatisticsDTO> result = new ArrayList<>();
            
        for (Event event : events) {
            int approvedApplications = eventRepository.countApprovedApplications(event.getId());
            int totalApplications = eventRepository.countTotalApplications(event.getId());
            double fillPercentage = event.getMaxParticipants() != null && event.getMaxParticipants() > 0 ?
                (double) approvedApplications / event.getMaxParticipants() * 100 : 0;
                
            EventStatisticsDTO dto = new EventStatisticsDTO(
                event.getId(),
                event.getTitle(),
                event.getEventType(),
                event.getMaxParticipants() != null ? event.getMaxParticipants() : 0,
                approvedApplications,
                totalApplications,
                fillPercentage
            );
                
            result.add(dto);
        }
            
        return result;
    }
}
