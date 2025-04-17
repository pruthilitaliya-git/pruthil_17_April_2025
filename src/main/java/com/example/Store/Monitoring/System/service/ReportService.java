package com.example.Store.Monitoring.System.service;

import com.example.Store.Monitoring.System.model.*;
import com.example.Store.Monitoring.System.repository.ReportRepository;
import com.example.Store.Monitoring.System.repository.StoreScheduleRepository;
import com.example.Store.Monitoring.System.repository.StoreStatusRepository;
import com.example.Store.Monitoring.System.repository.StoreTimezonesRepository;
import com.example.Store.Monitoring.System.util.DateTimeConverter;
import com.opencsv.CSVWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.io.File;
import java.io.FileInputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.concurrent.CompletableFuture;

import java.io.FileWriter;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    ReportRepository reportRepository;

    @Autowired
    StoreStatusRepository storeStatusRepository;

    @Autowired
    StoreScheduleRepository storeScheduleRepository;

    @Autowired
    StoreTimezonesRepository storeTimezonesRepository;

//    Creates a new report record with status "Running" and triggers report generation asynchronously.
    public String triggerReport() {
        Report report = new Report();
        report.setStatus("Running");
        report.setCreatedAt(ZonedDateTime.now());
        reportRepository.save(report);

//        Asynchronous report generation
        generateReport(report.getId());
        return  report.getId();
    }

