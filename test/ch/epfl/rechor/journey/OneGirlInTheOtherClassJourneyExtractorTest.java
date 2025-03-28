package ch.epfl.rechor.journey;

import ch.epfl.rechor.journey.*;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.time.LocalDate;
import java.util.List;


public class OneGirlInTheOtherClassJourneyExtractorTest {

    public Profile readProfile(TimeTable timeTable, LocalDate date, int arrStationId) throws IOException {
        Path path = Path.of("test/ch/epfl/rechor/journey/profile_2025-03-18_11486.txt");

        try (BufferedReader r = Files.newBufferedReader(path)) {
            Profile.Builder profileB = new Profile.Builder(timeTable, date, arrStationId);
            int stationId = -1;
            String line;
            while ((line = r.readLine()) != null) {
                stationId += 1;

                if (line.isEmpty()) {
                    continue;
                }

                ParetoFront.Builder frontB = new ParetoFront.Builder();

                for (String t : line.split(",")) {
                    frontB.add(Long.parseLong(t, 16));
                }

                profileB.setForStation(stationId, frontB);
            }
            return profileB.build();
        }
    }

    @Test
    void exampleProf() throws IOException{
        TimeTable t = FileTimeTable.in(Path.of("timetable"));
        LocalDate date = LocalDate.of(2025, 3, 18);
        Profile p = readProfile(t, date, 11486);

        List<Journey> js = JourneyExtractor.journeys(p, 7872);
        String j = JourneyIcalConverter.toIcalendar(js.get(32));
        System.out.println(j);
    }

    @Test
    void exampleWithFirstLegStep() throws IOException{
        TimeTable t = FileTimeTable.in(Path.of("timetable"));
        LocalDate date = LocalDate.of(2025, 3, 18);
        Profile p = readProfile(t, date, 11486);

//        int idEcublens = 0;
//        for (int i = 0; i < 10000; i ++) {
//            if (p.timeTable().stations().name(i).equals("Ecublens VD, EPFL (bus)")) {
//                idEcublens = i;
//            }
//        }
        List<Journey> js = JourneyExtractor.journeys(p, 7874);
        String j = JourneyIcalConverter.toIcalendar(js.get(32));
        System.out.println(j);
    }

    @Test
    void exampleWithLastLegStep() throws IOException{
        TimeTable t = FileTimeTable.in(Path.of("timetable"));
        LocalDate date = LocalDate.of(2025, 3, 18);
        Profile p = readProfile(t, date, 11486);

//        int Moleson = 0;
//        for (int i = 0; i < 33000; i ++) {
//            if (p.timeTable().stations().name(i).equals("Moléson-sur-Gruyères")) {
//                Moleson = i;
//            }
//        }
//        System.out.println(Moleson);
        List<Journey> js = JourneyExtractor.journeys(p, 18601);
        String j = JourneyIcalConverter.toIcalendar(js.get(19));
        System.out.println(j);
    }

    @Test
    void exampleWithLastLegStep2() throws IOException{
        TimeTable t = FileTimeTable.in(Path.of("timetable"));
        LocalDate date = LocalDate.of(2025, 3, 18);
        Profile p = readProfile(t, date, 11486);

        List<Journey> js = JourneyExtractor.journeys(p, 19002);
        String j = JourneyIcalConverter.toIcalendar(js.get(23));
        System.out.println(j);
    }

    @Test
    void exampleWithLastLegStep3() throws IOException{
        TimeTable t = FileTimeTable.in(Path.of("timetable"));
        LocalDate date = LocalDate.of(2025, 3, 18);
        Profile p = readProfile(t, date, 11486);

        List<Journey> js = JourneyExtractor.journeys(p, 13341);
        String j = JourneyIcalConverter.toIcalendar(js.get(5));
        System.out.println(j);
    }

    @Test
    void exampleWithLimit() throws IOException{
        TimeTable t = FileTimeTable.in(Path.of("timetable"));
        LocalDate date = LocalDate.of(2025, 3, 18);
        Profile p = readProfile(t, date, 11486);

        List<Journey> js = JourneyExtractor.journeys(p, 11486);
        String j = JourneyIcalConverter.toIcalendar(js.get(32));
        System.out.println(j);
    }

    @Test
    void exampleWithLongTrip() throws IOException{
        TimeTable t = FileTimeTable.in(Path.of("timetable"));
        LocalDate date = LocalDate.of(2025, 3, 18);
        Profile p = readProfile(t, date, 11486);

        List<Journey> js = JourneyExtractor.journeys(p, 25465);
        String j = JourneyIcalConverter.toIcalendar(js.get(3));
        System.out.println(j);
    }
}