//    Generates a CSV report containing store uptime/downtime metrics for different timeframes.
    @Async
    private CompletableFuture<Void> generateReport(String reportId) {

        try{
//            Get latest timestamp across all store statuses
            String maximumTime = storeStatusRepository.findAll()
                    .stream()
                    .map(StoreStatus::getTimestampUtc)
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .orElse(ZonedDateTime.now().toString());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS z", Locale.ENGLISH);
            ZonedDateTime maxTime = ZonedDateTime.parse(maximumTime, formatter);

//            Define time ranges for calculations
            ZonedDateTime hourAgo = maxTime.minusHours(1);
            ZonedDateTime dayAgo = maxTime.minusDays(1);
            ZonedDateTime weekAgo = maxTime.minusWeeks(1);

//            Organize data by store ID
            List<StoreStatus> allStatuses = storeStatusRepository.findAll();
            Map<String, List<StoreStatus>> statusesByStore = allStatuses.stream()
                    .collect(Collectors.groupingBy(StoreStatus::getStoreId));

            List<StoreSchedule> allHours = storeScheduleRepository.findAll();
            Map<String, List<StoreSchedule>> hoursByStore = allHours.stream()
                    .collect(Collectors.groupingBy(s -> s.getId().getStoreId()));

            List<StoreTimezones> allZones = storeTimezonesRepository.findAll();
            Map<String, String> timezoneByStore = allZones.stream()
                    .collect(Collectors.toMap(StoreTimezones::getStoreId, StoreTimezones::getTimezonesStr));

//            Prepare CSV content
            List<String[]> csvRows = new ArrayList<>();
            csvRows.add(new String[]{"store_id", "uptime_last_hour(in minutes)", "uptime_last_day(in hours)", "uptime_last_week(in hours)", "downtime_last_hour(in minutes)", "downtime_last_day(in hours)", "downtime_last_week(in hours)"});

//            Compute uptime/downtime metrics per store
            for(String storeId: statusesByStore.keySet()){
                List<StoreStatus> statusList = statusesByStore.get(storeId);
                String zone = timezoneByStore.getOrDefault(storeId, "America/Chicago");
                List<StoreSchedule> schedules = hoursByStore.getOrDefault(storeId, get24By7());

                double[] metrics = calculateUpAndDowntime(storeId, statusList, schedules, zone, hourAgo, dayAgo, weekAgo, maxTime);

                csvRows.add(new String[]{
                        String.valueOf(storeId),
                        String.format("%.2f", metrics[0]),
                        String.format("%.2f", metrics[1]),
                        String.format("%.2f", metrics[2]),
                        String.format("%.2f", metrics[3]),
                        String.format("%.2f", metrics[4]),
                        String.format("%.2f", metrics[5])
                });

            }

//            Write CSV to file
            String path = "report_" + reportId + ".csv";
            try (CSVWriter csvWriter = new CSVWriter(new FileWriter(path))){
                csvWriter.writeAll(csvRows);
            }

//            Update report status to complete
            Report report = reportRepository.findById(reportId).orElseThrow();
            report.setStatus("Complete");
            report.setFilePath(path);
            report.setCreatedAt(ZonedDateTime.now());
            reportRepository.save(report);

        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        return CompletableFuture.completedFuture(null);
    }

//    Calculates the uptime and downtime for the store in hourly, daily, and weekly intervals.
    private double[] calculateUpAndDowntime(String storeId, List<StoreStatus> storeStatusList, List<StoreSchedule> storeScheduleList, String zone, ZonedDateTime hourAgo, ZonedDateTime dayAgo, ZonedDateTime weekAgo, ZonedDateTime maxTime){

        Map<ZonedDateTime, String> statusMap = new TreeMap<>();

//        Parse timestamps and build status map
        for(StoreStatus s: storeStatusList){

            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd HH:mm:ss")
                    .optionalStart()
                    .appendFraction(ChronoField.NANO_OF_SECOND, 0, 6, true)
                    .optionalEnd()
                    .appendLiteral(' ')
                    .appendZoneText(TextStyle.SHORT) // or FULL if it says "Coordinated Universal Time"
                    .toFormatter(Locale.ENGLISH);

            ZonedDateTime tempVar = ZonedDateTime.parse(s.getTimestampUtc(), formatter);

            statusMap.put(tempVar, s.getStatus().toString());
        }

        double uptimeHour = 0, uptimeDay = 0, uptimeWeek = 0;
        double downtimeHour = 0, downtimeDay = 0, downtimeWeek = 0;

        ZonedDateTime[] ranges = new ZonedDateTime[]{hourAgo, dayAgo, weekAgo};
        double []uptime = new double[3];
        double []downtime = new double[3];

//        Simulate store status in time windows
        for(int i=0; i<3; i++){
            ZonedDateTime start = ranges[i];
            ZonedDateTime end = maxTime;
            Duration step = Duration.ofMinutes(5);
            ZonedDateTime cursor = start;

            while(!cursor.isAfter(end)){
                ZonedDateTime localCursor = DateTimeConverter.toZonedTime(cursor, zone);
                int dayOfWeek = localCursor.getDayOfWeek().getValue() % 7;
                LocalTime localTime = localCursor.toLocalTime();


                boolean isOpen = storeScheduleList.stream().anyMatch(bh ->
                        bh.getId().getDayOfWeek() == dayOfWeek &&
                                DateTimeConverter.isWithin(localTime, bh.getId().getStartTimeLocal(), bh.getEndTimeLocal() )
                );

                if(isOpen){
                    String state = getStatusAtTime(statusMap, cursor);
                    if("active".equalsIgnoreCase(state)){
                        uptime[i] +=  step.toMinutes() / 60.0;
                    }
                    else{
                        downtime[i] += step.toMinutes() / 60.0;
                    }
                }
                cursor = cursor.plus(step);

            }
        }

//        Return array: uptimeHour, uptimeDay, uptimeWeek, downtimeHour, downtimeDay, downtimeWeek
        return new double[]{uptime[0] * 60, uptime[1], uptime[2], downtime[0] * 60, downtime[1], downtime[2]};
    }

//    Returns the latest known store status before or at the given time.
    private String getStatusAtTime(Map<ZonedDateTime, String> statusMap, ZonedDateTime time){
        return statusMap.entrySet().stream()
                .filter(e -> !e.getKey().isAfter(time))
                .max(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue).orElse("inactive");
    }

//    Returns default 24x7 schedule for stores without specified hours.
    private  List<StoreSchedule> get24By7(){
        List<StoreSchedule> list = new ArrayList<>();
        for(int i=0; i<7; i++){
            StoreScheduleId storeScheduleId = new StoreScheduleId();
            storeScheduleId.setDayOfWeek((byte) i);
            storeScheduleId.setStartTimeLocal(LocalTime.parse("00:00:00"));

            StoreSchedule storeSchedule = new StoreSchedule();
            storeSchedule.setId(storeScheduleId);
            storeSchedule.setEndTimeLocal(LocalTime.parse("23:59:59"));
            list.add(storeSchedule);
        }
        return  list;
    }

//    Provides the CSV report file for download once it is generated.
    public ResponseEntity<?> getReport(String reportId) {
        if(reportId == null){
            return  ResponseEntity.notFound().build();
        }
        Optional<Report> reportOptional = reportRepository.findById(reportId);
        if(reportOptional.isPresent()){
            Report report = reportOptional.get();

            if (!"Complete".equals(report.getStatus())) {
                return ResponseEntity.ok("Running");
            }
            try {
                File file = new File(report.getFilePath());
                InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                        .body(resource);
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body("Error reading report");
            }
        }else{
            return ResponseEntity.notFound().build();
        }

    }
}
